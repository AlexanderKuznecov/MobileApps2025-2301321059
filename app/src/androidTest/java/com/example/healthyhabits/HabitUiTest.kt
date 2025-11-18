package com.example.healthyhabits

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class HabitUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun addHabitScreen_showsSaveButton() {
        composeTestRule.setContent {
            AddHabitScreen(
                onBackClick = {},
                onSaveHabit = { _, _ -> }
            )
        }

        // Проверяваме, че бутонът "Запази навика" се вижда на екрана
        composeTestRule
            .onNodeWithText("Запази навика")
            .assertIsDisplayed()
    }
}
