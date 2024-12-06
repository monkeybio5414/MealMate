package com.comp3040.mealmate.Activity

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class MainActivityInstrumentTestTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Helper function to set content
    private fun ComposeContentTestRule.setMainActivityContent(
        username: String,
        onCartClick: () -> Unit = {},
        onCameraClick: () -> Unit = {},
        onProfileClick: () -> Unit = {},
        onShoppingListClick: () -> Unit = {}
    ) {
        /**
         * UI tests for the MainActivityScreen composable function.
         * These tests verify the behavior of the main screen, including
         * the welcome section, bottom menu, and navigation actions.
         */
        class MainActivityInstrumentTestTest {

            @get:Rule
            val composeTestRule = createComposeRule()

            /**
             * Helper function to set the content of the Compose test rule
             * to the MainActivityScreen composable function.
             *
             * @param username The username to display in the welcome section.
             * @param onCartClick The callback to be invoked when the Cart button is clicked.
             * @param onCameraClick The callback to be invoked when the Camera button is clicked.
             * @param onProfileClick The callback to be invoked when the Profile button is clicked.
             * @param onShoppingListClick The callback to be invoked when the Shopping List button is clicked.
             */
            private fun ComposeContentTestRule.setMainActivityContent(
                username: String,
                onCartClick: () -> Unit = {},
                onCameraClick: () -> Unit = {},
                onProfileClick: () -> Unit = {},
                onShoppingListClick: () -> Unit = {}
            ) {
                setContent {
                    MainActivityScreen(
                        onCartClick = onCartClick,
                        onCameraClick = onCameraClick,
                        onProfileClick = onProfileClick,
                        onShoppingListClick = onShoppingListClick,
                        username = username
                    )
                }
            }

            /**
             * Tests that the welcome section displays the correct username.
             */
            @Test
            fun testWelcomeSectionDisplaysUsername() {
                val username = "TestUser"

                composeTestRule.setContent {
                    WelcomeSection(username = username, onForumClick = {})
                }

                // Check that the welcome message and username are displayed
                composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
                composeTestRule.onNodeWithText(username).assertIsDisplayed()
            }

            /**
             * Tests that all bottom menu items are displayed.
             */
            @Test
            fun testBottomMenuItemsDisplayed() {
                composeTestRule.setContent {
                    BottomMenu(
                        modifier = Modifier,
                        onCartClick = {},
                        onCameraClick = {},
                        onProfileClick = {},
                        onShoppingListClick = {}
                    )
                }

                // Assert each menu item is displayed
                composeTestRule.onNodeWithText("Home").assertIsDisplayed()
                composeTestRule.onNodeWithText("Meal Plans").assertIsDisplayed()
                composeTestRule.onNodeWithText("Camera").assertIsDisplayed()
                composeTestRule.onNodeWithText("Shopping List").assertIsDisplayed()
                composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
            }

            /**
             * Tests navigation to the Profile screen when the Profile button is clicked.
             */
            @Test
            fun testProfileNavigation() {
                var profileClicked = false

                composeTestRule.setMainActivityContent(
                    username = "TestUser",
                    onProfileClick = { profileClicked = true }
                )

                // Perform a click on the Profile button
                composeTestRule.onNodeWithText("Profile").performClick()

                // Verify the click action
                assert(profileClicked)
            }

            /**
             * Tests navigation to the Shopping List screen when the Shopping List button is clicked.
             */
            @Test
            fun testShoppingListNavigation() {
                var shoppingListClicked = false

                composeTestRule.setMainActivityContent(
                    username = "TestUser",
                    onShoppingListClick = { shoppingListClicked = true }
                )

                // Perform a click on the Shopping List button
                composeTestRule.onNodeWithText("Shopping List").performClick()

                // Verify the click action
                assert(shoppingListClicked)
            }
        }
        setContent {
            MainActivityScreen(
                onCartClick = onCartClick,
                onCameraClick = onCameraClick,
                onProfileClick = onProfileClick,
                onShoppingListClick = onShoppingListClick,
                username = username
            )
        }
    }

    @Test
    fun testWelcomeSectionDisplaysUsername() {
        val username = "TestUser"

        composeTestRule.setContent {
            WelcomeSection(username = username, onForumClick = {})
        }

        // Check that the welcome message and username are displayed
        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
        composeTestRule.onNodeWithText(username).assertIsDisplayed()
    }

    @Test
    fun testBottomMenuItemsDisplayed() {
        composeTestRule.setContent {
            BottomMenu(
                modifier = androidx.compose.ui.Modifier,
                onCartClick = {},
                onCameraClick = {},
                onProfileClick = {},
                onShoppingListClick = {}
            )
        }

        // Assert each menu item is displayed
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Meal Plans").assertIsDisplayed()
        composeTestRule.onNodeWithText("Camera").assertIsDisplayed()
        composeTestRule.onNodeWithText("Shopping List").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    }

    @Test
    fun testProfileNavigation() {
        var profileClicked = false

        composeTestRule.setMainActivityContent(
            username = "TestUser",
            onProfileClick = { profileClicked = true }
        )

        // Perform a click on the Profile button
        composeTestRule.onNodeWithText("Profile").performClick()

        // Verify the click action
        assert(profileClicked)
    }

    @Test
    fun testShoppingListNavigation() {
        var shoppingListClicked = false

        composeTestRule.setMainActivityContent(
            username = "TestUser",
            onShoppingListClick = { shoppingListClicked = true }
        )

        // Perform a click on the Shopping List button
        composeTestRule.onNodeWithText("Shopping List").performClick()

        // Verify the click action
        assert(shoppingListClicked)
    }



}
