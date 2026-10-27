package com.example.data.model

data class TechRadarItem(
    val id: String = "",
    val title: String = "",
    val domain: String = "", // "LLM / GenAI", "Tooling & Infra", "Job Market", "Research", "Python", "AI/ML"
    val summary: String = "",
    val impact: String = "", // "High Impact", "Trending", "Hot Skill"
    val source: String = "",
    val timeAgo: String = "",
    val isLiked: Boolean = false
)

data class UserEducation(
    val degree: String = "B.Tech",
    val major: String = "Computer Science & Engineering",
    val college: String = "National Institute of Technology",
    val graduation_year: String = "2025",
    val cgpa: String = "8.6 / 10"
)

data class UserJobPreferences(
    val roles: List<String> = listOf("AI/ML Intern", "Python Developer", "Data Science Intern"),
    val locations: List<String> = listOf("Remote", "Bengaluru", "Hyderabad"),
    val work_mode: List<String> = listOf("Remote", "Hybrid"),
    val min_stipend_or_salary: String = "₹25,000 / month",
    val willing_to_relocate: Boolean = true
)

data class NotificationPreferences(
    val job_alerts_enabled: Boolean = true,
    val alert_time: String = "09:00",
    val timezone: String = "Asia/Kolkata"
)

data class FirebaseUserData(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val education: UserEducation = UserEducation(),
    val skills: List<String> = listOf("Python", "PyTorch", "NumPy", "Pandas", "Scikit-Learn", "Git", "REST APIs"),
    val experience_level: String = "Fresher", // "Fresher", "Intern", "0–1 YOE", "1–3 YOE"
    val job_preferences: UserJobPreferences = UserJobPreferences(),
    val resume_url: String? = null,
    val notifications: NotificationPreferences = NotificationPreferences(),
    val focusAreas: List<String> = listOf("Python", "AI/ML"),
    val currentPath: String = "AIML",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class JobPost(
    val id: String = "",
    val source: String = "TheirStack", // "TheirStack", "Internshala", "Freshersworld", "JSearch"
    val title: String = "",
    val company: String = "",
    val location: String = "Remote / Bengaluru",
    val job_type: String = "Internship", // "Internship", "Fresher", "Apprentice", "0–1 YOE"
    val description: String = "",
    val required_skills: List<String> = emptyList(),
    val experience_level: String = "Fresher", // "Intern", "Fresher", "Apprentice", "0–1 YOE"
    val posted_at: String = "2 days ago",
    val posted_days_ago: Int = 2,
    val apply_url: String = "https://careers.example.com",
    val raw_data: Map<String, Any?> = emptyMap(),
    val indexed_at: Long = System.currentTimeMillis(),
    val ai_summary: String = ""
)

data class JobMatch(
    val id: String = "",
    val uid: String = "",
    val job_id: String = "",
    val match_score: Int = 85,
    val matched_skills: List<String> = emptyList(),
    val missing_skills: List<String> = emptyList(),
    val status: String = "new", // "new" | "viewed" | "applied" | "ignored"
    val notified_at: Long = System.currentTimeMillis(),
    val why_it_matches: String = ""
)

data class MatchedJobItem(
    val match: JobMatch,
    val post: JobPost
)

data class JobAlertsLog(
    val id: String = "",
    val uid: String = "",
    val run_date: String = "",
    val jobs_found_count: Int = 0,
    val top_job_ids: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class AppNotification(
    val id: String = "",
    val uid: String = "",
    val title: String = "",
    val body: String = "",
    val screen: String = "Jobs",
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class RadarFeedbackDoc(
    val id: String = "",
    val userId: String = "",
    val radarId: String = "",
    val liked: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

data class AgentItem(
    val id: String,
    val name: String,
    val roleTitle: String,
    val iconName: String, // "Coach", "Tutor", "Project", "Research"
    val description: String,
    val status: String = "ACTIVE", // "ACTIVE", "READY", "WORKING"
    val activeTask: String,
    val samplePrompts: List<String>
)

data class AgentChatMessage(
    val id: String,
    val agentId: String,
    val sender: String, // "USER" or "AGENT"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
