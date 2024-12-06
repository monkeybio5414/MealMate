package com.comp3040.mealmate.Model

/**
 * Data class representing a category in the application.
 * Each category has a title, an ID, and a picture URL.
 * @property title The name/title of the category. Defaults to an empty string.
 * @property id A unique identifier for the category. Defaults to 0.
 * @property picUrl The URL of the image associated with the category. Defaults to an empty string.
 */
data class CategoryModel(
    val title: String = "", // The name/title of the category
    val id: Int = 0,        // The unique identifier for the category
    val picUrl: String = "" // The URL for the category image
)
