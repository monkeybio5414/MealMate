package com.comp3040.mealmate.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.comp3040.mealmate.Model.CategoryModel
import com.comp3040.mealmate.Model.MealDetailsModel
import com.comp3040.mealmate.Model.SliderModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class MainViewModel : ViewModel() {
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    // LiveData for storing categories, banners, and recommended meals
    private val _category = MutableLiveData<MutableList<CategoryModel>>()
    private val _banner = MutableLiveData<List<SliderModel>>()
    private val _recommended = MutableLiveData<MutableList<MealDetailsModel>>()

    val banners: LiveData<List<SliderModel>> = _banner
    val categories: LiveData<MutableList<CategoryModel>> = _category
    val recommended: LiveData<MutableList<MealDetailsModel>> = _recommended

    private val TAG = "MainViewModel" // Tag for logging purposes

    /**
     * Loads meals filtered by the given category ID.
     * @param categoryId The ID of the category to filter meals by.
     */
    fun loadFiltered(categoryId: String) {
        Log.d("MainViewModel", "loadFiltered: Querying items for categoryId: $categoryId")
        val query =
            databaseReference.child("MealDetails").orderByChild("categoryId").equalTo(categoryId)

        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                if (dataSnapshot.exists()) {
                    val items =
                        dataSnapshot.children.mapNotNull { it.getValue(MealDetailsModel::class.java) }
                    _recommended.postValue(items.toMutableList())
                    Log.d("MainViewModel", "Items loaded: ${items.size}")
                } else {
                    Log.d("MainViewModel", "No data found for categoryId: $categoryId")
                    _recommended.postValue(mutableListOf())
                }
            } else {
                Log.e(
                    "MainViewModel",
                    "Error querying items for categoryId: $categoryId",
                    task.exception
                )
            }
        }
    }

    /**
     * Loads all recommended meals where `showRecommended = true`.
     */
    fun loadRecommended() {
        val Ref = firebaseDatabase.getReference("MealDetails")

        // Log initial data for debugging
        Ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { child ->
                    Log.d(TAG, "Child Key: ${child.key}, Data: ${child.value}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "loadRecommended: Failed to fetch data", error.toException())
            }
        })

        // Query meals with `showRecommended = true`
        val query: Query = Ref.orderByChild("showRecommended").equalTo(true)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<MealDetailsModel>()
                snapshot.children.forEach { childSnapshot ->
                    val list = childSnapshot.getValue(MealDetailsModel::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _recommended.value = lists
                Log.d(TAG, "loadRecommended: Total recommended items loaded: ${lists.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "loadRecommended: Failed to load recommended items", error.toException())
            }
        })
    }

    /**
     * Loads all banners from the "Banner" node in Firebase.
     */
    fun loadBanners() {
        val Ref = firebaseDatabase.getReference("Banner")
        Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<SliderModel>()
                snapshot.children.forEach { childSnapshot ->
                    val list = childSnapshot.getValue(SliderModel::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _banner.value = lists
                Log.d(TAG, "loadBanners: Total banners loaded: ${lists.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "loadBanners: Failed to load banners", error.toException())
            }
        })
    }

    /**
     * Loads all categories from the "Category" node in Firebase.
     */
    fun loadCategory() {
        val Ref = firebaseDatabase.getReference("Category")
        Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<CategoryModel>()
                snapshot.children.forEach { childSnapshot ->
                    val list = childSnapshot.getValue(CategoryModel::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _category.value = lists
                Log.d(TAG, "loadCategory: Total categories loaded: ${lists.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "loadCategory: Failed to load categories", error.toException())
            }
        })
    }
}