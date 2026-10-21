package com.ftbu.xaerocompat.client;

import com.ftbu.xaerocompat.CompatConfig;
import com.ftbu.xaerocompat.claim.ClaimCache;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

@OnlyIn(Dist.CLIENT)
public final class ClientSetup {
    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CompatConfig.SPEC);
        MinecraftForge.EVENT_BUS.addListener(ClientSetup::onLoggingOut);
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClaimCache.clear();
    }

    private ClientSetup() {
    }
}
