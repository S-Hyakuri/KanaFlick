# 验收记录

当前开发版本 **0.9.4 / versionCode 22**；设备验收未完成，不代表全部交付。各版验收统一保存在本文件，原始日志、XML 与截图仍在 `docs/evidence/`。

v0.5.3 已执行单元测试、Lint、构建和启动专项设备测试；完整业务设备回归未在本版重跑。旧版通过状态仅来自各版既有记录。单元测试、模拟器自动测试、截图目检和真机操作互不替代，代码或 Preview 定义不代表通过。历史 APK 路径可能被新构建覆盖，应以版本及已有哈希辨认，不能用当前 `app-debug.apk` 证明旧版产物。

## v0.9.4 — 2026-10-24（仓库信息修正）

### 已实现

用户已创建 `https://github.com/S-Hyakuri/KanaFlick`；本轮通过 GitHub 连接确认仓库存在。用户截图明确显示第一条为个人主页、第二条为仓库；按最新要求删除第一条个人主页，只保留仓库入口，覆盖旧版保留个人主页的要求。仓库改为主题圆角描边卡片，仓库名与完整网址分层展示，右侧使用外部打开图标；整卡可点击交由系统浏览器打开，网址仍可选择复制。完整网址允许换行，不裁切、不省略，适应窄屏与大字。仍为同次交付的 **0.9.4 / 22**，不重复递增版本。

### 本地验证

删除个人主页并完成仓库卡片样式后重跑 `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest` 成功；**86 项单元测试、0 失败／0 错误；Lint 0 错误／18 警告**，Debug 与设备测试包构建通过。本轮为信息、链接与样式修正，未新增测试；已有单元回归已重跑。源码复核确认个人主页显示和点击入口均已移除，仓库仅有一个点击入口与一个完整网址文本；该复核不替代设备操作。前次构建日志与产物信息保留为 `initial-check.txt`／`initial-verification.json` 及 `before-repository-style-check.txt`／`before-repository-style-verification.json`，当前证据与 APK 哈希对应最终卡片修正后的产物。

证据在 `docs/evidence/v0.9.4/`：`check.txt`、`unit-tests/`、`lint-results.xml`、`verification.json`。Debug APK 为 **0.9.4 / 22**，13,213,554 字节，SHA-256 `ad6c35393fda395e023d77e4324366bf8fa36244de6070065cefd31faea13ff4`。首次受限运行因 Gradle 用户缓存无写权限中止，日志保留为 `wrapper-access.txt`；允许访问现有缓存后重跑成功。

### 设备与未验证

用户提供的 `reported-two-links.jpg` 证明修正前仍有个人主页和仓库两条链接，作为问题证据保留，不作为新卡片的验收截图。本轮 ADB 列表为空（`devices.txt`），未执行 connectedDebugAndroidTest；没有新卡片的运行截图或设备操作证据，小屏／大字体、深浅主题、TalkBack、链接点击与长按复制仍需设备验证。0.9.3 及更早版本的输入法、兼容性与性能缺口继续保留。

## v0.9.3 — 2026-10-24（页面性能与选项样式修复）

### 已实现

- 熟练度从整组一次组合改为五列逐行 LazyColumn，使用稳定行键和内容类型；大字体横向滚动统一作用于视窗。离屏行按需组合，不再在进入外来音组时一次排版全部单元。
- 三个主页仍常驻，切换保持 220ms。动画值只由图层读取，是否可见通过派生布尔值更新；隐藏页不测量、不放置。切换到其他标签不再把熟练度替换成学习列表，返回保留熟练度与滚动状态；隐藏熟练度不处理返回键。
- 合并词库与符号过滤结果按数据缓存；管理模式切换复用筛选结果。针对性出题提示按词库／画像变更在后台计算，避免组合时重复分词扫描。静态主页图标按尺寸缓存 3 倍像素图像，避免滚动重绘时重复描绘复杂路径；原字样、位置和启动动画参数不变。
- 选项页参考用户提供的 ChatGPT 手机端截图，改为中性圆角描边分组、24dp 线条图标、细分隔线、弱化的辅助文字和整行入口。保留练习开关、词数、帮助、关于及许可功能，采用 LazyColumn；保持动态配色／深浅主题，无账号、搜索或订阅功能新增。

### 本地验证（通过）

`:app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest` 成功；**86 项单元测试、0 失败／0 错误；Lint 0 错误／18 警告**。比上一版多 1 项 bitmap 创建的 UseKtx 建议，不是运行错误。新增 2 项设备测试检查熟练度与选项页的滚动位置跨标签保留，以及来源入口；本次只编译，未执行通过。

证据在 `docs/evidence/v0.9.3/`：`check.txt`、`unit-tests/`、`lint-results.xml`、`verification.json`。Debug APK 为 **0.9.3 / 21**，12,729,037 字节，SHA-256 `ff97aa574194e8a4780adcc6fe146cded654920a90854e50e7a928cd2b2ef85c`。未升级依赖，没有修改输入与计分规则。

### 设备与性能证据（未完成）

connectedDebugAndroidTest 返回 **No connected devices!**，ADB 列表为空，详见 `connected-check.txt` 与 `devices.txt`。没有新界面截图、真机滚动／切换帧耗时或性能前后对照，因此以上是代码层面减少工作量，不能据此宣称卡顿已消除或提升某百分比。两项新增设备测试及旧版待验项仍未通过。

后续在同一设备、刷新率、字体、构建类型和词库配置下对照旧／新版：连续切换三个标签、熟练度上下往返滚动、学习词库翻页与滚动、选项页滚动及打开许可，记录帧耗时与卡顿帧；另验 360dp／150% 字体、深浅主题、TalkBack 和滚动位置恢复。Debug 构建与优化发布构建的结果不可混作同一性能对照。

## v0.9.2 — 2026-10-24（修复产物，设备验收未完成）

### 已实现与回归覆盖

- 判题将平片假名作等价比较，原文显示不变；单词／句子、正确前缀、错误去重均使用同一规则。「コーヒー」可以输入「こーひー」，长音、小假名和浊点仍须正确，汉字答案规则不变。
- 大假名可转换为目标小假名时，无论是否有组合标记均暂缓；「きや」等待，「きやk」继续等待，「きやく」才记错，改为「きゃく」完成。覆盖ぁぃぅぇぉっゃゅょゎゕゖ；末尾大字不会被误当作正确小字完成。
- QWERTY 罗马字后缀（含全角字母）等待转换，组合标记缺失／结束不会产生错误；已有明确错误假名不会被后续字母掩盖。只处理新文本事件，纯组合标记变化不重新判题。未增加罗马字答案或内置输入法。
- 熟练度按五列五十音图排列，や／わ行留空，所有支持单元恰好出现一次；保留状态文字、详情与重置。窄屏／大字体可横向滚动，未缩小系统字号。

### 单元、Lint 与构建（通过）

执行 `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest`，**86 项测试、0 失败／0 错误**；Lint **0 错误／17 警告**；Debug 与设备测试包构建通过。新增 7 项回归测试覆盖平片等价与画像、原文显示、全部小假名转换、末尾大字不误完成、QWERTY 输入序列、先前错误不被字母掩盖、五列单元完整性；更新 2 个旧测试中的组合结束预期。

证据：`docs/evidence/v0.9.2/check.txt`、`unit-tests/`、`lint-results.xml`、`verification.json`。APK 为 0.9.2 / 20，12,725,215 字节，SHA-256 `b2a2e102d2d4783f93e1318a5d0789a363bf9ed4d6f8530319f83670e2acb5d8`。

### 设备、截图与未验证

`:app:connectedDebugAndroidTest` 返回 **No connected devices!**，ADB 列表为空，见 `connected-check.txt` 和 `devices.txt`。本版没有设备测试通过或截图验收证据。仍须在实际日语 12 键／QWERTY 输入法上验证转换、切题及恢复，并检查五列在小屏／大字体、深浅主题和 TalkBack 下的表现。性能、兼容性及历史版本缺口继续保留。用户旧画像不自动清除，可使用已有确认重置入口。

## v0.9.1 — 2026-10-23（用户指定版本，开发产物）

### 已实现行为

- 判题由真实 `onValueChange` 文本事件直接驱动，移除候选确认门槛和 150ms 防抖。逐字检查前缀，完整原文或可靠完整假名立即完成；句子也接受资源对应原文，不猜测任意混写。完成仍按词／句计数，不把每个正确字符当成一个完成题目。
- 明确错误立即计一次；同一最左错位及字符未改变时，继续输入后文不会重复罚分。回到正确前缀后再次犯错可计新错误。末尾浊点／小假名、罗马字转换中间态暂缓；结束组合时若仍是错误，才记录该中间态的最终错误。空输入／正确前缀／恢复文本／纯光标不新增错误。
- `DirectKanaInput` 使用现有 Compose 稳定依赖中的输入拦截 API，局部 opt-in `ExperimentalComposeUiApi`，没有升级依赖。请求 `TYPE_TEXT_FLAG_NO_SUGGESTIONS` 并关闭自动纠正／补全；不使用密码键盘。会话 ID＋题号作为连接标识，切题更新连接，旧连接的组合、提交、删除、光标及新系统重载拒绝写入下一题。仍只自动请求一次键盘。
- 依据 [Android InputType 文档](https://developer.android.com/reference/android/text/InputType#TYPE_TEXT_FLAG_NO_SUGGESTIONS) 及 [Compose KeyboardOptions 文档](https://developer.android.com/reference/kotlin/androidx/compose/foundation/text/KeyboardOptions)，候选建议标志是对 IME 的请求，不能保证所有键盘隐藏候选栏。参考 [AndroidX 输入拦截示例](https://android.googlesource.com/platform/frameworks/support/+/HEAD/compose/ui/ui/samples/src/main/java/androidx/compose/ui/samples/PlatformTextInputModifierNodeSample.kt) 包装输入连接。以上为 API／源码依据，不是设备效果验收。
- 快照增加错误去重键，支持旧无模式、0.8 与 0.9.0 布局；原画像文件和规则参数不迁移。画像只统计支持的纯假名目标，可观察的组合假名允许参与；汉字完成不生成虚构假名成功样本，既有明确错误仍结算。完整输入历史不保存。
- `PracticeVocabulary` 同时检查原文和读音的 Unicode 标点／符号，整词排除，保留长音符ー；释义中的标点不影响入选。普通抽样、针对性抽样、首页数量和学习浏览均应用筛选。自定义管理与导出不删除原数据；阅读文章不受词条过滤影响。

### 单元、Lint 与构建（通过）

最终执行 `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest`，**79 项单元测试，0 失败／0 错误**；Lint **0 错误／17 警告**。Debug 和设备测试包构建成功。证据：`docs/evidence/v0.9.1/check.txt`、`unit-tests/`、`lint-results.xml`、`verification.json`。

原“组合不评分”“假名不能替代汉字”测试按照本次用户授权更新，不再保留为现行要求。新增 10 项测试覆盖：组合假名逐字完成、两种完整答案／拒绝任意混写、即时错误与后文不重复惩罚、浊音／小假名中间态、罗马字转换、重复修订／旧题／恢复去重、汉字完成不伪造假名样本、两种选词过滤、旧 0.9.0 快照兼容、内置词条过滤审计。原画像持久化与分数／抽样测试继续通过。

设备测试已更新为“组合逐字完成、NO_SUGGESTIONS 标志、旧连接候选迟到、已评分文本恢复不双计”的新契约；同步句子前缀语义、帮助文案和错误截图用例。**设备测试包编译不代表这些流程已在设备通过。**

### 符号词条审计

| 等级 | 原始 | 可浏览／可练习 | 排除 |
| --- | ---: | ---: | ---: |
| N5 | 805 | 780 | 25 |
| N4 | 757 | 730 | 27 |
| N3 | 1,815 | 1,734 | 81 |
| N2 | 3,214 | 3,134 | 80 |
| N1 | 4,044 | 3,900 | 144 |
| 合计 | 10,635 | 10,278 | 357 |

见 `symbol-filter.tsv`。如 `〜する`、`A・B`、括号、斜杠备选读音及其他特殊符号会排除，`コーヒー` 保留。原始 CSV、来源和许可内容不改写，用户自定义原始词条仍可管理、修正和导出。

### 设备、截图与性能（未验证）

`:app:connectedDebugAndroidTest` 返回 **No connected devices!**，ADB 列表为空，见 `connected-check.txt` 和 `devices.txt`。本轮没有取得运行截图，也未完成实体手机／两种日语 IME 的候选栏表现、连续跨题、浊点／小假名、罗马字、删除／选区编辑、切换输入法、旋转／后台恢复、旧版覆盖安装、API 26／30／31+、浅深与大字体／TalkBack 验证。此前模拟器启动受阻，本轮未重复启动或清理 AVD。

逐字判定及连接切换是本版重点设备待验项；系统输入法忽略禁建议标志时仍可能显示候选栏。无同设备启动、输入帧耗时、内存和写盘实测，不宣称性能改善。0.8.0 与 0.9.0 的设备缺口继续保留，不能宣布全部验收通过。

### 开发安装包

`app/build/outputs/apk/debug/app-debug.apk`，**0.9.1 / 19**，12,708,831 字节，SHA256 `44acf9caddddd7c43504acfff62ca57d918bd95284cf86a60ad0acede403b7b0`。用户明确指定修复版本号，因此本次不按内容过滤自动升至 0.10.0。未推送或发布。

## v0.9.0 — 2026-10-23（开发产物，设备验收未完成）

### 实现与代码边界

- `proficiency/` 提供 NFC 与平片假名归一、显式拗音／外来音表、唯一最优对齐、每题每单元一次样本、样本阈值与近期倾向。只接收原判题实际接受的事件，不改变正确／错误／跳过计分。正确前缀、组合、恢复文本、旧题和重复修订不增加画像；非假名目标与无法唯一归因的错误不猜测。
- `PracticeSnapshot` 新增会话、世代、当前题错误位置和待结算单元；兼容旧快照。队列按题目结束顺序更新，汇总和当前会话游标一次原子落盘；重复恢复提交被忽略，新会话替换旧游标，不保存无限会话列表。
- 单一 IO 写队列、写失败保留待提交、重试幂等、未知版本／损坏保护；确认重置换世代、清待提交及上一局内存缓存，旧事件不可写回。新开局可排在待重置之后；写失败不会把内存统计显示为已保存。异常文件在显式确认重置前保留；重置恢复时留存 `kana-profile-unreadable.bin` 供排错。
- 画像文件为 `Context.getNoBackupFilesDir()/kana-profile-v1.bin`，沿用项目 fsync＋原子 rename 策略。核对现有备份 XML，未修改其他数据规则、未增加权限或依赖。[Android 官方备份文档](https://developer.android.com/identity/data/autobackup?hl=en) 说明 noBackup 目录排除云备份及设备转移；此处是源码与 API 核验，尚无实际备份／迁移实验。
- 学习页增加分组熟练度图与详情、空态、失败说明及确认重置；选项页增加默认关闭的开关。推荐在后台使用当前快照和候选集合，60% 名额针对性加权，余下普通探索；按稳定 ID 去重并优先避开本进程上一局。文章顺序不变。结果页再次练习继续保留同组复练，每次建立新统计会话；回首页从词库开始才重新抽取。

### 单元测试、Lint 与构建（通过）

执行 `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest`，最终 **69 项单元测试，0 失败／0 错误**；Lint **0 错误／17 警告**；应用和设备测试包均编译通过。原始证据：`docs/evidence/v0.9.0/check.txt`、`unit-tests/`、`lint-results.xml`、`verification.json`。

新增 23 项核心测试覆盖：NFC／平片假名／显式组合、非假名拒绝、唯一替换与内部删除、尾部未输入／多输入／歧义、组合与前缀／旧题／重复修订、纠正不双计、重复字符合并、跳过退出、0／7／8 样本及 0.10／0.30 边界、近期衰减、快照恢复、重复提交及世代隔离、文件往返／损坏／未知版本／原子写失败、队列失败重试／重置清空／旧会话迟到、选词范围与数量／去重／无效读音／长词权重／回退。固定 500 个随机种子比较，测试断言针对性样本中薄弱词出现次数超过普通基线 3 倍；不以单局截图证明概率。

开发过程中先发现会话随机 ID 影响旧模型等值测试，已将 ID 生成移至真实开局入口；旧快照测试改为实际旧字段布局，仍验证旧快照兼容。最终全部通过。另新增设备 UI 测试用于句子样本、旋转防双计、确认重置、开关恢复及截图；**仅编译通过，未运行**。

### 词库覆盖（已扫描资源）

| 词库 | 总词数 | 原文可统计 | 读音可推荐 |
| --- | ---: | ---: | ---: |
| N5 | 805 | 222 | 741 |
| N4 | 757 | 133 | 687 |
| N3 | 1,815 | 385 | 1,596 |
| N2 | 3,214 | 515 | 2,976 |
| N1 | 4,044 | 584 | 3,796 |
| 合计 | 10,635 | 1,839 | 9,796 |

见 `vocabulary-coverage.tsv`。这些数量是显式单元表的程序扫描结果，不表示日语词典核验或所有词条都可积累画像。包含汉字的词条可凭假名读音参与推荐，原文仍按原文判题；不改变为读音答案。

### 设备测试、截图与人工验收（未通过验收门槛）

最终运行 `:app:connectedDebugAndroidTest`，返回 **No connected devices!**；ADB 列表为空。现有 Pixel_10_Pro 使用无窗口、软件渲染、无快照方式启动，仍报 **too many emulator instances** 后退出。证据为 `connected-check.txt`、`devices.txt`、`emulator-output.txt` 和 `emulator-error.txt`。未清除 AVD 数据或修改系统显示配置。

没有取得本版运行截图。真实手机与两种日语 IME、组合／候选确认／切换／删除／跨句、后台／进程恢复、旧版本覆盖安装、API 26／30／31+、浅深色／两套动态色、360dp／150% 字体、TalkBack、故障注入与系统备份排除的设备验证均未完成。单元中的文件与队列测试不等于 Android 实机故障实验；0.8.0 未完成项继续保留。

### 包体与性能

当前 Debug APK：`app/build/outputs/apk/debug/app-debug.apk`，**0.9.0 / 18，12,968,683 字节**，SHA256 `f3df1761d310a31c9f7d73a117702e48bc32bc8b96526ff951550f41003013cc`。这是本地开发产物，未推送／发布。原 0.8.0 包体历史证据保留在下方，不把历史包与本版构建当作完整同配置性能实验。

未实测启动、帧耗时、峰值内存及写盘耗时，不宣称性能优化。抽样在 Default 调度器执行，写盘在单一 IO 队列执行；代码分线程不等于设备流畅度已通过。缺少设备截图和真实输入验证，**不能宣布 0.9.0 整版全部验收通过**。

## v0.8.0 — 2026-10-22（开发产物）

### 已实现与内容校对

新增 10 篇／80 句学习改编：N5 3、N4 3、N3 2、N2 2；合计 **25 篇／253 句，各等级 5 篇**。新材料均保存来源快照、原作者集合、来源版本、CC BY-SA 4.0 许可、改编范围、主题、参考分级及独立假名。旧 N1 5 篇逐句复核，旧 15 篇正文及 ID 保留。校对记录见 `docs/evidence/v0.8.0/content-review.md`，源文件哈希见 `new-reading-audit.json`。

阅读库新增索引安全格式及唯一性、索引与元数据 ID 对应、元数据键名与顺序、必填字段和 HTTPS URL 校验。新增测试检查索引覆盖所有 TXT／TSV、离线署名、各级数量、错误数据拒绝及旧 ID 顺序。保留既有界面、输入、计分、恢复、导航与品牌实现；没有凭假设进行交互重写。

### 单元测试、Lint 与构建

- 原版本基线：完整命令成功，但测试／Lint 使用增量结果；随后以 `:app:testDebugUnitTest --rerun-tasks` 强制重跑，成功。分别见 `baseline-check.txt`、`baseline-unit.txt`。
- 本轮执行 `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest`：通过；**46 项单元测试，0 失败、0 错误**。证据 `check.txt`、`unit-tests/`、`verification.json`。
- Lint：**0 错误／17 警告**。逐项位置见 `lint-results.xml`；类别为 GradleDependency 4、NewerVersionAvailable 1、UseKtx 9、MonochromeLauncherIcon 2、VectorRaster 1。未为清警告升级依赖或更改品牌资产。
- 设备测试包编译通过不计为设备测试通过。

### 设备测试（阻塞）

执行 `:app:connectedDebugAndroidTest` 失败：没有连接设备，详见 `connected-check.txt`。ADB 在授权环境成功启动后列表为空。尝试现有 Pixel_10_Pro 模拟器：默认启动报告实例过多；显式端口重试仍无设备；软件渲染冷启动后进程存在，但 5555 端口拒绝连接，未进入可测试状态。日志为 `emulator-*.txt`，设备列表为 `devices.txt`。未修改用户 AVD 数据或系统设置。

没有完成真实手机或两种日语 IME 的组合、确认、光标修改、跨句、切换与退出复验。API 26／30／31+、后台／重建、旧数据及覆盖安装、CSV、TalkBack、系统导航等设备矩阵仍待执行；既有单元测试不能替代这些项目。

### 截图检查（未验证）

本轮未取得应用运行截图。360dp／150% 字体、键盘展开、长句删除回退、深浅色及两套动态配色均未完成目检。来源 HTML 快照不属于应用界面截图验收。

### 包体与性能

- 当前 Debug APK：`app/build/outputs/apk/debug/app-debug.apk`，0.8.0 / 17，**12,643,236 字节**。
- SHA256：`e537626b512c3cadee8538cfd01590d9688b8ad29a0c78733f70bd977136c10e`。
- 修改前已有 0.7.0 Debug APK 的大小与哈希保存在 `baseline-apk.json`（12,828,381 字节）；它是已有增量产物，不能当作本轮完整重建的同配置性能样本。没有旧 APK 提交入库。
- 本轮缺少同设备冷启动、帧耗时、滚动、切换及内存实测，**不宣称体积优化或性能提升**。

### 交付边界

内容与资源校验已完成本轮开发，全部设备与兼容性门槛尚未满足，**不能宣布 0.8.0 整版验收通过**。当前 APK 为本地开发产物；未提交、推送 GitHub 或发布商店。后续完成本版验证与修复时不重复递增版本。

## v0.7.0 — 2026-10-10

已实现主页双模式、独立配置、离线阅读筛选／详情／假名／署名、整篇逐句练习与结算，移除专属词库搜索，保留普通浏览及原单词练习。修复阅读全文换行一致性，并在恢复阅读详情时显示加载或错误状态。

### 内容核验

实际 15 篇／173 句：N5 2、N4 2、N3 3、N2 3、N1 5。25 篇目标仍缺 10 篇（3／3／2／2／0）。14 篇改编或抄编、1 篇正文转录；原文与标准假名分开保存。来源、许可、改编及参考分级见 [reading-sources.md](reading-sources.md)。新 N1 内容已纳入本轮资源完整性测试与 APK，不沿用此前 10 篇构建的结果。

### 自动化测试与构建

- 使用 Android Studio 自带 Java，执行 `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`，另编译 `:app:assembleDebugAndroidTest`：通过。
- 单元测试 40 项，失败 0，错误 0。新增覆盖句子规范化、组合输入、有效前缀、删除回退、重复修订／旧题拒绝、跳过、35 句整篇、重建恢复、旧单词快照、退出重开及资源完整性；既有词汇与计时测试一并执行。
- Lint：0 错误、17 警告，未宣称零警告。
- 设备测试代码已编译，未运行。当前 adb 服务已正常启动，但设备列表为空；本轮未执行 connectedDebugAndroidTest。此前尝试失败的 final-check.txt 仅是历史记录。
- 证据：`docs/evidence/v0.7.0/check.txt`、`unit-tests/`、`lint-results.xml`、`devices.txt`、`verification.json`、`reading-audit.json`。

### 截图检查与真实设备

本轮没有应用截图检查，也没有真实设备验收。真实系统 IME、切换输入法、连续跨句输入、长句滚动、大字体／小屏／键盘布局、深浅主题、后台恢复、屏幕重建和提前退出的完整 UI 回归均待设备验证；单元测试与编译不能替代这些项目。来源许可截图仅为内容许可证据。

### APK 与已知限制

- 唯一交付：`app/build/outputs/apk/debug/app-debug.apk`，0.7.0 / 16，12,828,381 字节。
- SHA256：`55ac51eaf226ca653a74e31eb5eea6b71b85ca98e99287f527350e0cb2568430`。
- 尚缺 10 篇核验材料；分级仅供参考。百科抄编已披露，部分原页参考文献不足提示亦在来源记录说明。
- 没有同设备、同配置性能或体积对比，不宣称改善。当前体积仅为本次产物大小。
- 保留本地既有工作，未提交、推送或发布；版本号未再次递增。

## v0.6.1 — 2026-09-22

主页字样再上移 3dp（累计 11dp）；原始描摹轮廓在简化前平滑，并以 3 倍设备像素离屏绘制、过滤缩小，缓存随布局尺寸更新；关于页可点击个人主页显示完整 https://github.com/S-Hyakuri。

- 单元、Debug 构建成功，Lint 0 错误 / 17 警告；日志 `docs/evidence/v0.6.1/check.txt`。
- adb 无连接设备；本轮未执行设备测试、截图或实机锯齿对比，不能记为视觉验收通过。
- APK 0.6.1 / 15；SHA256：`8013102BEC1E3A70990F0F804EA346D65F47F70D139D31B3AA1E0D2E2DE10B56`。

## v0.6.0 — 2026-09-22

保留 220ms 切换时长，三个主页页面常驻组合，切换使用图层透明度与位移，避免目标页面反复构建；沿用学习列表按需排版。主页字样由上移 5dp 调整为 8dp，描摹轮廓去除像素台阶并增加有限幅度二次曲线平滑。应用名统一为 KanaFlick，关于页移除 Chongzheng Li 署名行。

- 单元测试、Lint、Debug 构建和 Android 测试包编译通过，日志 `docs/evidence/v0.6.0/check.txt`。
- 新增 RetainedTabsTest 验证反复切换时各页面只创建一次且不销毁，但仅完成编译，尚未设备执行。
- 尝试启动 Pixel_10_Pro 模拟器，但 adb 仍无连接设备；本轮无截图、设备测试或帧耗时实测。平滑字形视觉保真、快速连点、滚动状态、内存增量及卡顿改善均待实机验证。
- APK 0.6.0 / 14；SHA256：`4D72147ED7DEE09F870EB39345C1BFF6362FDC3ABF63E00C71AD8A7E522D6DFF`。

## v0.5.7 — 2026-09-22

恢复页面切换 220ms（淡入 180ms、淡出 120ms）；学习页由整页 30 张卡片同步排版改为 LazyColumn 按需组合，使用稳定词条键和内容类型；分页改用缓存 subList，避免 drop 复制其后全部词条。

- 单元测试、Lint 与 Debug 构建成功，Lint 0 错误 / 16 警告；日志 `docs/evidence/v0.5.7/check.txt`。
- adb 无连接设备。本次未做设备测试、截图或帧耗时采样；学习页滚动、分页位置恢复、快速切换及实机掉帧改善待验，不能宣称卡顿已解决。
- APK 0.5.7 / 13；SHA256：`40176E3CC3C0B4B28D3E3AE21C534977A12DB506467C3471666B4F31D65CA7DF`。

## v0.5.6 — 2026-09-22

导航内容整体下移 4dp，保留 64dp 底栏和系统安全区；页面切换缩短到 150ms，入场位移 12dp、出场位移 6dp，淡入 120ms、淡出 90ms，取消尺寸过渡。

- 单元、Lint、Debug 构建通过：`docs/evidence/v0.5.6/build-check.txt`。
- 设备检查尝试失败，原因 `No connected devices!`，日志 `docs/evidence/v0.5.6/check.txt`；未成功安装或切换页面，本轮无截图或帧率证据，不沿用旧版设备通过结论。真机实际流畅度、快速连点、小屏大字与底栏点击待验。
- APK 0.5.6 / 12；SHA256：`8E392D510F00CF8E6D21C0048534DB5D899979BDAD64823CA9C2D0B454338C17`。

## v0.5.5 — 2026-09-22

主页字样缩小 10%（208→187.2dp）、上移 5dp；左上图标改为透明矢量，与字样视觉对齐。导航图案 24→28dp，内容区 80→64dp，保留系统导航安全区并统一底色。

- 29 项单元、6 项启动专项设备测试及 Debug 构建通过，Lint 0 错误 / 16 警告。日志 `docs/evidence/v0.5.5/check.txt`。启动专项不代表底栏交互或完整业务自动回归。
- 模拟器安装成功，浅深主页截图保存在 `docs/evidence/v0.5.5/home-light.png`、`home-dark.png`；已目检图标透明底、品牌对齐与导航留白。真机、小屏大字、三键导航本轮未测。
- 最新 APK 0.5.5 / 11；SHA256：`FE91B104F7A085B502E5C654FFB4B8CB4BBCFA1D0059B6615525B2A8AEB07E1A`。

## v0.5.4 — 2026-09-22

主页品牌字样样式修复，复用原矢量字形与渐变，不改变业务流程。

- 单元：29 项通过；设备：Pixel_10_Pro / Android 17 上 6 项 LogoStartupTest 通过；这些是启动专项，不代替完整业务设备回归。
- Debug 构建成功，Lint 0 错误 / 16 警告。日志 `docs/evidence/v0.5.4/check.txt`。
- 安装新版后实际检查主页浅色与深色截图：`docs/evidence/v0.5.4/home-light.png`、`home-dark.png`；品牌字样透明底、比例正常，无裁切。真机、小屏大字、TalkBack 操作未在本次实测。
- APK：`app/build/outputs/apk/debug/app-debug.apk`，versionName 0.5.4，versionCode 10，SHA256 `D78C8EDC8616D843FFF213497F76CF48F80ED116BF5F9E3CDDA10C2772DD2F10`。

## 版本总览

| 版本 / 日期（2026 年） | 单元测试 | 模拟器测试 | 构建 / Lint | 截图与主要边界 |
| --- | --- | --- | --- | --- |
| 0.5.4 / 09-22 | 29 通过 | 6 项启动专项通过 | Debug 成功；0 错误 / 16 警告 | 主页浅深截图已目检；真机待验 |
| [0.5.3](#v053) / 09-22 修订 | 29 通过 | 6 项启动专项通过 | Debug 成功；0 错误 / 16 警告 | 实际截图字样水平中心与屏幕中线一致；完整品牌稳定 790ms |
| [0.5.2](#v052) / 09-21 | 29 通过 | 未执行 | Debug 成功；0 错误 / 7 警告 | 用户截图为调整前 0.5.1；新版待复验 |
| [0.5.1](#v051) / 09-21 | 29 通过 | 未执行 | Debug 成功；0 错误 / 7 警告 | 旧版遮挡有用户实机截图；新版待复验 |
| [0.5.0](#v050) / 09-21 | 29 通过 | 未执行；测试包构建成功 | Debug 成功；0 错误 / 7 警告 | 无连接设备／可用模拟器；动画、键盘与浅深截图待验 |
| [0.4.0](#v040) / 09-20 | 24 通过 | 15 通过 | Debug 成功；0 错误 / 7 警告 | 启动图标、主题与计时；浅深普通日语键盘截图，真机／多输入法切换待验 |
| [0.3.1](#v031) / 09-20 | 20 通过 | 13 通过 | Debug 成功；0 错误 / 6 警告 | 浅色、深色小屏大字；真机日语输入、启动器待验 |
| [0.3.0](#v030) / 09-19 | 18 通过 | 12 通过 | Debug 成功；0 错误 / 4 警告 | 浅深色、小屏大字，包内 10,635 词核对；词义未逐条核验 |
| [0.2.0](#v020) / 09-19 | 15 通过 | 9 通过 | Debug 成功；0 错误 / 4 警告 | 文件选择器、重启持久化；当时仅 11 条样例 |
| [0.1.0](#v010) / 09-19 | 9 通过 | 6 通过 | Debug 成功；0 错误 / 4 警告 | あ行五键、浅深色、两套动态色、小屏大字 |

设备自动测试均来自 Android 17 / API 37 模拟器，不等于实体手机覆盖。

<a id="v053"></a>

## 0.5.3 验收记录

### 09-22 居中与可读停留修订（当前交付）

版本保持 0.5.3 / 9。用户反馈字样偏心且停留过短，本轮只调整布局和动画节奏。

- **实现**：新增 SplashWordmarkLayout，布局完成后按根视图中线定位，并转换嵌套图标坐标；WordmarkDrawable 改以可见轮廓居中。图标不移动，避免为居中而破坏系统首帧衔接。Kana/Flick 在丝带收尾时接力出现，950ms 全部完成，完整品牌保持至 1740ms，然后 160ms 淡出；总时长 1900ms，真实加载另计。原有稳定停留仅 140ms，现为 790ms。
- **单元／构建／Lint**：29 项单元通过，Debug 构建成功，Lint 0 errors / 16 warnings；[检查日志](evidence/v0.5.3/centering/check.txt)、[Lint](evidence/v0.5.3/centering/lint-results.txt)。
- **设备专项**：Android 17 API 37 模拟器，6 项通过、0 失败；[报告](evidence/v0.5.3/centering/tests.xml)。新增不对称父级留白、嵌套图标与布局宽度变化的定位测试；新增完整字样稳定保持、可见像素中心及总动画不超过 2 秒的测试。原信号避让、丝带路径与透明终帧测试继续通过。
- **实际截图**：[品牌稳定阶段](evidence/v0.5.3/centering/brand.png)为运行中的 APK 截图，1280×2856；字样蓝色可见边界 x=417..862，中心 x=639.5，与屏幕像素中线 639.5 一致，[测量记录](evidence/v0.5.3/centering/screen-centering.txt)。此证据为该模拟器横向居中，不泛化为所有机型验收。
- **录像检查**：[完整启动录像](evidence/v0.5.3/centering/launch.mp4)显示丝带收尾与字样接力、完整停留及正常淡出到主页；未观察到这次布局修改引入的图标跳位。截图检查、设备自动测试与真实用户设备复验分别记录，不互相替代。
- **未验证**：实体手机上的偶发闪烁、其他系统启动层级、旧 Android 和 120 Hz 帧耗时；完整输入法／业务设备回归未重跑。本次未重跑深色录像，上一轮深色证据为历史记录。
- **当前 APK**：app/build/outputs/apk/debug/app-debug.apk，SHA-256 `B3B1537A771B0E2A0AC0FB4EEBC74D1759F654FB7843884EC5567AE21AEFA713`。单一安装入口，无提交／推送／发布。

### 09-21 首轮记录（历史）

日期：2026-09-21。用户指定本次验收版本 0.5.3 / versionCode 9。范围：信号点与「か」避让、原图风格丝带高光渐变、左尖端顺时针轨迹、启动闪烁改进，以及参考字形 Kana／Flick 接力出现。

- **实现**：三个信号继承前方方块的基础跳跃位移，叠加独立向外脉冲；不改终帧静态位置。丝带采用固定 rear/front 轮廓，显露从 (208,1173) 向左上／后侧、右侧和下方前景推进；高光回收而不旋转整张图，最终轮廓外不绘制。
- **字样**：按 docs/font.png 提取字形闭合路径，保留字腔及三点，未捆绑字体或文字位图。Kana 先轻弹、Flick 晚 70ms、三点随后依次出现；没有两行标语。1230ms 开始字样，1680ms 全部稳定，1820–2000ms 淡出；真实加载另计。
- **闪烁修复范围**：系统启动与 MaterialTheme.surface 使用相同背景资源；API 34+ 使用系统 surface_light/dark，API 31–33 使用已有动态中性色。动作和淡出由同一个 ValueAnimator 驱动，取消独立退出动画交接。不是对所有机型“绝不闪烁”的保证。
- **单元测试**：29 项通过，0 失败；本次没有改变判题／计分逻辑。记录 [final-check.txt](evidence/v0.5.3/final-check.txt)。
- **设备专项测试**：Pixel 10 Pro 模拟器 / Android 17 API 37，4 项通过、0 失败；[XML](evidence/v0.5.3/logo-tests.xml)。逐 4ms 验证 0–1000ms 的前方方块与每个信号真实路径无交集；验证左尖端起点与左下空白不被绘制；验证透明边缘、终帧稳定与 API 34+ 主题 surface 一致。
- **构建与 Lint**：Debug 成功；0 errors / 17 warnings，[Lint 报告](evidence/v0.5.3/lint-results.txt)。现存依赖更新、启动器 monochrome、矢量尺寸及 KTX 建议未扩展处理。
- **截图／录像检查**：原生绘制关键帧与字形图保存在 [logo-evidence-v053](evidence/v0.5.3/logo-evidence-v053/)。实际安装 v0.5.3，检查[浅色录像](evidence/v0.5.3/launch-light.mp4)、[深色录像](evidence/v0.5.3/launch-dark.mp4)及[再次冷启动](evidence/v0.5.3/launch-repeat.mp4)。本次观察到原有图形重叠消失，丝带先出现左侧尖端再到右侧／前景，字样按所选接力出现。完整终帧与主页交接以浅深 12 秒录屏为准；repeat 早期录屏只覆盖到字样阶段，不单独证明淡出完成。
- **测试环境说明**：初次录像跳过动画，诊断得到 ValueAnimator.areAnimatorsEnabled=false；系统 setting 显示 null 不代表当前运行时默认倍率。明确恢复模拟器 1 倍动画后重新安装／冷启动检查。临时诊断日志已从最终代码移除。
- **未验证**：实体手机上的偶发闪烁、120 Hz 帧耗时、Android 8–13 启动衔接、其他厂商系统遮罩，以及完整输入法／业务设备回归。上一版输入法测试停滞仍保留历史记录，本次没有将 4 项专项结果当作完整设备回归通过。
- **还原边界**：字样轮廓由原图提取；Logo 与丝带为参考原图的矢量描摹，补回多段渐变及高光，未声称与 JPG 每个像素一致，未加入原图背景纹理或柔焦投影。
- **交付 APK**：app/build/outputs/apk/debug/app-debug.apk，SHA-256 `6ED1C333CA39B1849A204668CF0A54D3A997FDDF98B9DAF9765838A1CF03B5F0`。只提供这个安装入口；没有提交、推送或发布。

<a id="v052"></a>

## 0.5.2 验收记录

日期：2026-09-21。范围仅为练习页留白调整、删除“答案”标题和升级 0.5.2 / versionCode 8。

- **用户截图检查**：[调整前 0.5.1](evidence/v0.5.2/reported-before.jpg)。可见跳过、反馈与统计位于键盘上方，统计下方仍有较大空白；用户反馈内容过于贴顶。该图不证明 0.5.2 通过。
- **代码实现**：顶部栏不动，按实际内容测量后剩余高度，分别为单词上方和输入框上方分配最多 32dp／44dp 新增留白，空间不够则同比例收缩至零。保留键盘安全区与滚动；“答案”只作为无障碍名称，不可见。
- **单元测试**：29 项通过，0 失败；原有判分、跳过、计时、词库回归保持。纯布局变更未新增镜像测试，单元通过不证明布局正确。
- **构建／Lint**：执行 `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug` 成功，[日志](evidence/v0.5.2/validation.txt)；Lint 0 错误／7 警告，[报告](evidence/v0.5.2/lint-results.txt)。[APK 摘要](evidence/v0.5.2/apk-metadata.txt)。
- **设备测试／新版截图**：未执行／未获取。adb 无设备，AVD 清单为空。待同一手机输入法验证单词下移与输入间距、键盘不遮挡跳过和统计；补验长词／长释义、大字号、小屏滚动和键盘显隐。

<a id="v051"></a>

## 0.5.1 验收记录

日期：2026-09-21。范围为实机反馈的练习页键盘遮挡与布局收紧；不改变判题、计分、计时与词库规则。

- **用户实机证据**：[修复前截图](evidence/v0.5.1/reported-before.jpg)。退出和时间下方仍有独立进度行，单词区留白偏大，跳过位于输入法顶边，统计未显示。该截图仅证明修复前问题，不作为新版通过证据。
- **实现**：头部退出／数字／时间同排，下接进度条；练习页顶部及区间距缩为 4dp；单词区由固定 160dp 改为最小 80dp 随内容增高；跳过在输入框右下，增加下一词图标并保持 48dp 点击区。保留键盘安全区、窗口缩放与滚动兜底。
- **单元测试**：29 项，0 失败；沿用原回归测试，不为纯布局添加镜像测试。XML 保存在本版证据目录。
- **构建与 Lint**：`:app:testDebugUnitTest :app:lintDebug :app:assembleDebug` 成功。[日志](evidence/v0.5.1/validation.txt)；Lint 0 错误／7 警告，[报告](evidence/v0.5.1/lint-results.txt)。APK 为 0.5.1 / versionCode 7，[产物摘要](evidence/v0.5.1/apk-metadata.txt)。
- **设备测试**：未执行，adb 没有连接设备，模拟器清单为空。
- **截图检查**：已检查用户旧版截图；新版截图未获取。待在相同手机／日语输入法下复验默认视口能否同时显示题目、释义、输入、跳过与统计；补验大字、长释义、小屏滚动、三词动画和键盘收放。

<a id="v050"></a>

## 0.5.0 验收记录

日期：2026-09-21。范围：三词移动、紧凑居中练习页、快速判分与忽略符号、跳过、提前退出结算、GitHub 关于信息。用户确认答错不换词；跳过推进，单独计数且不额外计错。

### 单元测试、Lint 与构建

- 执行 `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest`，BUILD SUCCESSFUL。[构建日志](evidence/v0.5.0/validation.txt)
- 29 项单元测试通过，0 失败：原 24 项与新增 5 项。新增覆盖跳过后旧题事件拒绝、答错再跳过不重复计错、提前结算与结算后禁止计分、符号及长音／前缀／组合输入保护、全部跳过零正确率。[新增单元 XML](evidence/v0.5.0/TEST-com.hyakuri.kanaflick.PracticeV050Test.xml)
- 全量词库 10,635 条加载及去重回归通过。首次验证暴露判题归一化与词库去重共用导致冲突，已将两者拆开后全量复验通过；不修改词库原文。[词库 XML](evidence/v0.5.0/TEST-com.hyakuri.kanaflick.VocabularyTest.xml)
- Lint：0 错误、7 警告。[报告](evidence/v0.5.0/lint-results.txt)
- Debug APK versionName 0.5.0、versionCode 6 已生成。设备测试 APK 已编译，不等于设备测试通过。

### 设备测试与截图检查（未验证）

- `adb devices` 没有设备；`emulator -list-avds` 没有可用 AVD。本版未执行 connectedDebugAndroidTest，不借用 v0.4.0 的通过结果。
- 已更新关于、退出结算和立即判分的现有设备用例，仅验证编译成功。
- 待设备检查：浅／深色、360dp／150% 字号、长词省略与无障碍、键盘打开时答案／跳过可达、连续答题与快速跳过、首末词空位、220ms 移动及系统关闭动画。
- 待设备回归：真实候选组合不计分、确认即判正确、150ms 错误稳定窗口、同修订去重、跳过后旋转恢复、后台暂停与提前结算冻结用时、退出与重开、个人主页跳转。
- 计划仓库 `https://github.com/S-Hyakuri/KanaFlick` 只作为尚未发布的可复制地址；GitHub 用户名与显示名来自本次连接账号查询，未建立或发布仓库。

<a id="v040"></a>

## 0.4.0 验收记录

日期：2026-09-20。范围仅为启动、系统导航区域融合、作者／帮助、练习进入、页面精简与本局计时。作者“百里茂”由用户确认，无联系方式；移除教程，不增加错词再练或历史库。

### 单元测试与构建

- 24 项单元测试通过，0 失败：原有练习／词库／学习 20 项，加计时 4 项（前台分段、后台排除、重复事件、恢复、完成／重开／退出及分钟格式）。[单元 XML](evidence/v0.4.0/TEST-com.hyakuri.kanaflick.SessionTimerTest.xml)
- 完整执行 `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:connectedDebugAndroidTest`，BUILD SUCCESSFUL。[完整日志](evidence/v0.4.0/validation.txt)
- 截图发现深色启动后状态栏图标偏暗，修正为浅深主题布尔资源后重新执行单元、Lint、Debug 和设备测试包构建，成功。[最终构建](evidence/v0.4.0/final-build.txt)
- Lint 0 错误、7 警告：5 条依赖更新提示（含兼容启动库），2 条原有单色启动器图层提示。没有跳过 Lint 或新增 baseline。[Lint](evidence/v0.4.0/lint-results.txt)
- 新增 `androidx.core:core-splashscreen:1.0.1`，用于 Android 8–11 与 Android 12+ 共用一次启动过渡；没有新增图片、字体、动画框架或网络权限。Debug APK 12,951,447 字节（约 12.35 MiB）；仅记录本次产物，不声称已做同配置体积优化。
- APK 路径 `app/build/outputs/apk/debug/app-debug.apk` 已实际存在；versionName 0.4.0 / versionCode 5 / minSdk 26。SHA-256：`BAA580374EA9F9467053B253034058E2FFC8E0882607C6945B3B0A5373741BB3`。没有提交 APK、推送或发布。

### 设备自动测试

Pixel_10_Pro 模拟器，Android 17 / API 37，手势导航：15 项通过，0 失败。[设备 XML](<evidence/v0.4.0/TEST-Pixel_10_Pro(AVD) - 17.xml>)

覆盖组合输入不判分、候选确认后自动判题、重复事件不重复计数、重建不把未确认组合文本误判、错误保留、完成／退出／重开、学习筛选与导航恢复、词数设置、词库编辑持久化、来源许可，以及：

- 进入新局自动聚焦，系统 IME 确认可见；主动隐藏后计时刷新不强制重开。
- 进入后台暂停计时，返回与重建保留用时；完成冻结，重开归零并重新聚焦。
- 关于显示“百里茂”，帮助可滚动，教程入口不存在。

旧测试经真实自定义词库入口创建测试夹具，并按实际随机题序断言，没有在正式应用保留测试／教程入口。中间新增测试曾因测试时钟未刷新及多文本节点匹配失败，修正后完整重跑通过；没有删去失败覆盖点。随后系统栏资源修正以最终构建及新版截图单独复验。

### 截图和启动观察

- [浅色首页](evidence/v0.4.0/light/evidence/home.png)、[选项](evidence/v0.4.0/light/evidence/options.png)、[练习](evidence/v0.4.0/light/evidence/practice.png)、[错误](evidence/v0.4.0/light/evidence/error.png)、[结果](evidence/v0.4.0/light/evidence/result.png)。
- [深色小屏首页](evidence/v0.4.0/dark-small/evidence/home.png)、[选项](evidence/v0.4.0/dark-small/evidence/options.png)、[练习](evidence/v0.4.0/dark-small/evidence/practice.png)：360dp、150% 字体，内容可滚动，底部安全区与 NavigationBar 背景连续；状态栏图标对比修正后可读。浅深截图流程各通过，记录分别见 light-capture.txt 与 dark-small-capture.txt。
- [普通日语键盘](evidence/v0.4.0/normal-japanese-keyboard.png)：模拟器 Gboard 17.2.2.895242737-preload-x86_64 日语 12 键，实际前台计时持续更新，题目、输入与计数可见。此键盘属于系统输入法，应用不捆绑。最初的模拟器手写浮条及 Gboard 首次布局选择页不作为普通键盘布局通过证据；通过模拟器设置关闭手写浮条后补测，应用不修改这些系统设置。
- [冷启动命令结果](evidence/v0.4.0/cold-start.txt)：COLD / Status ok，随后首页可用；[录屏](evidence/v0.4.0/cold-start.mp4) 能看到复用现有图标的系统启动画面，但采样未完整覆盖淡出至首页，不能据此认定逐帧无闪烁。无最低等待时长和无第二启动页由实现检查确认；启动耗时只保留原始单次记录，不作为性能基准或优化结论。

### 平台限制与未验证

系统“小白条”与应用三页 NavigationBar 是不同区域。普通应用没有独立永久移除手势提示且保持原系统导航行为的通用接口；本版使用透明系统栏和连续主题底色，不使用强制沉浸、不拦截返回、不修改用户系统导航。键盘展开时底部区域由 IME 管理。[Android 系统栏说明](https://developer.android.com/design/ui/mobile/guides/layout-and-content/immersive-content)、[edge-to-edge](https://developer.android.com/develop/ui/views/layout/edge-to-edge)。

仍未验证：实体手机、另一款日语输入法与实际切换过程、旧 Android 8–11、Android 12 本身、三键导航、TalkBack 实际朗读、不同厂商键盘、不同动态配色完整组合、关闭动画的设备流程、冷启动淡出和练习进入的逐帧流畅度／帧耗时。InputConnection 模拟和代码检查不能替代上述真机验证。屏幕重建已测，但不承诺恢复输入法候选窗口或所有杀进程／折叠场景。

<a id="v031"></a>

## 0.3.1 验收记录

日期：2026-09-20。范围：导航过渡、分组设置页、自动判题、词条居中及用户提供的应用图标。历史详情见下方折叠区，不作为本版通过证据。

### 单元测试

20 项通过，0 失败。新增自动判题对正确前缀、组合输入、错误修订、重复事件和过期题号的检查。证据位于 [v0.3.1](evidence/v0.3.1)。

### 设备测试

Pixel_10_Pro 模拟器，Android 17 / API 37：13 项通过，0 失败。包含输入法 InputConnection 候选组合与提交、组合中重建不误判、自动判题与键盘完成键不重复计分、错误保留、正确推进、退出重开、浏览状态恢复、词数设置、自定义词库与许可读取。

设备 XML：[13 项设备回归](<evidence/v0.3.1/TEST-Pixel_10_Pro(AVD) - 17.xml>)。新增重建场景中，恢复文本等待新的输入事件，防止 Android 未恢复 composing 标记而误判。

自动判题口径：候选确认后输入稳定 600ms；空白、组合输入、正确答案的未完成前缀不计分；其他不匹配输入记错且保留；同一修订只计一次。短暂停顿是输入完成的启发式判定，真机输入节奏仍需体验验证。

### 构建与静态检查

实际执行 :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:connectedDebugAndroidTest，最终 BUILD SUCCESSFUL。中间一次截图任务与重装操作争用模拟器，已改为串行后完整重跑通过。

- [完整验证](evidence/v0.3.1/validation.txt)
- [Lint](evidence/v0.3.1/lint-results.txt)：0 errors / 6 warnings。4 条既有依赖更新提示；2 条单色启动器图层提示。本版使用用户原彩色图，未制作单色派生图。
- 已确认 app/build/outputs/apk/debug/app-debug.apk，versionName 0.3.1 / versionCode 4 / minSdk 26。APK 不纳入 Git。

### 截图检查

实际模拟器截图，不是 Preview：

- [浅色首页](evidence/v0.3.1/light/home.png)、[设置](evidence/v0.3.1/light/options.png)、[练习](evidence/v0.3.1/light/practice.png)：图标可见，设置分组清晰，原文与辅助文本居中，无提交按钮。
- [深色小屏首页](evidence/v0.3.1/dark-small/home.png)、[设置](evidence/v0.3.1/dark-small/options.png)、[练习](evidence/v0.3.1/dark-small/practice.png)：360dp、150% 字号，内容换行并可滚动，底部导航保留；该配置截图流程通过。
- 模拟器手写浮动工具条会遮挡部分反馈与计数，不能作为普通手机软键盘避让通过证据。

## 待验与已知限制

历史测试只保留原版本效力，不能升级为当前版本通过。后续按下表补记录。

| 事项 | 当前边界 / 下一次检查 |
| --- | --- |
| 真机输入与布局 | 普通 Gboard 日语候选、另一输入法、600ms 输入节奏、选词／改错／重建、软键盘遮挡与横屏；记录设备和输入法版本 |
| 系统与无障碍 | Android 8–11 回退、Android 12 本身、不同动态配色、TalkBack、三键导航及其他厂商；0.1 配色证据不覆盖新页面 |
| 图标与流畅度 | 220ms 动画与导航状态测试已有记录，未测帧耗时，不宣称量化消除卡顿；彩色自适应资源已构建，各品牌裁剪、系统主题图标与真机流畅度待验 |
| 词库与许可 | 民间分级、释义未逐词独立核验，上游逐条引用不足；CC BY-NC 4.0 非商业使用，见 [来源](vocabulary-sources.md) |
| 文件与升级 | 0.2 已测本机文件选择器，后续未完整重测；云盘提供方、旧 APK 原地升级、满容量、磁盘满／断电与提供方故障压力测试仍缺证据 |
| 恢复与备份 | 不承诺候选窗口、所有杀进程／折叠场景恢复；导入预览重建需重选。卸载／清数据会丢词库，需 CSV 自行备份；无云同步和练习历史 |

## 如何补记录

验证命令见 [README](../README.md#开发与安装)。每版只记实际执行结果，保留失败原因、复验和未覆盖范围。状态用“通过／失败／阻塞／未验证”。

新增版本沿用“范围 → 单元／设备／构建 → 截图 → 限制”，证据放 `docs/evidence/v版本/`，旧详情移入下方折叠区，不另建验收文件。单项记录格式：`日期｜版本／设备／输入法｜操作与预期｜实际结果／状态｜证据`。

## 历史验收详情

保留原记录（仅整理标题和链接）；其中操作步骤、路径与未完成项属于当时版本，例如 0.2 的手动提交与 11 条样例已被后续版本替代。


<a id="v030"></a>

<details>
<summary>0.3.0：展开原始验收、截图与产物信息</summary>

### 0.3.0 验收记录

日期：2026-09-19。范围：JLPT 离线词库与“打字／学习／选项”三页，保留系统输入法。没有新增内置键盘、账号、广告、服务端或历史数据库。旧版记录见 [0.2.0 归档](#v020)。

#### 单元测试

18 项通过：练习／计分 5 项、词库／CSV／存储 9 项、搜索／随机抽题／20 词完整局 3 项、原工程示例 1 项。覆盖正式分级数量、唯一 ID、字段校验、搜索日语／中文／NFC、空结果、抽题不足、错误不推进、重复提交防护、重开清零。

证据：[词库](evidence/v0.3.0/TEST-com.hyakuri.kanaflick.VocabularyTest.xml)、[学习与抽题](evidence/v0.3.0/TEST-com.hyakuri.kanaflick.StudyTest.xml)、[练习](evidence/v0.3.0/TEST-com.hyakuri.kanaflick.PracticeStateTest.xml)。

#### 设备测试

环境：Pixel_10_Pro 模拟器，Android 17 / API 37，1280×2856、480dpi。无实体手机。

12 项通过：原有计分／退出／输入法组合协议／重建／自定义词库编辑／持久化，以及新增的导航、搜索、真实随机词库完整局、长度设置、离线来源和许可读取。证据：[设备 XML](<evidence/v0.3.0/TEST-Pixel_10_Pro(AVD) - 17.xml>)。

额外实际跨进程验证：界面选择 20 词 → 强制停止应用 → 新进程仍选中 20 → 恢复 10。证据：[设置写入](evidence/v0.3.0/settings-seed.txt)、[重启读取](evidence/v0.3.0/settings-restart.txt)。

测试初轮发现“学校”的原文／中文同名节点歧义，以及异步加载等待不足，已修正定位和等待后复验。没有删除失败场景或放宽业务判定。

#### 构建与打包

实际执行：

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:connectedDebugAndroidTest --console=plain
```

最终 **BUILD SUCCESSFUL**；18 项单元和 12 项设备测试通过。Lint **0 errors / 4 warnings**，均为既有稳定依赖更新提示，未屏蔽规则。证据：[完整验证](evidence/v0.3.0/validation.txt)、[Lint](evidence/v0.3.0/lint-results.txt)。

APK 内五份 CSV 已与 manifest 数量及 SHA-256 核对，来源说明、许可证全文均实际打包。N5 805、N4 757、N3 1815、N2 3214、N1 4044，共 10635。应用不申请网络或壁纸权限。

#### 截图检查

截图由真实模拟器采集，非 Preview。浅色、深色及 360dp／150% 字体分别执行截图流程，包含三页导航、练习、错误与结果。

- 浅色：导航、词数、查询结果、选项和结果页可读。
- 深色：发现新导航根容器未覆盖系统栏底色，已修复；最终截图已目检，三个入口的系统栏底色与图标对比正常。
- 360dp／150%：页面内容可滚动；不宣称所有内容同时占一屏。模拟器 Gboard 当前为手写浮动栏，会遮挡部分输入区域，不能当作普通日语键盘无遮挡验收通过。
- 完成后收起输入法；主要按钮与导航采用 Material 控件，主要目标至少 48dp，开始／提交等至少 56dp；未做物理触摸尺寸测量。

| 配置 | 打字 | 学习 | 选项 | 练习 | 错误 | 结果 |
| --- | --- | --- | --- | --- | --- | --- |
| 浅色 | [图](evidence/v0.3.0/light/evidence/home.png) | [图](evidence/v0.3.0/light/evidence/learning.png) | [图](evidence/v0.3.0/light/evidence/options.png) | [图](evidence/v0.3.0/light/evidence/practice.png) | [图](evidence/v0.3.0/light/evidence/error.png) | [图](evidence/v0.3.0/light/evidence/result.png) |
| 深色 | [图](evidence/v0.3.0/dark/evidence/home.png) | [图](evidence/v0.3.0/dark/evidence/learning.png) | [图](evidence/v0.3.0/dark/evidence/options.png) | [图](evidence/v0.3.0/dark/evidence/practice.png) | [图](evidence/v0.3.0/dark/evidence/error.png) | [图](evidence/v0.3.0/dark/evidence/result.png) |
| 小屏大字 | [图](evidence/v0.3.0/360dp-font150-dark/evidence/home.png) | [图](evidence/v0.3.0/360dp-font150-dark/evidence/learning.png) | [图](evidence/v0.3.0/360dp-font150-dark/evidence/options.png) | [图](evidence/v0.3.0/360dp-font150-dark/evidence/practice.png) | [图](evidence/v0.3.0/360dp-font150-dark/evidence/error.png) | [图](evidence/v0.3.0/360dp-font150-dark/evidence/result.png) |

#### 未验证与限制

- 词库分级和中文释义来自社区牌组，未逐词做独立词典核验；原作者未提供每条释义的更细上游引用。数据仅按 CC BY-NC 4.0 非商业使用，详见 [来源](vocabulary-sources.md)。
- 实体手机、Android 8–11 回退、Android 12 本身、两套动态配色、TalkBack、三键导航、横屏键盘布局未在本版验证。
- 标准 InputConnection 测试通过不等于实际手指使用 Gboard 普通日语布局、选词或其他输入法已通过。当前浮动手写栏遮挡仍是设备验收限制。
- 本版未重测系统文件选择器和所有云盘提供方；CSV 与存储自动回归通过。旧二进制存储格式未改，但未做旧 APK 原地升级安装实验。
- 恢复覆盖屏幕重建、本局快照和已确认文本，不承诺恢复输入法候选窗口或所有系统杀进程场景；不保存练习历史。

交付为本地调试签名 APK，0.3.0 / versionCode 3。未推送 GitHub，未发布商店，未提交 APK、密钥或构建缓存。

最终 APK：历史路径：app/build/outputs/apk/debug/KanaFlick-0.3.0-debug.apk（2026-09-20 整理时文件已不存在，保留当时的大小与哈希），12,431,693 字节。SHA-256：`EF78675223590E27B039995DE4B49D5CCA2718AAC41006D65EAD6342B1AF4AAB`。
[包内数据校验](evidence/v0.3.0/package-verification.txt)。

</details>

<a id="v020"></a>

<details>
<summary>0.2.0：展开原始验收、截图与产物信息</summary>

### v0.2.0 验收记录

日期：2026-09-19。功能代码已实现并通过下列实际测试；**真实中文 JLPT 分级数据接入仍未完成，Gboard 普通日语布局候选转换仍待实测**，不宣称全部验收完成。
旧版记录保留在 [v0.1.0 验收归档](#v010)，旧截图和通过状态不作为本版证据。

#### 实现范围与阶段结果

| 阶段 | 已实现 | 实际验证 |
| --- | --- | --- |
| 1 系统输入 | 标准 OutlinedTextField/TextFieldValue；明确按钮提交；组合时禁用；错误保留；NFC 判分；重建恢复 | 单元、Debug 构建及基础设备练习流程通过 |
| 2 分级词库 | N5–N1 离线资源选择、名称/数量/来源；共 11 条自编功能样例 | 资源读取和计数单元测试通过；真实中文分级数据未接入 |
| 3 自定义词库 | 词库和词条增删改；删除整库确认；CSV 预览追加、导出；私有目录原子保存 | 单元、设备编辑流程、真实系统文件选择器导入导出、强制停止后重新读取通过 |
| 4 回归与文档 | 来源说明、CSV 规则、设计规范、README、变更记录、验收证据 | 最终规定命令成功；分开记录截图及未验证项 |

本次已将 AGENTS.md 范围改为 v0.2.0。保留 Kotlin、Compose、Material 3、minSdk 26 和现有稳定依赖；未增加服务器、账号、广告、自制键盘、主题设置或练习历史数据库。词库文件位于 noBackupFilesDir，不参与系统自动备份。

#### 单元测试

实际执行 `:app:testDebugUnitTest`：**15/15 通过，0 失败、0 跳过**。

| 分类 | 数量 | 覆盖 | 证据 |
| --- | --- | --- | --- |
| 练习/计分 | 5 | 明确提交、完成后忽略输入、组合/空白/旧题忽略、错误不推进、重复提交防护、NFC 与非等价字符、重开/退出/零分母 | [PracticeState XML](evidence/v0.2.0/TEST-com.hyakuri.kanaflick.PracticeStateTest.xml) |
| 词库/CSV/持久化 | 9 | UTF-8/BOM/引号/逗号/换行往返、重复检测、错误行、畸形 CSV、非法 UTF-8、大小上限、NFC 去重、新 Store 读取、编辑删除、无效保存不改原字节、损坏文件不静默重置、五级样例真实计数 | [Vocabulary XML](evidence/v0.2.0/TEST-com.hyakuri.kanaflick.VocabularyTest.xml) |
| 原工程示例 | 1 | 基础示例 | [Example XML](evidence/v0.2.0/TEST-com.hyakuri.kanaflick.ExampleUnitTest.xml) |

#### 设备测试

环境：已有 Android 17 / API 37、x86_64 模拟器 emulator-5554（sdk_gphone64_x86_64）。常规显示 1280×2856、480dpi、字体 100%。没有连接实体手机。

实际执行 `:app:connectedDebugAndroidTest`：**9/9 通过，0 失败、0 跳过**。

| 用例 | 数量 | 实际覆盖 |
| --- | --- | --- |
| KanaFlickUiTest | 4 | 3 题完整局加 1 错误 → 75%；错误原文保留/提交禁用；重开清零；Activity 重建恢复已编辑文本及计数；IME Done 不计分；退出确认/取消 |
| InputConnectionTest | 1 | 通过 Android 标准 InputConnection 执行 setComposingText、commitText、finishComposingText、setSelection；组合时禁止提交、候选提交本身不计分、光标替换后一次按钮计 1 次 |
| LibraryUiTest | 1 | UI 新建词库、添加词条、编辑中文释义、重命名、Activity 重建后保留、取消删除与确认删除 |
| StorageDeviceTest | 1 | Android 实际文件系统原子保存、新实例读取、重复词条保存被拒后原文件字节不变 |
| VisualEvidenceTest | 1 | 实际页面截图流程和结果正确率检查，截图目检另列 |
| 原工程包名测试 | 1 | 应用包名 |

证据：[设备测试 XML](<evidence/v0.2.0/TEST-emulator-5554 - 17.xml>)。
**InputConnection 测试模拟标准输入协议，不等于手指操作 Gboard 的词典候选验证。**

额外跨进程验证：先在应用目录保存测试词库，执行 `am force-stop`，另一次 instrumentation 从新进程读取，并验证首页仍显示 3 词。结果通过，证据：[重启检查](evidence/v0.2.0/restart-verify.txt)。仅本次创建的测试词库随后已清理。

#### 系统文件选择器实测

在同一 API 37 模拟器通过真实 UI 操作：

1. 将样例 CSV 放入本机 Downloads；从应用“导入 UTF-8 CSV”打开系统 OpenDocument 选择器。
2. 已有 3 词的测试词库选中 11 词文件；预览显示新增 8、重复 3、错误 0，确认后实际为 11 词。
3. “导出词库”打开系统 CreateDocument 选择器，保存到 Downloads；读回文件为 11 条，原 id、日语、读音、中文及等级完整。证据：[实际导出 CSV](evidence/v0.2.0/export-roundtrip.csv)。
4. 导入缺失中文释义/非法 N9 等级的文件；预览显示第 2、3 行错误。确认按钮灰色且点击不导入，取消后原词库仍为 11 词。

证据：[有效预览](evidence/v0.2.0/import-preview.png)、[错误预览](evidence/v0.2.0/import-errors.png)。
此处仅覆盖模拟器本机文件提供方，不声称已验证所有云盘/第三方提供方。非法 UTF-8、损坏 CSV 和大小上限由上方单元测试覆盖。

#### 构建与 Lint

最终实际命令：

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:connectedDebugAndroidTest --console=plain
```

结果 **BUILD SUCCESSFUL**。Lint **0 errors / 4 warnings**，均是既有依赖可更新提示；没有屏蔽规则或引入新依赖。
证据：[完整验证记录](evidence/v0.2.0/validation.txt)、[Lint](evidence/v0.2.0/lint-results.txt)。
构建环境的 emulator console / JDK Unsafe 提示没有造成任务失败；测试通过数量来自 XML，不只依据命令退出码。

Debug APK 为版本 0.2.0 / versionCode 2、开发调试签名，位于 app/build/outputs/apk/debug/app-debug.apk。未提交 APK、缓存、local.properties 或密钥，未推送 GitHub，未发布商店。

#### 截图检查

截图来自真实模拟器 UiAutomation.takeScreenshot。不是 Compose Preview，也未用源码定义代替验证。

| 检查 | 实际结果 | 范围 |
| --- | --- | --- |
| 常规浅色 | 已采集并目检 | 首页、输入、错误反馈、结果；文字可读，来源/样例状态明确 |
| 常规深色 | 已采集并目检 | 系统深色下练习与结果；配色角色与文字可读 |
| 360dp / 150% 字体 | 截图流程完成 | 题目和输入/提交可通过滚动访问；不声称反馈、计数及全部操作同时占据一屏 |
| 结果页输入法残留 | 截图发现后已修复 | 完成/退出时显式清焦点并收起输入法；最终浅深色和 360dp/150% 结果截图复验无遮挡 |
| 主要触摸目标 | 代码检查 + 设备操作通过 | 主按钮至少 56dp，Material 控件默认交互区至少 48dp；未做独立物理尺寸测量 |
| 两套动态配色、Android 8–11 回退 | 本版未重新设备验收 | 保留主题代码，不能继承旧版截图作为新页面已通过证据 |

| 配置 | 首页 | 练习 | 错误 | 结果 |
| --- | --- | --- | --- | --- |
| 常规浅色 | [首页](evidence/v0.2.0/light/evidence/home.png) | [练习](evidence/v0.2.0/light/evidence/practice.png) | [错误](evidence/v0.2.0/light/evidence/error.png) | [结果](evidence/v0.2.0/light/evidence/result.png) |
| 常规深色 | [首页](evidence/v0.2.0/dark/evidence/home.png) | [练习](evidence/v0.2.0/dark/evidence/practice.png) | [错误](evidence/v0.2.0/dark/evidence/error.png) | [结果](evidence/v0.2.0/dark/evidence/result.png) |
| 360dp / 150% | [首页](evidence/v0.2.0/360dp-font150/evidence/home.png) | [练习](evidence/v0.2.0/360dp-font150/evidence/practice.png) | [错误](evidence/v0.2.0/360dp-font150/evidence/error.png) | [结果](evidence/v0.2.0/360dp-font150/evidence/result.png) |

#### 未完成与未验证

- **真实 JLPT 中文分级数据未接入**：当前只有 11 条自编功能样例，等级暂定、释义未做词典核验。调查与许可证缺口见 [词库来源](vocabulary-sources.md)。
- **Gboard 普通日语候选转换未实测通过**：模拟器安装的 Gboard 为 18.1.3.962075747-release-x86_64，实际显示手写浮动栏；已验证标准组合输入接口，未完成普通日语布局的手指输入、候选翻页与选择。不得宣称所有输入法兼容性已验证。
- **实体手机、Android 8–11 回退、Android 12 本身、其他输入法、TalkBack、三键导航、横屏软键盘停靠遮挡**未验证。Activity 重建通过，不等同于所有旋转/折叠/后台回收情形。
- **满容量词库、磁盘满/系统断电注入、第三方文档提供方故障**未进行设备压力测试；有容量边界和原子替换实现，测试仅覆盖记录的异常。
- 导入预览在页面重建后需重新选择文件，不会自动写入；系统候选窗口与组合区域不保证重建恢复，已确认文本/光标和当前局可以恢复。
- 应用卸载或清除数据会删除本机词库，需自行导出备份；无云同步或练习历史。

建议真机重点顺序：Gboard 输入 がっこう → 选“学校” → 确认候选后点提交；组合中/完成键不计分；故意错答再移动光标修改；旋转恢复；键盘弹出后滚动访问题目与提交；导入异常及重启持久化。只记录实际执行结果。

最终 Debug APK SHA-256：`C02108C2B6BA59793D7BECE700E45AEE93BF1984C5B2D6DB8B7F665D4F1001B0`。

</details>

<a id="v010"></a>

<details>
<summary>0.1.0：展开原始验收、截图与产物信息</summary>

### v0.1.0 验收记录

日期：2026-09-19。结论：本版功能与 Android 17 Pixel 模拟器验收通过；旧版 Android 和实体手机测试尚未完成。
没有上传 GitHub，没有发布应用商店。

#### 环境与产物

| 项目 | 实际检查结果 |
| --- | --- |
| 工程 | 保留原工程、applicationId com.hyakuri.kanaflick，Kotlin / Compose / Material 3 |
| SDK | minSdk 26；compileSdk / targetSdk 37；本机已安装 Android 37.0、Build Tools 36.0.0 |
| 构建工具 | AGP 9.4.1、Gradle 9.6.0、Kotlin Compose 插件 2.2.10 |
| Java | 构建 daemon：Android Studio JBR 25；命令行 launcher 为 Oracle 26.0.1 |
| 模拟器 | 已有 Pixel_10_Pro，Android 17 / API 37，x86_64，手势导航 |
| 常规显示 | 1280×2856 px，480 dpi，约 427dp 宽，字体 100% |
| 小屏组合 | 1080×1920 px，480 dpi，即 360×640dp，字体 150% |
| Git | 本地仓库已初始化，Android .gitignore 已配置；未推送，无远程发布 |
| APK | [app-debug.apk](../app/build/outputs/apk/debug/app-debug.apk)，版本 0.1.0 / versionCode 1，开发调试签名 |

APK SHA-256：
`A893BF405D61A531811C717979C77006349ED5592F3EAC63558E2C5090C08DCD`

#### 自动化检查

实际执行：

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:connectedDebugAndroidTest --console=plain
```

| 检查 | 结果 | 证据 |
| --- | --- | --- |
| 单元测试 | 9/9 通过：8 项业务测试 + 原工程 1 项示例测试 | [业务测试 XML](evidence/unit-tests.xml)、app/build/reports/tests/testDebugUnitTest/index.html |
| 设备测试 | 6/6 通过：4 项交互/恢复/旋转测试、1 项截图流程、1 项原有包名测试 | [最终设备 XML](evidence/device-tests-final.xml) |
| Lint | 完成，0 errors / 4 warnings | [Lint 结果](evidence/lint-results.txt) |
| debug APK | BUILD SUCCESSFUL，实际安装并运行 | 上方 APK、下方设备截图 |
| 360dp / 150% 字体 | 最终 APK 截图流程通过，能完成十题并返回首页 | [运行记录](evidence/large-font-test.txt) |
| 绿色动态配色 + 大字体 | 实际系统配色切换后重启测试，流程通过 | [运行记录](evidence/green-large-font-test.txt) |

4 条 Lint 提示均为固定依赖有更新版本：Core KTX、Lifecycle Runtime、Compose BOM 和 Kotlin Compose 插件。
保留已通过构建与设备测试的稳定版本；Kotlin Compose 插件与 AGP 内置 Kotlin 2.2.10 对齐。
没有屏蔽 Lint。已修复模板冗余标签、未使用颜色资源和窗口尺寸读取建议。

初轮设备测试因旧 Espresso 3.5.1 调用 Android 17 已移除的 InputManager 方法失败；
升级稳定 Espresso 3.7.0 / AndroidX JUnit 1.3.0 后重跑通过。
构建工具另有 emulator console 与 JDK Unsafe 非致命提示，最终任务状态为成功。

#### 功能验收

| 项目 | 状态 | 方法 / 证据 |
| --- | --- | --- |
| 首页名称、介绍、开始入口 | 设备验证通过 | 浅深色首页截图、点击测试 |
| あ行 10 题、五个点击按钮 | 设备验证通过 | 固定顺序重复两轮，无滑动键盘 |
| 正确 10 次结束，错误不推进 | 单元 + 设备验证通过 | wrongAnswerDoesNotAdvance、fullRoundWithErrorsAndRestart |
| 正确 8、错误 2、再正确 2 → 10 / 2 / 83.33% | 单元 + 设备验证通过 | eightCorrectTwoWrongThenTwoCorrect、fullRoundWithErrorsAndRestart |
| 重开清空上一局 | 单元 + 设备验证通过 | restartClearsPreviousRound、设备结果页点击再次练习 |
| 零输入退出，无 NaN / 除零 | 单元 + 设备验证通过 | emptyExitAndZeroScoreAreFinite、退出确认和取消测试 |
| 返回首页、中途退出确认/取消 | 设备验证通过 | zeroInputExitCanBeCancelledThenConfirmed、截图流程 |
| 完成后继续输入不重复完成 | 单元测试通过 | tenCorrectFinishExactlyOnce；结果页由状态派生，无导航副作用 |
| Activity 重建保留目标、计数与错误反馈 | 设备验证通过 | recreationPreservesProgressAndFeedback |
| 实际横竖屏切换后继续答题 | 设备验证通过 | rotationPreservesSessionAndCanContinue |
| 不弹系统键盘 | 设备截图检查通过 | 实际点击流程无键盘；无文本输入控件 |
| 无账号、广告、网络服务、数据库 | 代码检查通过 | 只实现本地练习；这不是网络抓包审计 |

#### UI 验收

| 项目 | 状态 | 说明 |
| --- | --- | --- |
| 首页 / 练习 / 结果浅色 | 模拟器截图目检通过 | 系统动态浅色，见 light |
| 首页 / 练习 / 结果深色 | 模拟器截图目检通过 | 系统动态深色，见 dark |
| Android 12+ 不同系统配色 | API 37 模拟器验证通过 | 原系统蓝色 → 系统预设绿色，重启测试进程，按钮、容器与文字随之变化 |
| Android 12 系统本身 | 未验证 | 已测 API 37，不等同于覆盖所有 API 31+ 系统 |
| Android 11 或以下回退主题 | **未验证** | 本机只有 API 37 系统镜像；浅深回退代码和 Preview 定义完成，未运行旧版设备 |
| 360dp + 150% 字体 | 模拟器验证通过 | 练习页同时显示目标、反馈、计数与五键；首页/结果通过滚动访问所有内容和操作 |
| 错误提示不移动目标和按钮 | 截图目检通过 | practice 与 error 对照，两行反馈预留同一空间 |
| 手势导航安全区 | 模拟器截图目检通过 | 输入按钮在导航手势区域上方，浅深色手势条均可辨认 |
| 状态栏浅深图标 | 模拟器截图目检通过 | 常规及大字体截图；主题切换后稳定画面 |
| 主要触摸目标 ≥48dp | 代码检查 + 设备点击通过 | 返回 48dp，主要按钮至少 56dp；未做独立物理尺寸测量 |
| TalkBack 朗读顺序与提示 | **未验证** | 已添加返回说明、进度说明、polite 反馈；未实际开启 TalkBack |
| 真机、三键导航及其他厂商系统 | **未验证** | 无已连接实体手机，不将模拟器验证冒充真机验证 |
| Compose Preview | **未验证渲染** | 源码包含三页面预览定义；本次证据来自真实模拟器截图，不是 Preview |

#### 截图

以下均由模拟器 UiAutomation.takeScreenshot 实际采集，包含系统栏。

| 配置 | 首页 | 练习 | 错误 | 结果 |
| --- | --- | --- | --- | --- |
| 常规浅色 | [home](evidence/light/home.png) | [practice](evidence/light/practice.png) | [error](evidence/light/error.png) | [result](evidence/light/result.png) |
| 常规深色 | [home](evidence/dark/home.png) | [practice](evidence/dark/practice.png) | [error](evidence/dark/error.png) | [result](evidence/dark/result.png) |
| 360dp / 150% / 深色 | [home](evidence/360dp-font150-dark/home.png) | [practice](evidence/360dp-font150-dark/practice.png) | [error](evidence/360dp-font150-dark/error.png) | [result](evidence/360dp-font150-dark/result.png)、[滚动至操作区](evidence/360dp-font150-dark/result-actions.png) |
| 360dp / 150% / 系统绿色浅色 | [home](evidence/360dp-font150-green/home.png) | [practice](evidence/360dp-font150-green/practice.png) | [error](evidence/360dp-font150-green/error.png) | [result](evidence/360dp-font150-green/result.png)、[滚动至操作区](evidence/360dp-font150-green/result-actions.png) |

大字体首页截图是滚动到“开始练习”后的画面；结果截图分首屏与操作区，不声称全部内容同时占据一屏。
初次小屏截图发现计数需要滚动，随后增加短窗口紧凑间距，最终小屏截图与 assertIsDisplayed 已复验计数可见。
测试后已恢复模拟器分辨率、密度、100% 字体、浅色与原始系统配色配置。

#### 本人可继续进行的真机检查

1. **Android Studio**：打开此项目 → 等待同步 → 顶部设备列表选择已连接手机 → 点击绿色 Run ▶。
   手机连接及 USB 调试步骤见 [README](../README.md#开发与安装)。
2. **KanaFlick**：点击“开始练习”。依次输入 あ、い、う、え、お、あ、い、う；
   此时目标为え，故意点击あ两次，再点击え、お。应显示正确10、错误2、83.33%。
3. 点击“再次练习”，检查 0/10 和计数清零。未输入时点左上返回 → “退出练习”，确认回首页。
4. 练习中旋转手机，再答一题，确认目标和计数连续。
5. **Pixel 设置 → 壁纸和样式**：选择两套不同配色；每次应用后从最近任务划掉 KanaFlick，再重新打开并查看三个页面。
6. **手机设置 → 显示和触控 → 深色主题**：切换浅深色；再在“显示大小和文字”放大字体，检查可滚动访问按钮。
7. **Android Studio → Tools → Device Manager → + → Create Virtual Device**：选择 Pixel 设备 →
   下载并选择 Android 11 / API 30 系统镜像 → Finish → 启动 → 运行应用，验证蓝色浅深回退。
   在 Android 11 模拟器“Settings → Display → Dark theme”切换深色。
8. **手机设置 → 无障碍 → TalkBack**：开启后验证五个假名按钮、返回按钮、进度及错误反馈朗读。
   将设备型号、Android 版本、结果与截图补充到本文件；未执行前保持“未验证”。

</details>



## 启动 Logo 动画补充验收（2026-09-21）

仍为 0.5.2 / versionCode 8；本节为额外授权的启动动画，不替代历史练习页验证结论。

- **实现**：原生 Canvas/Path/LinearGradient Drawable，直接使用现有系统 SplashScreen 的图标位置。手绘双字块、三信号与丝带；透明背景不带 JPEG 方框。动画参数集中于 LogoMotion。Android 12+ 补偿系统静态图标归一化倍率，修复静态到动态缩小；无图标的系统启动回调直接释放，避免 AndroidX iconView 空值异常。系统禁用动画、重建与后台中断不继续播放。
- **单元测试**：最终代码 29 项通过；不是动画流畅性测试。
- **构建与 Lint**：Debug 成功，0 errors / 15 warnings。包括原有依赖升级／启动器 monochrome 提示，以及新增 VectorRaster 大尺寸静态图标与 UseKtx 建议；无新增依赖。
- **专项设备测试**：Pixel 10 Pro / Android 17 API 37，LogoStartupTest 1 项通过，校验透明四角、终帧稳定、起跳归零，并绘制六个关键帧。最终报告 [logo-result.xml](evidence/logo-startup/logo-result.xml)，构建记录 [final-check.txt](evidence/logo-startup/final-check.txt)。
- **实际启动与截图检查**：最终 APK 已安装启动，浅色与深色录屏检查通过：图标无方形背景、静态到动态大小一致、丝带逐步显露、结束进入首页。分别见 [浅色录像](evidence/logo-startup/launch.mp4)、[浅色连续帧](evidence/logo-startup/contact-sheet.png)、[深色录像](evidence/logo-startup/launch-dark.mp4)、[深色连续帧](evidence/logo-startup/dark-contact-sheet.png)。录屏采样与人工目检不等同 60/120 Hz 帧耗时测量。
- **关闭动画**：模拟器暂设 animator_duration_scale=0 后启动，首页“开始练习”节点存在，随后恢复原设置。证据 [animations-disabled-ui.xml](evidence/logo-startup/animations-disabled-ui.xml)。
- **完整设备回归未完成**：两次运行停在 InputConnectionTest.compositionWaitsAndCommittedCandidateAutomaticallyScores，主动终止，不能声明全套通过；尚未定位停滞根因。保留 build.txt 与 device-tests.txt。后续专项执行成功不代表输入法回归通过。
- **仍待验证**：实体手机、Android 8–11 兼容路径、其他厂商系统图标归一化、120 Hz 帧率、后台中断／旋转的实际设备用例及完整输入法回归。原图为手工矢量近似，未保留柔焦阴影与 JPEG 纹理，未声称像素级一致。
- **交付**：app/build/outputs/apk/debug/app-debug.apk；SHA-256：6B568A7F8824DC130F9900B6C7E541DCF2C2ABE63C7CD07CAA3EC177140B0A24。未提交、推送或发布。


### 2026-09-22 动画前等待修订（最新 APK）

- 删除动画开始对词库完成的依赖；首帧后动画与加载并行，慢加载在 1740ms 完整品牌帧暂停，完成或失败后恢复 160ms 淡出。总动作仍为 1900ms。
- 单元测试、lint、构建及 LogoStartupTest 启动专项检查通过，日志：`docs/evidence/v0.5.3/centering/parallel-loading-check.txt`。Lint 0 错误 / 16 警告。模拟器安装成功并冷启动，am start 返回 Status: ok；该工具耗时不是动画前停顿测量，不据此声称缩短了具体毫秒数。
- 本次没有重跑完整业务设备测试，没有实测物理手机、120Hz 或人为慢加载场景；先前视频对应上一轮居中修订，不能代表本次并行加载录屏。
- 最新 APK SHA256：`8B2B2898AAFDF322BBDB6D8BB7F805E4465ED544ECD6926876A34A3C6B60DF40`。版本仍为 0.5.3（9）。


2026-09-22 光学对齐修订：KanaFlick 保持屏幕居中；图标整体向右补偿 24 个原图坐标单位（约图标画布宽度的 1.56%），平衡深蓝前景方块偏左的视觉重量。参数集中于 LogoMotion.ART_OFFSET_X，系统首帧生成器读取同一参数，避免切入动画时跳位；动画时间保持不变。

验证：单元测试、lint、Debug 构建与 6 项 LogoStartupTest 通过。模拟器安装并截取完整品牌画面，已目检；证据为 `docs/evidence/v0.5.3/centering/optical.png` 与 `optical-check.txt`。光学对齐属于视觉调整，不将几何居中测试等同于主观验收；真机仍待验收。版本仍为 0.5.3（9）。最新 APK SHA256：`E06487429D8DB5F0E9BE558930A5AB6866AB783A7817DF3D365B707B6434CD72`。


2026-09-22 字样大小与位置修订（覆盖前次光学补偿）：撤回图标右移，ART_OFFSET_X=0；KanaFlick 宽度比从 0.78 改为 0.88（放大约 13%），中心向右偏移图标视图宽度的 3.5%，顶部比例从 0.40 调为 0.39。参数集中于 LogoMotion，字样限制在可用宽度内；系统首帧同步恢复原位置。动画时间与并行加载策略不变。

构建、单元测试与 6 项启动专项测试通过；已安装至模拟器并目检实际截图 `docs/evidence/v0.5.3/centering/wordmark-adjust.png`，验证日志 `wordmark-adjust-check.txt`。本次视觉位置仍待用户实机验收，不以测试通过代替主观确认。版本 0.5.3（9），最新 APK SHA256：`2227AA1F5CDA3B3376D3E42CA707113E89D5F976E0A6B023CE9CD6903D4F31AA`。
