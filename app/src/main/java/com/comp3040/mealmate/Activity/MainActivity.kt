package com.comp3040.mealmate.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.comp3040.mealmate.Model.CategoryModel
import com.comp3040.mealmate.Model.MealDetailsModel
import com.comp3040.mealmate.Model.SliderModel
import com.comp3040.mealmate.R
import com.comp3040.mealmate.ViewModel.MainViewModel

import android.util.Log
import androidx.activity.viewModels
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val email = FirebaseAuth.getInstance().currentUser?.email ?: "Guest"
        val username = email.substringBefore("@") // Extract username from email
// Call the function to update the database on app start
        setContent {
            MainActivityScreen(
                username = username, // Pass the username here

                onCartClick = {
                    startActivity(Intent(this, MealPlanActivity::class.java))
                },
                onCameraClick = {
                    startActivity(Intent(this, CameraActivity::class.java))
                },
                onProfileClick = {
                    Log.d("MainActivity", "Navigating to ProfileActivity")
                    try {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        Log.d("MainActivity", "Successfully navigated to ProfileActivity")
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error navigating to ProfileActivity", e)
                    }
                },
                onShoppingListClick = {
                    Log.d("MainActivity", "Navigating to ShoppingListActivity")
                    try {
                        startActivity(Intent(this, ShoppingListActivity::class.java))
                        Log.d("MainActivity", "Successfully navigated to ShoppingListActivity")
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error navigating to ShoppingListActivity", e)
                    }
                }
            )
        }
    }



}


@Composable
fun WelcomeSection(username: String, onForumClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Forum icon on the left
        Icon(
            painter = painterResource(R.drawable.forum), // Replace with your search icon resource
            contentDescription = "Forum",
            modifier = Modifier
                .size(60.dp)
                .clickable {
                    onForumClick()
                },
            tint = Color.Unspecified // Prevents any tint from being applied
        )

        // Welcome message on the right
        Column(horizontalAlignment = Alignment.End) {
            Text("Welcome Back", color = Color.Black)
            Text(
                text = username, // Use dynamic username
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun MainActivityScreen(
    onCartClick: () -> Unit,
    onCameraClick: () -> Unit,
    onProfileClick: () -> Unit,
    onShoppingListClick: () -> Unit,
    username: String
) {
    val viewModel = MainViewModel()
    val banners = remember { mutableStateListOf<SliderModel>() }
    val categories = remember { mutableStateListOf<CategoryModel>() }
    val recommended = remember { mutableStateListOf<MealDetailsModel>() }
    var showBannerLoading by remember { mutableStateOf(true) }
    var showCategoryLoading by remember { mutableStateOf(true) }
    var showRecommendedLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Banner
    LaunchedEffect(Unit) {
        Log.d("MainActivityScreen", "Loading banners...")
        viewModel.loadBanners()
        viewModel.banners.observeForever {
            banners.clear()
            banners.addAll(it)
            showBannerLoading = false
            Log.d("MainActivityScreen", "Banners loaded: ${banners.size}")
        }
    }

    // Category
    LaunchedEffect(Unit) {
        Log.d("MainActivityScreen", "Loading categories...")
        viewModel.loadCategory()
        viewModel.categories.observeForever {
            categories.clear()
            categories.addAll(it)
            showCategoryLoading = false
            Log.d("MainActivityScreen", "Categories loaded: ${categories.size}")
        }
    }

    // Recommended
    LaunchedEffect(Unit) {
        Log.d("MainActivityScreen", "Loading recommended recipes...")
        viewModel.loadRecommended()
        viewModel.recommended.observeForever {
            recommended.clear()
            recommended.addAll(it)
            showRecommendedLoading = false
            Log.d("MainActivityScreen", "Recommended recipes loaded: ${recommended.size}")
        }
    }

    ConstraintLayout(modifier = Modifier.background(Color.White)) {
        val (scrollList, bottomMenu) = createRefs()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .constrainAs(scrollList) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                }
        ) {

            // Welcome Section
            item {
                WelcomeSection(username = username) {
                    val intent = Intent(context, ForumActivity::class.java)
                    context.startActivity(intent)
                }
            }

            // Banners
            item {
                if (showBannerLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Log.d("MainActivityScreen", "Displaying banners...")
                    Banners(banners)
                }
            }

// Categories
            item {
                SectionTitle("Categories")
            }

            item {
                if (showCategoryLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Log.d("MainActivityScreen", "Displaying categories...")
                    CategoryList(categories)
                }
            }

// Recommended Recipes
            item {
                SectionTitle("Featured Recipes")
            }
            item {
                if (showRecommendedLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (recommended.isEmpty()) {
                        Log.d("MainActivityScreen", "No recommended recipes found.")
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No featured recipes available.", color = Color.Gray)
                        }
                    } else {
                        Log.d("MainActivityScreen", "Displaying recommended recipes...")
                        ListItems(recommended)
                    }
                }
            }

            // Spacer
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        BottomMenu(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(bottomMenu) {
                    bottom.linkTo(parent.bottom)
                },
            onCartClick = onCartClick,
            onCameraClick = onCameraClick, // Pass the onCameraClick parameter
            onProfileClick = onProfileClick,
            onShoppingListClick = onShoppingListClick // Pass Shopping List navigation here
        )
    }
}



@Composable
fun CategoryList(categories: SnapshotStateList<CategoryModel>) {
    var selectedIndex by remember { mutableStateOf(-1) }
    val context = LocalContext.current
    LazyRow(
        modifier = Modifier
            .fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp)
    ) {

        items(categories.size) { index ->
            CategoryItem(item = categories[index],
                isSelected = selectedIndex == index,
                onItemClick = {
                    selectedIndex = index
                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(context, ListItemsActivity::class.java).apply {
                            putExtra("id", categories[index].id.toString())
                            putExtra("title", categories[index].title)
                        }
                        startActivity(context, intent, null)
                    }, 1000)
                })
        }
    }
}

@Composable
fun CategoryItem(item: CategoryModel, isSelected: Boolean, onItemClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onItemClick)
            .background(
                color = if (isSelected) colorResource(R.color.purple) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = (item.picUrl),
            contentDescription = item.title,
            modifier = Modifier
                .size(45.dp)
                .background(
                    color = if (isSelected) Color.Transparent else colorResource(R.color.lightGrey),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentScale = ContentScale.Inside,
            colorFilter = null // No tint applied, keeps original color

        )
        if (isSelected) {
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )
        }


    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Banners(banners: List<SliderModel>) {
    AutoSlidingCarousel(banners = banners)
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AutoSlidingCarousel(
    modifier: Modifier = Modifier,
    pagerState: PagerState = remember { PagerState() },
    banners: List<SliderModel>
) {

    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    Column(modifier = modifier.fillMaxSize()) {
        HorizontalPager(count = banners.size, state = pagerState) { page ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(banners[page].url)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                    .height(150.dp)
            )
        }

        DotIndicator(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .align(Alignment.CenterHorizontally),
            totalDots = banners.size,
            selectedIndex = if (isDragged) pagerState.currentPage else pagerState.currentPage,
            dotSize = 8.dp
        )
    }
}

@Composable
fun DotIndicator(
    modifier: Modifier = Modifier,
    totalDots: Int,
    selectedIndex: Int,
    selectedColor: Color = colorResource(R.color.purple),
    unSelectedColor: Color = colorResource(R.color.grey),
    dotSize: Dp
) {
    LazyRow(
        modifier = modifier
            .wrapContentWidth()
            .wrapContentHeight()
    ) {
        items(totalDots) { index ->
            IndicatorDot(
                color = if (index == selectedIndex) selectedColor else unSelectedColor,
                size = dotSize
            )

            if (index != totalDots - 1) {
                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
            }
        }
    }
}

@Composable
fun IndicatorDot(
    modifier: Modifier = Modifier,
    size: Dp,
    color: Color
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )

}

@Composable
fun SectionTitle(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        horizontalArrangement = Arrangement.Start // Align text to the start
    ) {
        Text(
            text = title,
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BottomMenu(
    modifier: Modifier,
    onCartClick: () -> Unit,
    onCameraClick: () -> Unit,
    onProfileClick: () -> Unit,    onShoppingListClick: () -> Unit

) {
    Row(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 32.dp)
            .background(
                colorResource(R.color.purple),
                shape = RoundedCornerShape(10.dp)
            ),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        BottomMenuItem(icon = painterResource(R.drawable.btn_1), text = "Home")
        BottomMenuItem(
            icon = painterResource(R.drawable.btn_4),
            text = "Meal Plans",
            onItemClick = onCartClick
        )
        BottomMenuItem(
            icon = painterResource(R.drawable.btn_3),
            text = "Camera",
            onItemClick = onCameraClick
        )
        BottomMenuItem(
            icon = painterResource(R.drawable.btn_2),
            text = "Shopping List",
            onItemClick = {
                Log.d("BottomMenu", "Shopping List button clicked")
                onShoppingListClick()
            }
        )
        BottomMenuItem(
            icon = painterResource(R.drawable.btn_5),
            text = "Profile",
            onItemClick = {
                Log.d("BottomMenu", "Profile button clicked")
                onProfileClick()
            }
        )
    }
}


@Composable
fun BottomMenuItem(icon: Painter, text: String, onItemClick: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .height(60.dp)
            .clickable { onItemClick?.invoke() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = text, tint = Color.White)
        Text(text, color = Color.White, fontSize = 10.sp)
    }
}


