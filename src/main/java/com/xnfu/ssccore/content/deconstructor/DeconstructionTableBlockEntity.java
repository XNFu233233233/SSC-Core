package com.xnfu.ssccore.content.deconstructor;

import com.xnfu.ssccore.SSCCore;
import com.xnfu.ssccore.content.deconstructor.recipe.DeconstructionRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class DeconstructionTableBlockEntity extends BlockEntity implements MenuProvider {
    private boolean machineDirty = true; // 更通用的脏位标记

    // 内部完整访问权限
    private final ItemStackHandler itemHandler = new ItemStackHandler(DeconstructionTableMenu.SLOT_COUNT) {
        @Override protected void onContentsChanged(int slot) {
            setChanged();
            machineDirty = true;
        }
    };

    // --- 统一暴露接口 (所有面通用) ---
    private final IItemHandler exposedHandler = new IItemHandler() {
        @Override public int getSlots() { return itemHandler.getSlots(); }
        @Override public @NotNull ItemStack getStackInSlot(int slot) { return itemHandler.getStackInSlot(slot); }
        
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            // 严禁向非输入槽推入物品
            if (slot != DeconstructionTableMenu.INPUT_SLOT) return stack;
            return itemHandler.insertItem(slot, stack, simulate);
        }

        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            // 严禁从输入槽拉取物品，只能从输出槽提取
            if (slot == DeconstructionTableMenu.INPUT_SLOT) return ItemStack.EMPTY;
            return itemHandler.extractItem(slot, amount, simulate);
        }

        @Override public int getSlotLimit(int slot) { return itemHandler.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            // 仅输入槽有效
            return slot == DeconstructionTableMenu.INPUT_SLOT;
        }
    };

    private static class CustomEnergyStorage extends EnergyStorage {
        public CustomEnergyStorage(int capacity, int maxReceive, int maxExtract) {
            super(capacity, maxReceive, maxExtract);
        }
        public void setEnergy(int energy) { this.energy = energy; }
    }

    private final CustomEnergyStorage energyStorage = new CustomEnergyStorage(com.xnfu.ssccore.Config.MAX_ENERGY.get(), 1000, 1000) {
        @Override public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (received > 0) {
                setChanged();
                machineDirty = true;
            }
            return received;
        }
    };

    private int progress = 0;
    private int maxProgress = 5;
    private int currentEnergyCost = 100;
    private boolean isWorking = false;
    private boolean machineEnabled = true;
    private int configValue = 0;
    
    private final List<ItemStack> pendingOutputs = new ArrayList<>();
    private final List<ItemStack> outputBuffer = new ArrayList<>();

    protected final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            switch (index) {
                case 0: return progress;
                case 1: return maxProgress;
                case 2: return configValue;
                case 4: return machineEnabled ? 1 : 0;
                case 5: return isWorking ? 1 : 0;
                default: return 0;
            }
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 0: progress = value; break;
                case 1: maxProgress = value; break;
                case 2: if(configValue != value) { configValue = value; machineDirty = true; } break;
                case 4: if(machineEnabled != (value!=0)) { machineEnabled = value != 0; machineDirty = true; } break;
                case 5: isWorking = value != 0; break;
            }
        }
        @Override public int getCount() { return 6; }
    };

    public DeconstructionTableBlockEntity(BlockPos pos, BlockState blockState) {
        super(SSCCore.DECONSTRUCTION_TABLE_BE.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DeconstructionTableBlockEntity be) {
        if (level.isClientSide) return;

        // 1. 工作状态优先处理
        if (be.isWorking) {
            // 加速逻辑：如果能量存储足以支付当前“整批任务”的总能耗，则瞬间完成
            if (be.energyStorage.getEnergyStored() >= be.currentEnergyCost) {
                be.energyStorage.extractEnergy(be.currentEnergyCost, false);
                be.completeProcess(); // 1 tick 完成
            } else {
                // 无电或电量不足模式：不消耗能量，按照正常速度（1 tick / 1 progress）运行
                be.progress++;
                if (be.progress >= be.maxProgress) {
                    be.completeProcess();
                }
            }
            return; // 工作期间不处理启动逻辑
        }

        // 2. 空闲状态：仅在脏标记激活时尝试清理缓冲区或启动新任务
        // (完全事件驱动：只有库存变动/配置变动/完成工作/加载 完成时才运行)
        if (be.machineDirty) {
            be.machineDirty = false; // 先清除标记，容许后续操作再次触发脏标记 (例如 dumpBuffer 成功时)

            // A. 尝试清空缓冲区
            if (!be.outputBuffer.isEmpty()) {
                be.dumpBuffer();
            }

            // B. 尝试启动新任务 (前提：缓冲区已空且机器开启)
            if (be.outputBuffer.isEmpty() && be.machineEnabled) {
                be.tryStartNextCycle();
            }
        }
    }

    // 缓存最后一次成功的配方，避免每 tick 遍历注册表
    private RecipeHolder<DeconstructionRecipe> lastRecipe;

    private void tryStartNextCycle() {
        ItemStack inputStack = itemHandler.getStackInSlot(DeconstructionTableMenu.INPUT_SLOT);
        if (inputStack.isEmpty() || level == null) return;

        RecipeManager rm = level.getRecipeManager();
        HolderLookup.Provider regs = level.registryAccess();
        RecipeHolder<DeconstructionRecipe> match = null;

        // 1. 尝试使用缓存
        if (lastRecipe != null) {
            if (lastRecipe.value().matches(new SingleRecipeInput(inputStack), level) && lastRecipe.value().getConfig() == configValue) {
                match = lastRecipe;
            }
        }

        // 2. 缓存失效，全量扫描
        if (match == null) {
            List<RecipeHolder<DeconstructionRecipe>> recipes = rm.getAllRecipesFor(SSCCore.DECONSTRUCTION_RECIPE_TYPE.get()).stream()
                    .filter(r -> r.value().matches(new SingleRecipeInput(inputStack), level) && r.value().getConfig() == configValue)
                    .toList();
            if (!recipes.isEmpty()) {
                match = recipes.get(0);
                lastRecipe = match; // 更新缓存
            }
        }

        if (match == null) return;
        DeconstructionRecipe recipe = match.value();
        List<ItemStack> recipeOutputs = recipe.getOutputs(rm, regs);
        boolean hasEnchants = !inputStack.getEnchantments().isEmpty();
        int inputCount = recipe.getInputCount(rm, regs);

        // --- 核心优化：精准库存模拟计算批处理数量 ---
        int maxPossible = inputStack.getCount() / inputCount;
        int batchCount = 0;

        // 创建一个模拟 Inventory 用于空间计算
        ItemStackHandler simHandler = new ItemStackHandler(DeconstructionTableMenu.SLOT_COUNT);
        for (int i = 0; i < DeconstructionTableMenu.SLOT_COUNT; i++) {
            simHandler.setStackInSlot(i, itemHandler.getStackInSlot(i).copy());
        }

        // 循环模拟每一轮产出的存入
        while (batchCount < maxPossible) {
            List<ItemStack> currentOutputs = new ArrayList<>();
            recipeOutputs.forEach(s -> currentOutputs.add(s.copy()));
            if (hasEnchants) {
                ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                book.set(DataComponents.STORED_ENCHANTMENTS, inputStack.getEnchantments());
                currentOutputs.add(book);
            }

            boolean canFitThisRound = true;
            for (ItemStack out : currentOutputs) {
                ItemStack remaining = out;
                // 模拟插入到输出槽 (1-21)
                for (int i = DeconstructionTableMenu.OUTPUT_START; i <= DeconstructionTableMenu.OUTPUT_END; i++) {
                    remaining = simHandler.insertItem(i, remaining, false);
                    if (remaining.isEmpty()) break;
                }
                if (!remaining.isEmpty()) {
                    canFitThisRound = false;
                    break;
                }
            }

            if (canFitThisRound) {
                batchCount++;
            } else {
                break;
            }
        }

        if (batchCount > 0) {
            startBatchProcess(recipe, inputStack, recipeOutputs, batchCount, inputCount);
        }
    }

    private void startBatchProcess(DeconstructionRecipe recipe, ItemStack input, List<ItemStack> resolvedOutputs, int batchCount, int inputCountPerOp) {
        this.isWorking = true;
        this.progress = 0;
        
        // --- 核心优化：整批处理仅消耗单份成本 ---
        this.maxProgress = recipe.getProcessingTime();
        this.currentEnergyCost = recipe.getEnergyCost(); 
        
        this.pendingOutputs.clear();
        for (ItemStack s : resolvedOutputs) {
            long totalCount = (long) s.getCount() * batchCount;
            int maxStack = s.getMaxStackSize();
            
            // 将大额产出拆分为多个标准堆，避免超过 Minecraft 1.21.1 的 Codec 数量限制
            while (totalCount > 0) {
                int toAdd = (int) Math.min(totalCount, maxStack);
                ItemStack stack = s.copy();
                stack.setCount(toAdd);
                this.pendingOutputs.add(stack);
                totalCount -= toAdd;
            }
        }

        if (!input.getEnchantments().isEmpty()) {
            ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
            enchantedBook.set(DataComponents.STORED_ENCHANTMENTS, input.getEnchantments());
            // 附魔书每本都是独立的
            for (int i = 0; i < batchCount; i++) {
                this.pendingOutputs.add(enchantedBook.copy());
            }
        }

        itemHandler.extractItem(DeconstructionTableMenu.INPUT_SLOT, batchCount * inputCountPerOp, false);
        setChanged();
    }

    private void completeProcess() {
        this.isWorking = false;
        this.progress = 0;
        this.outputBuffer.addAll(this.pendingOutputs);
        this.pendingOutputs.clear();
        this.dumpBuffer();
        this.machineDirty = true; // 完成后再次标记为脏，尝试触发下一轮
        setChanged();
    }

    private void dumpBuffer() {
        if (outputBuffer.isEmpty()) return;
        List<ItemStack> remaining = new ArrayList<>();
        for (ItemStack stack : outputBuffer) {
            ItemStack toInsert = stack;
            // 尝试插入 1-21 号输出槽
            for (int i = DeconstructionTableMenu.OUTPUT_START; i <= DeconstructionTableMenu.OUTPUT_END; i++) {
                toInsert = itemHandler.insertItem(i, toInsert, false);
                if (toInsert.isEmpty()) break;
            }
            if (!toInsert.isEmpty()) remaining.add(toInsert);
        }
        outputBuffer.clear();
        outputBuffer.addAll(remaining);
        if (!remaining.isEmpty()) setChanged();
    }

    public List<ItemStack> getBuffersForDrop() {
        List<ItemStack> all = new ArrayList<>();
        all.addAll(pendingOutputs);
        all.addAll(outputBuffer);
        return all;
    }

    public void startDeconstruction(int config) { this.configValue = config; this.machineDirty = true; setChanged(); }
    public void toggleMachine() { this.machineEnabled = !this.machineEnabled; this.machineDirty = true; setChanged(); }
    
    public IItemHandler getItemHandler(@Nullable Direction side) {
        return exposedHandler;
    }

    public IEnergyStorage getEnergyStorage() { return energyStorage; }
    public ContainerData getData() { return data; }

    @Override public Component getDisplayName() { return Component.translatable("block.ssccore.deconstruction_table"); }

    @Nullable @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new DeconstructionTableMenu(containerId, playerInventory, this.itemHandler, this.data, this.worldPosition, net.minecraft.world.inventory.ContainerLevelAccess.create(level, worldPosition));
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
        tag.putInt("CurrentEnergyCost", currentEnergyCost);
        tag.putBoolean("IsWorking", isWorking);
        tag.putBoolean("MachineEnabled", machineEnabled);
        tag.putInt("ConfigValue", configValue);
        tag.putInt("Energy", energyStorage.getEnergyStored());
        
        if (!outputBuffer.isEmpty()) {
            CompoundTag bufferTag = new CompoundTag();
            saveBuffer(bufferTag, outputBuffer, registries);
            tag.put("OutputBuffer", bufferTag);
        }
        if (!pendingOutputs.isEmpty()) {
            CompoundTag pendingTag = new CompoundTag();
            saveBuffer(pendingTag, pendingOutputs, registries);
            tag.put("PendingOutputs", pendingTag);
        }
    }

    private void saveBuffer(CompoundTag tag, List<ItemStack> list, HolderLookup.Provider registries) {
        tag.putInt("Size", list.size());
        for (int i = 0; i < list.size(); i++) {
            tag.put("Item" + i, list.get(i).save(registries));
        }
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        progress = tag.getInt("Progress");
        maxProgress = tag.getInt("MaxProgress");
        currentEnergyCost = tag.getInt("CurrentEnergyCost");
        isWorking = tag.getBoolean("IsWorking");
        machineEnabled = tag.getBoolean("MachineEnabled");
        configValue = tag.getInt("ConfigValue");
        
        if (tag.contains("Energy")) {
            energyStorage.setEnergy(tag.getInt("Energy"));
        }

        if (tag.contains("OutputBuffer")) {
            loadBuffer(tag.getCompound("OutputBuffer"), outputBuffer, registries);
        }
        if (tag.contains("PendingOutputs")) {
            loadBuffer(tag.getCompound("PendingOutputs"), pendingOutputs, registries);
        }
        
        if (!isWorking) {
            // 如果非工作状态下有输入或残留缓冲区，标记为脏以触发处理
            boolean hasInput = !itemHandler.getStackInSlot(DeconstructionTableMenu.INPUT_SLOT).isEmpty();
            boolean hasBuffer = !outputBuffer.isEmpty();
            if (hasInput || hasBuffer) {
                machineDirty = true;
            }
        }
    }

    private void loadBuffer(CompoundTag tag, List<ItemStack> list, HolderLookup.Provider registries) {
        list.clear();
        int size = tag.getInt("Size");
        for (int i = 0; i < size; i++) {
            String itemKey = "Item" + i;
            if (tag.contains(itemKey)) {
                ItemStack.parse(registries, tag.getCompound(itemKey)).ifPresent(list::add);
            }
        }
    }
}
