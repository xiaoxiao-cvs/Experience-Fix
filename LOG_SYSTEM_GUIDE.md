# Experience Bar Fix - 详细日志系统说明

## 📊 日志系统功能特性

### 🔧 配置选项

#### 基础配置
- `enabled`: 启用/禁用经验条修复功能
- `fixDelay`: 修复延迟时间（1-20游戏刻）
- `fallbackMethod`: 启用备用修复方法

#### 日志配置
- `debugLogging`: 启用调试日志记录
- `performanceLogging`: 启用性能监控日志
- `statisticsLogging`: 启用统计信息日志
- `statisticsInterval`: 统计信息记录间隔（1-60分钟）
- `playerTrackingLogging`: 启用玩家跟踪详细日志
- `errorDetailsLogging`: 启用详细错误信息日志

### 📈 统计信息

#### 修复统计
- 总尝试次数
- 成功/失败次数及百分比
- 各种事件类型（传送指令、维度变化、重生、实体传送）的修复次数
- 主要方法与备用方法的成功次数

#### 性能统计
- 平均修复时间
- 最长/最短修复时间
- 每次修复的详细耗时（当启用性能日志时）

#### 错误统计
- 各种错误类型的发生次数
- 详细的错误堆栈跟踪（当启用详细错误日志时）

#### 玩家统计
- 当前活跃玩家数量
- 待处理修复数量
- 玩家会话时长
- 每个玩家的修复次数

### 🎯 日志示例

#### 启动日志
```
[INFO] ==================================================
[INFO]          Experience Bar Fix v1.0.0 启动中...
[INFO] ==================================================
[INFO] 启动时间: 2025-08-05 14:30:25
[INFO] 模组ID: experiencebarfix
[INFO] Minecraft 版本: 1.20.1
[INFO] Forge 版本: 47.2.0+
```

#### 配置状态日志
```
[INFO] 当前配置状态:
[INFO]   - 功能启用: ✓ 是
[INFO]   - 修复延迟: 2 游戏刻
[INFO]   - 备用方法: ✓ 启用
[INFO]   - 维度变化修复: ✓ 启用
[INFO]   - 重生修复: ✓ 启用
[INFO]   - 调试日志: ✗ 禁用
[INFO]   - 性能日志: ✗ 禁用
[INFO]   - 统计日志: ✓ 启用
```

#### 修复过程日志（调试模式）
```
[DEBUG] 📩 接收到传送指令事件: 玩家=Steve, 时间=14:30:45.123
[DEBUG] 📝 安排经验条修复: 玩家=Steve, 来源=teleport command, 延迟=2刻, 时间=14:30:45.124
[DEBUG] ⏳ 递归延迟调度: 玩家=Steve, 剩余延迟=2
[DEBUG] 🔧 开始执行经验条修复: 玩家=Steve, 来源=teleport command, 时间=14:30:45.164
[DEBUG] 📊 经验值状态 [修复前]: 玩家=Steve, 总经验=1395, 等级=30, 进度=0.123
[DEBUG] ✅ 主要修复方法成功: 玩家=Steve, 方法=数据包发送
[DEBUG] 📊 经验值状态 [修复后]: 玩家=Steve, 总经验=1395, 等级=30, 进度=0.123
[INFO] ✅ 经验条修复成功: 玩家=Steve, 来源=teleport command
```

#### 统计信息日志
```
[INFO] === 经验条修复统计信息 ===
[INFO] 统计时间: 2025-08-05 14:40:25
[INFO] 📈 修复统计:
[INFO]   总尝试次数: 15
[INFO]   成功次数: 14 (93.3%)
[INFO]   失败次数: 1 (6.7%)
[INFO] 📊 事件类型分布:
[INFO]   传送指令: 8
[INFO]   维度变化: 4
[INFO]   玩家重生: 2
[INFO]   实体传送: 1
[INFO] 🔧 修复方法效果:
[INFO]   主要方法成功: 13
[INFO]   备用方法成功: 1
[INFO] ⏱️ 性能统计:
[INFO]   平均修复时间: 2.45ms
[INFO]   最长修复时间: 5.67ms
[INFO]   最短修复时间: 1.23ms
[INFO] 👥 当前活跃玩家: 3
[INFO] ⏳ 待处理修复: 0
```

#### 玩家跟踪日志
```
[INFO] 👤 玩家跟踪 [玩家登入]: Steve - 新会话已创建
[INFO] 👤 玩家跟踪 [事件接收]: Steve - 事件类型: 传送指令
[INFO] 👤 玩家跟踪 [修复安排]: Steve - 来源: teleport command, 延迟: 2刻
[INFO] 👤 玩家跟踪 [修复成功]: Steve - 方法: 数据包发送
[INFO] 👤 玩家跟踪 [玩家登出]: Steve - 会话时长: 25分钟, 修复次数: 3
```

#### 性能监控日志
```
[INFO] ⏱️ 性能: 数据包发送方法 耗时 1.25ms
[INFO] ⏱️ 修复性能: 耗时 2.34ms, 结果=成功
```

#### 错误日志
```
[WARN] ⚠️ 主要修复方法失败: 玩家=Steve, 错误=Connection reset
[ERROR] ❌ 详细错误 [数据包发送方法]: Connection reset
[ERROR] 异常类型: IOException
[ERROR] 堆栈跟踪: [详细堆栈信息...]
```

#### 服务器停止日志
```
[INFO] 📋 最终运行统计:
[INFO] 服务器运行时间: 120 分钟
[INFO] 👤 玩家会话摘要:
[INFO]   Steve: 25分钟 会话时长, 3 次修复
[INFO]   Alex: 45分钟 会话时长, 7 次修复
[INFO] === 经验条修复器运行结束 ===
```

### 🛠️ 配置建议

#### 开发/测试环境
```toml
debugLogging = true
performanceLogging = true
statisticsLogging = true
statisticsInterval = 5
playerTrackingLogging = true
errorDetailsLogging = true
```

#### 生产环境
```toml
debugLogging = false
performanceLogging = false
statisticsLogging = true
statisticsInterval = 30
playerTrackingLogging = false
errorDetailsLogging = true
```

### 📁 日志文件位置

日志将写入到标准的Minecraft日志文件中：
- `logs/latest.log` - 当前会话日志
- `logs/YYYY-MM-DD-N.log.gz` - 历史日志（压缩）

### 🔍 故障排除

如果遇到问题，请：
1. 启用 `debugLogging` 和 `errorDetailsLogging`
2. 重现问题
3. 检查日志中的错误信息
4. 查看统计信息以了解修复成功率

这个详细的日志系统将帮助您监控模组的性能，诊断问题，并了解经验条修复的效果。
