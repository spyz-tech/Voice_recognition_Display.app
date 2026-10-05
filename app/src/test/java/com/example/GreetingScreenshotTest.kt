package com.example

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.model.LedDisplaySettings
import com.example.ui.components.LedSignboard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun led_signboard_screenshot() {
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.setContent {
      MyApplicationTheme {
        LedSignboard(
          text = "LIVE LED SIGN",
          settings = LedDisplaySettings(),
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
        )
      }
    }

    composeTestRule.mainClock.advanceTimeBy(16)
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
