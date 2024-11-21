package com.comp3040.mealmate.Activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.comp3040.mealmate.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {
    private var selectedImageUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var profilePictureUrl = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        val email = auth.currentUser?.email ?: "Guest@example.com"
        val userName = email.substringBefore("@")

        // Initialize Image Picker
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImageUri = result.data?.data
                selectedImageUri?.let {
                    uploadImageToFirebase(it, userId)
                }
            }
        }

        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("users").child(userId)

            val defaultPreferences = listOf("None")

            userRef.child("profile_picture").get().addOnSuccessListener { snapshot ->
                profilePictureUrl.value = snapshot.getValue(String::class.java) ?: ""

                userRef.child("dietary_preferences").get().addOnSuccessListener { dietarySnapshot ->
                    val dietaryPreferences = if (dietarySnapshot.exists()) {
                        dietarySnapshot.children.mapNotNull { it.getValue(String::class.java) }
                    } else {
                        defaultPreferences
                    }

                    setContent {
                        MaterialTheme {
                            ProfileScreen(
                                userName = userName,
                                userEmail = email,
                                profilePictureUrl = profilePictureUrl.value,
                                dietaryPreferences = dietaryPreferences,
                                onLogoutClick = { handleLogout() },
                                onBackClick = { finish() },
                                onUploadProfilePictureClick = { launchImagePicker() },
                                onPreferencesChange = { updatedPreferences ->
                                    updateDietaryPreferences(userId, updatedPreferences)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateDietaryPreferences(userId: String, updatedPreferences: List<String>) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(userId)

        userRef.child("dietary_preferences").setValue(updatedPreferences)
            .addOnSuccessListener {
                Toast.makeText(this, "Preferences updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to update preferences: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun uploadImageToFirebase(imageUri: Uri, userId: String?) {
        if (userId == null) {
            Log.e("ProfileActivity", "Error: User not authenticated.")
            Toast.makeText(this, "Error: User not authenticated.", Toast.LENGTH_SHORT).show()
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference.child("profile_pictures/$userId.jpg")
        Log.d("ProfileActivity", "Firebase Storage Path: profile_pictures/$userId.jpg")

        val uploadTask = storageRef.putFile(imageUri)

        uploadTask
            .addOnSuccessListener {
                Log.d("ProfileActivity", "Image uploaded successfully.")
                storageRef.downloadUrl
                    .addOnSuccessListener { downloadUrl ->
                        Log.d("ProfileActivity", "Download URL obtained: $downloadUrl")
                        updateProfilePictureInDatabase(userId, downloadUrl.toString())
                        profilePictureUrl.value = downloadUrl.toString() // Update state
                    }
                    .addOnFailureListener { exception ->
                        Log.e("ProfileActivity", "Failed to get download URL: ${exception.message}")
                        Toast.makeText(this, "Failed to get download URL: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileActivity", "Image upload failed: ${exception.message}")
                Toast.makeText(this, "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfilePictureInDatabase(userId: String, downloadUrl: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(userId)

        userRef.child("profile_picture").setValue(downloadUrl)
            .addOnSuccessListener {
                Log.d("ProfileActivity", "Profile picture updated successfully in Realtime Database.")
                Toast.makeText(this, "Profile picture updated successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileActivity", "Failed to update profile picture in database: ${exception.message}")
                Toast.makeText(this, "Failed to update profile picture: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun launchImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun handleLogout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, IntroActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


}@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String,
    profilePictureUrl: String,
    dietaryPreferences: List<String>,
    onLogoutClick: () -> Unit,
    onBackClick: () -> Unit,
    onUploadProfilePictureClick: () -> Unit,
    onPreferencesChange: (List<String>) -> Unit
) {
    var refreshedImageUrl by remember { mutableStateOf(profilePictureUrl) }
    var expanded by remember { mutableStateOf(false) } // State for dropdown
    var selectedPreferences by remember { mutableStateOf(dietaryPreferences.toMutableSet()) } // Current selection

    val allPreferences = listOf("None", "Vegan", "Gluten-Free", "Keto", "Vegetarian", "Pescatarian")

    LaunchedEffect(profilePictureUrl) {
        refreshedImageUrl = profilePictureUrl
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // Leave space for the logout button
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back Button",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onBackClick() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Profile Image Section
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            ) {
                if (refreshedImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = refreshedImageUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.btn_5),
                        contentDescription = "Placeholder Profile Image",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Upload Profile Picture Button
            Button(
                onClick = onUploadProfilePictureClick,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Text(text = "Upload Profile Picture")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // User Info
            Text(
                text = userName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = userEmail,
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Dietary Preferences Dropdown
            Text(
                text = "Dietary Preferences:",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text(text = "Edit Preferences")
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
                                    selectedPreferences.add("None")
                                } else {
                                    selectedPreferences.remove("None")
                                    if (selectedPreferences.contains(preference)) {
                                        selectedPreferences.remove(preference)
                                    } else {
                                        selectedPreferences.add(preference)
                                    }
                                }
                                expanded = false
                            },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedPreferences.contains(preference),
                                        onCheckedChange = null
                                    )
                                    Text(
                                        text = preference,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Display Selected Preferences
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                selectedPreferences.forEach { preference ->
                    Text(
                        text = preference,
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Preferences Button
            Button(
                onClick = { onPreferencesChange(selectedPreferences.toList()) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text(text = "Save Preferences")
            }
        }

        // Logout Button (Fixed to the Bottom)
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 50.dp, vertical = 20.dp)
        ) {
            Text(text = "Logout")
        }
    }
}




