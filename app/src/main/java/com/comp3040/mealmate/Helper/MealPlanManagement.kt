package com.example.project1762.Helper

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.comp3040.mealmate.Helper.TinyDB
import com.comp3040.mealmate.Model.ItemsModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MealPlanManagement(private val context: Context) {

    private val tinyDB = TinyDB(context)

    // Insert an item into the meal plan
    fun insertItem(item: ItemsModel) {
        val mealPlanList = getMealPlan()
        val existAlready = mealPlanList.any { it.title == item.title }
        val index = mealPlanList.indexOfFirst { it.title == item.title }

        if (existAlready) {
            mealPlanList[index].day = item.day
        } else {
            mealPlanList.add(item)
        }
        saveMealPlan(mealPlanList)
        Log.d("MealPlanManagement", "Inserted Item: $item")
        Toast.makeText(context, "Added to your Meal Plan", Toast.LENGTH_SHORT).show()
    }

    // Get all items in the meal plan
    fun getMealPlan(): ArrayList<ItemsModel> {
        val mealPlanList = tinyDB.getListObject("MealPlanList") ?: arrayListOf()
        Log.d("MealPlanManagement", "Retrieved Meal Plan: $mealPlanList")
        return mealPlanList
    }

    // Remove a specific item from the meal plan
    fun removeItem(item: ItemsModel) {
        val mealPlanList = getMealPlan()
        mealPlanList.removeIf { it.title == item.title }
        saveMealPlan(mealPlanList)
        Log.d("MealPlanManagement", "Removed Item: $item. Updated Plan: $mealPlanList")
        Toast.makeText(context, "${item.title} removed from your meal plan", Toast.LENGTH_SHORT).show()
    }

    // Update an item in the meal plan
    fun updateItem(item: ItemsModel) {
        val mealPlanList = getMealPlan()
        val index = mealPlanList.indexOfFirst { it.title == item.title }
        if (index != -1) {
            mealPlanList[index] = item
            saveMealPlan(mealPlanList)
            Log.d("MealPlanManagement", "Updated Item: $item")
        }
    }

    // Save the meal plan list persistently
    private fun saveMealPlan(mealPlanList: ArrayList<ItemsModel>) {
        tinyDB.putListObject("MealPlanList", mealPlanList)
        Log.d("MealPlanManagement", "Saved Meal Plan: $mealPlanList")
    }

    // Clear the entire meal plan
    fun clearMealPlan() {
        tinyDB.putListObject("MealPlanList", arrayListOf<ItemsModel>())
        Log.d("MealPlanManagement", "Cleared Meal Plan")
        Toast.makeText(context, "Meal plan cleared", Toast.LENGTH_SHORT).show()
    }

    // Remove a saved plan
    fun removeSavedPlan(planName: String) {
        val currentPlans = getSavedPlans().toMutableList()
        currentPlans.remove(planName)
        saveSavedPlans(currentPlans)
        Log.d("MealPlanManagement", "Removed Saved Plan: $planName. Remaining Plans: $currentPlans")
    }



    private fun saveSavedPlans(plans: List<String>) {
        tinyDB.putListString("SavedMealPlans", ArrayList(plans))
        Log.d("MealPlanManagement", "Persisted Saved Plans: $plans")
    }



    fun loadPlan(planName: String): List<ItemsModel> {
        val meals = getSavedPlansWithMeals()[planName] ?: emptyList()
        Log.d("MealPlanManagement", "Loaded Plan: $planName with Meals: $meals")
        return meals
    }

    fun removePlan(planName: String) {
        val savedPlans = getSavedPlansWithMeals()
        savedPlans.remove(planName)
        saveSavedPlansWithMeals(savedPlans)
        Log.d("MealPlanManagement", "Removed Plan: $planName. Remaining Plans: $savedPlans")
    }





    fun saveCurrentWeekPlan(currentWeekPlan: List<ItemsModel>) {
        tinyDB.putListObject("currentWeekPlan", ArrayList(currentWeekPlan))
        Log.d("MealPlanManagement", "Saved Current Week Plan: $currentWeekPlan")
    }

    fun getSavedCurrentWeekPlan(): List<ItemsModel> {
        val currentWeekPlan = tinyDB.getListObject("currentWeekPlan")
        Log.d("MealPlanManagement", "Retrieved Current Week Plan: $currentWeekPlan")
        return currentWeekPlan
    }

    fun savePlanWithMeals(planName: String, meals: List<ItemsModel>) {
        val savedPlans = getSavedPlansWithMeals().toMutableMap()
        savedPlans[planName] = meals // Add the new plan with meals
        saveSavedPlansWithMeals(savedPlans) // Persist the updated map
    }

    fun getSavedPlansWithMeals(): MutableMap<String, List<ItemsModel>> {
        val json = tinyDB.getString("SavedPlansWithMeals") ?: "{}"
        val type = object : TypeToken<MutableMap<String, List<ItemsModel>>>() {}.type
        return Gson().fromJson(json, type) ?: mutableMapOf()
    }

    private fun saveSavedPlansWithMeals(plans: Map<String, List<ItemsModel>>) {
        tinyDB.putString("SavedPlansWithMeals", Gson().toJson(plans))
    }

    fun getSavedPlans(): List<String> {
        return getSavedPlansWithMeals().keys.toList()
    }

}

