package com.ftbu.xaerocompat.claim;

import com.ftbu.xaerocompat.client.DisplayMode;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xaero.map.highlight.ChunkHighlighter;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class FTBChunksHighlighter extends ChunkHighlighter {
    public FTBChunksHighlighter() {
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
    public int calculateRegionHash(ResourceKey<Level> dimension, int regionX, int regionZ) {
        if (!DisplayMode.current().showClaims()) {
            return 0;
        }

        boolean showForceLoad = DisplayMode.current().showForceLoaded();
        long hash = showForceLoad ? 1L : 0L;
        int baseX = regionX << 5;
        int baseZ = regionZ << 5;
        for (int x = 0; x < 32; x++) {
            for (int z = 0; z < 32; z++) {
                ClaimCache.Claim claim = ClaimCache.get(dimension, baseX + x, baseZ + z);
                if (claim != null) {
                    hash = hash * 37L + claim.teamId().getMostSignificantBits();
                    hash = hash * 37L + claim.teamId().getLeastSignificantBits();
                    if (showForceLoad) {
                        hash = hash * 37L + (claim.forceLoaded() ? 1 : 0);
                    }
                }
            }
        }
        return (int) (hash ^ (hash >>> 32));
    }

    @Override
    public Component getChunkHighlightSubtleTooltip(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        if (!DisplayMode.current().showClaims()) {
            return null;
        }

        ClaimCache.Claim claim = ClaimCache.get(dimension, chunkX, chunkZ);
        if (claim == null) {
            return null;
        }

        Team team = ClaimHighlighterSupport.getTeam(claim.teamId());
        int rgb = team == null ? ClaimHighlighterSupport.DEFAULT_COLOR : ClaimHighlighterSupport.getTeamColor(team);
        MutableComponent tooltip = Component.literal("\u25a0 ").withStyle(Style.EMPTY.withColor(rgb));
        tooltip.append(Component.literal(team == null ? "FTB Chunks" : team.getShortName())
                .withStyle(ChatFormatting.WHITE));

        if (claim.forceLoaded()) {
            tooltip.append(Component.literal("  \u26a1 ").withStyle(ChatFormatting.GOLD));
            tooltip.append(Component.translatable("ftbcxaerocompat.tooltip.force_loaded")
                    .withStyle(ChatFormatting.GOLD));
        }
        return tooltip;
    }

    @Override
    public Component getChunkHighlightBluntTooltip(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return null;
    }

    @Override
    public void addMinimapBlockHighlightTooltips(List<Component> list, ResourceKey<Level> dimension,
                                                 int blockX, int blockZ, int width) {
        // The world map tooltip is enough; keeping the minimap tooltip clean.
    }
}
