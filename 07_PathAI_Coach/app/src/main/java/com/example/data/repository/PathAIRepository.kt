package com.example.data.repository

import com.example.data.local.PathAIDao
import com.example.data.local.SeedData
import com.example.data.model.*
import com.example.data.remote.GeminiCoachService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class PathAIRepository(
    private val dao: PathAIDao,
    private val geminiService: GeminiCoachService = GeminiCoachService()
) {
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val skillGaps: Flow<List<SkillGap>> = dao.getAllSkillGaps()
    val roadmapWeeks: Flow<List<RoadmapWeek>> = dao.getAllRoadmapWeeks()
    val tasks: Flow<List<TaskItem>> = dao.getAllTasks()
    val jobs: Flow<List<JobItem>> = dao.getAllJobs()
    val practiceItems: Flow<List<PracticeItem>> = dao.getAllPracticeItems()
    val quizzes: Flow<List<QuizItem>> = dao.getAllQuizzes()
    val quizLogs: Flow<List<QuizLog>> = dao.getAllQuizLogs()
    val interviews: Flow<List<InterviewSession>> = dao.getAllInterviews()
    val projects: Flow<List<ProjectItem>> = dao.getAllProjects()

    suspend fun checkAndSeedDatabase() {
        val existingProfile = dao.getUserProfileOnce()
        if (existingProfile == null) {
            dao.insertUserProfile(SeedData.initialProfile)
            dao.insertSkillGaps(SeedData.initialSkillGaps)
            dao.insertRoadmapWeeks(SeedData.initialRoadmapWeeks)
            dao.insertTasks(SeedData.initialTasks)
            dao.insertPracticeItems(SeedData.initialPracticeItems)
            dao.insertQuizzes(SeedData.initialQuizzes)
            dao.insertProjects(SeedData.initialProjects)
            SeedData.initialJobs.forEach { dao.insertJob(it) }
        }
    }

    suspend fun resetToSeedData() {
        dao.clearSkillGaps()
        dao.clearRoadmapWeeks()
        dao.clearTasks()
        dao.insertUserProfile(SeedData.initialProfile)
        dao.insertSkillGaps(SeedData.initialSkillGaps)
        dao.insertRoadmapWeeks(SeedData.initialRoadmapWeeks)
        dao.insertTasks(SeedData.initialTasks)
        dao.insertPracticeItems(SeedData.initialPracticeItems)
        dao.insertQuizzes(SeedData.initialQuizzes)
        dao.insertProjects(SeedData.initialProjects)
        SeedData.initialJobs.forEach { dao.insertJob(it) }
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        dao.insertUserProfile(profile)
    }

    suspend fun updateTaskStatus(task: TaskItem, isCompleted: Boolean) {
        val updated = task.copy(
            isCompleted = isCompleted,
            completedAt = if (isCompleted) System.currentTimeMillis() else 0L
        )
        dao.updateTask(updated)
    }

    suspend fun addTask(task: TaskItem) {
        dao.insertTask(task)
    }

    suspend fun updateJobStatus(job: JobItem, newStatus: String) {
        dao.updateJob(job.copy(status = newStatus, updatedTimestamp = System.currentTimeMillis()))
    }

    suspend fun saveJob(job: JobItem): Long {
        return dao.insertJob(job)
    }

    suspend fun deleteJob(job: JobItem) {
        dao.deleteJob(job)
    }

    suspend fun updatePracticeItemStatus(item: PracticeItem, isSolved: Boolean) {
        dao.updatePracticeItem(item.copy(isSolved = isSolved))
    }

    suspend fun recordQuizAnswer(quizId: Long, selectedIndex: Int, isCorrect: Boolean) {
        dao.insertQuizLog(QuizLog(quizId = quizId, selectedIndex = selectedIndex, isCorrect = isCorrect))
    }

    suspend fun saveInterviewSession(session: InterviewSession): Long {
        return dao.insertInterview(session)
    }

    suspend fun updateRoadmapWeek(week: RoadmapWeek) {
        dao.updateRoadmapWeek(week)
    }

    suspend fun updateSkillGap(gap: SkillGap) {
        dao.updateSkillGap(gap)
    }

    // Gemini-powered operations
    suspend fun runSkillGapAnalysis(
        role: String,
        tier: String,
        skills: String,
        resume: String
    ): List<SkillGap> {
        val gaps = geminiService.analyzeSkillGaps(role, tier, skills, resume)
        dao.clearSkillGaps()
        dao.insertSkillGaps(gaps)
        return gaps
    }

    suspend fun generateFullRoadmap(
        role: String,
        deadlineWeeks: Int,
        hoursPerDay: Int,
        gaps: List<SkillGap>
    ): List<RoadmapWeek> {
        val weeks = geminiService.generateRoadmap(role, deadlineWeeks, hoursPerDay, gaps)
        dao.clearRoadmapWeeks()
        dao.insertRoadmapWeeks(weeks)
        return weeks
    }

    suspend fun performAdaptiveReschedule(hoursPerDay: Int): List<TaskItem> {
        val allTasks = dao.getAllTasks().firstOrNull().orEmpty()
        val missed = allTasks.filter { !it.isCompleted && it.isMissed }
        val toReschedule = if (missed.isNotEmpty()) missed else allTasks.filter { !it.isCompleted }.take(2)
        val rescheduled = geminiService.rescheduleMissedTasks(toReschedule, hoursPerDay)
        dao.insertTasks(rescheduled)
        return rescheduled
    }

    suspend fun evaluateMockInterview(
        role: String,
        topic: String,
        question: String,
        candidateAnswer: String
    ): Triple<Int, String, String> {
        return geminiService.evaluateInterviewResponse(role, topic, question, candidateAnswer)
    }

    suspend fun tailorResume(
        profile: UserProfile,
        company: String,
        role: String,
        jdText: String
    ): String {
        return geminiService.tailorResumeForJob(
            candidateProfile = profile.resumeSummary,
            candidateSkills = profile.skillsText,
            company = company,
            role = role,
            jobDescription = jdText
        )
    }

    suspend fun getNextBestAction(role: String, gaps: List<SkillGap>, tasks: List<TaskItem>): String {
        val unaddressed = gaps.filter { !it.isAddressed && it.priority == "HIGH" }
        val pending = tasks.filter { !it.isCompleted }
        return geminiService.calculateNextBestAction(role, unaddressed, pending)
    }
}
