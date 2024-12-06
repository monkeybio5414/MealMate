package com.comp3040.mealmate.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.comp3040.mealmate.Model.ForumModel
import com.comp3040.mealmate.Model.CommentModel
import com.google.android.gms.tasks.Task
import java.text.SimpleDateFormat
import java.util.Locale

class ForumViewModel : ViewModel() {
    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("ForumPosts")
    private val usersReference: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("users")
    private val _forumPosts = MutableLiveData<List<ForumModel>>()
    val forumPosts: LiveData<List<ForumModel>> get() = _forumPosts


    fun loadPosts() {
        Log.d("loadPosts", "Loading posts from Firebase...")

        databaseReference.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val posts = mutableListOf<ForumModel>()
                Log.d("loadPosts", "Firebase data retrieved. Parsing posts...")

                task.result?.children?.forEach { snapshot ->
                    val post = snapshot.toForumModel()
                    if (post != null) {
                        posts.add(post)
                        Log.d(
                            "loadPosts",
                            "Parsed Post - ID: ${post.postId}, Title: ${post.title}, Total Comments: ${post.comments.size}"
                        )
                    } else {
                        Log.w("loadPosts", "Failed to parse post: ${snapshot.key}")
                    }
                }

                resolveUsernamesForComments(posts) { resolvedPosts ->
                    _forumPosts.value = resolvedPosts
                    Log.d("loadPosts", "Posts updated. Total: ${resolvedPosts.size}")
                }
            } else {
                Log.e("loadPosts", "Error loading posts from Firebase", task.exception)
            }
        }
    }


    private fun resolveUsernamesForComments(
        posts: List<ForumModel>,
        onComplete: (List<ForumModel>) -> Unit
    ) {
        val unresolvedUserIds = mutableSetOf<String>()

        // Collect all unresolved user IDs
        posts.forEach { post ->
            post.comments.values.forEach { comment ->
                unresolvedUserIds.add(comment.userId)
            }
        }

        Log.d("resolveUsernames", "Unresolved User IDs: $unresolvedUserIds")

        if (unresolvedUserIds.isEmpty()) {
            Log.d("resolveUsernames", "No usernames to resolve. Returning original posts.")
            onComplete(posts)
            return
        }

        val unresolvedMap = mutableMapOf<String, String>()
        unresolvedUserIds.forEach { userId ->
            usersReference.child(userId).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result
                    val resolvedName = snapshot?.child("name")?.value as? String ?: "Unknown"
                    unresolvedMap[userId] = resolvedName
                    Log.d("resolveUsernames", "Resolved Username: $resolvedName for User ID: $userId")

                    // Once all user IDs are resolved, update the posts
                    if (unresolvedMap.size == unresolvedUserIds.size) {
                        val resolvedPosts = posts.map { post ->
                            post.copy(
                                comments = post.comments.mapValues { (_, comment) ->
                                    comment.copy(
                                        resolvedUsername = unresolvedMap[comment.userId] ?: "Unknown"
                                    )
                                }
                            )
                        }
                        Log.d("resolveUsernames", "All usernames resolved. Returning resolved posts.")
                        onComplete(resolvedPosts)
                    }
                } else {
                    Log.e("resolveUsernames", "Failed to resolve User ID: $userId", task.exception)
                }
            }
        }
    }



    fun addPost(title: String, content: String, userId: String) {
        Log.d("ForumViewModel", "addPost() called with Title=$title, Content=$content, UserId=$userId")
        val postId = databaseReference.push().key ?: return

        val timestamp = System.currentTimeMillis()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormatter.format(timestamp)

        val newPost = ForumModel(
            postId = postId,
            title = title,
            content = content,
            postDate = formattedDate,
            userId = userId
        )
        databaseReference.child(postId).setValue(newPost).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("ForumViewModel", "Post added successfully: $title")
                loadPosts()
            } else {
                Log.e("ForumViewModel", "Failed to add post", task.exception)
            }
        }
    }

    fun addComment(postId: String, content: String, userId: String) {
        Log.d("addComment", "Adding comment to Post ID: $postId")
        Log.d("addComment", "Comment Content: $content")
        Log.d("addComment", "User ID: $userId")

        val commentId = databaseReference.child(postId).child("Comments").push().key ?: return

        val timestamp = System.currentTimeMillis()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormatter.format(timestamp)

        val newComment = CommentModel(
            commentId = commentId,
            content = content,
            commentDate = formattedDate,
            userId = userId,
            resolvedUsername = "Unknown" // Set to "Unknown" until resolved later
        )

        databaseReference.child(postId).child("Comments").child(commentId).setValue(newComment)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("addComment", "Comment added successfully: $content")
                    loadPosts()
                } else {
                    Log.e("addComment", "Failed to add comment", task.exception)
                }
            }
    }



    private fun DataSnapshot.toForumModel(): ForumModel? {
        return try {
            val key = this.key ?: return null
            val postId = child("postId").value as? String ?: key // Use "postId" field or fallback to the Firebase key
            val title = child("title").value as? String ?: "[Untitled]"
            val content = child("content").value as? String ?: "[No Content]"
            val postDate = child("postDate").value as? String ?: "[Unknown Date]"
            val userId = child("userId").value as? String ?: "[Unknown User]"

            // Debugging post fields
            Log.d("toForumModel", "Post ID: $postId")
            Log.d("toForumModel", "Title: $title")
            Log.d("toForumModel", "Content: $content")
            Log.d("toForumModel", "Post Date: $postDate")
            Log.d("toForumModel", "User ID: $userId")

            // Parse comments
            val commentsSnapshot = child("Comments")
            val comments = mutableMapOf<String, CommentModel>()

            commentsSnapshot?.children?.forEach { commentSnapshot ->
                val commentId = commentSnapshot.key ?: return@forEach
                val commentContent = commentSnapshot.child("content").value as? String ?: "[No Content]"
                val commentDate = commentSnapshot.child("commentDate").value as? String ?: "[Unknown Date]"
                val commentUserId = commentSnapshot.child("userId").value as? String ?: "[Unknown User]"
                val resolvedUsername = commentSnapshot.child("resolvedUsername").value as? String ?: "Unknown"

                // Debugging individual comment
                Log.d(
                    "toForumModel",
                    """
                Parsed Comment:
                Comment ID: $commentId
                Content: $commentContent
                User ID: $commentUserId
                Resolved Username: $resolvedUsername
                Comment Date: $commentDate
                """.trimIndent()
                )

                comments[commentId] = CommentModel(
                    commentId = commentId,
                    content = commentContent,
                    commentDate = commentDate,
                    userId = commentUserId,
                    resolvedUsername = resolvedUsername
                )
            }

            // Debugging total comments
            Log.d("toForumModel", "Total Comments: ${comments.size}")

            ForumModel(
                postId = postId,
                title = title,
                content = content,
                postDate = postDate,
                userId = userId,
                comments = comments
            )
        } catch (e: Exception) {
            Log.e("toForumModel", "Error parsing post snapshot: ${this.key}", e)
            null
        }
    }






    fun fetchUsername(userId: String, onComplete: (String) -> Unit) {
        usersReference.child(userId).child("name").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val username = task.result?.value as? String ?: "Unknown"
                onComplete(username)
            } else {
                onComplete("Unknown")
            }
        }
    }

    fun deletePost(postId: String, currentUserId: String) {
        // Retrieve the post from the database
        databaseReference.child(postId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                val postOwnerId = snapshot?.child("userId")?.value as? String
                if (postOwnerId == currentUserId) {
                    // Proceed with deletion if the current user is the owner
                    databaseReference.child(postId).removeValue().addOnCompleteListener { deleteTask ->
                        if (deleteTask.isSuccessful) {
                            Log.d("ForumViewModel", "Post deleted successfully: $postId")
                            loadPosts() // Reload posts to update the UI
                        } else {
                            Log.e("ForumViewModel", "Failed to delete post", deleteTask.exception)
                        }
                    }
                } else {
                    Log.e("ForumViewModel", "Cannot delete post: User mismatch ($currentUserId vs $postOwnerId)")
                }
            } else {
                Log.e("ForumViewModel", "Failed to fetch post for deletion: $postId", task.exception)
            }
        }
    }

    fun deleteComment(postId: String, commentId: String, currentUserId: String) {
        Log.d("ForumViewModel", "Attempting to delete comment: $commentId under Post ID: $postId")

        // Retrieve the specific comment
        databaseReference.child(postId).child("Comments").child(commentId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result
                    val commentOwnerId = snapshot?.child("userId")?.value as? String
                    Log.d("ForumViewModel", "Fetched comment owner: $commentOwnerId for Comment ID: $commentId")

                    if (commentOwnerId == currentUserId) {
                        // Proceed to delete the specific comment
                        databaseReference.child(postId).child("Comments").child(commentId)
                            .removeValue().addOnCompleteListener { deleteTask ->
                                if (deleteTask.isSuccessful) {
                                    Log.d("ForumViewModel", "Comment deleted successfully: $commentId")
                                    loadPosts() // Refresh the UI
                                } else {
                                    Log.e("ForumViewModel", "Failed to delete comment: $commentId", deleteTask.exception)
                                }
                            }
                    } else {
                        Log.e("ForumViewModel", "Cannot delete comment: User mismatch ($currentUserId vs $commentOwnerId)")
                    }
                } else {
                    Log.e("ForumViewModel", "Failed to fetch comment for deletion: $commentId", task.exception)
                }
            }
    }



}