package com.comp3040.mealmate.Activity

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the SignUpActivity UI.
 * This test class verifies that the SignUpActivity UI components behave correctly
 * under various conditions, such as invalid input, mismatched passwords, and navigation.
 */
@RunWith(AndroidJUnit4::class)
class SignUpActivityInstrumentTest {

    /**
     * Rule to set up the Jetpack Compose testing environment.
     */
    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Scenario to manage the lifecycle of SignUpActivity during tests.
     */
    private lateinit var activityScenario: ActivityScenario<SignUpActivity>

    /**
     * UiDevice instance to interact with system-level UI elements.
     */
    private lateinit var device: UiDevice

    /**
     * Set up the testing environment before each test.
     */
    @Before
    fun setup() {
        // Launch only the SignUpActivity for testing
        activityScenario = ActivityScenario.launch(SignUpActivity::class.java)

        // Wait for the UI to be fully initialized and idle
        composeTestRule.waitForIdle()

        // Initialize UiDevice for interacting with system UI elements
        device = UiDevice.getInstance(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation())
    }

    /**
     * Test to verify that attempting to sign up with empty fields displays an error message.
     */
    @Test
    fun testSignUpWithEmptyFields() {
        // Simulate clicking the "Sign Up" button without filling any fields
        composeTestRule.onNodeWithText("Sign Up").performClick()

        // Assert that the error message for empty fields is displayed
        composeTestRule.onNodeWithText("Fields cannot be empty.").assertExists()
    }

    /**
     * Test to verify that mismatched passwords during sign-up display an error message.
     */
    @Test
    fun testSignUpWithMismatchedPasswords() {
        // Enter a valid email
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")

        // Enter a password
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        // Enter a different confirmation password
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("wrongpassword")

        // Simulate clicking the "Sign Up" button
        composeTestRule.onNodeWithText("Sign Up").performClick()

        // Assert that the error message for mismatched passwords is displayed
        composeTestRule.onNodeWithText("Passwords do not match.").assertExists()
    }

    /**
     * Test to verify that entering an invalid email during sign-up displays an appropriate error message.
     */
    @Test
    fun testSignUpWithInvalidEmail() {
        // Enter an invalid email
        composeTestRule.onNodeWithText("Email").performTextInput("invalid-email")

        // Enter matching passwords
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("password123")

        // Simulate clicking the "Sign Up" button
        composeTestRule.onNodeWithText("Sign Up").performClick()

        // Wait for UI updates and error message to appear
        composeTestRule.waitForIdle()
        Thread.sleep(1000) // Allow time for the error message to become visible

        // Assert that the error message for invalid email format is displayed
        composeTestRule.onNodeWithText("Sign-Up Failed: The email address is badly formatted.").assertExists()
    }

    /**
     * Test to verify that the back navigation button functions correctly.
     */
    @Test
    fun testBackNavigation() {
        // Simulate clicking the back button
        composeTestRule.onNodeWithContentDescription("Back Button").performClick()

        // Assert that the activity finishes and navigates back to the previous screen
        activityScenario.onActivity { activity ->
            assert(activity.isFinishing)
        }
    }

    /**
     * Tear down the testing environment after each test.
     */
    @After
    fun tearDown() {
        // Close the activity to clean up resources
        if (::activityScenario.isInitialized) {
            activityScenario.close()
        }
    }
}
