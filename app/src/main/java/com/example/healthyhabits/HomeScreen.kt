package com.example.healthyhabits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthyhabits.model.Habit
import com.example.healthyhabits.ui.HomeViewModel
import androidx.compose.material3.Checkbox
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material3.TextButton
import android.content.Intent
import androidx.compose.ui.platform.LocalContext



@Composable
fun HomeScreen(
    onAddHabitClick: () -> Unit,
    viewModel: HomeViewModel
) {
    val habits by viewModel.habits.collectAsState()

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
                items(habits) { habit ->
                    HabitItem(
                        habit = habit,
                        onToggleCompleted = { selected ->
                            viewModel.toggleHabitCompleted(selected)
                        },
                        onDelete = { selected ->
                            viewModel.deleteHabit(selected)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    onToggleCompleted: (Habit) -> Unit,
    onDelete: (Habit) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = habit.isCompleted,
                onCheckedChange = {
                    onToggleCompleted(habit)
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (habit.isCompleted) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )
                if (!habit.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            TextButton(
                onClick = { onDelete(habit) }
            ) {
                Text("Изтрий")
            }
        }
    }
}


