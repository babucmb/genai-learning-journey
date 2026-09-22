package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AgentSeedData
import com.example.data.local.PathAIDatabase
import com.example.data.local.SeedData
import com.example.data.model.*
import com.example.data.remote.AgentService
import com.example.data.remote.FirebaseAuthService
import com.example.data.repository.PathAIRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppNavTab(val label: String) {
    DASHBOARD("Dashboard"),
    TODAY("Today"),
    LEARNING("Learning"),
    AGENTS("Agents"),
    ROADMAP("Roadmap"),
    PRACTICE("Practice"),
    INTERVIEWS("Interviews"),
    JOBS("Jobs"),
    SKILL_MAP("Skill Map"),
    SETTINGS("Settings"),
    NOTIFICATIONS("Notifications")
}

class PathAIViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PathAIRepository
    private val authService = FirebaseAuthService()
    private val jobService = com.example.data.remote.JobService()
    private val learningService = com.example.data.remote.LearningPathService()

    // Structured Learning Paths & Daily Modules State
    private val _learningPaths = MutableStateFlow<List<LearningPath>>(com.example.data.local.LearningPathSeedData.initialPaths)
    val learningPaths: StateFlow<List<LearningPath>> = _learningPaths.asStateFlow()

    private val _activePathId = MutableStateFlow("python")
    val activePathId: StateFlow<String> = _activePathId.asStateFlow()

    private val _allModules = MutableStateFlow<List<LearningModule>>(com.example.data.local.LearningPathSeedData.initialModules)
    val allModules: StateFlow<List<LearningModule>> = _allModules.asStateFlow()

    private val _userProgress = MutableStateFlow<List<UserProgressDoc>>(emptyList())
    val userProgress: StateFlow<List<UserProgressDoc>> = _userProgress.asStateFlow()

    private val _firestoreTasks = MutableStateFlow<List<FirestoreTask>>(emptyList())
    val firestoreTasks: StateFlow<List<FirestoreTask>> = _firestoreTasks.asStateFlow()

    private val _dailyLearningStats = MutableStateFlow(DailyLearningStats())
    val dailyLearningStats: StateFlow<DailyLearningStats> = _dailyLearningStats.asStateFlow()

    val activePath: StateFlow<LearningPath?> = combine(_learningPaths, _activePathId) { paths, pathId ->
        paths.firstOrNull { it.id == pathId } ?: paths.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.local.LearningPathSeedData.initialPaths.firstOrNull())

    val currentActiveModule: StateFlow<LearningModule?> = combine(_allModules, _activePathId, _userProgress) { mods, pathId, progressList ->
        val pathMods = mods.filter { it.path_id == pathId }.sortedBy { it.order_index }
        val inProgId = progressList.firstOrNull { it.path_id == pathId && it.status == "in_progress" }?.module_id
        pathMods.firstOrNull { it.id == inProgId } ?: pathMods.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.local.LearningPathSeedData.initialModules.firstOrNull())

    val pathStatsList: StateFlow<List<PathCompletionStats>> = combine(_learningPaths, _allModules, _userProgress, _activePathId) { paths, mods, progressList, activeId ->
        paths.map { path ->
            val pMods = mods.filter { it.path_id == path.id }
            val completed = pMods.count { mod ->
                progressList.any { it.module_id == mod.id && it.status == "completed" }
            }
            val inProgress = pMods.count { mod ->
                progressList.any { it.module_id == mod.id && it.status == "in_progress" }
            }
            val total = if (pMods.isNotEmpty()) pMods.size else path.modules.size
            val pct = if (total > 0) ((completed.toFloat() / total) * 100).toInt() else 0
            PathCompletionStats(
                path = path,
                completedModules = completed,
                inProgressModules = inProgress,
                totalModules = total,
                percentCompleted = pct,
                isPrimary = path.id == activeId
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tutor Agent Modal & Quiz State
    private val _tutorModalModule = MutableStateFlow<LearningModule?>(null)
    val tutorModalModule: StateFlow<LearningModule?> = _tutorModalModule.asStateFlow()

    private val _tutorExplanation = MutableStateFlow<String?>(null)
    val tutorExplanation: StateFlow<String?> = _tutorExplanation.asStateFlow()

    private val _isTutorExplaining = MutableStateFlow(false)
    val isTutorExplaining: StateFlow<Boolean> = _isTutorExplaining.asStateFlow()

    private val _moduleQuizQuestions = MutableStateFlow<List<ModuleQuizQuestion>>(emptyList())
    val moduleQuizQuestions: StateFlow<List<ModuleQuizQuestion>> = _moduleQuizQuestions.asStateFlow()

    private val _moduleQuizSubmission = MutableStateFlow<ModuleQuizSubmission?>(null)
    val moduleQuizSubmission: StateFlow<ModuleQuizSubmission?> = _moduleQuizSubmission.asStateFlow()

    private val _isGeneratingQuiz = MutableStateFlow(false)
    val isGeneratingQuiz: StateFlow<Boolean> = _isGeneratingQuiz.asStateFlow()

    private val _firebaseUser = MutableStateFlow<FirebaseUserData?>(null)
    val firebaseUser: StateFlow<FirebaseUserData?> = _firebaseUser.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _radarFeedback = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val radarFeedback: StateFlow<Map<String, Boolean>> = _radarFeedback.asStateFlow()

    // Jobs & Live Job Search Agent State
    private val _matchedJobs = MutableStateFlow<List<MatchedJobItem>>(emptyList())
    val matchedJobs: StateFlow<List<MatchedJobItem>> = _matchedJobs.asStateFlow()

    private val _isSearchingJobs = MutableStateFlow(false)
    val isSearchingJobs: StateFlow<Boolean> = _isSearchingJobs.asStateFlow()

    private val _jobSearchQuery = MutableStateFlow("")
    val jobSearchQuery: StateFlow<String> = _jobSearchQuery.asStateFlow()

    private val _roleFilter = MutableStateFlow("ALL")
    val roleFilter: StateFlow<String> = _roleFilter.asStateFlow()

    private val _locationFilter = MutableStateFlow("ALL")
    val locationFilter: StateFlow<String> = _locationFilter.asStateFlow()

    private val _jobTypeFilter = MutableStateFlow("ALL")
    val jobTypeFilter: StateFlow<String> = _jobTypeFilter.asStateFlow()

    private val _postedWithinDays = MutableStateFlow(7)
    val postedWithinDays: StateFlow<Int> = _postedWithinDays.asStateFlow()

    // Notifications State
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    val unreadNotificationsCount: StateFlow<Int> = _notifications.map { list ->
        list.count { !it.read }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)


    init {
        val db = PathAIDatabase.getDatabase(application)
        repository = PathAIRepository(db.dao())
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
            learningService.seedCurriculumIfEmpty()
        }

        // Observe real-time learning paths
        viewModelScope.launch {
            learningService.observeLearningPaths().collect { paths ->
                _learningPaths.value = paths
            }
        }

        val current = authService.currentUser
        if (current != null) {
            viewModelScope.launch {
                loadFirebaseProfile(current)
            }
        }
    }

    private suspend fun loadFirebaseProfile(user: FirebaseUser) {
        val data = authService.getOrCreateUserDocument(user)
        _firebaseUser.value = data

        val initialPath = if (data.currentPath.isNotBlank()) data.currentPath else "python"
        _activePathId.value = initialPath

        // Observe modules for active path
        viewModelScope.launch {
            _activePathId.collectLatest { pathId ->
                learningService.observeModules(pathId).collect { mods ->
                    _allModules.value = mods
                }
            }
        }

        // Observe user progress for active path
        viewModelScope.launch {
            _activePathId.collectLatest { pathId ->
                learningService.observeUserProgress(data.uid, pathId).collect { progressList ->
                    _userProgress.value = progressList
                    if (progressList.isEmpty()) {
                        // Initialize user progress and initial tasks for first module
                        learningService.selectOrSwitchPath(data.uid, pathId)
                    }
                }
            }
        }

        // Observe daily tasks for user and recompute stats
        viewModelScope.launch {
            learningService.observeUserTasks(data.uid).collect { tasks ->
                _firestoreTasks.value = tasks
                val stats = learningService.computeStats(
                    tasks = tasks,
                    activePathId = _activePathId.value,
                    allModules = _allModules.value,
                    progressList = _userProgress.value
                )
                _dailyLearningStats.value = stats
            }
        }

        // Load radar feedback from Firestore for this user
        val feedback = agentService.loadRadarFeedback(data.uid)
        _radarFeedback.value = feedback

        // Load chat history for each agent from Firestore
        listOf("coach", "tutor", "project", "research").forEach { agentId ->
            val history = agentService.loadChatHistory(data.uid, agentId)
            if (history.isNotEmpty()) {
                val current = _agentChatMessages.value
                _agentChatMessages.value = current + (agentId to history)
            }
        }

        // Apply radar liked state
        filterRadarForUser(data.focusAreas)

        // Observe real-time job matches from Firestore
        viewModelScope.launch {
            jobService.observeUserJobMatches(data.uid).collect { matches ->
                _matchedJobs.value = matches
                if (matches.isEmpty()) {
                    runJobSearch()
                }
            }
        }

        // Observe real-time in-app notifications
        viewModelScope.launch {
            jobService.observeNotifications(data.uid).collect { notifs ->
                _notifications.value = notifs
            }
        }
    }

    fun saveFullUserProfile(profile: FirebaseUserData) {
        viewModelScope.launch {
            _isAILoading.value = true
            val (isValid, validationMsg) = authService.validateProfileForJobAlerts(profile)
            if (profile.notifications.job_alerts_enabled && !isValid) {
                _statusNotice.value = "Note: $validationMsg"
            }

            val result = authService.saveFullUserProfile(profile)
            result.onSuccess {
                _firebaseUser.value = profile
                _statusNotice.value = "Profile & Job Alert Preferences saved to Firebase!"
                runJobSearch()
            }.onFailure { ex ->
                _statusNotice.value = "Failed saving profile: ${ex.message}"
            }
            _isAILoading.value = false
        }
    }

    fun runJobSearch(query: String = _jobSearchQuery.value) {
        val user = _firebaseUser.value ?: return
        viewModelScope.launch {
            _isSearchingJobs.value = true
            try {
                val results = jobService.runJobSearchAgent(
                    user = user,
                    naturalLanguageQuery = query,
                    roleFilter = _roleFilter.value,
                    locationFilter = _locationFilter.value,
                    jobTypeFilter = _jobTypeFilter.value,
                    postedWithinDays = _postedWithinDays.value
                )
                if (results.isNotEmpty()) {
                    _matchedJobs.value = results
                }
            } catch (e: Exception) {
                android.util.Log.w("PathAIViewModel", "Error running job search agent", e)
            } finally {
                _isSearchingJobs.value = false
            }
        }
    }

    fun updateJobMatchStatus(matchId: String, newStatus: String) {
        viewModelScope.launch {
            jobService.updateMatchStatus(matchId, newStatus)
            val current = _matchedJobs.value
            _matchedJobs.value = current.map { item ->
                if (item.match.id == matchId) {
                    item.copy(match = item.match.copy(status = newStatus))
                } else item
            }
        }
    }

    fun triggerDaily9AMJobAlert() {
        val user = _firebaseUser.value ?: return
        viewModelScope.launch {
            _isAILoading.value = true
            val notif = jobService.triggerDaily9AMJobAlert(user)
            _isAILoading.value = false
            if (notif != null) {
                _statusNotice.value = "9:00 AM Alert Triggered! In-app notification created & logged."
            } else {
                _statusNotice.value = "Daily alert ran successfully."
            }
        }
    }

    fun markNotificationRead(notifId: String) {
        viewModelScope.launch {
            jobService.markNotificationRead(notifId)
            val current = _notifications.value
            _notifications.value = current.map {
                if (it.id == notifId) it.copy(read = true) else it
            }
        }
    }

    fun setJobSearchQuery(query: String) {
        _jobSearchQuery.value = query
    }

    fun setJobFilters(role: String, location: String, jobType: String, days: Int) {
        _roleFilter.value = role
        _locationFilter.value = location
        _jobTypeFilter.value = jobType
        _postedWithinDays.value = days
        runJobSearch()
    }


    private fun filterRadarForUser(focusAreas: List<String>) {
        val currentRadar = _techRadar.value
        val feedback = _radarFeedback.value
        val updated = currentRadar.map { item ->
            item.copy(isLiked = feedback[item.id] == true)
        }
        _techRadar.value = updated
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            val result = authService.signInWithEmail(email, pass)
            result.onSuccess { user ->
                loadFirebaseProfile(user)
            }.onFailure { ex ->
                _authErrorMessage.value = ex.localizedMessage ?: "Failed to sign in. Please verify your email and password."
            }
            _isAuthLoading.value = false
        }
    }

    fun signUpWithEmail(email: String, pass: String, name: String) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            val result = authService.signUpWithEmail(email, pass, name)
            result.onSuccess { user ->
                loadFirebaseProfile(user)
            }.onFailure { ex ->
                _authErrorMessage.value = ex.localizedMessage ?: "Failed to create account. Password must be at least 6 characters."
            }
            _isAuthLoading.value = false
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            val result = authService.signInWithGoogle(context)
            result.onSuccess { user ->
                loadFirebaseProfile(user)
            }.onFailure { ex ->
                _authErrorMessage.value = ex.localizedMessage ?: "Google Sign-In was cancelled or unavailable on this device."
            }
            _isAuthLoading.value = false
        }
    }

    fun quickDemoSignIn() {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            val result = authService.signInWithEmail("candidate.demo@pathaicoach.com", "DemoPass123!")
            result.onSuccess { user ->
                loadFirebaseProfile(user)
            }.onFailure {
                val signUpResult = authService.signUpWithEmail("candidate.demo@pathaicoach.com", "DemoPass123!", "Alex Chen (Demo)")
                signUpResult.onSuccess { user ->
                    loadFirebaseProfile(user)
                }.onFailure {
                    // Fallback profile if offline
                    val demoUser = FirebaseUserData(
                        uid = "demo_fresher_01",
                        name = "Alex Chen (Demo)",
                        email = "candidate.demo@pathaicoach.com",
                        focusAreas = listOf("Python", "AI/ML", "LLM / RAG"),
                        currentPath = "AIML"
                    )
                    _firebaseUser.value = demoUser
                }
            }
            _isAuthLoading.value = false
        }
    }

    fun signOut() {
        authService.signOut()
        _firebaseUser.value = null
        _selectedAgent.value = null
        _currentTab.value = AppNavTab.DASHBOARD
    }

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    fun updateUserFocusAreas(focusAreas: List<String>, currentPath: String) {
        val user = _firebaseUser.value ?: return
        val updated = user.copy(focusAreas = focusAreas, currentPath = currentPath, updatedAt = System.currentTimeMillis())
        _firebaseUser.value = updated
        viewModelScope.launch {
            authService.updateUserFocusAreas(user.uid, focusAreas, currentPath)
            agentService.savePlanForUser(user.uid, currentPath, focusAreas, "Roadmap track updated to $currentPath")
            filterRadarForUser(focusAreas)
        }
    }

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val skillGaps: StateFlow<List<SkillGap>> = repository.skillGaps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val roadmapWeeks: StateFlow<List<RoadmapWeek>> = repository.roadmapWeeks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskItem>> = repository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val jobs: StateFlow<JobItem?> = repository.jobs.map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allJobs: StateFlow<List<JobItem>> = repository.jobs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val practiceItems: StateFlow<List<PracticeItem>> = repository.practiceItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizzes: StateFlow<List<QuizItem>> = repository.quizzes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizLogs: StateFlow<List<QuizLog>> = repository.quizLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val interviews: StateFlow<List<InterviewSession>> = repository.interviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projects: StateFlow<List<ProjectItem>> = repository.projects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state
    private val _currentTab = MutableStateFlow(AppNavTab.DASHBOARD)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    private val _isAILoading = MutableStateFlow(false)
    val isAILoading: StateFlow<Boolean> = _isAILoading.asStateFlow()

    private val _statusNotice = MutableStateFlow<String?>(null)
    val statusNotice: StateFlow<String?> = _statusNotice.asStateFlow()

    private val _nextBestAction = MutableStateFlow(
        "Next Best Action: Implement Multi-Head Attention from scratch (Week 3, Day 1). Closes the high-priority gap in Transformer mechanics."
    )
    val nextBestAction: StateFlow<String> = _nextBestAction.asStateFlow()

    // Resume tailoring state
    private val _activeJobForTailoring = MutableStateFlow<JobItem?>(null)
    val activeJobForTailoring: StateFlow<JobItem?> = _activeJobForTailoring.asStateFlow()

    private val _tailoredResumeResult = MutableStateFlow<String?>(null)
    val tailoredResumeResult: StateFlow<String?> = _tailoredResumeResult.asStateFlow()

    // Mock Interview state
    private val _interviewQuestion = MutableStateFlow(
        "Explain the difference between Self-Attention and Cross-Attention in the Transformer architecture, and why is causal masking necessary during decoding?"
    )
    val interviewQuestion: StateFlow<String> = _interviewQuestion.asStateFlow()

    private val _interviewCandidateAnswer = MutableStateFlow("")
    val interviewCandidateAnswer: StateFlow<String> = _interviewCandidateAnswer.asStateFlow()

    private val _interviewEvaluation = MutableStateFlow<Triple<Int, String, String>?>(null)
    val interviewEvaluation: StateFlow<Triple<Int, String, String>?> = _interviewEvaluation.asStateFlow()

    // Multi-Agent & Tech Radar state
    private val agentService = AgentService()

    private val _agents = MutableStateFlow(AgentSeedData.initialAgents)
    val agents: StateFlow<List<AgentItem>> = _agents.asStateFlow()

    private val _selectedAgent = MutableStateFlow<AgentItem?>(null)
    val selectedAgent: StateFlow<AgentItem?> = _selectedAgent.asStateFlow()

    private val _agentChatMessages = MutableStateFlow<Map<String, List<AgentChatMessage>>>(emptyMap())
    val activeAgentChat: StateFlow<List<AgentChatMessage>> = combine(_selectedAgent, _agentChatMessages) { agent, map ->
        if (agent != null) map[agent.id] ?: emptyList() else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _techRadar = MutableStateFlow(AgentSeedData.initialTechRadar)
    val techRadar: StateFlow<List<TechRadarItem>> = _techRadar.asStateFlow()

    private val _isAgentThinking = MutableStateFlow(false)
    val isAgentThinking: StateFlow<Boolean> = _isAgentThinking.asStateFlow()

    fun selectAgent(agent: AgentItem?) {
        _selectedAgent.value = agent
    }

    fun sendAgentMessage(userText: String) {
        val currentAgent = _selectedAgent.value ?: return
        val userMsg = AgentChatMessage(
            id = System.currentTimeMillis().toString(),
            agentId = currentAgent.id,
            sender = "USER",
            message = userText
        )

        val existing = _agentChatMessages.value[currentAgent.id] ?: emptyList()
        val updated = existing + userMsg
        _agentChatMessages.value = _agentChatMessages.value + (currentAgent.id to updated)

        viewModelScope.launch {
            _isAgentThinking.value = true
            val profile = userProfile.value
            val user = _firebaseUser.value
            val candidateContext = "Target Role: ${profile?.targetRole ?: "AIML Engineer"}, Tier: ${profile?.targetCompanyTier ?: "Tier 1"}, Streak: ${profile?.currentStreak ?: 4} days, Completed Tasks: ${tasks.value.count { it.isCompleted }}/${tasks.value.size}"
            
            val currentMod = currentActiveModule.value
            val currentModInfo = if (currentMod != null && currentAgent.id == "tutor") {
                "${currentMod.title}: ${currentMod.description} (Skills: ${currentMod.skills.joinToString(", ")})"
            } else ""

            val responseText = agentService.chatWithAgent(
                agentId = currentAgent.id,
                userQuery = userText,
                candidateContext = candidateContext,
                userId = user?.uid.orEmpty(),
                focusAreas = user?.focusAreas ?: listOf("Python", "AI/ML"),
                currentPath = user?.currentPath ?: _activePathId.value,
                currentModuleInfo = currentModInfo
            )

            val agentMsg = AgentChatMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                agentId = currentAgent.id,
                sender = "AGENT",
                message = responseText
            )
            val finalMessages = (_agentChatMessages.value[currentAgent.id] ?: emptyList()) + agentMsg
            _agentChatMessages.value = _agentChatMessages.value + (currentAgent.id to finalMessages)
            _isAgentThinking.value = false

            // Persist to Firestore per-user
            user?.uid?.let { uid ->
                agentService.saveChatHistory(uid, currentAgent.id, finalMessages)
            }
        }
    }

    fun toggleRadarLike(radarId: String) {
        val user = _firebaseUser.value ?: return
        val currentLiked = _radarFeedback.value[radarId] == true
        val newLiked = !currentLiked
        val updatedMap = _radarFeedback.value + (radarId to newLiked)
        _radarFeedback.value = updatedMap

        _techRadar.value = _techRadar.value.map {
            if (it.id == radarId) it.copy(isLiked = newLiked) else it
        }

        viewModelScope.launch {
            agentService.saveRadarFeedback(user.uid, radarId, newLiked)
        }
    }

    fun refreshTechRadar() {
        viewModelScope.launch {
            _isAILoading.value = true
            val user = _firebaseUser.value
            val items = agentService.fetchLatestTechRadar(user?.focusAreas ?: listOf("Python", "AI/ML"))
            if (items.isNotEmpty()) {
                val feedback = _radarFeedback.value
                _techRadar.value = items.map { it.copy(isLiked = feedback[it.id] == true) }
                _statusNotice.value = "Tech Radar personalized for ${user?.currentPath ?: "AIML"}."
            } else {
                _statusNotice.value = "Tech Radar synchronized."
            }
            _isAILoading.value = false
        }
    }

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun clearStatusNotice() {
        _statusNotice.value = null
    }

    fun toggleTaskCompletion(task: TaskItem) {
        viewModelScope.launch {
            val newCompleted = !task.isCompleted
            repository.updateTaskStatus(task, newCompleted)
            _firebaseUser.value?.let { user ->
                agentService.saveTaskForUser(user.uid, task.id.toString(), task.title, task.category, newCompleted)
            }
            refreshNextBestAction()
        }
    }

    fun addNewTask(title: String, category: String, minutes: Int, deliverable: String) {
        viewModelScope.launch {
            val newTask = TaskItem(
                weekNumber = 3,
                dayNumber = 1,
                title = title,
                description = "Custom planned sprint task.",
                deliverable = deliverable.ifBlank { "Complete code and verify correctness" },
                category = category,
                estimatedMinutes = minutes,
                dueDateText = "Today"
            )
            repository.addTask(newTask)
            _firebaseUser.value?.let { user ->
                agentService.saveTaskForUser(user.uid, System.currentTimeMillis().toString(), title, category, false)
            }
            _statusNotice.value = "Task '$title' added to daily planner"
            refreshNextBestAction()
        }
    }

    fun triggerAdaptiveReschedule() {
        viewModelScope.launch {
            _isAILoading.value = true
            _statusNotice.value = "Gemini is adaptively rescheduling overdue tasks..."
            val profile = userProfile.value ?: SeedData.initialProfile
            repository.performAdaptiveReschedule(profile.hoursPerDay)
            _isAILoading.value = false
            _statusNotice.value = "Tasks adaptively rebalanced to prevent burnout!"
            refreshNextBestAction()
        }
    }

    fun updateJobStatus(job: JobItem, status: String) {
        viewModelScope.launch {
            repository.updateJobStatus(job, status)
            _statusNotice.value = "${job.company} moved to $status"
        }
    }

    fun addNewJob(company: String, role: String, location: String, salary: String, jd: String) {
        viewModelScope.launch {
            repository.saveJob(
                JobItem(
                    company = company,
                    role = role,
                    status = "SAVED",
                    location = location.ifBlank { "Remote / Hybrid" },
                    salaryRange = salary.ifBlank { "$100k - $140k" },
                    jdText = jd,
                    notes = "Target application added."
                )
            )
            _statusNotice.value = "Added $company to Job Tracker"
        }
    }

    fun deleteJob(job: JobItem) {
        viewModelScope.launch {
            repository.deleteJob(job)
            _statusNotice.value = "Removed ${job.company}"
        }
    }

    fun openResumeTailorModal(job: JobItem) {
        _activeJobForTailoring.value = job
        _tailoredResumeResult.value = job.tailoredResumeNotes.ifBlank { null }
    }

    fun closeResumeTailorModal() {
        _activeJobForTailoring.value = null
        _tailoredResumeResult.value = null
    }

    fun generateTailoredResume(job: JobItem) {
        viewModelScope.launch {
            _isAILoading.value = true
            _statusNotice.value = "Gemini is tailoring resume for ${job.company}..."
            val profile = userProfile.value ?: SeedData.initialProfile
            val result = repository.tailorResume(profile, job.company, job.role, job.jdText)
            _tailoredResumeResult.value = result
            repository.saveJob(job.copy(tailoredResumeNotes = result))
            _isAILoading.value = false
            _statusNotice.value = "Resume points tailored for ${job.company}!"
        }
    }

    fun submitQuizAnswer(quizId: Long, selectedIndex: Int, isCorrect: Boolean) {
        viewModelScope.launch {
            repository.recordQuizAnswer(quizId, selectedIndex, isCorrect)
        }
    }

    fun togglePracticeItemSolved(item: PracticeItem) {
        viewModelScope.launch {
            repository.updatePracticeItemStatus(item, !item.isSolved)
        }
    }

    fun setCandidateAnswer(answer: String) {
        _interviewCandidateAnswer.value = answer
    }

    fun evaluateInterview(role: String, topic: String) {
        val answer = _interviewCandidateAnswer.value
        if (answer.isBlank()) {
            _statusNotice.value = "Please enter or speak your answer before submitting."
            return
        }
        viewModelScope.launch {
            _isAILoading.value = true
            _statusNotice.value = "Gemini is analyzing interview response..."
            val eval = repository.evaluateMockInterview(
                role = role,
                topic = topic,
                question = _interviewQuestion.value,
                candidateAnswer = answer
            )
            _interviewEvaluation.value = eval
            _isAILoading.value = false
            _statusNotice.value = "Interview evaluated! Score: ${eval.first}/100"

            repository.saveInterviewSession(
                InterviewSession(
                    role = role,
                    topic = topic,
                    mode = "VOICE_OR_TEXT",
                    transcriptJson = "[{\"question\":\"${_interviewQuestion.value}\",\"answer\":\"$answer\"}]",
                    overallScore = eval.first,
                    feedback = eval.second,
                    followUpQuestions = eval.third
                )
            )
        }
    }

    fun loadNewInterviewQuestion(newQuestion: String) {
        _interviewQuestion.value = newQuestion
        _interviewCandidateAnswer.value = ""
        _interviewEvaluation.value = null
    }

    fun completeOnboarding(
        targetRole: String,
        targetTier: String,
        deadlineWeeks: Int,
        hoursPerDay: Int,
        skillsText: String,
        resumeText: String
    ) {
        viewModelScope.launch {
            _isAILoading.value = true
            _statusNotice.value = "Gemini is generating custom skill-gap matrix and roadmap..."

            val updatedProfile = UserProfile(
                id = 1,
                name = _firebaseUser.value?.name ?: "Candidate",
                targetRole = targetRole,
                targetCompanyTier = targetTier,
                deadlineWeeks = deadlineWeeks,
                hoursPerDay = hoursPerDay,
                skillsText = skillsText,
                resumeSummary = resumeText,
                isOnboarded = true
            )
            repository.updateUserProfile(updatedProfile)

            val gaps = repository.runSkillGapAnalysis(targetRole, targetTier, skillsText, resumeText)
            repository.generateFullRoadmap(targetRole, deadlineWeeks, hoursPerDay, gaps)

            _isAILoading.value = false
            _statusNotice.value = "Onboarding completed! Roadmap & skill gaps generated."
            _currentTab.value = AppNavTab.DASHBOARD
            refreshNextBestAction()
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            _isAILoading.value = true
            repository.resetToSeedData()
            _isAILoading.value = false
            _statusNotice.value = "Reset to fresh seed curriculum & challenges."
            _currentTab.value = AppNavTab.DASHBOARD
        }
    }

    fun refreshNextBestAction() {
        viewModelScope.launch {
            val role = userProfile.value?.targetRole ?: "AIML Engineer"
            val gaps = skillGaps.value
            val currentTasks = tasks.value
            val recommendation = repository.getNextBestAction(role, gaps, currentTasks)
            _nextBestAction.value = recommendation
        }
    }

    // --- LEARNING PATHS & TUTOR ACTIONS ---

    fun selectOrSwitchLearningPath(pathId: String) {
        _activePathId.value = pathId
        val user = _firebaseUser.value ?: return
        viewModelScope.launch {
            _isAILoading.value = true
            learningService.selectOrSwitchPath(user.uid, pathId)
            _isAILoading.value = false
            _statusNotice.value = "Active path switched to ${pathId.uppercase()}. Daily sprint tasks updated!"
        }
    }

    fun toggleFirestoreTask(task: FirestoreTask) {
        viewModelScope.launch {
            learningService.toggleTaskStatus(task)
            val currentTasks = _firestoreTasks.value.map {
                if (it.id == task.id) it.copy(status = if (it.status == "done") "todo" else "done") else it
            }
            _dailyLearningStats.value = learningService.computeStats(
                tasks = currentTasks,
                activePathId = _activePathId.value,
                allModules = _allModules.value,
                progressList = _userProgress.value
            )
        }
    }

    fun generateTasksForCurrentModule() {
        val user = _firebaseUser.value ?: return
        val currentMod = currentActiveModule.value ?: return
        viewModelScope.launch {
            _isAILoading.value = true
            learningService.generateDailyTasksForModule(user.uid, currentMod)
            _isAILoading.value = false
            _statusNotice.value = "Generated new daily tasks for ${currentMod.title}."
        }
    }

    fun openTutorModalForModule(module: LearningModule, autoExplain: Boolean = true) {
        _tutorModalModule.value = module
        _moduleQuizSubmission.value = null
        if (autoExplain) {
            askTutorQuestion(module, "Explain the core concepts and intuition of ${module.title} in simple terms with a clear Python code snippet.")
        }
    }

    fun askTutorQuestion(module: LearningModule, query: String) {
        viewModelScope.launch {
            _isTutorExplaining.value = true
            val explanation = learningService.tutorExplainConcept(module, query)
            _tutorExplanation.value = explanation
            _isTutorExplaining.value = false
        }
    }

    fun generateQuizForModule(module: LearningModule) {
        viewModelScope.launch {
            _isGeneratingQuiz.value = true
            _moduleQuizSubmission.value = null
            val questions = learningService.tutorGenerateQuiz(module)
            _moduleQuizQuestions.value = questions
            _isGeneratingQuiz.value = false
        }
    }

    fun submitModuleQuiz(module: LearningModule, answers: Map<Int, Int>) {
        val user = _firebaseUser.value ?: return
        viewModelScope.launch {
            val questions = _moduleQuizQuestions.value
            val result = learningService.evaluateQuizSubmission(
                uid = user.uid,
                pathId = _activePathId.value,
                module = module,
                userAnswers = answers,
                questions = questions
            )
            _moduleQuizSubmission.value = result
            if (result.scorePercentage >= 70) {
                _statusNotice.value = "Module Passed (${result.scorePercentage}%). Next module unlocked!"
            }
        }
    }

    fun dismissTutorModal() {
        _tutorModalModule.value = null
        _tutorExplanation.value = null
        _moduleQuizQuestions.value = emptyList()
        _moduleQuizSubmission.value = null
    }

    fun openTutorInAgentsTab(module: LearningModule, prompt: String = "") {
        dismissTutorModal()
        val tutorAgent = AgentSeedData.initialAgents.firstOrNull { it.id == "tutor" }
        _selectedAgent.value = tutorAgent
        _currentTab.value = AppNavTab.AGENTS
        if (prompt.isNotBlank()) {
            sendAgentMessage(prompt)
        }
    }
}
