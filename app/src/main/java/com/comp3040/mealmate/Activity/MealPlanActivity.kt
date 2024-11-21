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
    val highlightedPlanId = mealPlanViewModel.highlightedPlanId // Observe highlighted plan ID

    // Fetch and apply highlighted plan when screen is displayed
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
        MealPlanHeader(onBackClick)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { mealPlanViewModel.createNewMealPlan() },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create New Meal Plan", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Saved Meal Plans",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        SavedMealPlansSection(
            savedMealPlans = savedMealPlans,
            highlightedPlanId = highlightedPlanId.value, // Pass the current highlighted plan ID
            onPlanClick = { planId ->
                mealPlanViewModel.highlightMealPlan(planId)
                mealPlanViewModel.selectSavedPlan(planId)
            },
            onRemovePlan = { planName ->
                mealPlanViewModel.removePlan(planName) {
                    // Handle removal logic, e.g., a Toast
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "This Week's Meal Plan",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
            items(daysOfWeek) { day ->
                DayPlanSection(
                    day = day,
                    currentWeekPlan = currentWeekPlan,
                    mealPlanViewModel = mealPlanViewModel,
                    planId = highlightedPlanId.value ?: "" // Pass the highlighted plan ID
                )
            }
        }
    }
}



@Composable
fun SavedMealPlansSection(
    savedMealPlans: List<MealPlan>,
    highlightedPlanId: String?,
    onPlanClick: (String) -> Unit,
    onRemovePlan: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(savedMealPlans) { plan ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(
                        color = if (highlightedPlanId == plan.planID) {
                            colorResource(R.color.highlight)
                        } else {
                            colorResource(R.color.lightGrey)
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = plan.name,
                    fontWeight = if (highlightedPlanId == plan.planID) FontWeight.Bold else FontWeight.Normal,
                    color = if (highlightedPlanId == plan.planID) Color.White else Color.Black,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onPlanClick(plan.planID)
                        }
                )
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
    planId: String
) {
    val mealsForDay = currentWeekPlan.find { it.day == day }?.items ?: emptyList()

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = day,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (mealsForDay.isEmpty()) {
            Text(
                "No meals planned.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 16.dp)
            )
        } else {
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
@Composable
fun MealPlanItem(
    mealTitle: String,
    daysOfWeek: List<String>,
    onReassign: (String) -> Unit,
    onRemove: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(colorResource(R.color.lightGrey), shape = RoundedCornerShape(10.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            mealTitle,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Box {
            Text(
                "Reassign",
                modifier = Modifier.clickable { expanded = true },
                color = Color.Blue
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                daysOfWeek.forEach { day ->
                    DropdownMenuItem(
                        onClick = {
                            selectedDay = day
                            expanded = false
                            onReassign(day) // Trigger reassignment immediately
                        },
                        text = { Text(text = day) }
                    )
                }
            }
        }
        Text(
            "Remove",
            color = Color.Red,
            modifier = Modifier.clickable { onRemove() }
        )
    }
}









