package com.comp3040.mealmate.Model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the `DayPlan` class.
 * These tests verify that the `DayPlan` model behaves as expected, covering default values,
 * property assignments, updates, equality, `toString`, and `hashCode` methods.
 */
class DayPlanUnitTest {

    /**
     * Test to verify that a `DayPlan` instance initialized with default values
     * has the correct default settings for all properties.
     */
    @Test
    fun testDefaultValues() {
        // Create a DayPlan instance with default values
        val dayPlan = DayPlan()

        // Verify default values
        assertEquals("", dayPlan.day)
        assertTrue(dayPlan.items.isEmpty())
    }

    /**
     * Test to verify that a `DayPlan` instance correctly assigns
     * values passed to its constructor.
     */
    @Test
    fun testCustomValues() {
        // Create a DayPlan instance with specific values
        val dayPlan = DayPlan(
            day = "Monday",
            items = listOf("Breakfast: Pancakes", "Lunch: Salad", "Dinner: Pasta")
        )

        // Verify assigned values
        assertEquals("Monday", dayPlan.day)
        assertEquals(3, dayPlan.items.size)
        assertEquals("Breakfast: Pancakes", dayPlan.items[0])
        assertEquals("Lunch: Salad", dayPlan.items[1])
        assertEquals("Dinner: Pasta", dayPlan.items[2])
    }

    /**
     * Test to verify that the `day` property of a `DayPlan` instance
     * can be updated correctly.
     */
    @Test
    fun testUpdateDay() {
        // Create a DayPlan instance
        val dayPlan = DayPlan(day = "Monday", items = listOf("Breakfast", "Lunch", "Dinner"))

        // Update the day
        dayPlan.day = "Tuesday"

        // Verify updated value
        assertEquals("Tuesday", dayPlan.day)
    }

    /**
     * Test to verify that the `items` property of a `DayPlan` instance
     * can be updated correctly.
     */
    @Test
    fun testUpdateItems() {
        // Create a DayPlan instance
        val dayPlan = DayPlan(day = "Monday", items = listOf("Breakfast"))

        // Update the items list
        dayPlan.items = listOf("Brunch", "Afternoon Snack")

        // Verify updated items
        assertEquals(2, dayPlan.items.size)
        assertEquals("Brunch", dayPlan.items[0])
        assertEquals("Afternoon Snack", dayPlan.items[1])
    }

    /**
     * Test to verify that two `DayPlan` instances with the same values
     * are considered equal.
     */
    @Test
    fun testEquality() {
        // Create two DayPlan instances with the same values
        val dayPlan1 = DayPlan(day = "Wednesday", items = listOf("Meal 1", "Meal 2"))
        val dayPlan2 = DayPlan(day = "Wednesday", items = listOf("Meal 1", "Meal 2"))

        // Verify equality
        assertEquals(dayPlan1, dayPlan2)
    }

    /**
     * Test to verify that the `toString` method of a `DayPlan` instance
     * generates a string representation consistent with its property values.
     */
    @Test
    fun testToString() {
        // Create a DayPlan instance
        val dayPlan = DayPlan(day = "Friday", items = listOf("Pizza", "Burger"))

        // Verify the toString output
        val expectedString = "DayPlan(day=Friday, items=[Pizza, Burger])"
        assertEquals(expectedString, dayPlan.toString())
    }

    /**
     * Test to verify that the `hashCode` implementation of a `DayPlan` instance
     * is consistent with the property values.
     */
    @Test
    fun testHashCode() {
        // Create a DayPlan instance
        val dayPlan = DayPlan(day = "Saturday", items = listOf("Breakfast", "Lunch"))

        // Verify hashCode consistency
        val expectedHashCode = dayPlan.hashCode()
        assertEquals(expectedHashCode, dayPlan.hashCode())
    }
}
