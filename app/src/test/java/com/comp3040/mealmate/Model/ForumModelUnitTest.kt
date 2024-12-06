package com.comp3040.mealmate.Model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the `ForumModel` and `CommentModel` classes.
 * These tests validate default values, property assignments, and equality for both models.
 */
class ForumModelUnitTest {

    /**
     * Test to verify that a `ForumModel` instance initialized with default values
     * has the correct defaults for all properties.
     */
    @Test
    fun testForumModelDefaultValues() {
        // Create a ForumModel instance with default values
        val forum = ForumModel()

        // Verify default values
        assertEquals("", forum.postId)
        assertEquals("", forum.title)
        assertEquals("", forum.content)
        assertEquals("", forum.postDate)
        assertEquals("", forum.userId)
        assertTrue(forum.comments.isEmpty())
    }

    /**
     * Test to verify that a `CommentModel` instance initialized with default values
     * has the correct defaults for all properties.
     */
    @Test
    fun testCommentModelDefaultValues() {
        // Create a CommentModel instance with default values
        val comment = CommentModel()

        // Verify default values
        assertEquals("", comment.commentId)
        assertEquals("", comment.content)
        assertEquals("", comment.commentDate)
        assertEquals("", comment.userId)
        assertEquals("Unknown", comment.resolvedUsername)
    }

    /**
     * Test to verify that a `ForumModel` instance correctly assigns
     * values passed to its constructor and contains valid comments.
     */
    @Test
    fun testForumModelWithValues() {
        // Create some comments
        val comment1 = CommentModel(
            commentId = "c1",
            content = "Great post!",
            commentDate = "2024-11-01",
            userId = "user1",
            resolvedUsername = "User One"
        )
        val comment2 = CommentModel(
            commentId = "c2",
            content = "Interesting thoughts!",
            commentDate = "2024-11-02",
            userId = "user2",
            resolvedUsername = "User Two"
        )

        // Create a ForumModel instance with values
        val forum = ForumModel(
            postId = "p1",
            title = "Interesting Post",
            content = "This is the content of the forum post.",
            postDate = "2024-11-01",
            userId = "creator123",
            comments = mapOf(
                "c1" to comment1,
                "c2" to comment2
            )
        )

        // Verify the ForumModel values
        assertEquals("p1", forum.postId)
        assertEquals("Interesting Post", forum.title)
        assertEquals("This is the content of the forum post.", forum.content)
        assertEquals("2024-11-01", forum.postDate)
        assertEquals("creator123", forum.userId)

        // Verify comments in the ForumModel
        assertEquals(2, forum.comments.size)
        assertTrue(forum.comments.containsKey("c1"))
        assertEquals("Great post!", forum.comments["c1"]?.content)
        assertTrue(forum.comments.containsKey("c2"))
        assertEquals("Interesting thoughts!", forum.comments["c2"]?.content)
    }

    /**
     * Test to verify that a `CommentModel` instance correctly assigns
     * values passed to its constructor.
     */
    @Test
    fun testCommentModelWithValues() {
        // Create a CommentModel with specific values
        val comment = CommentModel(
            commentId = "c3",
            content = "Very helpful!",
            commentDate = "2024-11-28",
            userId = "user123",
            resolvedUsername = "Test User"
        )

        // Verify the CommentModel values
        assertEquals("c3", comment.commentId)
        assertEquals("Very helpful!", comment.content)
        assertEquals("2024-11-28", comment.commentDate)
        assertEquals("user123", comment.userId)
        assertEquals("Test User", comment.resolvedUsername)
    }

    /**
     * Test to verify equality for two `ForumModel` instances with the same values.
     */
    @Test
    fun testForumModelEquality() {
        // Create two ForumModel instances with the same values
        val forum1 = ForumModel(
            postId = "p1",
            title = "Equality Post",
            content = "Content for equality test.",
            postDate = "2024-11-28",
            userId = "creator123"
        )
        val forum2 = ForumModel(
            postId = "p1",
            title = "Equality Post",
            content = "Content for equality test.",
            postDate = "2024-11-28",
            userId = "creator123"
        )

        // Verify the two ForumModel instances are equal
        assertEquals(forum1, forum2)
    }

    /**
     * Test to verify equality for two `CommentModel` instances with the same values.
     */
    @Test
    fun testCommentModelEquality() {
        // Create two CommentModel instances with the same values
        val comment1 = CommentModel(
            commentId = "c1",
            content = "Equality test comment",
            commentDate = "2024-11-28",
            userId = "user123",
            resolvedUsername = "Test User"
        )
        val comment2 = CommentModel(
            commentId = "c1",
            content = "Equality test comment",
            commentDate = "2024-11-28",
            userId = "user123",
            resolvedUsername = "Test User"
        )

        // Verify the two CommentModel instances are equal
        assertEquals(comment1, comment2)
    }
}
