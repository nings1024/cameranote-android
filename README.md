# 相记 · CameraNote

> 一个「先拍下来，描述晚点补」的本地记事本。

[![Platform](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3-7F52FF?logo=kotlin&logoColor=white)](#)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](#)
[![License](https://img.shields.io/badge/License-MIT-blue)](#许可证)

---

## 为什么做这个

记笔记最大的成本是「写」。等你想清楚怎么描述，眼前的事情已经过去了。

相记把顺序颠倒过来：**先按快门，描述候补。**

同时它不做「一处一记」。一次连续的记录会被归到**同一条备忘**里 —— 比如租房子，中介带看、合同条款、房东证件、房屋现状，一路拍下来都属于同一条备忘，最后补一句「XX 小区 2 栋，押一付三」就归档了。

## 功能

- **随手拍**：进入即为相机，单手持握、一键快门，拍照不打断思路
- **自动归类**：连续拍摄的照片自动进同一条「草稿」，写完描述即归档成备忘
- **像聊天一样补内容**：详情页用气泡时间线展示，可以继续补文字
- **两种补充方式**：`+` → 「拍一张」（应用内相机）或「从相册选择」（最多 9 张）
- **搜索与筛选**：按标题 / 描述 / 内容搜索，另有「全部 / 今天 / 待备注」快捷筛选和日历选日
- **导出到相册**：长按图片，或全屏预览页右上角，把重要的图存进系统相册
- **深色模式**：跟随系统，浅色/深色各有独立配色

## 截图

<!--
截图占位。把图片放到 docs/screenshots/ 后取消注释：

| 相机 | 备忘列表 | 详情 |
|:---:|:---:|:---:|
| ![](docs/screenshots/camera.png) | ![](docs/screenshots/list.png) | ![](docs/screenshots/detail.png) |
-->

## 技术栈

| 类别 | 技术 | 版本 |
| --- | --- | --- |
| 语言 | Kotlin | 2.3.0 |
| 构建 | AGP / Gradle | 8.13.2 / 8.14.3 |
| UI | Jetpack Compose（BOM 统一版本） | 2025.12.01 |
| UI | Material 3 | 随 BOM |
| 相机 | CameraX | 1.5.2 |
| 数据库 | Room（KSP） | 2.8.4 |
| 依赖注入 | Koin | 4.1.1 |
| 导航 | Navigation Compose（类型安全路由） | 2.9.6 |
| 图片 | Coil 3 / Telephoto 缩放 | 3.3.0 / 0.18.0 |
| 权限 | Accompanist Permissions | 0.37.3 |
| 适配 | minSdk 26（Android 8.0）→ targetSdk 36 | — |

## 快速开始

**环境要求**：JDK 21、Android SDK 36（compileSdk）。

```bash
git clone https://github.com/<your-name>/cameranote-android.git
cd cameranote-android
```

用 Android Studio 直接打开项目即可（会自动生成 `local.properties`）。命令行构建时，需要先手动创建 `local.properties` 并写入 SDK 路径：

```properties
sdk.dir=/path/to/Android/Sdk
```

```bash
# 调试包
./gradlew :app:assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk

# Release 包（已开启 R8 混淆 + 资源压缩）
./gradlew :app:assembleRelease

# 单元测试
./gradlew :app:testDebugUnitTest
```

> Windows 下把 `./gradlew` 换成 `gradlew.bat`。

## 项目结构

```text
app/src/main/java/com/mnn/cameranote/
├── App.kt / MainActivity.kt     入口，启动 Koin、套主题
├── di/AppModule.kt              Koin 依赖图
├── navigation/                  类型安全路由（kotlinx.serialization）
├── data/
│   ├── local/                   Room：实体、DAO、数据库与迁移
│   ├── manager/                 FileStore（照片文件）、GallerySaver（导出相册）
│   └── repository/              MessageRepository：跨表/跨存储的事务
├── screens/
│   ├── camera/                  相机页、拍照、备注面板
│   ├── messagelist/             备忘列表、搜索与筛选
│   └── messagedetail/           详情时间线、输入栏、补拍弹窗、全屏预览
├── ui/theme/                    配色与排版
├── ui/image/                    Coil 图片请求
└── util/                        日期格式化、目录工具
```

## 几个设计取舍

### 一条备忘 = 一条草稿 + N 条内容

数据库只有两张表：`messages`（备忘）和 `message_items`（内容，图片或文字）。

拍照时不会立刻生成一堆记录，而是**追加到当前草稿**（`messages.isTemp = 1`）。等你补上描述，草稿归档成正式备忘（`isTemp = 0`），下一张照片才会开启新的草稿。

这样「归档」是一个单条 UPDATE，不需要把多条记录合并来合并去 —— 也就没有合并到一半失败、照片变成孤儿的风险。

> 如果你一直不写描述，照片会持续进同一条草稿。想在没有描述的情况下另起一条，可以在详情页 **设置 → 修改主题** 改标题，会顺带归档。

### 数据库迁移

当前 schema 版本为 **3**，两条迁移都是手写并逐条验证过的：

| 版本 | 内容 |
| --- | --- |
| 1 → 2 | 修正 `deletedTIme` 列名拼写；补齐查询索引；清理历史孤儿数据 |
| 2 → 3 | 给 `message_items.messageId` 增加指向 `messages.id` 的外键（CASCADE） |

API 26 的 SQLite 版本较老，不支持 `ALTER TABLE ... RENAME COLUMN`，所以列名变更是用「建新表 → 拷数据 → 改名」的标准做法完成的。

另外 SQLite 的外键**默认不生效**，Room 也不会自动打开，因此 `AppDatabase.FOREIGN_KEY_CALLBACK` 会在打开数据库时显式启用 `PRAGMA foreign_keys`。

### 照片存在哪

照片写入应用私有外部目录，按年月分文件夹：

```text
Android/data/com.mnn.cameranote/files/Pictures/<年>/<月>/IMG_<时间戳>_<随机串>.jpg
```

这意味着：

- **卸载 App 时照片会一起删除**
- 系统相册里看不到它们（除非你手动导出）
- 想长期保留重要图片，请用「长按图片」或全屏页的「保存到相册」

删除一条备忘时，数据库记录软删除，同时对应磁盘文件也会被清理。

### 状态管理

所有页面都是 `StateFlow` 单向数据流：UI 只读状态、只发事件，写操作都下沉到 Repository 并在 `database.withTransaction {}` 里完成。

有个容易踩的坑值得一提：`popBackStack()` 会销毁 `NavBackStackEntry` 并取消它的 `viewModelScope`。所以「保存后返回」这类操作，本项目是在**写库完成之后才导航**，否则更新会被取消掉、表现为「改了没保存」。

## 权限说明

| 权限 | 用途 | 说明 |
| --- | --- | --- |
| `CAMERA` | 拍照 | 必需 |
| `WRITE_EXTERNAL_STORAGE` | 导出图片到系统相册 | 仅 Android 9 及以下需要；Android 10+ 走 MediaStore 无需权限 |

## 隐私

- **没有声明 `INTERNET` 权限**，网络能力在安装包层面就不存在
- 照片、备忘全部保存在本机
- 没有统计、埋点、广告 SDK
- `allowBackup=false`：不参与系统云备份，避免「备份恢复了数据库但照片没恢复」的错位状态

## 已知限制

- 只支持单机使用，没有云同步与多设备
- 数据出口目前只有「单张图片保存到相册」，还没有整条备忘的导出/备份
- 界面文案为中文硬编码，未做国际化
- 没有 UI 自动化测试（单元测试覆盖搜索词转义、UTC 日期换算等纯逻辑）

## Roadmap

- [ ] 整条备忘导出（Markdown / 压缩包）
- [ ] 批量选择图片导出到相册
- [ ] 补充 UI 测试与数据库迁移测试
- [ ] Baseline Profile，优化冷启动与相机首帧

## 参与贡献

欢迎提 Issue 和 PR。提交前请确认：

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

能通过。代码风格跟随现有文件（中文注释、Kotlin 官方风格）。

## 许可证

本项目基于 [MIT License](LICENSE) 开源。
