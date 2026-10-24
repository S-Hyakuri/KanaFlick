package com.hyakuri.kanaflick.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hyakuri.kanaflick.vocabulary.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import com.hyakuri.kanaflick.proficiency.*

@Composable
internal fun LibraryHome(tab: Int, roundSize: Int, onRoundSize: (Int) -> Unit, onStart: (List<Word>) -> Unit, onReady: () -> Unit = {}, onSentenceStart: (com.hyakuri.kanaflick.reading.ReadingArticle) -> Unit = {}, targeted: Boolean = false, onTargeted: (Boolean) -> Unit = {}) {
    val context = LocalContext.current
    val repository = remember { ProfileRepository.get(context) }
    val profile by repository.state.collectAsState()
    val stats = if (profile.ready && !profile.unavailable) profile.profile.stats else emptyMap()
    var proficiency by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val store = remember { VocabularyStore(context.noBackupFilesDir) }
    var builtIns by remember { mutableStateOf<List<Vocabulary>>(emptyList()) }
    var custom by remember { mutableStateOf<List<Vocabulary>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    fun startWords(words: List<Word>) {
        if (busy) return
        busy = true
        val snapshot = stats
        val enabled = targeted
        val requestedSize = roundSize
        val previous = repository.previousIds
        scope.launch {
            try {
                val chosen = withContext(Dispatchers.Default) {
                    if (enabled) TargetedVocabulary.select(words, requestedSize, snapshot, previous)
                    else VocabularyStudy.select(words, requestedSize)
                }
                onStart(chosen)
            } finally { busy = false }
        }
    }
    var selected by rememberSaveable { mutableStateOf("jlpt-N5") }
    var sentenceMode by rememberSaveable { mutableStateOf(false) }
    val modeState = rememberSaveableStateHolder()
    var managing by rememberSaveable { mutableStateOf(false) }
    var page by rememberSaveable { mutableIntStateOf(0) }
    var nameAction by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var deleteId by rememberSaveable { mutableStateOf<String?>(null) }
    var editorId by rememberSaveable { mutableStateOf<String?>(null) }
    var importTarget by rememberSaveable { mutableStateOf<String?>(null) }
    var exportTarget by rememberSaveable { mutableStateOf<String?>(null) }
    var preview by remember { mutableStateOf<CsvPreview?>(null) }
    var showFormat by rememberSaveable { mutableStateOf(false) }
    val reportReady by rememberUpdatedState(onReady)
    LaunchedEffect(Unit) {
        try {
            val data = withContext(Dispatchers.IO) { BuiltInVocabulary.load { context.assets.open(it) } to store.load() }
            builtIns = data.first; custom = data.second; loaded = true
        } catch (e: Exception) { message = "读取失败，已禁止修改以保护原词库：${e.message}" }
        finally { reportReady() }
    }
    fun persist(next: List<Vocabulary>, after: () -> Unit = {}) {
        if (!loaded || busy) return
        busy = true
        scope.launch {
            try { withContext(Dispatchers.IO) { store.save(next) }; custom = next; message = "已保存到本机"; after() }
            catch (e: Exception) { message = "保存失败，原词库未更改：${e.message}" }
            finally { busy = false }
        }
    }
    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null && loaded && !busy) {
            busy = true
            val target = custom.find { it.id == importTarget }
            scope.launch {
                try {
                    requireNotNull(target) { "目标词库不存在" }
                    preview = withContext(Dispatchers.IO) {
                        val bytes = requireNotNull(context.contentResolver.openInputStream(uri)).use { stream ->
                            val output = java.io.ByteArrayOutputStream()
                            val buffer = ByteArray(8192)
                            while (true) {
                                val read = stream.read(buffer); if (read < 0) break
                                require(output.size() + read <= VocabularyCsv.MAX_BYTES) { "CSV 超过 2 MiB 上限" }
                                output.write(buffer, 0, read)
                            }
                            output.toByteArray()
                        }
                        VocabularyCsv.preview(VocabularyCsv.decode(bytes), target.words)
                    }
                } catch (e: Exception) { message = "导入失败，原词库未更改：${e.message}" }
                finally { busy = false }
            }
        }
    }
    val exportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null && loaded && !busy) {
            busy = true
            val target = custom.find { it.id == exportTarget }
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val text = if (exportTarget == "__example__") VocabularyCsv.export(Samples.words)
                            else VocabularyCsv.export(requireNotNull(target) { "待导出的词库不存在" }.words)
                        requireNotNull(context.contentResolver.openOutputStream(uri, "wt")).use { it.write(text.toByteArray(Charsets.UTF_8)) }
                    }
                    message = "CSV 已导出"
                } catch (e: Exception) { message = "导出失败：${e.message}；目标文件可能不完整，请重新导出" }
                finally { busy = false }
            }
        }
    }
    val rawLibraries = remember(builtIns, custom) { builtIns + custom }
    val practiceLibraries = remember(rawLibraries) { rawLibraries.map { it.copy(words = PracticeVocabulary.filter(it.words)) } }
    val libraries = if (managing) rawLibraries else practiceLibraries
    val recommendations by produceState<Map<String, String>>(emptyMap(), targeted, practiceLibraries, stats) {
        value = emptyMap()
        if (targeted) value = withContext(Dispatchers.Default) {
            practiceLibraries.associate { it.id to TargetedVocabulary.message(it.words, stats) }
        }
    }
    val library = libraries.find { it.id == selected } ?: libraries.firstOrNull()
    BackHandler(managing && tab == 0 && !busy) { managing = false }
    val tabState = rememberSaveableStateHolder()
    RetainedTabs(tab) { visibleTab ->
    tabState.SaveableStateProvider(visibleTab) {
    if (visibleTab == 1) {
        if (proficiency) ProficiencyPage(active = tab == 1) { proficiency = false }
        else LearningPage(builtIns, custom, loaded && !busy, message, roundSize, ::startWords,
            onProficiency = { proficiency = true }, selectWords = { it },
            recommendations = recommendations)
    } else if (visibleTab == 2) {
        OptionsPage(roundSize, onRoundSize, targeted, onTargeted)
    } else PageBody(insets = false) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (!managing) HomeBrandIcon()
            if (managing) Text("自定义词库", style = MaterialTheme.typography.headlineLarge)
            else BrandWordmark(Modifier.offset(y = (-11).dp))
        }
        if (!managing) SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            listOf("单词练习", "句子练习").forEachIndexed { i, label ->
                SegmentedButton(selected = sentenceMode == (i == 1), onClick = { sentenceMode = i == 1 },
                    shape = SegmentedButtonDefaults.itemShape(i, 2), modifier = Modifier.heightIn(min = 48.dp)) { Text(label) }
            }
        }
        if (sentenceMode && !managing) {
            modeState.SaveableStateProvider("sentences") { SentenceHome(tab == 0, onSentenceStart) }
        } else {
        if (message.isNotEmpty()) Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (!loaded) Text(if (message.isEmpty()) "正在读取词库…" else "请保留本机文件，修复后重启应用")
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (!managing) {
            Text("日语打字练习 · 每局最多 $roundSize 词，随机抽取")
            Text("逐字输入原文或完整假名，无须选择候选。", style = MaterialTheme.typography.bodyLarge)
            Text("JLPT 参考分级 · 中文释义 · 已排除含符号词条", color = MaterialTheme.colorScheme.onSurfaceVariant)
            libraries.forEach { item ->
                OutlinedButton(onClick = { selected = item.id }, enabled = !busy, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("${if (library?.id == item.id) "✓ " else ""}${item.name} · ${item.words.size} 词")
                }
            }
            library?.let { lib ->
                Text("来源：${lib.source}")
                if (targeted && recommendations[lib.id].orEmpty().isNotEmpty()) Text(recommendations[lib.id].orEmpty())
                if (profile.message.isNotEmpty()) Text(profile.message, color = MaterialTheme.colorScheme.error)
                Button(onClick = { startWords(lib.words) },
                    enabled = loaded && !busy && lib.words.isNotEmpty(), modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("开始练习") }
                if (lib.words.isEmpty()) Text("词库为空，请先添加或导入词条。")
            }
            OutlinedButton(onClick = { managing = true }, enabled = loaded && !busy, modifier = Modifier.heightIn(min = 56.dp)) { Text("管理自定义词库") }
        } else {
            TextButton(onClick = { managing = false }, enabled = !busy) { Text("返回词库选择") }
            Button(onClick = { name = ""; nameAction = "new" }, enabled = !busy) { Text("新建词库") }
            custom.forEach { item ->
                OutlinedButton(onClick = { selected = item.id; page = 0 }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                    Text("${if (library?.id == item.id) "✓ " else ""}${item.name} · ${item.words.size} 词")
                }
            }
            if (library?.custom == true) {
                Text(library.name, style = MaterialTheme.typography.titleLarge)
                Text("来源：${library.source}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { name = library.name; nameAction = library.id }, enabled = !busy) { Text("重命名") }
                    TextButton(onClick = { deleteId = library.id }, enabled = !busy) { Text("删除词库") }
                }
                Button(onClick = { editorId = "" }, enabled = !busy) { Text("添加词条") }
                OutlinedButton(onClick = { importTarget = library.id; importPicker.launch(arrayOf("text/*", "application/csv", "application/octet-stream")) }, enabled = !busy) { Text("导入 UTF-8 CSV") }
                OutlinedButton(onClick = { exportTarget = library.id; exportPicker.launch("KanaFlick-${library.id}.csv") }, enabled = !busy) { Text("导出词库") }
                val maxPage = ((library.words.size - 1).coerceAtLeast(0)) / 30
                val visiblePage = page.coerceAtMost(maxPage)
                Text("第 ${visiblePage + 1} / ${maxPage + 1} 页 · 共 ${library.words.size} 词")
                library.words.drop(visiblePage * 30).take(30).forEach { w ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(w.text, style = MaterialTheme.typography.titleLarge)
                            Text("${w.reading} · ${w.meaning} · ${w.level}")
                            Row {
                                TextButton(onClick = { editorId = w.id }, enabled = !busy) { Text("编辑 ${w.text}") }
                                TextButton(onClick = { persist(custom.map { if (it.id == library.id) it.copy(words = it.words.filterNot { word -> word.id == w.id }) else it }) }, enabled = !busy) { Text("删除 ${w.text}") }
                            }
                        }
                    }
                }
                Row {
                    TextButton(onClick = { page = visiblePage - 1 }, enabled = !busy && visiblePage > 0) { Text("上一页") }
                    TextButton(onClick = { page = visiblePage + 1 }, enabled = !busy && visiblePage < maxPage) { Text("下一页") }
                }
            } else Text("新建或选择一个自定义词库后可编辑、导入和导出。")
            TextButton(onClick = { showFormat = true }) { Text("CSV 格式说明") }
            TextButton(onClick = { exportTarget = "__example__"; exportPicker.launch("KanaFlick-example.csv") }, enabled = !busy) { Text("保存 CSV 示例") }
        }
    }
    }
    }
    }
    if (nameAction.isNotEmpty()) AlertDialog(onDismissRequest = { if (!busy) nameAction = "" }, title = { Text(if (nameAction == "new") "新建词库" else "重命名词库") },
        text = { OutlinedTextField(name, { name = it }, label = { Text("词库名称（1–80 字符）") }, singleLine = true) },
        confirmButton = { TextButton(enabled = !busy && name.isNotBlank() && name.length <= 80, onClick = {
            val id = if (nameAction == "new") UUID.randomUUID().toString() else nameAction
            val next = if (nameAction == "new") custom + Vocabulary(id, name.trim(), "用户自行添加或导入；等级与释义由用户提供", emptyList(), true)
                else custom.map { if (it.id == id) it.copy(name = name.trim()) else it }
            persist(next) { selected = id; nameAction = ""; page = 0 }
        }) { Text("保存") } }, dismissButton = { TextButton(onClick = { nameAction = "" }, enabled = !busy) { Text("取消") } })
    if (deleteId != null) AlertDialog(onDismissRequest = { if (!busy) deleteId = null }, title = { Text("删除整个词库？") },
        text = { Text("将永久删除“${custom.find { it.id == deleteId }?.name}”及全部词条。可先导出备份。") },
        confirmButton = { TextButton(onClick = { persist(custom.filterNot { it.id == deleteId }) { selected = "jlpt-N5"; deleteId = null } }, enabled = !busy) { Text("确认删除") } },
        dismissButton = { TextButton(onClick = { deleteId = null }, enabled = !busy) { Text("取消") } })
    if (editorId != null && library?.custom == true) key(library.id, editorId) {
        WordEditor(library.words.find { it.id == editorId }, busy, { editorId = null }) { word ->
            val other = library.words.filterNot { it.id == word.id }
            if (other.any { WordRules.key(it) == WordRules.key(word) }) "原文与读音重复，请编辑已有词条"
            else { persist(custom.map { if (it.id == library.id) it.copy(words = if (editorId == "") it.words + word else it.words.map { old -> if (old.id == word.id) word else old }) else it }) { editorId = null }; null }
        }
    }
    preview?.let { result ->
        AlertDialog(onDismissRequest = { if (!busy) preview = null }, title = { Text("导入预览") }, text = {
            Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                Text("目标：${custom.find { it.id == importTarget }?.name}\n有效新增 ${result.words.size} 条\n重复 ${result.duplicates.size} 条（跳过）\n错误 ${result.errors.size} 行")
                Text("重复行：${result.duplicates.joinToString().ifEmpty { "无" }}")
                result.errors.forEach { Text(it, color = MaterialTheme.colorScheme.error) }
                result.words.take(5).forEach { Text("${it.text} · ${it.reading} · ${it.meaning} · ${it.level}") }
                if (result.errors.isNotEmpty()) Text("存在错误，整批不导入。请修正文件后重试。")
            }
        }, confirmButton = { TextButton(enabled = result.canImport && !busy, onClick = {
            val target = custom.find { it.id == importTarget }
            if (target == null) { message = "目标词库不存在"; preview = null }
            else persist(custom.map { if (it.id == target.id) it.copy(words = it.words + result.words) else it }) { preview = null }
        }) { Text("确认导入") } }, dismissButton = { TextButton(onClick = { preview = null }, enabled = !busy) { Text("取消") } })
    }
    if (showFormat) AlertDialog(onDismissRequest = { showFormat = false }, title = { Text("UTF-8 CSV 格式") }, text = {
        Text("表头：\n${VocabularyCsv.HEADER}\n\n每行：唯一 id、日语原文、假名读音、中文释义、等级（N5–N1 或 未分级）。支持 BOM、CRLF、带引号的逗号和换行，双引号写成两个双引号。\n\n最多 2 MiB、每库 10000 词；id 或原文+读音相同视为重复。预览后确认才追加，有错误整批取消，绝不覆盖旧词条。")
    }, confirmButton = { TextButton(onClick = { showFormat = false }) { Text("知道了") } })
}

@Composable
private fun WordEditor(original: Word?, busy: Boolean, dismiss: () -> Unit, save: (Word) -> String?) {
    val id by rememberSaveable { mutableStateOf(original?.id ?: UUID.randomUUID().toString()) }
    var text by rememberSaveable { mutableStateOf(original?.text ?: "") }
    var reading by rememberSaveable { mutableStateOf(original?.reading ?: "") }
    var meaning by rememberSaveable { mutableStateOf(original?.meaning ?: "") }
    var level by rememberSaveable { mutableStateOf(original?.level ?: "未分级") }
    var error by rememberSaveable { mutableStateOf("") }
    AlertDialog(onDismissRequest = { if (!busy) dismiss() }, title = { Text(if (original == null) "添加词条" else "编辑词条") }, text = {
        Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(text, { text = it }, label = { Text("日语原文") }, singleLine = true)
            OutlinedTextField(reading, { reading = it }, label = { Text("假名读音") }, singleLine = true)
            OutlinedTextField(meaning, { meaning = it }, label = { Text("中文释义") })
            OutlinedTextField(level, { level = it }, label = { Text("参考等级：N5–N1 或 未分级") }, singleLine = true)
            if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
        }
    }, confirmButton = { TextButton(enabled = !busy, onClick = {
        val w = Word(id,text,reading,meaning,level)
        error = WordRules.error(w) ?: save(w).orEmpty()
    }) { Text("保存词条") } }, dismissButton = { TextButton(onClick = dismiss, enabled = !busy) { Text("取消") } })
}

