package com.ftbu.xaerocompat.claim;

import com.ftbu.xaerocompat.client.DisplayMode;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xaero.common.minimap.highlight.ChunkHighlighter;
import xaero.hud.minimap.info.render.compile.InfoDisplayCompiler;

@OnlyIn(Dist.CLIENT)
public final class FTBChunksMinimapHighlighter extends ChunkHighlighter {
    public FTBChunksMinimapHighlighter() {
        super(true);
    }

    @Override
    public boolean regionHasHighlights(ResourceKey<Level> dimension, int regionX, int regionZ) {
        return DisplayMode.current().showClaims() && ClaimCache.regionHasClaims(dimension, regionX, regionZ);
    }

    @Override
    public boolean chunkIsHighlit(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return DisplayMode.current().showClaims() && ClaimCache.get(dimension, chunkX, chunkZ) != null;
    }

    @Override
    protected int[] getColors(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return ClaimHighlighterSupport.getColors(this.resultStore, dimension, chunkX, chunkZ,
                DisplayMode.current().showForceLoaded());
    }

    @Override
    public int[] getChunkHighlitColor(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        int[] colors = super.getChunkHighlitColor(dimension, chunkX, chunkZ);
        if (DisplayMode.current().showForceLoaded()) {
            return ClaimHighlighterSupport.addForceLoadMarker(colors, dimension, chunkX, chunkZ);
        }
        return colors;
    }

    @Override
    public void addChunkHighlightTooltips(InfoDisplayCompiler compiler, ResourceKey<Level> dimension,
                                          int chunkX, int chunkZ, int width) {
        // Keep the minimap tooltip clean; the world map already shows the claim team.
    }
}
