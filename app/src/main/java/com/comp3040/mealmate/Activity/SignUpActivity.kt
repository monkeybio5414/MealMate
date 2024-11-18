package com.comp3040.mealmate.Activity

import android.os.Bundle
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.comp3040.mealmate.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        setContent {
            SignUpScreen(
                onSignUp = { email, password, confirmPassword, dietaryPreferences ->
                    handleSignUp(email, password, confirmPassword, dietaryPreferences)
                },
                onBackClick = { finish() }
            )
        }
    }

    private fun handleSignUp(email: String, password: String, confirmPassword: String, dietaryPreferences: List<String>) {
        if (email.isNotBlank() && password.isNotBlank()) {
            if (password == confirmPassword) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = task.result?.user?.uid
                            val name = email.substringBefore("@")
                            saveUserToDatabase(userId, name, email, dietaryPreferences)
                            Toast.makeText(this, "Sign-Up Successful!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Sign-Up Failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Fields cannot be empty.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserToDatabase(userId: String?, name: String, email: String, dietaryPreferences: List<String>) {
        if (userId == null) {
            Toast.makeText(this, "Error: User ID is null.", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(userId)

        val user = mapOf(
            "profile_picture" to "https://example.com/photo.jpg",
            "dietary_preferences" to dietaryPreferences,
            "name" to name,
            "email" to email
        )

        userRef.setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User data saved successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

@Composable
fun SignUpScreen(onSignUp: (String, String, String, List<String>) -> Unit, onBackClick: () -> Unit) {
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
        // Back Button Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.back), // Replace with your back button icon
                contentDescription = "Back Button",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onBackClick() } // Call onBackClick when pressed
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Email Field
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

        // Password Field
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

        // Confirm Password Field
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
                                // Clear all selections when "None" is clicked
                                selectedPreferences.clear()
                            } else {
                                // Add or remove preferences
                                if (selectedPreferences.contains(preference)) {
                                    selectedPreferences.remove(preference)
                                } else {
                                    selectedPreferences.add(preference)
                                }
                                // Remove "None" if any preference is selected
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
                                    onCheckedChange = null // Handled by menu item click
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = preference)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Ensure default "None" if no preference is selected
                val preferencesToSave = if (selectedPreferences.isEmpty()) listOf("None") else selectedPreferences
                onSignUp(email, password, confirmPassword, preferencesToSave)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Sign Up", fontSize = 16.sp)
        }
    }
}


