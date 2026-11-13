# 心晴

一个私人用的安卓 App：随手记录心情、写日记与周记、课程表提醒、每日计划。

当前处于开发初期，已完成工程脚手架和「提醒链路自检」；课表、日记、回顾等功能按里程碑逐步加入。

## 功能规划

- **今天**：随手记心情（5 档）、今日课程、每日计划（清单 / 时间轴两种视图）
- **我的日记**：一天可记多条，独立日期与时间标签，可搜索、按心情筛选、珍藏
- **每周课表**：周一到周日网格，课程支持学期起止日期、单双周、提前 0/5/15/30/60 分钟提醒
- **生活回顾**：本周条数与字数、记录天数、连续天数、心情分布、四周热图、周记模板
- **设置与备份**：JSON 导出/导入，可导入网页版「慢慢记」的备份数据

第一版数据全部保存在手机本地，不联网、不上传。

## 一、用 GitHub Actions 打包 APK（不用装 Android Studio）

1. 在 GitHub 新建仓库（例如 `xinqing`）。
2. 把本目录 `xinqing/` 里的**全部内容**上传到仓库根目录，`.github/workflows/build.yml` 必须一起上传。
3. 打开仓库的 **Actions** 标签页，等待 `Build APK` 工作流运行完成。
4. 在运行详情页底部 **Artifacts** 下载 `xinqing-debug-apk`，解压得到 `app-debug.apk`。
5. 传到手机安装（需要允许「安装未知应用」）。

也可以手动触发：Actions → Build APK → Run workflow。

## 二、首次使用

1. 打开「心晴」，允许通知权限。
2. 点「允许精确闹钟」，在系统设置中允许「闹钟和提醒」。
3. 建议在应用设置中允许自启动，并关闭电池优化（国产 ROM 尤其重要）。
4. 点「发送 5 分钟后的测试提醒」，锁屏等待，确认通知能准时到达。

建议做一次三连测：发送测试提醒后，① 划掉后台任务；② 锁屏等待；③ 重启手机后再次发送。三次都应准时收到通知。

## 三、开发与构建

需要 JDK 17、Android SDK Platform 34 与 Gradle 8.7；本仓库不包含 Gradle wrapper，本地构建需自备 Gradle，或直接使用上面的云端构建。

```sh
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
```

技术栈：Kotlin 1.9.24、AGP 8.5.2、Jetpack Compose（BOM 2024.06.00）、Room、AlarmManager 精确闹钟、JUnit 4。

## 四、目录结构

```
xinqing/
├── app/src/main/java/com/hucw/xinqing/
│   ├── MainActivity.kt / XinqingApp.kt
│   ├── domain/          # 纯 Kotlin 业务逻辑（心情等）
│   ├── reminder/        # 通知、闹钟调度、广播接收器
│   └── ui/              # 主题、页面
├── app/src/test/        # JVM 单元测试
└── .github/workflows/   # 云端构建
```

## 五、说明

- 课程提醒依赖系统精确闹钟；部分厂商 ROM 会限制后台，需要在系统中放行。
- debug APK 使用调试签名，仅适合自己安装使用；上架应用商店需自行配置正式签名。
