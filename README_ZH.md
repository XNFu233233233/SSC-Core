# SSC Core (简单空岛纪实核心) - NeoForge 1.21.1

[English Version](./README.md)

![AI Driven](https://img.shields.io/badge/Developed%20with-AI-blueviolet)
![NeoForge](https://img.shields.io/badge/Minecraft-1.21.1-orange)
![License](https://img.shields.io/badge/License-MIT-green)

## 🤖 AI 开发声明

**本项目是一个极具实验性的 AI 全流程开发案例：**
本模组 **99%** 的内容均由人工智能生成，涵盖了代码实现、纹理绘制、数值设计及项目文档。

- **核心架构与逻辑：** 由 **Gemini 1.5 Pro** 及 **Gemini 2.0 Flash** 驱动构建。
- **辅助调试：** 部分逻辑优化由 **DeepSeek** 协作完成。

### ⚠️ 维护与贡献指南
由于本项目深度依赖 AI 驱动，开发者本人并不具备脱离 AI 独立进行大规模代码重构或底层调试的能力。
- **维护模式：** 所有修复与功能更新均需通过 AI 协同完成。
- **项目周期：** 本模组是整合包 **《简单空岛纪实 (Simple Skyblock Chronicle)》** 的核心驱动模组。我仅会在该整合包开发期间提供有限的维护支持。一旦整合包运行稳定，本项目将**停止后续维护**。
- **贡献建议：** 如果您希望改进此模组或进行长期扩展，**强烈建议您 Fork 本仓库**进行独立开发。

---

## 🧩 模组简介

**SSC Core** 是专为 **《简单空岛纪实》** 整合包定制的功能模组，旨在通过添加一系列独特的机器来丰富空岛生存的自动化流程与资源循环逻辑。

### 核心特性
*   **🛠️ 拆解台 (Deconstruction Table)：**
    *   **逆向工程：** 能够将物品分解为其原始合成材料。
    *   **双重逻辑：**
        *   **手动定义：** 允许通过配置文件或脚本精确指定特定的拆解产物。
        *   **自动派生：** 支持直接引用现有配方（通过 ID 或产出物品过滤），动态提取其原料作为拆解结果。
    *   **工业属性：** 支持自定义处理时间、能量消耗（FE）以及多级机器配置。
*   **🔌 KubeJS 深度集成：**
    *   模组包作者可以使用极简的 JS 语法定义复杂的拆解逻辑。
    *   支持 `deconstruction` 与 `from_recipe` 两种原生 API，完美对齐后端 Record 结构。

## 🛠️ 安装要求
*   **Minecraft:** 1.21.1
*   **NeoForge:** 21.1.x 或更高版本
*   **依赖项:** 
    *   [KubeJS](https://www.curseforge.com/minecraft/mc-mods/kubejs-neoforge) (强烈建议，用于自定义配方)

---

## 💖 鸣谢 (Acknowledgments)
*   本项目纹理基于 AI 生成，并参照了现代工业模组的视觉风格进行调优。
