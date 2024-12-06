package com.comp3040.mealmate.Model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the `ShoppingListItem` class.
 * These tests validate default values, property assignments, equality, `toString`, and `hashCode` methods.
 */
class ShoppingListItemUnitTest {

    /**
     * Test to verify that a `ShoppingListItem` instance initialized with default values
     * has the correct default settings for all properties.
     */
    @Test
    fun testDefaultValues() {
        // Create a ShoppingListItem instance with default values
        val item = ShoppingListItem()

        // Verify default values
        assertEquals("", item.id)
        assertEquals("", item.itemName)
        assertEquals("", item.quantity)
        assertEquals("", item.category)
        assertFalse(item.isChecked)
    }

    /**
     * Test to verify that a `ShoppingListItem` instance correctly assigns
     * values passed to its constructor.
     */
    @Test
    fun testCustomValues() {
        // Create a ShoppingListItem instance with specific values
        val item = ShoppingListItem(
            id = "item123",
            itemName = "Milk",
            quantity = "2 liters",
            category = "Dairy",
            isChecked = true
        )

        // Verify assigned values
        assertEquals("item123", item.id)
        assertEquals("Milk", item.itemName)
        assertEquals("2 liters", item.quantity)
        assertEquals("Dairy", item.category)
        assertTrue(item.isChecked)
    }

    /**
     * Test to verify that two `ShoppingListItem` instances with the same values
     * are considered equal.
     */
    @Test
    fun testEquality() {
        // Create two ShoppingListItem instances with the same values
        val item1 = ShoppingListItem(
            id = "item001",
            itemName = "Apples",
            quantity = "1 kg",
            category = "Fruits",
            isChecked = false
        )
        val item2 = ShoppingListItem(
            id = "item001",
            itemName = "Apples",
            quantity = "1 kg",
            category = "Fruits",
            isChecked = false
        )

        // Verify the two instances are considered equal
        assertEquals(item1, item2)
    }

    /**
     * Test to verify that the `toString` method of a `ShoppingListItem` instance
     * generates a string representation consistent with its property values.
     */
    @Test
    fun testToString() {
        // Create a ShoppingListItem instance
        val item = ShoppingListItem(
            id = "item002",
            itemName = "Bread",
            quantity = "1 loaf",
            category = "Bakery",
            isChecked = false
        )

        // Verify the toString output
        val expectedString = "ShoppingListItem(id=item002, itemName=Bread, quantity=1 loaf, category=Bakery, isChecked=false)"
        assertEquals(expectedString, item.toString())
    }

    /**
     * Test to verify that the `hashCode` implementation of a `ShoppingListItem` instance
     * is consistent with its property values.
     */
    @Test
    fun testHashCode() {
        // Create a ShoppingListItem instance
        val item = ShoppingListItem(
            id = "item003",
            itemName = "Eggs",
            quantity = "12",
            category = "Dairy",
            isChecked = true
        )

        // Verify the hashCode consistency
        val expectedHashCode = item.hashCode()
        assertEquals(expectedHashCode, item.hashCode())
    }
}
