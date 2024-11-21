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
import com.comp3040.mealmate.R
import com.comp3040.mealmate.ViewModel.MealPlanViewModel


class MealPlanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: MealPlanViewModel by viewModels()

        setContent {
            MealPlanScreen(
                mealPlanViewModel = viewModel,
                onBackClick = { finish() }
            )
        }
    }
}
@Composable
fun MealPlanScreen(
    mealPlanViewModel: MealPlanViewModel,
    onBackClick: () -> Unit
) {
    val currentWeekPlan = mealPlanViewModel.currentWeekPlan
    val savedMealPlans = mealPlanViewModel.savedMealPlans
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        MealPlanHeader(onBackClick)

        Spacer(modifier = Modifier.height(16.dp))

        // Save Current Week Plan Button
        Button(
            onClick = {
                mealPlanViewModel.saveCurrentWeekAsPlan { success ->
                    if (!success) {
//                        Toast.makeText(LocalContext.current, "Failed to save plan", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Current Week as Plan", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Saved Meal Plans
        Text("Saved Meal Plans", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        SavedMealPlansSection(
            savedMealPlans = savedMealPlans,
            onPlanClick = { planName ->
                mealPlanViewModel.selectSavedPlan(planName) { success ->
                    if (!success) {
//                        Toast.makeText(LocalContext.current, "Plan not found.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onRemovePlan = { planName ->
                mealPlanViewModel.removePlan(planName) { success ->
                    if (!success) {
//                        Toast.makeText(LocalContext.current, "Failed to remove plan.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display Current Week Plan
        Text("This Week's Meal Plan", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(daysOfWeek) { day ->
                DayPlanSection(
                    day = day,
                    currentWeekPlan = currentWeekPlan,
                    mealPlanViewModel = mealPlanViewModel,
                    planId = "plan1" // Replace with the current plan ID dynamically
                )
            }
        }

    }
}



@Composable
fun SavedMealPlansSection(
    savedMealPlans: List<String>,
    onPlanClick: (String) -> Unit,
    onRemovePlan: (String) -> Unit
) {
    if (savedMealPlans.isEmpty()) {
        Text(
            "No saved meal plans available.",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
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
                        plan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPlanClick(plan) }
                    )
                    Text(
                        "Remove",
                        color = Color.Red,
                        modifier = Modifier.clickable { onRemovePlan(plan) }
                    )
                }
            }
        }
    }
}



@Composable
fun MealPlanHeader(onBackClick: () -> Unit) {
    ConstraintLayout(modifier = Modifier.padding(top = 36.dp)) {
        val (backBtn, titleTxt) = createRefs()

        Text(
            modifier = Modifier.fillMaxWidth().constrainAs(titleTxt) { centerTo(parent) },
            text = "Meal Plans",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )
        Image(
            painter = painterResource(R.drawable.back),
            contentDescription = null,
            modifier = Modifier.clickable { onBackClick() }
                .constrainAs(backBtn) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
        )
    }
}

@Composable
fun DayPlanSection(
    day: String,
    currentWeekPlan: SnapshotStateList<DayPlan>,
    mealPlanViewModel: MealPlanViewModel,
    planId: String // Pass the current plan ID to identify the original plan
) {
    val mealsForDay = currentWeekPlan.find { it.day == day }?.items ?: emptyList()
    Log.d("DayPlanSection", "Meals for $day: $mealsForDay")

    if (mealsForDay.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Text(
                text = day,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            mealsForDay.forEach { mealTitle ->
                MealPlanItem(
                    mealTitle = mealTitle,
                    onRemove = {
                        val dayPlan = currentWeekPlan.find { it.day == day }
                        if (dayPlan != null) {
                            val updatedItems = dayPlan.items.toMutableList().apply { remove(mealTitle) }
                            val updatedDayPlan = dayPlan.copy(items = updatedItems)
                            currentWeekPlan[currentWeekPlan.indexOf(dayPlan)] = updatedDayPlan

                            // Update the original plan in Firebase
                            mealPlanViewModel.updateOriginalPlan(planId, currentWeekPlan)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MealPlanItem(
    mealTitle: String,
    onRemove: () -> Unit
) {
    Log.d("MealPlanItem", "Displaying meal item: $mealTitle")
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            .background(colorResource(R.color.lightGrey), shape = RoundedCornerShape(10.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(mealTitle, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("Remove", color = Color.Red, modifier = Modifier.clickable { onRemove() })
    }
}


