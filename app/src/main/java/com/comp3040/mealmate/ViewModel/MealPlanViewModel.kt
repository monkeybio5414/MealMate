package com.comp3040.mealmate.ViewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.comp3040.mealmate.Model.DayPlan
import com.comp3040.mealmate.Model.MealDetailsModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth

class MealPlanViewModel(application: Application) : AndroidViewModel(application) {
    private fun getUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }
    val currentWeekPlan = mutableStateListOf<DayPlan>()
    val savedMealPlans = mutableStateListOf<String>()

    init {
        fetchMealPlans()
    }

    private fun fetchMealPlans() {
        val userId = getUserId()
        if (userId == null) {
            Log.e("MealPlanViewModel", "User not logged in.")
            return
        }

        val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId)

        // Fetch current week plan
        databaseReference.child("currentWeekPlan").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val weekPlan = snapshot.children.mapNotNull { daySnapshot ->
                        daySnapshot.getValue(DayPlan::class.java)
                    }
                    currentWeekPlan.clear()
                    currentWeekPlan.addAll(weekPlan)
                    Log.d("MealPlanViewModel", "Current Week Plan fetched: $weekPlan")
                } else {
                    currentWeekPlan.clear()
                    Log.d("MealPlanViewModel", "No Current Week Plan found.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MealPlanViewModel", "Error fetching Current Week Plan: ${error.message}")
            }
        })

        // Fetch saved meal plans
        databaseReference.child("mealPlans").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val plans = mutableListOf<String>()
                    for (planSnapshot in snapshot.children) {
                        val planName = planSnapshot.child("name").getValue(String::class.java)
                        val planId = planSnapshot.child("planID").getValue(String::class.java)
                        val meals = planSnapshot.child("meals").children.map { mealSnapshot ->
                            val day = mealSnapshot.child("day").getValue(String::class.java)
                            val items = mealSnapshot.child("items").children.map { it.getValue(String::class.java) }
                            "Day: $day, Items: $items"
                        }

                        if (planName != null) {
                            plans.add(planName)
                            Log.d("MealPlanViewModel", "Plan Name: $planName, Plan ID: $planId, Meals: $meals")
                        }
                    }
                    savedMealPlans.clear()
                    savedMealPlans.addAll(plans)
                    Log.d("MealPlanViewModel", "Saved Meal Plans fetched: $plans")
                } else {
                    savedMealPlans.clear()
                    Log.d("MealPlanViewModel", "No Saved Meal Plans found.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MealPlanViewModel", "Error fetching Saved Meal Plans: ${error.message}")
            }
        })
    }





    fun saveCurrentWeekAsPlan(onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val newPlanName = "Plan ${savedMealPlans.size + 1}"
            delay(200) // Simulate saving process
            savedMealPlans.add(newPlanName)
            onSuccess(true) // Simulate success
        }
    }

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
                                    planSnapshot.ref.removeValue().addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            savedMealPlans.remove(planName)
                                            onComplete(true)
                                            Log.d("MealPlanViewModel", "Plan '$planName' removed successfully.")
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

    fun selectSavedPlan(planName: String, onComplete: (Boolean) -> Unit) {
        val userId = getUserId()
        if (userId == null) {
            Log.e("MealPlanViewModel", "User not logged in.")
            onComplete(false)
            return
        }

        val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("mealPlans")

        databaseReference.orderByChild("name").equalTo(planName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val selectedPlan = snapshot.children.firstOrNull() // Get the first matching plan
                    val dayPlans = selectedPlan?.child("meals")?.children?.mapNotNull { daySnapshot ->
                        val day = daySnapshot.child("day").getValue(String::class.java)
                        val items = daySnapshot.child("items").children.mapNotNull { it.getValue(String::class.java) }
                        if (day != null) DayPlan(day, items) else null
                    } ?: emptyList()

                    currentWeekPlan.clear()
                    currentWeekPlan.addAll(dayPlans)

                    Log.d("MealPlanViewModel", "Selected Plan '$planName': $dayPlans")
                    onComplete(true)
                } else {
                    Log.d("MealPlanViewModel", "Plan '$planName' not found.")
                    onComplete(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MealPlanViewModel", "Error selecting plan '$planName': ${error.message}")
                onComplete(false)
            }
        })
    }


    fun addItemToPlan(item: MealDetailsModel, planId: String, onComplete: (Boolean) -> Unit) {
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
            .child(planId)
            .child("meals")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                databaseReference.get().addOnSuccessListener { snapshot ->
                    val updatedMeals = snapshot.children.mapNotNull { it.getValue(DayPlan::class.java) }.toMutableList()

                    val dayPlan = updatedMeals.find { it.day == item.day }
                    if (dayPlan != null) {
                        val updatedItems = dayPlan.items.toMutableList().apply { add(item.title) }
                        val updatedDayPlan = dayPlan.copy(items = updatedItems)
                        updatedMeals[updatedMeals.indexOf(dayPlan)] = updatedDayPlan
                    } else {
                        updatedMeals.add(DayPlan(item.day, listOf(item.title)))
                    }

                    databaseReference.setValue(updatedMeals).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            currentWeekPlan.clear()
                            currentWeekPlan.addAll(updatedMeals)
                            onComplete(true)
                        } else {
                            onComplete(false)
                        }
                    }
                }.addOnFailureListener {
                    onComplete(false)
                }
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

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



}
