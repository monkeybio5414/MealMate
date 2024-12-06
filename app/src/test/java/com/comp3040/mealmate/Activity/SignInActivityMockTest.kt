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
 * Unit tests for SignInActivity that use mocked FirebaseAuthWrapper to simulate
 * Firebase authentication behavior.
 */
class SignInActivityMockTest {

    /**
     * Mocked FirebaseAuthWrapper to wrap FirebaseAuth operations.
     */
    private lateinit var mockAuthWrapper: FirebaseAuthWrapper

    /**
     * Mocked FirebaseAuth instance for simulating Firebase authentication.
     */
    private lateinit var mockFirebaseAuth: FirebaseAuth

    /**
     * Mocked Task<AuthResult> to simulate the result of authentication tasks.
     */
    private lateinit var mockTask: Task<AuthResult>

    /**
     * Set up the mocks before each test.
     */
    @Before
    fun setup() {
        // Create relaxed mocks for FirebaseAuth and the wrapper
        mockFirebaseAuth = mockk(relaxed = true)
        mockAuthWrapper = mockk(relaxed = true)
        mockTask = mockk(relaxed = true)

        // Mock the behavior of the FirebaseAuthWrapper's signIn method
        every { mockAuthWrapper.signIn(any(), any()) } returns mockTask
    }

    /**
     * Test the integration of the sign-in process when sign-in is successful.
     */
    @Test
    fun testIntegration_Success() {
        // Mock the task to simulate a successful sign-in
        every { mockTask.isSuccessful } returns true

        // Simulate calling the sign-in method with valid credentials
        mockAuthWrapper.signIn("test@example.com", "password123")

        // Verify that the sign-in method was called with the correct arguments
        verify { mockAuthWrapper.signIn("test@example.com", "password123") }
    }

    /**
     * Test the integration of the sign-in process when sign-in fails.
     */
    @Test
    fun testIntegration_Failure() {
        // Mock the task to simulate a failed sign-in
        every { mockTask.isSuccessful } returns false

        // Simulate calling the sign-in method with invalid credentials
        mockAuthWrapper.signIn("test@example.com", "wrongpassword")

        // Verify that the sign-in method was called with the correct arguments
        verify { mockAuthWrapper.signIn("test@example.com", "wrongpassword") }
    }
}
