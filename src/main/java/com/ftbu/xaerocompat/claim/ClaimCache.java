package com.ftbu.xaerocompat.claim;

import dev.ftb.mods.ftbchunks.client.map.MapChunk;
import dev.ftb.mods.ftbchunks.net.SendChunkPacket;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xaero.map.MapProcessor;
import xaero.map.WorldMapSession;
import xaero.map.mods.SupportMods;
import xaero.map.world.MapDimension;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory index of the claims FTB Chunks has synced to this client.
 *
 * <p>FTB Chunks sends all visible claims on login and sends incremental updates
 * when claims change, so this index is kept in sync by
 * {@code FTBChunksClient#updateChunksFromServer}.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class ClaimCache {
    public record Claim(UUID teamId, boolean forceLoaded) {
    }

    private static final Map<ResourceKey<Level>, DimensionClaims> DIMENSIONS = new ConcurrentHashMap<>();

    private static final class DimensionClaims {
        private final Map<Long, Claim> claims = new ConcurrentHashMap<>();
        private final Set<Long> regions = ConcurrentHashMap.newKeySet();
    }

    public static Claim get(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        DimensionClaims dim = DIMENSIONS.get(dimension);
        return dim == null ? null : dim.claims.get(ChunkPos.asLong(chunkX, chunkZ));
    }

    public static boolean regionHasClaims(ResourceKey<Level> dimension, int regionX, int regionZ) {
        DimensionClaims dim = DIMENSIONS.get(dimension);
        return dim != null && dim.regions.contains(regionKey(regionX, regionZ));
    }

    public static void update(ResourceKey<Level> dimension, UUID teamId,
                              Collection<SendChunkPacket.SingleChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return;
        }

        DimensionClaims dim = DIMENSIONS.computeIfAbsent(dimension, key -> new DimensionClaims());
        boolean remove = Util.NIL_UUID.equals(teamId);
        long now = System.currentTimeMillis();
        Set<Long> touchedRegions = new HashSet<>();

        synchronized (dim) {
            for (SendChunkPacket.SingleChunk chunk : chunks) {
                int chunkX = chunk.getX();
                int chunkZ = chunk.getZ();
                long chunkKey = ChunkPos.asLong(chunkX, chunkZ);
                long regKey = regionKey(chunkX >> 5, chunkZ >> 5);
                touchedRegions.add(regKey);

                if (remove) {
                    dim.claims.remove(chunkKey);
                } else {
                    MapChunk.DateInfo dateInfo = chunk.getDateInfo(true, now);
                    dim.claims.put(chunkKey, new Claim(teamId, dateInfo.forceLoaded() != null));
                    dim.regions.add(regKey);
                }
            }

            if (remove) {
                for (long regKey : touchedRegions) {
                    if (!regionHasAnyClaim(dim, regKey)) {
                        dim.regions.remove(regKey);
                    }
                }
            }
        }

        Minecraft.getInstance().execute(() -> refreshMapHighlights(dimension, touchedRegions));
    }

    public static void clear() {
        DIMENSIONS.clear();
    }

    private static boolean regionHasAnyClaim(DimensionClaims dim, long regKey) {
        int regionX = (int) (regKey >> 32);
        int regionZ = (int) regKey;
        for (int x = 0; x < 32; x++) {
            for (int z = 0; z < 32; z++) {
                if (dim.claims.containsKey(ChunkPos.asLong((regionX << 5) | x, (regionZ << 5) | z))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static long regionKey(int regionX, int regionZ) {
        return ((long) regionX << 32) | (regionZ & 0xFFFFFFFFL);
    }

    private static void refreshMapHighlights(ResourceKey<Level> dimension, Set<Long> regions) {
        if (Minecraft.getInstance().level != null) {
            WorldMapSession session = WorldMapSession.getCurrentSession();
            if (session != null) {
                MapProcessor processor = session.getMapProcessor();
                if (processor != null) {
                    MapDimension mapDimension = processor.getMapWorld().getDimension(dimension);
                    if (mapDimension != null) {
                        for (long regKey : regions) {
                            mapDimension.getHighlightHandler().clearCachedHash((int) (regKey >> 32), (int) regKey);
                        }
                    }
                }
            }
        }
        refreshMinimapHighlights();
    }

    /**
     * Clears the baked highlight hashes of every dimension in the current world
     * map session and asks the minimap to clear its own highlight cache.
     *
     * <p>Call this when the global FTB Chunks display state changes: the world
     * map and minimap both read the same state, and this makes the change
     * visible without closing either map.</p>
     */
    public static void refreshAllHighlights() {
        try {
            WorldMapSession session = WorldMapSession.getCurrentSession();
            if (session != null) {
                MapProcessor processor = session.getMapProcessor();
                if (processor != null && processor.getMapWorld() != null) {
                    processor.getMapWorld().clearAllCachedHighlightHashes();
                }
            }
        } catch (Throwable ignored) {
            // The world map may be absent, closing, or not fully initialized.
        }
        refreshMinimapHighlights();
    }

    public static void refreshMinimapHighlights() {
        try {
            if (SupportMods.minimap()) {
                SupportMods.xaeroMinimap.onClearHighlightHashes();
            }
        } catch (Throwable ignored) {
            // Xaero Minimap may be absent or may be between sessions.
        }
    }

    private ClaimCache() {
    }
}
