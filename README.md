# 廉颇时钟 🍅

一款简洁优雅的番茄时钟Android应用，帮助你管理学习时间，提高专注效率。

## 功能特性

- **番茄钟计时器** - 25分钟专注 + 5分钟休息的经典模式
- **自定义时间** - 自由调整工作/休息时长
- **任务管理** - 创建任务并关联番茄钟，追踪完成进度
- **统计报告** - 每日/每周专注时间图表，一目了然
- **提醒通知** - 计时结束时播放提示音
- **暗黑模式** - 支持系统主题自动切换

## 技术栈

| 技术 | 用途 |
|------|------|
| Kotlin | 开发语言 |
| Jetpack Compose | 声明式UI |
| Material3 | 设计系统 |
| Room | 本地数据库 |
| Hilt | 依赖注入 |
| WorkManager | 后台任务 |
| Navigation Compose | 页面导航 |

## 项目结构

```
app/src/main/java/com/lianpo/clock/
├── MainActivity.kt
├── LianpoClockApp.kt
├── navigation/          # 导航配置
├── ui/
│   ├── theme/           # 主题和样式
│   ├── timer/           # 计时器页面
│   ├── tasks/           # 任务管理页面
│   ├── statistics/      # 统计报告页面
│   └── settings/        # 设置页面
├── data/
│   ├── database/        # Room数据库
│   └── repository/      # 数据仓库
├── di/                  # 依赖注入
├── worker/              # 后台任务
└── util/                # 工具类
```

## 安装运行

1. 克隆项目
```bash
git clone https://github.com/xuyuanbao666/lianpo_clock.git
```

2. 使用 Android Studio 打开项目

3. 连接设备或启动模拟器

4. 点击 Run 运行

## 构建要求

- Android Studio Hedgehog 或更高版本
- JDK 17
- Android SDK 34
- Kotlin 1.9.0

## 截图

*待添加*

## 许可证

MIT License