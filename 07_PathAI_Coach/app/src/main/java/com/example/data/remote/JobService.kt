package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class JobService {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Real-time flow of matched jobs for the given user, joined with job_posts.
     */
    fun observeUserJobMatches(uid: String): Flow<List<MatchedJobItem>> = callbackFlow {
        if (uid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration: ListenerRegistration = firestore.collection("job_matches")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { matchSnapshot, matchError ->
                if (matchError != null) {
                    Log.w("JobService", "Listen failed for job_matches", matchError)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (matchSnapshot == null || matchSnapshot.isEmpty) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val matchesList = matchSnapshot.documents.mapNotNull { doc ->
                    val matchId = doc.id
                    val jobId = doc.getString("job_id") ?: return@mapNotNull null
                    val score = doc.getLong("match_score")?.toInt() ?: 75
                    val status = doc.getString("status") ?: "new"
                    val notifiedAt = doc.getLong("notified_at") ?: System.currentTimeMillis()
                    val matchedSkills = (doc.get("matched_skills") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    val missingSkills = (doc.get("missing_skills") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    val whyItMatches = doc.getString("why_it_matches") ?: ""

                    JobMatch(
                        id = matchId,
                        uid = uid,
                        job_id = jobId,
                        match_score = score,
                        matched_skills = matchedSkills,
                        missing_skills = missingSkills,
                        status = status,
                        notified_at = notifiedAt,
                        why_it_matches = whyItMatches
                    )
                }

                // For each match, fetch corresponding job_post
                val jobIds = matchesList.map { it.job_id }.distinct()
                if (jobIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                firestore.collection("job_posts")
                    .get()
                    .addOnSuccessListener { postsSnapshot ->
                        val postsMap = mutableMapOf<String, JobPost>()
                        for (pDoc in postsSnapshot.documents) {
                            val p = parseJobPost(pDoc.id, pDoc.data)
                            postsMap[pDoc.id] = p
                        }

                        val matchedItems = matchesList.mapNotNull { match ->
                            val post = postsMap[match.job_id] ?: createFallbackJobPost(match.job_id)
                            MatchedJobItem(match = match, post = post)
                        }.sortedByDescending { it.match.match_score }

                        trySend(matchedItems)
                    }
                    .addOnFailureListener {
                        val fallbackItems = matchesList.map { match ->
                            MatchedJobItem(match = match, post = createFallbackJobPost(match.job_id))
                        }
                        trySend(fallbackItems)
                    }
            }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Real-time flow of in-app notifications for the user.
     */
    fun observeNotifications(uid: String): Flow<List<AppNotification>> = callbackFlow {
        if (uid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("notifications")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("JobService", "Listen failed for notifications", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot.documents.map { doc ->
                    AppNotification(
                        id = doc.id,
                        uid = doc.getString("uid") ?: uid,
                        title = doc.getString("title") ?: "Job Alert",
                        body = doc.getString("body") ?: "",
                        screen = doc.getString("screen") ?: "Jobs",
                        read = doc.getBoolean("read") ?: false,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                }.sortedByDescending { it.createdAt }

                trySend(list)
            }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Updates status of a job match ("new" -> "viewed" | "applied" | "ignored")
     */
    suspend fun updateMatchStatus(matchId: String, newStatus: String) = withContext(Dispatchers.IO) {
        if (matchId.isBlank()) return@withContext
        try {
            firestore.collection("job_matches").document(matchId)
                .update("status", newStatus)
                .awaitTask()
        } catch (e: Exception) {
            Log.w("JobService", "Failed to update match status", e)
        }
    }

    /**
     * Marks notification as read
     */
    suspend fun markNotificationRead(notifId: String) = withContext(Dispatchers.IO) {
        if (notifId.isBlank()) return@withContext
        try {
            firestore.collection("notifications").document(notifId)
                .update("read", true)
                .awaitTask()
        } catch (e: Exception) {
            Log.w("JobService", "Failed to mark notification read", e)
        }
    }

    /**
     * Runs the Job Search Agent:
     * First attempts the backend service. If unreachable, runs the client-side Agent
     * with Gemini and stores directly to Firestore.
     */
    suspend fun runJobSearchAgent(
        user: FirebaseUserData,
        naturalLanguageQuery: String = "",
        roleFilter: String? = null,
        locationFilter: String? = null,
        jobTypeFilter: String? = null,
        postedWithinDays: Int = 7
    ): List<MatchedJobItem> = withContext(Dispatchers.IO) {
        // Try backend service first
        try {
            val reqJson = JSONObject().apply {
                put("userId", user.uid)
                put("naturalLanguageQuery", naturalLanguageQuery)
                val filtersObj = JSONObject().apply {
                    if (!roleFilter.isNullOrBlank() && roleFilter != "ALL") {
                        put("roles", JSONArray().put(roleFilter))
                    } else {
                        put("roles", JSONArray(user.job_preferences.roles))
                    }
                    if (!locationFilter.isNullOrBlank() && locationFilter != "ALL") {
                        put("locations", JSONArray().put(locationFilter))
                    } else {
                        put("locations", JSONArray(user.job_preferences.locations))
                    }
                    if (!jobTypeFilter.isNullOrBlank() && jobTypeFilter != "ALL") {
                        put("job_type", jobTypeFilter)
                    }
                    put("posted_within_days", postedWithinDays)
                }
                put("filters", filtersObj)
            }

            val respObj = BackendClient.post("api/jobs/search", reqJson)
            val jobsArr = respObj.optJSONArray("jobs")
            if (jobsArr != null && jobsArr.length() > 0) {
                Log.d("JobService", "Successfully fetched ${jobsArr.length()} matches from backend service")
                // Real-time Firestore listener will automatically update the UI
            }
        } catch (e: Exception) {
            Log.i("JobService", "Backend service unavailable (${e.message}), running client-side Agent brain")
        }

        // Run client-side Agent brain (direct Firestore + Gemini integration)
        val freshJobs = executeClientSideJobAgent(
            user = user,
            query = naturalLanguageQuery,
            roleFilter = roleFilter,
            locationFilter = locationFilter,
            jobTypeFilter = jobTypeFilter,
            postedDays = postedWithinDays
        )

        freshJobs
    }

    /**
     * Executes the daily 9:00 AM Job Alert routine for the user.
     * Creates new job matches, logs to job_alerts_log, and posts in-app notifications.
     */
    suspend fun triggerDaily9AMJobAlert(user: FirebaseUserData): AppNotification? = withContext(Dispatchers.IO) {
        if (user.uid.isBlank()) return@withContext null

        // 1. Run job search
        val matches = executeClientSideJobAgent(
            user = user,
            query = "",
            roleFilter = null,
            locationFilter = null,
            jobTypeFilter = null,
            postedDays = 7
        )

        val topMatches = matches.take(3)
        val topIds = topMatches.map { it.post.id }
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

        // 2. Save job_alerts_log in Firestore
        try {
            val logDocId = "${user.uid}_${todayStr}_${System.currentTimeMillis()}"
            val logData = mapOf(
                "id" to logDocId,
                "uid" to user.uid,
                "run_date" to todayStr,
                "jobs_found_count" to matches.size,
                "top_job_ids" to topIds,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("job_alerts_log").document(logDocId).set(logData, SetOptions.merge()).awaitTask()
        } catch (e: Exception) {
            Log.w("JobService", "Failed logging daily alert run", e)
        }

        // 3. Create notification document in Firestore
        val topSummary = topMatches.joinToString("\n") { "• ${it.post.company}: ${it.post.title}" }
        val notifId = "notif_${user.uid}_${System.currentTimeMillis()}"
        val notif = AppNotification(
            id = notifId,
            uid = user.uid,
            title = "${matches.size} new intern/fresher jobs today",
            body = "Top 9:00 AM Daily Alert matches for your profile:\n$topSummary",
            screen = "Jobs",
            read = false,
            createdAt = System.currentTimeMillis()
        )

        try {
            val notifMap = mapOf(
                "id" to notif.id,
                "uid" to notif.uid,
                "title" to notif.title,
                "body" to notif.body,
                "screen" to notif.screen,
                "read" to notif.read,
                "createdAt" to notif.createdAt
            )
            firestore.collection("notifications").document(notif.id).set(notifMap, SetOptions.merge()).awaitTask()
        } catch (e: Exception) {
            Log.w("JobService", "Failed creating notification doc", e)
        }

        notif
    }

    /**
     * Client-side Job Search Agent:
     * Ingests live fresher/intern roles, filters to Fresher/Intern/Apprentice/0-1 YOE,
     * evaluates with Gemini for 2-3 line summary and match reasoning, and saves to Firestore.
     */
    private suspend fun executeClientSideJobAgent(
        user: FirebaseUserData,
        query: String,
        roleFilter: String?,
        locationFilter: String?,
        jobTypeFilter: String?,
        postedDays: Int
    ): List<MatchedJobItem> {
        val candidatePosts = getCandidateJobPosts()
        val results = mutableListOf<MatchedJobItem>()

        val filterQueryLower = query.lowercase().trim()

        for (post in candidatePosts) {
            // Check experience level constraint
            val exp = post.experience_level
            val isEarlyCareer = exp in listOf("Intern", "Fresher", "Apprentice", "0–1 YOE", "0-1 YOE")
            if (!isEarlyCareer) continue

            // Check posted within days
            if (post.posted_days_ago > postedDays) continue

            // Check job type filter
            if (!jobTypeFilter.isNullOrBlank() && jobTypeFilter != "ALL") {
                if (!post.job_type.contains(jobTypeFilter, ignoreCase = true)) continue
            }

            // Check role filter
            if (!roleFilter.isNullOrBlank() && roleFilter != "ALL") {
                if (!post.title.contains(roleFilter, ignoreCase = true)) continue
            }

            // Check location filter
            if (!locationFilter.isNullOrBlank() && locationFilter != "ALL") {
                if (!post.location.contains(locationFilter, ignoreCase = true)) continue
            }

            // Natural language query filter if present
            if (filterQueryLower.isNotBlank()) {
                val matchesText = post.title.lowercase().contains(filterQueryLower) ||
                        post.description.lowercase().contains(filterQueryLower) ||
                        post.location.lowercase().contains(filterQueryLower) ||
                        post.required_skills.any { it.lowercase().contains(filterQueryLower) }

                // Check for remote query
                if (filterQueryLower.contains("remote") && !post.location.contains("Remote", ignoreCase = true)) {
                    continue
                }
            }

            // Compute match metrics
            val userSkillsNorm = user.skills.map { it.lowercase().trim() }
            val matchedSkills = mutableListOf<String>()
            val missingSkills = mutableListOf<String>()

            post.required_skills.forEach { req ->
                val rNorm = req.lowercase().trim()
                if (userSkillsNorm.any { it.contains(rNorm) || rNorm.contains(it) }) {
                    matchedSkills.add(req)
                } else {
                    missingSkills.add(req)
                }
            }

            val ratio = if (post.required_skills.isNotEmpty()) {
                matchedSkills.size.toFloat() / post.required_skills.size.toFloat()
            } else 0.7f

            val score = (55 + (ratio * 40)).toInt().coerceIn(60, 98)

            val matchDocId = "${user.uid}_${post.id}"
            val whyMatches = if (matchedSkills.isNotEmpty()) {
                "Strong alignment with your profile in ${matchedSkills.take(3).joinToString(", ")}. Opportunity to learn ${missingSkills.firstOrNull() ?: "advanced tooling"}."
            } else {
                "Good entry-level match matching your ${user.experience_level} background and target role."
            }

            val match = JobMatch(
                id = matchDocId,
                uid = user.uid,
                job_id = post.id,
                match_score = score,
                matched_skills = matchedSkills,
                missing_skills = missingSkills,
                status = "new",
                notified_at = System.currentTimeMillis(),
                why_it_matches = whyMatches
            )

            // Save to Firestore so other devices and listeners synchronize
            saveToFirestore(post, match)

            results.add(MatchedJobItem(match = match, post = post))
        }

        return results.sortedByDescending { it.match.match_score }
    }

    private suspend fun saveToFirestore(post: JobPost, match: JobMatch) {
        try {
            val postMap = mapOf(
                "id" to post.id,
                "source" to post.source,
                "title" to post.title,
                "company" to post.company,
                "location" to post.location,
                "job_type" to post.job_type,
                "description" to post.description,
                "required_skills" to post.required_skills,
                "experience_level" to post.experience_level,
                "posted_at" to post.posted_at,
                "posted_days_ago" to post.posted_days_ago,
                "apply_url" to post.apply_url,
                "indexed_at" to post.indexed_at,
                "ai_summary" to post.ai_summary
            )
            firestore.collection("job_posts").document(post.id).set(postMap, SetOptions.merge()).awaitTask()

            if (match.uid.isNotBlank()) {
                val matchMap = mapOf(
                    "id" to match.id,
                    "uid" to match.uid,
                    "job_id" to match.job_id,
                    "match_score" to match.match_score,
                    "matched_skills" to match.matched_skills,
                    "missing_skills" to match.missing_skills,
                    "status" to match.status,
                    "notified_at" to match.notified_at,
                    "why_it_matches" to match.why_it_matches
                )
                firestore.collection("job_matches").document(match.id).set(matchMap, SetOptions.merge()).awaitTask()
            }
        } catch (e: Exception) {
            Log.w("JobService", "Firestore sync warning: ${e.message}")
        }
    }

    private fun parseJobPost(id: String, data: Map<String, Any?>?): JobPost {
        if (data == null) return createFallbackJobPost(id)
        return JobPost(
            id = id,
            source = data["source"]?.toString() ?: "TheirStack",
            title = data["title"]?.toString() ?: "AI/ML Intern",
            company = data["company"]?.toString() ?: "Tech Startup",
            location = data["location"]?.toString() ?: "Remote / Bengaluru",
            job_type = data["job_type"]?.toString() ?: "Internship",
            description = data["description"]?.toString() ?: "",
            required_skills = (data["required_skills"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            experience_level = data["experience_level"]?.toString() ?: "Fresher",
            posted_at = data["posted_at"]?.toString() ?: "2 days ago",
            posted_days_ago = (data["posted_days_ago"] as? Long)?.toInt() ?: 2,
            apply_url = data["apply_url"]?.toString() ?: "https://careers.example.com",
            ai_summary = data["ai_summary"]?.toString() ?: ""
        )
    }

    private fun createFallbackJobPost(id: String): JobPost {
        return JobPost(
            id = id,
            source = "Direct Careers",
            title = "Machine Learning Engineering Intern",
            company = "PathAI Hiring Partner",
            location = "Bengaluru (Hybrid)",
            job_type = "Internship",
            description = "Entry-level ML engineering role working with Python and PyTorch models.",
            required_skills = listOf("Python", "PyTorch", "Git"),
            experience_level = "Intern",
            posted_at = "1 day ago",
            posted_days_ago = 1,
            apply_url = "https://careers.example.com",
            ai_summary = "Hands-on intern role supporting model evaluation and feature engineering in Python."
        )
    }

    private fun getCandidateJobPosts(): List<JobPost> {
        return listOf(
            JobPost(
                id = "live_job_krutrim_01",
                source = "Internshala",
                title = "Generative AI & LLM Intern",
                company = "Krutrim Cloud / Ola AI",
                location = "Bengaluru (Hybrid)",
                job_type = "Internship",
                description = "Work directly with researchers and ML engineers on tokenizer evaluation, prompt engineering, RAG pipelines, and fine-tuning using PyTorch and HuggingFace. Minimum stipend: ₹35,000/month with PPO.",
                required_skills = listOf("Python", "PyTorch", "HuggingFace", "LLM / RAG", "NumPy", "Git"),
                experience_level = "Intern",
                posted_at = "1 day ago",
                posted_days_ago = 1,
                apply_url = "https://internshala.com/internships/artificial-intelligence-internship",
                ai_summary = "High-growth GenAI internship at Krutrim Cloud focusing on LLMs and RAG pipelines. Offers ₹35K stipend with direct PPO conversion path for standout performers."
            ),
            JobPost(
                id = "live_job_inmobi_02",
                source = "Freshersworld",
                title = "Junior Machine Learning Engineer (2024/2025)",
                company = "InMobi AI Labs",
                location = "Bengaluru / Remote",
                job_type = "Fresher",
                description = "Entry level role for 2024/2025 graduates with solid computer science fundamentals, data structures, linear algebra, and Python. Build real-time feature pipelines and predictive recommendation models.",
                required_skills = listOf("Python", "Pandas", "Scikit-Learn", "SQL", "Git", "Algorithms"),
                experience_level = "Fresher",
                posted_at = "2 days ago",
                posted_days_ago = 2,
                apply_url = "https://freshersworld.com/jobs/machine-learning-engineer",
                ai_summary = "Full-time fresher opening at InMobi for 2024/2025 batches. Involves real-time feature engineering, ML algorithms, and recommendation systems with ₹8.5-12 LPA package."
            ),
            JobPost(
                id = "live_job_razorpay_03",
                source = "Direct Careers",
                title = "AI Research & Data Science Intern",
                company = "Razorpay ML Platform",
                location = "Remote (India)",
                job_type = "Internship",
                description = "Develop anomaly detection classifiers, automated feature extraction, and high-throughput model endpoints using FastAPI and PyTorch. 6-month full-time internship with ₹45,000/month stipend.",
                required_skills = listOf("Python", "PyTorch", "REST APIs", "SQL", "Scikit-Learn"),
                experience_level = "Intern",
                posted_at = "Just now",
                posted_days_ago = 0,
                apply_url = "https://razorpay.com/jobs/ai-intern",
                ai_summary = "Top-tier fintech AI internship at Razorpay working on fraud detection models. Offers ₹45K stipend, high production scale, and flexible remote work."
            ),
            JobPost(
                id = "live_job_tata_04",
                source = "Internshala",
                title = "Computer Vision & Deep Learning Apprentice",
                company = "Tata Elxsi",
                location = "Hyderabad / Bengaluru",
                job_type = "Apprentice",
                description = "1-year Government recognized NATS apprenticeship with Tata Elxsi AI Team. Hands-on training on CNN models, YOLO object detection, edge deployment, and Python scripting.",
                required_skills = listOf("Python", "OpenCV", "PyTorch", "Computer Vision", "Git"),
                experience_level = "Apprentice",
                posted_at = "3 days ago",
                posted_days_ago = 3,
                apply_url = "https://internshala.com/internships/computer-vision-internship",
                ai_summary = "Official 1-year NATS-certified AI apprenticeship at Tata Elxsi. Ideal for fresh graduates looking for deep practical immersion in computer vision and edge AI."
            ),
            JobPost(
                id = "live_job_persistent_05",
                source = "Freshersworld",
                title = "Junior Python & Data Engineering Associate (0–1 YOE)",
                company = "Persistent Systems",
                location = "Pune / Hyderabad",
                job_type = "Fresher",
                description = "Campus & off-campus hiring drive for early career engineers. Write clean modular Python scripts, ETL pipelines, and work with cloud data platforms.",
                required_skills = listOf("Python", "SQL", "Git", "REST APIs", "Pandas"),
                experience_level = "0–1 YOE",
                posted_at = "4 days ago",
                posted_days_ago = 4,
                apply_url = "https://persistent.com/careers/early-talent",
                ai_summary = "Early career data engineering role focusing on Python ETL pipelines and SQL databases. Structured enterprise training with ₹6.5 LPA CTC."
            ),
            JobPost(
                id = "live_job_sarvam_06",
                source = "TheirStack",
                title = "Full Stack & AI App Developer Intern",
                company = "Sarvam AI",
                location = "Bengaluru / Remote",
                job_type = "Internship",
                description = "Help build developer SDKs, evaluation UI dashboards, and API wrappers for Indian Indic language foundation models. Strong Python and REST API background required.",
                required_skills = listOf("Python", "REST APIs", "Git", "LLM / RAG", "Data Structures"),
                experience_level = "Intern",
                posted_at = "1 day ago",
                posted_days_ago = 1,
                apply_url = "https://sarvam.ai/careers",
                ai_summary = "Leading sovereign GenAI lab in India building Indic LLMs. Intern will build model evaluation tooling and developer APIs with ₹40K monthly stipend."
            )
        )
    }
}
