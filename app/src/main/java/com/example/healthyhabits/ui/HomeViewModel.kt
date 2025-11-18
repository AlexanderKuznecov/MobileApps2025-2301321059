package com.example.healthyhabits.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthyhabits.model.Habit
import com.example.healthyhabits.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HabitRepository
) : ViewModel() {

    // 🔹 вече се стартира веднага (Eagerly), не чака колектор
    val habits: StateFlow<List<Habit>> =
        repository.getAllHabits()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )

    fun addHabit(name: String, description: String?) {
        viewModelScope.launch {
            val newHabit = Habit(
                name = name,
                description = description
            )
            repository.insertHabit(newHabit)
        }
    }

    fun toggleHabitCompleted(habit: Habit) {
        viewModelScope.launch {
            val updated = habit.copy(
                isCompleted = !habit.isCompleted
            )
            repository.updateHabit(updated)
        }
    }

    fun updateHabitDetails(habit: Habit, newName: String, newDescription: String) {
        viewModelScope.launch {
            repository.updateHabit(
                habit.copy(
                    name = newName,
                    description = newDescription
                )
            )
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }
}
