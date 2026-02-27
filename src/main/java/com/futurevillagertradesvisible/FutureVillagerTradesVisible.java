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

    @SuppressWarnings("removal")
    public FutureVillagerTradesVisible() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        NetworkHandler.register();
    }
}
