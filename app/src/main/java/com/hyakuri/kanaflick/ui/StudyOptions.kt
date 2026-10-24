package com.hyakuri.kanaflick.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.hyakuri.kanaflick.R
import androidx.compose.ui.Alignment
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.hyakuri.kanaflick.vocabulary.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun LearningPage(builtIns: List<Vocabulary>, custom: List<Vocabulary>, loaded: Boolean, error: String,
                          roundSize: Int, onStart: (List<Word>) -> Unit,
                          onProficiency: () -> Unit = {}, selectWords: (List<Word>) -> List<Word> = { VocabularyStudy.select(it, roundSize) },
                          recommendations: Map<String, String> = emptyMap()) {
    var selected by rememberSaveable { mutableStateOf("jlpt-N5") }
    var page by rememberSaveable { mutableIntStateOf(0) }
    val libraries = remember(builtIns, custom) { builtIns + custom }
    val library = libraries.find { it.id == selected } ?: libraries.firstOrNull()
    val words = remember(library) { PracticeVocabulary.filter(library?.words.orEmpty()) }
    val lastPage = ((words.size - 1).coerceAtLeast(0)) / 30
    val visiblePage = page.coerceAtMost(lastPage)
    val pageWords = remember(words, visiblePage) {
        val start = visiblePage * 30
        words.subList(start.coerceAtMost(words.size), (start + 30).coerceAtMost(words.size))
    }
    Surface(Modifier.fillMaxSize()) {
    LazyColumn(Modifier.fillMaxSize().imePadding(), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item(key = "header", contentType = "header") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("学习词库", style = MaterialTheme.typography.headlineLarge)
        OutlinedButton(onClick = onProficiency, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("假名输入熟练度") }
        if (recommendations[library?.id].orEmpty().isNotEmpty()) Text(recommendations[library?.id].orEmpty())
        Text("查阅原文、读音与释义；含标点或特殊符号的词条不展示、不出题（保留ー）。", color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (!loaded) Text(error.ifEmpty { "正在读取词库…" })
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            libraries.forEach { item ->
                FilterChip(selected = library?.id == item.id, onClick = { selected = item.id; page = 0 },
                    label = { Text(if (item.custom) item.name else item.words.first().level) }, modifier = Modifier.heightIn(min = 48.dp))
            }
        }
        Text("${library?.name.orEmpty()} · ${words.size} / ${library?.words?.size ?: 0} 词", Modifier.testTag("study-count"))
        Button(onClick = { onStart(selectWords(words)) }, enabled = loaded && words.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("练习这些词") }
        if (words.isEmpty() && loaded) Text("词库为空，请选择其他词库。")
        }
        }
        if (words.isNotEmpty()) {
            item(key = "pager-top", contentType = "pager") { StudyPager(visiblePage, lastPage) { page = it } }
            items(pageWords, key = { it.id }, contentType = { "word" }) { word ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(word.text, style = MaterialTheme.typography.headlineSmall)
                        Text(word.reading, color = MaterialTheme.colorScheme.primary)
                        Text(word.meaning)
                    }
                }
            }
            item(key = "pager-bottom", contentType = "pager") { StudyPager(visiblePage, lastPage) { page = it } }
        }
        item(key = "source", contentType = "source") {
            Text("来源：${library?.source.orEmpty()}", style = MaterialTheme.typography.bodySmall)
        }
    }
    }
}

@Composable
private fun StudyPager(page: Int, last: Int, change: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = { change(page - 1) }, enabled = page > 0) { Text("上一页") }
        Text("${page + 1} / ${last + 1}", Modifier.padding(top = 12.dp))
        TextButton(onClick = { change(page + 1) }, enabled = page < last) { Text("下一页") }
    }
}

@Composable
internal fun OptionsPage(roundSize: Int, onRoundSize: (Int) -> Unit, targeted: Boolean = false, onTargeted: (Boolean) -> Unit = {}) {
    val context = LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    var document by rememberSaveable { mutableStateOf("") }
    var documentText by remember { mutableStateOf("") }
    var info by rememberSaveable { mutableStateOf("") }
    val version = remember {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }
    LaunchedEffect(document) {
        documentText = ""
        if (document.isNotEmpty()) documentText = try {
            withContext(Dispatchers.IO) { context.assets.open(if (document.startsWith("reading/")) document else "vocabulary/$document").bufferedReader().use { it.readText() } }
        } catch (e: Exception) { "无法读取说明：${e.message}" }
    }
    LazyColumn(Modifier.fillMaxSize().testTag("settings-list"), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item(key = "title", contentType = "heading") {
            Text("设置", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(vertical = 8.dp))
        }
        item(key = "practice", contentType = "section") {
            SettingsGroup("练习") {
                Row(Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(painterResource(R.drawable.settings_target), null, Modifier.size(24.dp))
                    Text("根据薄弱假名安排词汇", Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = targeted, onCheckedChange = onTargeted, modifier = Modifier.testTag("targeted-kana")
                        .semantics { contentDescription = "根据薄弱假名安排词汇" })
                }
                SettingsDivider()
                Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(painterResource(R.drawable.nav_typing), null, Modifier.size(24.dp))
                        Text("每局词数", Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        Text("$roundSize 词", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VocabularyStudy.roundSizes.forEach { count ->
                            FilterChip(selected = roundSize == count, onClick = { onRoundSize(count) }, label = { Text("$count 词") },
                                modifier = Modifier.heightIn(min = 48.dp).testTag("round-$count"))
                        }
                    }
                }
            }
            Text("用于下一局单词练习。针对性出题默认关闭；关闭后仍积累熟练度，样本不足时随机出题。部分汉字词条只参与推荐，句子练习可继续积累。",
                Modifier.padding(horizontal = 12.dp, vertical = 12.dp), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item(key = "help", contentType = "section") {
            SettingsGroup("帮助") {
                SettingsRow("使用帮助", "练习与输入法", R.drawable.settings_help) { info = "help" }
            }
        }
        item(key = "about", contentType = "section") {
            SettingsGroup("关于") {
                SettingsRow("关于 KanaFlick", "版本 $version", R.drawable.settings_info) { info = "about" }
                SettingsDivider()
                SettingsRow("查看来源与处理说明", "词库来源", R.drawable.settings_source) { document = "NOTICE.txt" }
                SettingsDivider()
                SettingsRow("查看许可证全文", "CC BY-NC 4.0", R.drawable.settings_license) { document = "LICENSE-CC-BY-NC-4.0.txt" }
                SettingsDivider()
                SettingsRow("阅读来源与许可", "署名与改编说明", R.drawable.nav_learning) { document = "reading/NOTICE.txt" }
            }
        }
    }
    if (info.isNotEmpty()) AlertDialog(onDismissRequest = { info = "" },
        title = { Text(if (info == "about") "关于 KanaFlick" else "使用帮助") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (info == "about") {
                    AppLogo(80.dp)
                    Text("KanaFlick")
                    Text("版本 $version")
                    Text("单词练习、句子练习与离线分级阅读。")
                    Surface(onClick = { uriHandler.openUri("https://github.com/S-Hyakuri/KanaFlick") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("about-repository")
                            .semantics { role = Role.Button },
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(painterResource(R.drawable.settings_source), null, Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary)
                                Text("S-Hyakuri / KanaFlick", Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface)
                                Icon(painterResource(R.drawable.settings_open_external), null, Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            androidx.compose.foundation.text.selection.SelectionContainer {
                                Text("https://github.com/S-Hyakuri/KanaFlick",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    HelpSection("开始练习", "在打字页选择词库并开始，或在学习页按等级浏览后选择“练习这些词”。输入框会自动聚焦并打开系统键盘。逐字输入原文或完整假名读音，例如“学校”和“がっこう”均可答对，无须选择候选。")
                    HelpSection("句子练习与阅读", "在品牌下切换到句子练习，选择参考等级及篇目。查看所有阅读可查看全文、假名、来源和改编说明。整篇从第一句开始，逐字输入假名或对应原文；忽略标点及排版空白，保留助词は／へ／を与长音符ー。错误保留输入，反馈标出首个不匹配位置。跳过只计跳过句数。完成句数不含跳过；正确率按完成句数÷（完成句数+错误次数）计算。词数设置只影响单词练习。")
                    HelpSection("切换输入法", "先在手机系统设置中启用需要的日语输入法，再通过系统键盘切换入口选择。入口位置因手机和输入法而异，也可到系统设置中的语言与输入法更换。KanaFlick 不绑定或内置键盘。")
                    HelpSection("自动判题", "每次文本变化立即判断，包括输入法正在组合的假名，不等待候选确认或 150ms。正确前缀显示逐字进度，完整匹配原文或完整假名后推进；错误保留。浊点、小假名和罗马字转换的末尾中间态暂不计错。同一个未修改的错误位置不会因继续输入后文而重复计错。忽略标点与符号，保留长音符「ー」。答案框下可跳过当前词，跳过不增加正确或错误次数。键盘完成键不提交答案。")
                    HelpSection("本局用时与成绩", "页面就绪开始计时，切到后台暂停，回到前台继续，完成时停止。屏幕重建保留本局，退出进入本局结算，返回首页或重开清零。正确率 = 正确次数 ÷（正确次数 + 错误次数），未作答为 0%。重开使用同一组词。")
                    HelpSection("没有判题？", "检查是否仍为未完成前缀、浊点／小假名或罗马字转换中间态。支持词条提供的原文和完整假名，不推测任意汉字假名混写。空格、大小写和全半角仍可能不同。")
                    HelpSection("键盘收起了？", "点一下答案框即可再次打开。主动收起后应用不会强制弹回。应用请求关闭候选建议，但是否隐藏候选栏由系统输入法决定；仍可直接输入假名完成。布局与底部手势区域由手机系统管理。")
                    HelpSection("词数不够？", "每局按选项中的 5、10 或 20 词随机抽取；所选词库不足时按实际数量练习。")
                }
            }
        },
        confirmButton = { TextButton(onClick = { info = "" }) { Text("关闭") } })
    if (document.isNotEmpty()) AlertDialog(onDismissRequest = { document = "" }, title = { Text(if (document.startsWith("reading/")) "阅读来源与许可" else "词库来源与许可") },
        text = { Text(documentText.ifEmpty { "正在读取…" }, Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) },
        confirmButton = { TextButton(onClick = { document = "" }) { Text("关闭") } })
}

@Composable
private fun SettingsRow(title: String, subtitle: String, icon: Int, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).heightIn(min = 76.dp).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(painterResource(icon), null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(painterResource(R.drawable.settings_chevron), null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable
private fun HelpSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Text(body, style = MaterialTheme.typography.bodyMedium)
    }
}


@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, Modifier.padding(horizontal = 12.dp), style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
}
