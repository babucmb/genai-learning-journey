const { generateStructured } = require('../llm/llmProvider');
const VerifierAgent = require('./verifierAgent');
const { db } = require('../firebaseAdmin');
const crypto = require('crypto');

/**
 * Coach / Planner Agent:
 * - Generates tailored daily learning tasks based on user capacity, module, and deadline.
 * - Reschedules overdue tasks.
 * - The ONLY agent permitted to write learning tasks to Firestore.
 */

async function generateDailyLearningTasks({
  uid,
  userProfile = {},
  currentModule = {},
  existingTasks = [],
  allowFallback = true
}) {
  if (!uid) {
    throw new Error('User UID is required to generate daily tasks.');
  }

  const hoursPerDay = Number(userProfile.hoursPerDay) || 4;
  const maxMinutes = hoursPerDay * 60;
  const todayDateStr = new Date().toISOString().split('T')[0];

  const prompt = `
You are the Coach & Planner Agent in PathAI.
Design today's atomic, measurable daily sprint tasks for the candidate.

Candidate Profile:
- Target Role: ${userProfile.targetRole || 'AI/ML Engineer'}
- Daily Study Budget: ${hoursPerDay} hours (${maxMinutes} minutes maximum)
- Deadline: ${userProfile.deadlineWeeks || 12} weeks
- Skills: ${userProfile.skillsText || 'Python, Git, Machine Learning'}

Current Active Module:
- Title: "${currentModule.title || 'Core Foundations'}" (ID: ${currentModule.id || 'mod_1'})
- Description: ${currentModule.description || 'Core technical concepts.'}
- Core Skills: ${Array.isArray(currentModule.skills) ? currentModule.skills.join(', ') : (currentModule.skills || 'Core fundamentals')}

Existing tasks scheduled for today (${existingTasks.length}):
${existingTasks.map(t => `- ${t.title}`).join('\n') || 'None'}

Requirement:
1. Generate between 2 to 4 distinct, highly actionable tasks.
2. The SUM of "estimate_minutes" of all generated tasks MUST be less than or equal to ${maxMinutes} minutes.
3. Each task must have a specific, measurable deliverable (e.g., "Implement X", "Read section Y and summarize", "Solve 3 problems on Z").
4. Do NOT duplicate any existing tasks.

Return JSON array:
[
  {
    "title": "Actionable task title",
    "description": "Clear step-by-step deliverable and acceptance criteria",
    "estimate_minutes": 45
  }
]`;

  let tasksToWrite = [];
  let isLive = true;
  let fallbackReason = null;
  let providerUsed = 'gemini';
  let modelUsed = 'default';
  let requestId = crypto.randomUUID();

  try {
    const result = await generateStructured({
      prompt,
      systemPrompt: 'You are PathAI Coach & Planner Agent. Generate atomic, measurable daily sprint tasks adhering strictly to time constraints.',
      temperature: 0.3
    });

    providerUsed = result.provider;
    modelUsed = result.model;
    requestId = result.requestId;

    const rawTasks = Array.isArray(result.structuredData)
      ? result.structuredData
      : (result.structuredData?.tasks || []);

    // Verifier Agent checks time limits, duplicate detection, and schema
    const validation = VerifierAgent.validateDailyTasks({
      tasks: rawTasks,
      dailyHoursBudget: hoursPerDay,
      existingTasks
    });

    if (!validation.valid || validation.validatedTasks.length === 0) {
      throw new Error(`Verifier rejected tasks: ${validation.errors.join('; ')}`);
    }

    tasksToWrite = validation.validatedTasks.map((t, idx) => ({
      id: `task_${Date.now()}_${idx + 1}`,
      uid,
      module_id: currentModule.id || 'current_module',
      title: t.title,
      description: t.description,
      estimate_minutes: t.estimate_minutes,
      status: 'todo',
      scheduled_date: todayDateStr,
      source: 'llm_generated',
      created_at: new Date().toISOString()
    }));
  } catch (err) {
    console.warn('[CoachAgent] Live task generation failed:', err.message);
    if (!allowFallback) {
      throw err;
    }
    isLive = false;
    fallbackReason = err.message;
    providerUsed = 'fallback_engine';
    modelUsed = 'template_fallback';

    // Verifier-compliant fallback tasks
    const fallbackTemplates = [
      {
        title: `Deep-dive study: ${currentModule.title || 'Core Module Concepts'}`,
        description: `Review module architecture, notes, and foundational implementation rules for ${currentModule.title || 'the syllabus'}.`,
        estimate_minutes: 45
      },
      {
        title: `Hands-on implementation: ${Array.isArray(currentModule.skills) && currentModule.skills[0] ? currentModule.skills[0] : 'Practical Coding'}`,
        description: 'Build and verify a minimal working code pattern with thorough assertions.',
        estimate_minutes: 60
      },
      {
        title: `Verify competency with Tutor Quiz for ${currentModule.title || 'Module'}`,
        description: 'Take the module quiz to assess retention and unlock subsequent learning modules.',
        estimate_minutes: 30
      }
    ];

    const validation = VerifierAgent.validateDailyTasks({
      tasks: fallbackTemplates,
      dailyHoursBudget: hoursPerDay,
      existingTasks
    });

    tasksToWrite = (validation.validatedTasks.length > 0 ? validation.validatedTasks : fallbackTemplates).map((t, idx) => ({
      id: `task_fallback_${Date.now()}_${idx + 1}`,
      uid,
      module_id: currentModule.id || 'current_module',
      title: t.title,
      description: t.description,
      estimate_minutes: t.estimate_minutes,
      status: 'todo',
      scheduled_date: todayDateStr,
      source: 'template_fallback',
      created_at: new Date().toISOString()
    }));
  }

  // Coach Agent writes learning tasks to Firestore
  if (db && tasksToWrite.length > 0) {
    const batch = db.batch();
    for (const task of tasksToWrite) {
      const docRef = db.collection('tasks').doc(`${uid}_${task.id}`);
      batch.set(docRef, task, { merge: true });
    }
    await batch.commit();
  }

  return {
    live: isLive,
    fallback: !isLive,
    reason: fallbackReason,
    provider: providerUsed,
    model: modelUsed,
    requestId,
    count: tasksToWrite.length,
    tasks: tasksToWrite
  };
}

/**
 * Reschedule uncompleted / overdue tasks for a user
 */
async function rescheduleTasks({ uid, userProfile = {}, uncompletedTasks = [] }) {
  if (!uid) throw new Error('User UID is required.');
  const todayDateStr = new Date().toISOString().split('T')[0];

  if (!uncompletedTasks.length) {
    return { live: true, rescheduledCount: 0, tasks: [] };
  }

  const batch = db ? db.batch() : null;
  const updatedTasks = [];

  for (const t of uncompletedTasks) {
    const updated = {
      ...t,
      scheduled_date: todayDateStr,
      rescheduled: true,
      updated_at: new Date().toISOString()
    };
    updatedTasks.push(updated);
    if (batch) {
      const docRef = db.collection('tasks').doc(`${uid}_${t.id}`);
      batch.set(docRef, updated, { merge: true });
    }
  }

  if (batch) {
    await batch.commit();
  }

  return {
    live: true,
    fallback: false,
    rescheduledCount: updatedTasks.length,
    tasks: updatedTasks
  };
}

module.exports = {
  generateDailyLearningTasks,
  rescheduleTasks
};
