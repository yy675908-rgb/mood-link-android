# Mood Android

一个不必打开网页的 Android 桌面小组件。首次打开应用填写配对地址与密钥，之后可从桌面直接同步六种状态：平静、开心、疲惫、烦躁、难过、想被陪。

## 发布维护

- 应用显示名称和桌面小组件标题均为 `Mood`。
- Android 包名保持 `com.moodlink.app`，因此既有配对数据和小组件功能保持不变。
- Release APK 使用 GitHub Secrets 注入的固定签名；keystore 文件不得提交到 Git。只要包名和签名不变，后续版本即可长期覆盖安装。
- 当前版本为 `versionCode 2` / `versionName 1.1`。每次发布都必须递增 `versionCode`，并按需更新 `versionName`。
- GitHub Actions 在 `main` 分支构建 release APK，并把稳定下载文件发布为 `mood.apk` 到 `latest` release。
- 仓库必须配置 `ANDROID_KEYSTORE_BASE64`、`ANDROID_KEYSTORE_PASSWORD`、`ANDROID_KEY_ALIAS`、`ANDROID_KEY_PASSWORD` 四个 GitHub Secrets；不要在日志、README 或提交历史中写出它们的值。

## 本地构建

```bash
gradle assembleRelease --no-daemon
```

生成的 APK 位于 `app/build/outputs/apk/release/app-release.apk`。
