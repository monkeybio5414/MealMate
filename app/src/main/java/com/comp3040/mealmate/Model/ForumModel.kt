package com.comp3040.mealmate.Model

/**
 * Data class representing a forum post.
 * Each post contains an ID, title, content, post date, user ID, and associated comments.
 *
 * @property postId The unique identifier for the forum post. Defaults to an empty string.
 * @property title The title of the forum post. Defaults to an empty string.
 * @property content The main content/body of the forum post. Defaults to an empty string.
 * @property postDate The date the forum post was created (format: "YYYY-MM-DD" or similar). Defaults to an empty string.
 * @property userId The ID of the user who created the post. Defaults to an empty string.
 * @property comments A map of associated comments, where the key is the comment ID, and the value is a `CommentModel`. Defaults to an empty map.
 */
data class ForumModel(
    val postId: String = "", // Unique identifier for the post
    val title: String = "",  // Title of the post
    val content: String = "", // Body/content of the post
    val postDate: String = "", // Date the post was created
    var userId: String = "", // User ID of the post's creator
    val comments: Map<String, CommentModel> = emptyMap() // Map of comments (comment ID to CommentModel)
)

/**
 * Data class representing a comment on a forum post.
 * Each comment contains an ID, content, comment date, user ID, and the resolved username for display purposes.
 *
 * @property commentId The unique identifier for the comment. Defaults to an empty string.
 * @property content The main content/body of the comment. Defaults to an empty string.
 * @property commentDate The date the comment was created (format: "YYYY-MM-DD" or similar). Defaults to an empty string.
 * @property userId The ID of the user who created the comment. Defaults to an empty string.
 * @property resolvedUsername The display name of the user who created the comment. Defaults to "Unknown".
 */
data class CommentModel(
    val commentId: String = "", // Unique identifier for the comment
    val content: String = "", // Body/content of the comment
    val commentDate: String = "", // Date the comment was created
    val userId: String = "", // User ID of the comment's creator (used for backend logic)
    val resolvedUsername: String = "Unknown" // Display name of the user (used in UI)
)
