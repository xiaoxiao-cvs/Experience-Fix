package com.github.experiencebarfix;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExperienceBarFixMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    
    public static final ForgeConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("启用经验条修复功能")
            .define("enabled", true);
    
    public static final ForgeConfigSpec.IntValue FIX_DELAY = BUILDER
            .comment("修复经验条前的延迟时间，单位为游戏刻 (1-20)")
            .defineInRange("fixDelay", 2, 1, 20);
    
    public static final ForgeConfigSpec.BooleanValue FALLBACK_METHOD = BUILDER
            .comment("当主要修复方法失败时使用备用方法")
            .define("fallbackMethod", true);
    
    public static final ForgeConfigSpec.BooleanValue DEBUG_LOGGING = BUILDER
            .comment("启用调试日志记录以便故障排除")
            .define("debugLogging", false);
    
    public static final ForgeConfigSpec.BooleanValue FIX_DIMENSION_CHANGE = BUILDER
            .comment("在维度变化时修复经验条")
            .define("fixDimensionChange", true);
    
    public static final ForgeConfigSpec.BooleanValue FIX_RESPAWN = BUILDER
            .comment("在玩家重生时修复经验条")
            .define("fixRespawn", true);
    
    public static final ForgeConfigSpec SPEC = BUILDER.build();
}
