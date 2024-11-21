package com.comp3040.mealmate.Model

data class ShoppingListItem(
    val id: String = "", // Unique identifier
    val itemName: String = "",
    val quantity: String = "",
    val category: String = "",
    val isChecked: Boolean = false
)