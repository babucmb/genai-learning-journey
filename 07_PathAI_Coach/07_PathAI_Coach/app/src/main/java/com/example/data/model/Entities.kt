package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Fresher Candidate",
    val targetRole: String = "AI / ML Engineer",
    val targetCompanyTier: String = "Tier 1 & High Growth Startups",
    val deadlineWeeks: Int = 12,
    val hoursPerDay: Int = 4,
    val skillsText: String = "Python, NumPy, Pandas, Basic PyTorch, Git, Data Structures",
    val resumeSummary: String = "CS graduate with academic project in CNN image classification, basic REST APIs, and foundational DSA in Python/C++.",
    val currentStreak: Int = 3,
    val longestStreak: Int = 7,
    val lastActiveEpochDay: Long = 0L,
    val isOnboarded: Boolean = true
)

@Entity(tableName = "skill_gaps")
data class SkillGap(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "DSA", "Machine Learning", "LLM / RAG", "System Design", "SQL & Data", "MLOps"
    val skillName: String,
    val priority: String, // "HIGH", "MEDIUM", "LOW"
    val currentLevel: String, // "None", "Beginner", "Intermediate"
    val targetRequirement: String,
    val gapDescription: String,
    val recommendedAction: String,
    val isAddressed: Boolean = false
)

@Entity(tableName = "roadmap_weeks")
data class RoadmapWeek(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekNumber: Int,
    val title: String,
    val focusDomain: String, // e.g., "DSA & Foundations", "Core ML", "LLM/RAG", "System Design"
    val summary: String,
    val keyMilestone: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekNumber: Int,
    val dayNumber: Int, // 1 to 7
    val title: String,
    val description: String,
    val deliverable: String,
    val category: String, // "DSA", "ML", "LLM", "SQL", "PROJECT", "INTERVIEW"
    val estimatedMinutes: Int = 60,
    val isCompleted: Boolean = false,
    val isMissed: Boolean = false,
    val dueDateText: String = "Today",
    val completedAt: Long = 0L
)

@Entity(tableName = "jobs")
data class JobItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val company: String,
    val role: String,
    val status: String, // "SAVED", "APPLIED", "INTERVIEW", "OFFER", "REJECTED"
    val location: String = "Remote / Hybrid",
    val salaryRange: String = "$90,000 - $125,000",
    val jdText: String = "",
    val tailoredResumeNotes: String = "",
    val notes: String = "",
    val appliedDate: String = "Just now",
    val updatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "practice_items")
data class PracticeItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "DSA", "ML_CODING", "MINI_PROJECT"
    val topic: String, // e.g. "Arrays & Hashmaps", "Trees", "Loss Functions", "LoRA Fine-tuning"
    val title: String,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val description: String,
    val hints: String,
    val starterCode: String,
    val solutionCode: String,
    val testCases: String = "Input: nums = [2,7,11,15], target = 9 -> Output: [0,1]",
    val isSolved: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "quizzes")
data class QuizItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "ML_CORE", "LLM_RAG", "DSA", "SYSTEM_DESIGN"
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctIndex: Int, // 0, 1, 2, 3
    val explanation: String
)

@Entity(tableName = "quiz_logs")
data class QuizLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizId: Long,
    val selectedIndex: Int,
    val isCorrect: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "interviews")
data class InterviewSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,
    val topic: String,
    val mode: String, // "TEXT" or "VOICE"
    val transcriptJson: String, // JSON array of { "speaker": "AI"|"USER", "text": "..." }
    val overallScore: Int, // e.g. 85 / 100
    val feedback: String,
    val followUpQuestions: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "projects")
data class ProjectItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val domain: String, // "LLM / RAG", "Computer Vision", "MLOps", "Recommendation"
    val description: String,
    val techStack: String,
    val deliverables: String,
    val status: String = "NOT_STARTED", // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
    val githubUrl: String = ""
)
