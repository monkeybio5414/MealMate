package com.comp3040.mealmate.Activity

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.comp3040.mealmate.R

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ProfileActivity", "onCreate called")

        try {
            setContent {
                MaterialTheme {
                    ProfileScreen(
                        userName = "John Doe",
                        userEmail = "john.doe@example.com",
                        onLogoutClick = {
                            Log.d("ProfileActivity", "Logout clicked")
                            // Add Logout logic here
                        },
                        onBackClick = { finish() }, // Add back button functionality
                        onUploadProfilePictureClick = {
                            Log.d("ProfileActivity", "Upload Profile Picture clicked")
                            // Add Upload Profile Picture logic here
                        }
                    )
                }
            }
            Log.d("ProfileActivity", "Content set successfully")
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Error setting content", e)
        }
    }
}
@Composable
fun ProfileScreen(
    userName: String = "John Doe",
    userEmail: String = "john.doe@example.com",
    onLogoutClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onUploadProfilePictureClick: () -> Unit = {}
) {
    val dietaryPreferencesOptions = listOf("Veg", "Vegan", "Keto", "Pesc", "Omni")
    val selectedPreferences = remember { mutableStateMapOf<String, Boolean>() }

    // Initialize all options as unselected
    dietaryPreferencesOptions.forEach { option ->
        if (selectedPreferences[option] == null) {
            selectedPreferences[option] = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
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
                painter = painterResource(id = R.drawable.back), // Replace with your back button icon
                contentDescription = "Back Button",
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onBackClick() }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Profile Image
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        ) {
            Image(
                painter = painterResource(id = R.drawable.btn_5), // Replace with your profile picture icon
                contentDescription = "Profile Image",
                modifier = Modifier.fillMaxSize()
            )
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

        // User Name
        Text(
            text = userName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // User Email
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

        // Preferences Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp), // Adjust spacing
            modifier = Modifier
                .width(800.dp) // Fixed width
                .height(200.dp) // Fixed height
                .padding(horizontal = 11.dp, vertical = 34.dp) // Adjust padding
        ) {
            dietaryPreferencesOptions.forEach { preference ->
                Button(
                    onClick = {
                        val currentSelection = selectedPreferences[preference] ?: false
                        selectedPreferences[preference] = !currentSelection
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedPreferences[preference] == true) Color.DarkGray else Color.LightGray
                    ),
                    modifier = Modifier.weight(1f) // Ensure buttons take equal space
                ) {
                    Text(
                        text = preference,
                        color = if (selectedPreferences[preference] == true) Color.White else Color.Black,
                        fontSize = 9.sp,
                        maxLines = 2, // Ensure the text stays on one line
                        overflow = TextOverflow.Ellipsis, // Handle overflow with ellipsis
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Logout Button
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(text = "Logout")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
