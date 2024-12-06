package com.comp3040.mealmate.Activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.comp3040.mealmate.Model.ForumModel
import com.comp3040.mealmate.ViewModel.ForumViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.comp3040.mealmate.Model.CommentModel
import com.comp3040.mealmate.R
import com.google.firebase.auth.FirebaseAuth

class ForumActivity : ComponentActivity() {
    /**
     * Called when the activity is first created.
     * Sets the content view to the ForumScreen Composable.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ForumScreen(onBackClick = { finish() })
        }
    }

    // Lifecycle methods for debugging
    override fun onStart() {
        super.onStart()
        Log.d("ForumActivity", "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.d("ForumActivity", "onResume called")
    }
}

@Composable
fun ForumScreen(viewModel: ForumViewModel = viewModel(), onBackClick: () -> Unit) {
    // Observing forum posts using LiveData
    val forumPosts by viewModel.forumPosts.observeAsState(emptyList())
    var newPostTitle by remember { mutableStateOf("") }
    var newPostContent by remember { mutableStateOf("") }
    var showErrorMessage by remember { mutableStateOf(false) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // Handle scenario when user is not logged in
    if (currentUserId == null) {
        Log.e("ForumScreen", "User not logged in")
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("You need to log in to access the forum.")
        }
        return
    }

    // Load forum posts when the screen is displayed
    LaunchedEffect(Unit) {
        Log.d("ForumScreen", "Triggering loadPosts")
        viewModel.loadPosts()
    }

    // Main forum screen layout
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Forum header
        ForumHeader(onBackClick)

        Spacer(modifier = Modifier.height(16.dp))

        // Section for adding a new post
        Column(modifier = Modifier.padding(16.dp)) {
            TextField(
                value = newPostTitle,
                onValueChange = { newPostTitle = it },
                label = { Text("Post Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = newPostContent,
                onValueChange = { newPostContent = it },
                label = { Text("Post Content") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                if (newPostTitle.isNotBlank() && newPostContent.isNotBlank()) {
                    viewModel.addPost(newPostTitle, newPostContent, currentUserId)
                    newPostTitle = ""
                    newPostContent = ""
                } else {
                    showErrorMessage = true
                }
            }) {
                Text("Add Post")
            }

            // Error message for empty fields
            if (showErrorMessage) {
                Snackbar(
                    action = {
                        TextButton(onClick = { showErrorMessage = false }) {
                            Text("Dismiss")
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Fields cannot be empty")
                }
            }
        }

        // List of forum posts
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(forumPosts) { post ->
                ForumPost(post = post, viewModel = viewModel, currentUserId = currentUserId)
            }
        }
    }
}

@Composable
fun ForumPost(
    post: ForumModel,
    viewModel: ForumViewModel,
    currentUserId: String
) {
    var commentText by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("Fetching...") }

    // Resolve username of the post's author
    LaunchedEffect(post.userId) {
        viewModel.fetchUsername(post.userId) { fetchedUsername ->
            username = fetchedUsername
        }
    }

    // Post card layout
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(post.title.ifBlank { "Untitled Post" }, style = MaterialTheme.typography.titleLarge)
            Text(post.content.ifBlank { "[No Content]" }, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "Posted by: $username",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Delete button for the post's owner
            if (post.userId == currentUserId) {
                Button(
                    onClick = { viewModel.deletePost(post.postId, currentUserId) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text("Delete Post")
                }
            }

            // Displaying comments
            if (post.comments.isNullOrEmpty()) {
                Text(
                    text = "No comments yet.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                post.comments.values.forEach { comment ->
                    CommentItem(comment, currentUserId, post.postId, viewModel)
                }
            }

            // Add a new comment
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Add a comment...") },
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Button(onClick = {
                    if (commentText.isNotBlank()) {
                        viewModel.addComment(post.postId, commentText, currentUserId)
                        commentText = ""
                    }
                }) {
                    Text("Post")
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: CommentModel,
    currentUserId: String,
    postId: String,
    viewModel: ForumViewModel
) {
    var username by remember { mutableStateOf("Fetching...") }

    // Resolve the username of the comment's author
    LaunchedEffect(comment.userId) {
        viewModel.fetchUsername(comment.userId) { fetchedUsername ->
            username = fetchedUsername
        }
    }

    // Comment card layout
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(comment.content.ifBlank { "[No Content]" }, style = MaterialTheme.typography.bodyLarge)
            Text("Commented by: $username", style = MaterialTheme.typography.bodySmall)

            // Remove button for the comment's owner
            if (comment.userId == currentUserId) {
                Text(
                    text = "Remove",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error),
                    modifier = Modifier.clickable { viewModel.deleteComment(postId, comment.commentId, currentUserId) }
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ForumHeader(onBackClick: () -> Unit) {
    ConstraintLayout(modifier = Modifier.padding(top = 36.dp)) {
        val (backBtn, titleTxt) = createRefs()

        Text(
            modifier = Modifier.fillMaxWidth().constrainAs(titleTxt) { centerTo(parent) },
            text = "Forum",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )
        Image(
            painter = painterResource(R.drawable.back),
            contentDescription = null,
            modifier = Modifier.clickable { onBackClick() }.constrainAs(backBtn) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
            }
        )
    }
}
