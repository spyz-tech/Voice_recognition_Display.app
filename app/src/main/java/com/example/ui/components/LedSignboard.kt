package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.Typeface
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.LedDisplaySettings
import com.example.model.LedShape
import com.example.model.ScrollDirection
import kotlin.math.sin

/**
 * High performance LED Signboard Canvas component that turns any text into an authentic
 * digital LED scrolling matrix display with illuminated diodes, glow halo, raster scanlines,
 * bezel frame, and sound-reactive brightness.
 */
@Composable
fun LedSignboard(
    text: String,
    settings: LedDisplaySettings,
    rmsLevel: Float = 0f,
    modifier: Modifier = Modifier,
    height: Dp = 220.dp,
    isFullscreen: Boolean = false
) {
    val displayText = if (text.isBlank()) "SPEAK SOMETHING..." else text.trim()

    // Smooth scroll position driven by nano frame clock
    var scrollOffsetPx by remember { mutableFloatStateOf(0f) }
    var lastFrameTimeNanos by remember { mutableLongStateOf(0L) }

    // Blink animation
    val blinkAnim = remember { Animatable(1f) }
    LaunchedEffect(settings.isBlinking, settings.scrollDirection) {
        if (settings.isBlinking || settings.scrollDirection == ScrollDirection.BLINKING) {
            blinkAnim.animateTo(
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            blinkAnim.snapTo(1f)
        }
    }

    // Scroll animation loop
    LaunchedEffect(settings.speed, settings.scrollDirection) {
        lastFrameTimeNanos = 0L
        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameTimeNanos != 0L) {
                    val deltaSeconds = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f
                    // Speed multiplier: 1 to 10 mapped to 60px/s - 600px/s
                    val speedPxPerSec = (settings.speed * 65f + 40f)

                    when (settings.scrollDirection) {
                        ScrollDirection.RIGHT_TO_LEFT -> {
                            scrollOffsetPx += speedPxPerSec * deltaSeconds
                        }
                        ScrollDirection.LEFT_TO_RIGHT -> {
                            scrollOffsetPx -= speedPxPerSec * deltaSeconds
                        }
                        ScrollDirection.STATIC_CENTER, ScrollDirection.BLINKING -> {
                            // No continuous scrolling
                        }
                    }
                }
                lastFrameTimeNanos = frameTimeNanos
            }
        }
    }

    // Outer bezel box
    Box(
        modifier = modifier
            .testTag("led_signboard_container")
            .then(
                if (isFullscreen) Modifier.fillMaxSize()
                else Modifier
                    .fillMaxWidth()
                    .height(height)
            )
            .shadow(
                elevation = if (isFullscreen) 0.dp else 16.dp,
                shape = RoundedCornerShape(if (isFullscreen) 0.dp else 16.dp),
                spotColor = Color(settings.selectedColorHex).copy(alpha = 0.35f * settings.glowIntensity)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF222228),
                        Color(0xFF141418),
                        Color(0xFF0D0D10),
                        Color(0xFF1A1A20)
                    )
                ),
                shape = RoundedCornerShape(if (isFullscreen) 0.dp else 16.dp)
            )
            .border(
                width = if (isFullscreen) 0.dp else 4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF444450),
                        Color(0xFF202026),
                        Color(0xFF15151A),
                        Color(0xFF33333E)
                    )
                ),
                shape = RoundedCornerShape(if (isFullscreen) 0.dp else 16.dp)
            )
            .padding(if (isFullscreen) 0.dp else 10.dp)
    ) {
        // Inner Display Canvas
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(if (isFullscreen) 0.dp else 8.dp))
                .background(Color(0xFF050507))
        ) {
            val canvasWidth = constraints.maxWidth.toFloat()
            val canvasHeight = constraints.maxHeight.toFloat()

            // Calculate grid rows and cols based on density
            val numRows = settings.dotDensity // e.g. 14 to 24 rows
            val dotSpacing = canvasHeight / numRows.toFloat()
            val dotRadius = (dotSpacing * 0.40f) // diode radius
            val numCols = (canvasWidth / dotSpacing).toInt() + 2

            // Prepare text raster bitmap cache
            val textMatrix = remember(displayText, numRows, settings.letterSpacing) {
                renderTextToDotMatrix(displayText, numRows, settings.letterSpacing)
            }

            val textMatrixWidth = textMatrix.firstOrNull()?.size ?: 1
            // Total loop length in matrix columns (text width + screen spacing gap)
            val gapCols = (numCols * 0.6f).toInt().coerceAtLeast(8)
            val totalLoopCols = textMatrixWidth + gapCols
            val totalLoopPx = totalLoopCols * dotSpacing

            // Rainbow cycle phase
            var rainbowPhase by remember { mutableFloatStateOf(0f) }
            LaunchedEffect(settings.rainbowMode) {
                while (settings.rainbowMode) {
                    withFrameNanos {
                        rainbowPhase = (rainbowPhase + 0.02f) % (2f * Math.PI.toFloat())
                    }
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("led_canvas")
            ) {
                val isBlinkOff = (settings.isBlinking || settings.scrollDirection == ScrollDirection.BLINKING) &&
                        blinkAnim.value < 0.5f

                val baseColor = Color(settings.selectedColorHex)
                val reactiveBoost = if (settings.soundReactive) (rmsLevel * 0.5f) else 0f
                val effectiveBrightness = (settings.brightness + reactiveBoost).coerceIn(0.2f, 2.0f)
                val effectiveGlow = (settings.glowIntensity + reactiveBoost * 0.5f).coerceIn(0f, 1.5f)

                // Draw each diode on the screen grid
                val currentOffsetPx = if (settings.scrollDirection == ScrollDirection.STATIC_CENTER) {
                    // Center the text
                    val startX = (canvasWidth - (textMatrixWidth * dotSpacing)) / 2f
                    -startX
                } else {
                    if (totalLoopPx > 0) {
                        (scrollOffsetPx % totalLoopPx + totalLoopPx) % totalLoopPx
                    } else 0f
                }

                // If mirror mode enabled, flip horizontally
                val shouldMirror = settings.mirrorMode

                for (r in 0 until numRows) {
                    val centerY = (r + 0.5f) * dotSpacing

                    for (c in 0 until numCols) {
                        val visualCol = if (shouldMirror) (numCols - 1 - c) else c
                        val centerX = (visualCol + 0.5f) * dotSpacing

                        // Determine matrix sample column
                        val sampleColFloat = (currentOffsetPx + (c * dotSpacing)) / dotSpacing
                        val sampleColInt = ((sampleColFloat.toInt() % totalLoopCols) + totalLoopCols) % totalLoopCols

                        val isLit = if (isBlinkOff) {
                            false
                        } else if (sampleColInt in 0 until textMatrixWidth && r in textMatrix.indices) {
                            textMatrix[r][sampleColInt]
                        } else {
                            false
                        }

                        // Determine diode color
                        val diodeColor = if (settings.rainbowMode) {
                            val hue = (((c + sampleColInt) * 8f) + (rainbowPhase * 57.3f)) % 360f
                            hsvToColor(hue, 0.95f, 1f)
                        } else {
                            baseColor
                        }

                        if (isLit) {
                            // 1. Glow bloom (if glow > 0)
                            if (effectiveGlow > 0.05f) {
                                val glowRadius = dotRadius * (1.6f + effectiveGlow * 1.8f)
                                drawCircle(
                                    color = diodeColor.copy(alpha = (0.28f * effectiveGlow * effectiveBrightness).coerceIn(0f, 0.9f)),
                                    radius = glowRadius,
                                    center = Offset(centerX, centerY)
                                )
                                drawCircle(
                                    color = diodeColor.copy(alpha = (0.45f * effectiveGlow * effectiveBrightness).coerceIn(0f, 0.95f)),
                                    radius = dotRadius * 1.3f,
                                    center = Offset(centerX, centerY)
                                )
                            }

                            // 2. Main Diode Body
                            when (settings.ledShape) {
                                LedShape.CIRCLE -> {
                                    drawCircle(
                                        color = diodeColor.copy(alpha = (0.95f * effectiveBrightness).coerceIn(0f, 1f)),
                                        radius = dotRadius,
                                        center = Offset(centerX, centerY)
                                    )
                                    // 3. Bright core center
                                    drawCircle(
                                        color = Color.White.copy(alpha = (0.75f * effectiveBrightness).coerceIn(0f, 1f)),
                                        radius = dotRadius * 0.42f,
                                        center = Offset(centerX, centerY)
                                    )
                                }
                                LedShape.SQUARE -> {
                                    val size = dotRadius * 1.7f
                                    drawRect(
                                        color = diodeColor.copy(alpha = (0.95f * effectiveBrightness).coerceIn(0f, 1f)),
                                        topLeft = Offset(centerX - size / 2, centerY - size / 2),
                                        size = Size(size, size)
                                    )
                                    val coreSize = size * 0.42f
                                    drawRect(
                                        color = Color.White.copy(alpha = (0.75f * effectiveBrightness).coerceIn(0f, 1f)),
                                        topLeft = Offset(centerX - coreSize / 2, centerY - coreSize / 2),
                                        size = Size(coreSize, coreSize)
                                    )
                                }
                                LedShape.DIAMOND -> {
                                    val dSize = dotRadius * 1.8f
                                    val path = Path().apply {
                                        moveTo(centerX, centerY - dSize / 2)
                                        lineTo(centerX + dSize / 2, centerY)
                                        lineTo(centerX, centerY + dSize / 2)
                                        lineTo(centerX - dSize / 2, centerY)
                                        close()
                                    }
                                    drawPath(
                                        path = path,
                                        color = diodeColor.copy(alpha = (0.95f * effectiveBrightness).coerceIn(0f, 1f))
                                    )
                                    drawCircle(
                                        color = Color.White.copy(alpha = (0.75f * effectiveBrightness).coerceIn(0f, 1f)),
                                        radius = dotRadius * 0.38f,
                                        center = Offset(centerX, centerY)
                                    )
                                }
                            }
                        } else if (settings.showPixelGrid) {
                            // Unlit inactive diode (gives realistic physical hardware appearance)
                            when (settings.ledShape) {
                                LedShape.CIRCLE -> {
                                    drawCircle(
                                        color = Color(0xFF14141C),
                                        radius = dotRadius * 0.75f,
                                        center = Offset(centerX, centerY)
                                    )
                                    drawCircle(
                                        color = Color(0xFF20202C).copy(alpha = 0.5f),
                                        radius = dotRadius * 0.75f,
                                        center = Offset(centerX, centerY),
                                        style = Stroke(width = 0.8f)
                                    )
                                }
                                LedShape.SQUARE -> {
                                    val s = dotRadius * 1.3f
                                    drawRect(
                                        color = Color(0xFF14141C),
                                        topLeft = Offset(centerX - s / 2, centerY - s / 2),
                                        size = Size(s, s)
                                    )
                                }
                                LedShape.DIAMOND -> {
                                    val dSize = dotRadius * 1.3f
                                    val path = Path().apply {
                                        moveTo(centerX, centerY - dSize / 2)
                                        lineTo(centerX + dSize / 2, centerY)
                                        lineTo(centerX, centerY + dSize / 2)
                                        lineTo(centerX - dSize / 2, centerY)
                                        close()
                                    }
                                    drawPath(path = path, color = Color(0xFF14141C))
                                }
                            }
                        }
                    }
                }

                // 4. Scanlines overlay (if enabled)
                if (settings.showScanlines) {
                    val scanlineSpacing = 4f
                    var y = 0f
                    while (y < canvasHeight) {
                        drawLine(
                            color = Color(0x33000000),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1.5f
                        )
                        y += scanlineSpacing
                    }
                }

                // 5. Acrylic glass sheen reflection gradient
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x12FFFFFF),
                            Color(0x04FFFFFF),
                            Color(0x00000000),
                            Color(0x08FFFFFF)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(canvasWidth, canvasHeight)
                    ),
                    size = Size(canvasWidth, canvasHeight)
                )

                // 6. Corner screw hardware accents (in normal mode)
                if (!isFullscreen) {
                    val screwRadius = 3f
                    val screwColor = Color(0xFF555562)
                    drawCircle(screwColor, screwRadius, Offset(8f, 8f))
                    drawCircle(screwColor, screwRadius, Offset(canvasWidth - 8f, 8f))
                    drawCircle(screwColor, screwRadius, Offset(8f, canvasHeight - 8f))
                    drawCircle(screwColor, screwRadius, Offset(canvasWidth - 8f, canvasHeight - 8f))
                }
            }
        }
    }
}

// Memory cache for rendered dot matrix fonts to avoid re-rasterizing identical text
private val dotMatrixCache = android.util.LruCache<String, Array<BooleanArray>>(128)

/**
 * Universal text-to-dot-matrix rasterizer that renders any text/script (English, Hindi, Marathi,
 * Spanish, Japanese, Chinese, Emojis, Symbols) onto an off-screen bitmap and samples pixel luminance.
 */
fun renderTextToDotMatrix(
    text: String,
    matrixHeight: Int,
    letterSpacingMultiplier: Float = 1.0f
): Array<BooleanArray> {
    if (text.isEmpty()) {
        return Array(matrixHeight) { BooleanArray(1) { false } }
    }

    val cacheKey = "${text}_${matrixHeight}_${letterSpacingMultiplier}"
    synchronized(dotMatrixCache) {
        dotMatrixCache.get(cacheKey)?.let { return it }
    }

    try {
        // Bitmap height = matrixHeight (e.g. 16 or 20 pixels high)
        val bmpHeight = matrixHeight.coerceAtLeast(8)
        val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = bmpHeight * 0.88f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = (0.12f * letterSpacingMultiplier)
        }

        val textWidthMeasured = paint.measureText(text)
        val bmpWidth = (textWidthMeasured.toInt() + 8).coerceAtLeast(bmpHeight)

        // Using standard ARGB_8888 to avoid ashmem pinning warnings
        val bitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)

        // Center vertically
        val fontMetrics = paint.fontMetrics
        val baselineY = (bmpHeight - (fontMetrics.descent + fontMetrics.ascent)) / 2f
        canvas.drawText(text, 2f, baselineY, paint)

        // Bulk extraction of pixels
        val pixelArray = IntArray(bmpWidth * bmpHeight)
        bitmap.getPixels(pixelArray, 0, bmpWidth, 0, 0, bmpWidth, bmpHeight)

        // Sample pixel luminance into BooleanArray grid [row][col]
        val result = Array(bmpHeight) { BooleanArray(bmpWidth) }

        for (y in 0 until bmpHeight) {
            val rowOffset = y * bmpWidth
            for (x in 0 until bmpWidth) {
                val pixel = pixelArray[rowOffset + x]
                // Check RGB luminosity / alpha
                val alpha = (pixel ushr 24) and 0xFF
                val red = (pixel ushr 16) and 0xFF
                val green = (pixel ushr 8) and 0xFF
                val blue = pixel and 0xFF
                val luminance = (0.299f * red + 0.587f * green + 0.114f * blue).toInt()
                result[y][x] = (alpha > 80 && luminance > 70)
            }
        }

        bitmap.recycle()

        synchronized(dotMatrixCache) {
            dotMatrixCache.put(cacheKey, result)
        }

        return result
    } catch (e: Exception) {
        Log.e("LedSignboard", "Error rasterizing text: ${e.message}", e)
        return Array(matrixHeight) { BooleanArray(text.length * 6) { (it % 2 == 0) } }
    }
}

private fun hsvToColor(hue: Float, saturation: Float, value: Float): Color {
    val hsv = floatArrayOf(hue, saturation, value)
    val argb = android.graphics.Color.HSVToColor(hsv)
    return Color(argb)
}
