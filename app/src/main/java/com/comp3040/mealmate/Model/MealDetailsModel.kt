package com.comp3040.mealmate.Model

import android.os.Parcel
import android.os.Parcelable


data class MealDetailsModel(
    var title: String = "", // The name of the meal/recipe
    var description: String = "", // Description of the meal/recipe
    var picUrl: ArrayList<String> = ArrayList(), // List of image URLs
    var ingredients: ArrayList<String> = ArrayList(), // Updated from `items` to `ingredients`
    var calories: Int = 0, // Updated from price to calories
    var rating: Double = 0.0, // Rating of the meal/recipe
    var day: String = "", // Day for the meal (e.g., "Monday")
    var showRecommended: Boolean = false, // Whether the item is recommended
    var categoryId: String = "", // Category ID of the meal/recipe
    var steps: List<String> = listOf() // Preparation steps
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.createStringArrayList() as ArrayList<String>,
        parcel.createStringArrayList() as ArrayList<String>, // Ingredients
        parcel.readInt(), // Calories
        parcel.readDouble(),
        parcel.readString().toString(), // Day of the meal
        parcel.readByte() != 0.toByte(),
        parcel.readString().toString(),
        parcel.createStringArrayList() ?: listOf()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeStringList(picUrl)
        parcel.writeStringList(ingredients) // Ingredients
        parcel.writeInt(calories) // Calories
        parcel.writeDouble(rating)
        parcel.writeString(day) // Day of the meal
        parcel.writeByte(if (showRecommended) 1 else 0)
        parcel.writeString(categoryId)
        parcel.writeStringList(steps)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MealDetailsModel> {
        override fun createFromParcel(parcel: Parcel): MealDetailsModel {
            return MealDetailsModel(parcel)
        }

        override fun newArray(size: Int): Array<MealDetailsModel?> {
            return arrayOfNulls(size)
        }
    }
}