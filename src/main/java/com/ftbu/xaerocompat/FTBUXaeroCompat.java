package com.ftbu.xaerocompat;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Client-side bridge between FTB Chunks and Xaero's World Map.
 *
 * <p>FTB Chunks already syncs all claim data it is allowed to show to the client
 * (see {@code FTBChunks#loggedIn}).  This mod intercepts that sync, keeps a small
 * in-memory claim index, and exposes it to Xaero's World Map through the map's
 * own highlighter registry.  Claim/force-load actions are sent back using FTB
 * Chunks' existing {@code RequestChunkChangePacket}, so no server-side component
 * is required.</p>
 */
@Mod(FTBUXaeroCompat.MOD_ID)
public final class FTBUXaeroCompat {
    public static final String MOD_ID = "ftbcxaerocompat";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public FTBUXaeroCompat() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.ftbu.xaerocompat.client.ClientSetup.init();
        }
    }
}
