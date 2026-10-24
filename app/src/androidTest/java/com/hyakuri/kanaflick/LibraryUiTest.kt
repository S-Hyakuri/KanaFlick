package com.hyakuri.kanaflick
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class LibraryUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private fun click(label: String) { rule.onNodeWithText(label).performScrollTo().performClick() }
    @Test fun createEditRenameRecreateAndDeleteWithConfirmation() {
        click("管理自定义词库"); click("新建词库")
        rule.onNodeWithText("词库名称（1–80 字符）").performTextInput("设备测试词库")
        rule.onNodeWithText("保存").performClick()
        rule.waitUntil(5000) { rule.onAllNodesWithText("添加词条").fetchSemanticsNodes().isNotEmpty() }
        click("添加词条")
        rule.onNodeWithText("日语原文").performTextInput("猫")
        rule.onNodeWithText("假名读音").performTextInput("ねこ")
        rule.onNodeWithText("中文释义").performTextInput("猫")
        rule.onNodeWithText("保存词条").performClick()
        rule.waitUntil(5000) { rule.onAllNodesWithText("编辑 猫").fetchSemanticsNodes().isNotEmpty() }
        click("编辑 猫")
        rule.onNodeWithText("中文释义").performTextReplacement("小猫")
        rule.onNodeWithText("保存词条").performClick()
        rule.waitUntil(5000) { rule.onAllNodesWithText("ねこ · 小猫 · 未分级").fetchSemanticsNodes().isNotEmpty() }
        click("重命名")
        rule.onNodeWithText("词库名称（1–80 字符）").performTextReplacement("重命名测试词库")
        rule.onNodeWithText("保存").performClick()
        rule.waitUntil(5000) { rule.onAllNodesWithText("重命名测试词库").fetchSemanticsNodes().isNotEmpty() }
        rule.activityRule.scenario.recreate()
        rule.waitUntil(5000) { rule.onAllNodesWithText("ねこ · 小猫 · 未分级").fetchSemanticsNodes().isNotEmpty() }
        click("删除词库"); rule.onNodeWithText("取消").performClick()
        rule.onNodeWithText("ねこ · 小猫 · 未分级").assertExists()
        click("删除词库"); rule.onNodeWithText("确认删除").performClick()
        rule.waitUntil(5000) { rule.onAllNodesWithText("重命名测试词库").fetchSemanticsNodes().isEmpty() }
        rule.activityRule.scenario.recreate()
        rule.onNodeWithText("重命名测试词库").assertDoesNotExist()
    }
}
