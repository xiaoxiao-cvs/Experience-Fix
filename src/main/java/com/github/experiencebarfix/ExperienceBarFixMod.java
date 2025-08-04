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

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Mod(ExperienceBarFixMod.MODID)
public class ExperienceBarFixMod {
    
    public static final String MODID = "experiencebarfix";
    public static final String MOD_NAME = "Experience Bar Fix";
    public static final String VERSION = "1.0.0";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    
    // 日志格式化器
    private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // 统计信息调度器
    private static ScheduledExecutorService statisticsScheduler;
    
    public ExperienceBarFixMod() {
        logModStartup();
        
        // 注册模组加载的设置方法
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        
        // 注册配置文件
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        
        // 注册服务器和其他我们感兴趣的游戏事件
        MinecraftForge.EVENT_BUS.register(this);
        
        logModInitialized();
    }
    
    /**
     * 记录模组启动信息
     */
    private void logModStartup() {
        LOGGER.info("==================================================");
        LOGGER.info("         {} v{} 启动中...", MOD_NAME, VERSION);
        LOGGER.info("==================================================");
        LOGGER.info("启动时间: {}", getCurrentTimeFormatted());
        LOGGER.info("模组ID: {}", MODID);
        LOGGER.info("Minecraft 版本: 1.20.1");
        LOGGER.info("Forge 版本: 47.2.0+");
        LOGGER.info("Java 版本: {}", System.getProperty("java.version"));
        LOGGER.info("操作系统: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
    }
    
    /**
     * 记录模组初始化完成信息
     */
    private void logModInitialized() {
        LOGGER.info("模组构造函数完成，等待服务器设置...");
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        logSetupStart();
        
        try {
            // 注册经验条修复器
            MinecraftForge.EVENT_BUS.register(new ExperienceBarFixer());
            LOGGER.info("✓ 经验条修复器已成功注册");
            
            // 初始化统计信息调度器
            initializeStatisticsScheduler();
            
            logSetupComplete();
            
        } catch (Exception e) {
            LOGGER.error("❌ 模组设置过程中发生错误:", e);
            throw new RuntimeException("Experience Bar Fix 模组设置失败", e);
        }
    }
    
    /**
     * 记录设置开始信息
     */
    private void logSetupStart() {
        LOGGER.info("--------------------------------------------------");
        LOGGER.info("开始进行 {} 设置...", MOD_NAME);
        LOGGER.info("设置时间: {}", getCurrentTimeFormatted());
    }
    
    /**
     * 记录设置完成信息
     */
    private void logSetupComplete() {
        LOGGER.info("✓ {} 设置完成!", MOD_NAME);
        LOGGER.info("模组现在已准备好处理经验条修复!");
        logConfigurationStatus();
        LOGGER.info("--------------------------------------------------");
    }
    
    /**
     * 记录配置状态
     */
    private void logConfigurationStatus() {
        LOGGER.info("当前配置状态:");
        LOGGER.info("  - 功能启用: {}", Config.ENABLED.get() ? "✓ 是" : "✗ 否");
        LOGGER.info("  - 修复延迟: {} 游戏刻", Config.FIX_DELAY.get());
        LOGGER.info("  - 备用方法: {}", Config.FALLBACK_METHOD.get() ? "✓ 启用" : "✗ 禁用");
        LOGGER.info("  - 维度变化修复: {}", Config.FIX_DIMENSION_CHANGE.get() ? "✓ 启用" : "✗ 禁用");
        LOGGER.info("  - 重生修复: {}", Config.FIX_RESPAWN.get() ? "✓ 启用" : "✗ 禁用");
        LOGGER.info("  - 调试日志: {}", Config.DEBUG_LOGGING.get() ? "✓ 启用" : "✗ 禁用");
        LOGGER.info("  - 性能日志: {}", Config.PERFORMANCE_LOGGING.get() ? "✓ 启用" : "✗ 禁用");
        LOGGER.info("  - 统计日志: {}", Config.STATISTICS_LOGGING.get() ? "✓ 启用" : "✗ 禁用");
        LOGGER.info("  - 玩家跟踪日志: {}", Config.PLAYER_TRACKING_LOGGING.get() ? "✓ 启用" : "✗ 禁用");
        LOGGER.info("  - 详细错误日志: {}", Config.ERROR_DETAILS_LOGGING.get() ? "✓ 启用" : "✗ 禁用");
    }
    
    /**
     * 初始化统计信息调度器
     */
    private void initializeStatisticsScheduler() {
        if (Config.STATISTICS_LOGGING.get()) {
            statisticsScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "ExperienceBarFix-Statistics");
                t.setDaemon(true);
                return t;
            });
            
            long intervalMinutes = Config.STATISTICS_INTERVAL.get();
            statisticsScheduler.scheduleAtFixedRate(
                this::logStatistics, 
                intervalMinutes, 
                intervalMinutes, 
                TimeUnit.MINUTES
            );
            
            LOGGER.info("✓ 统计信息调度器已初始化，间隔: {} 分钟", intervalMinutes);
        } else {
            LOGGER.info("ℹ 统计信息日志已禁用");
        }
    }
    
    /**
     * 记录统计信息
     */
    private void logStatistics() {
        try {
            LOGGER.info("=== 经验条修复统计信息 ===");
            LOGGER.info("统计时间: {}", getCurrentTimeFormatted());
            ExperienceBarFixer.logStatistics();
            LOGGER.info("========================");
        } catch (Exception e) {
            LOGGER.error("记录统计信息时发生错误:", e);
        }
    }
    
    /**
     * 服务器启动事件
     */
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        LOGGER.info("🚀 服务器已启动 - {} 已激活!", MOD_NAME);
        LOGGER.info("服务器启动时间: {}", getCurrentTimeFormatted());
        ExperienceBarFixer.onServerStarted();
    }
    
    /**
     * 服务器停止事件
     */
    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("🛑 服务器正在停止 - {} 正在清理...", MOD_NAME);
        
        // 停止统计信息调度器
        if (statisticsScheduler != null && !statisticsScheduler.isShutdown()) {
            statisticsScheduler.shutdown();
            try {
                if (!statisticsScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    statisticsScheduler.shutdownNow();
                }
                LOGGER.info("✓ 统计信息调度器已停止");
            } catch (InterruptedException e) {
                LOGGER.warn("等待统计信息调度器停止时被中断", e);
                statisticsScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // 清理修复器资源
        ExperienceBarFixer.onServerStopping();
        
        // 记录最终统计信息
        logFinalStatistics();
        
        LOGGER.info("✓ {} 清理完成", MOD_NAME);
        LOGGER.info("服务器停止时间: {}", getCurrentTimeFormatted());
    }
    
    /**
     * 记录最终统计信息
     */
    private void logFinalStatistics() {
        LOGGER.info("=== 最终统计信息 ===");
        ExperienceBarFixer.logFinalStatistics();
        LOGGER.info("==================");
    }
    
    /**
     * 获取格式化的当前时间
     */
    public static String getCurrentTimeFormatted() {
        return LocalDateTime.now().format(LOG_TIME_FORMAT);
    }
    
    /**
     * 记录性能信息的便捷方法
     */
    public static void logPerformance(String operation, long durationNanos) {
        if (Config.PERFORMANCE_LOGGING.get()) {
            double durationMs = durationNanos / 1_000_000.0;
            LOGGER.info("⏱️ 性能: {} 耗时 {:.2f}ms", operation, durationMs);
        }
    }
    
    /**
     * 记录详细错误信息的便捷方法
     */
    public static void logDetailedError(String context, Exception e) {
        if (Config.ERROR_DETAILS_LOGGING.get()) {
            LOGGER.error("❌ 详细错误 [{}]: {}", context, e.getMessage());
            LOGGER.error("异常类型: {}", e.getClass().getSimpleName());
            LOGGER.error("堆栈跟踪:", e);
        } else {
            LOGGER.error("❌ 错误 [{}]: {}", context, e.getMessage());
        }
    }
    
    /**
     * 记录玩家跟踪信息的便捷方法
     */
    public static void logPlayerTracking(String action, String playerName, String details) {
        if (Config.PLAYER_TRACKING_LOGGING.get()) {
            LOGGER.info("👤 玩家跟踪 [{}]: {} - {}", action, playerName, details);
        }
    }
}
