package com.comp3040.mealmate.ViewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.comp3040.mealmate.Model.DayPlan
import com.comp3040.mealmate.Model.MealDetailsModel
import com.comp3040.mealmate.Model.MealPlan
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth

/**
 * ViewModel for managing meal plans. This ViewModel interacts with Firebase to handle CRUD operations
 * related to meal plans, highlighting specific plans, and managing daily meals within the plans.
 */
class MealPlanViewModel(application: Application) : AndroidViewModel(application) {

    // Firebase Authentication helper to get the current user's ID
    private fun getUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    // State variables for managing meal plans
    val currentWeekPlan = mutableStateListOf<DayPlan>() // Current week's plan (daily meal list)
    val savedMealPlans = mutableStateListOf<MealPlan>() // All saved meal plans for the user
    var highlightedPlanId = mutableStateOf<String?>(null) // The ID of the highlighted meal plan

    // Initialize by fetching existing meal plans
    init {
        fetchMealPlans()
    }

    /**
     * Fetch all meal plans from Firebase for the current user.
     * Automatically highlights the first plan if none is highlighted.
     */
    fun fetchMealPlans() {
        val userId = getUserId() ?: return

        val databaseReference = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("mealPlans")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val plans = mutableListOf<MealPlan>()
                for (planSnapshot in snapshot.children) {
                    val plan = planSnapshot.getValue(MealPlan::class.java)
                    if (plan != null) plans.add(plan)
                }

                savedMealPlans.clear()
                savedMealPlans.addAll(plans)

                val highlightedPlan = plans.find { it.highlighted }
                if (highlightedPlan != null) {
                    highlightedPlanId.value = highlightedPlan.planID
                    selectSavedPlan(highlightedPlan.planID)
                } else if (plans.isNotEmpty()) {
                    // Default to the first plan if no plan is highlighted
                    val firstPlan = plans.first()
                    highlightedPlanId.value = firstPlan.planID
                    highlightMealPlan(firstPlan.planID)
                    selectSavedPlan(firstPlan.planID)
                }

                Log.d("MealPlanViewModel", "Fetched meal plans: $plans")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MealPlanViewModel", "Failed to fetch meal plans: ${error.message}")
            }
        })
    }

    /**
     * Highlights the specified meal plan in Firebase.
     * Only one plan can be highlighted at a time.
     *
     * @param planId The ID of the plan to highlight.
     */
    fun highlightMealPlan(planId: String) {
        val userId = getUserId() ?: return

        val databaseReference = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("mealPlans")

        // Update the highlighted plan locally
        highlightedPlanId.value = planId

        viewModelScope.launch(Dispatchers.IO) {
            databaseReference.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    snapshot.children.forEach { planSnapshot ->
                        val currentPlanId = planSnapshot.child("planID").getValue(String::class.java)
                        val isHighlighted = currentPlanId == planId
                        planSnapshot.ref.child("highlighted").setValue(isHighlighted)
                    }
                }
            }.addOnFailureListener { exception ->
                Log.e("MealPlanViewModel", "Failed to highlight plan: ${exception.message}")
            }
        }
    }

    /**
     * Creates a new meal plan with default values (7 days of empty plans).
     * Automatically highlights the new plan and resets the highlighting for others.
     */
    fun createNewMealPlan() {
        val userId = getUserId() ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                val newPlan = daysOfWeek.map { DayPlan(day = it) }

                // Generate a unique plan name
                val existingNames = savedMealPlans.map { it.name }
                val newPlanName = generateUniquePlanName(existingNames)

                val newPlanId = System.currentTimeMillis().toString()

                val mealPlan = MealPlan(
                    name = newPlanName,
                    planID = newPlanId,
                    meals = newPlan,
                    highlighted = true
                )

                val databaseReference = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("mealPlans")
                    .child(newPlanId)

                databaseReference.setValue(mealPlan).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Reset highlighting in other plans
                        savedMealPlans.forEach { it.highlighted = false }
                        savedMealPlans.add(mealPlan)

                        viewModelScope.launch(Dispatchers.Main) {
                            highlightMealPlan(newPlanId)
                            Log.d("MealPlanViewModel", "New meal plan '$newPlanName' created successfully.")
                        }
                    } else {
                        Log.e("MealPlanViewModel", "Failed to create new meal plan.")
                    }
                }
            } catch (e: Exception) {
                Log.e("MealPlanViewModel", "Exception creating new meal plan: ${e.message}")
            }
        }
    }

    /**
     * Generates a unique name for a new meal plan by appending a suffix to "Plan".
     *
     * @param existingNames A list of existing plan names.
     * @return A unique plan name.
     */
    private fun generateUniquePlanName(existingNames: List<String>): String {
        var suffix = 1
        var newPlanName: String

        do {
            newPlanName = "Plan $suffix"
            suffix++
        } while (existingNames.contains(newPlanName))

        return newPlanName
    }

    /**
     * Selects a saved meal plan by its ID and loads the corresponding daily meals into the current week's plan.
     *
     * @param planId The ID of the meal plan to select.
     */
    fun selectSavedPlan(planId: String) {
        val userId = getUserId() ?: return

        val databaseReference = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("mealPlans")
            .child(planId)

        viewModelScope.launch(Dispatchers.IO) {
            databaseReference.child("meals").get().addOnSuccessListener { snapshot ->
                val dayPlans = snapshot.children.mapNotNull { daySnapshot ->
                    val day = daySnapshot.child("day").getValue(String::class.java)
                    val items = daySnapshot.child("items").children.mapNotNull { it.getValue(String::class.java) }
                    if (day != null) DayPlan(day, items) else null
                }

                currentWeekPlan.clear()
                currentWeekPlan.addAll(dayPlans)
            }
        }
    }

    /**
     * Adds an item (meal) to the highlighted meal plan for a specific day.
     * If no day is specified, the default day is "Monday".
     *
     * @param item The meal details to be added.
     * @param onComplete Callback to indicate success or failure.
     */
    fun addItemToHighlightedPlan(item: MealDetailsModel, onComplete: (Boolean) -> Unit) {
        val userId = getUserId() ?: return

        val highlightedPlan = savedMealPlans.find { it.highlighted }
        if (highlightedPlan == null) {
            Log.e("MealPlanViewModel", "No highlighted plan selected.")
            onComplete(false)
            return
        }

        // Default to the first day if no day is specified
        val targetDay = item.day.takeIf { it.isNotBlank() } ?: "Monday"

        val databaseReference = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("mealPlans")
            .child(highlightedPlan.planID)
            .child("meals")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                databaseReference.get().addOnSuccessListener { snapshot ->
                    val updatedMeals = snapshot.children.mapNotNull { it.getValue(DayPlan::class.java) }.toMutableList()

                    // Add the item to the specified day
                    val dayPlan = updatedMeals.find { it.day == targetDay }
                    if (dayPlan != null) {
                        dayPlan.items = dayPlan.items + item.title
                    } else {
                        updatedMeals.add(DayPlan(day = targetDay, items = listOf(item.title)))
                    }

                    // Update Firebase
                    databaseReference.setValue(updatedMeals).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            currentWeekPlan.clear()
                            currentWeekPlan.addAll(updatedMeals)
                            Log.d(
                                "MealPlanViewModel",
                                "Item '${item.title}' added to day '$targetDay' in highlighted plan '${highlightedPlan.name}'."
                            )
                            onComplete(true)
                        } else {
                            Log.e("MealPlanViewModel", "Failed to update Firebase.")
                            onComplete(false)
                        }
                    }
                }.addOnFailureListener {
                    Log.e("MealPlanViewModel", "Failed to fetch meals: ${it.message}")
                    onComplete(false)
                }
            } catch (e: Exception) {
                Log.e("MealPlanViewModel", "Exception in adding item to highlighted plan: ${e.message}")
                onComplete(false)
            }
        }
    }

    /**
     * Updates an existing meal plan in Firebase with a new list of daily plans.
     *
     * @param planId The ID of the meal plan to update.
     * @param updatedPlan The updated list of daily plans.
     */
    fun updateOriginalPlan(planId: String, updatedPlan: List<DayPlan>) {
        val userId = getUserId()
        if (userId == null) {
            Log.e("MealPlanViewModel", "User not logged in.")
            return
        }

        val databaseReference = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("mealPlans")
            .child(planId)
            .child("meals")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                databaseReference.setValue(updatedPlan).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("MealPlanViewModel", "Successfully updated plan: $planId")
                    } else {
                        Log.e("MealPlanViewModel", "Failed to update plan: $planId")
                    }
                }
            } catch (e: Exception) {
                Log.e("MealPlanViewModel", "Error updating plan: ${e.message}")
            }
        }
    }

    /**
     * Reassigns a meal from one day to another within the current meal plan.
     * Updates Firebase with the changes.
     *
     * @param mealTitle The title of the meal to reassign.
     * @param oldDay The day the meal is currently assigned to.
     * @param newDay The day the meal should be reassigned to.
     * @param currentPlan The current meal plan.
     * @param planId The ID of the plan to update.
     */
    fun reassignMeal(
        mealTitle: String,
        oldDay: String,
        newDay: String,
        currentPlan: SnapshotStateList<DayPlan>,
        planId: String
    ) {
        val userId = getUserId() ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val oldDayPlan = currentPlan.find { it.day == oldDay }
            val newDayPlan = currentPlan.find { it.day == newDay }

            // Remove the meal from the old day
            if (oldDayPlan != null) {
                val updatedItems = oldDayPlan.items.toMutableList()
                updatedItems.remove(mealTitle)
                val updatedDayPlan = oldDayPlan.copy(items = updatedItems)
                val index = currentPlan.indexOf(oldDayPlan)
                if (index != -1) currentPlan[index] = updatedDayPlan // Trigger recomposition
            }

            // Add the meal to the new day
            if (newDayPlan != null) {
                val updatedItems = newDayPlan.items.toMutableList()
                updatedItems.add(mealTitle)
                val updatedDayPlan = newDayPlan.copy(items = updatedItems)
                val index = currentPlan.indexOf(newDayPlan)
                if (index != -1) currentPlan[index] = updatedDayPlan // Trigger recomposition
            } else {
                currentPlan.add(DayPlan(day = newDay, items = listOf(mealTitle)))
            }

            // Update Firebase
            val databaseReference = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("mealPlans")
                .child(planId)
                .child("meals")

            databaseReference.setValue(currentPlan).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("MealPlanViewModel", "Meal reassigned and updated successfully in Firebase.")
                } else {
                    Log.e("MealPlanViewModel", "Failed to reassign meal in Firebase.")
                }
            }
        }
    }

    /**
     * Removes a meal plan by its name. Updates the local list and Firebase accordingly.
     *
     * @param planName The name of the meal plan to remove.
     * @param onComplete Callback to indicate success or failure.
     */
    fun removePlan(planName: String, onComplete: (Boolean) -> Unit) {
        val userId = getUserId()
        if (userId == null) {
            Log.e("MealPlanViewModel", "User not logged in.")
            onComplete(false)
            return
        }

        val databaseReference = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("mealPlans")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                databaseReference.orderByChild("name").equalTo(planName)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (planSnapshot in snapshot.children) {
                                    val wasHighlighted = planSnapshot.child("highlighted").getValue(Boolean::class.java) ?: false

                                    planSnapshot.ref.removeValue().addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            viewModelScope.launch(Dispatchers.Main) {
                                                // Remove the plan from the local list
                                                savedMealPlans.removeIf { it.name == planName }

                                                if (wasHighlighted) {
                                                    // Reset highlighting and assign a new highlighted plan if available
                                                    if (savedMealPlans.isNotEmpty()) {
                                                        // Highlight the first plan in the list
                                                        val newHighlightedPlan = savedMealPlans.first()
                                                        highlightMealPlan(newHighlightedPlan.planID)
                                                    } else {
                                                        Log.d("MealPlanViewModel", "No plans left to highlight.")
                                                    }
                                                }

                                                onComplete(true)
                                                Log.d("MealPlanViewModel", "Plan '$planName' removed successfully.")
                                            }
                                        } else {
                                            onComplete(false)
                                            Log.e("MealPlanViewModel", "Failed to remove plan '$planName'.")
                                        }
                                    }
                                }
                            } else {
                                onComplete(false)
                                Log.e("MealPlanViewModel", "Plan '$planName' not found in the database.")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            onComplete(false)
                            Log.e("MealPlanViewModel", "Error removing plan: ${error.message}")
                        }
                    })
            } catch (e: Exception) {
                onComplete(false)
                Log.e("MealPlanViewModel", "Exception while removing plan: ${e.message}")
            }
        }
    }
}

