package com.ftbu.xaerocompat.mixin;

import com.ftbu.xaerocompat.client.FTBChunksMapButton;
import com.ftbu.xaerocompat.client.MapIntegration;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.gui.GuiMap;
import xaero.map.gui.MapTileSelection;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

import java.util.ArrayList;

@Mixin(value = GuiMap.class, remap = false)
public abstract class GuiMapMixin {
    @Shadow
    private ResourceKey<Level> rightClickDim;
    @Shadow
    private MapTileSelection mapTileSelection;
    @Shadow
    private int rightClickX;
    @Shadow
    private int rightClickZ;

    @Inject(method = "getRightClickOptions", at = @At("TAIL"), remap = false)
    private void ftbu$addClaimOptions(CallbackInfoReturnable<ArrayList<RightClickOption>> cir) {
        MapIntegration.addOptions((GuiMap) (Object) this, cir.getReturnValue(), this.rightClickDim,
                this.mapTileSelection, this.rightClickX >> 4, this.rightClickZ >> 4);
    }

    @Inject(method = "m_7856_", at = @At("TAIL"), remap = false)
    private void ftbu$addDisplayModeButton(CallbackInfo ci) {
        GuiMap map = (GuiMap) (Object) this;
        map.addButton(FTBChunksMapButton.createFor(map));
    }
}
