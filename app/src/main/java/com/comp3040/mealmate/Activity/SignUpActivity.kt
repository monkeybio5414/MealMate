package com.comp3040.mealmate.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.comp3040.mealmate.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Activity for user sign-up.
 * Handles user registration, password validation, and saving user data to Firebase Realtime Database.
 */
class SignUpActivity : BaseActivity() {

    lateinit var auth: FirebaseAuth // Firebase authentication instance
    var signUpStatus: String? by mutableStateOf(null) // Track sign-up status message

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SignUpActivity", "onCreate called")

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Set content with SignUpScreen composable
        setContent {
            SignUpScreen(
                onSignUp = { email, password, confirmPassword, dietaryPreferences ->
                    handleSignUp(email, password, confirmPassword, dietaryPreferences)
                },
                onBackClick = { finish() },
                signUpStatus = signUpStatus
            )
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("SignUpActivity", "onResume called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("SignUpActivity", "onDestroy called")
    }

    /**
     * Handles user sign-up process.
     * Validates input fields and registers the user with Firebase Authentication.
     * Saves user data to Firebase Realtime Database on successful registration.
     * @param email The user's email address.
     * @param password The user's password.
     * @param confirmPassword The confirmation password for validation.
     * @param dietaryPreferences The user's dietary preferences.
     */
    fun handleSignUp(email: String, password: String, confirmPassword: String, dietaryPreferences: List<String>) {
        if (email.isNotBlank() && password.isNotBlank()) {
            if (password == confirmPassword) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = task.result?.user?.uid
                            val name = email.substringBefore("@")
                            saveUserToDatabase(userId, name, email, dietaryPreferences)
                            signUpStatus = "Sign-Up Successful!" // Update status

                            // After sign-up, navigate to the next activity (MainActivity) and finish SignUpActivity
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            signUpStatus = "Sign-Up Failed: ${task.exception?.message}"
                            Log.d("SignUpActivity", "Error message: ${signUpStatus}")

                        }
                    }
            } else {
                signUpStatus = "Passwords do not match." // Set mismatch message
            }
        } else {
            signUpStatus = "Fields cannot be empty." // Set empty field error
        }
    }

    /**
     * Saves user data to Firebase Realtime Database.
     * @param userId The unique ID of the user.
     * @param name The user's name (derived from email).
     * @param email The user's email address.
     * @param dietaryPreferences The user's dietary preferences.
     */
    private fun saveUserToDatabase(userId: String?, name: String, email: String, dietaryPreferences: List<String>) {
        if (userId == null) {
            Toast.makeText(this, "Error: User ID is null.", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(userId)

        // User data map
        val user = mapOf(
            "profile_picture" to "https://example.com/photo.jpg", // Placeholder profile picture
            "dietary_preferences" to dietaryPreferences,
            "name" to name,
            "email" to email
        )

        // Save user data
        userRef.setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User data saved successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

/**
 * Composable function for rendering the sign-up screen.
 * Provides input fields for email, password, and dietary preferences,
 * along with a back button and sign-up action.
 * @param onSignUp Callback triggered to handle sign-up with the provided inputs.
 * @param onBackClick Callback triggered to navigate back to the previous screen.
 */
@Composable
fun SignUpScreen(
    onSignUp: (String, String, String, List<String>) -> Unit,
    onBackClick: () -> Unit,
    signUpStatus: String? = null // This will hold the status message
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val allPreferences = listOf("None", "Vegan", "Gluten-Free", "Keto", "Vegetarian", "Pescatarian")
    val selectedPreferences = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Back Button",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onBackClick() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Email Input Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Password Input Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Confirm Password Input Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Dietary Preferences Dropdown
        Text(
            text = "Select Dietary Preferences",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (selectedPreferences.isEmpty()) "None Selected" else selectedPreferences.joinToString(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                allPreferences.forEach { preference ->
                    DropdownMenuItem(
                        onClick = {
                            if (preference == "None") {
                                selectedPreferences.clear()
                            } else {
                                if (selectedPreferences.contains(preference)) {
                                    selectedPreferences.remove(preference)
                                } else {
                                    selectedPreferences.add(preference)
                                }
                                selectedPreferences.remove("None")
                            }
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = selectedPreferences.contains(preference),
                                    onCheckedChange = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = preference)
                            }
                        }
                    )
                }

                // Confirmation button to confirm the dietary preferences and close the dropdown
                DropdownMenuItem(
                    onClick = {
                        expanded = false // Close the dropdown after confirming
                    },
                    text = {
                        Text(text = "Confirm Preferences")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sign-Up Button
        Button(
            onClick = {
                val preferencesToSave = if (selectedPreferences.isEmpty()) listOf("None") else selectedPreferences
                onSignUp(email, password, confirmPassword, preferencesToSave)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Sign Up", fontSize = 16.sp)
        }

        // Show Sign-Up Status
        signUpStatus?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
