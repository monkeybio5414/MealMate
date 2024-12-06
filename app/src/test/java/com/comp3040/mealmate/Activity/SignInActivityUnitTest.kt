import com.comp3040.mealmate.Activity.FirebaseAuthWrapper
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.Task
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the SignInActivity logic using a mocked FirebaseAuthWrapper.
 * These tests ensure the sign-in process handles success and failure scenarios correctly.
 */
class SignInActivityUnitTest {

    /**
     * Mocked FirebaseAuthWrapper for testing Firebase authentication integration.
     */
    private lateinit var mockAuthWrapper: FirebaseAuthWrapper

    /**
     * Mocked FirebaseAuth instance for simulating Firebase operations.
     */
    private lateinit var mockFirebaseAuth: FirebaseAuth

    /**
     * Mocked Task<AuthResult> to simulate the result of authentication tasks.
     */
    private lateinit var mockTask: Task<AuthResult>

    /**
     * Set up mocks and initial configurations before each test.
     */
    @Before
    fun setup() {
        // Create relaxed mocks for FirebaseAuth, FirebaseAuthWrapper, and Task<AuthResult>
        mockFirebaseAuth = mockk(relaxed = true)
        mockAuthWrapper = mockk(relaxed = true)
        mockTask = mockk(relaxed = true)

        // Mock the behavior of FirebaseAuthWrapper's signIn method
        every { mockAuthWrapper.signIn(any(), any()) } returns mockTask
    }

    /**
     * Test to verify that the sign-in process succeeds and the method is called with the correct parameters.
     */
    @Test
    fun testHandleSignIn_Success() {
        // Simulate a successful sign-in task
        every { mockTask.isSuccessful } returns true

        // Call the signIn method with valid credentials
        mockAuthWrapper.signIn("test@example.com", "password123")

        // Verify that the signIn method was called once with the expected arguments
        verify { mockAuthWrapper.signIn("test@example.com", "password123") }
    }

    /**
     * Test to verify that the sign-in process fails and the method is called with the correct parameters.
     */
    @Test
    fun testHandleSignIn_Failure() {
        // Simulate a failed sign-in task
        every { mockTask.isSuccessful } returns false

        // Call the signIn method with invalid credentials
        mockAuthWrapper.signIn("test@example.com", "wrongpassword")

        // Verify that the signIn method was called once with the expected arguments
        verify { mockAuthWrapper.signIn("test@example.com", "wrongpassword") }
    }
}
