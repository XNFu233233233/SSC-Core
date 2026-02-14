package com.xnfu.ssccore.datagen;

import com.xnfu.ssccore.SSCCore;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class SSCLangProvider extends LanguageProvider {
    protected final String locale;

    public SSCLangProvider(PackOutput output, String locale) {
        super(output, SSCCore.MODID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        if (locale.equals("en_us")) {
            addEnUs();
        } else if (locale.equals("zh_cn")) {
            addZhCn();
        }
    }

    private void addEnUs() {
        addBlock(SSCCore.DECONSTRUCTION_TABLE, "Deconstruction Table");
        add("container.ssccore.deconstruction_table", "Deconstruction Table");
        add("itemGroup.ssccore", "SSC Core");
        
        add("gui.ssccore.config", "Config: %s");
        add("gui.ssccore.energy", "Energy: %s / %s FE");
        add("gui.ssccore.on", "On");
        add("gui.ssccore.off", "Off");
        
        add("gui.ssccore.tooltip.config", "Recipe Index Number");
        add("gui.ssccore.tooltip.progress", "Deconstruction Progress");
        add("gui.ssccore.tooltip.energy", "Machine Energy");
    }

    private void addZhCn() {
        addBlock(SSCCore.DECONSTRUCTION_TABLE, "拆解台");
        add("container.ssccore.deconstruction_table", "拆解台");
        add("itemGroup.ssccore", "SSC 核心");
        
        add("gui.ssccore.config", "配置: %s");
        add("gui.ssccore.energy", "能量: %s / %s FE");
        add("gui.ssccore.on", "开启");
        add("gui.ssccore.off", "关闭");
        
        add("gui.ssccore.tooltip.config", "配方索引编号");
        add("gui.ssccore.tooltip.progress", "拆解进度");
        add("gui.ssccore.tooltip.energy", "机器能量");
    }
}
