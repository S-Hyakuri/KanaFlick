# 发布到 GitHub Releases

源码提交、APK 构建与 GitHub Release 是三个步骤；上传源码不会自动生成可安装的 APK。本说明不表示发布或设备验收已经完成。

## 1. 确定发布版本

当前应用配置为 **0.9.4 / versionCode 22**。首次将这一版本放到 Releases，不需要仅因创建 Release 再递增版本号。

准备标签 `v0.9.4`，标题可用 `KanaFlick v0.9.4`。已有设备验收缺口，建议此次标记为 **Pre-release**，如实写明已测与未测范围。

代码许可证由项目所有者决定，当前尚未指定。保留第三方词库与阅读材料的署名、许可全文及改编说明，不能用项目代码许可证替换它们。

## 2. 生成签名 APK

Android Studio 选择 **Build → Generate Signed App Bundle / APK**（文字可能随版本略有不同）：

1. 选择 **APK**，点击 Next。GitHub 上供用户直接安装时使用 APK，而不是 AAB。
2. 已有本应用的正式签名密钥时，选择已有密钥；以后更新继续使用同一签名身份。没有密钥时选择 **Create new**。
3. 将 keystore 保存在项目目录之外，设置自己的密钥库密码、别名与密钥密码。备份密钥及密码；不要将其发送到聊天或上传 GitHub。
4. 选择 `app` 模块与 **release** 构建类型，完成向导。不要把密码写入仓库内的构建脚本。
5. 从完成提示中的 **Locate** 找到产物，路径由向导所选输出目录决定。

当前源码未配置自动发布签名，不能仅运行 `assembleRelease` 就认定得到可分发的签名包，也不要上传 unsigned APK。Android Studio 向导可以在本机完成签名。

参考：[Android 官方签名说明](https://developer.android.com/studio/publish/app-signing)。

## 3. 核对安装包

- 确认版本为 0.9.4 / 22、包名为 `com.hyakuri.kanaflick`，且没有启用调试。
- 使用 Android SDK Build Tools 的 `apksigner verify --verbose --print-certs` 检查签名，保留用于将来更新的证书身份。
- 在实际设备安装这个签名 APK，检查单词与句子练习、输入法、熟练度、深浅主题、关于页链接。未验证的内容写入说明，不写成通过。
- 已安装 Debug 版时，签名可能不同，不能直接覆盖。先导出自定义词库；熟练度目前没有导出入口，不承诺卸载重装后能保留画像。

附件可命名为 `KanaFlick-0.9.4.apk`，重命名不改变应用内版本。对最终附件计算 SHA-256，并写入发布说明：

```powershell
Get-FileHash -Algorithm SHA256 "实际的APK完整路径"
```

已有 Debug 构建证据与签名 Release APK 的设备检查分别记录在现有 `docs/acceptance.md`、`docs/evidence/` 中。不要用旧 APK 的大小、哈希或截图充当新附件证据。

## 4. 提交对应源码

完成文档、版本与代码修改后：

```powershell
git add .
git --no-pager diff --cached --stat
git status
```

确认没有密钥、密码、本机配置、缓存或 APK，再执行：

```powershell
git commit -m "Prepare KanaFlick 0.9.4 release"
git push
```

没有新修改时，无须创建空提交。APK 上传到 Release 附件，不放入 Git 源码提交。最终应用源码应包含在即将选择的发布提交中。

## 5. 创建 Release

1. 打开 [新建 Release](https://github.com/S-Hyakuri/KanaFlick/releases/new)。
2. **Choose a tag** 输入 `v0.9.4`，选择创建新标签。已有同名标签时先核对目标，不覆盖它。
3. 新标签的 **Target** 选择包含发布源码的 `master` 分支或确定的提交。
4. 标题填写 `KanaFlick v0.9.4`。
5. 说明写清功能概况、本版变化、Android 8.0+ 要求、安装方式、已知问题与验证范围。首次公开发布还应说明整体用途，不能只复制本版关于仓库入口的修复记录。
6. 上传签名 APK，写入该文件的 SHA-256。GitHub 自动附带的 Source code 压缩包不能替代安装包。
7. 本次建议勾选 **This is a pre-release**。先 **Save draft**，核对标签目标、附件与文字。
8. 确认无误后点击 **Publish release**。从公开页面重新下载附件，核对哈希与安装结果。

预发布可能不出现在 `/releases/latest`，因此 README 的下载入口使用 [Releases 列表](https://github.com/S-Hyakuri/KanaFlick/releases)。

参考：[GitHub 官方 Release 操作说明](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository)。

## 后续版本

沿用同一正式签名身份，按 AGENTS.md 更新版本及文档、执行验证、提交源码，为新版本创建新的标签与 Release。文档调整、重复构建或同一版本首次发布，不单独增加应用版本号。
