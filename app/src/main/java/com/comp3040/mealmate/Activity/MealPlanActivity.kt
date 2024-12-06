package com.comp3040.mealmate.Activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.comp3040.mealmate.Model.DayPlan
import com.comp3040.mealmate.Model.MealPlan
import com.comp3040.mealmate.R
import com.comp3040.mealmate.ViewModel.MealPlanViewModel

/**
 * Activity to manage meal plans. Users can create, view, and manage weekly and saved meal plans.
 */
class MealPlanActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewModel for managing meal plan data
        val viewModel: MealPlanViewModel by viewModels()

        // Set the main screen using Jetpack Compose
        setContent {
            MealPlanScreen(
                mealPlanViewModel = viewModel,
                onBackClick = { finish() } // Close the activity when back is clicked
            )
        }
    }
}

/**
 * Composable function to display the meal plan screen.
 * @param mealPlanViewModel ViewModel to manage meal plan data.
 * @param onBackClick Action to perform when back button is clicked.
 */
@Composable
fun MealPlanScreen(
    mealPlanViewModel: MealPlanViewModel,
    onBackClick: () -> Unit
) {
    val currentWeekPlan = mealPlanViewModel.currentWeekPlan // Observe current week's meal plan
    val savedMealPlans = mealPlanViewModel.savedMealPlans // List of saved meal plans
    val highlightedPlanId = mealPlanViewModel.highlightedPlanId // Currently highlighted meal plan ID

    // Automatically select a highlighted meal plan when the screen loads
    LaunchedEffect(Unit) {
        if (highlightedPlanId.value == null && savedMealPlans.isNotEmpty()) {
            val highlightedPlan = savedMealPlans.find { it.highlighted }
            if (highlightedPlan != null) {
                mealPlanViewModel.selectSavedPlan(highlightedPlan.planID)
                mealPlanViewModel.highlightMealPlan(highlightedPlan.planID)
            } else if (savedMealPlans.isNotEmpty()) {
                val firstPlan = savedMealPlans.first()
                mealPlanViewModel.selectSavedPlan(firstPlan.planID)
                mealPlanViewModel.highlightMealPlan(firstPlan.planID)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header section with a back button
        MealPlanHeader(onBackClick)

        Spacer(modifier = Modifier.height(16.dp))

        // Button to create a new meal plan
        Button(
            onClick = { mealPlanViewModel.createNewMealPlan() },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create New Meal Plan", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section to display saved meal plans
        Text(
            "Saved Meal Plans",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        SavedMealPlansSection(
            savedMealPlans = savedMealPlans,
            highlightedPlanId = highlightedPlanId.value, // Current highlighted plan
            onPlanClick = { planId ->
                mealPlanViewModel.highlightMealPlan(planId) // Highlight selected plan
                mealPlanViewModel.selectSavedPlan(planId) // Set it as the current plan
            },
            onRemovePlan = { planName ->
                mealPlanViewModel.removePlan(planName) {
                    // Optionally handle UI feedback for removal
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section to display this week's meal plan
        Text(
            "This Week's Meal Plan",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Ensure it takes up remaining vertical space
        ) {
            // Display meal plan for each day of the week
            val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
            items(daysOfWeek) { day ->
                DayPlanSection(
                    day = day,
                    currentWeekPlan = currentWeekPlan,
                    mealPlanViewModel = mealPlanViewModel,
                    planId = highlightedPlanId.value ?: "" // Pass highlighted plan ID
                )
            }
        }
    }
}

/**
 * Composable to display the header section of the meal plan screen.
 * @param onBackClick Action for the back button.
 */
@Composable
fun MealPlanHeader(onBackClick: () -> Unit) {
    ConstraintLayout(modifier = Modifier.padding(top = 36.dp)) {
        val (backBtn, titleTxt) = createRefs()

        // Title text
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(titleTxt) { centerTo(parent) },
            text = "Meal Plans",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )

        // Back button
        Image(
            painter = painterResource(R.drawable.back),
            contentDescription = null,
            modifier = Modifier
                .clickable { onBackClick() }
                .constrainAs(backBtn) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
        )
    }
}

/**
 * Composable to display a section for a specific day's meal plan.
 * @param day The day of the week (e.g., Monday, Tuesday).
 * @param currentWeekPlan Current meal plan for the week.
 * @param mealPlanViewModel ViewModel to manage meal plan operations.
 * @param planId The ID of the highlighted meal plan.
 */
@Composable
fun DayPlanSection(
    day: String,
    currentWeekPlan: SnapshotStateList<DayPlan>,
    mealPlanViewModel: MealPlanViewModel,
    planId: String
) {
    val mealsForDay = currentWeekPlan.find { it.day == day }?.items ?: emptyList()

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        // Day title
        Text(
            text = day,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (mealsForDay.isEmpty()) {
            // Display if no meals are planned
            Text(
                "No meals planned.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 16.dp)
            )
        } else {
            // Display each meal planned for the day
            mealsForDay.forEach { mealTitle ->
                MealPlanItem(
                    mealTitle = mealTitle,
                    daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"),
                    onReassign = { newDay ->
                        mealPlanViewModel.reassignMeal(
                            mealTitle = mealTitle,
                            oldDay = day,
                            newDay = newDay,
                            currentPlan = currentWeekPlan,
                            planId = planId
                        )
                    },
                    onRemove = {
                        val dayPlan = currentWeekPlan.find { it.day == day }
                        if (dayPlan != null) {
                            val updatedItems = dayPlan.items.toMutableList().apply { remove(mealTitle) }
                            val updatedDayPlan = dayPlan.copy(items = updatedItems)
                            currentWeekPlan[currentWeekPlan.indexOf(dayPlan)] = updatedDayPlan
                            mealPlanViewModel.updateOriginalPlan(planId, currentWeekPlan)
                        }
                    }
                )
            }
        }
    }
}
/**
 * Composable to display a list of saved meal plans.
 * @param savedMealPlans List of saved MealPlan objects.
 * @param highlightedPlanId ID of the currently highlighted meal plan.
 * @param onPlanClick Callback when a plan is selected.
 * @param onRemovePlan Callback when a plan is removed.
 */
@Composable
fun SavedMealPlansSection(
    savedMealPlans: List<MealPlan>,
    highlightedPlanId: String?,
    onPlanClick: (String) -> Unit,
    onRemovePlan: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        // Display each saved meal plan in a list
        items(savedMealPlans) { plan ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(
                        color = if (highlightedPlanId == plan.planID) {
                            // Highlight the selected plan
                            colorResource(R.color.highlight)
                        } else {
                            // Default color for unselected plans
                            colorResource(R.color.lightGrey)
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Display the name of the meal plan
                Text(
                    text = plan.name,
                    fontWeight = if (highlightedPlanId == plan.planID) FontWeight.Bold else FontWeight.Normal,
                    color = if (highlightedPlanId == plan.planID) Color.White else Color.Black,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            // Handle plan selection
                            onPlanClick(plan.planID)
                        }
                )
                // "Remove" button to delete the plan
                Text(
                    text = "Remove",
                    color = Color.Red,
                    modifier = Modifier
                        .clickable { onRemovePlan(plan.name) }
                )
            }
        }
    }
}

/**
 * Composable to display an individual meal item with options to reassign or remove it.
 * @param mealTitle Title of the meal.
 * @param daysOfWeek List of days to which the meal can be reassigned.
 * @param onReassign Callback for reassigning the meal to another day.
 * @param onRemove Callback for removing the meal from the current day.
 */
@Composable
fun MealPlanItem(
    mealTitle: String,
    daysOfWeek: List<String>,
    onReassign: (String) -> Unit,
    onRemove: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) } // Track dropdown menu state
    var selectedDay by remember { mutableStateOf("") } // Track selected day for reassignment

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(colorResource(R.color.lightGrey), shape = RoundedCornerShape(10.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display the meal title
        Text(
            mealTitle,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Box {
            // Button to open the dropdown menu for reassignment
            Text(
                "Reassign",
                modifier = Modifier.clickable { expanded = true },
                color = Color.Blue
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false } // Close the dropdown on dismiss
            ) {
                // Dropdown items for each day of the week
                daysOfWeek.forEach { day ->
                    DropdownMenuItem(
                        onClick = {
                            selectedDay = day // Update the selected day
                            expanded = false // Close the dropdown
                            onReassign(day) // Trigger reassignment callback
                        },
                        text = { Text(text = day) }
                    )
                }
            }
        }
        // "Remove" button to delete the meal
        Text(
            "Remove",
            color = Color.Red,
            modifier = Modifier.clickable { onRemove() }
        )
    }
}







