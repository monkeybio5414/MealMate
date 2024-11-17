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
import com.comp3040.mealmate.Model.ItemsModel
import com.comp3040.mealmate.Model.SliderModel

class MainViewModel() : ViewModel() {

    private val firebaseDatabase = FirebaseDatabase.getInstance()

    private val _category = MutableLiveData<MutableList<CategoryModel>>()
    private val _banner = MutableLiveData<List<SliderModel>>()
    private val _recommended = MutableLiveData<MutableList<ItemsModel>>()

    val banners: LiveData<List<SliderModel>> = _banner
    val categories: LiveData<MutableList<CategoryModel>> = _category
    val recommended: LiveData<MutableList<ItemsModel>> = _recommended

    private val TAG = "MainViewModel"

    fun loadFiltered(id: String) {
        val Ref = firebaseDatabase.getReference("Items")
        val query: Query = Ref.orderByChild("categoryId").equalTo(id)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<ItemsModel>()
                Log.d(TAG, "loadFiltered: DataSnapshot size: ${snapshot.childrenCount}")
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(ItemsModel::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _recommended.value = lists
                Log.d(TAG, "loadFiltered: Loaded ${lists.size} items for categoryId $id")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "loadFiltered: Failed to load items for categoryId $id", error.toException())
            }
        })
    }

    fun loadRecommended() {
        val Ref = firebaseDatabase.getReference("Items")
        val query: Query = Ref.orderByChild("showRecommended").equalTo(true)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<ItemsModel>()
                Log.d(TAG, "loadRecommended: DataSnapshot size: ${snapshot.childrenCount}")
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(ItemsModel::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _recommended.value = lists
                Log.d(TAG, "loadRecommended: Loaded ${lists.size} recommended items")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "loadRecommended: Failed to load recommended items", error.toException())
            }
        })
    }

    fun loadBanners() {
        val Ref = firebaseDatabase.getReference("Banner")
        Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<SliderModel>()
                Log.d(TAG, "loadBanners: DataSnapshot size: ${snapshot.childrenCount}")
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(SliderModel::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _banner.value = lists
                Log.d(TAG, "loadBanners: Loaded ${lists.size} banners")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "loadBanners: Failed to load banners", error.toException())
            }
        })
    }

    fun loadCategory() {
        val Ref = firebaseDatabase.getReference("Category")
        Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<CategoryModel>()
                Log.d(TAG, "loadCategory: DataSnapshot size: ${snapshot.childrenCount}")
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(CategoryModel::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _category.value = lists
                Log.d(TAG, "loadCategory: Loaded ${lists.size} categories")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "loadCategory: Failed to load categories", error.toException())
            }
        })
    }
}
