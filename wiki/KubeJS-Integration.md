# KubeJS Integration Guide

SSC Core features deep KubeJS integration, allowing you to extend deconstruction rules using a streamlined JavaScript API.

---

### 📖 Quick Navigation
*   [API Rules](#api-rules)
*   [`deconstruction`](#deconstruction---manual)
*   [`deconstruction_for_recipe`](#deconstruction_for_recipe---derived)

---

## API Rules
To keep scripts concise, we follow a **"Lean Constructor + Chainable Calls"** pattern:
*   **Required**: Placed inside `()`.
*   **Optional**: Appended using `.methodName()`.
*   **Namespace**: All methods are under `event.recipes.ssccore`.

---

## deconstruction - Manual
Maps to Mode A in JSON.

### Positional Parameters (Constructor)
| Position | Name | Type | Description |
| :--- | :--- | :--- | :--- |
| 1 | `outputs` | Array/String | List of outputs. Supports `'2x item'` or `['item1', 'item2']`. |
| 2 | `input` | String | Input item ID. |
| 3 | `config` | Integer | (Optional) Config index. Defaults to 0. |

### Available Chainable Methods
| Method | Description |
| :--- | :--- |
| `.time(val)` | Sets processing time (ticks). Default: 5. |
| `.energy(val)` | Sets acceleration cost (FE). Default: 100. |

### Example
```javascript
ServerEvents.recipes(event => {
    // Basic deconstruction
    event.recipes.ssccore.deconstruction(['3x minecraft:diamond', '2x minecraft:stick'], 'minecraft:diamond_pickaxe')
        .time(20)
        .energy(200)
    
    // With config index (only active when machine GUI shows 5)
    event.recipes.ssccore.deconstruction('minecraft:gold_ingot', 'minecraft:golden_helmet', 5)
})
```

---

## deconstruction_for_recipe - Derived
Maps to Mode B in JSON. Currently, KubeJS supports referencing via **Recipe ID** or **SourceFilter Object**.

### Positional Parameters (Constructor)
| Position | Name | Type | Description |
| :--- | :--- | :--- | :--- |
| 1 | `source` | String/Object | Target recipe ID or full SourceFilter object. |
| 2 | `extraOutputs` | Array/String | (Optional) Forcefully added extra items. |
| 3 | `config` | Integer | (Optional) Config index. Defaults to 0. |

### Example
```javascript
ServerEvents.recipes(event => {
    // Reverse furnace recipe
    event.recipes.ssccore.deconstruction_for_recipe('minecraft:furnace')

    // Reverse golden apple and force extra stick drop
    // Syntax: (recipeID, [extraOutputs], config)
    event.recipes.ssccore.deconstruction_for_recipe('minecraft:golden_apple', ['minecraft:stick'], 1)
        .time(40)
        .energy(500)
})
```
