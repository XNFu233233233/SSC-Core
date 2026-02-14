package com.xnfu.ssccore.datagen;

import com.xnfu.ssccore.SSCCore;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class SSCLanguageProvider extends LanguageProvider {
    public SSCLanguageProvider(PackOutput output) {
        super(output, SSCCore.MODID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
        add("block.ssccore.deconstruction_table", "拆解台");
        add("container.ssccore.deconstruction_table", "拆解台");
        add("itemGroup.ssccore", "SSC 核心");
        add("gui.ssccore.config", "配置: %s");
        add("gui.ssccore.energy", "能量: %s / %s FE");
        add("gui.ssccore.on", "开启");
        add("gui.ssccore.off", "关闭");
        add("gui.ssccore.tooltip.config", "配方索引编号");
        add("gui.ssccore.tooltip.progress", "分解进度");
        add("gui.ssccore.tooltip.energy", "机器能量");
    }
}
