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
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.HomeViewModel
import com.example.myapplication.viewmodel.WorkoutViewModel

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screens.WorkoutScreen

import com.example.myapplication.viewmodel.AbilityViewModel
import com.example.myapplication.ui.screens.AbilityScreen
import com.example.myapplication.viewmodel.TitleViewModel
import com.example.myapplication.ui.screens.TitleArchiveScreen
import com.example.myapplication.viewmodel.BadgeViewModel
import com.example.myapplication.ui.screens.HunterArchiveScreen
import com.example.myapplication.viewmodel.StatisticsViewModel
import com.example.myapplication.ui.screens.StatisticsScreen
import com.example.myapplication.viewmodel.WorkoutHistoryViewModel
import com.example.myapplication.ui.screens.WorkoutHistoryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        val repository = FitnessRepository(database.userDao(), database.abilityDao(), database.workoutDao(), database.titleDao())
        
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
                            HomeScreen(
                                viewModel = homeViewModel,
                                onStartWorkout = { navController.navigate("workout") },
                                onViewAbilities = { navController.navigate("abilities") },
                                onViewArchive = { navController.navigate("archive") },
                                onViewStatistics = { navController.navigate("statistics") },
                                onViewHistory = { navController.navigate("history") },
                                onViewTitles = { navController.navigate("titles") }
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
                            WorkoutScreen(
                                viewModel = workoutViewModel,
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
