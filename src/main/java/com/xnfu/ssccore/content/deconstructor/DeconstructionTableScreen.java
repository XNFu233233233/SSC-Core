package com.xnfu.ssccore.content.deconstructor;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xnfu.ssccore.SSCCore;
import com.xnfu.ssccore.network.ConfirmPayload;
import com.xnfu.ssccore.network.ToggleMachinePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class DeconstructionTableScreen extends AbstractContainerScreen<DeconstructionTableMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(SSCCore.MODID, "textures/gui/deconstruction_table.png");
    private Button toggleButton;
    private EditBox configInput;

    public DeconstructionTableScreen(DeconstructionTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 220;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        
        // 配置输入框 (左上角 x=10, y=20)
        this.configInput = new EditBox(this.font, leftPos + 10, topPos + 20, 30, 14, Component.translatable("gui.ssccore.config", ""));
        this.configInput.setValue(String.valueOf(this.getMenu().getConfigValue()));
        this.configInput.setFilter(s -> s.matches("[0-9]*"));
        this.configInput.setMaxLength(3);
        this.configInput.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("gui.ssccore.tooltip.config")));
        
        // 允许按回车同步
        this.configInput.setResponder(s -> {}); 
        addRenderableWidget(configInput);

        // 机器开关 (右上角 x=140, y=18)
        this.toggleButton = Button.builder(Component.translatable("gui.ssccore.on"), b -> {
            PacketDistributor.sendToServer(new ToggleMachinePayload(this.getMenu().getPos()));
        })
        .pos(leftPos + 140, topPos + 18)
        .size(30, 20)
        .tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("gui.ssccore.on")))
        .build();
        addRenderableWidget(toggleButton);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) { // Enter or Keypad Enter
            if (configInput.isFocused()) {
                syncConfig();
                configInput.setFocused(false);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (configInput != null && !configInput.isFocused()) {
            String serverVal = String.valueOf(this.getMenu().getConfigValue());
            if (!configInput.getValue().equals(serverVal)) {
                configInput.setValue(serverVal);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (!configInput.isMouseOver(mouseX, mouseY) && configInput.isFocused()) {
            configInput.setFocused(false);
            syncConfig();
        }
        return handled;
    }
    
    private void syncConfig() {
        String val = configInput.getValue();
        if (!val.isEmpty()) {
            try {
                int intVal = Integer.parseInt(val);
                if (intVal != this.getMenu().getConfigValue()) {
                    PacketDistributor.sendToServer(new ConfirmPayload(this.getMenu().getPos(), intVal));
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // 垂直进度条渲染 (80, 42)
        DeconstructionTableMenu menu = this.getMenu();
        if (menu.isWorking() && menu.getMaxProgress() > 0) {
            float pct = (float)menu.getProgress() / menu.getMaxProgress();
            int h = (int)(pct * 24);
            if (h > 0) {
                guiGraphics.blit(TEXTURE, x + 80, y + 42, 176, 0, 16, h);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        boolean enabled = this.getMenu().isMachineEnabled();
        toggleButton.setMessage(enabled ? 
                Component.translatable("gui.ssccore.on") : Component.translatable("gui.ssccore.off"));
        toggleButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                enabled ? Component.translatable("gui.ssccore.on") : Component.translatable("gui.ssccore.off")));
        
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
