package com.example.healthyhabits.repository

import com.example.healthyhabits.data.HabitDao
import com.example.healthyhabits.model.Habit
import kotlinx.coroutines.flow.Flow

class HabitRepository(
    private val habitDao: HabitDao
) {

    fun getAllHabits(): Flow<List<Habit>> =
        habitDao.getAllHabits()

    suspend fun insertHabit(habit: Habit) {
        habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }

    suspend fun deleteAll() {
        habitDao.deleteAll()
    }
}
