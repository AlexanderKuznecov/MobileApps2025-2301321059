package com.example.healthyhabits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthyhabits.model.Habit

@Composable
fun HomeScreen(onAddHabitClick: () -> Unit) {
    // временни примерни навици
    val sampleHabits = listOf(
        Habit(id = 1, name = "Пиене на вода", description = "8 чаши на ден"),
        Habit(id = 2, name = "Разходка", description = "30 минути навън"),
        Habit(id = 3, name = "Четене", description = "15 минути книга")
    )

    androidx.compose.material3.Scaffold(
        floatingActionButton = {
            Button(onClick = onAddHabitClick) {
                Text(text = "Добави навик")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "HealthyHabits+",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sampleHabits) { habit ->
                    HabitItem(habit = habit)
                }
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleMedium
            )
            if (!habit.description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
