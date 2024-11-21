package com.comp3040.mealmate.Activity

import ShoppingListViewModel
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import com.comp3040.mealmate.Model.ShoppingListItem
import com.comp3040.mealmate.R


class ShoppingListActivity : AppCompatActivity() {
    private val shoppingListViewModel: ShoppingListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShoppingListScreen(
                shoppingListViewModel = shoppingListViewModel,
                onBackClick = { finish() }
            )
        }
    }
}

@Composable
fun ShoppingListScreen(
    shoppingListViewModel: ShoppingListViewModel,
    onBackClick: () -> Unit
) {
    // Observe the shopping list from the ViewModel
    val shoppingList = shoppingListViewModel.shoppingList

    var newItemName by remember { mutableStateOf("") }
    var newItemQuantity by remember { mutableStateOf("") }
    var newItemCategory by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    // List of categories
    val categories = listOf("Fruits", "Vegetables", "Dairy", "Meat", "Snacks")

    LaunchedEffect(Unit) {
        // Fetch the shopping list when the screen is loaded
        shoppingListViewModel.fetchShoppingList()
    }

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

        // Input Fields for Adding Items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item Name Input
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.LightGray, shape = MaterialTheme.shapes.small)
                    .padding(8.dp)
            ) {
                if (newItemName.isEmpty()) {
                    Text(
                        text = "Item Name",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
                BasicTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                    keyboardOptions = KeyboardOptions.Default,
                    keyboardActions = KeyboardActions.Default,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quantity Input
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.LightGray, shape = MaterialTheme.shapes.small)
                    .padding(8.dp)
            ) {
                if (newItemQuantity.isEmpty()) {
                    Text(
                        text = "Quantity",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
                BasicTextField(
                    value = newItemQuantity,
                    onValueChange = { newItemQuantity = it },
                    textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                    keyboardOptions = KeyboardOptions.Default,
                    keyboardActions = KeyboardActions.Default,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Category Dropdown
            Box(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (newItemCategory.isEmpty()) "Select Category" else newItemCategory,
                    color = if (newItemCategory.isEmpty()) Color.Gray else Color.Black,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray, shape = MaterialTheme.shapes.small)
                        .padding(8.dp)
                        .clickable { expanded = true }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                newItemCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Add Button
            Button(
                onClick = {
                    if (newItemName.isNotBlank() && newItemQuantity.isNotBlank() && newItemCategory.isNotBlank()) {
                        shoppingListViewModel.addItemToShoppingList(
                            ShoppingListItem(
                                itemName = newItemName,
                                quantity = newItemQuantity,
                                category = newItemCategory
                            )
                        )
                        newItemName = ""
                        newItemQuantity = ""
                        newItemCategory = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                Text(text = "Add", color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List of Items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            shoppingList.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = {
                            shoppingListViewModel.toggleItemChecked(item)
                        }
                    )
                    Text(
                        text = "${item.itemName} (${item.quantity}) - ${item.category}",
                        color = Color.Black,
                        fontSize = 16.sp,
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                // Toggle the item's checked status
                                shoppingListViewModel.toggleItemChecked(item)
                            }
                    )
                    // Remove item
                    Text(
                        text = "Remove",
                        color = Color.Red,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable {
                            shoppingListViewModel.removeItemFromShoppingList(item)
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
                    shoppingListViewModel.clearShoppingList()
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


