const { generateText } = require('../llm/llmProvider');

/**
 * Multi-Agent Chat Dispatcher:
 * Handles contextual conversations for Coach, Tutor, Project, and Research agents.
 */
async function processAgentChat({
  agentType = 'coach',
  message,
  history = [],
  currentModuleInfo = null,
  userProfile = {}
}) {
  const normalizedType = (agentType || 'coach').toLowerCase();

  let systemPrompt = '';
  let agentName = 'AI Agent';

  switch (normalizedType) {
    case 'tutor':
      agentName = 'Tutor Agent';
      systemPrompt = `You are the Tutor Agent in PathAI, an elite Senior Technical Tutor in AI, ML, Python, and Software Systems.
Candidate Target Role: ${userProfile.targetRole || 'AI/ML Engineer'}
${currentModuleInfo ? `Current Active Curriculum Module: "${currentModuleInfo.title || currentModuleInfo.id}"
Module Concepts: ${Array.isArray(currentModuleInfo.skills) ? currentModuleInfo.skills.join(', ') : ''}
Reference Summary: ${currentModuleInfo.description || ''}` : 'General Technical Coaching Mode.'}

Behavior:
1. Explain technical concepts deeply yet accessibly.
2. Provide concise code examples when appropriate.
3. If discussing the current module, ground your answers in the module skills.
4. Keep answers focused, encouraging, and pedagogically clear.`;
      break;

    case 'project':
      agentName = 'Project Agent';
      systemPrompt = `You are the Project Agent in PathAI, a Senior Software Architect and Tech Lead.
Candidate Target Role: ${userProfile.targetRole || 'AI/ML Engineer'}
Focus: Portfolio architecture, code reviews, milestone definitions, and acceptance criteria.
Behavior:
1. Advise on production-grade architecture, data pipelines, and test coverage.
2. Recommend clean API structures, database design, and deployment pipelines.
3. Be direct, pragmatic, and review-oriented.`;
      break;

    case 'research':
      agentName = 'Research Agent';
      systemPrompt = `You are the Research Agent in PathAI, a Frontier AI Research Engineer and Tech Scout.
Candidate Target Role: ${userProfile.targetRole || 'AI/ML Engineer'}
Focus: Frontier models, research papers, emerging libraries (PyTorch, Transformers, LangChain, vLLM), and technology radar trends.
Behavior:
1. Provide accurate technical summaries of modern developments.
2. Discuss trade-offs, benchmarks, and production relevance.`;
      break;

    case 'coach':
    default:
      agentName = 'Coach Agent';
      systemPrompt = `You are the Coach Agent in PathAI, an Executive Career & Preparation Coach.
Candidate Profile:
- Target Role: ${userProfile.targetRole || 'AI/ML Engineer'}
- Target Tier: ${userProfile.targetCompanyTier || 'Tier 1 Tech'}
- Deadline: ${userProfile.deadlineWeeks || 12} weeks
- Daily Study: ${userProfile.hoursPerDay || 4} hours

Behavior:
1. Keep the candidate accountable, focused, and motivated.
2. Recommend daily sprint focus and interview prep strategies.
3. Offer empathetic yet rigorous guidance.`;
      break;
  }

  // Build conversational context
  const recentHistory = (history || []).slice(-6);
  let conversationPrompt = '';
  if (recentHistory.length > 0) {
    conversationPrompt += 'Previous conversation:\n';
    recentHistory.forEach(h => {
      const roleName = h.role === 'user' ? 'Candidate' : agentName;
      conversationPrompt += `${roleName}: ${h.content}\n`;
    });
    conversationPrompt += '\n';
  }
  conversationPrompt += `Candidate: ${message}\n${agentName}:`;

  try {
    const result = await generateText({
      prompt: conversationPrompt,
      systemPrompt,
      temperature: 0.7
    });

    return {
      live: true,
      fallback: false,
      agentType: normalizedType,
      agentName,
      provider: result.provider,
      model: result.model,
      requestId: result.requestId,
      reply: result.text,
      latencyMs: result.latencyMs
    };
  } catch (err) {
    console.warn(`[MultiAgentChat] Live chat error for ${normalizedType}:`, err.message);
    return {
      live: false,
      fallback: true,
      reason: err.message,
      agentType: normalizedType,
      agentName,
      provider: 'fallback_engine',
      model: 'template_fallback',
      requestId: `fallback_chat_${Date.now()}`,
      reply: `[Offline Mode] ${agentName}: I received your message: "${message}". Live AI provider is currently unreachable (${err.message}). Focusing on current milestones is recommended.`
    };
  }
}

module.exports = {
  processAgentChat
};
