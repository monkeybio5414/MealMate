package com.comp3040.mealmate.Model
data class DayPlan(
    var day: String = "", // Day of the week (e.g., "Monday", "Tuesday")
    var items: List<String> = mutableListOf() // Mutable list of meal titles (can be updated)
)
