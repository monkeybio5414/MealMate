package com.comp3040.mealmate.Model

data class Profile(
    val userId: String, // Unique identifier for the user
    var name: String = "Guest", // User's name
    var email: String = "", // User's email address
    var phone: String? = null, // Optional phone number
    var avatarUrl: String? = null, // URL to the user's avatar/profile picture
    var preferences: Preferences = Preferences(), // User-specific preferences
    var createdAt: String = "", // Account creation date
    var updatedAt: String? = null // Last profile update timestamp
)

data class Preferences(
    var preferredLanguage: String = "en", // Default language (e.g., "en" for English)
    var dietaryRestrictions: List<String> = emptyList(), // List of dietary restrictions (e.g., ["vegan", "gluten-free"])
    var favoriteCuisines: List<String> = emptyList(), // List of favorite cuisines (e.g., ["Italian", "Mexican"])
    var notificationEnabled: Boolean = true // Toggle for notifications
)
