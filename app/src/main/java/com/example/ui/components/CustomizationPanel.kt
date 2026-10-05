package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DEFAULT_COLOR_PRESETS
import com.example.model.LedDisplaySettings
import com.example.model.LedShape
import com.example.model.ScrollDirection

@Composable
fun CustomizationPanel(
    settings: LedDisplaySettings,
    onSettingsChanged: (LedDisplaySettings) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("customization_panel_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF282836))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Customize",
                        tint = Color(settings.selectedColorHex),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "LED SIGNBOARD CUSTOMIZATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9090A8),
                        letterSpacing = 1.sp
                    )
                }
            }

            // 1. LED Color Palette Row
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.setting_color).uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7E7E94),
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rainbow spectrum option
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.sweepGradient(
                                    listOf(
                                        Color.Red, Color.Yellow, Color.Green,
                                        Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                                    )
                                )
                            )
                            .border(
                                width = if (settings.rainbowMode) 3.dp else 1.dp,
                                color = if (settings.rainbowMode) Color.White else Color(0x55FFFFFF),
                                shape = CircleShape
                            )
                            .clickable {
                                onSettingsChanged(settings.copy(rainbowMode = true))
                            }
                            .testTag("color_rainbow"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (settings.rainbowMode) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Rainbow Active",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Preset Color Swatches
                    DEFAULT_COLOR_PRESETS.forEach { preset ->
                        val isSelected = !settings.rainbowMode && settings.selectedColorHex == preset.hexColor
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(preset.hexColor))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color(0x33FFFFFF),
                                    shape = CircleShape
                                )
                                .clickable {
                                    onSettingsChanged(
                                        settings.copy(
                                            selectedColorHex = preset.hexColor,
                                            rainbowMode = false
                                        )
                                    )
                                }
                                .testTag("color_preset_${preset.name.replace(" ", "_")}"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Scroll Direction / Motion Mode Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "MOTION & ANIMATION MODE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7E7E94),
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScrollDirection.values().forEach { dir ->
                        val isSelected = settings.scrollDirection == dir
                        val title = when (dir) {
                            ScrollDirection.RIGHT_TO_LEFT -> "Scroll Left ←"
                            ScrollDirection.LEFT_TO_RIGHT -> "Scroll Right →"
                            ScrollDirection.STATIC_CENTER -> "Static Centered"
                            ScrollDirection.BLINKING -> "Blink Flashing ⚡"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(settings.selectedColorHex).copy(alpha = 0.2f) else Color(0xFF1B1B26))
                                .border(
                                    1.dp,
                                    if (isSelected) Color(settings.selectedColorHex) else Color(0xFF2E2E40),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onSettingsChanged(settings.copy(scrollDirection = dir))
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .testTag("mode_${dir.name}")
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(settings.selectedColorHex) else Color(0xFFA0A0B0)
                            )
                        }
                    }
                }
            }

            // 3. LED Diode Shapes (Round, Square, Diamond)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "LED DIODE SHAPE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7E7E94),
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LedShape.values().forEach { shape ->
                        val isSelected = settings.ledShape == shape
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF28283C) else Color(0xFF1A1A24))
                                .border(
                                    1.dp,
                                    if (isSelected) Color(settings.selectedColorHex) else Color(0xFF282836),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSettingsChanged(settings.copy(ledShape = shape)) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (shape) {
                                    LedShape.CIRCLE -> "● Round"
                                    LedShape.SQUARE -> "■ Square"
                                    LedShape.DIAMOND -> "◆ Diamond"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else Color(0xFF888899)
                            )
                        }
                    }
                }
            }

            // 4. Sliders Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Scroll Speed Slider
                SliderSettingItem(
                    label = stringResource(R.string.setting_speed),
                    valueDisplay = "${settings.speed}x",
                    value = settings.speed.toFloat(),
                    valueRange = 1f..10f,
                    steps = 8,
                    onValueChange = { onSettingsChanged(settings.copy(speed = it.toInt())) },
                    activeColor = Color(settings.selectedColorHex)
                )

                // Matrix Dot Density / Size Slider
                SliderSettingItem(
                    label = stringResource(R.string.setting_font_size),
                    valueDisplay = "${settings.dotDensity} rows",
                    value = settings.dotDensity.toFloat(),
                    valueRange = 12f..24f,
                    steps = 11,
                    onValueChange = { onSettingsChanged(settings.copy(dotDensity = it.toInt())) },
                    activeColor = Color(settings.selectedColorHex)
                )

                // Glow Intensity Slider
                SliderSettingItem(
                    label = stringResource(R.string.setting_glow),
                    valueDisplay = "${(settings.glowIntensity * 100).toInt()}%",
                    value = settings.glowIntensity,
                    valueRange = 0f..1.2f,
                    onValueChange = { onSettingsChanged(settings.copy(glowIntensity = it)) },
                    activeColor = Color(settings.selectedColorHex)
                )

                // Brightness Slider
                SliderSettingItem(
                    label = stringResource(R.string.setting_brightness),
                    valueDisplay = "${(settings.brightness * 100).toInt()}%",
                    value = settings.brightness,
                    valueRange = 0.3f..1.5f,
                    onValueChange = { onSettingsChanged(settings.copy(brightness = it)) },
                    activeColor = Color(settings.selectedColorHex)
                )
            }

            // 5. Toggles Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0C0C12))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ToggleSettingRow(
                    label = "Scanlines CRT Effect",
                    checked = settings.showScanlines,
                    onCheckedChange = { onSettingsChanged(settings.copy(showScanlines = it)) }
                )
                ToggleSettingRow(
                    label = "Pixel Matrix Unlit Grid",
                    checked = settings.showPixelGrid,
                    onCheckedChange = { onSettingsChanged(settings.copy(showPixelGrid = it)) }
                )
                ToggleSettingRow(
                    label = "Audio-Reactive Glow (Voice Boost)",
                    checked = settings.soundReactive,
                    onCheckedChange = { onSettingsChanged(settings.copy(soundReactive = it)) }
                )
                ToggleSettingRow(
                    label = "Mirror HUD Mode (Windshield Reflection)",
                    checked = settings.mirrorMode,
                    onCheckedChange = { onSettingsChanged(settings.copy(mirrorMode = it)) }
                )
            }
        }
    }
}

@Composable
private fun SliderSettingItem(
    label: String,
    valueDisplay: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    activeColor: Color,
    steps: Int = 0
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFA0A0B0)
            )
            Text(
                text = valueDisplay,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = activeColor,
                activeTrackColor = activeColor,
                inactiveTrackColor = Color(0xFF282836)
            ),
            modifier = Modifier.height(30.dp)
        )
    }
}

@Composable
private fun ToggleSettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (checked) Color(0xFFE0E0FF) else Color(0xFF777788)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFFF2233),
                uncheckedThumbColor = Color(0xFF888899),
                uncheckedTrackColor = Color(0xFF22222E)
            )
        )
    }
}
