# Experience Bar Fix

[![Build and Release](https://github.com/xiaoxiao-cvs/ExperienceBar-Fix/actions/workflows/build.yml/badge.svg)](https://github.com/xiaoxiao-cvs/ExperienceBar-Fix/actions/workflows/build.yml)
[![Security Scan](https://github.com/xiaoxiao-cvs/ExperienceBar-Fix/actions/workflows/security-scan.yml/badge.svg)](https://github.com/xiaoxiao-cvs/ExperienceBar-Fix/actions/workflows/security-scan.yml)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.20.1-green.svg)](https://minecraft.net/)
[![Forge Version](https://img.shields.io/badge/Forge-47.2.0-orange.svg)](https://minecraftforge.net/)
[![GitHub release](https://img.shields.io/github/v/release/xiaoxiao-cvs/ExperienceBar-Fix)](https://github.com/xiaoxiao-cvs/ExperienceBar-Fix/releases)

一个用于修复 Minecraft 1.20.1 中传送后经验条消失问题的 Forge Mod。

## 问题描述

在 Minecraft 1.20.1 中，当玩家执行任意传送操作（如 `/tp` 命令、维度切换、重生等）后，经验条的视觉显示会消失。虽然经验数据本身没有丢失，但玩家需要获得经验才能重新显示经验条。

## 解决方案

此 Mod 使用健壮的事件监听系统，自动检测各种传送事件并刷新经验条显示：

- **传送命令监听**: `/tp`, `/teleport` 等命令
- **维度切换监听**: 前往下界、末地等
- **重生事件监听**: 玩家死亡重生
- **通用传送监听**: 其他可能的传送方式

## 特性

-  **多重保障**: 主要修复方法 + 备用方法确保成功率
-  **高性能**: 只在需要时执行，不影响游戏性能
-  **可配置**: 丰富的配置选项，满足不同需求
-  **兼容性**: 不依赖额外存储，兼容性强
-  **容错性**: 异常处理完善，不会导致崩溃
-  **详细日志**: 完整的日志系统，包含统计、性能监控和错误跟踪
-  **玩家跟踪**: 实时监控玩家活动和修复效果
-  **性能分析**: 详细的修复时间分析和性能报告

## 安装

1. 确保安装了 Minecraft 1.20.1 和 Forge 47.2.0+
2. 下载最新版本的 Mod jar 文件
3. 将 jar 文件放入 `mods` 文件夹
4. 启动游戏

## 配置

配置文件位于 `config/experiencebarfix-common.toml`：

### 基础配置
```toml
[general]
# 启用经验条修复
enabled = true
# 修复延迟（tick）
fixDelay = 2
# 启用备用修复方法
fallbackMethod = true
# 修复维度切换
fixDimensionChange = true
# 修复重生
fixRespawn = true
```

### 日志配置
```toml
[logging]
# 启用调试日志
debugLogging = false
# 启用性能监控日志
performanceLogging = false
# 启用统计信息日志
statisticsLogging = true
# 统计信息记录间隔（分钟）
statisticsInterval = 10
# 启用玩家跟踪详细日志
playerTrackingLogging = false
# 启用详细错误信息日志
errorDetailsLogging = true
```

**详细日志系统说明请查看**: [LOG_SYSTEM_GUIDE.md](LOG_SYSTEM_GUIDE.md)

## 开发

### 环境要求

- JDK 17+
- Gradle 8.1+

### 构建项目

```bash
# Windows
.\gradlew build

# Linux/Mac
./gradlew build
```

### 开发环境

```bash
# 运行客户端
.\gradlew runClient

# 运行服务器
.\gradlew runServer
```

## 技术实现

### 核心原理

1. **事件监听**: 监听多种传送相关事件
2. **数据包重发**: 重新发送经验数据包刷新客户端显示
3. **备用方案**: 临时重置经验后恢复，强制刷新
4. **异步处理**: 使用服务器调度器避免阻塞

### 代码结构

```
src/main/java/com/github/experiencebarfix/
├── ExperienceBarFixMod.java      # 主 Mod 类
├── Config.java                   # 配置管理
└── ExperienceBarFixer.java       # 核心修复逻辑
```

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

本项目采用 GNU General Public License v3.0 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

本程序是自由软件：您可以在自由软件基金会发布的 GNU 通用公共许可证第 3 版或（根据您的选择）任何更高版本的条款下重新分发和/或修改它。

发布此程序是希望它有用，但没有任何保证；甚至没有适销性或特定用途适用性的隐含保证。有关更多详细信息，请参阅 GNU 通用公共许可证。

## 致谢

- Minecraft Forge 团队
- Minecraft 社区
- 所有贡献者

## 相关链接

- [Issue 追踪](https://github.com/xiaoxiao-cvs/Experience-Fix/issues)
- [Minecraft Forge](https://minecraftforge.net/)
- [Mod 发布页面](https://www.curseforge.com/minecraft/mc-mods/experience-bar-fix)

---

如果这个 Mod 帮助了您，请考虑给项目一个 ⭐ 星标！
