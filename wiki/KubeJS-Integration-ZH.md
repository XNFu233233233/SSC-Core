# KubeJS 集成指南

SSC Core 深度集成了 KubeJS，允许通过极简的 JS 语法扩展拆解逻辑。

---

### 📖 快速导航
*   [API 规范](#api-规范)
*   [`deconstruction`](#deconstruction---手动拆解)
*   [`deconstruction_for_recipe`](#deconstruction_for_recipe---配方派生)

---

## API 规范
为了保持脚本简洁，我们采用了 **“精简构造函数 + 链式调用”** 的设计模式：
*   **必填项**：放在括号 `()` 中。
*   **选填项**：使用 `.方法名()` 追加。
*   **命名空间**：所有方法均在 `event.recipes.ssccore` 下。

---

## deconstruction - 手动拆解
对应 JSON 中的模式 A。

### 位置参数 (构造函数)
| 位置 | 参数名 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| 1 | `outputs` | Array/String | 产出列表。支持 `'2x item'` 或 `['item1', 'item2']`。 |
| 2 | `input` | String | 输入物品 ID。 |
| 3 | `config` | Integer | (可选) 配置索引。默认为 0。 |

### 可用的链式方法
| 方法名 | 说明 |
| :--- | :--- |
| `.time(val)` | 设置处理时长 (ticks)。默认 5。 |
| `.energy(val)` | 设置加速能耗 (FE)。默认 100。 |

### 示例
```javascript
ServerEvents.recipes(event => {
    // 基础分解
    event.recipes.ssccore.deconstruction(['3x minecraft:diamond', '2x minecraft:stick'], 'minecraft:diamond_pickaxe')
        .time(20)
        .energy(200)
    
    // 带配置索引 (仅当机器 GUI 显示为 5 时生效)
    event.recipes.ssccore.deconstruction('minecraft:gold_ingot', 'minecraft:golden_helmet', 5)
})
```

---

## deconstruction_for_recipe - 配方派生
对应 JSON 中的模式 B。目前 KubeJS 仅支持通过 **配方 ID** 或 **SourceFilter 对象** 引用。

### 位置参数 (构造函数)
| 位置 | 参数名 | 类型 | 说明 |
| :--- | :--- | :--- | :--- |
| 1 | `source` | String/Object | 目标配方 ID 或完整对象。 |
| 2 | `extraOutputs` | Array/String | (可选) 强制增加的额外产出。 |
| 3 | `config` | Integer | (可选) 配置索引。默认为 0。 |

### 示例
```javascript
ServerEvents.recipes(event => {
    // 反转熔炉配方
    event.recipes.ssccore.deconstruction_for_recipe('minecraft:furnace')

    // 反转金苹果配方，并强制额外掉落木棍
    // 语法: (配方ID, [额外产出], 配置)
    event.recipes.ssccore.deconstruction_for_recipe('minecraft:golden_apple', ['minecraft:stick'], 1)
        .time(40)
        .energy(500)
})
```
