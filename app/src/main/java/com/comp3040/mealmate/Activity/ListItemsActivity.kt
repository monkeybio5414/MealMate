package com.comp3040.mealmate.Activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.comp3040.mealmate.R
import com.comp3040.mealmate.ViewModel.MainViewModel

/**
 * ListItemsActivity
 *
 * Displays a list of items filtered by a category ID. The title and category ID
 * are passed as intent extras from the previous activity.
 */
class ListItemsActivity : BaseActivity() {
    private val viewModel = MainViewModel() // ViewModel for managing UI-related data
    private var id: String = "" // Category ID
    private var title: String = "" // Title of the list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve category ID and title from the intent extras
        id = intent.getStringExtra("id") ?: ""
        title = intent.getStringExtra("title") ?: ""

        // Set the content to display the ListItemScreen composable
        setContent {
            ListItemScreen(
                title = title, // Pass the list title
                onBackClick = { finish() }, // Close the activity on back press
                viewModel = viewModel, // Provide the ViewModel
                id = id // Pass the category ID
            )
        }
    }
}

/**
 * ListItemScreen
 *
 * Composable function for displaying a screen with a title, back button, and a filtered
 * list of items based on the provided category ID.
 *
 * @param title The title to display at the top of the screen.
 * @param onBackClick Callback for handling back button clicks.
 * @param viewModel ViewModel for managing and fetching data.
 * @param id The category ID for filtering the list.
 */
@Composable
fun ListItemScreen(
    title: String,
    onBackClick: () -> Unit,
    viewModel: MainViewModel,
    id: String
) {
    // Observe the list of recommended items from the ViewModel
    val items by viewModel.recommended.observeAsState(emptyList())
    var isLoading by remember { mutableStateOf(true) } // State to track loading status

    // Load the filtered list of items when the category ID changes
    LaunchedEffect(id) {
        viewModel.loadFiltered(id) // Fetch filtered items from the ViewModel
    }

    // Screen layout
    Column(modifier = Modifier.fillMaxSize()) {
        // Header with title and back button
        ConstraintLayout(modifier = Modifier.padding(top = 36.dp, start = 16.dp, end = 16.dp)) {

            // Create references for layout positioning
            val (backBtn, cartTxt) = createRefs()

            // Title text displayed in the center
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(cartTxt) { centerTo(parent) },
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                text = title // Display the passed title
            )

            // Back button to navigate back to the previous screen
            Image(
                painter = painterResource(R.drawable.back), // Back button icon
                contentDescription = null,
                modifier = Modifier
                    .clickable {
                        onBackClick() // Handle back button click
                    }
                    .constrainAs(backBtn) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                    }
            )
        }

        // Display a loading indicator while the list is being fetched
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center // Center align the progress indicator
            ) {
                CircularProgressIndicator() // Loading indicator
            }
        } else {
            // Display the list of items when loading is complete
            ListItemsFullSize(items)
        }
    }

    // Update the loading state whenever the items list changes
    LaunchedEffect(items) {
        isLoading = items.isEmpty() // Set isLoading to true if the list is empty
    }
}
