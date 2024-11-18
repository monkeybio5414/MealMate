package com.comp3040.mealmate.Model

class IngredientRepository {
    private val ingredients = mutableListOf<IngredientModel>()

    fun addIngredient(ingredient: IngredientModel) {
        ingredients.add(ingredient)
    }

    fun getAllIngredients(): List<IngredientModel> {
        return ingredients
    }

    fun clearIngredients() {
        ingredients.clear()
    }
}
