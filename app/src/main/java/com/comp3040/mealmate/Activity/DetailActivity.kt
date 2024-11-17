package com.comp3040.mealmate.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.rememberAsyncImagePainter
import com.example.project1762.Helper.MealPlanManagement
import com.comp3040.mealmate.Model.ItemsModel
import com.comp3040.mealmate.R

class DetailActivity : BaseActivity() {
    private lateinit var item: ItemsModel
    private lateinit var mealPlanManagement: MealPlanManagement

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        item = intent.getParcelableExtra("object")!!
        mealPlanManagement = MealPlanManagement(this)

        setContent {
            DetailScreen(
                item = item,
                onBackClick = { finish() },
                onAddToMealPlanClick = {
                    item.day = "Monday" // Default day or selected day logic
                    mealPlanManagement.insertItem(item)
                },
                onMealPlanClick = {
                    startActivity(Intent(this, MealPlanActivity::class.java))
                }
            )
        }
    }
}

@Composable
fun DetailScreen(
    item: ItemsModel,
    onBackClick: () -> Unit,
    onAddToMealPlanClick: () -> Unit,
    onMealPlanClick: () -> Unit
) {
    var selectedImageUrl by remember { mutableStateOf(item.picUrl.first()) }
    var selectedIngredientIndex by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ConstraintLayout(
            modifier = Modifier
                .padding(top = 36.dp, bottom = 16.dp)
                .fillMaxWidth()
        ) {
            val (back, fav) = createRefs()
            Image(
                painter = painterResource(R.drawable.back),
                contentDescription = "",
                modifier = Modifier
                    .clickable { onBackClick() }
                    .constrainAs(back) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                    }
            )
            Image(
                painter = painterResource(R.drawable.fav_icon),
                contentDescription = "",
                modifier = Modifier
                    .constrainAs(fav) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    }
            )
        }

        Image(
            painter = rememberAsyncImagePainter(model = selectedImageUrl),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .background(
                    colorResource(R.color.lightGrey),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
        )

        LazyRow(modifier = Modifier.padding(vertical = 16.dp)) {
            items(item.picUrl) { imageUrl ->
                ImageThumbnail(
                    imageUrl = imageUrl,
                    isSelected = selectedImageUrl == imageUrl,
                    onClick = { selectedImageUrl = imageUrl }
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = item.title,
                fontSize = 23.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(end = 16.dp)
            )
            Text(
                text = "${item.calories} kcal",
                fontSize = 22.sp
            )
        }

        IngredientsSelector(
            ingredients = item.ingredients,
            selectedIngredientIndex = selectedIngredientIndex,
            onIngredientSelected = { selectedIngredientIndex = it }
        )

        Text(
            text = item.description,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // New Section for Preparation Steps
        Text(
            text = "Preparation Steps:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        item.steps.forEach { step ->
            Text(
                text = step,
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onAddToMealPlanClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.purple)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .height(50.dp)
            ) {
                Text(text = "Add to Meal Plan", fontSize = 18.sp)
            }
            IconButton(
                onClick = onMealPlanClick,
                modifier = Modifier.background(
                    colorResource(R.color.lightGrey),
                    shape = RoundedCornerShape(10.dp)
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.btn_4),
                    contentDescription = "Meal Plan",
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
fun IngredientsSelector(
    ingredients: List<String>, selectedIngredientIndex: Int,
    onIngredientSelected: (Int) -> Unit
) {
    LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
        itemsIndexed(ingredients) { index, ingredient ->
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .height(48.dp)
                    .then(
                        if (index == selectedIngredientIndex) {
                            Modifier.border(
                                1.dp, colorResource(R.color.purple),
                                RoundedCornerShape(10.dp)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .background(
                        if (index == selectedIngredientIndex) colorResource(R.color.lightPurple) else
                            colorResource(R.color.lightGrey),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onIngredientSelected(index) }
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = ingredient,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = if (index == selectedIngredientIndex) colorResource(R.color.purple)
                    else colorResource(R.color.black),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun ImageThumbnail(
    imageUrl: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backColor = if (isSelected) colorResource(R.color.lightPurple) else
        colorResource(R.color.lightGrey)

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
            .padding(4.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = imageUrl),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        )
    }
}
