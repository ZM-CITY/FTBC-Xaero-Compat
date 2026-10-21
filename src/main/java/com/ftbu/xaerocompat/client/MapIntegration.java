package com.ftbu.xaerocompat.client;

import com.ftbu.xaerocompat.CompatConfig;
import com.ftbu.xaerocompat.claim.ClaimCache;
import dev.ftb.mods.ftbchunks.net.RequestChunkChangePacket;
import dev.ftb.mods.ftbchunks.net.RequestChunkChangePacket.ChunkChangeOp;
import dev.ftb.mods.ftblibrary.math.XZ;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xaero.map.WorldMapSession;
import xaero.map.gui.GuiMap;
import xaero.map.gui.MapTileSelection;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public final class MapIntegration {
    private static final Component MESSAGE_SOURCE = Component.translatable("ftbcxaerocompat.message.source");
    private static final int MAX_AREA = 16384;

    public static void addOptions(GuiMap map, ArrayList<RightClickOption> options, ResourceKey<Level> dimension,
                                  MapTileSelection selection, int fallbackChunkX, int fallbackChunkZ) {
        if (!CompatConfig.SHOW_CONTEXT_MENU.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || dimension == null) {
            return;
        }

        if (!dimension.equals(mc.level.dimension())) {
            options.add(disabled("ftbcxaerocompat.menu.wrong_dimension", options.size(), map));
            return;
        }

        int left;
        int top;
        int right;
        int bottom;

        if (selection != null) {
            int startX = selection.getStartX();
            int startZ = selection.getStartZ();
            int endX = selection.getEndX();
            int endZ = selection.getEndZ();

            // A right-click without dragging leaves MapTileSelection.end at (0, 0).
            // In that case the player almost certainly wants the single chunk they clicked.
            if (endX == 0 && endZ == 0 && (startX != 0 || startZ != 0)) {
                endX = startX;
                endZ = startZ;
            }

            left = Math.min(startX, endX);
            top = Math.min(startZ, endZ);
            right = Math.max(startX, endX);
            bottom = Math.max(startZ, endZ);
        } else {
            left = right = fallbackChunkX;
            top = bottom = fallbackChunkZ;
        }

        long area = (long) (right - left + 1) * (bottom - top + 1);
        if (area > MAX_AREA) {
            options.add(disabled("ftbcxaerocompat.menu.too_large", options.size(), map));
            return;
        }

        int claimable = 0;
        int unclaimable = 0;
        int loadable = 0;
        int unloadable = 0;

        for (int x = left; x <= right; x++) {
            for (int z = top; z <= bottom; z++) {
                ClaimCache.Claim claim = ClaimCache.get(dimension, x, z);
                if (claim == null) {
                    claimable++;
                } else {
                    unclaimable++;
                    if (claim.forceLoaded()) {
                        unloadable++;
                    } else {
                        loadable++;
                    }
                }
            }
        }

        if (claimable > 0) {
            options.add(action("ftbcxaerocompat.menu.claim", options.size(), map, claimable,
                    screen -> request(screen, ChunkChangeOp.CLAIM, dimension, left, top, right, bottom)));
        }
        if (unclaimable > 0) {
            options.add(action("ftbcxaerocompat.menu.unclaim", options.size(), map, unclaimable,
                    screen -> request(screen, ChunkChangeOp.UNCLAIM, dimension, left, top, right, bottom)));
        }
        if (loadable > 0) {
            options.add(action("ftbcxaerocompat.menu.force_load", options.size(), map, loadable,
                    screen -> request(screen, ChunkChangeOp.LOAD, dimension, left, top, right, bottom)));
        }
        if (unloadable > 0) {
            options.add(action("ftbcxaerocompat.menu.unforce_load", options.size(), map, unloadable,
                    screen -> request(screen, ChunkChangeOp.UNLOAD, dimension, left, top, right, bottom)));
        }
        if (claimable == 0 && unclaimable == 0) {
            options.add(disabled("ftbcxaerocompat.menu.nothing", options.size(), map));
        }
    }

    private static void request(Screen returnScreen, ChunkChangeOp op, ResourceKey<Level> dimension,
                                int left, int top, int right, int bottom) {
        Set<XZ> chunks = new HashSet<>();
        for (int x = left; x <= right; x++) {
            for (int z = top; z <= bottom; z++) {
                if (matches(op, ClaimCache.get(dimension, x, z))) {
                    chunks.add(XZ.of(x, z));
                }
            }
        }

        int count = chunks.size();
        if (count == 0) {
            return;
        }

        Runnable send = () -> {
            new RequestChunkChangePacket(op, chunks).sendToServer();
            message(Component.translatable("ftbcxaerocompat.message.requested", count));
        };

        if (CompatConfig.CONFIRM_LARGE_OPERATIONS.get() && count > CompatConfig.CONFIRM_THRESHOLD.get()) {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new ConfirmScreen(confirmed -> {
                mc.setScreen(returnScreen);
                if (confirmed) {
                    send.run();
                }
            },
                    Component.translatable("ftbcxaerocompat.confirm.title"),
                    Component.translatable("ftbcxaerocompat.confirm.message", count,
                            Component.translatable("ftbcxaerocompat.action." + op.name().toLowerCase(Locale.ROOT)))));
        } else {
            send.run();
        }
    }

    private static boolean matches(ChunkChangeOp op, ClaimCache.Claim claim) {
        return switch (op) {
            case CLAIM -> claim == null;
            case UNCLAIM -> claim != null;
            case LOAD -> claim != null && !claim.forceLoaded();
            case UNLOAD -> claim != null && claim.forceLoaded();
        };
    }

    public static void message(Component message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof GuiMap) {
            WorldMapSession session = WorldMapSession.getCurrentSession();
            if (session != null && session.getMapProcessor() != null) {
                session.getMapProcessor().getMessageBox().addMessageWithSource(MESSAGE_SOURCE, message);
                return;
            }
        }
        if (mc.player != null) {
            mc.player.displayClientMessage(message, true);
        }
    }

    private static RightClickOption action(String key, int index, GuiMap map, int count,
                                           Consumer<Screen> action) {
        return new RightClickOption(key, index, map) {
            @Override
            public void onAction(Screen screen) {
                action.accept(screen);
            }
        }.setNameFormatArgs(Component.literal(String.valueOf(count)).withStyle(ChatFormatting.YELLOW));
    }

    private static RightClickOption disabled(String key, int index, GuiMap map) {
        return new RightClickOption(key, index, map) {
            @Override
            public void onAction(Screen screen) {
            }
        }.setActive(false);
    }

    private MapIntegration() {
    }
}
