package com.comp3040.mealmate.Activity

// Necessary imports for the activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.rememberAsyncImagePainter
import com.comp3040.mealmate.Model.MealDetailsModel
import com.comp3040.mealmate.R
import com.comp3040.mealmate.ViewModel.MealPlanViewModel

/**
 * DetailActivity
 *
 * Displays the details of a selected meal. Users can view the meal's description,
 * ingredients, preparation steps, and associated images. Users can also add the
 * meal to a highlighted meal plan or navigate to the meal plan screen.
 */
class DetailActivity : ComponentActivity() {
    private lateinit var item: MealDetailsModel // Selected meal details

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mealPlanViewModel: MealPlanViewModel by viewModels()

        // Retrieve the passed MealDetailsModel object from the intent
        item = intent.getParcelableExtra("object")!!

        // Set up the Compose UI
        setContent {
            // Retrieve the currently highlighted meal plan
            val highlightedPlan = mealPlanViewModel.savedMealPlans.find { it.highlighted }

            DetailScreen(
                item = item,
                onBackClick = { finish() }, // Handle back navigation
                onAddToMealPlanClick = {
                    if (highlightedPlan != null) {
                        // Add the item to the highlighted meal plan
                        mealPlanViewModel.addItemToHighlightedPlan(item) { success ->
                            Toast.makeText(
                                this,
                                if (success) "Item added to '${highlightedPlan.name}'." else "Failed to add item.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(this, "No highlighted plan selected.", Toast.LENGTH_SHORT).show()
                    }
                },
                onMealPlanClick = {
                    // Navigate to the MealPlanActivity
                    startActivity(Intent(this, MealPlanActivity::class.java))
                }
            )
        }
    }
}

@Composable
fun DetailScreen(
    item: MealDetailsModel, // Meal details object
    onBackClick: () -> Unit, // Callback for back navigation
    onAddToMealPlanClick: () -> Unit, // Callback for adding meal to plan
    onMealPlanClick: () -> Unit // Callback for navigating to meal plan screen
) {
    var selectedImageUrl by remember { mutableStateOf(item.picUrl.first()) } // Track selected image URL

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Set background color
    ) {
        val (content, buttons) = createRefs()

        // Scrollable content section
        Column(
            modifier = Modifier
                .constrainAs(content) {
                    top.linkTo(parent.top)
                    bottom.linkTo(buttons.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .verticalScroll(rememberScrollState()) // Enable vertical scrolling
                .padding(16.dp)
        ) {
            HeaderSection(onBackClick)

            // Main image and thumbnails
            MainImageSection(selectedImageUrl, item.picUrl) { selectedImageUrl = it }

            // Display meal details (title, description, etc.)
            MealDetailsSection(item)
        }

        // Fixed action buttons (e.g., Add to Meal Plan)
        Box(
            modifier = Modifier
                .constrainAs(buttons) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            ActionButtons(
                onAddToMealPlanClick = onAddToMealPlanClick,
                onMealPlanClick = onMealPlanClick
            )
        }
    }
}

@Composable
fun HeaderSection(onBackClick: () -> Unit) {
    // Header section with a back button
    ConstraintLayout(
        modifier = Modifier
            .padding(top = 36.dp, bottom = 16.dp)
            .fillMaxWidth()
    ) {
        val back = createRef()
        Image(
            painter = painterResource(R.drawable.back), // Back icon resource
            contentDescription = "Back",
            modifier = Modifier
                .clickable { onBackClick() } // Handle back button click
                .constrainAs(back) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
        )
    }
}

@Composable
fun MainImageSection(
    selectedImageUrl: String, // Currently selected image URL
    imageUrls: List<String>, // List of all image URLs
    onImageSelected: (String) -> Unit // Callback for selecting an image
) {
    // Display the main selected image
    Image(
        painter = rememberAsyncImagePainter(model = selectedImageUrl),
        contentDescription = null,
        contentScale = ContentScale.Crop, // Crop the image to fill the container
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp)
            .background(
                colorResource(R.color.lightGrey), // Light grey background
                shape = RoundedCornerShape(8.dp) // Rounded corners
            )
            .clip(RoundedCornerShape(8.dp)) // Clip the image to match the rounded corners
    )

    // Display a row of image thumbnails
    LazyRow(modifier = Modifier.padding(vertical = 16.dp)) {
        items(imageUrls) { imageUrl ->
            ImageThumbnail(
                imageUrl = imageUrl,
                isSelected = selectedImageUrl == imageUrl,
                onClick = { onImageSelected(imageUrl) }
            )
        }
    }
}

@Composable
fun MealDetailsSection(item: MealDetailsModel) {
    // Display meal title and calorie count
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Text(
            text = item.title, // Meal title
            fontSize = 23.sp,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(end = 16.dp)
        )
        Text(
            text = "${item.calories} kcal", // Calorie count
            fontSize = 22.sp
        )
    }

    // Display list of ingredients
    IngredientList(ingredients = item.ingredients)

    // Display meal description
    Text(
        text = item.description, // Description
        fontSize = 14.sp,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 16.dp)
    )

    // Display preparation steps
    Text(
        text = "Preparation Steps:",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    // Iterate through and display each step
    item.steps.forEach { step ->
        Text(
            text = step,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(vertical = 2.dp)
        )
    }
}

@Composable
fun ActionButtons(
    onAddToMealPlanClick: () -> Unit, // Callback for adding to meal plan
    onMealPlanClick: () -> Unit, // Callback for navigating to meal plan screen
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 0.5.dp)
    ) {
        Button(
            onClick = onAddToMealPlanClick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.purple)), // Purple button
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .height(50.dp)
        ) {
            Text(text = "Add to Meal Plan", fontSize = 18.sp)
        }
        IconButton(
            onClick = onMealPlanClick,
            modifier = Modifier
                .background(
                    colorResource(R.color.lightGrey),
                    shape = RoundedCornerShape(10.dp)
                )
                .size(50.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.btn_4), // Meal plan icon
                contentDescription = "Meal Plan",
                tint = Color.Black
            )
        }
    }
}

@Composable
fun IngredientList(ingredients: List<String>) {
    // LazyRow to display ingredients as scrollable chips
    LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
        items(ingredients) { ingredient ->
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .height(48.dp)
                    .background(
                        colorResource(R.color.lightGrey),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = ingredient, // Ingredient name
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.black),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun ImageThumbnail(
    imageUrl: String, // Image URL for the thumbnail
    isSelected: Boolean, // Whether the thumbnail is selected
    onClick: () -> Unit // Callback for clicking the thumbnail
) {
    val backColor = if (isSelected) colorResource(R.color.lightPurple) else
        colorResource(R.color.lightGrey)

    // Display the image thumbnail with optional border for selected state
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(55.dp)
            .then(
                if (isSelected) {
                    Modifier.border(1.dp, colorResource(R.color.purple), RoundedCornerShape(10.dp))
                } else {
                    Modifier
                }
            )
            .background(backColor, shape = RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(10.dp))
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = imageUrl), // Load image using Coil
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
