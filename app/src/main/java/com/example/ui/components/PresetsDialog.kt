package com.example.ui.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.PresetMessage

@Composable
fun PresetsDialog(
    presets: List<PresetMessage>,
    currentMessage: String,
    currentColorHex: Long,
    currentSpeed: Int,
    onSelectPreset: (PresetMessage) -> Unit,
    onSaveNewPreset: (PresetMessage) -> Unit,
    onDeletePreset: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newEmoji by remember { mutableStateOf("⚡") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 580.dp)
                .testTag("presets_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14141E)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333346))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Presets",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Saved Sign Presets",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF888899)
                        )
                    }
                }

                // Add current message button
                if (!showAddForm) {
                    Button(
                        onClick = {
                            newTitle = if (currentMessage.length > 15) currentMessage.take(15) + "…" else currentMessage
                            showAddForm = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_save_current_preset"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222234)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3A3A50))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Save",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Current Sign as Preset",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    // Quick Save Form
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0B0B12))
                            .border(1.dp, Color(0xFF2E2E42), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "SAVE NEW PRESET",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300),
                            letterSpacing = 1.sp
                        )
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            placeholder = { Text("Preset Title (e.g. TAXI, SALE)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFB300),
                                unfocusedBorderColor = Color(0xFF333346)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 12.sp,
                                color = Color(0xFF888899),
                                modifier = Modifier
                                    .clickable { showAddForm = false }
                                    .padding(8.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newTitle.isNotBlank()) {
                                        onSaveNewPreset(
                                            PresetMessage(
                                                id = System.currentTimeMillis().toString(),
                                                title = newTitle.trim(),
                                                message = currentMessage,
                                                colorHex = currentColorHex,
                                                speed = currentSpeed,
                                                iconEmoji = newEmoji
                                            )
                                        )
                                        showAddForm = false
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                            ) {
                                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Presets List
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presets, key = { it.id }) { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF191924))
                                .border(1.dp, Color(preset.colorHex).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .clickable {
                                    onSelectPreset(preset)
                                    onDismiss()
                                }
                                .padding(12.dp)
                                .testTag("preset_item_${preset.id}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(text = preset.iconEmoji, fontSize = 20.sp)
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = preset.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(preset.colorHex)
                                    )
                                    Text(
                                        text = preset.message,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF9E9EB2),
                                        maxLines = 1
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        onSelectPreset(preset)
                                        onDismiss()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color(preset.colorHex),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                if (preset.id.length > 2) { // Allow deleting custom presets
                                    IconButton(
                                        onClick = { onDeletePreset(preset.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFF884444),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
