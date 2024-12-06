package com.comp3040.mealmate.Model

import android.os.Parcel
import android.os.Parcelable

/**
 * Data model for meal or recipe details.
 * Implements Parcelable to enable passing instances between Android components.
 *
 * @property title Name of the meal or recipe.
 * @property description A brief description of the meal or recipe.
 * @property picUrl List of image URLs associated with the meal or recipe.
 * @property ingredients List of ingredients required for the meal or recipe.
 * @property calories Total calories in the meal.
 * @property rating User rating for the meal or recipe.
 * @property day Day of the week associated with the meal (e.g., "Monday").
 * @property showRecommended Flag indicating if the meal is recommended.
 * @property categoryId Identifier for the category this meal belongs to.
 * @property steps List of preparation steps for the meal.
 * @property cookingTime Estimated cooking time for the meal.
 * @property servingSize Recommended serving size for the meal.
 */
data class MealDetailsModel(
    var title: String = "", // The name of the meal/recipe
    var description: String = "", // Description of the meal/recipe
    var picUrl: ArrayList<String> = ArrayList(), // List of image URLs
    var ingredients: ArrayList<String> = ArrayList(), // List of ingredients
    var calories: Int = 0, // Calories of the meal
    var rating: Double = 0.0, // Rating of the meal/recipe
    var day: String = "", // Day for the meal (e.g., "Monday")
    var showRecommended: Boolean = false, // Whether the item is recommended
    var categoryId: String = "", // Category ID of the meal/recipe
    var steps: List<String> = listOf(), // Preparation steps
    var cookingTime: String = "", // Cooking time for the meal/recipe
    var servingSize: String = "" // Serving size for the meal/recipe
) : Parcelable {

    /**
     * Constructor to create a MealDetailsModel instance from a Parcel.
     * Reads and assigns data from the Parcel to the model's properties.
     */
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(), // Reading title
        parcel.readString().toString(), // Reading description
        parcel.createStringArrayList() as ArrayList<String>, // Reading picUrl list
        parcel.createStringArrayList() as ArrayList<String>, // Reading ingredients list
        parcel.readInt(), // Reading calories
        parcel.readDouble(), // Reading rating
        parcel.readString().toString(), // Reading day
        parcel.readByte() != 0.toByte(), // Reading showRecommended as Boolean
        parcel.readString().toString(), // Reading categoryId
        parcel.createStringArrayList() ?: listOf(), // Reading steps list
        parcel.readString().toString(), // Reading cookingTime
        parcel.readString().toString() // Reading servingSize
    )

    /**
     * Writes the MealDetailsModel properties to a Parcel.
     * @param parcel The Parcel object to write data to.
     * @param flags Flags for how the object should be written (unused here).
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title) // Writing title
        parcel.writeString(description) // Writing description
        parcel.writeStringList(picUrl) // Writing picUrl list
        parcel.writeStringList(ingredients) // Writing ingredients list
        parcel.writeInt(calories) // Writing calories
        parcel.writeDouble(rating) // Writing rating
        parcel.writeString(day) // Writing day
        parcel.writeByte(if (showRecommended) 1 else 0) // Writing showRecommended as byte
        parcel.writeString(categoryId) // Writing categoryId
        parcel.writeStringList(steps) // Writing steps list
        parcel.writeString(cookingTime) // Writing cooking time
        parcel.writeString(servingSize) // Writing serving size
    }

    /**
     * Describes the contents of the parcelable object.
     * @return An integer bitmask indicating special object types (default is 0).
     */
    override fun describeContents(): Int {
        return 0
    }

    /**
     * Companion object that provides methods for creating and array of MealDetailsModel from a Parcel.
     */
    companion object CREATOR : Parcelable.Creator<MealDetailsModel> {
        /**
         * Creates a MealDetailsModel instance from a Parcel.
         * @param parcel The Parcel containing the data.
         * @return A MealDetailsModel instance populated with data from the Parcel.
         */
        override fun createFromParcel(parcel: Parcel): MealDetailsModel {
            return MealDetailsModel(parcel)
        }

        /**
         * Creates an array of MealDetailsModel instances.
         * @param size The size of the array to create.
         * @return An array of MealDetailsModel instances, initialized to null.
         */
        override fun newArray(size: Int): Array<MealDetailsModel?> {
            return arrayOfNulls(size)
        }
    }
}
