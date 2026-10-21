package com.ftbu.xaerocompat.client;

import com.ftbu.xaerocompat.CompatConfig;
import com.ftbu.xaerocompat.FTBUXaeroCompat;
import com.ftbu.xaerocompat.claim.ClaimCache;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The single, global FTB Chunks display state shared by Xaero's World Map and
 * Xaero's Minimap.
 *
 * <p>The state is persisted in the client config and is changed with the FTB
 * button injected into the bottom-left corner of the world map.</p>
 */
@OnlyIn(Dist.CLIENT)
public enum DisplayMode {
    HIDDEN(0, "ftbcxaerocompat.display_mode.hidden", 0xFF9E9E9E),
    CLAIMS(1, "ftbcxaerocompat.display_mode.claims", 0xFF55FF55),
    CLAIMS_AND_FORCE_LOADED(2, "ftbcxaerocompat.display_mode.force_loaded", 0xFFFFD700);

    private static final DisplayMode[] VALUES = values();
    private static final DisplayMode FALLBACK = CLAIMS;

    private final int configValue;
    private final String translationKey;
    private final int indicatorColor;

    DisplayMode(int configValue, String translationKey, int indicatorColor) {
        this.configValue = configValue;
        this.translationKey = translationKey;
        this.indicatorColor = indicatorColor;
    }

    public boolean showClaims() {
        return this != HIDDEN;
    }

    public boolean showForceLoaded() {
        return this == CLAIMS_AND_FORCE_LOADED;
    }

    public int indicatorColor() {
        return this.indicatorColor;
    }

    public Component displayName() {
        return Component.translatable(this.translationKey);
    }

    public DisplayMode next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }

    public Component tooltipText() {
        // Xaero's TextSplitter only treats a newline as a line break when it is
        // a separate word, so surround it with spaces. Building the three lines
        // manually also keeps the current state highlighted.
        MutableComponent tooltip = Component.empty();
        for (int i = 0; i < VALUES.length; i++) {
            DisplayMode mode = VALUES[i];
            if (i > 0) {
                tooltip.append(Component.literal(" \n "));
            }
            MutableComponent line = mode.displayName().copy();
            if (mode == this) {
                line.withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD);
            } else {
                line.withStyle(ChatFormatting.GRAY);
            }
            tooltip.append(line);
        }
        return tooltip;
    }

    public static DisplayMode current() {
        try {
            if (CompatConfig.SPEC.isLoaded()) {
                return fromConfigValue(CompatConfig.DISPLAY_MODE.get());
            }
        } catch (RuntimeException ignored) {
            // The config may not be loaded yet while the game is starting.
        }
        return FALLBACK;
    }

    public static void cycle() {
        DisplayMode next = current().next();
        try {
            CompatConfig.DISPLAY_MODE.set(next.configValue);
            CompatConfig.DISPLAY_MODE.save();
        } catch (RuntimeException e) {
            FTBUXaeroCompat.LOGGER.warn("Could not save the FTB Chunks display mode", e);
        }

        // Both maps read this same state, so refreshing both caches keeps the
        // world map and the minimap in sync immediately.
        ClaimCache.refreshAllHighlights();
        MapIntegration.message(Component.translatable("ftbcxaerocompat.display_mode.changed", next.displayName()));
    }

    private static DisplayMode fromConfigValue(int value) {
        for (DisplayMode mode : VALUES) {
            if (mode.configValue == value) {
                return mode;
            }
        }
        return FALLBACK;
    }
}
