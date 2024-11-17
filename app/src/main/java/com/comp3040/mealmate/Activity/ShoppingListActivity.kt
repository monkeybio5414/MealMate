package com.comp3040.mealmate.Activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.comp3040.mealmate.R

class ShoppingListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShoppingListScreen(onBackClick = { finish() }) // Pass the back button logic
        }
    }
}

@Composable
fun ShoppingListScreen(onBackClick: () -> Unit) {
    var shoppingList by remember { mutableStateOf(mutableListOf<Pair<String, Boolean>>()) }
    var newItem by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.back), // Replace with your back button icon
                contentDescription = "Back Button",
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onBackClick() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Shopping List Title
        Text(
            text = "Shopping List",
            style = TextStyle(
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input Field for Adding Items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = newItem,
                onValueChange = { newItem = it },
                textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                modifier = Modifier
                    .weight(1f)
                    .background(Color.LightGray, shape = MaterialTheme.shapes.small)
                    .padding(8.dp),
                keyboardOptions = KeyboardOptions.Default,
                keyboardActions = KeyboardActions.Default
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (newItem.isNotBlank()) {
                        shoppingList.add(newItem to false)
                        newItem = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                Text(text = "Add", color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List of Items with Strike-Through Effect
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            shoppingList.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.first,
                        color = Color.Black,
                        fontSize = 16.sp,
                        textDecoration = if (item.second) TextDecoration.LineThrough else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                // Toggle strike-through effect
                                shoppingList = shoppingList.toMutableList().apply {
                                    this[index] = item.first to !item.second
                                }
                            }
                    )
                    // Remove item on click
                    Text(
                        text = "Remove",
                        color = Color.Red,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable {
                            shoppingList = shoppingList
                                .filterIndexed { i, _ -> i != index }
                                .toMutableList()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Clear List Button
        Text(
            text = "Clear List",
            color = Color.Black,
            fontSize = 16.sp,
            modifier = Modifier
                .clickable {
                    shoppingList = mutableListOf()
                }
                .padding(8.dp),
            style = TextStyle(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Total Items Count
        Text(
            text = "Total Items: ${shoppingList.size}",
            color = Color.Black,
            fontSize = 16.sp,
            modifier = Modifier.padding(8.dp),
            style = TextStyle(fontWeight = FontWeight.Bold)
        )
    }
}
