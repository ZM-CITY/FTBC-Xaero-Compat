package com.ftbu.xaerocompat.mixin;

import com.ftbu.xaerocompat.client.MapIntegration;
import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbchunks.api.ClaimResult;
import dev.ftb.mods.ftbchunks.net.ChunkChangeResponsePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;
import java.util.Map;

@Mixin(value = ChunkChangeResponsePacket.class, remap = false)
public abstract class ChunkChangeResponseMixin {
    @Shadow
    @Final
    private int totalChunks;
    @Shadow
    @Final
    private int changedChunks;
    @Shadow
    @Final
    private EnumMap<ClaimResult.StandardProblem, Integer> problems;

    @Inject(method = "handle", at = @At("TAIL"), remap = false)
    private void ftbu$showResult(NetworkManager.PacketContext context, CallbackInfo ci) {
        if (this.totalChunks <= 0) {
            return;
        }

        MutableComponent message;
        if (this.changedChunks == this.totalChunks) {
            message = Component.translatable("ftbcxaerocompat.message.success", this.changedChunks);
        } else {
            message = Component.translatable("ftbcxaerocompat.message.partial", this.changedChunks,
                    this.totalChunks);
        }

        for (Map.Entry<ClaimResult.StandardProblem, Integer> entry : this.problems.entrySet()) {
            message.append("\n").append(Component.translatable("ftbcxaerocompat.message.problem",
                    entry.getValue(), entry.getKey().getMessage()));
        }

        MapIntegration.message(message);
    }
}
