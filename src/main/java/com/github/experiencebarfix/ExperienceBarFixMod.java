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
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ExperienceBarFixMod.MODID)
public class ExperienceBarFixMod {
    
    public static final String MODID = "experiencebarfix";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    
    public ExperienceBarFixMod() {
        // 注册模组加载的设置方法
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        
        // 注册配置文件
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        
        // 注册服务器和其他我们感兴趣的游戏事件
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        // 注册经验条修复器
        MinecraftForge.EVENT_BUS.register(new ExperienceBarFixer());
        LOGGER.info("Experience Bar Fix mod initialized successfully!");
    }
}
