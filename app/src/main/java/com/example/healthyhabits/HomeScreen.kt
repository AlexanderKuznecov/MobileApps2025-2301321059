package com.example.healthyhabits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.healthyhabits.model.Habit
import com.example.healthyhabits.ui.HomeViewModel
import com.example.healthyhabits.utils.calculateCompletionPercentage
import android.content.Intent

@Composable
fun HomeScreen(
    onAddHabitClick: () -> Unit,
    viewModel: HomeViewModel
) {
    val habits by viewModel.habits.collectAsState()
    val context = LocalContext.current
    val completionPercent = calculateCompletionPercentage(habits)

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

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Изпълнени навици: $completionPercent%",
                style = MaterialTheme.typography.bodyMedium
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
                        },
                        onShare = { selected ->
                            val shareText = buildString {
                                append("Навик: ${selected.name}\n")
                                if (!selected.description.isNullOrEmpty()) {
                                    append("Описание: ${selected.description}\n")
                                }
                                append("Статус: ")
                                append(if (selected.isCompleted) "завършен ✅" else "в процес ⏳")
                            }

                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Моят навик от HealthyHabits+")
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }

                            val chooser = Intent.createChooser(intent, "Сподели навика чрез...")
                            context.startActivity(chooser)
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
    onDelete: (Habit) -> Unit,
    onShare: (Habit) -> Unit
) {
    // --- 1) Разбиваме описанието на части ---
    val rawDescription = habit.description.orEmpty()
    val lines = rawDescription
        .lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val dateLine = lines.firstOrNull { it.startsWith("Дата:") }
    val dayLine = lines.firstOrNull { it.startsWith("Ден:") }

    val bodyText = lines
        .filterNot { it.startsWith("Дата:") || it.startsWith("Ден:") }
        .joinToString("\n")

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompleted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // чекбокс
                Checkbox(
                    checked = habit.isCompleted,
                    onCheckedChange = {
                        onToggleCompleted(habit)
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // лява колона: име + ден/дата + описание
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Име на навика
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (habit.isCompleted) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        }
                    )

                    // ред с ден и дата, ако ги има
                    if (dayLine != null || dateLine != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            dayLine?.let {
                                Text(
                                    text = it.removePrefix("Ден:").trim(),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            dateLine?.let {
                                Text(
                                    text = it.removePrefix("Дата:").trim(),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    // описание, ако има
                    if (bodyText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = bodyText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // дясна колона: бутони
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    TextButton(
                        onClick = { onShare(habit) }
                    ) {
                        Text("Сподели")
                    }
                    TextButton(
                        onClick = { onDelete(habit) }
                    ) {
                        Text("Изтрий")
                    }
                }
            }
        }
    }
}
