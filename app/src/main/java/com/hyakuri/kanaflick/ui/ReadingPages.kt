package com.hyakuri.kanaflick.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.hyakuri.kanaflick.reading.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun SentenceHome(active: Boolean, onStart: (ReadingArticle) -> Unit) {
    val context = LocalContext.current
    var articles by remember { mutableStateOf<List<ReadingArticle>>(emptyList()) }
    var error by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }
    var level by rememberSaveable { mutableStateOf("N5") }
    var selected by rememberSaveable { mutableStateOf("") }
    var showingList by rememberSaveable { mutableStateOf(false) }
    var detail by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        try { articles = withContext(Dispatchers.IO) { ReadingLibrary.load { context.assets.open(it) } } }
        catch (e: Exception) { error = "阅读库加载失败：${e.message}" }
        finally { loaded = true }
    }
    BackHandler(active && (showingList || detail != null)) { if (detail != null) detail = null else showingList = false }
    val filtered = remember(articles, level) { articles.filter { it.level == level } }
    val article = filtered.find { it.id == selected } ?: filtered.firstOrNull()
    if (!loaded || error.isNotEmpty()) {
        Text(if (!loaded) "正在读取阅读库…" else error,
            color = if (error.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
    } else if (detail != null) {
        articles.find { it.id == detail }?.let { a ->
            ReadingDetail(a, { detail = null }, { onStart(a) })
        }
    } else if (showingList) {
        // Fixed viewport inside the home scroll container; cards are composed on demand.
        LazyColumn(Modifier.fillMaxWidth().heightIn(min = 280.dp, max = 540.dp).testTag("reading-list"),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { TextButton(onClick = { showingList = false }) { Text("返回句子练习") } }
            item { Text("所有阅读", style = MaterialTheme.typography.titleLarge) }
            item { ReadingLevels(level) { level = it } }
            if (filtered.isEmpty()) item { Text("$level 暂无已核验篇目，请选择其他等级。") }
            items(filtered, key = { it.id }, contentType = { "reading" }) { a ->
                OutlinedCard(onClick = { detail = a.id }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReadingSummary(a)
                        Text(a.fullText.take(80), maxLines = 3, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
            }
        }
    } else {
        Text("整篇逐句练习 · 输入假名", style = MaterialTheme.typography.titleMedium)
        Text("参考分级，不是官方 JLPT 题库。标点与排版空白不参与判断。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        ReadingLevels(level) { level = it }
        if (!loaded) Text("正在读取阅读库…")
        if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
        filtered.forEach { a ->
            OutlinedButton(onClick = { selected = a.id }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text("${if (article?.id == a.id) "✓ " else ""}${a.title}")
            }
        }
        if (loaded && filtered.isEmpty() && error.isEmpty()) Text("$level 暂无已核验篇目，请选择其他等级。")
        article?.let { a ->
            ReadingSummary(a)
            Button(onClick = { onStart(a) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("start-sentence")) { Text("开始练习") }
        }
        OutlinedButton(onClick = { showingList = true }, modifier = Modifier.heightIn(min = 56.dp)) { Text("查看所有阅读") }
    }
}

@Composable private fun ReadingLevels(level: String, change: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ReadingLibrary.levels.forEach { value -> FilterChip(selected = level == value, onClick = { change(value) },
            label = { Text(value) }, modifier = Modifier.heightIn(min = 48.dp).testTag("reading-$value")) }
    }
}

@Composable private fun ReadingSummary(a: ReadingArticle) {
    Text(a.title, style = MaterialTheme.typography.titleLarge)
    Text("参考 ${a.level} · ${a.topic} · ${a.sentences.size} 句 · 原文 ${a.characterCount} 字", color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable private fun ReadingDetail(a: ReadingArticle, back: () -> Unit, start: () -> Unit) {
    var kana by rememberSaveable(a.id) { mutableStateOf(false) }
    val uri = LocalUriHandler.current
    TextButton(onClick = back, modifier = Modifier.heightIn(min = 48.dp)) { Text("返回阅读列表") }
    ReadingSummary(a)
    Text(a.adaptation, style = MaterialTheme.typography.bodySmall)
    Text(a.fullText, Modifier.testTag("reading-original"), style = MaterialTheme.typography.bodyLarge)
    TextButton(onClick = { kana = !kana }, modifier = Modifier.heightIn(min = 48.dp)) { Text(if (kana) "收起假名" else "展开假名") }
    if (kana) Text(a.sentences.joinToString("\n") { it.kana })
    Text("作者：${a.author}\n${a.license}\n分级依据：${a.grading}")
    TextButton(onClick = { uri.openUri(a.sourceUrl) }) { Text("来源：${a.sourceUrl}") }
    TextButton(onClick = { uri.openUri(a.licenseUrl) }) { Text("许可：${a.licenseUrl}") }
    Button(onClick = start, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("练习这篇") }
}
