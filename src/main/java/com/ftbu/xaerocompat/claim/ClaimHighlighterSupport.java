package com.ftbu.xaerocompat.claim;

import com.ftbu.xaerocompat.CompatConfig;
import com.ftbu.xaerocompat.client.DisplayMode;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.property.TeamProperties;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public final class ClaimHighlighterSupport {
    public static final int DEFAULT_COLOR = 0x55FF55;
    public static final int FORCE_LOAD_COLOR = 0xFFD700;

    private static final int FORCE_LOAD_STRIPE_PERIOD = 8;
    private static final int FORCE_LOAD_STRIPE_WIDTH = 2;

    public static int[] getColors(int[] resultStore, ResourceKey<Level> dimension, int chunkX, int chunkZ,
                                  boolean showForceLoad) {
        if (!DisplayMode.current().showClaims()) {
            return null;
        }

        ClaimCache.Claim claim = ClaimCache.get(dimension, chunkX, chunkZ);
        if (claim == null) {
            return null;
        }

        int rgb = getTeamColor(claim.teamId());
        int fillAlpha = CompatConfig.FILL_OPACITY.get();
        int borderAlpha = CompatConfig.BORDER_OPACITY.get();
        if (showForceLoad && claim.forceLoaded()) {
            fillAlpha = Math.min(255, fillAlpha + 32);
            borderAlpha = Math.min(255, borderAlpha + 32);
        }

        int fill = abgr(rgb, fillAlpha);
        int border = abgr(rgb, borderAlpha);

        resultStore[0] = fill;
        resultStore[1] = sameTeam(claim, ClaimCache.get(dimension, chunkX, chunkZ - 1)) ? fill : border;
        resultStore[2] = sameTeam(claim, ClaimCache.get(dimension, chunkX + 1, chunkZ)) ? fill : border;
        resultStore[3] = sameTeam(claim, ClaimCache.get(dimension, chunkX, chunkZ + 1)) ? fill : border;
        resultStore[4] = sameTeam(claim, ClaimCache.get(dimension, chunkX - 1, chunkZ)) ? fill : border;
        return resultStore;
    }

    /**
     * Draws four broad diagonal gold stripes across the 16x16 chunk texture.
     * Xaero stores colors as ABGR values indexed by {@code (z << 4) | x}.
     */
    public static int[] addForceLoadMarker(int[] colors, ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        if (colors == null) {
            return null;
        }

        ClaimCache.Claim claim = ClaimCache.get(dimension, chunkX, chunkZ);
        if (claim == null || !claim.forceLoaded()) {
            return colors;
        }

        int marker = abgr(FORCE_LOAD_COLOR, 255);
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                if (((x + z) & (FORCE_LOAD_STRIPE_PERIOD - 1)) < FORCE_LOAD_STRIPE_WIDTH) {
                    colors[(z << 4) | x] = marker;
                }
            }
        }
        return colors;
    }

    public static Team getTeam(UUID teamId) {
        try {
            return FTBTeamsAPI.api().getClientManager().getTeamByID(teamId).orElse(null);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static int getTeamColor(UUID teamId) {
        Team team = getTeam(teamId);
        return team == null ? DEFAULT_COLOR : getTeamColor(team);
    }

    public static int getTeamColor(Team team) {
        try {
            Color4I color = team.getProperty(TeamProperties.COLOR);
            return color == null ? DEFAULT_COLOR : color.rgb() & 0xFFFFFF;
        } catch (Exception ignored) {
            return DEFAULT_COLOR;
        }
    }

    private static boolean sameTeam(ClaimCache.Claim claim, ClaimCache.Claim other) {
        return other != null && other.teamId().equals(claim.teamId());
    }

    private static int abgr(int rgb, int alpha) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        return (blue << 24) | (green << 16) | (red << 8) | (alpha & 0xFF);
    }

    private ClaimHighlighterSupport() {
    }
}
