package com.hyakuri.kanaflick.ui

import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.view.inputmethod.TextAttribute
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.PlatformTextInputInterceptor
import androidx.compose.ui.platform.PlatformTextInputMethodRequest

/** Keep the system keyboard, request no dictionary suggestions, and retire old-question connections. */
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
internal fun DirectKanaInput(question: String, activeQuestion: () -> String?, content: @Composable () -> Unit) {
    val current by rememberUpdatedState(activeQuestion)
    val interceptor = remember(question) {
        PlatformTextInputInterceptor { request, next ->
            next.startInputMethod(object : PlatformTextInputMethodRequest {
                override fun createInputConnection(outAttributes: EditorInfo): InputConnection {
                    val connection = request.createInputConnection(outAttributes)
                    outAttributes.inputType = (outAttributes.inputType and InputType.TYPE_TEXT_FLAG_AUTO_CORRECT.inv() and
                        InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE.inv()) or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    return object : InputConnectionWrapper(connection, false) {
                        private fun valid() = current() == question
                        override fun commitText(text: CharSequence?, newCursorPosition: Int) = valid() && super.commitText(text, newCursorPosition)
                        override fun setComposingText(text: CharSequence?, newCursorPosition: Int) = valid() && super.setComposingText(text, newCursorPosition)
                        override fun setComposingRegion(start: Int, end: Int) = valid() && super.setComposingRegion(start, end)
                        override fun finishComposingText() = valid() && super.finishComposingText()
                        override fun deleteSurroundingText(beforeLength: Int, afterLength: Int) = valid() && super.deleteSurroundingText(beforeLength, afterLength)
                        override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int) = valid() && super.deleteSurroundingTextInCodePoints(beforeLength, afterLength)
                        override fun sendKeyEvent(event: KeyEvent?) = valid() && super.sendKeyEvent(event)
                        override fun setSelection(start: Int, end: Int) = valid() && super.setSelection(start, end)
                        @RequiresApi(33)
                        override fun commitText(text: CharSequence, newCursorPosition: Int, textAttribute: TextAttribute?) = valid() && super.commitText(text, newCursorPosition, textAttribute)
                        @RequiresApi(33)
                        override fun setComposingText(text: CharSequence, newCursorPosition: Int, textAttribute: TextAttribute?) = valid() && super.setComposingText(text, newCursorPosition, textAttribute)
                        @RequiresApi(33)
                        override fun setComposingRegion(start: Int, end: Int, textAttribute: TextAttribute?) = valid() && super.setComposingRegion(start, end, textAttribute)
                        @RequiresApi(34)
                        override fun replaceText(start: Int, end: Int, text: CharSequence, newCursorPosition: Int, textAttribute: TextAttribute?) = valid() && super.replaceText(start, end, text, newCursorPosition, textAttribute)
                    }
                }
            })
        }
    }
    InterceptPlatformTextInput(interceptor, content)
}
