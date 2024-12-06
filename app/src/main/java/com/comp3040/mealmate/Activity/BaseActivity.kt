package com.comp3040.mealmate.Activity

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

/**
 * BaseActivity
 *
 * A base class for all activities in the app. This class sets up a consistent
 * window configuration, including immersive layouts and status bar settings.
 * Activities in the app can extend this class to inherit these configurations.
 */
open class BaseActivity : AppCompatActivity() {

    /**
     * onCreate
     *
     * Called when the activity is created. Sets up window configurations for immersive UI
     * and light-themed status bar icons.
     *
     * @param savedInstanceState - Bundle containing the saved state of the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable a layout with no limits, allowing content to extend into the system UI areas
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        // Clear the translucent status bar flag to enable a fully opaque background
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        // Add a flag to allow the app to draw its own status bar background
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        // Set light status bar icons (dark text/icons) for better visibility on light backgrounds
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}
