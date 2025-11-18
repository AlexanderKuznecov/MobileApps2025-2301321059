package com.example.healthyhabits

import com.example.healthyhabits.FakeHabitDao
import com.example.healthyhabits.model.Habit
import com.example.healthyhabits.repository.HabitRepository
import com.example.healthyhabits.ui.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    // Тестов dispatcher, който ще замести Dispatchers.Main
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeDao: FakeHabitDao
    private lateinit var repository: HabitRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        // пренасочваме Main към тестов dispatcher
        Dispatchers.setMain(testDispatcher)

        fakeDao = FakeHabitDao()
        repository = HabitRepository(fakeDao)
        viewModel = HomeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addHabit_addsHabitToList() = runTest {
        // WHEN
        viewModel.addHabit("Тестов навик", "Описание")

        // изчакваме всички корутини във viewModelScope да приключат
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        val habits = viewModel.habits.value
        assertEquals(1, habits.size)
        assertEquals("Тестов навик", habits[0].name)
        assertEquals("Описание", habits[0].description)
    }

    @Test
    fun toggleHabitCompleted_changesIsCompletedFlag() = runTest {
        // GIVEN – добавяме един навик в базата през Fake DAO
        val habit = Habit(
            id = 1,
            name = "Навик за тест",
            description = null,
            isCompleted = false
        )
        fakeDao.insertHabit(habit)

        // реинициализираме ViewModel, за да зареди текущите данни
        viewModel = HomeViewModel(repository)

        // WHEN – маркираме като изпълнен
        viewModel.toggleHabitCompleted(habit)
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN
        val updatedList = viewModel.habits.value
        assertEquals(1, updatedList.size)
        assertEquals(true, updatedList[0].isCompleted)
    }

    @Test
    fun deleteHabit_removesFromList() = runTest {
        // GIVEN – два навика
        val habit1 = Habit(id = 1, name = "Първи", description = null)
        val habit2 = Habit(id = 2, name = "Втори", description = null)
        fakeDao.insertHabit(habit1)
        fakeDao.insertHabit(habit2)

        viewModel = HomeViewModel(repository)

        // WHEN – изтриваме първия
        viewModel.deleteHabit(habit1)
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN – остава само вторият
        val habits = viewModel.habits.value
        assertEquals(1, habits.size)
        assertEquals("Втори", habits[0].name)
    }
}
