package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.LearningPathSeedData
import com.example.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class LearningPathService {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Seeds learning_paths and modules into Firestore if they don't already exist.
     */
    suspend fun seedCurriculumIfEmpty() = withContext(Dispatchers.IO) {
        try {
            val pathsSnapshot = firestore.collection("learning_paths").limit(1).get().awaitTask()
            if (pathsSnapshot.isEmpty) {
                Log.d("LearningPathService", "Seeding initial learning paths into Firestore...")
                val batch = firestore.batch()
                LearningPathSeedData.initialPaths.forEach { path ->
                    val ref = firestore.collection("learning_paths").document(path.id)
                    batch.set(ref, mapOf(
                        "id" to path.id,
                        "name" to path.name,
                        "description" to path.description,
                        "order_index" to path.order_index,
                        "modules" to path.modules,
                        "total_estimated_hours" to path.total_estimated_hours
                    ))
                }
                LearningPathSeedData.initialModules.forEach { mod ->
                    val ref = firestore.collection("modules").document(mod.id)
                    batch.set(ref, mapOf(
                        "id" to mod.id,
                        "path_id" to mod.path_id,
                        "title" to mod.title,
                        "description" to mod.description,
                        "order_index" to mod.order_index,
                        "prerequisites" to mod.prerequisites,
                        "content" to mod.content,
                        "estimated_hours" to mod.estimated_hours,
                        "skills" to mod.skills,
                        "related_concepts" to mod.related_concepts
                    ))
                }
                batch.commit().awaitTask()
                Log.d("LearningPathService", "Curriculum seed completed successfully.")
            }
        } catch (e: Exception) {
            Log.w("LearningPathService", "Failed seeding curriculum to Firestore (falling back to memory)", e)
        }
    }

    /**
     * Observe all learning paths in real time.
     */
    fun observeLearningPaths(): Flow<List<LearningPath>> = callbackFlow {
        val listener = firestore.collection("learning_paths")
            .orderBy("order_index")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) {
                    trySend(LearningPathSeedData.initialPaths)
                    return@addSnapshotListener
                }
                val paths = snapshot.documents.mapNotNull { doc ->
                    try {
                        LearningPath(
                            id = doc.getString("id") ?: doc.id,
                            name = doc.getString("name") ?: "",
                            description = doc.getString("description") ?: "",
                            order_index = (doc.getLong("order_index") ?: 0).toInt(),
                            modules = (doc.get("modules") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                            total_estimated_hours = (doc.getLong("total_estimated_hours") ?: 0).toInt()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(if (paths.isNotEmpty()) paths else LearningPathSeedData.initialPaths)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Observe modules for a given path in real time.
     */
    fun observeModules(pathId: String): Flow<List<LearningModule>> = callbackFlow {
        val listener = firestore.collection("modules")
            .whereEqualTo("path_id", pathId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) {
                    val fallback = LearningPathSeedData.initialModules.filter { it.path_id == pathId }
                    trySend(fallback)
                    return@addSnapshotListener
                }
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        LearningModule(
                            id = doc.getString("id") ?: doc.id,
                            path_id = doc.getString("path_id") ?: pathId,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            order_index = (doc.getLong("order_index") ?: 0).toInt(),
                            prerequisites = (doc.get("prerequisites") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                            content = doc.getString("content") ?: "",
                            estimated_hours = (doc.getLong("estimated_hours") ?: 4).toInt(),
                            skills = (doc.get("skills") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                            related_concepts = (doc.get("related_concepts") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }.sortedBy { it.order_index }
                trySend(if (list.isNotEmpty()) list else LearningPathSeedData.initialModules.filter { it.path_id == pathId })
            }
        awaitClose { listener.remove() }
    }

    /**
     * Observe user progress across modules for a path.
     */
    fun observeUserProgress(uid: String, pathId: String): Flow<List<UserProgressDoc>> = callbackFlow {
        if (uid.isBlank()) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val listener = firestore.collection("user_progress")
            .whereEqualTo("uid", uid)
            .whereEqualTo("path_id", pathId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val docs = snapshot.documents.mapNotNull { doc ->
                    try {
                        UserProgressDoc(
                            id = doc.id,
                            uid = doc.getString("uid") ?: uid,
                            path_id = doc.getString("path_id") ?: pathId,
                            module_id = doc.getString("module_id") ?: "",
                            status = doc.getString("status") ?: "locked",
                            started_at = doc.getLong("started_at"),
                            completed_at = doc.getLong("completed_at"),
                            score = (doc.getLong("score") ?: 0).toInt(),
                            last_position = (doc.getLong("last_position") ?: 0).toInt()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(docs)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Observe today's / all tasks for this user.
     */
    fun observeUserTasks(uid: String): Flow<List<FirestoreTask>> = callbackFlow {
        if (uid.isBlank()) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }
        val listener = firestore.collection("tasks")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val tasks = snapshot.documents.mapNotNull { doc ->
                    try {
                        FirestoreTask(
                            id = doc.id,
                            uid = doc.getString("uid") ?: uid,
                            module_id = doc.getString("module_id") ?: "",
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            estimate_minutes = (doc.getLong("estimate_minutes") ?: 45).toInt(),
                            status = doc.getString("status") ?: "todo",
                            scheduled_date = doc.getString("scheduled_date") ?: "Today",
                            completed_at = doc.getLong("completed_at"),
                            task_type = doc.getString("task_type") ?: "PRACTICE"
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(tasks)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Initializes or switches the primary path for the user.
     * Sets the first module to "in_progress", all other modules to "locked",
     * and generates initial daily tasks for the first module.
     */
    suspend fun selectOrSwitchPath(uid: String, pathId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (uid.isBlank()) return@withContext Result.failure(Exception("User UID is empty"))
        try {
            val pathModules = LearningPathSeedData.initialModules
                .filter { it.path_id == pathId }
                .sortedBy { it.order_index }

            if (pathModules.isEmpty()) {
                return@withContext Result.failure(Exception("Path has no modules"))
            }

            val batch = firestore.batch()

            // Initialize user_progress documents
            pathModules.forEachIndexed { index, mod ->
                val progressDocRef = firestore.collection("user_progress").document("${uid}_${mod.id}")
                val isFirst = index == 0
                val progressData = mapOf(
                    "id" to "${uid}_${mod.id}",
                    "uid" to uid,
                    "path_id" to pathId,
                    "module_id" to mod.id,
                    "status" to if (isFirst) "in_progress" else "locked",
                    "started_at" to if (isFirst) System.currentTimeMillis() else null,
                    "completed_at" to null,
                    "score" to 0,
                    "last_position" to 0
                )
                batch.set(progressDocRef, progressData, SetOptions.merge())
            }

            // Generate initial tasks for the first module
            val firstModule = pathModules.first()
            val initialTasks = LearningPathSeedData.generateTasksForModule(uid, firstModule)
            initialTasks.forEach { task ->
                val taskDocRef = firestore.collection("tasks").document("${uid}_${task.id}")
                batch.set(taskDocRef, mapOf(
                    "id" to "${uid}_${task.id}",
                    "uid" to uid,
                    "module_id" to task.module_id,
                    "title" to task.title,
                    "description" to task.description,
                    "estimate_minutes" to task.estimate_minutes,
                    "status" to task.status,
                    "scheduled_date" to task.scheduled_date,
                    "task_type" to task.task_type,
                    "completed_at" to null
                ), SetOptions.merge())
            }

            // Update user's currentPath in users collection
            val userRef = firestore.collection("users").document(uid)
            batch.set(userRef, mapOf(
                "currentPath" to pathId,
                "updatedAt" to System.currentTimeMillis()
            ), SetOptions.merge())

            batch.commit().awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("LearningPathService", "Error selecting/switching path", e)
            Result.failure(e)
        }
    }

    /**
     * Toggles task completion state in Firestore.
     * When all tasks in the module are done, checks if module should be marked completed.
     */
    suspend fun toggleTaskStatus(task: FirestoreTask): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val newStatus = if (task.status == "done") "todo" else "done"
            val completedAt = if (newStatus == "done") System.currentTimeMillis() else null

            firestore.collection("tasks").document(task.id)
                .set(mapOf(
                    "status" to newStatus,
                    "completed_at" to completedAt
                ), SetOptions.merge()).awaitTask()

            val isDone = newStatus == "done"
            Result.success(isDone)
        } catch (e: Exception) {
            Log.w("LearningPathService", "Failed toggling task", e)
            Result.failure(e)
        }
    }

    /**
     * Generates daily tasks for the current module via authenticated backend call to /api/learning/generate-daily.
     * Enforces user daily budget, prevents duplicates, and gracefully falls back to template if offline.
     */
    suspend fun generateDailyTasksForModule(uid: String, module: LearningModule): List<FirestoreTask> = withContext(Dispatchers.IO) {
        try {
            val req = JSONObject().apply {
                put("moduleId", module.id)
                put("allowFallback", BackendConfig.allowOfflineFallback)
            }
            val resp = BackendClient.post("api/learning/generate-daily", req)
            val tasksArray = resp.optJSONArray("tasks")
            val isLive = resp.optBoolean("live", true)
            if (tasksArray != null && tasksArray.length() > 0) {
                val list = mutableListOf<FirestoreTask>()
                for (i in 0 until tasksArray.length()) {
                    val item = tasksArray.getJSONObject(i)
                    list.add(FirestoreTask(
                        id = item.optString("id", "${uid}_task_$i"),
                        uid = uid,
                        module_id = item.optString("module_id", module.id),
                        title = item.optString("title"),
                        description = item.optString("description"),
                        estimate_minutes = item.optInt("estimate_minutes", 45),
                        status = item.optString("status", "todo"),
                        scheduled_date = item.optString("scheduled_date", "Today"),
                        task_type = item.optString("task_type", "PRACTICE"),
                        source = item.optString("source", if (isLive) "llm_generated" else "template_fallback")
                    ))
                }
                return@withContext list
            }
        } catch (e: Exception) {
            Log.w("LearningPathService", "Backend task generation failed, checking fallback mode", e)
        }

        if (!BackendConfig.allowOfflineFallback) {
            return@withContext emptyList()
        }

        // Fallback: Seed task templates marked with source="template_fallback"
        val fallbackTasks = LearningPathSeedData.generateTasksForModule(uid, module).map {
            it.copy(source = "template_fallback")
        }
        try {
            val batch = firestore.batch()
            fallbackTasks.forEach { task ->
                val ref = firestore.collection("tasks").document("${uid}_${task.id}")
                batch.set(ref, mapOf(
                    "id" to "${uid}_${task.id}",
                    "uid" to uid,
                    "module_id" to task.module_id,
                    "title" to task.title,
                    "description" to task.description,
                    "estimate_minutes" to task.estimate_minutes,
                    "status" to "todo",
                    "scheduled_date" to "Today",
                    "task_type" to task.task_type,
                    "source" to "template_fallback"
                ), SetOptions.merge())
            }
            batch.commit().awaitTask()
        } catch (e: Exception) {
            Log.w("LearningPathService", "Failed saving fallback tasks to Firestore", e)
        }
        fallbackTasks
    }

    /**
     * Marks a module as completed in user_progress and unlocks the next module in the path sequence.
     */
    suspend fun completeModuleAndUnlockNext(
        uid: String,
        pathId: String,
        currentModuleId: String,
        score: Int = 100
    ): Result<String?> = withContext(Dispatchers.IO) {
        try {
            val pathModules = LearningPathSeedData.initialModules
                .filter { it.path_id == pathId }
                .sortedBy { it.order_index }

            val currentIndex = pathModules.indexOfFirst { it.id == currentModuleId }
            val nextModule = if (currentIndex in 0 until pathModules.size - 1) {
                pathModules[currentIndex + 1]
            } else null

            val batch = firestore.batch()

            // Complete current module
            val currentProgressRef = firestore.collection("user_progress").document("${uid}_${currentModuleId}")
            batch.set(currentProgressRef, mapOf(
                "status" to "completed",
                "completed_at" to System.currentTimeMillis(),
                "score" to score
            ), SetOptions.merge())

            // Unlock next module
            if (nextModule != null) {
                val nextProgressRef = firestore.collection("user_progress").document("${uid}_${nextModule.id}")
                batch.set(nextProgressRef, mapOf(
                    "status" to "in_progress",
                    "started_at" to System.currentTimeMillis()
                ), SetOptions.merge())
            }

            batch.commit().awaitTask()

            // Dynamically generate daily tasks for next module via backend
            if (nextModule != null) {
                generateDailyTasksForModule(uid, nextModule)
            }

            Result.success(nextModule?.title)
        } catch (e: Exception) {
            Log.e("LearningPathService", "Failed to complete module", e)
            Result.failure(e)
        }
    }

    // --- TUTOR AGENT INTEGRATION ---

    /**
     * Tutor Agent: Explains concepts via authenticated backend endpoint /api/tutor/explain.
     * Fully grounded in module skills with explicit visibility if in offline fallback.
     */
    suspend fun tutorExplainConcept(
        module: LearningModule,
        userQuestion: String,
        candidateContext: String = ""
    ): String = withContext(Dispatchers.IO) {
        try {
            val req = JSONObject().apply {
                put("moduleId", module.id)
                put("title", module.title)
                put("description", module.description)
                put("skills", JSONArray(module.skills))
                put("content", module.content)
                put("userQuestion", userQuestion)
            }
            val resp = BackendClient.post("api/tutor/explain", req)
            val explanation = resp.optString("explanation")
            val isLive = resp.optBoolean("live", true)
            if (explanation.isNotBlank()) {
                return@withContext if (!isLive) {
                    "⚠️ [Offline / Template Content]\n\n$explanation"
                } else {
                    explanation
                }
            }
        } catch (e: Exception) {
            Log.w("LearningPathService", "Backend tutor explain call failed: ${e.message}")
        }

        if (!BackendConfig.allowOfflineFallback) {
            return@withContext "Tutor agent is currently offline. Please check your backend connection."
        }

        // Offline / Fallback Tutor Response
        """
        ⚠️ [Offline / Template Content]
        🎓 **Tutor Agent — ${module.title}**
        
        **Core Principle:**
        ${module.description}
        
        **Key Architectural Takeaway:**
        In **${module.skills.firstOrNull() ?: "production code"}**, consistency and understanding runtime semantics are critical.
        
        **Quick Example:**
        ```python
        # Idiomatic pattern for ${module.title}
        def verify_module_understanding():
            skills = ${module.skills.joinToString(prefix = "[\"", separator = "\", \"", postfix = "\"]")}
            return f"Mastered {len(skills)} competencies in ${module.title}"
        ```
        
        **Analogical Insight:**
        Think of ${module.related_concepts.firstOrNull() ?: "this concept"} like building with standardized LEGO bricks: each component contract guarantees seamless composition downstream.
        
        👉 *Ready for a quick 3-question quiz on this module to test your readiness?*
        """.trimIndent()
    }

    /**
     * Tutor Agent: Generates quiz questions via authenticated backend endpoint /api/tutor/quiz.
     */
    suspend fun tutorGenerateQuiz(module: LearningModule): List<ModuleQuizQuestion> = withContext(Dispatchers.IO) {
        try {
            val req = JSONObject().apply {
                put("moduleId", module.id)
                put("title", module.title)
                put("description", module.description)
                put("skills", JSONArray(module.skills))
                put("count", 3)
            }
            val resp = BackendClient.post("api/tutor/quiz", req)
            val arr = resp.optJSONArray("questions")
            if (arr != null && arr.length() > 0) {
                val generatedQuestions = mutableListOf<ModuleQuizQuestion>()
                for (i in 0 until arr.length()) {
                    val qObj = arr.getJSONObject(i)
                    val opts = mutableListOf<String>()
                    val optArr = qObj.optJSONArray("options")
                    if (optArr != null) {
                        for (j in 0 until optArr.length()) opts.add(optArr.getString(j))
                    }
                    generatedQuestions.add(
                        ModuleQuizQuestion(
                            id = qObj.optString("id", "${module.id}_q$i"),
                            question = qObj.optString("question"),
                            options = opts,
                            correctIndex = qObj.optInt("correctIndex", 0),
                            explanation = qObj.optString("explanation")
                        )
                    )
                }
                if (generatedQuestions.isNotEmpty()) return@withContext generatedQuestions
            }
        } catch (e: Exception) {
            Log.w("LearningPathService", "Backend tutor quiz call failed: ${e.message}")
        }

        if (!BackendConfig.allowOfflineFallback) {
            return@withContext emptyList()
        }

        // Return verified seed quiz questions
        LearningPathSeedData.getSampleQuizForModule(module.id)
    }

    /**
     * Evaluates quiz submission, updates user_progress.score in Firestore,
     * and automatically marks module completed if score >= 70%.
     */
    suspend fun evaluateQuizSubmission(
        uid: String,
        pathId: String,
        module: LearningModule,
        userAnswers: Map<Int, Int>,
        questions: List<ModuleQuizQuestion>
    ): ModuleQuizSubmission = withContext(Dispatchers.IO) {
        var correctCount = 0
        questions.forEachIndexed { index, question ->
            val userPick = userAnswers[index]
            if (userPick != null && userPick == question.correctIndex) {
                correctCount++
            }
        }
        val total = questions.size
        val scorePercent = if (total > 0) ((correctCount.toFloat() / total) * 100).toInt() else 0
        val passed = scorePercent >= 70

        val feedback = if (passed) {
            "🎉 Excellent work! You scored $scorePercent% ($correctCount/$total correct) on ${module.title}. Concept mastery verified!"
        } else {
            "You scored $scorePercent% ($correctCount/$total). Review the Tutor explanations and retry to achieve the 70% threshold."
        }

        // Update score in Firestore user_progress
        if (uid.isNotBlank()) {
            try {
                val progressDocRef = firestore.collection("user_progress").document("${uid}_${module.id}")
                val updateMap = mutableMapOf<String, Any>(
                    "score" to scorePercent
                )
                if (passed) {
                    updateMap["status"] = "completed"
                    updateMap["completed_at"] = System.currentTimeMillis()
                }
                progressDocRef.set(updateMap, SetOptions.merge()).awaitTask()

                // If passed, unlock the next module automatically
                if (passed) {
                    completeModuleAndUnlockNext(uid, pathId, module.id, scorePercent)
                }
            } catch (e: Exception) {
                Log.w("LearningPathService", "Failed updating user_progress with quiz score", e)
            }
        }

        ModuleQuizSubmission(
            moduleId = module.id,
            totalQuestions = total,
            correctCount = correctCount,
            scorePercentage = scorePercent,
            feedback = feedback
        )
    }

    /**
     * Compute streak (consecutive days with at least one completed task) and all-time totals.
     */
    fun computeStats(tasks: List<FirestoreTask>, activePathId: String, allModules: List<LearningModule>, progressList: List<UserProgressDoc>): DailyLearningStats {
        val completedTasks = tasks.filter { it.status == "done" }
        val totalCompleted = completedTasks.size

        // Calculate consecutive streak days from completed_at timestamps
        val datesWithCompletions = completedTasks
            .mapNotNull { it.completed_at }
            .filter { it > 0 }
            .map { ts ->
                val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                sdf.format(Date(ts))
            }
            .toSet()

        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        var streak = 0
        var checkDate = sdf.format(cal.time)

        // If today has completed task, start streak at 1, else check yesterday
        if (datesWithCompletions.contains(checkDate)) {
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
            while (datesWithCompletions.contains(sdf.format(cal.time))) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
        } else {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            if (datesWithCompletions.contains(sdf.format(cal.time))) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
                while (datesWithCompletions.contains(sdf.format(cal.time))) {
                    streak++
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                }
            } else {
                streak = if (totalCompleted > 0) 1 else 0
            }
        }

        // Today's tasks count
        val todayTasks = tasks.filter { it.scheduled_date == "Today" || it.scheduled_date.isBlank() }
        val todayCompleted = todayTasks.count { it.status == "done" }

        // Find active module in current path
        val activePathModules = allModules.filter { it.path_id == activePathId }.sortedBy { it.order_index }
        val inProgressModuleId = progressList
            .firstOrNull { it.path_id == activePathId && it.status == "in_progress" }
            ?.module_id ?: activePathModules.firstOrNull()?.id.orEmpty()

        val currentModule = activePathModules.firstOrNull { it.id == inProgressModuleId }
            ?: activePathModules.firstOrNull()

        // Calculate module completion %
        val moduleTasks = tasks.filter { it.module_id == currentModule?.id }
        val moduleProgress = if (moduleTasks.isNotEmpty()) {
            val done = moduleTasks.count { it.status == "done" }
            ((done.toFloat() / moduleTasks.size) * 100).toInt()
        } else {
            val progressDoc = progressList.firstOrNull { it.module_id == currentModule?.id }
            if (progressDoc?.status == "completed") 100 else 25
        }

        val pathName = LearningPathSeedData.initialPaths.firstOrNull { it.id == activePathId }?.name ?: "Python"

        return DailyLearningStats(
            currentStreakDays = maxOf(streak, if (totalCompleted > 0) 1 else 0),
            totalTasksCompleted = totalCompleted,
            todayCompletedTasks = todayCompleted,
            todayTotalTasks = todayTasks.size,
            activePathName = pathName,
            currentModuleTitle = currentModule?.title ?: "Foundations",
            currentModuleProgress = moduleProgress
        )
    }
}
