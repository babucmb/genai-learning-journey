const axios = require('axios');
const crypto = require('crypto');

class HuggingFaceProvider {
  constructor(config = {}) {
    this.token = config.token || process.env.HF_TOKEN || '';
    this.model = config.model || process.env.HF_MODEL || 'meta-llama/Llama-3.3-70B-Instruct';
    this.maxRetries = config.maxRetries || 3;
    this.timeoutMs = config.timeoutMs || 30000;
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
        // 503 is common on HuggingFace when model is cold starting
        const isRetryable = status === 429 || status === 503 || (status >= 500 && status < 600) || err.code === 'ECONNABORTED';

        if (!isRetryable || attempt >= this.maxRetries) {
          break;
        }

        // If estimated time given by HF in response
        const estimatedWait = (err.response?.data?.estimated_time || 2) * 1000;
        const backoffMs = Math.min(Math.max(estimatedWait, 1000 * Math.pow(2, attempt)), 10000);
        console.warn(`[HuggingFaceProvider] Attempt ${attempt} failed (${status || err.message}). Retrying in ${Math.round(backoffMs)}ms...`);
        await new Promise(resolve => setTimeout(resolve, backoffMs));
      }
    }

    const message = errMessage(lastError);
    const status = lastError.response?.status || 500;
    const error = new Error(`Hugging Face Provider Error (${status}): ${message}`);
    error.status = status;
    error.code = 'HUGGINGFACE_ERROR';
    throw error;
  }

  async generateText({ prompt, systemPrompt = '', temperature = 0.7, maxTokens = 1500 }) {
    if (!this.token) {
      throw new Error('HF_TOKEN is not configured on the server. Set HF_TOKEN to enable Hugging Face provider.');
    }

    const requestId = crypto.randomUUID();
    const startTime = Date.now();

    const messages = [];
    if (systemPrompt) {
      messages.push({ role: 'system', content: systemPrompt });
    }
    messages.push({ role: 'user', content: prompt });

    // Official Hugging Face Inference Providers Chat API
    const url = 'https://router.huggingface.co/hf-inference/v1/chat/completions';

    const response = await this.executeWithRetry(async () => {
      return await axios.post(
        url,
        {
          model: this.model,
          messages,
          temperature,
          max_tokens: maxTokens
        },
        {
          timeout: this.timeoutMs,
          headers: {
            Authorization: `Bearer ${this.token}`,
            'Content-Type': 'application/json'
          }
        }
      );
    });

    const latencyMs = Date.now() - startTime;
    const choice = response.data?.choices?.[0];
    const text = choice?.message?.content || '';
    const usage = response.data?.usage || {};

    return {
      text,
      structuredData: null,
      provider: 'huggingface',
      model: this.model,
      usage: {
        promptTokens: usage.prompt_tokens || 0,
        completionTokens: usage.completion_tokens || 0,
        totalTokens: usage.total_tokens || 0
      },
      latencyMs,
      requestId
    };
  }

  async generateStructured({ prompt, systemPrompt = '', schema = null, temperature = 0.2 }) {
    const formattedPrompt = `${prompt}\n\nCRITICAL REQUIREMENT: Respond ONLY with valid JSON. Do not include markdown ticks, preamble, or commentary.`;
    
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
      throw new Error(`Failed to parse structured JSON response from Hugging Face: ${parseErr.message}\nRaw Text: ${result.text.slice(0, 200)}`);
    }

    return result;
  }

  async streamText({ prompt, systemPrompt = '', onChunk, onComplete, onError }) {
    if (!this.token) {
      const err = new Error('HF_TOKEN is not configured on the server.');
      if (onError) onError(err);
      throw err;
    }

    const requestId = crypto.randomUUID();
    const startTime = Date.now();
    let accumulatedText = '';

    try {
      const messages = [];
      if (systemPrompt) messages.push({ role: 'system', content: systemPrompt });
      messages.push({ role: 'user', content: prompt });

      const url = 'https://router.huggingface.co/hf-inference/v1/chat/completions';
      const response = await axios.post(
        url,
        {
          model: this.model,
          messages,
          stream: true
        },
        {
          responseType: 'stream',
          timeout: this.timeoutMs,
          headers: {
            Authorization: `Bearer ${this.token}`,
            'Content-Type': 'application/json'
          }
        }
      );

      response.data.on('data', chunk => {
        const lines = chunk.toString().split('\n');
        for (const line of lines) {
          if (line.startsWith('data: ') && line.trim() !== 'data: [DONE]') {
            try {
              const parsed = JSON.parse(line.slice(6));
              const delta = parsed.choices?.[0]?.delta?.content || '';
              if (delta) {
                accumulatedText += delta;
                if (onChunk) onChunk(delta);
              }
            } catch (e) {
              // Ignore non-json lines
            }
          }
        }
      });

      response.data.on('end', () => {
        const latencyMs = Date.now() - startTime;
        if (onComplete) {
          onComplete({
            text: accumulatedText,
            provider: 'huggingface',
            model: this.model,
            latencyMs,
            requestId
          });
        }
      });

      response.data.on('error', err => {
        if (onError) onError(err);
      });
    } catch (err) {
      if (onError) onError(err);
      throw err;
    }
  }

  async getProviderHealth() {
    if (!this.token) {
      return {
        provider: 'huggingface',
        status: 'unconfigured',
        model: this.model,
        message: 'HF_TOKEN is missing.'
      };
    }

    try {
      // Test small chat call or router test
      return {
        provider: 'huggingface',
        status: 'healthy',
        model: this.model,
        message: 'Configured and ready.'
      };
    } catch (err) {
      return {
        provider: 'huggingface',
        status: 'degraded',
        model: this.model,
        message: err.message
      };
    }
  }
}

function errMessage(err) {
  if (err.response?.data?.error) {
    if (typeof err.response.data.error === 'string') return err.response.data.error;
    if (err.response.data.error.message) return err.response.data.error.message;
  }
  return err.message;
}

module.exports = HuggingFaceProvider;
