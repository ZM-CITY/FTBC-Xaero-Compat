package com.ftbu.xaerocompat.mixin;

import com.ftbu.xaerocompat.claim.FTBChunksHighlighter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xaero.map.WorldMapSession;
import xaero.map.highlight.HighlighterRegistry;

@Mixin(value = WorldMapSession.class, remap = false)
public abstract class WorldMapSessionMixin {
    @Redirect(method = "init", at = @At(value = "INVOKE",
            target = "Lxaero/map/highlight/HighlighterRegistry;end()V"), remap = false)
    private void ftbu$registerHighlighter(HighlighterRegistry registry) {
        registry.register(new FTBChunksHighlighter());
        registry.end();
    }
}
