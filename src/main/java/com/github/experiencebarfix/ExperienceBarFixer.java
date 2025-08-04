/*
 * Experience Bar Fix - Fixes experience bar disappearing after teleportation in Minecraft 1.20.1
 * Copyright (C) 2025 Experience Bar Fix Team
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.experiencebarfix;

import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Mod.EventBusSubscriber(modid = ExperienceBarFixMod.MODID)
public class ExperienceBarFixer {
    
    // 玩家修复跟踪
    private static final Set<UUID> pendingFix = ConcurrentHashMap.newKeySet();
    
    // 统计信息
    private static final AtomicInteger totalFixAttempts = new AtomicInteger(0);
    private static final AtomicInteger successfulFixes = new AtomicInteger(0);
    private static final AtomicInteger failedFixes = new AtomicInteger(0);
    private static final AtomicInteger teleportCommandFixes = new AtomicInteger(0);
    private static final AtomicInteger dimensionChangeFixes = new AtomicInteger(0);
    private static final AtomicInteger respawnFixes = new AtomicInteger(0);
    private static final AtomicInteger entityTeleportFixes = new AtomicInteger(0);
    private static final AtomicInteger primaryMethodSuccess = new AtomicInteger(0);
    private static final AtomicInteger fallbackMethodSuccess = new AtomicInteger(0);
    
    // 性能监控
    private static final AtomicLong totalFixDuration = new AtomicLong(0);
    private static final AtomicLong longestFixDuration = new AtomicLong(0);
    private static final AtomicLong shortestFixDuration = new AtomicLong(Long.MAX_VALUE);
    
    // 玩家会话跟踪
    private static final Map<UUID, PlayerSession> playerSessions = new ConcurrentHashMap<>();
    
    // 错误跟踪
    private static final Map<String, AtomicInteger> errorCounts = new ConcurrentHashMap<>();
    
    // 时间格式化器
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    // 服务器启动时间
    private static LocalDateTime serverStartTime;
    
    /**
     * 玩家会话信息类
     */
    private static class PlayerSession {
        final String playerName;
        final LocalDateTime joinTime;
        int fixesApplied = 0;
        LocalDateTime lastFixTime;
        
        PlayerSession(String playerName) {
            this.playerName = playerName;
            this.joinTime = LocalDateTime.now();
        }
    }
    
    /**
     * 处理传送指令 (/tp, /teleport 等)
     */
    @SubscribeEvent
    public static void onTeleportCommand(EntityTeleportEvent.TeleportCommand event) {
        if (Config.ENABLED.get()) {
            logEventReceived("传送指令", event.getEntity());
            handleTeleport(event.getEntity(), "teleport command");
            teleportCommandFixes.incrementAndGet();
        }
    }
    
    /**
     * 处理维度变化 (下界、末地等)
     */
    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (Config.ENABLED.get() && Config.FIX_DIMENSION_CHANGE.get()) {
            logEventReceived("维度变化", event.getEntity());
            handleTeleport(event.getEntity(), "dimension change");
            dimensionChangeFixes.incrementAndGet();
        }
    }
    
    /**
     * 处理玩家重生
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (Config.ENABLED.get() && Config.FIX_RESPAWN.get()) {
            logEventReceived("玩家重生", event.getEntity());
            handleTeleport(event.getEntity(), "respawn");
            respawnFixes.incrementAndGet();
        }
    }
    
    /**
     * 处理任何可能影响玩家的实体传送
     */
    @SubscribeEvent
    public static void onEntityTeleport(EntityTeleportEvent event) {
        if (Config.ENABLED.get() && event.getEntity() instanceof ServerPlayer) {
            logEventReceived("实体传送", event.getEntity());
            handleTeleport(event.getEntity(), "entity teleport");
            entityTeleportFixes.incrementAndGet();
        }
    }
    
    /**
     * 记录事件接收信息
     */
    private static void logEventReceived(String eventType, Entity entity) {
        if (entity instanceof ServerPlayer player) {
            if (Config.DEBUG_LOGGING.get()) {
                ExperienceBarFixMod.LOGGER.debug("📩 接收到{}事件: 玩家={}, 时间={}", 
                    eventType, player.getName().getString(), LocalDateTime.now().format(TIME_FORMAT));
            }
            
            ExperienceBarFixMod.logPlayerTracking("事件接收", player.getName().getString(), 
                String.format("事件类型: %s", eventType));
        }
    }
    
    /**
     * 核心传送处理方法
     */
    private static void handleTeleport(Entity entity, String source) {
        if (!(entity instanceof ServerPlayer player)) {
            if (Config.DEBUG_LOGGING.get()) {
                ExperienceBarFixMod.LOGGER.debug("🚫 忽略非玩家实体的传送事件: {}", entity.getClass().getSimpleName());
            }
            return;
        }
        
        UUID playerId = player.getUUID();
        String playerName = player.getName().getString();
        
        // 性能监控开始
        long startTime = System.nanoTime();
        
        // 防止对同一玩家进行重复修复
        if (pendingFix.contains(playerId)) {
            if (Config.DEBUG_LOGGING.get()) {
                ExperienceBarFixMod.LOGGER.debug("⏸️ 经验条修复已在等待队列中: 玩家={}, 来源={}", 
                    playerName, source);
            }
            
            ExperienceBarFixMod.logPlayerTracking("重复修复阻止", playerName, 
                String.format("来源: %s", source));
            return;
        }
        
        pendingFix.add(playerId);
        totalFixAttempts.incrementAndGet();
        
        // 更新玩家会话信息
        updatePlayerSession(playerId, playerName);
        
        if (Config.DEBUG_LOGGING.get()) {
            ExperienceBarFixMod.LOGGER.debug("📝 安排经验条修复: 玩家={}, 来源={}, 延迟={}刻, 时间={}", 
                playerName, source, Config.FIX_DELAY.get(), LocalDateTime.now().format(TIME_FORMAT));
        }
        
        ExperienceBarFixMod.logPlayerTracking("修复安排", playerName, 
            String.format("来源: %s, 延迟: %d刻", source, Config.FIX_DELAY.get()));
        
        MinecraftServer server = player.getServer();
        if (server != null) {
            // 使用可配置的延迟来安排修复
            scheduleFixWithDelay(server, player, playerId, Config.FIX_DELAY.get(), source, startTime);
        } else {
            ExperienceBarFixMod.LOGGER.warn("⚠️ 无法获取服务器实例，玩家: {}", playerName);
            pendingFix.remove(playerId);
            failedFixes.incrementAndGet();
            incrementErrorCount("无服务器实例");
        }
    }
    
    /**
     * 按指定延迟安排经验条修复
     */
    private static void scheduleFixWithDelay(MinecraftServer server, ServerPlayer player, UUID playerId, 
                                           int delay, String source, long startTime) {
        if (Config.DEBUG_LOGGING.get()) {
            ExperienceBarFixMod.LOGGER.debug("⏳ 递归延迟调度: 玩家={}, 剩余延迟={}", 
                player.getName().getString(), delay);
        }
        
        // 首次延迟以确保传送完成
        server.execute(() -> {
            if (delay <= 1) {
                performFix(player, playerId, source, startTime);
            } else {
                // 如果配置了额外延迟
                scheduleFixWithDelay(server, player, playerId, delay - 1, source, startTime);
            }
        });
    }
    
    /**
     * 执行实际的经验条修复
     */
    private static void performFix(ServerPlayer player, UUID playerId, String source, long startTime) {
        String playerName = player.getName().getString();
        
        try {
            if (Config.DEBUG_LOGGING.get()) {
                ExperienceBarFixMod.LOGGER.debug("🔧 开始执行经验条修复: 玩家={}, 来源={}, 时间={}", 
                    playerName, source, LocalDateTime.now().format(TIME_FORMAT));
            }
            
            // 检查玩家是否仍在线且有效
            if (player == null || player.hasDisconnected()) {
                if (Config.DEBUG_LOGGING.get()) {
                    ExperienceBarFixMod.LOGGER.debug("❌ 玩家已离线或无效: {}", playerName);
                }
                pendingFix.remove(playerId);
                failedFixes.incrementAndGet();
                incrementErrorCount("玩家离线");
                recordPerformance(startTime, false);
                return;
            }
            
            boolean fixSuccessful = false;
            
            // 记录修复前的经验值状态
            logExperienceState(player, "修复前");
            
            // 方法1: 重新发送经验数据包 (主要方法)
            try {
                long methodStartTime = System.nanoTime();
                
                player.connection.send(new ClientboundSetExperiencePacket(
                    player.experienceProgress, 
                    player.totalExperience, 
                    player.experienceLevel
                ));
                fixSuccessful = true;
                primaryMethodSuccess.incrementAndGet();
                
                long methodDuration = System.nanoTime() - methodStartTime;
                ExperienceBarFixMod.logPerformance("数据包发送方法", methodDuration);
                
                if (Config.DEBUG_LOGGING.get()) {
                    ExperienceBarFixMod.LOGGER.debug("✅ 主要修复方法成功: 玩家={}, 方法=数据包发送", playerName);
                }
                
                ExperienceBarFixMod.logPlayerTracking("修复成功", playerName, "方法: 数据包发送");
                
            } catch (Exception e) {
                ExperienceBarFixMod.LOGGER.warn("⚠️ 主要修复方法失败: 玩家={}, 错误={}", playerName, e.getMessage());
                incrementErrorCount("数据包发送失败");
                
                if (Config.ERROR_DETAILS_LOGGING.get()) {
                    ExperienceBarFixMod.logDetailedError("数据包发送方法", e);
                }
            }
            
            // 方法2: 备用方法 - 临时经验值操作
            if (!fixSuccessful && Config.FALLBACK_METHOD.get()) {
                if (Config.DEBUG_LOGGING.get()) {
                    ExperienceBarFixMod.LOGGER.debug("🔄 尝试备用修复方法: 玩家={}", playerName);
                }
                
                try {
                    long methodStartTime = System.nanoTime();
                    
                    if (player.totalExperience > 0) {
                        int backupTotal = player.totalExperience;
                        int backupLevel = player.experienceLevel;
                        float backupProgress = player.experienceProgress;
                        
                        if (Config.DEBUG_LOGGING.get()) {
                            ExperienceBarFixMod.LOGGER.debug("💾 备份经验数据: 总经验={}, 等级={}, 进度={:.3f}", 
                                backupTotal, backupLevel, backupProgress);
                        }
                        
                        // 临时重置并恢复经验值
                        player.totalExperience = 0;
                        player.experienceLevel = 0;
                        player.experienceProgress = 0.0f;
                        player.giveExperiencePoints(backupTotal);
                        
                        // 确保进度正确恢复
                        if (player.experienceLevel == backupLevel) {
                            player.experienceProgress = backupProgress;
                        }
                        
                        if (Config.DEBUG_LOGGING.get()) {
                            ExperienceBarFixMod.LOGGER.debug("🔄 经验数据已恢复: 总经验={}, 等级={}, 进度={:.3f}", 
                                player.totalExperience, player.experienceLevel, player.experienceProgress);
                        }
                        
                        fixSuccessful = true;
                        fallbackMethodSuccess.incrementAndGet();
                        
                        long methodDuration = System.nanoTime() - methodStartTime;
                        ExperienceBarFixMod.logPerformance("经验重置方法", methodDuration);
                        
                        if (Config.DEBUG_LOGGING.get()) {
                            ExperienceBarFixMod.LOGGER.debug("✅ 备用修复方法成功: 玩家={}, 方法=经验重置", playerName);
                        }
                        
                        ExperienceBarFixMod.logPlayerTracking("修复成功", playerName, "方法: 经验重置");
                        
                    } else {
                        // 对于经验值为0的玩家，只需触发刷新
                        player.giveExperiencePoints(0);
                        fixSuccessful = true;
                        fallbackMethodSuccess.incrementAndGet();
                        
                        if (Config.DEBUG_LOGGING.get()) {
                            ExperienceBarFixMod.LOGGER.debug("✅ 零经验玩家修复成功: 玩家={}", playerName);
                        }
                        
                        ExperienceBarFixMod.logPlayerTracking("修复成功", playerName, "方法: 零经验刷新");
                    }
                } catch (Exception e) {
                    ExperienceBarFixMod.LOGGER.error("❌ 备用修复方法失败: 玩家={}, 错误={}", playerName, e.getMessage());
                    incrementErrorCount("经验重置失败");
                    
                    if (Config.ERROR_DETAILS_LOGGING.get()) {
                        ExperienceBarFixMod.logDetailedError("备用修复方法", e);
                    }
                }
            }
            
            // 记录修复后的经验值状态
            if (fixSuccessful) {
                logExperienceState(player, "修复后");
            }
            
            // 更新统计信息
            if (fixSuccessful) {
                successfulFixes.incrementAndGet();
                updatePlayerSessionFix(playerId);
                recordPerformance(startTime, true);
                
                ExperienceBarFixMod.LOGGER.info("✅ 经验条修复成功: 玩家={}, 来源={}", playerName, source);
            } else {
                failedFixes.incrementAndGet();
                recordPerformance(startTime, false);
                incrementErrorCount("所有方法失败");
                
                ExperienceBarFixMod.LOGGER.warn("❌ 所有修复方法均失败: 玩家={}, 来源={}", playerName, source);
                ExperienceBarFixMod.logPlayerTracking("修复失败", playerName, "所有方法均失败");
            }
            
        } catch (Exception e) {
            failedFixes.incrementAndGet();
            recordPerformance(startTime, false);
            incrementErrorCount("意外错误");
            
            ExperienceBarFixMod.LOGGER.error("💥 修复经验条时发生意外错误: 玩家={}, 来源={}", playerName, source);
            ExperienceBarFixMod.logDetailedError("经验条修复", e);
            
        } finally {
            // 始终从待处理集合中移除
            pendingFix.remove(playerId);
            
            if (Config.DEBUG_LOGGING.get()) {
                ExperienceBarFixMod.LOGGER.debug("🧹 清理完成: 玩家={} 已从待处理队列移除", playerName);
            }
        }
    }
    
    /**
     * 断开连接玩家的清理方法
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();
        String playerName = event.getEntity().getName().getString();
        
        // 从待处理修复中移除
        boolean wasPending = pendingFix.remove(playerId);
        
        // 清理玩家会话
        PlayerSession session = playerSessions.remove(playerId);
        
        if (Config.DEBUG_LOGGING.get()) {
            ExperienceBarFixMod.LOGGER.debug("🚪 玩家登出清理: 玩家={}, 待处理修复={}, 会话清理={}", 
                playerName, wasPending ? "是" : "否", session != null ? "是" : "否");
        }
        
        ExperienceBarFixMod.logPlayerTracking("玩家登出", playerName, 
            String.format("会话时长: %s, 修复次数: %d", 
                session != null ? getSessionDuration(session) : "未知", 
                session != null ? session.fixesApplied : 0));
    }
    
    /**
     * 玩家登入事件处理
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        UUID playerId = event.getEntity().getUUID();
        String playerName = event.getEntity().getName().getString();
        
        // 创建新的玩家会话
        playerSessions.put(playerId, new PlayerSession(playerName));
        
        if (Config.DEBUG_LOGGING.get()) {
            ExperienceBarFixMod.LOGGER.debug("🔑 玩家登入: 玩家={}, 时间={}", 
                playerName, LocalDateTime.now().format(TIME_FORMAT));
        }
        
        ExperienceBarFixMod.logPlayerTracking("玩家登入", playerName, "新会话已创建");
    }
    
    /**
     * 记录经验值状态
     */
    private static void logExperienceState(ServerPlayer player, String context) {
        if (Config.DEBUG_LOGGING.get()) {
            ExperienceBarFixMod.LOGGER.debug("📊 经验值状态 [{}]: 玩家={}, 总经验={}, 等级={}, 进度={:.3f}", 
                context, player.getName().getString(), 
                player.totalExperience, player.experienceLevel, player.experienceProgress);
        }
    }
    
    /**
     * 更新玩家会话信息
     */
    private static void updatePlayerSession(UUID playerId, String playerName) {
        PlayerSession session = playerSessions.get(playerId);
        if (session == null) {
            session = new PlayerSession(playerName);
            playerSessions.put(playerId, session);
        }
    }
    
    /**
     * 更新玩家会话修复计数
     */
    private static void updatePlayerSessionFix(UUID playerId) {
        PlayerSession session = playerSessions.get(playerId);
        if (session != null) {
            session.fixesApplied++;
            session.lastFixTime = LocalDateTime.now();
        }
    }
    
    /**
     * 记录性能数据
     */
    private static void recordPerformance(long startTime, boolean successful) {
        long duration = System.nanoTime() - startTime;
        totalFixDuration.addAndGet(duration);
        
        // 更新最长和最短修复时间
        long current = longestFixDuration.get();
        while (duration > current && !longestFixDuration.compareAndSet(current, duration)) {
            current = longestFixDuration.get();
        }
        
        current = shortestFixDuration.get();
        while (duration < current && !shortestFixDuration.compareAndSet(current, duration)) {
            current = shortestFixDuration.get();
        }
        
        if (Config.PERFORMANCE_LOGGING.get()) {
            double durationMs = duration / 1_000_000.0;
            ExperienceBarFixMod.LOGGER.info("⏱️ 修复性能: 耗时 {:.2f}ms, 结果={}", 
                durationMs, successful ? "成功" : "失败");
        }
    }
    
    /**
     * 增加错误计数
     */
    private static void incrementErrorCount(String errorType) {
        errorCounts.computeIfAbsent(errorType, k -> new AtomicInteger(0)).incrementAndGet();
    }
    
    /**
     * 获取会话持续时间
     */
    private static String getSessionDuration(PlayerSession session) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(session.joinTime, now).toMinutes();
        return String.format("%d分钟", minutes);
    }
    
    /**
     * 服务器启动时调用
     */
    public static void onServerStarted() {
        serverStartTime = LocalDateTime.now();
        ExperienceBarFixMod.LOGGER.info("🚀 经验条修复器已在服务器启动时激活");
        ExperienceBarFixMod.LOGGER.info("服务器启动时间: {}", serverStartTime.format(TIME_FORMAT));
        
        // 重置统计信息
        resetStatistics();
    }
    
    /**
     * 服务器停止时调用
     */
    public static void onServerStopping() {
        ExperienceBarFixMod.LOGGER.info("🛑 经验条修复器正在清理资源...");
        
        // 清理待处理的修复
        int pendingCount = pendingFix.size();
        pendingFix.clear();
        
        // 清理玩家会话
        int sessionCount = playerSessions.size();
        playerSessions.clear();
        
        ExperienceBarFixMod.LOGGER.info("✓ 已清理 {} 个待处理修复和 {} 个玩家会话", pendingCount, sessionCount);
    }
    
    /**
     * 重置统计信息
     */
    private static void resetStatistics() {
        totalFixAttempts.set(0);
        successfulFixes.set(0);
        failedFixes.set(0);
        teleportCommandFixes.set(0);
        dimensionChangeFixes.set(0);
        respawnFixes.set(0);
        entityTeleportFixes.set(0);
        primaryMethodSuccess.set(0);
        fallbackMethodSuccess.set(0);
        totalFixDuration.set(0);
        longestFixDuration.set(0);
        shortestFixDuration.set(Long.MAX_VALUE);
        errorCounts.clear();
        
        ExperienceBarFixMod.LOGGER.info("📊 统计信息已重置");
    }
    
    /**
     * 记录统计信息
     */
    public static void logStatistics() {
        if (!Config.STATISTICS_LOGGING.get()) {
            return;
        }
        
        int totalAttempts = totalFixAttempts.get();
        int successful = successfulFixes.get();
        int failed = failedFixes.get();
        
        if (totalAttempts == 0) {
            ExperienceBarFixMod.LOGGER.info("📊 暂无修复尝试");
            return;
        }
        
        double successRate = (double) successful / totalAttempts * 100;
        
        ExperienceBarFixMod.LOGGER.info("📈 修复统计:");
        ExperienceBarFixMod.LOGGER.info("  总尝试次数: {}", totalAttempts);
        ExperienceBarFixMod.LOGGER.info("  成功次数: {} ({:.1f}%)", successful, successRate);
        ExperienceBarFixMod.LOGGER.info("  失败次数: {} ({:.1f}%)", failed, 100 - successRate);
        
        ExperienceBarFixMod.LOGGER.info("📊 事件类型分布:");
        ExperienceBarFixMod.LOGGER.info("  传送指令: {}", teleportCommandFixes.get());
        ExperienceBarFixMod.LOGGER.info("  维度变化: {}", dimensionChangeFixes.get());
        ExperienceBarFixMod.LOGGER.info("  玩家重生: {}", respawnFixes.get());
        ExperienceBarFixMod.LOGGER.info("  实体传送: {}", entityTeleportFixes.get());
        
        ExperienceBarFixMod.LOGGER.info("🔧 修复方法效果:");
        ExperienceBarFixMod.LOGGER.info("  主要方法成功: {}", primaryMethodSuccess.get());
        ExperienceBarFixMod.LOGGER.info("  备用方法成功: {}", fallbackMethodSuccess.get());
        
        // 性能统计
        if (totalAttempts > 0) {
            long avgDuration = totalFixDuration.get() / totalAttempts;
            double avgMs = avgDuration / 1_000_000.0;
            double longestMs = longestFixDuration.get() / 1_000_000.0;
            double shortestMs = shortestFixDuration.get() == Long.MAX_VALUE ? 0 : shortestFixDuration.get() / 1_000_000.0;
            
            ExperienceBarFixMod.LOGGER.info("⏱️ 性能统计:");
            ExperienceBarFixMod.LOGGER.info("  平均修复时间: {:.2f}ms", avgMs);
            ExperienceBarFixMod.LOGGER.info("  最长修复时间: {:.2f}ms", longestMs);
            ExperienceBarFixMod.LOGGER.info("  最短修复时间: {:.2f}ms", shortestMs);
        }
        
        // 错误统计
        if (!errorCounts.isEmpty()) {
            ExperienceBarFixMod.LOGGER.info("❌ 错误统计:");
            errorCounts.forEach((errorType, count) -> 
                ExperienceBarFixMod.LOGGER.info("  {}: {}", errorType, count.get()));
        }
        
        // 活跃玩家统计
        ExperienceBarFixMod.LOGGER.info("👥 当前活跃玩家: {}", playerSessions.size());
        ExperienceBarFixMod.LOGGER.info("⏳ 待处理修复: {}", pendingFix.size());
    }
    
    /**
     * 记录最终统计信息
     */
    public static void logFinalStatistics() {
        ExperienceBarFixMod.LOGGER.info("📋 最终运行统计:");
        
        if (serverStartTime != null) {
            long uptimeMinutes = java.time.Duration.between(serverStartTime, LocalDateTime.now()).toMinutes();
            ExperienceBarFixMod.LOGGER.info("服务器运行时间: {} 分钟", uptimeMinutes);
        }
        
        logStatistics();
        
        // 记录玩家会话摘要
        if (!playerSessions.isEmpty()) {
            ExperienceBarFixMod.LOGGER.info("👤 玩家会话摘要:");
            playerSessions.values().forEach(session -> {
                String duration = getSessionDuration(session);
                ExperienceBarFixMod.LOGGER.info("  {}: {} 会话时长, {} 次修复", 
                    session.playerName, duration, session.fixesApplied);
            });
        }
        
        ExperienceBarFixMod.LOGGER.info("=== 经验条修复器运行结束 ===");
    }
}
