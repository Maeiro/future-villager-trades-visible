package com.futurevillagertradesvisible;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(FutureVillagerTradesVisible.MODID)
public class FutureVillagerTradesVisible {
    public static final String MODID = "futurevillagertradesvisible";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FutureVillagerTradesVisible(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}