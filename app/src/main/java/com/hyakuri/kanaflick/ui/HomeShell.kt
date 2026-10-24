package com.hyakuri.kanaflick.ui

import androidx.activity.compose.BackHandler
import androidx.core.content.edit
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.hyakuri.kanaflick.R
import com.hyakuri.kanaflick.vocabulary.Word

@Composable
internal fun HomeShell(onReady: () -> Unit = {}, onSentenceStart: (com.hyakuri.kanaflick.reading.ReadingArticle) -> Unit = {}, onStart: (List<Word>) -> Unit) {
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("practice-options", 0) }
    var targeted by remember { mutableStateOf(preferences.getBoolean("targeted-kana", false)) }
    var roundSize by rememberSaveable { mutableIntStateOf(preferences.getInt("round-size", 10).takeIf { it in listOf(5, 10, 20) } ?: 10) }
    BackHandler(tab != 0) { tab = 0 }
    Surface(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))) {
        Box(Modifier.weight(1f)) {
            LibraryHome(tab, roundSize, { value ->
                preferences.edit { putInt("round-size", value) }
                roundSize = value
            }, onStart, onReady, onSentenceStart, targeted) { value ->
                targeted = value; preferences.edit { putBoolean("targeted-kana", value) }
            }
        }
        NavigationBar(
            modifier = Modifier.background(NavigationBarDefaults.containerColor).windowInsetsPadding(WindowInsets.navigationBars).height(64.dp),
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            listOf("打字", "学习", "选项").forEachIndexed { index, label ->
                NavigationBarItem(selected = tab == index, onClick = { focus.clearFocus(); keyboard?.hide(); tab = index },
                    icon = { Icon(painterResource(listOf(R.drawable.nav_typing, R.drawable.nav_learning, R.drawable.nav_options)[index]), contentDescription = null, modifier = Modifier.size(28.dp)) },
                    label = { Text(label) }, modifier = Modifier.offset(y = 4.dp).testTag("tab-$index"))
            }
        }
    }
}
}

