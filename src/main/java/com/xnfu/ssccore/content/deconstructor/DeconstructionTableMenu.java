package com.xnfu.ssccore.content.deconstructor;

import com.xnfu.ssccore.SSCCore;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class DeconstructionTableMenu extends AbstractContainerMenu {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_START = 1;
    public static final int OUTPUT_END = 21;
    public static final int SLOT_COUNT = 22;
    private final ContainerLevelAccess access;
    private final IItemHandler itemHandler;
    private final ContainerData data;
    private final BlockPos pos;

    public DeconstructionTableMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, new ItemStackHandler(SLOT_COUNT), new SimpleContainerData(8), extraData.readBlockPos(), ContainerLevelAccess.NULL);
    }

    public DeconstructionTableMenu(int containerId, Inventory playerInv, IItemHandler itemHandler, ContainerData data, BlockPos pos, ContainerLevelAccess access) {
        super(SSCCore.DECONSTRUCTION_MENU.get(), containerId);
        this.access = access;
        this.itemHandler = itemHandler;
        this.data = data;
        this.pos = pos;

        // Input Slot (x=80, y=21)
        addSlot(new SlotItemHandler(itemHandler, INPUT_SLOT, 80, 21));

        // Output Grid 3x7 (start x=26, y=69)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 7; ++col) {
                int slotIndex = OUTPUT_START + col + row * 7;
                addSlot(new SlotItemHandler(itemHandler, slotIndex, 26 + col * 18, 69 + row * 18) {
                    @Override public boolean mayPlace(ItemStack stack) { return false; }
                });
            }
        }

        // Player Inventory (start x=8, y=137)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 137 + row * 18));
            }
        }

        // Hotbar (start x=8, y=195)
        for (int col = 0; col < 9; ++col) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 195));
        }

        addDataSlots(data);
    }

    public int getProgress() { return data.get(0); }
    public int getMaxProgress() { return data.get(1); }
    public int getConfigValue() { return data.get(2); } // 新增 getter 用于 Screen 初始化
    public boolean isMachineEnabled() { return data.get(4) != 0; }
    public boolean isWorking() { return data.get(5) != 0; }
    public BlockPos getPos() { return pos; }
    public ContainerData getData() { return data; }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        ItemStack sourceStack = ItemStack.EMPTY;
        Slot sourceSlot = this.slots.get(index);
        if (sourceSlot != null && sourceSlot.hasItem()) {
            ItemStack originalStack = sourceSlot.getItem();
            sourceStack = originalStack.copy();
            if (index < SLOT_COUNT) {
                if (!this.moveItemStackTo(originalStack, SLOT_COUNT, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(originalStack, INPUT_SLOT, INPUT_SLOT + 1, false)) return ItemStack.EMPTY;
            }
            if (originalStack.isEmpty()) sourceSlot.set(ItemStack.EMPTY);
            else sourceSlot.setChanged();
        }
        return sourceStack;
    }

    @Override public boolean stillValid(Player player) { return stillValid(this.access, player, SSCCore.DECONSTRUCTION_TABLE.get()); }
}