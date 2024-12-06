package com.comp3040.mealmate.Model

/**
 * Represents an item in a shopping list.
 *
 * @property id Unique identifier for the shopping list item.
 * @property itemName The name of the item (e.g., "Milk", "Apples").
 * @property quantity The quantity of the item (e.g., "2 liters", "1 kg").
 * @property category The category of the item (e.g., "Dairy", "Fruits").
 * @property isChecked Indicates whether the item has been checked off the list.
 */
data class ShoppingListItem(
    val id: String = "", // Unique identifier
    val itemName: String = "", // Name of the item
    val quantity: String = "", // Quantity of the item
    val category: String = "", // Category of the item
    val isChecked: Boolean = false // Whether the item is checked off
)
