package com.ftbu.xaerocompat.client;

import com.ftbu.xaerocompat.FTBUXaeroCompat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xaero.lib.client.gui.widget.Tooltip;
import xaero.map.gui.GuiMap;
import xaero.map.gui.TooltipButton;

/**
 * The single FTB Chunks display button shown in the bottom-left corner of
 * Xaero's World Map.
 *
 * <p>Its texture is the 32x32 FTB logo supplied with the mod. The button is
 * deliberately kept small (20x20) so it fits into Xaero's map-button column.
 * A colored status dot shows the current global display mode.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class FTBChunksMapButton extends TooltipButton {
    private static final int SIZE = 20;
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(FTBUXaeroCompat.MOD_ID, "textures/gui/ftb.png");

    private FTBChunksMapButton(int x, int y) {
        super(x, y, SIZE, SIZE, Component.empty(), button -> DisplayMode.cycle(),
                () -> new Tooltip(DisplayMode.current().tooltipText()));
    }

    public static FTBChunksMapButton createFor(GuiMap map) {
        int[] position = findFreePosition(map);
        return new FTBChunksMapButton(position[0], position[1]);
    }

    /**
     * Finds the first free 20x20 slot in the bottom-left button area. Checking
     * the actual widgets instead of hard-coding a coordinate avoids overlapping
     * Xaero's own buttons, the cave-mode controls, or buttons added by other
     * mods. The first two columns are preferred because they are the usual
     * bottom-left map-button columns.
     */
    private static int[] findFreePosition(GuiMap map) {
        // Xaero leaves a free 20x20 gap between the dimension button at
        // height - 60 and the hop button at height - 100. Prefer that gap so
        // the FTB button sits directly above Xaero's existing bottom-left
        // button column and cannot cover a custom-drawn button in the usual
        // two-column cluster.
        int preferredY = map.height - SIZE * 4;
        if (preferredY >= 0) {
            for (int x : new int[]{0, SIZE, SIZE * 2, SIZE * 3}) {
                if (isFree(map, x, preferredY)) {
                    return new int[]{x, preferredY};
                }
            }
        }

        for (int row = 0; row < 10; row++) {
            int y = map.height - SIZE - row * SIZE;
            if (y < 0) {
                break;
            }
            for (int x : new int[]{0, SIZE}) {
                if (isFree(map, x, y)) {
                    return new int[]{x, y};
                }
            }
        }

        // Fallback for heavily modded screens: use the next free column.
        for (int row = 0; row < 6; row++) {
            int y = map.height - SIZE - row * SIZE;
            if (y < 0) {
                break;
            }
            for (int x : new int[]{SIZE * 2, SIZE * 3}) {
                if (isFree(map, x, y)) {
                    return new int[]{x, y};
                }
            }
        }

        return new int[]{0, Math.max(0, map.height - SIZE)};
    }

    private static boolean isFree(GuiMap map, int x, int y) {
        for (GuiEventListener listener : map.children()) {
            if (!(listener instanceof AbstractWidget widget) || !widget.visible) {
                continue;
            }

            int widgetRight = widget.getX() + widget.getWidth();
            int widgetBottom = widget.getY() + widget.getHeight();
            if (x < widgetRight && x + SIZE > widget.getX()
                    && y < widgetBottom && y + SIZE > widget.getY()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        boolean hovered = isHovered();

        if (hovered) {
            // Keep the small native-style lift, but do not draw a white frame.
            y--;
        }

        // Full 32x32 source texture scaled into the 20x20 button.
        graphics.blitInscribed(TEXTURE, x, y, SIZE, SIZE, 32, 32);

        int dotX = getX() + 15;
        int dotY = getY() + 15;
        graphics.fill(dotX, dotY, dotX + 5, dotY + 5, 0xDD000000);
        graphics.fill(dotX + 1, dotY + 1, dotX + 4, dotY + 4,
                DisplayMode.current().indicatorColor());
    }
}
