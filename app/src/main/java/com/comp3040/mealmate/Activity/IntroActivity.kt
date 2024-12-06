package com.comp3040.mealmate.Activity

import androidx.lifecycle.lifecycleScope
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.comp3040.mealmate.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * IntroActivity
 *
 * This activity serves as the introductory screen of the application.
 * It checks whether the user has previously viewed the intro screen and whether the user is logged in.
 * Based on these conditions, the user is navigated to the appropriate screen.
 */
class IntroActivity : BaseActivity() {

    /**
     * Checks if a user is logged in using Firebase Authentication.
     *
     * @return true if a user is logged in, false otherwise.
     */
    private fun isUserLoggedIn(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser != null
    }

    /**
     * Checks if the intro screen has been viewed before.
     *
     * @return true if the intro screen has been viewed, false otherwise.
     */
    private fun isIntroSeen(): Boolean {
        return getSharedPreferences("MealMatePrefs", MODE_PRIVATE)
            .getBoolean("isIntroSeen", false)
    }

    /**
     * Marks the intro screen as seen by saving the value in SharedPreferences.
     */
    private fun markIntroAsSeen() {
        getSharedPreferences("MealMatePrefs", MODE_PRIVATE).edit()
            .putBoolean("isIntroSeen", true)
            .apply()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure the window appearance for a polished UI
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        // Handle navigation logic asynchronously using coroutine
        lifecycleScope.launch {
            if (!isIntroSeen()) {
                // Display the intro screen if it hasn't been viewed yet
                setContent {
                    IntroScreen(
                        onClick = {
                            markIntroAsSeen() // Mark intro as seen
                            navigateToSignIn() // Navigate to sign-in screen
                        },
                        onSignInClick = {
                            navigateToSignIn() // Skip intro and navigate directly to sign-in
                        }
                    )
                }
            } else {
                // Navigate based on the user's login state
                if (isUserLoggedIn()) {
                    navigateToMain() // Navigate to the main screen if logged in
                } else {
                    navigateToSignIn() // Navigate to the sign-in screen if not logged in
                }
            }
        }
    }

    /**
     * Navigates to the main activity of the application.
     */
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * Navigates to the sign-in activity of the application.
     */
    private fun navigateToSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}

/**
 * IntroScreen
 *
 * This composable function displays the introductory UI for the application.
 * It includes a logo, title, description, and buttons for navigation.
 *
 * @param onClick Callback for the "Let’s Go" button.
 * @param onSignInClick Callback for the "Sign In" clickable text.
 */
@Composable
fun IntroScreen(
    onClick: () -> Unit = {}, // Callback for "Let’s Go" button
    onSignInClick: () -> Unit = {} // Callback for "Sign In" clickable text
) {
    Column(
        modifier = Modifier
            .fillMaxSize() // Fill the entire screen
            .background(Color.White) // Set a white background color
            .verticalScroll(rememberScrollState()) // Enable vertical scrolling for smaller screens
            .padding(horizontal = 20.dp, vertical = 16.dp), // Apply padding for spacing
        horizontalAlignment = Alignment.CenterHorizontally // Center align child elements
    ) {
        // Display the application logo
        Image(
            painter = painterResource(id = R.drawable.intro_logo), // Reference to the logo resource
            contentDescription = null, // No specific content description
            modifier = Modifier
                .padding(top = 48.dp) // Top padding for spacing
                .sizeIn(
                    minWidth = 150.dp,
                    minHeight = 150.dp,
                    maxWidth = 300.dp,
                    maxHeight = 300.dp
                ) // Adaptive sizing for logo
                .fillMaxWidth(),
            contentScale = ContentScale.Fit // Maintain original aspect ratio
        )

        Spacer(modifier = Modifier.height(24.dp)) // Spacer for vertical spacing

        // Display the title text
        Text(
            text = stringResource(id = R.string.intro_title), // Fetch title from resources
            fontSize = 24.sp, // Font size for high-density screens
            fontWeight = FontWeight.Bold, // Bold font weight for emphasis
            textAlign = TextAlign.Center, // Center align the text
            modifier = Modifier.padding(horizontal = 16.dp) // Horizontal padding
        )

        Spacer(modifier = Modifier.height(16.dp)) // Spacer for vertical spacing

        // Display the description text
        Text(
            text = stringResource(id = R.string.intro_sub_title), // Fetch subtitle from resources
            modifier = Modifier.padding(horizontal = 16.dp), // Horizontal padding
            color = Color.DarkGray, // Dark gray color for readability
            textAlign = TextAlign.Center, // Center align the text
            lineHeight = 22.sp // Adjust line height for better readability
        )

        Spacer(modifier = Modifier.height(32.dp)) // Spacer for vertical spacing

        // Display the "Let’s Go" button
        Button(
            onClick = { onClick() }, // Trigger the provided callback
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp) // Button padding
                .fillMaxWidth() // Make the button full width
                .height(56.dp), // Button height
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.purple)), // Button background color
            shape = RoundedCornerShape(8.dp) // Rounded corners
        ) {
            Text(
                text = stringResource(id = R.string.letgo), // Button text from resources
                color = Color.White, // White text color
                fontSize = 16.sp // Font size
            )
        }

        Spacer(modifier = Modifier.height(16.dp)) // Spacer for vertical spacing

        // Display the "Sign In" clickable text
        Text(
            text = stringResource(id = R.string.sign), // "Sign In" text from resources
            textAlign = TextAlign.Center, // Center align the text
            modifier = Modifier
                .clickable { onSignInClick() }, // Trigger the provided callback on click
            fontSize = 16.sp, // Font size
            color = Color.Blue // Blue color to indicate interactivity
        )
    }
}

/**
 * PreviewIntroScreen
 *
 * This function previews the IntroScreen composable in the design view.
 * It uses placeholder callbacks for the button and text interactions.
 */
@Preview
@Composable
fun PreviewIntroScreen() {
    IntroScreen(
        onClick = { /* Navigate to sign-in */ },
        onSignInClick = { /* Handle sign-in click */ }
    )
}
