package com.drew654.mocklocations.presentation.map_screen.components

import android.provider.Settings
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.platform.app.InstrumentationRegistry
import com.drew654.mocklocations.domain.model.Permission
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PermissionsDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun clickCancel_triggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            PermissionsDialog(
                permission = Permission.FineLocation,
                onDismiss = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Cancel").performClick()

        assertTrue(clicked)
    }

    @Test
    fun fineLocationPermission_displaysCorrectText_andLaunchesAppSettings() {
        composeTestRule.setContent {
            PermissionsDialog(permission = Permission.FineLocation)
        }

        composeTestRule.onNodeWithText("Location Permission Required").assertIsDisplayed()
        composeTestRule.onNodeWithText("To use this app, you must grant \"Fine Location\" permission in App Settings.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Open Location Settings").performClick()

        intended(hasAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS))
        intended(hasData("package:${InstrumentationRegistry.getInstrumentation().targetContext.packageName}"))
    }

    @Test
    fun mockLocationPermission_displaysCorrectText_andLaunchesSettings() {
        composeTestRule.setContent {
            PermissionsDialog(permission = Permission.MockLocations)
        }

        composeTestRule.onNodeWithText("Developer Options Required").assertIsDisplayed()
        composeTestRule.onNodeWithText("To use this app, you must select \"Mock Locations\" as the Mock Location App in Developer Options. It should be near the bottom of the list.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Open Developer Options").performClick()

        intended(hasAction(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)) }

    @Test
    fun developerOptionsPermission_displaysCorrectText_andLaunchesSettings() {
        composeTestRule.setContent {
            PermissionsDialog(permission = Permission.DeveloperOptions)
        }

        composeTestRule.onNodeWithText("Developer Options Required").assertIsDisplayed()
        composeTestRule.onNodeWithText("You need to enable Developer Options first. Go to Settings > About Phone and tap \"Build Number\" 7 times. \"Build Number\" may be found in About Phone > Software Information on Samsung devices.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Open About Phone").performClick()

        intended(hasAction(Settings.ACTION_DEVICE_INFO_SETTINGS))
    }

    @Test
    fun postNotificationsPermission_doesNotDisplayDialog() {
        composeTestRule.setContent {
            PermissionsDialog(permission = Permission.PostNotifications)
        }

        composeTestRule.onRoot().onChildren().assertCountEquals(0)
    }
}
