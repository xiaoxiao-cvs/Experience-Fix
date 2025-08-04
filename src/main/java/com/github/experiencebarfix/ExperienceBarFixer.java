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

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = ExperienceBarFixMod.MODID)
public class ExperienceBarFixer {
    
    private static final Set<UUID> pendingFix = ConcurrentHashMap.newKeySet();
    
    /**
     * 处理传送指令 (/tp, /teleport 等)
     */
    @SubscribeEvent
    public static void onTeleportCommand(EntityTeleportEvent.TeleportCommand event) {
        if (Config.ENABLED.get()) {
            handleTeleport(event.getEntity(), "teleport command");
        }
    }
    
    /**
     * 处理维度变化 (下界、末地等)
     */
    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (Config.ENABLED.get() && Config.FIX_DIMENSION_CHANGE.get()) {
            handleTeleport(event.getEntity(), "dimension change");
        }
    }
    
    /**
     * 处理玩家重生
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (Config.ENABLED.get() && Config.FIX_RESPAWN.get()) {
            handleTeleport(event.getEntity(), "respawn");
        }
    }
    
    /**
     * 处理任何可能影响玩家的实体传送
     */
    @SubscribeEvent
    public static void onEntityTeleport(EntityTeleportEvent event) {
        if (Config.ENABLED.get() && event.getEntity() instanceof ServerPlayer) {
            handleTeleport(event.getEntity(), "entity teleport");
        }
    }
    
    /**
     * 核心传送处理方法
     */
    private static void handleTeleport(Entity entity, String source) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        
        UUID playerId = player.getUUID();
        
        // 防止对同一玩家进行重复修复
        if (pendingFix.contains(playerId)) {
            if (Config.DEBUG_LOGGING.get()) {
                ExperienceBarFixMod.LOGGER.debug("Experience bar fix already pending for player: {}", 
                    player.getName().getString());
            }
            return;
        }
        
        pendingFix.add(playerId);
        
        if (Config.DEBUG_LOGGING.get()) {
            ExperienceBarFixMod.LOGGER.debug("Scheduling experience bar fix for player: {} (source: {})", 
                player.getName().getString(), source);
        }
        
        MinecraftServer server = player.getServer();
        if (server != null) {
            // 使用可配置的延迟来安排修复
            scheduleFixWithDelay(server, player, playerId, Config.FIX_DELAY.get());
        }
    }
    
    /**
     * 按指定延迟安排经验条修复
     */
    private static void scheduleFixWithDelay(MinecraftServer server, ServerPlayer player, UUID playerId, int delay) {
        // 首次延迟以确保传送完成
        server.execute(() -> {
            if (delay <= 1) {
                performFix(player, playerId);
            } else {
                // 如果配置了额外延迟
                scheduleFixWithDelay(server, player, playerId, delay - 1);
            }
        });
    }
    
    /**
     * 执行实际的经验条修复
     */
    private static void performFix(ServerPlayer player, UUID playerId) {
        try {
            // 检查玩家是否仍在线且有效
            if (player == null || player.hasDisconnected()) {
                pendingFix.remove(playerId);
                return;
            }
            
            boolean fixSuccessful = false;
            
            // 方法1: 重新发送经验数据包 (主要方法)
            try {
                player.connection.send(new ClientboundSetExperiencePacket(
                    player.experienceProgress, 
                    player.totalExperience, 
                    player.experienceLevel
                ));
                fixSuccessful = true;
                
                if (Config.DEBUG_LOGGING.get()) {
                    ExperienceBarFixMod.LOGGER.debug("Successfully fixed experience bar for player: {} using packet method", 
                        player.getName().getString());
                }
            } catch (Exception e) {
                ExperienceBarFixMod.LOGGER.warn("Primary fix method failed for player: {}, error: {}", 
                    player.getName().getString(), e.getMessage());
            }
            
            // 方法2: 备用方法 - 临时经验值操作
            if (!fixSuccessful && Config.FALLBACK_METHOD.get()) {
                try {
                    if (player.totalExperience > 0) {
                        int backupTotal = player.totalExperience;
                        int backupLevel = player.experienceLevel;
                        float backupProgress = player.experienceProgress;
                        
                        // 临时重置并恢复经验值
                        player.totalExperience = 0;
                        player.experienceLevel = 0;
                        player.experienceProgress = 0.0f;
                        player.giveExperiencePoints(backupTotal);
                        
                        // 确保进度正确恢复
                        if (player.experienceLevel == backupLevel) {
                            player.experienceProgress = backupProgress;
                        }
                        
                        fixSuccessful = true;
                        
                        if (Config.DEBUG_LOGGING.get()) {
                            ExperienceBarFixMod.LOGGER.debug("Successfully fixed experience bar for player: {} using fallback method", 
                                player.getName().getString());
                        }
                    } else {
                        // 对于经验值为0的玩家，只需触发刷新
                        player.giveExperiencePoints(0);
                        fixSuccessful = true;
                    }
                } catch (Exception e) {
                    ExperienceBarFixMod.LOGGER.error("Fallback fix method failed for player: {}, error: {}", 
                        player.getName().getString(), e.getMessage());
                }
            }
            
            if (!fixSuccessful) {
                ExperienceBarFixMod.LOGGER.warn("All fix methods failed for player: {}", 
                    player.getName().getString());
            }
            
        } catch (Exception e) {
            ExperienceBarFixMod.LOGGER.error("Unexpected error while fixing experience bar for player: {}", 
                player.getName().getString(), e);
        } finally {
            // 始终从待处理集合中移除
            pendingFix.remove(playerId);
        }
    }
    
    /**
     * 断开连接玩家的清理方法
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        pendingFix.remove(event.getEntity().getUUID());
    }
}
