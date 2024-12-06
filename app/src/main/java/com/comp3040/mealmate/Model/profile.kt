package com.comp3040.mealmate.Model

/**
 * Represents a user in the MealMate application.
 *
 * @property userId Unique identifier for the user.
 * @property name The name of the user.
 * @property email The email address of the user.
 * @property profilePicture URL to the user's profile picture (nullable).
 * @property dietaryPreferences List of dietary preferences specified by the user.
 * @property mealPlans A map of meal plans associated with the user, where the key is the plan ID and the value is the MealPlan object.
 * @property createdAt The account creation date in string format.
 * @property updatedAt The last profile update timestamp in string format (nullable).
 */
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

/**
 * Represents a meal plan created by a user.
 *
 * @property name The name of the meal plan (e.g., "Weekday Plan").
 * @property planID Unique identifier for the meal plan.
 * @property meals A list of meals organized by days of the week.
 * @property highlighted Indicates whether the meal plan is marked as highlighted or favorite.
 */
data class MealPlan(
    var name: String = "", // Meal plan name
    var planID: String = "", // Meal plan ID
    var meals: List<DayPlan> = emptyList(), // List of meals for each day
    var highlighted: Boolean = false // Indicates if the meal plan is highlighted
)
