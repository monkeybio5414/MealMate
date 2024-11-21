package com.comp3040.mealmate.Model

data class UserModel(
    val userId: String, // Unique identifier for the user
    var name: String = "", // User's name
    var email: String = "", // User's email address
    var profilePicture: String? = null, // URL to the user's profile picture
    var dietaryPreferences: List<String> = emptyList(), // List of dietary preferences
    var mealPlans: Map<String, MealPlan> = emptyMap(), // User's meal plans (a map of planID to MealPlan)
    var createdAt: String = "", // Account creation date
    var updatedAt: String? = null // Last profile update timestamp
)

data class MealPlan(
    var name: String = "", // Meal plan name
    var planID: String = "", // Meal plan ID
    var meals: List<DayPlan> = emptyList() // List of meals for each day
)

