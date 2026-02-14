package com.xnfu.ssccore.content.deconstructor;

import com.xnfu.ssccore.SSCCore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class DeconstructionTableBlock extends BaseEntityBlock {
    public static final MapCodec<DeconstructionTableBlock> CODEC = simpleCodec(DeconstructionTableBlock::new);
    public DeconstructionTableBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof DeconstructionTableBlockEntity menuProvider) player.openMenu(menuProvider, pos);
        }
        return InteractionResult.SUCCESS;
    }
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof DeconstructionTableBlockEntity table) {
                // 1. 手动掉落 IItemHandler 中的物品 (Containers.dropContents 不支持 IItemHandler)
                IItemHandler handler = table.getItemHandler(null);
                for (int i = 0; i < handler.getSlots(); i++) {
                    net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), handler.getStackInSlot(i));
                }
                
                // 2. 掉落尚未取出的缓冲区物品
                table.getBuffersForDrop().forEach(stack -> 
                    net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack));
                
                level.updateNeighborsAt(pos, state.getBlock());
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new DeconstructionTableBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, SSCCore.DECONSTRUCTION_TABLE_BE.get(), DeconstructionTableBlockEntity::tick);
    }
}
