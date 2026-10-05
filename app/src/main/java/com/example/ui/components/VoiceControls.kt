package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.SpeechLanguage
import com.example.speech.SpeechState

@Composable
fun VoiceControls(
    speechState: SpeechState,
    liveTranscript: String,
    rmsLevel: Float,
    selectedLanguage: SpeechLanguage,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onClearTranscript: () -> Unit,
    onOpenLanguagePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isListening = speechState is SpeechState.Listening || speechState is SpeechState.Initializing

    val pulseTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("voice_controls_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF13131A)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF282836))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Status & Language Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status indicator pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            when (speechState) {
                                is SpeechState.Listening -> Color(0xFF0F381E)
                                is SpeechState.Initializing -> Color(0xFF382F0F)
                                is SpeechState.Error -> Color(0xFF3B1216)
                                else -> Color(0xFF1F1F2A)
                            }
                        )
                        .border(
                            1.dp,
                            when (speechState) {
                                is SpeechState.Listening -> Color(0xFF00E676)
                                is SpeechState.Initializing -> Color(0xFFFFB300)
                                is SpeechState.Error -> Color(0xFFFF334B)
                                else -> Color(0xFF3C3C4E)
                            },
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(
                                when (speechState) {
                                    is SpeechState.Listening -> Color(0xFF00E676)
                                    is SpeechState.Initializing -> Color(0xFFFFB300)
                                    is SpeechState.Error -> Color(0xFFFF334B)
                                    else -> Color(0xFF888899)
                                }
                            )
                    )
                    Text(
                        text = when (speechState) {
                            is SpeechState.Listening -> stringResource(R.string.status_listening)
                            is SpeechState.Initializing -> "Starting Mic…"
                            is SpeechState.Error -> stringResource(R.string.status_error)
                            else -> stringResource(R.string.status_not_listening)
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (speechState) {
                            is SpeechState.Listening -> Color(0xFF80FFB7)
                            is SpeechState.Initializing -> Color(0xFFFFD54F)
                            is SpeechState.Error -> Color(0xFFFF8A95)
                            else -> Color(0xFFAAAAAA)
                        }
                    )
                }

                // Language selection pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1E1E2B))
                        .border(1.dp, Color(0xFF333346), RoundedCornerShape(20.dp))
                        .clickable { onOpenLanguagePicker() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("btn_select_language"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = selectedLanguage.flagEmoji, fontSize = 13.sp)
                    Text(
                        text = selectedLanguage.code,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFD0D0E0)
                    )
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = Color(0xFFA0A0C0),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Error banner if speech error
            AnimatedVisibility(visible = speechState is SpeechState.Error) {
                if (speechState is SpeechState.Error) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33FF1744))
                            .border(1.dp, Color(0x66FF1744), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = speechState.message,
                            fontSize = 12.sp,
                            color = Color(0xFFFFCDD2),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Primary Big Mic Button
            Button(
                onClick = {
                    if (isListening) onStopListening() else onStartListening()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .then(if (isListening) Modifier.scale(pulseScale) else Modifier)
                    .testTag("btn_toggle_mic"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening) Color(0xFF2C2C3A) else Color(0xFFFF2233)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (isListening) 2.dp else 8.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop Mic" else "Start Mic",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isListening) stringResource(R.string.btn_stop_listening)
                        else stringResource(R.string.btn_start_listening),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }

            // Audio Waveform / Level Meter (when listening)
            AnimatedVisibility(visible = isListening) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0A0A10))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE AUDIO INPUT LEVEL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${(rmsLevel * 100).toInt()}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF80FFB7)
                        )
                    }

                    // Audio level bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFF1E1E28)),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val totalSegments = 24
                        val activeSegments = (rmsLevel * totalSegments).toInt().coerceIn(0, totalSegments)
                        for (i in 0 until totalSegments) {
                            val isSegmentActive = i < activeSegments
                            val segColor = when {
                                i > 18 -> Color(0xFFFF2244)
                                i > 12 -> Color(0xFFFFB300)
                                else -> Color(0xFF00E676)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (isSegmentActive) segColor else Color(0xFF282836))
                            )
                        }
                    }
                }
            }

            // Live Transcript Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF09090E))
                    .border(1.dp, Color(0xFF1F1F2C), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.label_live_transcript).uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7E7E94),
                        letterSpacing = 1.sp
                    )
                    if (liveTranscript.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.btn_clear_screen),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252),
                            modifier = Modifier
                                .clickable { onClearTranscript() }
                                .testTag("btn_clear_transcript")
                        )
                    }
                }

                Text(
                    text = if (liveTranscript.isBlank()) stringResource(R.string.label_awaiting_speech) else liveTranscript,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (liveTranscript.isBlank()) FontWeight.Normal else FontWeight.Bold,
                    color = if (liveTranscript.isBlank()) Color(0xFF555566) else Color(0xFFE0E0FF),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
