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
| 1 | `outputs` | Array/String | List of outputs. Supports `'2x item'` format. |
| 2 | `input` | String | Input item. |
| 3 | `config` | Integer | (Optional) Config index. Defaults to 0. |

### Available Chainable Methods
| Method | Description |
| :--- | :--- |
| `.time(val)` | Sets processing time (ticks). |
| `.energy(val)` | Sets acceleration cost (FE). |

### Example
```javascript
event.recipes.ssccore.deconstruction(['3x minecraft:diamond', '2x minecraft:stick'], 'minecraft:diamond_pickaxe')
    .time(20)
    .energy(200)
```

---

## deconstruction_for_recipe - Derived
Maps to Mode B in JSON.

### Positional Parameters (Constructor)
| Position | Name | Type | Description |
| :--- | :--- | :--- | :--- |
| 1 | `recipeId` | String/Object | Target recipe ID or a full SourceFilter object. |
| 2 | `config` | Integer | (Optional) Config index. Defaults to 0. |

### Example
```javascript
// Basic derivation
event.recipes.ssccore.deconstruction_for_recipe('minecraft:furnace')

// Derivation with extra outputs (using object syntax)
event.recipes.ssccore.deconstruction_for_recipe({
    id: 'minecraft:golden_apple',
    outputs: ['minecraft:stick'] 
}, 1).time(40)
```
