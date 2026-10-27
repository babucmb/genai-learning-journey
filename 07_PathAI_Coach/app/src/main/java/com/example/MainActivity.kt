package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CleanBottomNavBar
import com.example.ui.components.TopNavBar
import com.example.ui.components.TutorModuleDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppNavTab
import com.example.viewmodel.PathAIViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PathAIApp()
            }
        }
    }
}

@Composable
fun PathAIApp(
    viewModel: PathAIViewModel = viewModel()
) {
    val firebaseUser by viewModel.firebaseUser.collectAsStateWithLifecycle()
    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val authErrorMessage by viewModel.authErrorMessage.collectAsStateWithLifecycle()

    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val skillGaps by viewModel.skillGaps.collectAsStateWithLifecycle()
    val roadmapWeeks by viewModel.roadmapWeeks.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val allJobs by viewModel.allJobs.collectAsStateWithLifecycle()
    val practiceItems by viewModel.practiceItems.collectAsStateWithLifecycle()
    val quizzes by viewModel.quizzes.collectAsStateWithLifecycle()
    val interviews by viewModel.interviews.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()

    // Learning Paths & Daily Tasks State
    val learningPaths by viewModel.learningPaths.collectAsStateWithLifecycle()
    val activePathId by viewModel.activePathId.collectAsStateWithLifecycle()
    val activePath by viewModel.activePath.collectAsStateWithLifecycle()
    val allModules by viewModel.allModules.collectAsStateWithLifecycle()
    val userProgress by viewModel.userProgress.collectAsStateWithLifecycle()
    val currentActiveModule by viewModel.currentActiveModule.collectAsStateWithLifecycle()
    val firestoreTasks by viewModel.firestoreTasks.collectAsStateWithLifecycle()
    val dailyLearningStats by viewModel.dailyLearningStats.collectAsStateWithLifecycle()
    val pathStatsList by viewModel.pathStatsList.collectAsStateWithLifecycle()

    // Tutor Agent Modal State
    val tutorModalModule by viewModel.tutorModalModule.collectAsStateWithLifecycle()
    val tutorExplanation by viewModel.tutorExplanation.collectAsStateWithLifecycle()
    val isTutorExplaining by viewModel.isTutorExplaining.collectAsStateWithLifecycle()
    val moduleQuizQuestions by viewModel.moduleQuizQuestions.collectAsStateWithLifecycle()
    val moduleQuizSubmission by viewModel.moduleQuizSubmission.collectAsStateWithLifecycle()
    val isGeneratingQuiz by viewModel.isGeneratingQuiz.collectAsStateWithLifecycle()

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isAILoading by viewModel.isAILoading.collectAsStateWithLifecycle()
    val statusNotice by viewModel.statusNotice.collectAsStateWithLifecycle()
    val nextBestAction by viewModel.nextBestAction.collectAsStateWithLifecycle()

    val activeJobForTailoring by viewModel.activeJobForTailoring.collectAsStateWithLifecycle()
    val tailoredResumeResult by viewModel.tailoredResumeResult.collectAsStateWithLifecycle()

    val interviewQuestion by viewModel.interviewQuestion.collectAsStateWithLifecycle()
    val candidateAnswer by viewModel.interviewCandidateAnswer.collectAsStateWithLifecycle()
    val interviewEvaluation by viewModel.interviewEvaluation.collectAsStateWithLifecycle()

    val agents by viewModel.agents.collectAsStateWithLifecycle()
    val selectedAgent by viewModel.selectedAgent.collectAsStateWithLifecycle()
    val activeAgentChat by viewModel.activeAgentChat.collectAsStateWithLifecycle()
    val techRadar by viewModel.techRadar.collectAsStateWithLifecycle()
    val isAgentThinking by viewModel.isAgentThinking.collectAsStateWithLifecycle()

    val matchedJobs by viewModel.matchedJobs.collectAsStateWithLifecycle()
    val isSearchingJobs by viewModel.isSearchingJobs.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()

    var showOnboardingModal by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusNotice) {
        statusNotice?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusNotice()
        }
    }

    // If the user is not authenticated in Firebase, display the clean minimalist AuthScreen
    if (firebaseUser == null) {
        AuthScreen(
            isLoading = isAuthLoading,
            errorMessage = authErrorMessage,
            onSignInWithEmail = { email, password ->
                viewModel.signInWithEmail(email, password)
            },
            onSignUpWithEmail = { email, password, name ->
                viewModel.signUpWithEmail(email, password, name)
            },
            onSignInWithGoogle = { context ->
                viewModel.signInWithGoogle(context)
            },
            onQuickDemoSignIn = {
                viewModel.quickDemoSignIn()
            },
            onClearError = {
                viewModel.clearAuthError()
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopNavBar(
                selectedTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) },
                userName = firebaseUser?.name,
                userPath = firebaseUser?.currentPath,
                unreadNotificationCount = unreadNotificationsCount,
                onNotificationClick = { viewModel.selectTab(AppNavTab.NOTIFICATIONS) },
                onProfileClick = { viewModel.selectTab(AppNavTab.SETTINGS) },
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            CleanBottomNavBar(
                selectedTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showOnboardingModal) {
                OnboardingScreen(
                    currentProfile = userProfile,
                    isAILoading = isAILoading,
                    onComplete = { role, tier, deadline, hours, skills, resume ->
                        viewModel.completeOnboarding(role, tier, deadline, hours, skills, resume)
                        showOnboardingModal = false
                    }
                )
            } else {
                when (currentTab) {
                    AppNavTab.DASHBOARD -> {
                        DashboardScreen(
                            userProfile = userProfile,
                            tasks = tasks,
                            skillGaps = skillGaps,
                            roadmapWeeks = roadmapWeeks,
                            jobs = allJobs,
                            practiceItems = practiceItems,
                            nextBestAction = nextBestAction,
                            onNavigateTab = { viewModel.selectTab(it) },
                            onToggleTask = { viewModel.toggleTaskCompletion(it) }
                        )
                    }
                    AppNavTab.AGENTS -> {
                        AgentsPanelScreen(
                            agents = agents,
                            selectedAgent = selectedAgent,
                            activeAgentChat = activeAgentChat,
                            techRadarItems = techRadar,
                            isAgentThinking = isAgentThinking,
                            onSelectAgent = { viewModel.selectAgent(it) },
                            onCloseChat = { viewModel.selectAgent(null) },
                            onSendMessage = { viewModel.sendAgentMessage(it) },
                            onRefreshTechRadar = { viewModel.refreshTechRadar() },
                            onToggleRadarLike = { viewModel.toggleRadarLike(it) }
                        )
                    }
                    AppNavTab.ROADMAP -> {
                        RoadmapScreen(
                            userProfile = userProfile,
                            roadmapWeeks = roadmapWeeks,
                            isAILoading = isAILoading,
                            onRegenerateRoadmap = {
                                val role = userProfile?.targetRole ?: "AIML Engineer"
                                val weeks = userProfile?.deadlineWeeks ?: 12
                                val hours = userProfile?.hoursPerDay ?: 4
                                viewModel.completeOnboarding(
                                    role,
                                    userProfile?.targetCompanyTier ?: "Tier 1 Tech",
                                    weeks,
                                    hours,
                                    userProfile?.skillsText ?: "Python, PyTorch",
                                    userProfile?.resumeSummary ?: "ML projects"
                                )
                            }
                        )
                    }
                    AppNavTab.TODAY -> {
                        TodayScreen(
                            userProfile = userProfile,
                            activePath = activePath,
                            currentModule = currentActiveModule,
                            firestoreTasks = firestoreTasks,
                            dailyStats = dailyLearningStats,
                            isAILoading = isAILoading,
                            onToggleTask = { viewModel.toggleFirestoreTask(it) },
                            onGenerateMoreTasks = { viewModel.generateTasksForCurrentModule() },
                            onAskTutor = { viewModel.openTutorModalForModule(it) },
                            onTakeQuiz = { viewModel.openTutorModalForModule(it, autoExplain = false) },
                            onNavigateToLearning = { viewModel.selectTab(AppNavTab.LEARNING) }
                        )
                    }
                    AppNavTab.LEARNING -> {
                        LearningPathsScreen(
                            paths = learningPaths,
                            modules = allModules,
                            activePathId = activePathId,
                            userProgress = userProgress,
                            dailyStats = dailyLearningStats,
                            pathStatsList = pathStatsList,
                            isLoading = isAILoading,
                            onSelectPath = { viewModel.selectOrSwitchLearningPath(it) },
                            onAskTutor = { viewModel.openTutorModalForModule(it) },
                            onTakeQuiz = { viewModel.openTutorModalForModule(it, autoExplain = false) },
                            onNavigateToToday = { viewModel.selectTab(AppNavTab.TODAY) }
                        )
                    }
                    AppNavTab.PRACTICE -> {
                        PracticeScreen(
                            practiceItems = practiceItems,
                            quizzes = quizzes,
                            projects = projects,
                            onTogglePracticeSolved = { viewModel.togglePracticeItemSolved(it) },
                            onSubmitQuizAnswer = { quizId, idx, isCorr ->
                                viewModel.submitQuizAnswer(quizId, idx, isCorr)
                            }
                        )
                    }
                    AppNavTab.INTERVIEWS -> {
                        InterviewScreen(
                            userProfile = userProfile,
                            interviewQuestion = interviewQuestion,
                            candidateAnswer = candidateAnswer,
                            evaluationResult = interviewEvaluation,
                            interviewsHistory = interviews,
                            isAILoading = isAILoading,
                            onAnswerChanged = { viewModel.setCandidateAnswer(it) },
                            onSubmitEvaluation = { role, topic -> viewModel.evaluateInterview(role, topic) },
                            onSelectNewQuestion = { viewModel.loadNewInterviewQuestion(it) }
                        )
                    }
                    AppNavTab.JOBS -> {
                        JobsScreen(
                            matchedJobs = matchedJobs,
                            isSearchingJobs = isSearchingJobs,
                            onNaturalLanguageSearch = { viewModel.runJobSearch(it) },
                            onFilterChange = { role, loc, jt, days -> viewModel.setJobFilters(role, loc, jt, days) },
                            onUpdateMatchStatus = { matchId, newStatus -> viewModel.updateJobMatchStatus(matchId, newStatus) },
                            jobs = allJobs,
                            activeJobForTailoring = activeJobForTailoring,
                            tailoredResumeResult = tailoredResumeResult,
                            isAILoading = isAILoading,
                            onMoveJobStatus = { job, status -> viewModel.updateJobStatus(job, status) },
                            onAddNewJob = { comp, role, loc, sal, jd ->
                                viewModel.addNewJob(comp, role, loc, sal, jd)
                            },
                            onDeleteJob = { viewModel.deleteJob(it) },
                            onOpenTailorModal = { viewModel.openResumeTailorModal(it) },
                            onGenerateTailoredResume = { viewModel.generateTailoredResume(it) },
                            onCloseTailorModal = { viewModel.closeResumeTailorModal() }
                        )
                    }
                    AppNavTab.SKILL_MAP -> {
                        SkillMapScreen(
                            userProfile = userProfile,
                            skillGaps = skillGaps,
                            isAILoading = isAILoading,
                            onRerunAnalysis = {
                                val role = userProfile?.targetRole ?: "AIML Engineer"
                                viewModel.completeOnboarding(
                                    role,
                                    userProfile?.targetCompanyTier ?: "Tier 1 Tech",
                                    userProfile?.deadlineWeeks ?: 12,
                                    userProfile?.hoursPerDay ?: 4,
                                    userProfile?.skillsText ?: "Python, PyTorch",
                                    userProfile?.resumeSummary ?: "ML projects"
                                )
                            }
                        )
                    }
                    AppNavTab.SETTINGS -> {
                        SettingsScreen(
                            userProfile = userProfile,
                            firebaseUser = firebaseUser,
                            onResetData = { viewModel.resetAllData() },
                            onLaunchOnboarding = { showOnboardingModal = true },
                            onSignOut = { viewModel.signOut() },
                            onUpdateFocusAreas = { areas, path ->
                                viewModel.updateUserFocusAreas(areas, path)
                            },
                            onSaveFullProfile = { updatedProfile ->
                                viewModel.saveFullUserProfile(updatedProfile)
                            },
                            onTriggerTestAlert = {
                                viewModel.triggerDaily9AMJobAlert()
                            }
                        )
                    }
                    AppNavTab.NOTIFICATIONS -> {
                        NotificationsScreen(
                            notifications = notifications,
                            isAILoading = isAILoading,
                            onNotificationClick = { notif ->
                                viewModel.markNotificationRead(notif.id)
                            },
                            onTriggerSimulatedDailyAlert = {
                                viewModel.triggerDaily9AMJobAlert()
                            },
                            onNavigateToJobs = {
                                viewModel.selectTab(AppNavTab.JOBS)
                            }
                        )
                    }
                }
            }

            // Global Tutor Agent Interactive Modal for Active Module
            tutorModalModule?.let { mod ->
                TutorModuleDialog(
                    module = mod,
                    explanation = tutorExplanation,
                    isExplaining = isTutorExplaining,
                    quizQuestions = moduleQuizQuestions,
                    quizSubmission = moduleQuizSubmission,
                    isGeneratingQuiz = isGeneratingQuiz,
                    onAskQuestion = { q -> viewModel.askTutorQuestion(mod, q) },
                    onRequestQuiz = { viewModel.generateQuizForModule(mod) },
                    onSubmitQuiz = { answers -> viewModel.submitModuleQuiz(mod, answers) },
                    onDismiss = { viewModel.dismissTutorModal() },
                    onOpenFullChat = { prompt -> viewModel.openTutorInAgentsTab(mod, prompt) }
                )
            }
        }
    }
}
