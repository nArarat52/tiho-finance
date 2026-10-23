package com.example.financetracker

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.rules.ExternalResource
import java.io.File

@RunWith(AndroidJUnit4::class)
class FinanceFlowTest {
    @get:Rule(order = 0) val cleanDatabase = object : ExternalResource() {
        override fun before() { ApplicationProvider.getApplicationContext<Context>().deleteDatabase("tiho.db") }
    }
    @get:Rule(order = 1) val rule = createAndroidComposeRule<MainActivity>()
    @Before fun dismissSystemDialogs() {
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS").close()
        rule.waitUntil(20000) { runCatching { rule.onAllNodesWithText("Общий баланс").fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) }
    }
    private fun snapshot(name: String) {
        rule.waitForIdle()
        val dir = File(ApplicationProvider.getApplicationContext<Context>().getExternalFilesDir(null), "screenshots").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use { output ->
            InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, output)
        }
    }
    @Test fun demoScreensAndRealEntryFlowSurviveRecreation() {
        rule.onNodeWithText("Посмотреть на примере").performScrollTo().performClick()
        rule.waitUntil(20000) { runCatching { rule.onAllNodesWithText("Расходы по категориям").fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) }
        snapshot("overview")
        rule.onNodeWithText("Операции", useUnmergedTree = false).performClick()
        rule.onNodeWithText("Найти операцию").assertExists()
        snapshot("transactions")
        rule.onNodeWithText("Аналитика").performClick()
        rule.onNodeWithText("Расходы за месяц").assertExists()
        snapshot("analytics")
        rule.onNodeWithText("Бюджеты").performClick()
        rule.onNodeWithText("Лимиты по категориям").assertExists()
        snapshot("budgets")
        rule.onNodeWithContentDescription("Добавить операцию").performClick()
        snapshot("entry")
        rule.onNodeWithText("Сумма, ₽").performTextInput("123,45")
        rule.onNodeWithText("Заметка (необязательно)").performScrollTo().performTextInput("Проверка сохранения")
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        rule.onNodeWithText("Сохранить операцию").performScrollTo().performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Операции").performClick()
        rule.onNodeWithText("Найти операцию").performTextInput("Проверка сохранения")
        rule.onNode(hasText("Проверка сохранения") and !hasSetTextAction()).assertExists()
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()
        rule.waitUntil(15000) { runCatching { rule.onAllNodes(hasText("Проверка сохранения") and !hasSetTextAction()).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) }
        rule.onNode(hasText("Проверка сохранения") and !hasSetTextAction()).performClick()
        rule.onNodeWithText("Сумма, ₽").performTextClearance()
        rule.onNodeWithText("Сумма, ₽").performTextInput("456,78")
        rule.onNodeWithText("Сохранить операцию").performScrollTo().performClick()
        rule.waitForIdle()
        rule.onNodeWithText("− 456,78 ₽").assertExists()
    }
}
