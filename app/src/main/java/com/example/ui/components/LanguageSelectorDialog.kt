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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.SUPPORTED_LANGUAGES
import com.example.model.SpeechLanguage

@Composable
fun LanguageSelectorDialog(
    currentLanguageCode: String,
    onLanguageSelected: (SpeechLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp)
                .testTag("language_selector_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14141E)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333346))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title Bar
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
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = Color(0xFFFF2244),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Speech Language",
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

                Text(
                    text = "Select speech recognition transcription language:",
                    fontSize = 12.sp,
                    color = Color(0xFF8E8E9E)
                )

                // List of languages
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SUPPORTED_LANGUAGES) { lang ->
                        val isSelected = lang.code.equals(currentLanguageCode, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF281E2E) else Color(0xFF1B1B26))
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFFFF2244) else Color(0xFF262638),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onLanguageSelected(lang)
                                    onDismiss()
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .testTag("lang_item_${lang.code}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(text = lang.flagEmoji, fontSize = 20.sp)
                                Column {
                                    Text(
                                        text = lang.displayName,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFFD8D8E8)
                                    )
                                    Text(
                                        text = lang.code,
                                        fontSize = 11.sp,
                                        color = Color(0xFF7A7A8E)
                                    )
                                }
                            }
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF2244)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
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
