package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PathAIDao {
    // UserProfile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    // SkillGap
    @Query("SELECT * FROM skill_gaps ORDER BY CASE priority WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 ELSE 3 END")
    fun getAllSkillGaps(): Flow<List<SkillGap>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkillGaps(gaps: List<SkillGap>)

    @Query("DELETE FROM skill_gaps")
    suspend fun clearSkillGaps()

    @Update
    suspend fun updateSkillGap(gap: SkillGap)

    // RoadmapWeek
    @Query("SELECT * FROM roadmap_weeks ORDER BY weekNumber ASC")
    fun getAllRoadmapWeeks(): Flow<List<RoadmapWeek>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoadmapWeeks(weeks: List<RoadmapWeek>)

    @Query("DELETE FROM roadmap_weeks")
    suspend fun clearRoadmapWeeks()

    @Update
    suspend fun updateRoadmapWeek(week: RoadmapWeek)

    // TaskItem
    @Query("SELECT * FROM tasks ORDER BY weekNumber ASC, dayNumber ASC, id ASC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE weekNumber = :week ORDER BY dayNumber ASC, id ASC")
    fun getTasksForWeek(week: Int): Flow<List<TaskItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem)

    @Update
    suspend fun updateTask(task: TaskItem)

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()

    // JobItem
    @Query("SELECT * FROM jobs ORDER BY updatedTimestamp DESC")
    fun getAllJobs(): Flow<List<JobItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobItem): Long

    @Update
    suspend fun updateJob(job: JobItem)

    @Delete
    suspend fun deleteJob(job: JobItem)

    // PracticeItem
    @Query("SELECT * FROM practice_items ORDER BY id ASC")
    fun getAllPracticeItems(): Flow<List<PracticeItem>>

    @Query("SELECT * FROM practice_items WHERE category = :category ORDER BY id ASC")
    fun getPracticeItemsByCategory(category: String): Flow<List<PracticeItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPracticeItems(items: List<PracticeItem>)

    @Update
    suspend fun updatePracticeItem(item: PracticeItem)

    // QuizItem
    @Query("SELECT * FROM quizzes ORDER BY id ASC")
    fun getAllQuizzes(): Flow<List<QuizItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizzes(quizzes: List<QuizItem>)

    // QuizLog
    @Query("SELECT * FROM quiz_logs ORDER BY timestamp DESC")
    fun getAllQuizLogs(): Flow<List<QuizLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizLog(log: QuizLog)

    // InterviewSession
    @Query("SELECT * FROM interviews ORDER BY timestamp DESC")
    fun getAllInterviews(): Flow<List<InterviewSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterview(interview: InterviewSession): Long

    // ProjectItem
    @Query("SELECT * FROM projects ORDER BY id ASC")
    fun getAllProjects(): Flow<List<ProjectItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<ProjectItem>)

    @Update
    suspend fun updateProject(project: ProjectItem)
}
