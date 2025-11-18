package com.example.healthyhabits

import com.example.healthyhabits.data.HabitDao
import com.example.healthyhabits.model.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow


class FakeHabitDao : HabitDao {

    private val habitsFlow = MutableStateFlow<List<Habit>>(emptyList())

    //Имплементация на DAO интерфейса

    override fun getAllHabits(): Flow<List<Habit>> = habitsFlow

    override suspend fun insertHabit(habit: Habit) {
        val current = habitsFlow.value.toMutableList()
        current.add(habit.copy(id = (current.maxOfOrNull { it.id } ?: 0) + 1))
        habitsFlow.value = current
    }

    override suspend fun updateHabit(habit: Habit) {
        val current = habitsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            current[index] = habit
            habitsFlow.value = current
        }
    }

    override suspend fun deleteHabit(habit: Habit) {
        val current = habitsFlow.value.toMutableList()
        current.removeAll { it.id == habit.id }
        habitsFlow.value = current
    }

    override suspend fun deleteAll() {
        habitsFlow.value = emptyList()
    }
}
