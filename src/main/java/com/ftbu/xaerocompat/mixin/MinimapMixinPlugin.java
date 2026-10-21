package com.ftbu.xaerocompat.mixin;

import net.minecraftforge.fml.loading.LoadingModList;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Keeps the optional Xaero's Minimap integration from loading any classes that
 * extend minimap types when the minimap is not installed.
 */
public final class MinimapMixinPlugin implements IMixinConfigPlugin {
    private static final boolean MINIMAP_PRESENT = detectMinimap();

    private static boolean detectMinimap() {
        try {
            LoadingModList modList = LoadingModList.get();
            if (modList != null && modList.getModFileById("xaerominimap") != null) {
                return true;
            }
        } catch (Throwable ignored) {
            // Fall through to the class check for early launch environments.
        }
        return classExists("xaero.common.IXaeroMinimap");
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className, false, MinimapMixinPlugin.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return MINIMAP_PRESENT;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass,
                         String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass,
                          String mixinClassName, IMixinInfo mixinInfo) {
    }
}
