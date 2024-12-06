package com.comp3040.mealmate.Activity

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock

/**
 * Unit tests for the SignUpActivity logic using mocked FirebaseAuth and FirebaseDatabase.
 * These tests verify the behavior of sign-up scenarios such as success, failure,
 * password mismatches, and empty input fields.
 */
class SignUpActivityMockTest {

    /**
     * Mocked FirebaseAuth instance to simulate authentication behavior.
     */
    private lateinit var mockFirebaseAuth: FirebaseAuth

    /**
     * Mocked FirebaseDatabase instance to simulate database interactions.
     */
    private lateinit var mockFirebaseDatabase: FirebaseDatabase

    /**
     * Mocked DatabaseReference for accessing the "users" node.
     */
    private lateinit var mockDatabaseReference: DatabaseReference

    /**
     * Tasks for simulating success and failure scenarios.
     */
    private lateinit var successTask: Task<Void>
    private lateinit var failureTask: Task<Void>

    /**
     * Set up mocked FirebaseAuth and FirebaseDatabase instances before each test.
     */
    @Before
    fun setUp() {
        // Mock FirebaseAuth
        mockFirebaseAuth = mock(FirebaseAuth::class.java)

        // Mock FirebaseDatabase and DatabaseReference
        mockFirebaseDatabase = mock(FirebaseDatabase::class.java)
        mockDatabaseReference = mock(DatabaseReference::class.java)

        // Mock the FirebaseDatabase reference to point to "users"
        `when`(mockFirebaseDatabase.getReference("users")).thenReturn(mockDatabaseReference)

        // Create tasks for success and failure scenarios
        successTask = Tasks.forResult(null)
        failureTask = Tasks.forException(Exception("Sign-up failed"))
    }

    /**
     * Test successful user sign-up with valid credentials and preferences.
     */
    @Test
    fun testSuccessfulSignUp() {
        // Mock a FirebaseUser and AuthResult
        val mockFirebaseUser = mock(FirebaseUser::class.java)
        `when`(mockFirebaseUser.uid).thenReturn("mockUserId")

        val mockAuthResult = mock(AuthResult::class.java)
        `when`(mockAuthResult.user).thenReturn(mockFirebaseUser)

        // Mock createUserWithEmailAndPassword to return a success task
        `when`(mockFirebaseAuth.createUserWithEmailAndPassword(anyString(), anyString()))
            .thenReturn(Tasks.forResult(mockAuthResult))

        // Launch the activity
        val scenario = ActivityScenario.launch(SignUpActivity::class.java)

        scenario.onActivity { activity ->
            // Inject the mocks
            activity.auth = mockFirebaseAuth

            // Call the handleSignUp method
            activity.handleSignUp(
                email = "test@example.com",
                password = "password123",
                confirmPassword = "password123",
                dietaryPreferences = listOf("Vegan", "Gluten-Free")
            )

            // Verify FirebaseAuth createUserWithEmailAndPassword was called
            verify(mockFirebaseAuth).createUserWithEmailAndPassword("test@example.com", "password123")

            // Verify the activity navigates to MainActivity
            assertTrue(activity.isFinishing) // Ensure the activity finishes
        }
    }

    /**
     * Test failed user sign-up due to an error in Firebase authentication.
     */
    @Test
    fun testFailedSignUp() {
        // Mock createUserWithEmailAndPassword to return a failure task
        val failureTask: Task<AuthResult> = Tasks.forException(Exception("Sign-Up Failed"))
        `when`(mockFirebaseAuth.createUserWithEmailAndPassword(anyString(), anyString()))
            .thenReturn(failureTask)

        // Launch the activity
        val scenario = ActivityScenario.launch(SignUpActivity::class.java)

        scenario.onActivity { activity ->
            // Inject the mocks
            activity.auth = mockFirebaseAuth

            // Call the handleSignUp method
            activity.handleSignUp(
                email = "test@example.com",
                password = "password123",
                confirmPassword = "password123",
                dietaryPreferences = listOf("None")
            )

            // Verify FirebaseAuth createUserWithEmailAndPassword was called
            verify(mockFirebaseAuth).createUserWithEmailAndPassword("test@example.com", "password123")

            // Verify that the sign-up failure message is set correctly
            assert(activity.signUpStatus!!.contains("Sign-Up Failed"))
        }
    }

    /**
     * Test sign-up with mismatched passwords.
     */
    @Test
    fun testPasswordMismatch() {
        // Launch the activity
        val scenario = ActivityScenario.launch(SignUpActivity::class.java)

        scenario.onActivity { activity ->
            // Call the handleSignUp method with mismatched passwords
            activity.handleSignUp(
                email = "test@example.com",
                password = "password123",
                confirmPassword = "password456",
                dietaryPreferences = listOf("None")
            )

            // Verify the sign-up status message is set correctly
            assertEquals("Passwords do not match.", activity.signUpStatus)
        }
    }

    /**
     * Test sign-up with empty input fields.
     */
    @Test
    fun testEmptyFields() {
        // Launch the activity
        val scenario = ActivityScenario.launch(SignUpActivity::class.java)

        scenario.onActivity { activity ->
            // Call the handleSignUp method with empty fields
            activity.handleSignUp(
                email = "",
                password = "",
                confirmPassword = "",
                dietaryPreferences = listOf("None")
            )

            // Verify the sign-up status message is set correctly
            assertEquals("Fields cannot be empty.", activity.signUpStatus)
        }
    }
}
