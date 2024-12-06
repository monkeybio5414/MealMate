package com.comp3040.mealmate.Activity

// Required imports
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

/**
 * ProfileActivity is responsible for managing user profile information.
 * This includes:
 * - Displaying user details (name, email, profile picture).
 * - Editing dietary preferences.
 * - Uploading and updating the profile picture in Firebase Storage.
 * - Logging out the user.
 */
class ProfileActivity : BaseActivity() {
    private var selectedImageUri: Uri? = null // Stores the URI of the selected profile image
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent> // Handles image selection
    private var profilePictureUrl = mutableStateOf("") // State to hold the profile picture URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get user authentication details
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        val email = auth.currentUser?.email ?: "Guest@example.com"
        val userName = email.substringBefore("@") // Extract username from email

        // Initialize image picker for selecting a new profile picture
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImageUri = result.data?.data
                selectedImageUri?.let {
                    uploadImageToFirebase(it, userId) // Upload the selected image to Firebase
                }
            }
        }

        // If user is authenticated, fetch profile details and dietary preferences
        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("users").child(userId)

            val defaultPreferences = listOf("None") // Default dietary preferences if none exist

            // Fetch profile picture URL
            userRef.child("profile_picture").get().addOnSuccessListener { snapshot ->
                profilePictureUrl.value = snapshot.getValue(String::class.java) ?: ""

                // Fetch dietary preferences
                userRef.child("dietary_preferences").get().addOnSuccessListener { dietarySnapshot ->
                    val dietaryPreferences = if (dietarySnapshot.exists()) {
                        dietarySnapshot.children.mapNotNull { it.getValue(String::class.java) }
                    } else {
                        defaultPreferences
                    }

                    // Set the content of the activity using Jetpack Compose
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

    /**
     * Updates dietary preferences for the authenticated user in the Firebase Realtime Database.
     * @param userId The ID of the user.
     * @param updatedPreferences The updated list of dietary preferences.
     */
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

    /**
     * Uploads a profile image to Firebase Storage and updates the user's profile picture URL.
     * @param imageUri The URI of the selected image.
     * @param userId The ID of the authenticated user.
     */
    private fun uploadImageToFirebase(imageUri: Uri, userId: String?) {
        if (userId == null) {
            Log.e("ProfileActivity", "Error: User not authenticated.")
            Toast.makeText(this, "Error: User not authenticated.", Toast.LENGTH_SHORT).show()
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference.child("profile_pictures/$userId.jpg")
        val uploadTask = storageRef.putFile(imageUri)

        uploadTask
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    updateProfilePictureInDatabase(userId, downloadUrl.toString())
                    profilePictureUrl.value = downloadUrl.toString()
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to get download URL: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Updates the profile picture URL in the Firebase Realtime Database.
     * @param userId The ID of the authenticated user.
     * @param downloadUrl The URL of the uploaded profile picture.
     */
    private fun updateProfilePictureInDatabase(userId: String, downloadUrl: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(userId)

        userRef.child("profile_picture").setValue(downloadUrl)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile picture updated successfully.", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to update profile picture: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Launches an intent to pick an image from the device storage.
     */
    private fun launchImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    /**
     * Logs out the user and redirects to the IntroActivity.
     */
    private fun handleLogout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, IntroActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

/**
 * Composable function for rendering the profile screen.
 * Displays user details, dietary preferences, and options for updating profile information.
 * @param userName The user's name.
 * @param userEmail The user's email address.
 * @param profilePictureUrl URL of the user's profile picture.
 * @param dietaryPreferences The user's dietary preferences.
 * @param onLogoutClick Callback triggered when the user clicks the logout button.
 * @param onBackClick Callback triggered when the user clicks the back button.
 * @param onUploadProfilePictureClick Callback triggered when the user uploads a new profile picture.
 * @param onPreferencesChange Callback triggered when the user updates dietary preferences.
 */
@Composable
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
    var refreshedImageUrl by remember { mutableStateOf(profilePictureUrl) } // To handle profile picture updates
    var expanded by remember { mutableStateOf(false) } // State for dropdown menu
    var selectedPreferences by remember { mutableStateOf(dietaryPreferences.toMutableSet()) } // Current dietary preferences

    val allPreferences = listOf("None", "Vegan", "Gluten-Free", "Keto", "Vegetarian", "Pescatarian") // Options for preferences

    // Update the displayed image URL when the profile picture changes
    LaunchedEffect(profilePictureUrl) {
        refreshedImageUrl = profilePictureUrl
    }

    // Main layout for the profile screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Background color
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // Space for the logout button
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

            // Profile Picture Section
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            ) {
                if (refreshedImageUrl.isNotEmpty()) {
                    // Display user's profile picture
                    AsyncImage(
                        model = refreshedImageUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Display placeholder image if no profile picture is available
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

            // User Info Section
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

            // Dietary Preferences Section
            Text(
                text = "Dietary Preferences:",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Dropdown to Edit Preferences
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text(text = "Edit Preferences")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false } // Close the dropdown when dismissed
                ) {
                    // Display all available preferences
                    allPreferences.forEach { preference ->
                        DropdownMenuItem(
                            onClick = {
                                if (preference == "None") {
                                    // If "None" is selected, clear other preferences
                                    selectedPreferences.clear()
                                    selectedPreferences.add("None")
                                } else {
                                    // Toggle preference selection
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
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

        // Logout Button Fixed at the Bottom
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





