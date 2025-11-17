package com.example.healthyhabits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthyhabits.ui.theme.HealthyHabitsTheme
import com.example.healthyhabits.model.Habit
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthyhabits.ui.HomeViewModel
import androidx.room.Room
import com.example.healthyhabits.data.HabitDatabase
import com.example.healthyhabits.repository.HabitRepository
import androidx.lifecycle.ViewModelProvider





class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Създаваме инстанция на Room базата
        val db = Room.databaseBuilder(
            applicationContext,
            HabitDatabase::class.java,
            "habit_database"
        ).build()

        // Създаваме Repository
        val habitRepository = HabitRepository(db.habitDao())

        setContent {
            HealthyHabitsTheme {
                val navController = rememberNavController()

                // общ ViewModel за двата екрана
                val homeViewModel = ViewModelProvider(
                    this,
                    HomeViewModel.Factory(habitRepository)
                )[HomeViewModel::class.java]


                Scaffold { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                onAddHabitClick = {
                                    navController.navigate("addHabit")
                                },
                                viewModel = homeViewModel
                            )
                        }
                        composable("addHabit") {
                            AddHabitScreen(
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                onSaveHabit = { name, description ->
                                    // викаме ViewModel – добавяме навика
                                    homeViewModel.addHabit(
                                        name = name,
                                        description = description.ifBlank { null }
                                    )
                                    // връщаме се към Home
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }

        }
    }
}

