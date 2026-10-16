const { runJobSearchAgent } = require('./jobSearchAgent');
const { generateDailyLearningTasks, rescheduleTasks } = require('./coachAgent');
const { explainModuleConcept, generateModuleQuiz } = require('./tutorAgent');
const { generateProjectPlan } = require('./projectAgent');
const { generateTechRadar } = require('./researchAgent');
const VerifierAgent = require('./verifierAgent');
const { recordAgentRun } = require('../services/observability');

/**
 * Career Agent Orchestrator:
 * Delegates to specialized sub-agents while enforcing strict governance:
 * - Coach/Planner Agent: creates or updates daily learning tasks.
 * - Tutor Agent: explains modules and author quizzes.
 * - Project Agent: creates project milestones and technical architecture.
 * - Research Agent: summarizes technology radar updates.
 * - Job Search Agent: fetches and matches live jobs.
 * - Verifier Agent: validates all outputs before writes or client returns.
 *
 * Governance Invariants:
 * - ONLY Coach/Planner Agent may write learning tasks.
 * - ONLY Job Search Agent may write job matches.
 * - Verifier MUST validate outputs before Firestore writes.
 */
async function orchestrateCareerTask({ taskType, payload = {}, uid }) {
  console.log(`[CareerOrchestrator] Orchestrating task: "${taskType}" for user: ${uid}`);
  const startTime = Date.now();

  let agentType = 'orchestrator';
  let responsePayload = null;
  let isLive = true;
  let fallback = false;
  let errorCode = null;

  try {
    switch (taskType) {
      case 'GENERATE_DAILY_TASKS': {
        agentType = 'coach_planner';
        responsePayload = await generateDailyLearningTasks({
          uid,
          userProfile: payload.userProfile,
          currentModule: payload.currentModule,
          existingTasks: payload.existingTasks || [],
          allowFallback: payload.allowFallback !== false
        });
        isLive = responsePayload.live;
        fallback = responsePayload.fallback;
        break;
      }

      case 'RESCHEDULE_TASKS': {
        agentType = 'coach_planner';
        responsePayload = await rescheduleTasks({
          uid,
          userProfile: payload.userProfile,
          uncompletedTasks: payload.uncompletedTasks || []
        });
        isLive = responsePayload.live;
        fallback = responsePayload.fallback;
        break;
      }

      case 'EXPLAIN_CONCEPT': {
        agentType = 'tutor';
        responsePayload = await explainModuleConcept({
          moduleId: payload.moduleId,
          title: payload.title,
          description: payload.description,
          skills: payload.skills,
          content: payload.content,
          userQuestion: payload.userQuestion
        });
        isLive = responsePayload.live;
        fallback = responsePayload.fallback;
        break;
      }

      case 'GENERATE_QUIZ': {
        agentType = 'tutor';
        responsePayload = await generateModuleQuiz({
          moduleId: payload.moduleId,
          title: payload.title,
          description: payload.description,
          skills: payload.skills,
          count: payload.count || 3
        });
        isLive = responsePayload.live;
        fallback = responsePayload.fallback;
        break;
      }

      case 'PLAN_PROJECT': {
        agentType = 'project';
        responsePayload = await generateProjectPlan({
          uid,
          projectTitle: payload.projectTitle,
          objective: payload.objective,
          techStack: payload.techStack,
          userProfile: payload.userProfile
        });
        isLive = responsePayload.live;
        fallback = responsePayload.fallback;
        break;
      }

      case 'TECH_RADAR': {
        agentType = 'research';
        responsePayload = await generateTechRadar({
          domain: payload.domain,
          userProfile: payload.userProfile
        });
        isLive = responsePayload.live;
        fallback = responsePayload.fallback;
        break;
      }

      case 'SEARCH_JOBS': {
        agentType = 'job_search';
        const rawMatches = await runJobSearchAgent({
          userId: uid,
          userProfile: payload.userProfile,
          filters: payload.filters || {},
          naturalLanguageQuery: payload.naturalLanguageQuery || ''
        });

        // Verifier Agent validates match scores and format
        const { validatedMatches } = VerifierAgent.validateJobMatches(rawMatches);
        responsePayload = {
          live: true,
          fallback: false,
          count: validatedMatches.length,
          jobs: validatedMatches
        };
        break;
      }

      default:
        throw new Error(`Unrecognized career orchestration taskType: "${taskType}"`);
    }

    const latencyMs = Date.now() - startTime;
    await recordAgentRun({
      uid,
      agentType,
      provider: responsePayload?.provider || 'orchestrator',
      model: responsePayload?.model || 'default',
      live: isLive,
      fallback,
      status: 'SUCCESS',
      latencyMs,
      metadata: { taskType }
    });

    return {
      success: true,
      orchestrator: 'PathAI.CareerOrchestrator',
      delegatedAgent: agentType,
      ...responsePayload
    };
  } catch (err) {
    const latencyMs = Date.now() - startTime;
    errorCode = err.code || 'ORCHESTRATION_ERROR';

    await recordAgentRun({
      uid,
      agentType,
      status: 'FAILED',
      latencyMs,
      errorCode,
      metadata: { taskType, errorMessage: err.message }
    });

    throw err;
  }
}

module.exports = {
  orchestrateCareerTask
};
