package com.hyakuri.kanaflick.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.hyakuri.kanaflick.proficiency.*

@Composable
internal fun ProficiencyPage(active: Boolean = true, onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { ProfileRepository.get(context) }
    val view by repository.state.collectAsState()
    var detail by rememberSaveable { mutableStateOf("") }
    var reset by rememberSaveable { mutableStateOf(false) }
    BackHandler(active) { onBack() }
    BoxWithConstraints(Modifier.fillMaxSize()) {
    val chartWidth = maxOf(maxWidth, 56.dp * LocalDensity.current.fontScale * 5 + 56.dp)
    Box(Modifier.fillMaxSize().horizontalScroll(rememberScrollState())) {
    LazyColumn(Modifier.width(chartWidth).fillMaxHeight().testTag("proficiency-list"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text("返回学习") }
            Text("假名输入熟练度", style = MaterialTheme.typography.headlineLarge)
        }
        item {
            Text("反映逐字输入中的可观察表现，不代表按键速度或日语能力。平假名与对应片假名合并；拗音作为完整单元。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!view.ready) Text("正在读取熟练度…")
            else if (view.profile.stats.isEmpty() && !view.unavailable) Text("完成可统计的假名练习后，这里会逐步显示你的表现。")
            if (view.message.isNotEmpty()) {
                Text(view.message, color = MaterialTheme.colorScheme.error)
                if (!view.unavailable || view.pending) TextButton(onClick = { repository.retry() }) { Text("重试保存") }
            }
        }
        if (view.ready && !view.unavailable) KanaChart.groups.forEach { (group, rows) ->
            item(key = group, contentType = "group") { Text(group, style = MaterialTheme.typography.titleLarge) }
            rows.forEachIndexed { index, row ->
                item(key = "$group-$index", contentType = "kana-row") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row.forEach { unit ->
                            if (unit.isEmpty()) Spacer(Modifier.weight(1f)) else {
                                val stat = view.profile.stats[unit] ?: KanaStat()
                                OutlinedCard(onClick = { detail = unit }, modifier = Modifier.weight(1f).heightIn(min = 72.dp)
                                    .semantics(mergeDescendants = true) { contentDescription = "$unit，${stat.status}" },
                                    colors = CardDefaults.outlinedCardColors(containerColor = if (stat.weak) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainerLow)) {
                                    Column(Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
                                        Text(unit, style = MaterialTheme.typography.titleMedium)
                                        Text(stat.status, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Text("每题每单元最多一个样本；纠正后仍保留该题明确错误。组合中的完整假名也可完成。正确前缀及浊音／小假名转换中间态不计错；跳过后的未输入部分不统计。含汉字或不支持字符的目标整题不统计；汉字完成不虚构假名成功样本，歧义错误不猜测。")
            Text("部分词条无法用于假名统计，句子练习可继续积累。关闭针对性出题仍会积累画像。仅保存本机汇总，不保存完整输入文本；粘贴和候选结果不一定能区分。")
            Text("保存失败可重试；突然终止时未保存或未完成的题目可能不计入。数据不参与系统云备份或设备转移。")
            OutlinedButton(onClick = { reset = true }, enabled = view.ready, modifier = Modifier.heightIn(min = 56.dp).testTag("reset-proficiency")) { Text("重置假名熟练度") }
        }
    }
    }
    }
    if (detail.isNotEmpty() && active) {
        val stat = view.profile.stats[detail] ?: KanaStat()
        AlertDialog(onDismissRequest = { detail = "" }, title = { Text(detail) }, text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stat.status)
                Text("累计有效样本：${stat.samples}\n累计错误样本：${stat.errors}\n最近练习日期：${stat.date.ifEmpty { "无" }}")
                Text(if (stat.samples == 0L) "近期表现：暂无样本" else "近期错误倾向：${"%.3f".format(stat.recent)}")
                Text("不足 8 个样本仅积累；达到 8 个后，近期错误倾向 ≥0.30 为需加强，≥0.10 为巩固中，其余较稳定。")
                Text("首个样本取成功 0／错误 1；随后为旧值 ×0.85 + 新样本 ×0.15。它不是掌握百分比，也不是累计错误率；停用不会扣分。")
            }
        }, confirmButton = { TextButton(onClick = { detail = "" }) { Text("关闭") } })
    }
    if (reset && active) AlertDialog(onDismissRequest = { reset = false }, title = { Text("重置假名熟练度？") },
        text = { Text("清除假名熟练度，之后重新积累；词库和练习配置不受影响。针对性出题开关保留。") },
        confirmButton = { TextButton(onClick = { reset = false; detail = ""; repository.reset() }) { Text("确认重置") } },
        dismissButton = { TextButton(onClick = { reset = false }) { Text("取消") } })
}
