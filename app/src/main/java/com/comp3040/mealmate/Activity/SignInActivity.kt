package com.comp3040.mealmate.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

/**
 * Activity for user sign-in.
 * Handles authentication using FirebaseAuth and navigates to other activities on success or failure.
 */
class SignInActivity : BaseActivity() {

    private lateinit var authWrapper: FirebaseAuthWrapper // Wrapper for FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FirebaseAuthWrapper
        authWrapper = FirebaseAuthWrapper(FirebaseAuth.getInstance())

        // Set up the content with the SignInScreen composable
        setContent {
            SignInScreen(
                onSignIn = { email, password -> handleSignIn(email, password) },
                onSignUp = { navigateToSignUp() }
            )
        }
    }

    // Setter method to inject a mock FirebaseAuthWrapper for testing
    fun setFirebaseAuthWrapper(wrapper: FirebaseAuthWrapper) {
        this.authWrapper = wrapper
    }

    fun handleSignIn(email: String, password: String) {
        if (email.isNotBlank() && password.isNotBlank()) {
            authWrapper.signIn(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Sign-In Successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Sign-In Failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Toast.makeText(this, "Fields cannot be empty.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToSignUp() {
        startActivity(Intent(this, SignUpActivity::class.java))
    }
}




/**
 * Composable function for rendering the sign-in screen.
 * Provides UI elements for user authentication, including fields for email and password,
 * a sign-in button, and a sign-up option.
 * @param onSignIn A callback to handle sign-in with email and password.
 * @param onSignUp A callback to navigate to the sign-up screen.
 */
@Composable
fun SignInScreen(onSignIn: (String, String) -> Unit, onSignUp: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome Back!",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

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

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Button(
            onClick = { onSignIn(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Sign In", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onSignUp) {
            Text("Don't have an account? Sign Up")
        }
    }
}



