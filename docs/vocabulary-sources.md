# JLPT 词库来源

0.3.0 使用 **egg rolls 的 JLPT10k v3.5 中文牌组**，共 **10,635 词**，可离线使用。民间分级仅供学习参考，不称作官方考纲或唯一标准。

| 等级 | 原始条数 | 去重后打包数量 |
| --- | ---: | ---: |
| N5 | 807 | 805 |
| N4 | 757 | 757 |
| N3 | 1,817 | 1,815 |
| N2 | 3,215 | 3,214 |
| N1 | 4,045 | 4,044 |
| 合计 | 10,641 | 10,635 |

按实际数量完整保留，未为凑到每级 1000 而补造或截断。0.2.0 的 11 条演示样例已从等级列表移除；选项页的 3 词教程仍使用独立自编内容。

## 来源与许可

- [JLPT 官方 FAQ](https://www.jlpt.jp/e/faq/) 说明现行考试不公布包含词汇、汉字和语法的完整出题基准表。
- 作者：**egg rolls**；[原项目与说明](https://github.com/5mdld/anki-jlpt-decks)。这是已有公开发布渠道和持续修订的社区牌组；没有可靠统计证明它是“最常用”的唯一词库。
- 固定版本：[b8a4712fa6e63054eed66564cf9cef9b60d13a2f](https://github.com/5mdld/anki-jlpt-decks/tree/b8a4712fa6e63054eed66564cf9cef9b60d13a2f)，词条标签版本 v26.09.06；[原始文件](https://github.com/5mdld/anki-jlpt-decks/blob/b8a4712fa6e63054eed66564cf9cef9b60d13a2f/deck-source/notes.csv)。取得日期：2026-09-19。
- [原作者许可证](https://github.com/5mdld/anki-jlpt-decks/blob/b8a4712fa6e63054eed66564cf9cef9b60d13a2f/LICENSE)：**CC BY-NC 4.0**，署名、非商业使用、说明修改、不得额外限制许可权利。本地 Debug 学习使用遵循此许可，未发布商店或加入商业功能。该数据许可不自动套用于本项目独立代码。
- 作者声明内容来自公开互联网资源和个人创作，未给出每条中文释义的更细上游引用；本项目如实保留这一限制，不宣称已独立核验全部释义或试题来源。
- 未引入音频、例句、字体、模板及其附带素材。

许可全文与中文来源说明已随 APK 离线附带，并可从“选项”查看：[NOTICE](../app/src/main/assets/vocabulary/NOTICE.txt)、[许可证全文](../app/src/main/assets/vocabulary/LICENSE-CC-BY-NC-4.0.txt)。

## 转换规则

原文件是带 Anki 注释的 39 列 TSV：第 3 列稳定 ID、第 4 列原文、第 7 列读音、第 8 列简体中文释义、第 2 列牌组等级、第 36 列排序值。等级、读音和释义均直接来自该社区牌组，不用英语或机器翻译冒充中文。

仅作 HTML 标签去除、实体解码、NFC 规范化和字段首尾空白清理；每级同原文同读音保留首次出现项，6 个排除 ID 记入 manifest；同形异读保留。原词条不补写、不重新定级。排序沿用原作者字段：N5/N4 为作者难度顺序，N3–N1 参考词频；这不是独立验证的考试频率。

- 原始 SHA-256：`30e9350c92c7bdeaf0f4c0317c9a0f659eb32e606a69870176deff159559eb1a`。
- [随包清单](../app/src/main/assets/vocabulary/manifest.json)：各级数量、输出 SHA-256、字段映射、排除 ID。
- [可复现转换脚本](../scripts/import_jlpt.py)：先下载固定原文件，再执行 `python scripts/import_jlpt.py --source build/vocabulary-source/notes.csv`。脚本验证原始散列后转换，不在运行应用时联网。

## 练习规则

输入原文，不接受用读音替代汉字；仅作 NFC 等价比较，不裁剪答案空格或混同平片假名。组合中和 IME 完成键不判分，只有提交按钮评分。同一文本修订只计一次，错误不推进，零输入正确率 0%。每局随机取 5/10/20 词，数量不足按实际；重开同组清零，退出清零。学习搜索结果使用相同规则抽题。
