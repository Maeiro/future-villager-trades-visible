package com.futurevillagertradesvisible;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import com.futurevillagertradesvisible.network.NetworkHandler;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(FutureVillagerTradesVisible.MODID)
public class FutureVillagerTradesVisible {
    public static final String MODID = "futurevillagertradesvisible";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final String COMMON_CONFIG_FILE = MODID + "-common-v2.toml";

    @SuppressWarnings("removal")
    public FutureVillagerTradesVisible() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, COMMON_CONFIG_FILE);
        NetworkHandler.register();
    }
}
