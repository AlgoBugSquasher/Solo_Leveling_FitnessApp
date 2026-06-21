package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.FitnessRepository
import com.example.myapplication.ui.screens.*
import com.example.myapplication.viewmodel.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        val repository = FitnessRepository(database.userDao(), database.abilityDao(), database.workoutDao(), database.titleDao(), database.trainingPlanDao())
        
        @Suppress("UNCHECKED_CAST")
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository) as T
                    modelClass.isAssignableFrom(WorkoutViewModel::class.java) -> WorkoutViewModel(repository) as T
                    modelClass.isAssignableFrom(AbilityViewModel::class.java) -> AbilityViewModel(repository) as T
                    modelClass.isAssignableFrom(BadgeViewModel::class.java) -> BadgeViewModel(repository) as T
                    modelClass.isAssignableFrom(TitleViewModel::class.java) -> TitleViewModel(repository) as T
                    modelClass.isAssignableFrom(StatisticsViewModel::class.java) -> StatisticsViewModel(repository) as T
                    modelClass.isAssignableFrom(WorkoutHistoryViewModel::class.java) -> WorkoutHistoryViewModel(repository) as T
                    modelClass.isAssignableFrom(AchievementViewModel::class.java) -> AchievementViewModel(repository) as T
                    modelClass.isAssignableFrom(ArchiveHubViewModel::class.java) -> ArchiveHubViewModel(repository) as T
                    modelClass.isAssignableFrom(TrainingPlanViewModel::class.java) -> TrainingPlanViewModel(repository) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.DarkGray
                ) {
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            val homeViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[HomeViewModel::class.java]
                            val trainingViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[TrainingPlanViewModel::class.java]
                            HomeScreen(
                                viewModel = homeViewModel,
                                trainingViewModel = trainingViewModel,
                                onStartWorkout = { navController.navigate("workout") },
                                onViewArchiveHub = { navController.navigate("archive_hub") },
                                onViewProfileHub = { navController.navigate("profile_hub") },
                                onOpenTrainingPlan = { navController.navigate("training_plan") }
                            )
                        }
                        composable("training_plan") {
                            val trainingViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[TrainingPlanViewModel::class.java]
                            TrainingPlanScreen(
                                viewModel = trainingViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("archive_hub") {
                            val archiveHubViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[ArchiveHubViewModel::class.java]
                            ArchiveHubScreen(
                                viewModel = archiveHubViewModel,
                                onViewArchive = { navController.navigate("archive") },
                                onViewAchievements = { navController.navigate("achievements") },
                                onViewTitles = { navController.navigate("titles") },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("profile_hub") {
                            val homeViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[HomeViewModel::class.java]
                            HunterProfileScreen(
                                viewModel = homeViewModel,
                                onViewStatistics = { navController.navigate("statistics") },
                                onViewHistory = { navController.navigate("history") },
                                onViewAbilities = { navController.navigate("abilities") },
                                onViewSettings = { navController.navigate("settings") },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("settings") {
                            val homeViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[HomeViewModel::class.java]
                            SettingsScreen(
                                viewModel = homeViewModel,
                                onViewAbout = { navController.navigate("about") },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("about") {
                            AboutScreen(onNavigateBack = { navController.popBackStack() })
                        }
                        composable("achievements") {
                            val achievementViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[AchievementViewModel::class.java]
                            AchievementArchiveScreen(
                                viewModel = achievementViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("titles") {
                            val titleViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[TitleViewModel::class.java]
                            TitleArchiveScreen(
                                viewModel = titleViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("workout") {
                            val workoutViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[WorkoutViewModel::class.java]
                            val trainingViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[TrainingPlanViewModel::class.java]
                            WorkoutScreen(
                                viewModel = workoutViewModel,
                                trainingViewModel = trainingViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("abilities") {
                            val abilityViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[AbilityViewModel::class.java]
                            AbilityScreen(
                                viewModel = abilityViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("archive") {
                            val badgeViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[BadgeViewModel::class.java]
                            HunterArchiveScreen(
                                viewModel = badgeViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("statistics") {
                            val statsViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[StatisticsViewModel::class.java]
                            StatisticsScreen(
                                viewModel = statsViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("history") {
                            val historyViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[WorkoutHistoryViewModel::class.java]
                            WorkoutHistoryScreen(
                                viewModel = historyViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
