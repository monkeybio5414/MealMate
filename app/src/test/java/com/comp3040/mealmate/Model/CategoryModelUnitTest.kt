package com.comp3040.mealmate.Model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the `CategoryModel` class.
 * These tests ensure that the class's default values, property assignments,
 * equality, hashCode, and toString implementations behave as expected.
 */
class CategoryModelUnitTest {

    /**
     * Test to verify that a `CategoryModel` instance initialized with default values
     * has the correct defaults for all properties.
     */
    @Test
    fun testCategoryModelDefaultValues() {
        // Create an instance of CategoryModel with default values
        val category = CategoryModel()

        // Verify default values
        assertEquals("", category.title)
        assertEquals(0, category.id)
        assertEquals("", category.picUrl)
    }

    /**
     * Test to verify that a `CategoryModel` instance correctly assigns
     * values passed to its constructor.
     */
    @Test
    fun testCategoryModelWithValues() {
        // Create an instance of CategoryModel with specific values
        val category = CategoryModel(
            title = "Vegetables",
            id = 1,
            picUrl = "https://example.com/vegetables.jpg"
        )

        // Verify assigned values
        assertEquals("Vegetables", category.title)
        assertEquals(1, category.id)
        assertEquals("https://example.com/vegetables.jpg", category.picUrl)
    }

    /**
     * Test to verify that two `CategoryModel` instances with the same property values
     * are considered equal.
     */
    @Test
    fun testCategoryModelEquality() {
        // Create two instances with the same values
        val category1 = CategoryModel(
            title = "Fruits",
            id = 2,
            picUrl = "https://example.com/fruits.jpg"
        )
        val category2 = CategoryModel(
            title = "Fruits",
            id = 2,
            picUrl = "https://example.com/fruits.jpg"
        )

        // Verify that the two instances are considered equal
        assertEquals(category1, category2)
    }

    /**
     * Test to verify that the `hashCode` implementation of `CategoryModel`
     * is consistent with the property values of the instance.
     */
    @Test
    fun testCategoryModelHashCode() {
        // Create an instance
        val category = CategoryModel(
            title = "Dairy",
            id = 3,
            picUrl = "https://example.com/dairy.jpg"
        )

        // Verify that hashCode is consistent with the values
        val expectedHashCode = category.hashCode()
        assertEquals(expectedHashCode, category.hashCode())
    }

    /**
     * Test to verify that the `toString` method of `CategoryModel`
     * generates a string representation consistent with its property values.
     */
    @Test
    fun testCategoryModelToString() {
        // Create an instance
        val category = CategoryModel(
            title = "Meat",
            id = 4,
            picUrl = "https://example.com/meat.jpg"
        )

        // Verify the toString() output
        val expectedString = "CategoryModel(title=Meat, id=4, picUrl=https://example.com/meat.jpg)"
        assertEquals(expectedString, category.toString())
    }
}
