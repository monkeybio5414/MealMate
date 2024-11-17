package com.comp3040.mealmate.Activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.rememberAsyncImagePainter
import com.example.project1762.Helper.MealPlanManagement
import com.comp3040.mealmate.Model.ItemsModel
import com.comp3040.mealmate.R
import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList

class MealPlanActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MealPlanScreen(
                mealPlanManagement = MealPlanManagement(this),
                onBackClick = { finish() }
            )
        }
    }
}

@Composable
fun MealPlanScreen(
    mealPlanManagement: MealPlanManagement = MealPlanManagement(LocalContext.current),
    onBackClick: () -> Unit
) {
    val savedMealPlans = remember { mutableStateListOf<String>() }
    val currentWeekPlan = remember { mutableStateListOf<ItemsModel>() }
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    // Load saved meal plans dynamically
    LaunchedEffect(savedMealPlans) {
        savedMealPlans.clear()
        savedMealPlans.addAll(mealPlanManagement.getSavedPlans()) // Reload saved plans
        currentWeekPlan.clear()
        currentWeekPlan.addAll(mealPlanManagement.getMealPlan()) // Reload current week plan
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Section
        Column(
            modifier = Modifier.background(Color.White)
        ) {
            // Header with Back Button
            ConstraintLayout(modifier = Modifier.padding(top = 36.dp)) {
                val (backBtn, titleTxt) = createRefs()
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(titleTxt) { centerTo(parent) },
                    text = "Meal Plans",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                )
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

            Spacer(modifier = Modifier.height(16.dp))

            // Save Current Week as Plan
            Button(
                onClick = {
                    val newPlanName = "Plan ${savedMealPlans.size + 1}"
                    Log.d("MealPlanScreen", "Saving Current Week as Plan: $newPlanName")
                    Log.d("MealPlanScreen", "Current Week Plan: $currentWeekPlan")

                    mealPlanManagement.savePlanWithMeals(newPlanName, currentWeekPlan)
                    savedMealPlans.add(newPlanName)
                    Log.d("MealPlanScreen", "Saved Meal Plans: $savedMealPlans")
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Current Week as Plan", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Saved Meal Plans Section
            Text(
                text = "Saved Meal Plans",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (savedMealPlans.isEmpty()) {
                Text(
                    text = "No saved meal plans available.",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(savedMealPlans) { plan ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .background(colorResource(R.color.lightGrey), shape = RoundedCornerShape(10.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = plan,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val mealsForSelectedPlan = mealPlanManagement.loadPlan(plan)
                                        currentWeekPlan.clear()
                                        currentWeekPlan.addAll(mealsForSelectedPlan)
                                    }
                            )
                            Text(
                                text = "Remove",
                                color = Color.Red,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        mealPlanManagement.removePlan(plan)
                                        savedMealPlans.remove(plan)
                                    }
                                    .padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Week Plan Section
            Text(
                text = "This Week's Meal Plan",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Scrollable Meal Plan Section
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            if (currentWeekPlan.isEmpty()) {
                item {
                    Text(
                        text = "No meals added for this week.",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                items(daysOfWeek) { day ->
                    Text(
                        text = day,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    val mealsForDay = currentWeekPlan.filter { it.day == day }
                    mealsForDay.forEach { meal ->
                        MealPlanItem(
                            mealPlanItems = currentWeekPlan,
                            item = meal,
                            mealPlanManagement = mealPlanManagement,
                            daysOfWeek = daysOfWeek,
                            onReassign = { mealToReassign, selectedDay ->
                                mealToReassign.day = selectedDay
                                mealPlanManagement.updateItem(mealToReassign)
                            },
                            onRemove = { mealToRemove ->
                                mealPlanManagement.removeItem(mealToRemove)
                                currentWeekPlan.remove(mealToRemove)
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun MealPlanItem(
    mealPlanItems: SnapshotStateList<ItemsModel>, // Updated to SnapshotStateList
    item: ItemsModel,
    mealPlanManagement: MealPlanManagement,
    daysOfWeek: List<String>,
    onReassign: (ItemsModel, String) -> Unit,
    onRemove: (ItemsModel) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        val (image, title, removeText, dropdown) = createRefs()

        // Recipe Image
        Image(
            painter = rememberAsyncImagePainter(item.picUrl.first()),
            contentDescription = null,
            modifier = Modifier
                .size(90.dp)
                .background(colorResource(R.color.lightGrey), shape = RoundedCornerShape(10.dp))
                .padding(8.dp)
                .constrainAs(image) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        )

        // Recipe Title
        Column(
            modifier = Modifier
                .constrainAs(title) {
                    start.linkTo(image.end)
                    top.linkTo(image.top)
                }
                .padding(start = 8.dp, top = 12.dp)
        ) {
            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(180.dp)
            )

            // Remove Text Button
            Text(
                text = "Remove",
                color = Color.Red,
                modifier = Modifier
                    .clickable { showDialog = true }
                    .padding(top = 8.dp)
            )
        }

        // Reassign Dropdown
        ReassignDropdown(
            daysOfWeek = daysOfWeek,
            onReassign = { selectedDay -> onReassign(item, selectedDay) },
            modifier = Modifier
                .constrainAs(dropdown) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                }
        )

        // Confirmation Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    Button(onClick = {
                        onRemove(item)
                        showDialog = false
                    }) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                },
                text = { Text("Are you sure you want to remove this meal?") }
            )
        }
    }
}

@Composable
fun ReassignDropdown(
    daysOfWeek: List<String>,
    onReassign: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Button(onClick = { expanded = true }) {
            Text("Reassign")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            daysOfWeek.forEach { day ->
                DropdownMenuItem(
                    onClick = {
                        onReassign(day)
                        expanded = false
                    },
                    text = { Text(day) }
                )
            }
        }
    }
}
