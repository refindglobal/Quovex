package com.quovex.ui.navigation

import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
    val startRoute = if (!isLoggedIn) QuovexRoute.Auth.route else QuovexRoute.Dashboard.route

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
                NavigationBar(
                    containerColor = colors.surface,
                    contentColor = colors.textPrimary,
                    tonalElevation = QuovexTheme.elevation.high
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- AUTH ---
            composable(QuovexRoute.Auth.route) {
                val authViewModel: AuthViewModel = hiltViewModel()
                AuthScreen(
                    viewModel = authViewModel,
                    onAuthSuccess = { isNewUser ->
                        if (isNewUser) {
                            navController.navigate(QuovexRoute.Onboarding.route) {
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

            // --- ONBOARDING ---
            composable(QuovexRoute.Onboarding.route) {
                val onboardingViewModel: OnboardingViewModel = hiltViewModel()
                OnboardingWizardScreen(
                    viewModel = onboardingViewModel,
                    onOnboardingComplete = {
                        navController.navigate(QuovexRoute.Dashboard.route) {
                            popUpTo(QuovexRoute.Onboarding.route) { inclusive = true }
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
                    onLibraryClick = { navController.navigate(QuovexRoute.KnowledgeHub.route) }
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
                            initialTitle = state.inference.topic,
                            onConfirm = { subject, topic, title ->
                                materialViewModel.confirmAndTransform(
                                    materialId = state.materialId,
                                    confirmedSubject = subject,
                                    confirmedTopic = topic,
                                    confirmedTitle = title,
                                    rawText = state.rawText
                                )
                            }
                        )
                    }
                    is MaterialUiState.Success -> {
                        androidx.compose.runtime.LaunchedEffect(state.materialId) {
                            val id = state.materialId
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
                ImportUrlScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onImportUrl = { url, type ->
                        materialViewModel.processRawText(title = url, rawText = "Extracting URL: $url", inputType = type)
                        navController.navigate(QuovexRoute.AddMaterial.route) {
                            popUpTo(QuovexRoute.AddMaterial.route) { inclusive = true }
                        }
                    }
                )
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
                CommunityScreen(viewModel = communityViewModel)
            }

            // --- PROFILE ---
            composable(QuovexRoute.Profile.route) {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                ProfileScreen(
                    viewModel = profileViewModel,
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
        }
    }
}
