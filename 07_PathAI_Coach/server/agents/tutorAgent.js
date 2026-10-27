const { generateText, generateStructured } = require('../llm/llmProvider');
const VerifierAgent = require('./verifierAgent');

/**
 * Tutor Agent: Explains module concepts with intuition, code examples, and check questions.
 */
async function explainModuleConcept({
  moduleId,
  title,
  description,
  skills = [],
  content = '',
  userQuestion = ''
}) {
  const prompt = `
You are the senior Technical Tutor Agent in PathAI.
Current Module: "${title}" (ID: ${moduleId})
Module Overview: ${description}
Core Skills: ${Array.isArray(skills) ? skills.join(', ') : skills}
Module Reference Content:
${content ? content.slice(0, 1500) : 'Standard curriculum syllabus.'}

Candidate Question:
"${userQuestion || `Explain the foundational intuition, architecture, and production implementation of ${title}`}"

Instructions:
1. Explain clearly with crisp technical depth and intuitive analogies.
2. Provide a clean, minimal code pattern with comments.
3. Highlight common real-world edge cases or pitfalls.
4. Conclude with 1 quick check-for-understanding question.
Tone: Mentoring, encouraging, technically rigorous.`;

  try {
    const result = await generateText({
      prompt,
      systemPrompt: 'You are PathAI Senior Technical Tutor. Explain concepts with crystal-clear code and architecture insights.',
      temperature: 0.6
    });

    return {
      live: true,
      fallback: false,
      provider: result.provider,
      model: result.model,
      requestId: result.requestId,
      explanation: result.text,
      latencyMs: result.latencyMs
    };
  } catch (err) {
    console.warn('[TutorAgent] Live generation error:', err.message);
    return {
      live: false,
      fallback: true,
      reason: err.message,
      provider: 'fallback_engine',
      model: 'template_fallback',
      requestId: `fallback_${Date.now()}`,
      explanation: `### 🎓 Tutor Agent (Offline/Template Content): ${title || moduleId}\n\n**Key Competencies:**\nFocus on mastering ${Array.isArray(skills) ? skills.slice(0, 3).join(', ') : 'foundational patterns'}.\n\n**Standard Implementation Pattern:**\n\`\`\`python\n# Practical snippet for ${title || moduleId}\ndef execute_pattern():\n    \"\"\"Standard modular implementation.\"\"\"\n    return True\n\`\`\`\n\n*Note: This explanation was generated from cached syllabus content due to provider unavailability (${err.message}).*`
    };
  }
}

/**
 * Tutor Agent: Generates multiple-choice quiz questions for a module.
 */
async function generateModuleQuiz({
  moduleId,
  title,
  description,
  skills = [],
  count = 3
}) {
  const prompt = `
Generate ${count} high-quality, technically rigorous multiple-choice questions for the learning module:
Title: "${title}" (ID: ${moduleId})
Skills: ${Array.isArray(skills) ? skills.join(', ') : skills}
Description: ${description}

Format: Return a JSON array of objects with the exact schema:
[
  {
    "id": "q1",
    "question": "Clear technical question prompt",
    "options": [
      "Correct answer option",
      "Plausible distractor 1",
      "Plausible distractor 2",
      "Plausible distractor 3"
    ],
    "correctIndex": 0,
    "explanation": "Detailed explanation of why this answer is correct and others are incorrect."
  }
]`;

  try {
    const result = await generateStructured({
      prompt,
      systemPrompt: 'You are PathAI Quiz Authoring Agent. Generate strict JSON quiz questions with accurate answers and rich explanations.',
      temperature: 0.3
    });

    const rawQuestions = Array.isArray(result.structuredData) ? result.structuredData : (result.structuredData?.questions || []);
    const { valid, validatedQuestions, errors } = VerifierAgent.validateQuizQuestions(rawQuestions);

    if (!valid || validatedQuestions.length === 0) {
      throw new Error(`Verifier rejected quiz: ${errors.join('; ')}`);
    }

    return {
      live: true,
      fallback: false,
      provider: result.provider,
      model: result.model,
      requestId: result.requestId,
      questions: validatedQuestions,
      latencyMs: result.latencyMs
    };
  } catch (err) {
    console.warn('[TutorAgent] Live quiz generation failed:', err.message);
    // Verified fallback questions
    const fallbackQuestions = [
      {
        id: `${moduleId}_fallback_q1`,
        question: `What is the primary architectural goal of ${title}?`,
        options: [
          `Implement maintainable, scalable solutions utilizing ${Array.isArray(skills) && skills[0] ? skills[0] : 'core patterns'}`,
          'Bypass all unit and integration testing',
          'Ignore time and memory complexity',
          'Hardcode static values in production'
        ],
        correctIndex: 0,
        explanation: 'Engineered systems prioritize maintainability, correctness, and clean abstraction.'
      },
      {
        id: `${moduleId}_fallback_q2`,
        question: `Which skill is essential when evaluating ${title}?`,
        options: [
          Array.isArray(skills) && skills[1] ? skills[1] : 'Error handling & efficiency',
          'Ignoring memory leak risks',
          'Suppression of all application logs',
          'Removing type annotations'
        ],
        correctIndex: 0,
        explanation: 'Robust error handling and operational efficiency are critical in production.'
      }
    ];

    return {
      live: false,
      fallback: true,
      reason: err.message,
      provider: 'fallback_engine',
      model: 'template_fallback',
      requestId: `fallback_quiz_${Date.now()}`,
      questions: fallbackQuestions
    };
  }
}

module.exports = {
  explainModuleConcept,
  generateModuleQuiz
};
