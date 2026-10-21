package com.ftbu.xaerocompat.mixin;

import com.ftbu.xaerocompat.claim.FTBChunksMinimapHighlighter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xaero.common.minimap.highlight.HighlighterRegistry;

@Pseudo
@Mixin(targets = "xaero.hud.minimap.module.MinimapSession", remap = false)
public abstract class MinimapSessionMixin {
    @Redirect(method = "<init>(Lxaero/common/HudMod;Lxaero/hud/module/HudModule;Lnet/minecraft/client/multiplayer/ClientPacketListener;)V",
            at = @At(value = "INVOKE",
                    target = "Lxaero/common/minimap/highlight/HighlighterRegistry;end()V"),
            remap = false)
    private void ftbu$registerMinimapHighlighter(HighlighterRegistry registry) {
        registry.register(new FTBChunksMinimapHighlighter());
        registry.end();
    }
}
