package com.example.ui.components

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LedDisplaySettings
import kotlinx.coroutines.delay

@Composable
fun FullscreenSignOverlay(
    text: String,
    settings: LedDisplaySettings,
    rmsLevel: Float,
    onSettingsChanged: (LedDisplaySettings) -> Unit,
    onExitFullscreen: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var showControls by remember { mutableStateOf(true) }
    var isLandscape by remember { mutableStateOf(false) }

    // Auto-hide controls after 4 seconds of inactivity
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(4000)
            showControls = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
            .testTag("fullscreen_sign_overlay")
    ) {
        // Fullscreen LED Signboard Canvas
        LedSignboard(
            text = text,
            settings = settings,
            rmsLevel = rmsLevel,
            isFullscreen = true,
            modifier = Modifier.fillMaxSize()
        )

        // Floating Quick Controls Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exit button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC181824))
                        .clickable { onExitFullscreen() }
                        .testTag("btn_exit_fullscreen"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Fullscreen",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Quick Tool Actions
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xCC181824))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rotate Landscape / Portrait
                    IconButton(
                        onClick = {
                            isLandscape = !isLandscape
                            activity?.requestedOrientation = if (isLandscape) {
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            } else {
                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenRotation,
                            contentDescription = "Rotate",
                            tint = if (isLandscape) Color(0xFFFFB300) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Blink / Flash Toggle
                    IconButton(
                        onClick = {
                            onSettingsChanged(settings.copy(isBlinking = !settings.isBlinking))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Flash",
                            tint = if (settings.isBlinking) Color(0xFFFF2244) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Speed change
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                val nextSpeed = if (settings.speed >= 10) 1 else settings.speed + 2
                                onSettingsChanged(settings.copy(speed = nextSpeed))
                            }
                            .padding(horizontal = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Speed",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${settings.speed}x",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(settings.selectedColorHex)
                        )
                    }
                }
            }
        }

        // Tap hint at bottom (fades out with controls)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text(
                text = "Tap anywhere to hide controls • Landscape supported",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0x99FFFFFF),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x88000000))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
