require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { admin, db } = require('./firebaseAdmin');
const { authenticateFirebaseToken } = require('./middleware/authMiddleware');
const { getProviderHealth } = require('./llm/llmProvider');
const { explainModuleConcept, generateModuleQuiz } = require('./agents/tutorAgent');
const { generateDailyLearningTasks, rescheduleTasks } = require('./agents/coachAgent');
const { processAgentChat } = require('./agents/multiAgentChat');
const { generateProjectPlan } = require('./agents/projectAgent');
const { generateTechRadar } = require('./agents/researchAgent');
const { orchestrateCareerTask } = require('./agents/careerOrchestrator');
const { runJobSearchAgent } = require('./agents/jobSearchAgent');
const { initScheduler, runDailyJobAlertForUser, runDailyJobAlertsForAllUsers } = require('./scheduler');
const { recordAgentRun } = require('./services/observability');

const app = express();
const PORT = process.env.PORT || process.env.BACKEND_PORT || process.env.PORT_BACKEND || 5000;

app.use(cors());
app.use(express.json());

// Helper: load user profile safely from Firestore
async function fetchUserProfile(uid) {
  if (!db || !uid) return {};
  try {
    const doc = await db.collection('users').doc(uid).get();
    return doc.exists ? doc.data() : {};
  } catch (err) {
    console.warn(`[Index] Could not fetch user profile for ${uid}:`, err.message);
    return {};
  }
}

// Health Check (returns HTTP 200 with status: "ok" and service: "pathai-backend")
app.get('/health', async (req, res) => {
  const providerHealth = await getProviderHealth();
  res.status(200).json({
    status: 'ok',
    service: 'pathai-backend',
    timestamp: new Date().toISOString(),
    llm: providerHealth
  });
});

/**
 * Endpoint: Multi-Agent Chat
 * POST /api/agents/chat
 * Accepts: { agentType, message, history, currentModuleInfo }
 */
app.post('/api/agents/chat', authenticateFirebaseToken, async (req, res) => {
  const startTime = Date.now();
  const uid = req.uid;
  const { agentType = 'coach', message, history = [], currentModuleInfo = null } = req.body;

  if (!message || typeof message !== 'string') {
    return res.status(400).json({ success: false, error: 'Field "message" is required.' });
  }

  try {
    const userProfile = await fetchUserProfile(uid);
    const result = await processAgentChat({
      agentType,
      message,
      history,
      currentModuleInfo,
      userProfile
    });

    const latencyMs = Date.now() - startTime;
    await recordAgentRun({
      uid,
      agentType: `chat_${agentType}`,
      provider: result.provider,
      model: result.model,
      live: result.live,
      fallback: result.fallback,
      status: 'SUCCESS',
      latencyMs,
      errorCode: result.fallback ? 'FALLBACK_TRIGGERED' : null
    });

    res.json({
      success: true,
      ...result
    });
  } catch (error) {
    const latencyMs = Date.now() - startTime;
    await recordAgentRun({
      uid,
      agentType: `chat_${agentType}`,
      status: 'FAILED',
      latencyMs,
      errorCode: error.code || 'CHAT_ERROR'
    });
    res.status(500).json({ success: false, error: error.message });
  }
});

/**
 * Endpoint: Tutor Explain Concept
 * POST /api/tutor/explain
 * Accepts: { moduleId, title, description, skills, content, userQuestion }
 */
app.post('/api/tutor/explain', authenticateFirebaseToken, async (req, res) => {
  const startTime = Date.now();
  const uid = req.uid;
  const { moduleId, title, description, skills = [], content = '', userQuestion = '' } = req.body;

  try {
    const result = await explainModuleConcept({
      moduleId,
      title,
      description,
      skills,
      content,
      userQuestion
    });

    const latencyMs = Date.now() - startTime;
    await recordAgentRun({
      uid,
      agentType: 'tutor_explain',
      provider: result.provider,
      model: result.model,
      live: result.live,
      fallback: result.fallback,
      status: 'SUCCESS',
      latencyMs,
      metadata: { moduleId }
    });

    res.json({
      success: true,
      ...result
    });
  } catch (error) {
    const latencyMs = Date.now() - startTime;
    await recordAgentRun({
      uid,
      agentType: 'tutor_explain',
      status: 'FAILED',
      latencyMs,
      errorCode: error.code || 'EXPLAIN_ERROR'
    });
    res.status(500).json({ success: false, error: error.message });
  }
});

/**
 * Endpoint: Tutor Quiz Generation
 * POST /api/tutor/quiz
 * Accepts: { moduleId, title, description, skills, count }
 */
app.post('/api/tutor/quiz', authenticateFirebaseToken, async (req, res) => {
  const startTime = Date.now();
  const uid = req.uid;
  const { moduleId, title, description, skills = [], count = 3 } = req.body;

  try {
    const result = await generateModuleQuiz({
      moduleId,
      title,
      description,
      skills,
      count
    });

    const latencyMs = Date.now() - startTime;
    await recordAgentRun({
      uid,
      agentType: 'tutor_quiz',
      provider: result.provider,
      model: result.model,
      live: result.live,
      fallback: result.fallback,
      status: 'SUCCESS',
      latencyMs,
      metadata: { moduleId, questionCount: result.questions?.length }
    });

    res.json({
      success: true,
      ...result
    });
  } catch (error) {
    const latencyMs = Date.now() - startTime;
    await recordAgentRun({
      uid,
      agentType: 'tutor_quiz',
      status: 'FAILED',
      latencyMs,
      errorCode: error.code || 'QUIZ_ERROR'
    });
    res.status(500).json({ success: false, error: error.message });
  }
});

/**
 * Endpoint: Generate Daily Tasks (Coach/Planner Agent)
 * POST /api/learning/generate-daily
 * Accepts: { moduleId, allowFallback }
 */
app.post('/api/learning/generate-daily', authenticateFirebaseToken, async (req, res) => {
  const startTime = Date.now();
  const uid = req.uid;
  const { moduleId, allowFallback = true } = req.body;

  try {
    const userProfile = await fetchUserProfile(uid);

    // Fetch current module details
    let currentModule = { id: moduleId || 'current_module', title: 'Curriculum Sprint' };
    if (db && moduleId) {
      const modDoc = await db.collection('modules').doc(moduleId).get();
      if (modDoc.exists) {
        currentModule = modDoc.data();
      }
    }

    // Fetch existing tasks for today to prevent duplicates
    const todayStr = new Date().toISOString().split('T')[0];
    let existingTasks = [];
    if (db) {
      const snap = await db.collection('tasks')
        .where('uid', '==', uid)
        .where('scheduled_date', '==', todayStr)
        .get();
      existingTasks = snap.docs.map(d => d.data());
    }

    const result = await generateDailyLearningTasks({
      uid,
      userProfile,
      currentModule,
      existingTasks,
      allowFallback: allowFallback !== false
    });

    const latencyMs = Date.now() - startTime;
    await recordAgentRun({
      uid,
      agentType: 'coach_planner',
      provider: result.provider,
      model: result.model,
      live: result.live,
      fallback: result.fallback,
      status: 'SUCCESS',
      latencyMs,
      metadata: { moduleId, generatedCount: result.count }
    });

    res.json({
      success: true,
      ...result
    });
  } catch (error) {
    const latencyMs = Date.now() - startTime;
    await recordAgentRun({
      uid,
      agentType: 'coach_planner',
      status: 'FAILED',
      latencyMs,
      errorCode: error.code || 'GENERATE_DAILY_ERROR'
    });
    res.status(500).json({ success: false, error: error.message });
  }
});

/**
 * Endpoint: Reschedule Unfinished Tasks
 * POST /api/learning/reschedule
 * Accepts: { uncompletedTasks }
 */
app.post('/api/learning/reschedule', authenticateFirebaseToken, async (req, res) => {
  const uid = req.uid;
  const { uncompletedTasks = [] } = req.body;

  try {
    const userProfile = await fetchUserProfile(uid);
    const result = await rescheduleTasks({
      uid,
      userProfile,
      uncompletedTasks
    });

    res.json({
      success: true,
      ...result
    });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});

/**
 * Endpoint: Project Plan
 * POST /api/project/plan
 * Accepts: { projectTitle, objective, techStack }
 */
app.post('/api/project/plan', authenticateFirebaseToken, async (req, res) => {
  const uid = req.uid;
  const { projectTitle, objective, techStack = [] } = req.body;

  try {
    const userProfile = await fetchUserProfile(uid);
    const result = await generateProjectPlan({
      uid,
      projectTitle,
      objective,
      techStack,
      userProfile
    });

    res.json({
      success: true,
      ...result
    });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});

/**
 * Endpoint: Tech Radar
 * POST /api/research/radar
 * Accepts: { domain }
 */
app.post('/api/research/radar', authenticateFirebaseToken, async (req, res) => {
  const uid = req.uid;
  const { domain = 'Machine Learning & LLMs' } = req.body;

  try {
    const userProfile = await fetchUserProfile(uid);
    const result = await generateTechRadar({
      domain,
      userProfile
    });

    res.json({
      success: true,
      ...result
    });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});

/**
 * Endpoint: Search Jobs
 * POST /api/jobs/search
 * Accepts: { filters, naturalLanguageQuery }
 */
app.post('/api/jobs/search', authenticateFirebaseToken, async (req, res) => {
  const uid = req.uid;
  const { filters = {}, naturalLanguageQuery = '' } = req.body;

  try {
    const userProfile = await fetchUserProfile(uid);
    const matches = await runJobSearchAgent({
      userId: uid,
      userProfile,
      filters,
      naturalLanguageQuery
    });

    res.json({
      success: true,
      count: matches.length,
      jobs: matches
    });
  } catch (error) {
    console.error('Error in /api/jobs/search:', error);
    res.status(500).json({ success: false, error: error.message });
  }
});

/**
 * Endpoint: Career Orchestrator
 * POST /api/career/orchestrate
 * Accepts: { taskType, payload }
 */
app.post('/api/career/orchestrate', authenticateFirebaseToken, async (req, res) => {
  const uid = req.uid;
  const { taskType, payload = {} } = req.body;

  if (!taskType) {
    return res.status(400).json({ success: false, error: 'taskType is required' });
  }

  try {
    const result = await orchestrateCareerTask({
      taskType,
      payload,
      uid
    });
    res.json(result);
  } catch (error) {
    console.error('Error in /api/career/orchestrate:', error);
    res.status(500).json({ success: false, error: error.message });
  }
});

/**
 * Endpoint: Run Daily 9 AM Job Alert
 * POST /api/alerts/run-daily-job
 */
app.post('/api/alerts/run-daily-job', authenticateFirebaseToken, async (req, res) => {
  const uid = req.uid;
  try {
    const userProfile = await fetchUserProfile(uid);
    const result = await runDailyJobAlertForUser(uid, userProfile);
    res.json({ success: true, result });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});

if (require.main === module && process.env.NODE_ENV !== 'test') {
  initScheduler();
  app.listen(PORT, '0.0.0.0', () => {
    console.log(`[PathAI Backend] Secure LLM & Agent Server listening on 0.0.0.0:${PORT}`);
  });
}

module.exports = app;
