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
