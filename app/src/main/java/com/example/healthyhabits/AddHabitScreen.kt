package com.example.healthyhabits

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
fun AddHabitScreen(
    onBackClick: () -> Unit,
    onSaveHabit: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // състояние за избрана дата и ден
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var selectedDay by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    // DatePickerDialog – потребителят избира дата, ние смятаме деня
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            // записваме датата като текст
            val formattedDate = "%02d.%02d.%04d".format(dayOfMonth, month + 1, year)
            selectedDate = formattedDate

            // смятаме деня от седмицата
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Нов навик",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                // Име
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Име на навика") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Описание
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание (по желание)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Бутон за избор на дата
                Text(
                    text = "Дата от календара:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedDate ?: "Избери дата"
                    )
                }

                // Показваме информация за избраната дата и ден
                if (selectedDate != null || selectedDay != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (selectedDate != null) {
                        Text(
                            text = "Избрана дата: $selectedDate",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (selectedDay != null) {
                        Text(
                            text = "Ден от седмицата: $selectedDay",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Запази
        Button(
            onClick = {
                if (name.isNotBlank() && selectedDate != null && selectedDay != null) {
                    val finalDescription = buildString {
                        append("Дата: $selectedDate\n")
                        append("Ден: $selectedDay\n")
                        if (description.isNotBlank()) {
                            append(description)
                        }
                    }
                    onSaveHabit(name, finalDescription)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Запази навика")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors()
        ) {
            Text("Назад без запис")
        }
    }
}
