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
import com.quovex.ui.ai.AiNoteSummarizerScreen
import com.quovex.ui.ai.AiNoteSummarizerViewModel
import com.quovex.ui.ai.ImageDoubtScreen
import com.quovex.ui.ai.ImageDoubtViewModel
import com.quovex.ui.auth.AuthScreen
import com.quovex.ui.auth.AuthViewModel
import com.quovex.ui.community.CommunityScreen
import com.quovex.ui.community.CommunityViewModel
import com.quovex.ui.dashboard.DashboardScreen
import com.quovex.ui.dashboard.DashboardViewModel
import com.quovex.ui.decks.DeckListViewModel
import com.quovex.ui.decks.DeckOverviewScreen
import com.quovex.ui.decks.DeckOverviewViewModel
import com.quovex.ui.decks.LibraryScreen
import com.quovex.ui.flashcards.FlashcardPlayerScreen
import com.quovex.ui.flashcards.FlashcardPlayerViewModel
import com.quovex.ui.notes.NoteDetailScreen
import com.quovex.ui.notes.NoteDetailViewModel
import com.quovex.ui.notes.NotesViewModel
import com.quovex.ui.onboarding.OnboardingViewModel
import com.quovex.ui.onboarding.OnboardingWizardScreen
import com.quovex.ui.profile.ProfileScreen
import com.quovex.ui.profile.ProfileViewModel
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
        NavTabItem(QuovexRoute.Library.route, "Library", Icons.Filled.AutoStories, Icons.Outlined.AutoStories),
        NavTabItem(QuovexRoute.Community.route, "Community", Icons.Filled.Groups, Icons.Outlined.Groups),
        NavTabItem(QuovexRoute.Profile.route, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    val bottomBarRoutes = navTabs.map { it.route }
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
                        // Dashboard deck clicks go to Deck Overview first
                        navController.navigate(QuovexRoute.DeckOverview.createRoute(deckId))
                    },
                    onAiChatClick = { navController.navigate(QuovexRoute.AiChat.route) },
                    onAiNoteParserClick = { navController.navigate(QuovexRoute.AiSummarizer.route) },
                    onLibraryClick = { navController.navigate(QuovexRoute.Library.route) }
                )
            }

            // --- FOCUS TIMER ---
            composable(QuovexRoute.Timer.route) {
                val timerViewModel: TimerViewModel = hiltViewModel()
                TimerScreen(viewModel = timerViewModel)
            }

            // --- LIBRARY (Flashcards / Notes / Plans tabs) ---
            composable(QuovexRoute.Library.route) {
                val deckListViewModel: DeckListViewModel = hiltViewModel()
                val notesViewModel: NotesViewModel = hiltViewModel()
                LibraryScreen(
                    deckViewModel = deckListViewModel,
                    notesViewModel = notesViewModel,
                    onDeckClick = { deckId ->
                        navController.navigate(QuovexRoute.DeckOverview.createRoute(deckId))
                    },
                    onNoteClick = { noteId ->
                        navController.navigate(QuovexRoute.NoteDetail.createRoute(noteId))
                    },
                    onScanDocumentClick = {
                        navController.navigate(QuovexRoute.DocumentScanner.route)
                    },
                    onImageDoubtClick = {
                        navController.navigate(QuovexRoute.ImageDoubt.route)
                    }
                )
            }

            // --- NOTE DETAIL & EDIT ---
            composable(
                route = QuovexRoute.NoteDetail.route,
                arguments = listOf(navArgument("noteId") { type = NavType.LongType })
            ) {
                val noteDetailViewModel: NoteDetailViewModel = hiltViewModel()
                NoteDetailScreen(
                    viewModel = noteDetailViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // --- DOCUMENT SCANNER (ML Kit OCR) ---
            composable(QuovexRoute.DocumentScanner.route) {
                val documentScannerViewModel: DocumentScannerViewModel = hiltViewModel()
                DocumentScannerScreen(
                    viewModel = documentScannerViewModel,
                    onBackClick = { navController.popBackStack() },
                    onNoteSaved = { noteId ->
                        navController.navigate(QuovexRoute.NoteDetail.createRoute(noteId)) {
                            popUpTo(QuovexRoute.Library.route)
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
                    onNavigateToNote = { noteId ->
                        navController.navigate(QuovexRoute.NoteDetail.createRoute(noteId)) {
                            popUpTo(QuovexRoute.Library.route)
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
            composable(QuovexRoute.AiChat.route) {
                val aiChatViewModel: AiChatViewModel = hiltViewModel()
                AiChatScreen(
                    viewModel = aiChatViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // --- AI NOTE PARSER ---
            composable(QuovexRoute.AiSummarizer.route) {
                val aiSummarizerViewModel: AiNoteSummarizerViewModel = hiltViewModel()
                AiNoteSummarizerScreen(
                    viewModel = aiSummarizerViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // --- FLASHCARD PLAYER ---
            // reviewAll=false (default): due-only mode
            // reviewAll=true: all cards in deck
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
        }
    }
}
