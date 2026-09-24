const { db, admin } = require('../firebaseAdmin');
const crypto = require('crypto');

/**
 * Observability logger: records agent execution telemetry in `agent_runs` Firestore collection.
 * CRITICAL RULE: Never store API keys or sensitive resume data in logs.
 */
async function recordAgentRun({
  uid,
  agentType,
  provider = 'gemini',
  model = 'default',
  live = true,
  fallback = false,
  status = 'SUCCESS',
  latencyMs = 0,
  errorCode = null,
  metadata = {}
}) {
  const runId = crypto.randomUUID();
  const runData = {
    id: runId,
    uid: uid || 'anonymous',
    agent_type: agentType,
    provider,
    model,
    live: Boolean(live),
    fallback: Boolean(fallback),
    status: status || 'SUCCESS',
    latency_ms: Math.round(latencyMs),
    error_code: errorCode || null,
    created_at: new Date().toISOString()
  };

  // Safe non-sensitive metadata only (e.g. counts, module ID)
  if (metadata && typeof metadata === 'object') {
    const safeMetadata = {};
    for (const [key, value] of Object.entries(metadata)) {
      // Exclude any keys that might hold secrets or raw resume text
      if (!/key|token|auth|secret|password|resume|credential/i.test(key) && (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean')) {
        safeMetadata[key] = value;
      }
    }
    runData.metadata = safeMetadata;
  }

  if (db) {
    try {
      await db.collection('agent_runs').doc(runId).set(runData);
    } catch (err) {
      console.warn('[Observability] Could not write to Firestore agent_runs:', err.message);
    }
  }

  return runData;
}

module.exports = {
  recordAgentRun
};
