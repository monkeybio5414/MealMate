package com.comp3040.mealmate.Model

import android.os.Parcel
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the `MealDetailsModel` class.
 * These tests validate default values, Parcelable functionality, and the handling of an array of Parcelable objects.
 */
class MealDetailsModelUnitTest {

    /**
     * Test to verify that the `MealDetailsModel` class implements the `Parcelable` interface correctly.
     * This ensures that objects can be serialized and deserialized without data loss.
     */
    @Test
    fun testParcelable() {
        // Create an instance of MealDetailsModel
        val original = MealDetailsModel(
            title = "Pasta",
            description = "Delicious homemade pasta",
            picUrl = arrayListOf("https://example.com/pasta.jpg"),
            ingredients = arrayListOf("Flour", "Eggs", "Salt"),
            calories = 350,
            rating = 4.5,
            day = "Monday",
            showRecommended = true,
            categoryId = "Italian",
            steps = listOf("Mix ingredients", "Roll dough", "Cook pasta"),
            cookingTime = "30 mins",
            servingSize = "2 servings"
        )

        // Write the object to a Parcel
        val parcel = Parcel.obtain()
        original.writeToParcel(parcel, 0)

        // Reset parcel for reading
        parcel.setDataPosition(0)

        // Recreate the object from the Parcel
        val recreated = MealDetailsModel.CREATOR.createFromParcel(parcel)

        // Ensure the recreated object matches the original
        assertEquals(original.title, recreated.title)
        assertEquals(original.description, recreated.description)
        assertEquals(original.picUrl, recreated.picUrl)
        assertEquals(original.ingredients, recreated.ingredients)
        assertEquals(original.calories, recreated.calories)
        assertEquals(original.rating, recreated.rating, 0.0)
        assertEquals(original.day, recreated.day)
        assertEquals(original.showRecommended, recreated.showRecommended)
        assertEquals(original.categoryId, recreated.categoryId)
        assertEquals(original.steps, recreated.steps)
        assertEquals(original.cookingTime, recreated.cookingTime)
        assertEquals(original.servingSize, recreated.servingSize)

        // Release the Parcel
        parcel.recycle()
    }

    /**
     * Test to verify the default values of a `MealDetailsModel` instance
     * when it is initialized without parameters.
     */
    @Test
    fun testDefaultValues() {
        // Create a default instance of MealDetailsModel
        val defaultModel = MealDetailsModel()

        // Check the default values
        assertEquals("", defaultModel.title)
        assertEquals("", defaultModel.description)
        assertTrue(defaultModel.picUrl.isEmpty())
        assertTrue(defaultModel.ingredients.isEmpty())
        assertEquals(0, defaultModel.calories)
        assertEquals(0.0, defaultModel.rating, 0.0)
        assertEquals("", defaultModel.day)
        assertFalse(defaultModel.showRecommended)
        assertEquals("", defaultModel.categoryId)
        assertTrue(defaultModel.steps.isEmpty())
        assertEquals("", defaultModel.cookingTime)
        assertEquals("", defaultModel.servingSize)
    }

    /**
     * Test to verify the Parcelable functionality for an array of `MealDetailsModel` instances.
     * This ensures that arrays of Parcelable objects can be serialized and deserialized correctly.
     */
    @Test
    fun testParcelableArray() {
        // Create an array of MealDetailsModel
        val model1 = MealDetailsModel(title = "Meal 1")
        val model2 = MealDetailsModel(title = "Meal 2")

        val array = arrayOf(model1, model2)

        // Write the array to a Parcel
        val parcel = Parcel.obtain()
        parcel.writeTypedArray(array, 0)

        // Reset parcel for reading
        parcel.setDataPosition(0)

        // Read the array back
        val recreatedArray = parcel.createTypedArray(MealDetailsModel.CREATOR)

        // Ensure the recreated array matches the original
        assertNotNull(recreatedArray)
        assertEquals(array.size, recreatedArray!!.size)
        assertEquals(model1.title, recreatedArray[0]?.title)
        assertEquals(model2.title, recreatedArray[1]?.title)

        // Release the Parcel
        parcel.recycle()
    }
}
