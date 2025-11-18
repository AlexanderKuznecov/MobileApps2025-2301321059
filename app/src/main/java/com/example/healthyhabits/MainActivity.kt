package com.example.healthyhabits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthyhabits.ui.HomeViewModel
import com.example.healthyhabits.ui.theme.HealthyHabitsTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HealthyHabitsRoot()
        }
    }
}

@Composable
fun HealthyHabitsRoot() {
    HealthyHabitsTheme {
        val navController = rememberNavController()

        // Взимаме ViewModel-а през Hilt
        val homeViewModel: HomeViewModel = hiltViewModel()

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
                        onBackClick = { navController.popBackStack() },
                        onSaveHabit = { name, description ->
                            homeViewModel.addHabit(
                                name = name,
                                description = description.ifBlank { null }
                            )
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
