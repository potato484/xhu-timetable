# xhu-timetable

一个面向西华大学教务体系的课表与信息查询客户端，采用 **Kotlin Multiplatform + Compose Multiplatform** 实现（当前以 Android 为主要运行端）。

> 说明：本项目偏向“课表/信息展示与管理”，**不包含选课/抢课** 等能力（当前接口层也未对接相关 API）。

## 功能概览

- 课表
  - 今日课表 / 本周课表展示
  - 周次选择（带日期范围）
  - 学期切换、开学日期设置
  - 课程颜色配置
  - 自定义课程（增删改查）
- 查询
  - 成绩查询、绩点信息
  - 考试安排查询
  - 实验成绩查询
- 学校信息
  - 校历（含农历信息）
  - 全校课表检索
  - 空教室查询
- Android 特有
  - Glance 桌面小组件（今日/本周）
  - 通知与后台任务（WorkManager）
  - 导出到系统日历

## 技术实现（简要）

### 模块划分

```
xhu-timetable/
├── app/                 # Android 入口 + Android-only 功能（Widget/通知/日历导出等）
└── shared/
    ├── core/            # 平台抽象、通用能力（SettingsStore、文件/网络状态、时间等）
    ├── network/         # Ktor API 定义与实现（REST 调用）
    ├── database/        # SQLDelight 本地存储（设置、颜色、缓存等）
    ├── domain/          # 领域模型、Repository/UseCase、加密与业务聚合
    └── ui/              # Compose Multiplatform UI、导航、ViewModel
```

### 关键技术栈

- UI：Compose Multiplatform + Material 3（`shared/ui`）
- 网络：Ktor Client + Kotlinx Serialization（`shared/network`）
- 存储：SQLDelight（`shared/database`）
- 依赖注入：Koin
- 时间：kotlinx-datetime

## 构建与运行（Android）

### 环境要求

- Android Studio / Android SDK
- JDK 17
- 可用的 ADB（真机或模拟器）

### 常用命令

```bash
# 构建 Debug APK
./gradlew :androidApp:assembleDebug

# 安装到已连接设备（确保 adb devices 能看到 device 状态）
./gradlew :androidApp:installDebug

# 或手动安装 APK
adb install -r "app/build/outputs/apk/debug/androidApp-debug.apk"
```

## 参考

- `XhuTimetable-master/`：用于对照/参考的历史版本代码（本仓库内仅作参考）。
项目地址：[XhuTimetable](https://github.com/Mystery00/XhuTimetable)

