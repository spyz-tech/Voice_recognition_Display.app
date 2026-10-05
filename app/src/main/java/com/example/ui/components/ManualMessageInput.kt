package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.PresetMessage

@Composable
fun ManualMessageInput(
    onSendMessage: (String) -> Unit,
    onSelectPreset: (PresetMessage) -> Unit,
    presets: List<PresetMessage>,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("manual_message_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF282836))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "MANUAL TEXT & QUICK PRESETS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7E7E94),
                letterSpacing = 1.2.sp
            )

            // Text Input Field with Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.placeholder_manual_input),
                            fontSize = 13.sp,
                            color = Color(0xFF666677)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("manual_text_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFFE0E0FF),
                        focusedContainerColor = Color(0xFF09090E),
                        unfocusedContainerColor = Color(0xFF09090E),
                        focusedBorderColor = Color(0xFFFF2233),
                        unfocusedBorderColor = Color(0xFF282838),
                        cursorColor = Color(0xFFFF2233)
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (textInput.isNotBlank()) {
                                onSendMessage(textInput)
                                keyboardController?.hide()
                            }
                        }
                    ),
                    trailingIcon = {
                        if (textInput.isNotEmpty()) {
                            IconButton(onClick = { textInput = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear input",
                                    tint = Color(0xFF888899)
                                )
                            }
                        }
                    },
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSendMessage(textInput)
                            keyboardController?.hide()
                        }
                    },
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("btn_display_manual_message"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF252535)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3E3E52))
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Display",
                        tint = Color(0xFFFF4455),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Quick Presets Horizontal Scroll
            Text(
                text = "POPULAR PHRASES",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF55556A),
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.take(6).forEach { preset ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1B1B26))
                            .border(1.dp, Color(preset.colorHex).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .clickable {
                                textInput = preset.message
                                onSelectPreset(preset)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("preset_chip_${preset.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = preset.iconEmoji, fontSize = 13.sp)
                        Text(
                            text = preset.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(preset.colorHex)
                        )
                    }
                }
            }
        }
    }
}
