package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.NetworkMonitor
import com.example.core.configureAppCheck
import com.example.data.curriculum.CurriculumCatalog
import com.example.data.local.AppDatabase
import com.example.data.repository.AiTeacherRepository
import com.example.data.repository.FirestoreRepository
import com.example.model.PracticeTestPack
import com.example.model.QuestLesson
import com.example.model.UiState
import com.example.model.UserProgressProfile
import com.example.ui.components.MilestoneCelebrationDialog
import com.example.ui.screens.AskAndTutorsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.Lab3DScreen
import com.example.ui.screens.LearnScreen
import com.example.ui.screens.LessonPlayerScreen
import com.example.ui.screens.PracticeTestScreen
import com.example.ui.screens.ProgressScheduleScreen
import com.example.ui.screens.signOutUser
import com.example.ui.theme.ForestEmerald
import com.example.ui.theme.GuruQuestTheme
import com.example.ui.theme.MintSurface
import com.example.ui.viewmodel.AskDoubtViewModel
import com.example.ui.viewmodel.Lab3DViewModel
import com.example.ui.viewmodel.MainDashboardViewModel
import com.example.ui.viewmodel.ScheduleProgressViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize Firebase App Check synchronously before any Firebase API calls
        configureAppCheck(this, intent)

        setContent {
            GuruQuestTheme {
                AppNavigation()
            }
        }
    }
}

private enum class GuruRoute(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    LEARN("learn", "Learn", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    LAB_3D("lab3d", "3D Lab", Icons.Filled.ViewInAr, Icons.Outlined.ViewInAr),
    PROGRESS("progress", "Progress", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    ASK("ask", "Ask", Icons.Filled.ChatBubbleOutline, Icons.Outlined.ChatBubbleOutline)
}

private fun FirebaseAuth.observeCurrentUser(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        trySend(firebaseAuth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation() {
    val auth = remember { Firebase.auth }
    val userFlow = remember(auth) { auth.observeCurrentUser() }
    val currentUser: FirebaseUser? by userFlow.collectAsStateWithLifecycle(initialValue = auth.currentUser)

    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val scope = rememberCoroutineScope()

    val signedInUser = currentUser
    if (signedInUser == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(signedInUser.uid) {
            GuruQuestMainShell(
                userId = signedInUser.uid,
                displayName = signedInUser.displayName ?: signedInUser.email?.substringBefore("@") ?: "Explorer",
                onSignOut = {
                    signOutUser(
                        credentialManager = credentialManager,
                        scope = scope
                    )
                }
            )
        }
    }
}

@Composable
private fun GuruQuestMainShell(
    userId: String,
    displayName: String,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val database = remember(context) { AppDatabase.getInstance(context) }
    val offlineDao = remember(database) { database.offlineStudyDao() }
    val firestoreRepo = remember(context) { FirestoreRepository(context) }
    val aiRepo = remember { AiTeacherRepository() }
    val networkMonitor = remember(context) { NetworkMonitor(context) }

    val dashboardVm: MainDashboardViewModel = viewModel(
        key = "dash_$userId",
        factory = viewModelFactory {
            initializer {
                MainDashboardViewModel(
                    currentUserId = userId,
                    displayName = displayName,
                    firestoreRepo = firestoreRepo,
                    offlineDao = offlineDao,
                    aiRepo = aiRepo,
                    networkMonitor = networkMonitor
                )
            }
        }
    )

    val lab3DVm: Lab3DViewModel = viewModel(
        key = "lab3d_$userId",
        factory = viewModelFactory {
            initializer { Lab3DViewModel(offlineDao = offlineDao, aiRepo = aiRepo) }
        }
    )

    val scheduleVm: ScheduleProgressViewModel = viewModel(
        key = "sched_$userId",
        factory = viewModelFactory {
            initializer {
                ScheduleProgressViewModel(
                    currentUserId = userId,
                    firestoreRepo = firestoreRepo,
                    offlineDao = offlineDao,
                    aiRepo = aiRepo
                )
            }
        }
    )

    val askVm: AskDoubtViewModel = viewModel(
        key = "ask_$userId",
        factory = viewModelFactory {
            initializer {
                AskDoubtViewModel(
                    currentUserId = userId,
                    studentName = displayName,
                    aiRepo = aiRepo,
                    firestoreRepo = firestoreRepo
                )
            }
        }
    )

    val isOnline by dashboardVm.isOnline.collectAsStateWithLifecycle()
    val profileState by dashboardVm.profileState.collectAsStateWithLifecycle()
    val selectedCategory by dashboardVm.selectedCategory.collectAsStateWithLifecycle()
    val selectedTrackId by dashboardVm.selectedTrackId.collectAsStateWithLifecycle()
    val selectedSubject by dashboardVm.selectedSubject.collectAsStateWithLifecycle()
    val activeLessons by dashboardVm.activeLessons.collectAsStateWithLifecycle()
    val activeTests by dashboardVm.activeTests.collectAsStateWithLifecycle()
    val progressMap by dashboardVm.lessonProgressMap.collectAsStateWithLifecycle()
    val aiLoading by dashboardVm.aiGenerationLoading.collectAsStateWithLifecycle()
    val aiError by dashboardVm.aiGenerationError.collectAsStateWithLifecycle()
    val feedback by dashboardVm.feedback.collectAsStateWithLifecycle()

    val scenes by lab3DVm.availableScenes.collectAsStateWithLifecycle()
    val selectedSceneId by lab3DVm.selectedSceneId.collectAsStateWithLifecycle()
    val selectedNode by lab3DVm.selectedNode.collectAsStateWithLifecycle()
    val labGenState by lab3DVm.generationState.collectAsStateWithLifecycle()

    val scheduleState by scheduleVm.scheduleState.collectAsStateWithLifecycle()
    val testAttemptsState by scheduleVm.testAttemptsState.collectAsStateWithLifecycle()
    val isGeneratingPlan by scheduleVm.isGeneratingPlan.collectAsStateWithLifecycle()
    val planError by scheduleVm.planError.collectAsStateWithLifecycle()

    val owlMessages by askVm.owlMessages.collectAsStateWithLifecycle()
    val isAskingOwl by askVm.isAskingOwl.collectAsStateWithLifecycle()
    val owlError by askVm.owlError.collectAsStateWithLifecycle()
    val doubtSessionsState by askVm.doubtSessionsState.collectAsStateWithLifecycle()
    val activeSessionId by askVm.activeSessionId.collectAsStateWithLifecycle()
    val activeMessagesState by askVm.activeMessagesState.collectAsStateWithLifecycle()
    val tutorLoading by askVm.tutorLoading.collectAsStateWithLifecycle()
    val tutorError by askVm.tutorError.collectAsStateWithLifecycle()

    var activeLessonPlayer by remember { mutableStateOf<QuestLesson?>(null) }
    var activeTestPlayer by remember { mutableStateOf<PracticeTestPack?>(null) }
    var openTestsInLearn by remember { mutableStateOf(false) }

    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val profile = when (val state = profileState) {
        is UiState.Success -> state.data
        else -> UserProgressProfile(
            userId = userId,
            displayName = displayName,
            selectedTrackId = selectedTrackId,
            selectedSubject = selectedSubject
        )
    }

    val activeTrack = CurriculumCatalog.tracks.firstOrNull { it.id == selectedTrackId }
        ?: CurriculumCatalog.tracks.first()

    // Show non-modal action feedback in snackbar when no celebration modal
    LaunchedEffect(feedback) {
        val fb = feedback
        if (fb != null && fb.xpGained == 0 && fb.newlyUnlockedMilestone == null && !fb.message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(fb.message)
            dashboardVm.clearFeedback()
        }
    }

    // Full-screen Playable Lesson Overlay
    val currentLesson = activeLessonPlayer
    if (currentLesson != null) {
        LessonPlayerScreen(
            lesson = currentLesson,
            initialHearts = profile.hearts.toInt().coerceIn(1, 5),
            onExit = { activeLessonPlayer = null },
            onOpen3DConcept = { conceptId ->
                activeLessonPlayer = null
                if (!conceptId.isNullOrBlank()) {
                    lab3DVm.selectSceneById(conceptId)
                }
                dashboardVm.unlockMilestoneById("visualizer_3d")
                navController.navigate(GuruRoute.LAB_3D.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onLessonFinished = { correct, total, hearts ->
                dashboardVm.completeLesson(currentLesson, correct, total, hearts)
                activeLessonPlayer = null
            }
        )
        return
    }

    // Full-screen Practice Test Overlay
    val currentTest = activeTestPlayer
    if (currentTest != null) {
        PracticeTestScreen(
            test = currentTest,
            onExit = { activeTestPlayer = null },
            onTestSubmitted = { score, elapsedSeconds ->
                dashboardVm.recordPracticeTestResult(currentTest, score, elapsedSeconds)
            }
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                GuruRoute.entries.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (item == GuruRoute.LEARN) {
                                openTestsInLearn = false
                            }
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.testTag("nav_${item.route}"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ForestEmerald,
                            selectedTextColor = ForestEmerald,
                            indicatorColor = MintSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        if (profileState is UiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .windowInsetsPadding(WindowInsets.statusBars),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ForestEmerald)
            }
        } else {
            NavHost(
                navController = navController,
                startDestination = GuruRoute.HOME.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                composable(GuruRoute.HOME.route) {
                    HomeScreen(
                        profile = profile,
                        selectedCategory = selectedCategory,
                        selectedTrackId = selectedTrackId,
                        selectedSubject = selectedSubject,
                        lessons = activeLessons,
                        progressMap = progressMap,
                        isOnline = isOnline,
                        onSelectCategory = { dashboardVm.selectCategory(it) },
                        onSelectTrack = { dashboardVm.selectTrack(it) },
                        onSelectSubject = { dashboardVm.selectSubject(it) },
                        onStartLesson = { activeLessonPlayer = it },
                        onOpen3DLab = { conceptId ->
                            if (!conceptId.isNullOrBlank()) {
                                lab3DVm.selectSceneById(conceptId)
                            }
                            dashboardVm.unlockMilestoneById("visualizer_3d")
                            navController.navigate(GuruRoute.LAB_3D.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onOpenPracticeTests = {
                            openTestsInLearn = true
                            navController.navigate(GuruRoute.LEARN.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onSaveOffline = { dashboardVm.saveCurrentTrackOffline() },
                        onSignOut = onSignOut
                    )
                }

                composable(GuruRoute.LEARN.route) {
                    LearnScreen(
                        selectedTrackId = selectedTrackId,
                        selectedSubject = selectedSubject,
                        lessons = activeLessons,
                        tests = activeTests,
                        progressMap = progressMap,
                        isOnline = isOnline,
                        isAiLoading = aiLoading,
                        aiError = aiError,
                        initialModeTests = openTestsInLearn,
                        onStartLesson = { activeLessonPlayer = it },
                        onStartTest = { activeTestPlayer = it },
                        onGenerateAiLesson = { topic ->
                            dashboardVm.generateCustomAiLesson(topic) { created ->
                                activeLessonPlayer = created
                            }
                        },
                        onGenerateAiTest = { topic, diff, count ->
                            dashboardVm.generateCustomPracticeTest(topic, diff, count) { created ->
                                activeTestPlayer = created
                            }
                        },
                        onOpen3DLab = { conceptId ->
                            if (!conceptId.isNullOrBlank()) {
                                lab3DVm.selectSceneById(conceptId)
                            }
                            dashboardVm.unlockMilestoneById("visualizer_3d")
                            navController.navigate(GuruRoute.LAB_3D.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onClearAiError = { dashboardVm.clearAiError() }
                    )
                }

                composable(GuruRoute.LAB_3D.route) {
                    Lab3DScreen(
                        scenes = scenes,
                        selectedSceneId = selectedSceneId,
                        selectedNode = selectedNode,
                        generationState = labGenState,
                        trackTitle = activeTrack.title,
                        subject = selectedSubject,
                        isOnline = isOnline,
                        onSelectScene = {
                            lab3DVm.selectSceneById(it)
                            dashboardVm.unlockMilestoneById("visualizer_3d")
                        },
                        onSelectNode = { lab3DVm.selectNode(it) },
                        onGenerateCustom3D = { topic ->
                            lab3DVm.generateCustom3DConcept(topic, activeTrack.title, selectedSubject) {
                                dashboardVm.unlockMilestoneById("visualizer_3d")
                            }
                        },
                        onClearGenerationError = { lab3DVm.clearGenerationStatus() }
                    )
                }

                composable(GuruRoute.PROGRESS.route) {
                    ProgressScheduleScreen(
                        profile = profile,
                        lessonProgressMap = progressMap,
                        scheduleState = scheduleState,
                        testAttemptsState = testAttemptsState,
                        isGeneratingPlan = isGeneratingPlan,
                        planError = planError,
                        isOnline = isOnline,
                        onGenerateSchedule = { mins, goal ->
                            scheduleVm.generateAiStudySchedule(
                                trackId = activeTrack.id,
                                dailyMinutes = mins,
                                examGoal = goal,
                                isOnline = isOnline
                            ) {
                                dashboardVm.unlockMilestoneById("planner_pro")
                            }
                        },
                        onAddCustomTask = { subj, topic, day, mins ->
                            scheduleVm.addCustomScheduleTask(
                                trackId = activeTrack.id,
                                subject = subj,
                                topicTitle = topic,
                                dayLabel = day,
                                durationMinutes = mins,
                                activityType = "LESSON",
                                isOnline = isOnline
                            ) {
                                dashboardVm.unlockMilestoneById("planner_pro")
                            }
                        },
                        onToggleScheduleTask = { task ->
                            scheduleVm.toggleTaskCompleted(task, isOnline)
                            dashboardVm.unlockMilestoneById("planner_pro")
                        },
                        onClearError = { scheduleVm.clearError() }
                    )
                }

                composable(GuruRoute.ASK.route) {
                    AskAndTutorsScreen(
                        owlMessages = owlMessages,
                        isAskingOwl = isAskingOwl,
                        owlError = owlError,
                        doubtSessionsState = doubtSessionsState,
                        activeSessionId = activeSessionId,
                        activeMessagesState = activeMessagesState,
                        tutorLoading = tutorLoading,
                        tutorError = tutorError,
                        trackTitle = activeTrack.title,
                        subject = selectedSubject,
                        isOnline = isOnline,
                        onAskOwl = { q ->
                            askVm.askOwl(q, activeTrack.title, selectedSubject, isOnline) {
                                dashboardVm.unlockMilestoneById("curious_mind")
                            }
                        },
                        onOpen3DWithTopic = { topic ->
                            dashboardVm.unlockMilestoneById("visualizer_3d")
                            navController.navigate(GuruRoute.LAB_3D.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            if (isOnline) {
                                lab3DVm.generateCustom3DConcept(topic, activeTrack.title, selectedSubject) {}
                            }
                        },
                        onStartTutorSession = { tutor, subj, topic, q ->
                            askVm.startTutorDoubtSession(tutor, subj, topic, q)
                        },
                        onOpenSession = { askVm.openSession(it) },
                        onCloseSession = { askVm.closeSessionView() },
                        onSendStudentMessage = { session, txt ->
                            askVm.sendStudentDoubtMessage(session, txt)
                        },
                        onRequestMentorAssist = { session, q ->
                            askVm.requestMentorAssist(
                                sessionId = session.id,
                                tutorName = session.tutorName,
                                specialty = session.tutorSpecialty,
                                subject = session.subject,
                                topic = session.topic,
                                question = q
                            )
                        },
                        onQueueLiveTutor = { askVm.queueForLiveHumanTutor(it) },
                        onResolveSession = { askVm.resolveSession(it) },
                        onClearErrors = { askVm.clearErrors() }
                    )
                }
            }
        }
    }

    // Celebration Modal when XP / Gems / Milestone earned
    val fb = feedback
    if (fb != null && (fb.xpGained > 0 || fb.newlyUnlockedMilestone != null)) {
        MilestoneCelebrationDialog(
            title = if (fb.newlyUnlockedMilestone != null) "Milestone Unlocked!" else "Quest Complete!",
            subtitle = fb.message ?: "Great progress! Keep your learning streak alive.",
            xpGained = fb.xpGained,
            gemsGained = fb.gemsGained,
            milestone = fb.newlyUnlockedMilestone,
            onDismiss = { dashboardVm.clearFeedback() }
        )
    }
}
