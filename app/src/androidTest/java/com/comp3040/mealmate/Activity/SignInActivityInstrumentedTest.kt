package com.comp3040.mealmate.Activity

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the SignInActivity UI.
 * These tests verify that the UI components of the sign-in screen are displayed
 * and behave as expected under different conditions.
 */
@RunWith(AndroidJUnit4::class)
class SignInActivityInstrumentedTest {

    /**
     * Rule to set up the Android Compose testing environment for SignInActivity.
     * This ensures that the activity is launched before each test.
     */
    @get:Rule
    val composeTestRule = createAndroidComposeRule<SignInActivity>()

    /**
     * Test to verify that all UI components of the sign-in screen are displayed correctly.
     */
    @Test
    fun testSignInUIComponentsDisplayed() {
        // Assert that the "Welcome Back!" text is displayed
        composeTestRule.onNodeWithText("Welcome Back!").assertIsDisplayed()

        // Assert that the email input field is displayed
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()

        // Assert that the password input field is displayed
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()

        // Assert that the "Sign In" button is displayed
        composeTestRule.onNodeWithText("Sign In").assertIsDisplayed()

        // Assert that the "Don't have an account? Sign Up" text is displayed
        composeTestRule.onNodeWithText("Don't have an account? Sign Up").assertIsDisplayed()
    }

    /**
     * Test to verify that clicking "Sign In" with empty fields triggers an error.
     * Note: Toast messages cannot be directly verified with Espresso.
     *       Consider using a custom `ToastMatcher` or other dependency to validate Toasts.
     */
    @Test
    fun testEmptyFieldsShowErrorToast() {
        // Simulate clicking the "Sign In" button without entering email or password
        composeTestRule.onNodeWithText("Sign In").performClick()

        // Check for error message display
        // Toasts are not directly verifiable in Espresso without additional setup.
        // Suggestion: Use ToastMatcher or similar to validate the error message.
    }

    /**
     * Test to verify that entering valid email and password and clicking "Sign In"
     * navigates the user to the next screen (or triggers the appropriate callback).
     */
    @Test
    fun testSignInButtonNavigatesOnValidInput() {
        // Simulate entering a valid email address
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")

        // Simulate entering a valid password
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        // Simulate clicking the "Sign In" button
        composeTestRule.onNodeWithText("Sign In").performClick()

        // Note: Navigation or callback verification is typically done in integration tests
        // by checking for specific outcomes or using mock navigation components.
    }
}
