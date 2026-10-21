package com.ftbu.xaerocompat.mixin;

import com.ftbu.xaerocompat.claim.ClaimCache;
import dev.ftb.mods.ftbchunks.client.FTBChunksClient;
import dev.ftb.mods.ftbchunks.net.SendChunkPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.UUID;

@Mixin(value = FTBChunksClient.class, remap = false)
public abstract class FTBChunksClientMixin {
    @Inject(method = "updateChunksFromServer", at = @At("HEAD"), remap = false)
    private void ftbu$updateClaimCache(ResourceKey<Level> dimension, UUID teamId,
                                       Collection<SendChunkPacket.SingleChunk> chunks, CallbackInfo ci) {
        ClaimCache.update(dimension, teamId, chunks);
    }
}
