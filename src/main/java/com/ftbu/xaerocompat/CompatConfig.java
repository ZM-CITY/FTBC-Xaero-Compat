package com.ftbu.xaerocompat;

import net.minecraftforge.common.ForgeConfigSpec;

public final class CompatConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue DISPLAY_MODE;
    public static final ForgeConfigSpec.BooleanValue SHOW_CONTEXT_MENU;
    public static final ForgeConfigSpec.BooleanValue CONFIRM_LARGE_OPERATIONS;
    public static final ForgeConfigSpec.IntValue CONFIRM_THRESHOLD;
    public static final ForgeConfigSpec.IntValue FILL_OPACITY;
    public static final ForgeConfigSpec.IntValue BORDER_OPACITY;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Display settings shared by Xaero's World Map and Minimap.")
                .push("display");
        DISPLAY_MODE = builder
                .comment("FTB Chunks display mode: 0 = hidden, 1 = claims, 2 = claims + force-loaded.",
                        "The FTB button in the bottom-left corner of the world map cycles this value.")
                .defineInRange("displayMode", 1, 0, 2);
        FILL_OPACITY = builder
                .comment("Opacity of the inside of claimed chunks (0-255).")
                .defineInRange("fillOpacity", 72, 0, 255);
        BORDER_OPACITY = builder
                .comment("Opacity of the border between different claims (0-255).")
                .defineInRange("borderOpacity", 190, 0, 255);
        builder.pop();

        builder.comment("Interaction settings for the Xaero's World Map context menu.")
                .push("interaction");
        SHOW_CONTEXT_MENU = builder
                .comment("Add FTB Chunks claim/force-load actions to the world map context menu.")
                .define("showContextMenu", true);
        CONFIRM_LARGE_OPERATIONS = builder
                .comment("Ask for confirmation before changing a large number of chunks.")
                .define("confirmLargeOperations", true);
        CONFIRM_THRESHOLD = builder
                .comment("Number of chunks at which the confirmation screen is shown.")
                .defineInRange("confirmThreshold", 256, 1, 100000);
        builder.pop();

        SPEC = builder.build();
    }

    private CompatConfig() {
    }
}
