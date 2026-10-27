const { generateStructured } = require('../llm/llmProvider');

/**
 * Project Agent: Creates project milestones, architectural outlines, and review criteria.
 */
async function generateProjectPlan({ uid, projectTitle, objective, techStack = [], userProfile = {} }) {
  const prompt = `
You are the Project Agent in PathAI.
Create a comprehensive portfolio project implementation plan.

Project Title: "${projectTitle || 'AI Career Assistant'}"
Core Objective: "${objective || 'Production-grade full-stack LLM application'}"
Tech Stack: ${Array.isArray(techStack) ? techStack.join(', ') : techStack}
Target Role: ${userProfile.targetRole || 'AI/ML Engineer'}

Provide:
1. Architectural overview.
2. 3-4 structured milestones with acceptance criteria.
3. Key engineering trade-offs.

Return JSON:
{
  "projectTitle": "string",
  "architectureOverview": "string",
  "milestones": [
    {
      "milestoneNumber": 1,
      "name": "string",
      "deliverables": ["string"],
      "acceptanceCriteria": "string"
    }
  ],
  "verificationChecklist": ["string"]
}`;

  try {
    const result = await generateStructured({
      prompt,
      systemPrompt: 'You are PathAI Project Agent. Output valid JSON only.',
      temperature: 0.3
    });

    return {
      live: true,
      fallback: false,
      provider: result.provider,
      model: result.model,
      requestId: result.requestId,
      plan: result.structuredData
    };
  } catch (err) {
    console.warn('[ProjectAgent] Live generation error:', err.message);
    return {
      live: false,
      fallback: true,
      reason: err.message,
      provider: 'fallback_engine',
      model: 'template_fallback',
      plan: {
        projectTitle: projectTitle || 'Production AI Portfolio Project',
        architectureOverview: 'Modular microservice / clean architecture with isolated LLM client layer and persistence.',
        milestones: [
          {
            milestoneNumber: 1,
            name: 'Core Domain & Schema Setup',
            deliverables: ['Database schemas', 'Repository interfaces', 'Unit tests'],
            acceptanceCriteria: 'Clean compilation and passing domain tests.'
          },
          {
            milestoneNumber: 2,
            name: 'API & Service Integration',
            deliverables: ['Controller endpoints', 'LLM Provider integration', 'Error handling'],
            acceptanceCriteria: 'End-to-end integration verified.'
          }
        ],
        verificationChecklist: ['Error handling', 'Type safety', 'Secure credential separation']
      }
    };
  }
}

module.exports = {
  generateProjectPlan
};
