package com.comp3040.mealmate.Activity

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import coil.compose.AsyncImage
import com.comp3040.mealmate.Model.MealDetailsModel
import com.comp3040.mealmate.R

/**
 * Displays a grid of meal items with fixed dimensions.
 *
 * @param items A list of MealDetailsModel containing meal data to be displayed.
 */
@Composable
fun ListItems(items: List<MealDetailsModel>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // Define grid with two columns
        modifier = Modifier
            .height(500.dp) // Fixed height for the grid
            .padding(start = 8.dp, end = 8.dp), // Padding for the grid
        verticalArrangement = Arrangement.spacedBy(16.dp), // Vertical spacing between rows
        horizontalArrangement = Arrangement.spacedBy(16.dp) // Horizontal spacing between columns
    ) {
        items(items.size) { row -> // Iterate through the list of items
            Row(
                modifier = Modifier.fillMaxWidth(), // Fill the width of the grid
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Space between items
            ) {
                RecommendedItem(items, row) // Display individual items
            }
        }
    }
}

/**
 * Displays a grid of meal items that fills the available space.
 *
 * @param items A list of MealDetailsModel containing meal data to be displayed.
 */
@Composable
fun ListItemsFullSize(items: List<MealDetailsModel>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // Define grid with two columns
        modifier = Modifier
            .fillMaxSize() // Fill the available space
            .padding(start = 8.dp, end = 8.dp), // Padding for the grid
        verticalArrangement = Arrangement.spacedBy(16.dp), // Vertical spacing between rows
        horizontalArrangement = Arrangement.spacedBy(16.dp) // Horizontal spacing between columns
    ) {
        items(items.size) { row -> // Iterate through the list of items
            Row(
                modifier = Modifier.fillMaxWidth(), // Fill the width of the grid
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Space between items
            ) {
                RecommendedItem(items, row) // Display individual items
            }
        }
    }
}

/**
 * Displays a single meal item with an image, title, rating, and calorie information.
 *
 * @param items The list of MealDetailsModel containing meal data.
 * @param pos The position of the item to display.
 */
@Composable
fun RecommendedItem(items: List<MealDetailsModel>, pos: Int) {
    val context = LocalContext.current // Get the current context for navigation
    Column(
        modifier = Modifier
            .padding(8.dp) // Padding around the item
            .width(175.dp) // Fixed width for consistency
            .height(225.dp) // Fixed height for the item
    ) {
        // Display the meal image
        AsyncImage(
            model = items[pos].picUrl.firstOrNull(), // Load the first image URL
            contentDescription = items[pos].title, // Provide a description for accessibility
            modifier = Modifier
                .width(175.dp) // Fixed width for the image
                .height(175.dp) // Fixed height for the image
                .clip(RoundedCornerShape(10.dp)) // Rounded corners for the image
                .clickable { // Navigate to the detail screen when clicked
                    val intent = Intent(context, DetailActivity::class.java).apply {
                        putExtra("object", items[pos]) // Pass the item data to the detail screen
                    }
                    startActivity(context, intent, null) // Start the detail activity
                },
            contentScale = ContentScale.Crop // Crop the image to fill the container
        )
        // Display the meal title
        Text(
            text = items[pos].title, // Meal title
            color = Color.Black, // Text color
            fontSize = 16.sp, // Font size
            fontWeight = FontWeight.Bold, // Bold font weight
            maxLines = 1, // Restrict to one line
            overflow = TextOverflow.Ellipsis, // Truncate text if it overflows
            modifier = Modifier.padding(top = 8.dp) // Padding above the title
        )
        // Display rating and calories
        Row(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(), // Fill the width of the container
            horizontalArrangement = Arrangement.SpaceBetween // Space out elements
        ) {
            // Rating display
            Row {
                Image(
                    painter = painterResource(id = R.drawable.star), // Star icon for rating
                    contentDescription = "Rating", // Accessibility description
                    modifier = Modifier.align(Alignment.CenterVertically) // Align vertically
                )
                Spacer(modifier = Modifier.width(4.dp)) // Space between the star and rating text
                Text(
                    text = items[pos].rating.toString(), // Display the rating
                    color = Color.Black, // Text color
                    fontSize = 15.sp // Font size
                )
            }
            // Calorie display
            Text(
                text = "${items[pos].calories} kcal", // Display the calorie information
                color = colorResource(R.color.purple), // Purple color for emphasis
                fontSize = 16.sp, // Font size
                fontWeight = FontWeight.Bold // Bold font weight
            )
        }
    }
}
