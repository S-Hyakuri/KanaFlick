package com.hyakuri.kanaflick.ui

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.hyakuri.kanaflick.R
import com.hyakuri.kanaflick.practice.*
import com.hyakuri.kanaflick.vocabulary.*
import kotlinx.coroutines.delay
import com.hyakuri.kanaflick.proficiency.*

private val SessionSaver = listSaver<PracticeState, Any>(
    save = { PracticeSnapshot.save(it) },
    restore = PracticeSnapshot::restore,
)
@Composable
fun KanaFlickApp(onReady: () -> Unit = {}) {
    val appContext = LocalContext.current
    val repository = remember { ProfileRepository.get(appContext) }
    val profileView by repository.state.collectAsState()
    val homeState = rememberSaveableStateHolder()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var session by rememberSaveable(stateSaver = SessionSaver) { mutableStateOf(PracticeState()) }
    var input by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    var confirmedInput by rememberSaveable { mutableStateOf("") }
    // Restored text is displayed only; scoring is driven exclusively by fresh onValueChange edits.
    var revision by rememberSaveable { mutableLongStateOf(0L) }
    var confirmExit by rememberSaveable { mutableStateOf(false) }
    val timer = rememberSaveable(saver = Saver<SessionTimer, Long>(
        save = { it.elapsedMillis() }, restore = { SessionTimer(SystemClock::elapsedRealtime, it) }
    )) { SessionTimer(SystemClock::elapsedRealtime) }
    var focusRequested by rememberSaveable { mutableStateOf(false) }
    var practiceReady by remember { mutableStateOf(false) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var resumed by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    val windowFocused = LocalWindowInfo.current.isWindowFocused
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, _ ->
            resumed = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            if (!resumed) { timer.pause() }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); timer.pause() }
    }
    LaunchedEffect(resumed, practiceReady, session.page) {
        if (resumed && practiceReady && session.page == Page.PRACTICE) timer.resume() else timer.pause()
    }
    if (session.page != Page.HOME) SideEffect { onReady() }
    fun clearInput() { input = TextFieldValue(); confirmedInput = ""; revision++ }
    fun start(words: List<Word>, sentences: Boolean = false, title: String = "") {
        timer.reset(); focusRequested = false; practiceReady = false
        session = session.start(words, sentences, title).copy(kana = KanaProgress(session = java.util.UUID.randomUUID().toString()))
        if (profileView.ready && !profileView.unavailable) {
            session = session.copy(kana = session.kana.copy(generation = repository.generationForNewSessions,
                session = session.kana.session.ifEmpty { java.util.UUID.randomUUID().toString() }))
            repository.begin(session.kana)
        }
        if (!sentences) repository.rememberWords(words.map { it.id }.toSet())
        clearInput()
    }
    fun exit() {
        focusManager.clearFocus(); keyboard?.hide(); timer.reset(); practiceReady = false
        session = session.exit(); clearInput()
    }
    LaunchedEffect(profileView.ready, profileView.unavailable, profileView.profile.generation, session.kana.session) {
        if (profileView.ready && !profileView.unavailable && session.page != Page.HOME && session.kana.generation.isEmpty()) {
            session = session.copy(kana = session.kana.copy(generation = repository.generationForNewSessions,
                session = session.kana.session.ifEmpty { java.util.UUID.randomUUID().toString() }))
            repository.begin(session.kana)
        }
    }
    LaunchedEffect(session.kana) {
        if (session.kana.generation.isNotEmpty() && session.kana.pending.isNotEmpty()) repository.submit(session.kana)
    }
    LaunchedEffect(profileView.profile.cursor, profileView.profile.session) {
        if (session.kana.session == profileView.profile.session) session = session.copy(kana = session.kana.copy(
            pending = session.kana.pending.filter { it.question > profileView.profile.cursor }))
    }
    BackHandler(session.page != Page.HOME) { if (session.page == Page.PRACTICE) confirmExit = true else exit() }
    when (session.page) {
        Page.HOME -> homeState.SaveableStateProvider("home") { HomeShell(onReady = onReady, onSentenceStart = { start(it.practiceWords(), true, it.title) }) { start(it) } }
        Page.PRACTICE -> {
            val requester = remember { FocusRequester() }
            val entrance = remember { Animatable(0f) }
            LaunchedEffect(Unit) { entrance.animateTo(1f, tween(180)) }
            LaunchedEffect(resumed, windowFocused) {
                if (resumed && windowFocused) {
                    withFrameNanos { }
                    practiceReady = true
                    if (!focusRequested && !confirmExit) {
                        focusRequested = true
                        requester.requestFocus()
                        keyboard?.show()
                    }
                }
            }
            PracticeBody(modifier = Modifier.graphicsLayer {
                alpha = entrance.value
                translationY = (1f - entrance.value) * 8.dp.toPx()
            }) {
                if (profileView.message.isNotEmpty()) Text(profileView.message, color = MaterialTheme.colorScheme.error)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        TextButton(onClick = { confirmExit = true }, modifier = Modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 4.dp)) { Text("退出练习") }
                    }
                    Text("${session.index} / ${session.words.size}", Modifier.weight(1f).testTag("progress"), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { TimerLabel(timer) }
                }
                LinearProgressIndicator(progress = { session.index.toFloat() / session.words.size }, modifier = Modifier.fillMaxWidth())
                if (session.sentenceMode) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(session.articleTitle, style = MaterialTheme.typography.titleMedium)
                        Text("第 ${session.index + 1} / ${session.words.size} 句 · ${session.word.reading}", Modifier.testTag("sentence-original"))
                        key(session.index) { SentencePrompt(session.match(confirmedInput).expected, confirmedInput) }
                    }
                } else WordCarousel(session)
                if (!session.sentenceMode) Text("${session.word.reading} · ${session.word.meaning}", Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Column(Modifier.align(Alignment.CenterHorizontally).widthIn(max = 280.dp).fillMaxWidth()) {
                val inputQuestion = session.index
                DirectKanaInput("${session.kana.session}:$inputQuestion", { if (session.page == Page.PRACTICE) "${session.kana.session}:${session.index}" else null }) {
                OutlinedTextField(value = input, onValueChange = {
                    if (session.index != inputQuestion || session.page != Page.PRACTICE) return@OutlinedTextField
                    val changed = it.text != input.text
                    input = it
                    if (changed) {
                        revision++
                        confirmedInput = it.text
                        if (windowFocused && resumed && !confirmExit) {
                            val next = session.evaluateInput(it.text, revision, inputQuestion, it.composition != null)
                            session = next
                            repository.submit(next.kana)
                            if (next.index != inputQuestion) clearInput()
                            if (next.page == Page.RESULT) { timer.pause(); focusManager.clearFocus(); keyboard?.hide() }
                        }
                    }
                }, singleLine = true, textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, autoCorrectEnabled = false), keyboardActions = KeyboardActions(onDone = { }),
                    modifier = Modifier.fillMaxWidth().focusRequester(requester).testTag("answer").semantics { contentDescription = "答案" })
                }
                TextButton(onClick = {
                    session = session.skip()
                    repository.submit(session.kana)
                    clearInput()
                    if (session.page == Page.RESULT) { timer.pause(); focusManager.clearFocus(); keyboard?.hide() }
                }, modifier = Modifier.align(Alignment.End).heightIn(min = 48.dp).testTag("skip")) {
                    Icon(painterResource(R.drawable.action_skip), contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (session.sentenceMode) "跳过本句" else "跳过")
                }
                }
                val live = session.match(input.text, input.composition != null)
                val liveAnswer = session.normalized(input.text)
                Text(when {
                    live.pendingConversion -> "正在完成假名输入…"
                    session.feedback == Feedback.WRONG -> "第 ${live.prefix + 1} 字不匹配，请修改；输入已保留。"
                    liveAnswer.isNotEmpty() -> "已匹配 ${live.prefix} / ${live.expected.length} 字"
                    session.feedback == Feedback.SKIPPED -> if (session.sentenceMode) "已跳过上一句" else "已跳过上一词"
                    session.feedback == Feedback.CORRECT -> "答对了！继续下一题"
                    else -> "逐字判断；可输入原文或完整假名，无须选择候选。"
                }, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().testTag("feedback").semantics { liveRegion = LiveRegionMode.Polite },
                    color = if (session.feedback == Feedback.WRONG) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.align(Alignment.CenterHorizontally).testTag("counts").clearAndSetSemantics { contentDescription = "正确 ${session.correct} 次，错误 ${session.errors} 次" }, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    PracticeCount(R.drawable.stat_correct, session.correct)
                    PracticeCount(R.drawable.stat_error, session.errors)
                }
            }
        }
        Page.RESULT -> Surface(Modifier.fillMaxSize()) {
            Column(Modifier.safeDrawingPadding().fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)) {
                    if (profileView.message.isNotEmpty()) {
                        Text(profileView.message, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = { repository.retry() }) { Text("重试保存") }
                    }
                    Text(if (session.index == session.words.size) "练习完成" else "本次练习结算", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
                    Surface(shape = MaterialTheme.shapes.extraLarge, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                        Column(Modifier.padding(horizontal = 32.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("正确率", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(Score.formatAccuracy(session.accuracy), Modifier.testTag("accuracy"), style = MaterialTheme.typography.displaySmall)
                        }
                    }
                    Text(if (session.sentenceMode) "正确率 = 完成句数 ÷（完成句数 + 错误次数）× 100%" else "正确率 = 正确次数 ÷（正确次数 + 错误次数）× 100%", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    Row(Modifier.clearAndSetSemantics { contentDescription = "正确 ${session.correct} 次，错误 ${session.errors} 次" }, horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        PracticeCount(R.drawable.stat_correct, session.correct)
                        PracticeCount(R.drawable.stat_error, session.errors)
                    }
                    Text(if (session.sentenceMode) "完成 ${session.correct} 句 · 跳过 ${session.skipped} 句 · 错误 ${session.errors} 次" else "已完成 ${session.index} / ${session.words.size} · 跳过 ${session.skipped}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("本局用时 ${formatElapsed(timer.elapsedMillis())}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = { exit() }, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) { Text("返回首页") }
                    Button(onClick = { start(session.words, session.sentenceMode, session.articleTitle) }, modifier = Modifier.weight(1f).heightIn(min = 56.dp)) { Text("再次练习") }
                }
            }
        }
    }
    if (confirmExit) AlertDialog(onDismissRequest = { confirmExit = false }, title = { Text("退出本次练习？") }, text = { Text("结束本次练习并查看已完成作答的成绩。") },
        confirmButton = { TextButton(onClick = { confirmExit = false; timer.pause(); focusManager.clearFocus(); keyboard?.hide(); session = session.finish(); repository.submit(session.kana); clearInput() }) { Text("确认退出") } }, dismissButton = { TextButton(onClick = { confirmExit = false }) { Text("继续练习") } })
}
@Composable
internal fun PageBody(modifier: Modifier = Modifier, insets: Boolean = true, compact: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxSize()) {
        Column(Modifier.then(if (insets) Modifier.safeDrawingPadding() else Modifier).imePadding().fillMaxSize().widthIn(max = 560.dp).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = if (compact) 4.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 16.dp), content = content)
    }
}
@Composable
private fun PracticeCount(icon: Int, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(painterResource(icon), null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(count.toString(), style = MaterialTheme.typography.titleMedium)
    }
}
@Composable
private fun TimerLabel(timer: SessionTimer) {
    var seconds by remember(timer) { mutableLongStateOf(timer.elapsedMillis() / 1000) }
    // Only this small label observes a ticking value; input and scoring never do.
    LaunchedEffect(timer) { while (true) { seconds = timer.elapsedMillis() / 1000; delay(250) } }
    Row(Modifier.testTag("timer").clearAndSetSemantics { contentDescription = "本局用时 ${formatElapsed(seconds * 1000)}" },
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(painterResource(R.drawable.stat_timer), null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(formatElapsed(seconds * 1000), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
    }
}

