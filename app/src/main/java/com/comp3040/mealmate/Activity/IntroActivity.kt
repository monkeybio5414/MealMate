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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

class IntroActivity : BaseActivity() {

    // Check if the user is logged in using FirebaseAuth
    private fun isUserLoggedIn(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser != null // Return true if a user is logged in
    }

    // Check if the intro screen has been viewed
    private fun isIntroSeen(): Boolean {
        return getSharedPreferences("MealMatePrefs", MODE_PRIVATE)
            .getBoolean("isIntroSeen", false)
    }

    // Mark the intro screen as seen
    private fun markIntroAsSeen() {
        getSharedPreferences("MealMatePrefs", MODE_PRIVATE).edit()
            .putBoolean("isIntroSeen", true)
            .apply()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure window settings
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        lifecycleScope.launch {
            if (!isIntroSeen()) {
                // Show the intro screen if it hasn't been seen
                setContent {
                    IntroScreen(
                        onClick = {
                            markIntroAsSeen()
                            navigateToSignIn() // Navigate to SignInActivity after intro
                        },
                        onSignInClick = {
                            navigateToSignIn() // Navigate directly to SignInActivity
                        }
                    )
                }
            } else {
                // If the intro has been seen, check login state
                if (isUserLoggedIn()) {
                    navigateToMain() // Navigate to MainActivity if the user is logged in
                } else {
                    navigateToSignIn() // Navigate to SignInActivity if not logged in
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@Composable
fun IntroScreen(
    onClick: () -> Unit = {},
    onSignInClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.intro_logo),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 48.dp)
                .size(300.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(id = R.string.intro_title),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(id = R.string.intro_sub_title),
            modifier = Modifier.padding(top = 16.dp),
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Button(
            onClick = { onClick() },
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.purple)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = stringResource(id = R.string.letgo),
                color = Color.White,
                fontSize = 18.sp
            )
        }

        Text(
            text = stringResource(id = R.string.sign),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 16.dp)
                .clickable { onSignInClick() }, // Call the onSignInClick lambda
            fontSize = 18.sp,
            color = Color.Blue // Highlight text to indicate it's clickable
        )
    }
}

