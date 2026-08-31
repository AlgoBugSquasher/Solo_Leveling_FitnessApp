package com.exork.app

import android.os.Bundle
import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.exork.app.data.AppDatabase
import com.exork.app.data.FitnessRepository
import com.exork.app.data.PreferencesManager
import com.exork.app.viewmodel.*
import com.exork.app.viewmodel.DialogType
import com.exork.app.ui.screens.*
import com.exork.app.ui.components.ExorkSystemDialog
import com.exork.app.ui.components.QuestInfoDialog
import com.exork.app.ui.components.UsernameSetupDialog
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.exork.app.ui.theme.ExorkTheme
import com.exork.app.ui.theme.MonarchSlate
import com.exork.app.util.ReviewHelper
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import kotlinx.coroutines.launch
import coil.imageLoader

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
    private lateinit var appUpdateManager: AppUpdateManager
    private val updateRequestCode = 123
    
    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            // Flexible update downloaded, prompt user to restart (Not implemented here for simplicity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkPlayStoreUpdate()
        
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
                    modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository, preferencesManager, this@MainActivity.filesDir, this@MainActivity.applicationContext) as T
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
                    modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel() as T
                    modelClass.isAssignableFrom(LeaderboardViewModel::class.java) -> LeaderboardViewModel() as T
                    modelClass.isAssignableFrom(HunterNetworkViewModel::class.java) -> HunterNetworkViewModel(repository) as T
                    modelClass.isAssignableFrom(GuildViewModel::class.java) -> GuildViewModel(repository) as T
                    modelClass.isAssignableFrom(AnalyticsViewModel::class.java) -> AnalyticsViewModel(repository) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }

        enableEdgeToEdge()
        
        com.exork.app.receiver.BootReceiver.scheduleDailyReminder(this)

        setContent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { }
                LaunchedEffect(Unit) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            
            ExorkTheme {
            var showForceUpdateDialog by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                db.collection("system").document("config").get()
                    .addOnSuccessListener { doc ->
                        val minVersion = doc.getLong("min_version") ?: 0L
                        if (BuildConfig.VERSION_CODE < minVersion.toInt()) {
                            showForceUpdateDialog = true
                        }
                    }
            }

            if (showForceUpdateDialog) {
                ExorkSystemDialog(
                    title = "SYSTEM UPDATE REQUIRED",
                    content = "A critical system update is mandatory to continue your journey. Please update to the latest version via the Play Store.",
                    primaryButtonText = "UPDATE NOW",
                    onPrimaryClick = {
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=$packageName"))
                            startActivity(intent)
                        } catch (e: Exception) {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                            startActivity(intent)
                        }
                    }
                )
            }
            
            val navController = rememberNavController()
                
                // Shared Activity-scoped HomeViewModel
                val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
                
                val userState = homeViewModel.user.collectAsState()

                // System Notification Queue
                val notificationQueue = remember { mutableStateListOf<SystemNotification>() }
                val activeNotification = notificationQueue.firstOrNull()

                // Dialog Flow Orchestration
                val activeDialog by homeViewModel.activeDialog.collectAsState()
                val quests by homeViewModel.dailyQuests.collectAsState()

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
                            is UiEvent.PenaltyTriggered -> {
                                notificationQueue.add(SystemNotification(
                                    title = "PENALTY ZONE",
                                    content = "You have missed a scheduled training session. Your streak has been reset to 0.",
                                    primaryText = "CONFIRM",
                                    secondaryText = null,
                                    onPrimary = { notificationQueue.removeAt(0) }
                                ))
                            }
                            is UiEvent.Logout -> {
                                // 1. Sign out from Firebase
                                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                
                                // 2. Sign out from Google if applicable
                                try {
                                    val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                                    com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this@MainActivity, gso).signOut()
                                } catch (e: Exception) {}

                                // 3. Clear Coil Image Memory & Disk Cache
                                try {
                                    imageLoader.memoryCache?.clear()
                                    imageLoader.diskCache?.clear()
                                } catch (e: Exception) {}

                                // 4. Reset AuthViewModel explicitly if possible
                                // Since it's activity-scoped, we can find it
                                val authViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[AuthViewModel::class.java]
                                authViewModel.signOut()

                                // 5. Force navigate to auth and clear ENTIRE backstack
                                navController.navigate("auth") {
                                    popUpTo(0) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                            is UiEvent.DeletionCancelled -> {
                                notificationQueue.add(SystemNotification(
                                    title = "🛡️ DELETION CANCELLED",
                                    content = "Welcome back, Hunter.\nYour account deletion request has been cancelled and your profile remains active.",
                                    primaryText = "OK",
                                    secondaryText = null,
                                    onPrimary = { notificationQueue.removeAt(0) }
                                ))
                            }
                            is UiEvent.RequestReview -> {
                                ReviewHelper.launchReviewFlow(this@MainActivity)
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
                    com.exork.app.util.SoundManager.getInstance(this@MainActivity)
                        .setEnabled(userState.value.soundEnabled)
                }

                // Navigation Route Observation
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MonarchSlate
                ) {
                    // Only render high-level dialogs when we are on the Home screen
                    // This prevents overlaps during the Splash sequence.
                    if (currentRoute == "home") {
                        when (activeDialog) {
                            DialogType.WELCOME -> {
                                ExorkSystemDialog(
                                    title = "NOTIFICATION",
                                    content = "You have acquired the qualifications to be a Player.\nWill you accept?",
                                    onPrimaryClick = { homeViewModel.acceptQualifications() },
                                    onSecondaryClick = {
                                        android.widget.Toast.makeText(
                                            this@MainActivity,
                                            "The System does not accept refusal.",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                            DialogType.QUEST_INFO -> {
                                QuestInfoDialog(
                                    quests = quests,
                                    onDismiss = { homeViewModel.dismissActiveDialog() }
                                )
                            }
                            DialogType.USERNAME_SETUP -> {
                                UsernameSetupDialog(
                                    onConfirm = { homeViewModel.saveUsername(it) },
                                    validationState = homeViewModel.usernameValidation.collectAsState().value,
                                    onNameChange = { homeViewModel.validateUsername(it) }
                                )
                            }
                            DialogType.NONE -> {
                                if (activeNotification != null) {
                                    ExorkSystemDialog(
                                        title = activeNotification.title,
                                        content = activeNotification.content,
                                        primaryButtonText = activeNotification.primaryText,
                                        secondaryButtonText = activeNotification.secondaryText,
                                        iconText = activeNotification.iconText,
                                        imageRes = activeNotification.imageRes,
                                        isBadgeLayout = activeNotification.isBadgeLayout,
                                        onPrimaryClick = activeNotification.onPrimary,
                                        onSecondaryClick = activeNotification.onSecondary
                                    )
                                }
                            }
                        }
                    }

                    NavHost(
                        navController = navController, 
                        startDestination = "splash",
                        enterTransition = { slideInHorizontally(animationSpec = tween(150, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(120)) },
                        exitTransition = { slideOutHorizontally(animationSpec = tween(150, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(120)) },
                        popEnterTransition = { slideInHorizontally(animationSpec = tween(150, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(120)) },
                        popExitTransition = { slideOutHorizontally(animationSpec = tween(150, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(120)) }
                    ) {
                        composable("splash") {
                            ExorkSplashScreen(
                                onNavigateToHome = {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.navigate("auth") {
                                        popUpTo("splash") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        composable("auth") {
                            val authViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[AuthViewModel::class.java]
                            AuthScreen(
                                viewModel = authViewModel,
                                onLoginSuccess = {
                                    homeViewModel.syncFromRemote()
                                    navController.navigate("home") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("home") {
                            val analyticsViewModel = viewModel<AnalyticsViewModel>(factory = viewModelFactory)
                            HomeScreen(
                                viewModel = homeViewModel,
                                analyticsViewModel = analyticsViewModel,
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
                                onOpenStatistics = { safeNavigate("statistics") },
                                onOpenLeaderboard = { safeNavigate("leaderboard") },
                                onOpenNetwork = { safeNavigate("network") },
                                onOpenGuild = { safeNavigate("guild") }
                            )
                        }
                        composable("guild") {
                            val guildViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[GuildViewModel::class.java]
                            val coroutineScope = rememberCoroutineScope()
                            var inspectingMember by remember { mutableStateOf<com.exork.app.model.HunterProfile?>(null) }
                            
                            GuildScreen(
                                viewModel = guildViewModel,
                                onNavigateBack = { safePop() },
                                onNavigateToUserProfile = { memberId ->
                                    coroutineScope.launch {
                                        inspectingMember = repository.getHunterProfile(memberId)
                                    }
                                }
                            )

                            if (inspectingMember != null) {
                                com.exork.app.ui.components.HunterProfileInspectDialog(
                                    profile = inspectingMember!!,
                                    onDismiss = { inspectingMember = null }
                                )
                            }
                        }
                        composable("leaderboard") {
                            val leaderboardViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[LeaderboardViewModel::class.java]
                            LeaderboardScreen(
                                viewModel = leaderboardViewModel,
                                onNavigateBack = { safePop() }
                            )
                        }
                        composable("network") {
                            val networkViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[HunterNetworkViewModel::class.java]
                            HunterNetworkScreen(
                                viewModel = networkViewModel,
                                onNavigateBack = { safePop() }
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
                            HunterProfileScreen(
                                viewModel = homeViewModel,
                                onNavigateBack = { safePop() }
                            )
                        }
                        composable("settings") {
                            val authViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[AuthViewModel::class.java]
                            SettingsScreen(
                                viewModel = homeViewModel,
                                authViewModel = authViewModel,
                                onViewAbout = { safeNavigate("about") },
                                onLogout = { homeViewModel.logout() },
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
                                onWorkoutComplete = { xp ->
                                    homeViewModel.triggerXpAnimation(xp)
                                    safePop()
                                },
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

    override fun onResume() {
        super.onResume()
        
        // Handle immediate updates that are already in progress
        if (::appUpdateManager.isInitialized) {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            updateRequestCode
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("AppUpdate", "Resume flow failed", e)
                    }
                }
            }
        }
    }

    private fun checkPlayStoreUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            updateRequestCode
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("AppUpdate", "Immediate flow failed", e)
                    }
                } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    appUpdateManager.registerListener(installStateUpdatedListener)
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.FLEXIBLE,
                            this,
                            updateRequestCode
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("AppUpdate", "Flexible flow failed", e)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::appUpdateManager.isInitialized) {
            try {
                appUpdateManager.unregisterListener(installStateUpdatedListener)
            } catch (e: Exception) {}
        }
    }
}
