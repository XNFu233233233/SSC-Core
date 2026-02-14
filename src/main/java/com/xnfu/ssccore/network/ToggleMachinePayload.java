package com.xnfu.ssccore.network;

import com.xnfu.ssccore.SSCCore;
import com.xnfu.ssccore.content.deconstructor.DeconstructionTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleMachinePayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<ToggleMachinePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(SSCCore.MODID, "toggle_machine"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleMachinePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ToggleMachinePayload::pos,
            ToggleMachinePayload::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(ToggleMachinePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Level level = player.level();
                if (level.isLoaded(payload.pos)) {
                    BlockEntity be = level.getBlockEntity(payload.pos);
                    if (be instanceof DeconstructionTableBlockEntity table) table.toggleMachine();
                }
            }
        });
    }
}
