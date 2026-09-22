package com.example.data.model

data class LearningPath(
    val id: String = "",
    val name: String = "", // "Python", "AI/ML", "LLM/RAG", "Projects"
    val description: String = "",
    val order_index: Int = 0,
    val modules: List<String> = emptyList(), // array of module_ids
    val total_estimated_hours: Int = 0
)

data class LearningModule(
    val id: String = "",
    val path_id: String = "",
    val title: String = "",
    val description: String = "",
    val order_index: Int = 0,
    val prerequisites: List<String> = emptyList(), // array of module_ids
    val content: String = "", // rich text / sections
    val estimated_hours: Int = 4,
    val skills: List<String> = emptyList(),
    val related_concepts: List<String> = emptyList()
)

data class UserProgressDoc(
    val id: String = "", // "${uid}_${module_id}"
    val uid: String = "",
    val path_id: String = "",
    val module_id: String = "",
    val status: String = "locked", // "locked" | "in_progress" | "completed"
    val started_at: Long? = null,
    val completed_at: Long? = null,
    val score: Int = 0, // quiz score
    val last_position: Int = 0
)

data class FirestoreTask(
    val id: String = "",
    val uid: String = "",
    val module_id: String = "",
    val title: String = "",
    val description: String = "",
    val estimate_minutes: Int = 45,
    val status: String = "todo", // "todo" | "done"
    val scheduled_date: String = "Today",
    val completed_at: Long? = null,
    val task_type: String = "PRACTICE", // "READING", "PRACTICE", "QUIZ", "PROJECT"
    val source: String = "llm_generated" // "llm_generated" | "template_fallback"
)

data class ModuleQuizQuestion(
    val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val explanation: String = ""
)

data class ModuleQuizSubmission(
    val moduleId: String,
    val totalQuestions: Int,
    val correctCount: Int,
    val scorePercentage: Int,
    val feedback: String
)

data class PathCompletionStats(
    val path: LearningPath,
    val totalModules: Int,
    val completedModules: Int,
    val inProgressModules: Int = 0,
    val percentCompleted: Int,
    val isPrimary: Boolean = false
)

data class DailyLearningStats(
    val currentStreakDays: Int = 0,
    val totalTasksCompleted: Int = 0,
    val todayCompletedTasks: Int = 0,
    val todayTotalTasks: Int = 0,
    val activePathName: String = "",
    val currentModuleTitle: String = "",
    val currentModuleProgress: Int = 0
)
