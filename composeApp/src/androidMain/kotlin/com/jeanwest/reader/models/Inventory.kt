package com.jeanwest.reader.models

/**
 * Represents an inventory record.
 *
 * @property inventoryId A unique identifier for the inventory.
 * @property creatorName The name of the user who created the inventory.
 * @property standardDate A standardized date format for the inventory (e.g., a database timestamp).  This is likely a fixed or initial date, not meant for modification.
 * @property date The date associated with the inventory. This can be different from the creation date and may be user-editable.
 * @property des A description of the inventory.  This likely holds details about the inventory's purpose or contents.
 * @property selected A boolean flag indicating whether the inventory is currently selected in the UI (default: `false`).
 * @property warehouse The name or identifier of the warehouse where the inventory is located.
 * @property inventoryItem A mutable list of [InventoryItem] objects, representing the items contained in this inventory (default: an empty list).  This allows for dynamic addition and removal of items.
 */
class Inventory(
    var inventoryId : String,
    var creatorName : String,
    val standardDate: String,
    var date : String,
    var des: String,
    var selected: Boolean = false,
    var warehouse: String,
    val inventoryItem: MutableList<InventoryItem> = mutableListOf()
)