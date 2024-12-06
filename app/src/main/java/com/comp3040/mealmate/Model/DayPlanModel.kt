package com.comp3040.mealmate.Model

/**
 * Data class representing a meal plan for a specific day of the week.
 * Each day plan contains the name of the day and a list of meal titles.
 *
 * @property day The name of the day (e.g., "Monday", "Tuesday").
 * @property items A mutable list of meal titles planned for the day. Defaults to an empty list.
 */
data class DayPlan(
    var day: String = "",                // Name of the day (e.g., "Monday", "Tuesday")
    var items: List<String> = mutableListOf() // List of meal titles planned for the day
)
