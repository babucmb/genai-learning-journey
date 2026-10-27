const cron = require('node-cron');
const { db } = require('./firebaseAdmin');
const { runJobSearchAgent } = require('./agents/jobSearchAgent');

/**
 * Executes the daily 9:00 AM Job Alert workflow for a specific user.
 */
async function runDailyJobAlertForUser(userId, userData) {
  try {
    console.log(`[DailyScheduler] Running daily alert job for user: ${userId}`);

    // Call Job Search Agent with user's profile and preferences
    const matches = await runJobSearchAgent({
      userId,
      userProfile: userData,
      filters: userData.job_preferences || {}
    });

    if (!matches || !matches.length) {
      console.log(`[DailyScheduler] No new matches found for ${userId}`);
      return null;
    }

    const todayDateStr = new Date().toISOString().split('T')[0];
    const topMatches = matches.slice(0, 3);
    const topJobIds = topMatches.map(m => m.post.id);

    // 1. Create job_alerts_log document
    const logDoc = {
      id: `${userId}_${todayDateStr}_${Date.now()}`,
      uid: userId,
      run_date: todayDateStr,
      jobs_found_count: matches.length,
      top_job_ids: topJobIds,
      timestamp: Date.now()
    };

    if (db) {
      await db.collection('job_alerts_log').doc(logDoc.id).set(logDoc);
    }

    // 2. Format in-app notification
    const topJobsSummary = topMatches
      .map(m => `• ${m.post.company}: ${m.post.title}`)
      .join('\n');

    const notifDoc = {
      id: `notif_${userId}_${Date.now()}`,
      uid: userId,
      title: `${matches.length} new intern/fresher jobs today`,
      body: `Top verified matches for 9:00 AM Alert:\n${topJobsSummary}`,
      screen: 'Jobs',
      read: false,
      createdAt: Date.now()
    };

    if (db) {
      await db.collection('notifications').doc(notifDoc.id).set(notifDoc);
      console.log(`[DailyScheduler] In-app notification created for ${userId}`);
    }

    return {
      log: logDoc,
      notification: notifDoc,
      matchesCount: matches.length
    };
  } catch (error) {
    console.error(`[DailyScheduler] Error running daily alert for ${userId}:`, error);
    throw error;
  }
}

/**
 * Scans all Firestore users with job_alerts_enabled = true and runs the daily alert.
 */
async function runDailyJobAlertsForAllUsers() {
  console.log('[DailyScheduler] Running 9:00 AM Daily Job Alerts for all subscribed users...');
  if (!db) {
    console.warn('[DailyScheduler] Firestore not initialized, skipping daily cron');
    return [];
  }

  try {
    const usersSnapshot = await db.collection('users').get();
    const results = [];

    for (const doc of usersSnapshot.docs) {
      const user = doc.data();
      const alertsEnabled = user.notifications?.job_alerts_enabled ?? true;
      if (alertsEnabled) {
        const res = await runDailyJobAlertForUser(doc.id, user);
        if (res) results.push(res);
      }
    }

    console.log(`[DailyScheduler] Finished daily alerts for ${results.length} active users.`);
    return results;
  } catch (error) {
    console.error('[DailyScheduler] Daily alert execution error:', error);
    return [];
  }
}

/**
 * Initializes the node-cron scheduled job.
 * Standard cron for 9:00 AM: '0 9 * * *'
 * In Asia/Kolkata timezone.
 */
function initScheduler() {
  console.log('[DailyScheduler] Registering 9:00 AM Asia/Kolkata Cron job...');
  cron.schedule('0 9 * * *', async () => {
    console.log('[DailyScheduler] ⏰ Cron Trigger: 9:00 AM Job Alert Run');
    await runDailyJobAlertsForAllUsers();
  }, {
    scheduled: true,
    timezone: 'Asia/Kolkata'
  });
}

module.exports = {
  initScheduler,
  runDailyJobAlertForUser,
  runDailyJobAlertsForAllUsers
};
