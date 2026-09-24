const axios = require('axios');
const crypto = require('crypto');

class GeminiProvider {
  constructor(config = {}) {
    this.apiKey = config.apiKey || process.env.GEMINI_API_KEY || '';
    this.model = config.model || process.env.GEMINI_MODEL || 'gemini-2.5-flash';
    this.maxRetries = config.maxRetries || 3;
    this.timeoutMs = config.timeoutMs || 25000;
  }

  async executeWithRetry(apiCallFn) {
    let attempt = 0;
    let lastError = null;

    while (attempt < this.maxRetries) {
      try {
        return await apiCallFn();
      } catch (err) {
        lastError = err;
        attempt++;
        const status = err.response?.status;
        // Retry on 429 (rate-limit) or 5xx (transient server errors)
        const isRetryable = status === 429 || (status >= 500 && status < 600) || err.code === 'ECONNABORTED';

        if (!isRetryable || attempt >= this.maxRetries) {
          break;
        }

        const backoffMs = Math.min(1000 * Math.pow(2, attempt) + Math.random() * 300, 8000);
        console.warn(`[GeminiProvider] Attempt ${attempt} failed (${status || err.message}). Retrying in ${Math.round(backoffMs)}ms...`);
        await new Promise(resolve => setTimeout(resolve, backoffMs));
      }
    }

    const message = lastError.response?.data?.error?.message || lastError.message;
    const status = lastError.response?.status || 500;
    const error = new Error(`Gemini Provider Error (${status}): ${message}`);
    error.status = status;
    error.code = lastError.response?.data?.error?.status || 'GEMINI_ERROR';
    throw error;
  }

  async generateText({ prompt, systemPrompt = '', temperature = 0.7, maxOutputTokens = 2048 }) {
    if (!this.apiKey) {
      throw new Error('GEMINI_API_KEY is not configured on the server.');
    }

    const requestId = crypto.randomUUID();
    const startTime = Date.now();

    const contents = [];
    if (systemPrompt) {
      contents.push({
        role: 'user',
        parts: [{ text: `[System Instructions]\n${systemPrompt}` }]
      });
      contents.push({
        role: 'model',
        parts: [{ text: 'Understood. I will strictly follow these instructions.' }]
      });
    }
    contents.push({
      role: 'user',
      parts: [{ text: prompt }]
    });

    const body = {
      contents,
      generationConfig: {
        temperature,
        maxOutputTokens
      }
    };

    const url = `https://generativelanguage.googleapis.com/v1beta/models/${this.model}:generateContent?key=${this.apiKey}`;

    const response = await this.executeWithRetry(async () => {
      return await axios.post(url, body, {
        timeout: this.timeoutMs,
        headers: { 'Content-Type': 'application/json' }
      });
    });

    const latencyMs = Date.now() - startTime;
    const candidate = response.data?.candidates?.[0];
    const text = candidate?.content?.parts?.map(p => p.text).join('') || '';
    const usageMetadata = response.data?.usageMetadata || {};

    return {
      text,
      structuredData: null,
      provider: 'gemini',
      model: this.model,
      usage: {
        promptTokens: usageMetadata.promptTokenCount || 0,
        completionTokens: usageMetadata.candidatesTokenCount || 0,
        totalTokens: usageMetadata.totalTokenCount || 0
      },
      latencyMs,
      requestId
    };
  }

  async generateStructured({ prompt, systemPrompt = '', schema = null, temperature = 0.2 }) {
    const formattedPrompt = `${prompt}\n\nIMPORTANT: You must respond ONLY with a valid JSON object or array. Do not include markdown formatting, backticks, or preamble.`;
    
    const result = await this.generateText({
      prompt: formattedPrompt,
      systemPrompt: systemPrompt ? `${systemPrompt}\nOutput valid JSON only.` : 'You are an accurate JSON generator. Output valid JSON only.',
      temperature
    });

    let cleaned = result.text.trim();
    if (cleaned.startsWith('```json')) {
      cleaned = cleaned.replace(/^```json\s*/, '').replace(/\s*```$/, '');
    } else if (cleaned.startsWith('```')) {
      cleaned = cleaned.replace(/^```\s*/, '').replace(/\s*```$/, '');
    }

    try {
      result.structuredData = JSON.parse(cleaned);
    } catch (parseErr) {
      throw new Error(`Failed to parse structured JSON response from Gemini: ${parseErr.message}\nRaw Text: ${result.text.slice(0, 200)}`);
    }

    return result;
  }

  async streamText({ prompt, systemPrompt = '', onChunk, onComplete, onError }) {
    if (!this.apiKey) {
      const err = new Error('GEMINI_API_KEY is not configured on the server.');
      if (onError) onError(err);
      throw err;
    }

    const requestId = crypto.randomUUID();
    const startTime = Date.now();
    let accumulatedText = '';

    try {
      const url = `https://generativelanguage.googleapis.com/v1beta/models/${this.model}:streamGenerateContent?alt=sse&key=${this.apiKey}`;
      const contents = [];
      if (systemPrompt) {
        contents.push({ role: 'user', parts: [{ text: `[System Instructions]\n${systemPrompt}` }] });
        contents.push({ role: 'model', parts: [{ text: 'Understood.' }] });
      }
      contents.push({ role: 'user', parts: [{ text: prompt }] });

      const response = await axios.post(
        url,
        { contents },
        {
          responseType: 'stream',
          timeout: this.timeoutMs,
          headers: { 'Content-Type': 'application/json' }
        }
      );

      response.data.on('data', chunk => {
        const lines = chunk.toString().split('\n');
        for (const line of lines) {
          if (line.startsWith('data: ')) {
            try {
              const parsed = JSON.parse(line.slice(6));
              const partText = parsed.candidates?.[0]?.content?.parts?.[0]?.text || '';
              if (partText) {
                accumulatedText += partText;
                if (onChunk) onChunk(partText);
              }
            } catch (e) {
              // Ignore non-JSON or partial SSE lines
            }
          }
        }
      });

      response.data.on('end', () => {
        const latencyMs = Date.now() - startTime;
        if (onComplete) {
          onComplete({
            text: accumulatedText,
            provider: 'gemini',
            model: this.model,
            latencyMs,
            requestId
          });
        }
      });

      response.data.on('error', (streamErr) => {
        if (onError) onError(streamErr);
      });
    } catch (err) {
      if (onError) onError(err);
      throw err;
    }
  }

  async getProviderHealth() {
    if (!this.apiKey) {
      return {
        provider: 'gemini',
        status: 'unconfigured',
        model: this.model,
        message: 'GEMINI_API_KEY is missing.'
      };
    }

    try {
      const url = `https://generativelanguage.googleapis.com/v1beta/models/${this.model}?key=${this.apiKey}`;
      await axios.get(url, { timeout: 6000 });
      return {
        provider: 'gemini',
        status: 'healthy',
        model: this.model,
        message: 'Connected to Gemini API.'
      };
    } catch (err) {
      return {
        provider: 'gemini',
        status: 'degraded',
        model: this.model,
        message: err.message
      };
    }
  }
}

module.exports = GeminiProvider;
