package com.quovex.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.quovex.data.local.UserPreferencesManager
import com.quovex.theme.QuovexTheme
import com.quovex.ui.ai.AiChatScreen
import com.quovex.ui.ai.AiChatViewModel
import com.quovex.ui.ai.ImageDoubtScreen
import com.quovex.ui.ai.ImageDoubtViewModel
import com.quovex.ui.auth.AuthScreen
import com.quovex.ui.auth.AuthViewModel
import com.quovex.ui.community.CommunityScreen
import com.quovex.ui.community.CommunityViewModel
import com.quovex.ui.community.StudyRoomLiveScreen
import com.quovex.ui.community.StudyRoomLiveViewModel
import com.quovex.ui.dashboard.DashboardScreen
import com.quovex.ui.dashboard.DashboardViewModel
import com.quovex.ui.decks.DeckOverviewScreen
import com.quovex.ui.decks.DeckOverviewViewModel
import com.quovex.ui.flashcards.FlashcardPlayerScreen
import com.quovex.ui.flashcards.FlashcardPlayerViewModel
import com.quovex.ui.knowledge.KnowledgeHubScreen
import com.quovex.ui.knowledge.KnowledgeHubViewModel
import com.quovex.ui.knowledge.MaterialDetailScreen
import com.quovex.ui.knowledge.MaterialDetailViewModel
import com.quovex.ui.material.AddMaterialScreen
import com.quovex.ui.material.ImportUrlScreen
import com.quovex.ui.material.MaterialUiState
import com.quovex.ui.material.MaterialViewModel
import com.quovex.ui.material.ProcessingScreen
import com.quovex.ui.material.SubjectInferenceScreen
import com.quovex.ui.onboarding.OnboardingViewModel
import com.quovex.ui.onboarding.OnboardingWizardScreen
import com.quovex.ui.profile.ProfileScreen
import com.quovex.ui.profile.ProfileViewModel
import com.quovex.ui.quiz.QuizScreen
import com.quovex.ui.quiz.QuizViewModel
import com.quovex.ui.scanner.DocumentScannerScreen
import com.quovex.ui.scanner.DocumentScannerViewModel
import com.quovex.ui.timer.TimerScreen
import com.quovex.ui.timer.TimerViewModel

import com.quovex.ui.profile.ProfileSetupScreen
import com.quovex.ui.profile.ProfileSetupViewModel
import com.quovex.ui.splash.SplashScreen

data class NavTabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun QuovexNavGraph(
    userPreferencesManager: UserPreferencesManager? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val colors = QuovexTheme.colors

    val startRoute = QuovexRoute.Splash.route

    val navTabs = listOf(
        NavTabItem(QuovexRoute.Dashboard.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavTabItem(QuovexRoute.Timer.route, "Timer", Icons.Filled.Timer, Icons.Outlined.Timer),
        NavTabItem(QuovexRoute.KnowledgeHub.route, "Hub", Icons.Filled.AutoStories, Icons.Outlined.AutoStories),
        NavTabItem(QuovexRoute.Community.route, "Community", Icons.Filled.Groups, Icons.Outlined.Groups),
        NavTabItem(QuovexRoute.Profile.route, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    val bottomBarRoutes = listOf(
        QuovexRoute.Dashboard.route,
        QuovexRoute.Timer.route,
        QuovexRoute.KnowledgeHub.route,
        QuovexRoute.Community.route,
        QuovexRoute.Profile.route
    )
    val showBottomBar = currentDestination?.route in bottomBarRoutes

    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            if (showBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        color = colors.surface.copy(alpha = 0.96f),
                        border = BorderStroke(1.dp, Color(0xFF1F2E28)),
                        shadowElevation = 16.dp
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            contentColor = colors.textPrimary,
                            tonalElevation = 0.dp,
                            modifier = Modifier.height(64.dp)
                        ) {
                            navTabs.forEach { tab ->
                                val isSelected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = {
                                        Text(tab.label, style = QuovexTheme.typography.labelSmall)
                                    },
                                    selected = isSelected,
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = colors.primary,
                                        selectedTextColor = colors.primary,
                                        indicatorColor = colors.surfaceVariant,
                                        unselectedIconColor = colors.textSecondary,
                                        unselectedTextColor = colors.textSecondary
                                    ),
                                    onClick = {
                                        navController.navigate(tab.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- SPLASH ---
            composable(QuovexRoute.Splash.route) {
                SplashScreen(
                    userPreferencesManager = userPreferencesManager,
                    onNavigateToOnboarding = {
                        navController.navigate(QuovexRoute.Onboarding.route) {
                            popUpTo(QuovexRoute.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToAuth = {
                        navController.navigate(QuovexRoute.Auth.route) {
                            popUpTo(QuovexRoute.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToDashboard = {
                        navController.navigate(QuovexRoute.Dashboard.route) {
                            popUpTo(QuovexRoute.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // --- ONBOARDING ---
            composable(QuovexRoute.Onboarding.route) {
                val onboardingViewModel: OnboardingViewModel = hiltViewModel()
                OnboardingWizardScreen(
                    viewModel = onboardingViewModel,
                    onOnboardingComplete = {
                        navController.navigate(QuovexRoute.Auth.route) {
                            popUpTo(QuovexRoute.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // --- AUTH ---
            composable(QuovexRoute.Auth.route) {
                val authViewModel: AuthViewModel = hiltViewModel()
                AuthScreen(
                    viewModel = authViewModel,
                    onAuthSuccess = { isNewUser ->
                        if (isNewUser) {
                            navController.navigate(QuovexRoute.ProfileSetup.route) {
                                popUpTo(QuovexRoute.Auth.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(QuovexRoute.Dashboard.route) {
                                popUpTo(QuovexRoute.Auth.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            // --- PROFILE SETUP (POST-REGISTRATION) ---
            composable(QuovexRoute.ProfileSetup.route) {
                val profileSetupViewModel: ProfileSetupViewModel = hiltViewModel()
                ProfileSetupScreen(
                    viewModel = profileSetupViewModel,
                    onSetupComplete = {
                        navController.navigate(QuovexRoute.Dashboard.route) {
                            popUpTo(QuovexRoute.ProfileSetup.route) { inclusive = true }
                        }
                    }
                )
            }

            // --- DASHBOARD ---
            composable(QuovexRoute.Dashboard.route) {
                val dashboardViewModel: DashboardViewModel = hiltViewModel()
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onStartTimerClick = { navController.navigate(QuovexRoute.Timer.route) },
                    onDeckClick = { deckId ->
                        navController.navigate(QuovexRoute.DeckOverview.createRoute(deckId))
                    },
                    onAiChatClick = { navController.navigate(QuovexRoute.AiChat.createRoute()) },
                    onAiNoteParserClick = { navController.navigate(QuovexRoute.AddMaterial.route) },
                    onLibraryClick = { navController.navigate(QuovexRoute.KnowledgeHub.route) },
                    onStudyPlannerClick = { navController.navigate(QuovexRoute.StudyPlanner.route) },
                    onNavigateToPaywall = { navController.navigate(QuovexRoute.PremiumPaywall.route) },
                    onAnalyticsClick = { navController.navigate(QuovexRoute.Analytics.route) },
                    onDailyDiagnosticQuizClick = { navController.navigate(QuovexRoute.DailyDiagnosticQuiz.route) },
                    onStreakClick = { navController.navigate(QuovexRoute.Streak.route) }
                )
            }

            // --- FOCUS TIMER ---
            composable(QuovexRoute.Timer.route) {
                val timerViewModel: TimerViewModel = hiltViewModel()
                TimerScreen(viewModel = timerViewModel)
            }

            // --- KNOWLEDGE HUB (Replaces old Library) ---
            composable(QuovexRoute.KnowledgeHub.route) {
                val knowledgeHubViewModel: KnowledgeHubViewModel = hiltViewModel()
                KnowledgeHubScreen(
                    viewModel = knowledgeHubViewModel,
                    onNavigateToAddMaterial = { navController.navigate(QuovexRoute.AddMaterial.route) },
                    onNavigateToMaterialDetail = { materialId ->
                        navController.navigate(QuovexRoute.MaterialDetail.createRoute(materialId))
                    },
                    onNavigateToFlashcards = { deckId ->
                        navController.navigate(QuovexRoute.DeckOverview.createRoute(deckId.toInt()))
                    },
                    onNavigateToNcert = {
                        navController.navigate(QuovexRoute.NcertBrowser.route)
                    },
                    onNavigateToOriginals = {
                        navController.navigate(QuovexRoute.OriginalsBrowser.route)
                    }
                )
            }

            // --- ADD MATERIAL ---
            composable(QuovexRoute.AddMaterial.route) {
                val materialViewModel: MaterialViewModel = hiltViewModel()
                val materialUiState by materialViewModel.uiState.collectAsState()

                when (val state = materialUiState) {
                    is MaterialUiState.Processing -> {
                        ProcessingScreen(message = state.progressMessage)
                    }
                    is MaterialUiState.Inferred -> {
                        SubjectInferenceScreen(
                            inference = state.inference,
                            initialTitle = state.initialTitle,
                            onConfirm = { subject, topic, title ->
                                materialViewModel.confirmAndSynthesize(
                                    confirmedSubject = subject,
                                    confirmedTopic = topic,
                                    confirmedTitle = title,
                                    rawText = state.rawText,
                                    inputType = state.inputType,
                                    sourceUrl = state.sourceUrl,
                                    confidence = state.inference.confidence
                                )
                            }
                        )
                    }
                    is MaterialUiState.Success -> {
                        androidx.compose.runtime.LaunchedEffect(state.material.id) {
                            val id = state.material.id
                            materialViewModel.resetState()
                            navController.navigate(QuovexRoute.MaterialDetail.createRoute(id)) {
                                popUpTo(QuovexRoute.KnowledgeHub.route)
                            }
                        }
                    }
                    else -> {
                        AddMaterialScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToScanner = { navController.navigate(QuovexRoute.DocumentScanner.route) },
                            onNavigateToImportUrl = { navController.navigate(QuovexRoute.ImportUrl.route) },
                            onNavigateToImageDoubt = { navController.navigate(QuovexRoute.ImageDoubt.route) },
                            onProcessText = { title, text, type ->
                                materialViewModel.processRawText(title, text, type)
                            }
                        )
                    }
                }
            }

            // --- IMPORT URL ---
            composable(QuovexRoute.ImportUrl.route) {
                val materialViewModel: MaterialViewModel = hiltViewModel()
                val materialUiState by materialViewModel.uiState.collectAsState()

                when (val state = materialUiState) {
                    is MaterialUiState.Processing -> {
                        ProcessingScreen(message = state.progressMessage)
                    }
                    is MaterialUiState.Inferred -> {
                        SubjectInferenceScreen(
                            inference = state.inference,
                            initialTitle = state.initialTitle,
                            onConfirm = { subject, topic, title ->
                                materialViewModel.confirmAndSynthesize(
                                    confirmedSubject = subject,
                                    confirmedTopic = topic,
                                    confirmedTitle = title,
                                    rawText = state.rawText,
                                    inputType = state.inputType,
                                    sourceUrl = state.sourceUrl,
                                    confidence = state.inference.confidence
                                )
                            }
                        )
                    }
                    is MaterialUiState.Success -> {
                        androidx.compose.runtime.LaunchedEffect(state.material.id) {
                            val id = state.material.id
                            materialViewModel.resetState()
                            navController.navigate(QuovexRoute.MaterialDetail.createRoute(id)) {
                                popUpTo(QuovexRoute.KnowledgeHub.route)
                            }
                        }
                    }
                    else -> {
                        ImportUrlScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onImportUrl = { url, type ->
                                materialViewModel.importUrlContent(url, type)
                            }
                        )
                    }
                }
            }

            // --- MATERIAL DETAIL ---
            composable(
                route = QuovexRoute.MaterialDetail.route,
                arguments = listOf(navArgument("materialId") { type = NavType.LongType })
            ) {
                val materialDetailViewModel: MaterialDetailViewModel = hiltViewModel()
                MaterialDetailScreen(
                    viewModel = materialDetailViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToQuiz = { materialId ->
                        navController.navigate(QuovexRoute.Quiz.createRoute(materialId))
                    },
                    onNavigateToFlashcards = { deckId ->
                        navController.navigate(QuovexRoute.DeckOverview.createRoute(deckId.toInt()))
                    }
                )
            }

            // --- QUIZ ---
            composable(
                route = QuovexRoute.Quiz.route,
                arguments = listOf(navArgument("materialId") { type = NavType.LongType })
            ) {
                val quizViewModel: QuizViewModel = hiltViewModel()
                QuizScreen(
                    viewModel = quizViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToResult = { navController.popBackStack() }
                )
            }

            // --- DOCUMENT SCANNER (ML Kit OCR) ---
            composable(QuovexRoute.DocumentScanner.route) {
                val documentScannerViewModel: DocumentScannerViewModel = hiltViewModel()
                DocumentScannerScreen(
                    viewModel = documentScannerViewModel,
                    onBackClick = { navController.popBackStack() },
                    onNoteSaved = { materialId ->
                        navController.navigate(QuovexRoute.MaterialDetail.createRoute(materialId)) {
                            popUpTo(QuovexRoute.KnowledgeHub.route)
                        }
                    }
                )
            }

            // --- IMAGE DOUBT SOLVER (Vision AI) ---
            composable(QuovexRoute.ImageDoubt.route) {
                val imageDoubtViewModel: ImageDoubtViewModel = hiltViewModel()
                ImageDoubtScreen(
                    viewModel = imageDoubtViewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToNote = { materialId ->
                        navController.navigate(QuovexRoute.MaterialDetail.createRoute(materialId)) {
                            popUpTo(QuovexRoute.KnowledgeHub.route)
                        }
                    }
                )
            }

            // --- DECK OVERVIEW ---
            composable(
                route = QuovexRoute.DeckOverview.route,
                arguments = listOf(navArgument("deckId") { type = NavType.IntType })
            ) {
                val deckOverviewViewModel: DeckOverviewViewModel = hiltViewModel()
                DeckOverviewScreen(
                    viewModel = deckOverviewViewModel,
                    onBackClick = { navController.popBackStack() },
                    onStudyNowClick = { deckId ->
                        navController.navigate(QuovexRoute.FlashcardPlayer.createRoute(deckId, reviewAll = false))
                    },
                    onReviewAllClick = { deckId ->
                        navController.navigate(QuovexRoute.FlashcardPlayer.createRoute(deckId, reviewAll = true))
                    }
                )
            }

            // --- FLASHCARD PLAYER ---
            composable(
                route = QuovexRoute.FlashcardPlayer.route,
                arguments = listOf(
                    navArgument("deckId") { type = NavType.IntType },
                    navArgument("reviewAll") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) {
                val flashcardViewModel: FlashcardPlayerViewModel = hiltViewModel()
                FlashcardPlayerScreen(
                    viewModel = flashcardViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // --- COMMUNITY ---
            composable(QuovexRoute.Community.route) {
                val communityViewModel: CommunityViewModel = hiltViewModel()
                CommunityScreen(
                    viewModel = communityViewModel,
                    onNavigateToRoom = { roomId ->
                        navController.navigate(QuovexRoute.StudyRoomLive.createRoute(roomId))
                    }
                )
            }

            // --- STUDY ROOM LIVE ---
            composable(
                route = QuovexRoute.StudyRoomLive.route,
                arguments = listOf(
                    navArgument("roomId") { type = NavType.StringType }
                )
            ) {
                val studyRoomLiveViewModel: StudyRoomLiveViewModel = hiltViewModel()
                StudyRoomLiveScreen(
                    viewModel = studyRoomLiveViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // --- PROFILE ---
            composable(QuovexRoute.Profile.route) {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    viewModel = profileViewModel,
                    onNavigateToPaywall = {
                        navController.navigate(QuovexRoute.PremiumPaywall.route)
                    },
                    onNavigateToBlocker = {
                        navController.navigate(QuovexRoute.DistractionBlocker.route)
                    },
                    onSignedOut = {
                        navController.navigate(QuovexRoute.Auth.route) {
                            popUpTo(QuovexRoute.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }

            // --- AI CHAT ---
            composable(
                route = QuovexRoute.AiChat.route,
                arguments = listOf(
                    navArgument("subject") { type = NavType.StringType; defaultValue = "" },
                    navArgument("topic") { type = NavType.StringType; defaultValue = "" },
                    navArgument("prompt") { type = NavType.StringType; defaultValue = "" }
                )
            ) {
                val aiChatViewModel: AiChatViewModel = hiltViewModel()
                AiChatScreen(
                    viewModel = aiChatViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // --- NCERT OFFICIAL RESOURCE LIBRARY ---
            composable(QuovexRoute.NcertBrowser.route) {
                val ncertBrowserViewModel: com.quovex.ui.ncert.NcertBrowserViewModel = hiltViewModel()
                com.quovex.ui.ncert.NcertBrowserScreen(
                    viewModel = ncertBrowserViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToBookDetail = { bookId ->
                        navController.navigate(QuovexRoute.NcertBookDetail.createRoute(bookId))
                    }
                )
            }

            composable(
                route = QuovexRoute.NcertBookDetail.route,
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) {
                val ncertBookDetailViewModel: com.quovex.ui.ncert.NcertBookDetailViewModel = hiltViewModel()
                com.quovex.ui.ncert.NcertBookDetailScreen(
                    viewModel = ncertBookDetailViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChapterDetail = { chapterId ->
                        navController.navigate(QuovexRoute.NcertChapterDetail.createRoute(chapterId))
                    }
                )
            }

            composable(
                route = QuovexRoute.NcertChapterDetail.route,
                arguments = listOf(navArgument("chapterId") { type = NavType.StringType })
            ) {
                val ncertChapterDetailViewModel: com.quovex.ui.ncert.NcertChapterDetailViewModel = hiltViewModel()
                com.quovex.ui.ncert.NcertChapterDetailScreen(
                    viewModel = ncertChapterDetailViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToMaterialDetail = { materialId ->
                        navController.navigate(QuovexRoute.MaterialDetail.createRoute(materialId)) {
                            popUpTo(QuovexRoute.NcertBrowser.route)
                        }
                    },
                    onReadInQuovex = { chapterId ->
                        navController.navigate(QuovexRoute.NcertPdfReader.createRoute(chapterId))
                    }
                )
            }

            composable(
                route = QuovexRoute.NcertPdfReader.route,
                arguments = listOf(navArgument("chapterId") { type = NavType.StringType })
            ) {
                val pdfReaderViewModel: com.quovex.ui.ncert.NcertPdfReaderViewModel = hiltViewModel()
                val readerState = pdfReaderViewModel.uiState.collectAsState().value
                com.quovex.ui.ncert.NcertPdfReaderScreen(
                    viewModel = pdfReaderViewModel,
                    onBack = { navController.popBackStack() },
                    onAskAi = { query, _ ->
                        navController.navigate(
                            QuovexRoute.AiChat.createRoute(
                                subject = readerState.chapter?.subject ?: "General",
                                topic = readerState.chapter?.chapterTitle ?: "NCERT",
                                prompt = query
                            )
                        )
                    }
                )
            }

            // --- QUOVEX ORIGINALS: BROWSER ---
            composable(QuovexRoute.OriginalsBrowser.route) {
                val originalsViewModel: com.quovex.ui.originals.OriginalsViewModel = hiltViewModel()
                com.quovex.ui.originals.OriginalsBrowserScreen(
                    viewModel = originalsViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToBookDetail = { bookId ->
                        navController.navigate(QuovexRoute.OriginalBookDetail.createRoute(bookId))
                    }
                )
            }

            // --- QUOVEX ORIGINALS: BOOK DETAIL ---
            composable(
                route = QuovexRoute.OriginalBookDetail.route,
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                val originalsViewModel: com.quovex.ui.originals.OriginalsViewModel = hiltViewModel()
                com.quovex.ui.originals.OriginalBookDetailScreen(
                    bookId = bookId,
                    viewModel = originalsViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToChapterReader = { bId, chapterNum ->
                        navController.navigate(QuovexRoute.OriginalChapterReader.createRoute(bId, chapterNum))
                    }
                )
            }

            // --- QUOVEX ORIGINALS: CHAPTER READER ---
            composable(
                route = QuovexRoute.OriginalChapterReader.route,
                arguments = listOf(
                    navArgument("bookId") { type = NavType.StringType },
                    navArgument("chapterNumber") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                val chapterNumber = backStackEntry.arguments?.getInt("chapterNumber") ?: 1
                val originalsViewModel: com.quovex.ui.originals.OriginalsViewModel = hiltViewModel()
                com.quovex.ui.originals.OriginalChapterReaderScreen(
                    bookId = bookId,
                    chapterNumber = chapterNumber,
                    viewModel = originalsViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onOpenAiChat = { subject, topic, prompt ->
                        navController.navigate(QuovexRoute.AiChat.createRoute(subject, topic, prompt))
                    },
                    onStartQuiz = { materialId ->
                        navController.navigate(QuovexRoute.Quiz.createRoute(materialId))
                    },
                    onStudyFlashcards = { deckId ->
                        navController.navigate(QuovexRoute.FlashcardPlayer.createRoute(deckId.toInt(), reviewAll = true))
                    }
                )
            }

            // --- AI STUDY PLANNER & EXAM ROADMAP ---
            composable(QuovexRoute.StudyPlanner.route) {
                val plannerViewModel: com.quovex.ui.planner.StudyPlannerViewModel = hiltViewModel()
                com.quovex.ui.planner.StudyPlannerScreen(
                    viewModel = plannerViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onStartFocusSession = { subject, topic, minutes ->
                        navController.navigate(QuovexRoute.Timer.route)
                    }
                )
            }

            // --- GOOGLE PLAY BILLING & PREMIUM SUBSCRIPTION PAYWALL ---
            composable(QuovexRoute.PremiumPaywall.route) {
                val paywallViewModel: com.quovex.ui.premium.PremiumPaywallViewModel = hiltViewModel()
                com.quovex.ui.premium.PremiumPaywallScreen(
                    viewModel = paywallViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // --- PERFORMANCE ANALYTICS & WEEKLY PDF REPORT CENTER ---
            composable(QuovexRoute.Analytics.route) {
                val analyticsViewModel: com.quovex.ui.analytics.AnalyticsViewModel = hiltViewModel()
                com.quovex.ui.analytics.AnalyticsScreen(
                    viewModel = analyticsViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPaywall = { navController.navigate(QuovexRoute.PremiumPaywall.route) }
                )
            }

            // --- DAILY DIAGNOSTIC QUIZ & REMEDIAL MASTERY ---
            composable(QuovexRoute.DailyDiagnosticQuiz.route) {
                val dailyQuizViewModel: com.quovex.ui.quiz.DailyDiagnosticQuizViewModel = hiltViewModel()
                com.quovex.ui.quiz.DailyDiagnosticQuizScreen(
                    viewModel = dailyQuizViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToFlashcards = { navController.navigate(QuovexRoute.KnowledgeHub.route) }
                )
            }

            // --- STREAK PROTECTION & RESILIENCE CEMETERY ---
            composable(QuovexRoute.Streak.route) {
                val streakViewModel: com.quovex.ui.streak.StreakViewModel = hiltViewModel()
                com.quovex.ui.streak.StreakScreen(
                    viewModel = streakViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // --- DISTRACTION BLOCKER & APP RESTRICTION SHIELD ---
            composable(QuovexRoute.DistractionBlocker.route) {
                val blockerViewModel: com.quovex.ui.blocker.DistractionBlockerViewModel = hiltViewModel()
                com.quovex.ui.blocker.DistractionBlockerScreen(
                    viewModel = blockerViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
