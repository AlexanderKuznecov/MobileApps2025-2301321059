package com.example.healthyhabits.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthyhabits.model.Habit
import com.example.healthyhabits.repository.HabitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: HabitRepository
) : ViewModel() {

    // 🔹 това вече идва директно от Room чрез Flow → StateFlow
    val habits: StateFlow<List<Habit>> =
        repository.getAllHabits()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    // 🔹 вече НЕ е suspend – викаме го от UI директно
    fun addHabit(name: String, description: String?) {
        viewModelScope.launch {
            val newHabit = Habit(
                name = name,
                description = description
            )
            repository.insertHabit(newHabit)
        }
    }

    // --- FACTORY ---
    class Factory(
        private val repository: HabitRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
