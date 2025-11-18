package com.example.healthyhabits

import android.app.DatePickerDialog
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.healthyhabits.model.Habit
import com.example.healthyhabits.ui.HomeViewModel
import com.example.healthyhabits.utils.calculateCompletionPercentage
import java.util.Calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share


@Composable
fun HomeScreen(
    onAddHabitClick: () -> Unit,
    viewModel: HomeViewModel
) {
    val habits by viewModel.habits.collectAsState()
    val context = LocalContext.current
    val completionPercent = calculateCompletionPercentage(habits)

    // тук пазим кой навик редактираме в момента
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }

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
                        },
                        onEdit = { selected ->
                            habitToEdit = selected
                        }
                    )
                }
            }

            // Диалог за редакция, ако има избран навик
            habitToEdit?.let { habit ->
                EditHabitDialog(
                    habit = habit,
                    onDismiss = { habitToEdit = null },
                    onSave = { newName, newDescription ->
                        viewModel.updateHabitDetails(habit, newName, newDescription)
                        habitToEdit = null
                    }
                )
            }
        }
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    onToggleCompleted: (Habit) -> Unit,
    onDelete: (Habit) -> Unit,
    onShare: (Habit) -> Unit,
    onEdit: (Habit) -> Unit
) {
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

    val day = dayLine?.removePrefix("Ден:")?.trim()
    val date = dateLine?.removePrefix("Дата:")?.trim()

    val dayColor = when (day) {
        "Понеделник" -> MaterialTheme.colorScheme.primary
        "Вторник" -> MaterialTheme.colorScheme.tertiary
        "Сряда" -> MaterialTheme.colorScheme.secondary
        "Четвъртък" -> MaterialTheme.colorScheme.error
        "Петък" -> MaterialTheme.colorScheme.inversePrimary
        "Събота" -> MaterialTheme.colorScheme.primaryContainer
        "Неделя" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompleted)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = habit.isCompleted,
                onCheckedChange = { onToggleCompleted(habit) }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {

                // Име
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (habit.isCompleted)
                        TextDecoration.LineThrough else TextDecoration.None
                )

                // Ден + дата — цветни бейджове
                if (day != null || date != null) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                        // Ден бейдж
                        if (day != null) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = dayColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = day,
                                    color = dayColor,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        // Дата бейдж
                        if (date != null) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = date,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // Описание
                if (bodyText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = bodyText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Иконки
            Column(horizontalAlignment = Alignment.End) {

                IconButton(onClick = { onShare(habit) }) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }

                IconButton(onClick = { onEdit(habit) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }

                IconButton(onClick = { onDelete(habit) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}


@Composable
fun EditHabitDialog(
    habit: Habit,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    // парсваме описанието
    val rawDescription = habit.description.orEmpty()
    val lines = rawDescription
        .lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val dateLine = lines.firstOrNull { it.startsWith("Дата:") }
    val dayLine = lines.firstOrNull { it.startsWith("Ден:") }

    val initialBody = lines
        .filterNot { it.startsWith("Дата:") || it.startsWith("Ден:") }
        .joinToString("\n")

    var name by remember { mutableStateOf(habit.name) }
    var description by remember { mutableStateOf(initialBody) }
    var selectedDate by remember { mutableStateOf(dateLine?.removePrefix("Дата:")?.trim()) }
    var selectedDay by remember { mutableStateOf(dayLine?.removePrefix("Ден:")?.trim()) }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    // когато отворим DatePicker – при избор изчисляваме и деня
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = "%02d.%02d.%04d".format(dayOfMonth, month + 1, year)
            selectedDate = formattedDate

            calendar.set(year, month, dayOfMonth)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            selectedDay = when (dayOfWeek) {
                Calendar.MONDAY -> "Понеделник"
                Calendar.TUESDAY -> "Вторник"
                Calendar.WEDNESDAY -> "Сряда"
                Calendar.THURSDAY -> "Четвъртък"
                Calendar.FRIDAY -> "Петък"
                Calendar.SATURDAY -> "Събота"
                Calendar.SUNDAY -> "Неделя"
                else -> null
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Редакция на навик")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Име на навика") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Дата: ${selectedDate ?: "не е избрана"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Ден: ${selectedDay ?: "не е определен"}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text("Промени дата")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val finalDescription = buildString {
                            if (selectedDate != null) {
                                append("Дата: $selectedDate\n")
                            }
                            if (selectedDay != null) {
                                append("Ден: $selectedDay\n")
                            }
                            if (description.isNotBlank()) {
                                append(description)
                            }
                        }
                        onSave(name, finalDescription)
                    }
                }
            ) {
                Text("Запази")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отказ")
            }
        }
    )
}
