package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.FitnessRepository
import com.example.myapplication.data.PreferencesManager
import com.example.myapplication.viewmodel.*
import com.example.myapplication.ui.screens.*
import com.example.myapplication.ui.components.ExorkSystemDialog
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.ExorkTheme
import com.example.myapplication.ui.theme.MonarchSlate

data class SystemNotification(
    val title: String = "NOTIFICATION",
    val content: String,
    val primaryText: String = "ACCEPT",
    val secondaryText: String? = "DECLINE",
    val iconText: String? = null,
    val imageRes: Int? = null,
    val isBadgeLayout: Boolean = false,
    val onPrimary: () -> Unit,
    val onSecondary: (() -> Unit)? = null
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Link hardware volume buttons to the media stream
        volumeControlStream = android.media.AudioManager.STREAM_MUSIC
        
        val database = AppDatabase.getDatabase(this)
        val repository = FitnessRepository(
            database, database.userDao(), database.abilityDao(), database.workoutDao(),
            database.titleDao(), database.trainingPlanDao(), database.journeyEventDao(),
            database.dailyQuestDao(), database.noteDao()
        )
        
        val preferencesManager = PreferencesManager(this)

        @Suppress("UNCHECKED_CAST")
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository, preferencesManager, this@MainActivity.filesDir) as T
                    modelClass.isAssignableFrom(WorkoutViewModel::class.java) -> WorkoutViewModel(repository) as T
                    modelClass.isAssignableFrom(AbilityViewModel::class.java) -> AbilityViewModel(repository) as T
                    modelClass.isAssignableFrom(BadgeViewModel::class.java) -> BadgeViewModel(repository) as T
                    modelClass.isAssignableFrom(TitleViewModel::class.java) -> TitleViewModel(repository) as T
                    modelClass.isAssignableFrom(StatisticsViewModel::class.java) -> StatisticsViewModel(repository) as T
                    modelClass.isAssignableFrom(WorkoutHistoryViewModel::class.java) -> WorkoutHistoryViewModel(repository) as T
                    modelClass.isAssignableFrom(AchievementViewModel::class.java) -> AchievementViewModel(repository) as T
                    modelClass.isAssignableFrom(ArchiveHubViewModel::class.java) -> ArchiveHubViewModel(repository) as T
                    modelClass.isAssignableFrom(TrainingPlanViewModel::class.java) -> TrainingPlanViewModel(repository) as T
                    modelClass.isAssignableFrom(JourneyViewModel::class.java) -> JourneyViewModel(repository) as T
                    modelClass.isAssignableFrom(NoteViewModel::class.java) -> NoteViewModel(repository) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }

        enableEdgeToEdge()
        // Removed local preferencesManager creation here as it's now in onCreate scope

        setContent {
            ExorkTheme {
                val navController = rememberNavController()
                val homeViewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]
                val userState = homeViewModel.user.collectAsState()

                // System Notification Queue
                val notificationQueue = remember { mutableStateListOf<SystemNotification>() }
                val activeNotification = notificationQueue.firstOrNull()

                // Onboarding / First Launch Logic
                val isFirstLaunch = remember { preferencesManager.isFirstLaunch() }
                LaunchedEffect(Unit) {
                    if (isFirstLaunch) {
                        kotlinx.coroutines.delay(3000L)
                        notificationQueue.add(SystemNotification(
                            content = "You have acquired the qualifications to be a Player.\nWill you accept?",
                            onPrimary = {
                                preferencesManager.setFirstLaunch(false)
                                notificationQueue.removeAt(0)
                            },
                            onSecondary = {
                                android.widget.Toast.makeText(
                                    this@MainActivity,
                                    "The System does not accept refusal.",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        ))
                    }
                }

                // Global Event Collection
                LaunchedEffect(Unit) {
                    homeViewModel.uiEvent.collect { event ->
                        when (event) {
                            is UiEvent.RankPromotion -> {
                                notificationQueue.add(SystemNotification(
                                    title = "RANK ADVANCEMENT",
                                    content = "You have acquired the qualifications for Rank Advancement.\nProceed to the Trial?",
                                    primaryText = "PROCEED",
                                    secondaryText = "LATER",
                                    onPrimary = { notificationQueue.removeAt(0) },
                                    onSecondary = { notificationQueue.removeAt(0) }
                                ))
                            }
                            is UiEvent.AchievementUnlocked -> {
                                notificationQueue.add(SystemNotification(
                                    title = "SECRET QUEST",
                                    content = "Secret Quest: [${event.achievement.name}] has arrived.\nAccept Rewards?",
                                    primaryText = "CLAIM REWARDS",
                                    secondaryText = null,
                                    iconText = event.achievement.icon,
                                    onPrimary = { notificationQueue.removeAt(0) }
                                ))
                            }
                            is UiEvent.BadgeUnlocked -> {
                                notificationQueue.add(SystemNotification(
                                    title = "ARCHIVE UNLOCKED",
                                    content = "New Archive: [${event.badge.name}] has been established.\n${event.badge.description}",
                                    primaryText = "CLAIM REWARDS",
                                    secondaryText = null,
                                    imageRes = event.badge.imageRes,
                                    isBadgeLayout = true,
                                    onPrimary = { notificationQueue.removeAt(0) }
                                ))
                            }
                            is UiEvent.TitleUnlocked -> {
                                notificationQueue.add(SystemNotification(
                                    title = "TITLE EARNED",
                                    content = "Title: [${event.title.name}] has been granted.\nAccept this legacy?",
                                    primaryText = "ACCEPT LEGACY",
                                    secondaryText = null,
                                    iconText = "👑",
                                    onPrimary = { notificationQueue.removeAt(0) }
                                ))
                            }
                            is UiEvent.LevelUp -> {
                                notificationQueue.add(SystemNotification(
                                    title = "LEVEL UP",
                                    content = "Hunter Level Increased: ${event.oldLevel} -> ${event.newLevel}.\nYour capacity has expanded.",
                                    primaryText = "CONFIRM",
                                    secondaryText = null,
                                    onPrimary = { notificationQueue.removeAt(0) }
                                ))
                            }
                            else -> {}
                        }
                    }
                }

                // Navigation Guarding: Prevents duplicate requests and popping start destination
                fun safeNav(action: () -> Unit) {
                    // Only allow navigation if the current destination is fully resumed (transition finished)
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        action()
                    }
                }

                fun safePop() = safeNav {
                    // Never pop the last remaining destination (Home) to avoid black screen
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                }

                fun safeNavigate(route: String) = safeNav {
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }

                // Sync SoundManager
                LaunchedEffect(userState.value.soundEnabled) {
                    com.example.myapplication.util.SoundManager.getInstance(this@MainActivity)
                        .setEnabled(userState.value.soundEnabled)
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MonarchSlate
                ) {
                    activeNotification?.let { notification ->
                        ExorkSystemDialog(
                            title = notification.title,
                            content = notification.content,
                            primaryButtonText = notification.primaryText,
                            secondaryButtonText = notification.secondaryText,
                            iconText = notification.iconText,
                            imageRes = notification.imageRes,
                            isBadgeLayout = notification.isBadgeLayout,
                            onPrimaryClick = notification.onPrimary,
                            onSecondaryClick = notification.onSecondary
                        )
                    }

                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") {
                            ExorkSplashScreen(
                                onAnimationComplete = {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("home") {
                            HomeScreen(
                                viewModel = homeViewModel,
                                onOpenArchives = { safeNavigate("archive_hub") },
                                onOpenHunterArchive = { safeNavigate("archive") },
                                onOpenAchievements = { safeNavigate("achievements") },
                                onOpenTitles = { safeNavigate("titles") },
                                onOpenProfile = { safeNavigate("profile_hub") },
                                onOpenSettings = { safeNavigate("settings") },
                                onOpenTodayTraining = { safeNavigate("training_plan") },
                                onOpenCustomTraining = { safeNavigate("workout") },
                                onOpenHunterNotes = { safeNavigate("hunter_notes") },
                                onOpenHunterJourney = { safeNavigate("history") },
                                onOpenStatistics = { safeNavigate("statistics") }
                            )
                        }
                        composable("hunter_notes") {
                            val noteViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[NoteViewModel::class.java]
                            HunterNotesScreen(
                                viewModel = noteViewModel,
                                onNavigateBack = { safePop() }
                            )
                        }
                        composable("training_plan") {
                            val trainingViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[TrainingPlanViewModel::class.java]
                            TrainingPlanScreen(
                                viewModel = trainingViewModel,
                                onStartTodayTraining = { safeNavigate("workout") },
                                onNavigateBack = { safePop() }
                            )
                        }
                        composable("archive_hub") {
                            val archiveHubViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[ArchiveHubViewModel::class.java]
                            ArchiveHubScreen(
                                viewModel = archiveHubViewModel,
                                onViewArchive = { safeNavigate("archive") },
                                onViewAchievements = { safeNavigate("achievements") },
                                onViewTitles = { safeNavigate("titles") },
                                onNavigateBack = { safePop() }
                            )
                        }
                        composable("profile_hub") {
                            val hunterViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[HomeViewModel::class.java]
                            HunterProfileScreen(
                                viewModel = hunterViewModel,
                                onNavigateBack = { safePop() }
                            )
                        }
                        composable("settings") {
                            val settingsViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[HomeViewModel::class.java]
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onViewAbout = { safeNavigate("about") },
                                onNavigateBack = { safePop() }
                            )
                        }
                        composable("about") {
                            AboutScreen(onNavigateBack = { safePop() })
                        }
                        composable("achievements") {
                            val achievementViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[AchievementViewModel::class.java]
                            AchievementArchiveScreen(
                                viewModel = achievementViewModel,
                                onNavigateBack = { safePop() }
                            )
                        }
                        composable("titles") {
                            val titleViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[TitleViewModel::class.java]
                            TitleArchiveScreen(
                                viewModel = titleViewModel,
                                onNavigateBack = { safePop() }
                            )
                        }
                        composable("workout") {
                            val workoutViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[WorkoutViewModel::class.java]
                            WorkoutScreen(
                                viewModel = workoutViewModel,
                                onNavigateBack = { safePop() }
                            )
                        }
                        composable("archive") {
                            val badgeViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[BadgeViewModel::class.java]
                            HunterArchiveScreen(
                                viewModel = badgeViewModel,
                                onNavigateBack = { safePop() }
                            )
                        }
                        composable("statistics") {
                            val statsViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[StatisticsViewModel::class.java]
                            StatisticsScreen(
                                viewModel = statsViewModel,
                                onNavigateBack = { safePop() }
                            )
                        }
                        composable("history") {
                            val journeyViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[JourneyViewModel::class.java]
                            HunterJourneyScreen(
                                viewModel = journeyViewModel,
                                onNavigateBack = { safePop() }
                            )
                        }
                    }
                }
            }
        }
    }
}
