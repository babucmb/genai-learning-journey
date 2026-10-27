const { generateStructured, generateText } = require('../llm/llmProvider');

/**
 * Research Agent: Summarizes current technology updates, libraries, and radar recommendations.
 */
async function generateTechRadar({ domain = 'Machine Learning & LLMs', userProfile = {} }) {
  const prompt = `
You are the Research Agent in PathAI.
Analyze emerging engineering trends, new models, and library shifts in: "${domain}".
Candidate Role: ${userProfile.targetRole || 'AI/ML Engineer'}

Provide:
1. Executive summary of recent breakthrough patterns.
2. 3-4 Radar items categorized by "ADOPT", "TRIAL", "ASSESS", or "HOLD".
3. Impact on interview expectations.

Return JSON:
{
  "domain": "${domain}",
  "executiveSummary": "string",
  "radarItems": [
    {
      "technology": "string",
      "ring": "ADOPT | TRIAL | ASSESS | HOLD",
      "rationale": "string",
      "practicalApplication": "string"
    }
  ],
  "interviewTakeaways": ["string"]
}`;

  try {
    const result = await generateStructured({
      prompt,
      systemPrompt: 'You are PathAI Tech Radar Research Agent. Provide cutting-edge industry insights in valid JSON.',
      temperature: 0.4
    });

    return {
      live: true,
      fallback: false,
      provider: result.provider,
      model: result.model,
      requestId: result.requestId,
      radar: result.structuredData
    };
  } catch (err) {
    console.warn('[ResearchAgent] Live generation error:', err.message);
    return {
      live: false,
      fallback: true,
      reason: err.message,
      provider: 'fallback_engine',
      model: 'template_fallback',
      radar: {
        domain,
        executiveSummary: 'Current state of modern engineering emphasizes smaller distilled models, structured outputs, and agentic workflows with strict verification.',
        radarItems: [
          {
            technology: 'Structured LLM Outputs & Schema Enforcers',
            ring: 'ADOPT',
            rationale: 'Guarantees reliable downstream parsing without brittle regex.',
            practicalApplication: 'Backend agent orchestrators and tool calling.'
          },
          {
            technology: 'RAG Hybrid Search (Dense + BM25)',
            ring: 'ADOPT',
            rationale: 'Substantially reduces hallucination across domain retrieval.',
            practicalApplication: 'Document intelligence and grounded tutoring.'
          }
        ],
        interviewTakeaways: ['Explain trade-offs between prompt engineering and fine-tuning', 'Understand KV cache and context window constraints']
      }
    };
  }
}

module.exports = {
  generateTechRadar
};
