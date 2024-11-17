package com.comp3040.mealmate.Model

import android.os.Parcel
import android.os.Parcelable

data class ItemsModel(
    var title: String = "", // The name of the meal/recipe
    var description: String = "", // Description of the meal/recipe
    var picUrl: ArrayList<String> = ArrayList(), // List of image URLs
    var ingredients: ArrayList<String> = ArrayList(), // Updated from model to ingredients
    var calories: Int = 0, // Updated from price to calories
    var rating: Double = 0.0, // Rating of the meal/recipe
    var day: String = "", // Updated from numberInCart to day
    var showRecommended: Boolean = false, // Whether the item is recommended
    var categoryId: String = "", // Category ID of the meal/recipe
    var steps: List<String> = listOf() // Preparation steps
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.createStringArrayList() as ArrayList<String>,
        parcel.createStringArrayList() as ArrayList<String>, // Updated from model to ingredients
        parcel.readInt(), // Updated from price to calories
        parcel.readDouble(),
        parcel.readString().toString(), // Updated from numberInCart to day
        parcel.readByte() != 0.toByte(),
        parcel.readString().toString(),
        parcel.createStringArrayList() ?: listOf()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeStringList(picUrl)
        parcel.writeStringList(ingredients) // Updated from model to ingredients
        parcel.writeInt(calories) // Updated from price to calories
        parcel.writeDouble(rating)
        parcel.writeString(day) // Updated from numberInCart to day
        parcel.writeByte(if (showRecommended) 1 else 0)
        parcel.writeString(categoryId)
        parcel.writeStringList(steps)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ItemsModel> {
        override fun createFromParcel(parcel: Parcel): ItemsModel {
            return ItemsModel(parcel)
        }

        override fun newArray(size: Int): Array<ItemsModel?> {
            return arrayOfNulls(size)
        }
    }
}
