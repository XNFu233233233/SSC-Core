# Custom Recipe Reference (JSON)

This guide details all definition modes for the Deconstruction Table (`ssccore:deconstruction`).

---

### 📖 Quick Navigation
*   [Common Properties](#common-properties)
*   [Mode A: Manual Mode](#mode-a-manual-mode)
*   [Mode B: Derived Mode](#mode-b-derived-mode)
*   [Core Mechanics: Batch Processing](#core-mechanics-batch-processing)

---

## Common Properties
Regardless of the mode, these optional fields can be used to control machine performance.

| Property | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `time` | Integer | `5` | **Processing Time (ticks)**: Duration required for one deconstruction cycle. |
| `energy` | Integer | `100` | **Acceleration Cost (FE)**: If sufficient FE is stored, it consumes this amount to complete instantly in **1 tick**. |
| `config` | Integer | `0` | **Config Index**: The recipe only works if the machine's GUI number matches this. |

---

## Mode A: Manual Mode
Allows full customization of specific inputs and multiple corresponding outputs.

### Properties
| Property | Type | Required | Description |
| :--- | :--- | :--- | :--- |
| `input` | Object | Yes | The input item or tag (Ingredient). |
| `outputs` | Array | Yes | List of items produced upon deconstruction (ItemStack). |

### Example
```json
{
  "type": "ssccore:deconstruction",
  "input": { "item": "minecraft:diamond_pickaxe" },
  "outputs": [
    { "count": 3, "id": "minecraft:diamond" },
    { "count": 2, "id": "minecraft:stick" }
  ],
  "time": 20,
  "energy": 2000
}
```

---

## Mode B: Derived Mode
The core feature of this mod: reverse existing "crafting" or "smelting" recipes automatically.

### `recipe` Object Properties
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | String | **Precise Reference**: Specify the exact recipe ID to reverse (e.g., `minecraft:chest`). |
| `output` | String | **Fuzzy Match**: Automatically find a recipe that produces this item. |
| `type` | String | **Type Filter**: Used with `output` to narrow down the recipe type. |
| `outputs` | Array | **Extra Outputs**: Forcefully adds items in addition to the reversed ingredients. |

### Example (Referencing by ID)
```json
{
  "type": "ssccore:deconstruction",
  "recipe": {
    "id": "minecraft:golden_apple",
    "outputs": [
      { "count": 1, "id": "minecraft:apple" } 
    ]
  }
}
```

---

## Core Mechanics: Batch Processing
1.  **Auto Packaging**: The machine scans the input slot and processes up to 64 items in a single batch based on available output space.
2.  **Fixed Cost**: A batch costs the same amount of time and energy regardless of whether it contains 1 or 64 items.
3.  **Instant Completion**: If the machine has enough FE to cover the batch's `energy` cost, it completes the process in **1 Tick**.

---

## Maintenance & Destruction
*   **Block Recovery**: The Deconstruction Table **drops itself** normally when broken.
*   **Item Safety**: When broken, all contents (input, output, and items currently in the processing buffer) will drop to the ground.

---

## Troubleshooting
*   **Ambiguous source!**: This occurs when one output item corresponds to multiple recipes. Specify a `type` (e.g., `minecraft:crafting_shaped`) in the `recipe` object or use a specific `id`.
*   **Reverse deconstruction is NOT supported for multiple input options**: If a source recipe uses a Tag that represents multiple different items, the system cannot determine which one to return. Automatic derivation will fail for such recipes; use "Manual Mode" instead.
