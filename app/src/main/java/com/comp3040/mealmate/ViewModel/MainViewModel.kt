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

class MainViewModel : ViewModel() {

    private val firebaseDatabase = FirebaseDatabase.getInstance()

    private val _category = MutableLiveData<MutableList<CategoryModel>>()
    private val _banner = MutableLiveData<List<SliderModel>>()
    private val _recommended = MutableLiveData<MutableList<MealDetailsModel>>()

    val banners: LiveData<List<SliderModel>> = _banner
    val categories: LiveData<MutableList<CategoryModel>> = _category
    val recommended: LiveData<MutableList<MealDetailsModel>> = _recommended

    private val TAG = "MainViewModel"

    fun loadFiltered(id: String) {
        val Ref = firebaseDatabase.getReference("Items")
        val query: Query = Ref.orderByChild("categoryId").equalTo(id)
        Log.d(TAG, "loadFiltered: Querying items for categoryId: $id")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<MealDetailsModel>()
                Log.d(TAG, "loadFiltered: DataSnapshot size: ${snapshot.childrenCount}")
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(MealDetailsModel::class.java)
                    if (list != null) {
                        lists.add(list)
                        Log.d(TAG, "loadFiltered: Added item - $list")
                    } else {
                        Log.d(TAG, "loadFiltered: Skipped null item")
                    }
                }
                _recommended.value = lists
                Log.d(TAG, "loadFiltered: Total items loaded for categoryId $id: ${lists.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "loadFiltered: Failed to load items for categoryId $id", error.toException())
            }
        })
    }

    fun loadRecommended() {
        val Ref = firebaseDatabase.getReference("MealDetails")
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


        val query: Query = Ref.orderByChild("showRecommended").equalTo(true)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { child ->
                    val showRecommended = child.child("showRecommended").value
                    Log.d(TAG, "Key: ${child.key}, showRecommended: $showRecommended")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Query failed", error.toException())
            }
        })

        Log.d(TAG, "loadRecommended: Querying recommended items...")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "loadRecommended: DataSnapshot size: ${snapshot.childrenCount}")
                if (snapshot.childrenCount.toInt() == 0) {
                    Log.d(TAG, "loadRecommended: No items found with 'showRecommended = true'")
                }

                val lists = mutableListOf<MealDetailsModel>()
                snapshot.children.forEach { childSnapshot ->
                    val list = childSnapshot.getValue(MealDetailsModel::class.java)
                    if (list != null) {
                        lists.add(list)
                        Log.d(TAG, "loadRecommended: Added item - $list")
                    } else {
                        Log.d(TAG, "loadRecommended: Failed to parse item for key: ${childSnapshot.key}")
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

    fun loadBanners() {
        val Ref = firebaseDatabase.getReference("Banner")
        Log.d(TAG, "loadBanners: Querying banners...")

        Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<SliderModel>()
                Log.d(TAG, "loadBanners: DataSnapshot size: ${snapshot.childrenCount}")
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(SliderModel::class.java)
                    if (list != null) {
                        lists.add(list)
                        Log.d(TAG, "loadBanners: Added banner - $list")
                    } else {
                        Log.d(TAG, "loadBanners: Skipped null banner")
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

    fun loadCategory() {
        val Ref = firebaseDatabase.getReference("Category")
        Log.d(TAG, "loadCategory: Querying categories...")

        Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<CategoryModel>()
                Log.d(TAG, "loadCategory: DataSnapshot size: ${snapshot.childrenCount}")
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(CategoryModel::class.java)
                    if (list != null) {
                        lists.add(list)
                        Log.d(TAG, "loadCategory: Added category - $list")
                    } else {
                        Log.d(TAG, "loadCategory: Skipped null category")
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

