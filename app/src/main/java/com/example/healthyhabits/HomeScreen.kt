package com.example.healthyhabits

import android.app.DatePickerDialog
import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.healthyhabits.model.Habit
import com.example.healthyhabits.ui.HomeViewModel
import com.example.healthyhabits.utils.calculateCompletionPercentage
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date


@Composable
fun HomeScreen(
    onAddHabitClick: () -> Unit,
    viewModel: HomeViewModel
) {
    val habits by viewModel.habits.collectAsState()
    val context = LocalContext.current
    val completionPercent = calculateCompletionPercentage(habits)

    val totalHabits = habits.size
    val completedHabits = habits.count { it.isCompleted }

    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    // групиране по (ден, дата)
    val groupedHabits: Map<Pair<String?, String?>, List<Habit>> = remember(habits) {
        habits.groupBy { habit ->
            val rawDescription = habit.description.orEmpty()
            val lines = rawDescription
                .lines()
                .map { it.trim() }
                .filter { it.isNotBlank() }

            val dateLine = lines.firstOrNull { it.startsWith("Дата:") }
            val dayLine = lines.firstOrNull { it.startsWith("Ден:") }

            val day = dayLine?.removePrefix("Ден:")?.trim()
            val date = dateLine?.removePrefix("Дата:")?.trim()

            day to date
        }
    }

    // днешна дата (без часове)
    val today = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    // разделяме групите на предстоящи/днес и минали
    val (upcomingOrTodayGroups, pastGroups) = remember(groupedHabits) {
        groupedHabits.entries.partition { entry ->
            val dateStr = entry.key.second
            val date = parseDateOrNull(dateStr)
            // ако няма дата – броим го като "предстоящ/без дата", за да е горе
            date == null || !date.before(today)
        }
    }

    // сортиране по дата (ако няма дата – отива най-отгоре в съответния списък)
    fun sortedGroups(entries: List<Map.Entry<Pair<String?, String?>, List<Habit>>>) =
        entries.sortedWith(
            compareBy<Map.Entry<Pair<String?, String?>, List<Habit>>> { entry ->
                val dateStr = entry.key.second
                parseDateOrNull(dateStr) ?: Date(Long.MAX_VALUE)
            }.thenBy { entry ->
                entry.key.first ?: ""
            }
        )

    val upcomingSorted = sortedGroups(upcomingOrTodayGroups)
    val pastSorted = sortedGroups(pastGroups)

    Scaffold(
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

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Общо: $totalHabits   Завършени: $completedHabits",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // --- ПРЕДСТОЯЩИ / ДНЕС ---
                if (upcomingSorted.isNotEmpty()) {
                    item {
                        Text(
                            text = "Предстоящи навици",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    upcomingSorted.forEach { (dayDatePair, habitsInGroup) ->
                        val (day, date) = dayDatePair

                        item {
                            DayHeader(day = day, date = date)
                        }

                        items(habitsInGroup) { habit ->
                            HabitItem(
                                habit = habit,
                                onToggleCompleted = { selected ->
                                    viewModel.toggleHabitCompleted(selected)
                                },
                                onDelete = { selected ->
                                    habitToDelete = selected
                                },
                                onShare = { selected ->
                                    val shareText = buildString {
                                        append("Навик: ${selected.name}\n")
                                        if (!selected.description.isNullOrEmpty()) {
                                            append("Описание: ${selected.description}\n")
                                        }
                                        append("Статус: ")
                                        append(
                                            if (selected.isCompleted)
                                                "завършен ✅"
                                            else
                                                "в процес ⏳"
                                        )
                                    }

                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_SUBJECT,
                                            "Моят навик от HealthyHabits+"
                                        )
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }

                                    val chooser =
                                        Intent.createChooser(intent, "Сподели навика чрез...")
                                    context.startActivity(chooser)
                                },
                                onEdit = { selected ->
                                    habitToEdit = selected
                                }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                // --- МИНАЛИ ---
                if (pastSorted.isNotEmpty()) {
                    item {
                        Text(
                            text = "Минали навици",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    pastSorted.forEach { (dayDatePair, habitsInGroup) ->
                        val (day, date) = dayDatePair

                        item {
                            DayHeader(day = day, date = date)
                        }

                        items(habitsInGroup) { habit ->
                            HabitItem(
                                habit = habit,
                                onToggleCompleted = { selected ->
                                    viewModel.toggleHabitCompleted(selected)
                                },
                                onDelete = { selected ->
                                    habitToDelete = selected
                                },
                                onShare = { selected ->
                                    val shareText = buildString {
                                        append("Навик: ${selected.name}\n")
                                        if (!selected.description.isNullOrEmpty()) {
                                            append("Описание: ${selected.description}\n")
                                        }
                                        append("Статус: ")
                                        append(
                                            if (selected.isCompleted)
                                                "завършен ✅"
                                            else
                                                "в процес ⏳"
                                        )
                                    }

                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_SUBJECT,
                                            "Моят навик от HealthyHabits+"
                                        )
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }

                                    val chooser =
                                        Intent.createChooser(intent, "Сподели навика чрез...")
                                    context.startActivity(chooser)
                                },
                                onEdit = { selected ->
                                    habitToEdit = selected
                                }
                            )
                        }
                    }
                }
            }

            // Диалог за редакция
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

            // Диалог за изтриване
            habitToDelete?.let { habit ->
                AlertDialog(
                    onDismissRequest = { habitToDelete = null },
                    title = { Text(text = "Изтриване на навик") },
                    text = {
                        Text(
                            "Сигурни ли сте, че искате да изтриете „${habit.name}“?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteHabit(habit)
                                habitToDelete = null
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Да, изтрий")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { habitToDelete = null }) {
                            Text("Отказ")
                        }
                    }
                )
            }
        }
    }
}


/**
 * Заглавие на група – ден + дата
 */
@Composable
fun DayHeader(
    day: String?,
    date: String?
) {
    val label = when {
        day != null && date != null -> "$day – $date"
        day != null -> day
        date != null -> date
        else -> "Без посочен ден"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(2.dp)
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
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

    val scale by animateFloatAsState(
        targetValue = if (habit.isCompleted) 0.97f else 1f,
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompleted)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(if (habit.isCompleted) 2.dp else 6.dp)
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
                                        color = dayColor.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
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
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
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
                    Icon(Icons.Filled.Share, contentDescription = "Сподели")
                }

                IconButton(onClick = { onEdit(habit) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Редактирай")
                }

                IconButton(onClick = { onDelete(habit) }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Изтрий",
                        tint = MaterialTheme.colorScheme.error
                    )
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

private fun parseDateOrNull(dateStr: String?): Date? {
    if (dateStr.isNullOrBlank()) return null
    return try {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        sdf.isLenient = false
        sdf.parse(dateStr)
    } catch (e: Exception) {
        null
    }
}

