package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.SUPPORTED_LANGUAGES
import com.example.speech.SpeechState
import com.example.ui.components.CustomizationPanel
import com.example.ui.components.FullscreenSignOverlay
import com.example.ui.components.LanguageSelectorDialog
import com.example.ui.components.LedSignboard
import com.example.ui.components.ManualMessageInput
import com.example.ui.components.PresetsDialog
import com.example.ui.components.VoiceControls
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.LedVoiceSignViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LedVoiceSignViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: LedVoiceSignViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val presets by viewModel.presets.collectAsState()
    val speechState by viewModel.speechState.collectAsState()
    val liveTranscript by viewModel.liveTranscript.collectAsState()
    val rmsLevel by viewModel.rmsLevel.collectAsState()
    val displayMessage by viewModel.displayMessage.collectAsState()

    val selectedLanguage = remember(settings.languageCode) {
        SUPPORTED_LANGUAGES.find { it.code.equals(settings.languageCode, ignoreCase = true) }
            ?: SUPPORTED_LANGUAGES.first()
    }

    // Permission launcher for audio recording
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    val onMicAction = {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            if (speechState is SpeechState.Listening || speechState is SpeechState.Initializing) {
                viewModel.stopListening()
            } else {
                viewModel.startListening()
            }
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    if (uiState.isFullscreen) {
        FullscreenSignOverlay(
            text = displayMessage,
            settings = settings,
            rmsLevel = rmsLevel,
            onSettingsChanged = { viewModel.updateSettings(it) },
            onExitFullscreen = { viewModel.setFullscreen(false) }
        )
        return
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            item {
                Spacer(modifier = Modifier.height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LIVE LED ",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "VOICE SIGN",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(settings.selectedColorHex),
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = stringResource(R.string.app_subtitle),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF7E7E94)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Presets Button
                        IconButton(
                            onClick = { viewModel.showPresetsDialog(true) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1B1B26))
                                .testTag("btn_open_presets")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Presets",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Fullscreen Button
                        IconButton(
                            onClick = { viewModel.setFullscreen(true) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1B1B26))
                                .testTag("btn_open_fullscreen")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Central LED Signboard Display
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    LedSignboard(
                        text = displayMessage,
                        settings = settings,
                        rmsLevel = rmsLevel,
                        height = 200.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick Fullscreen hint chip
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(14.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xBB000000))
                            .clickable { viewModel.setFullscreen(true) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Expand",
                            tint = Color(0xFFCCCCCC),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "FULLSCREEN",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCCCCCC)
                        )
                    }
                }
            }

            // Tab Navigation (Voice & Text vs LED Style vs Presets)
            item {
                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = Color(0xFF101017),
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                            color = Color(settings.selectedColorHex),
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("tab_row")
                ) {
                    Tab(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.setSelectedTab(0) },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(15.dp))
                                Text(
                                    text = stringResource(R.string.tab_controls),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color(0xFF7E7E94)
                    )
                    Tab(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.setSelectedTab(1) },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(15.dp))
                                Text(
                                    text = stringResource(R.string.tab_customize),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color(0xFF7E7E94)
                    )
                }
            }

            // Tab Content
            item {
                when (uiState.selectedTab) {
                    0 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Voice Microphone Engine
                            VoiceControls(
                                speechState = speechState,
                                liveTranscript = liveTranscript,
                                rmsLevel = rmsLevel,
                                selectedLanguage = selectedLanguage,
                                onStartListening = onMicAction,
                                onStopListening = { viewModel.stopListening() },
                                onClearTranscript = { viewModel.clearMessage() },
                                onOpenLanguagePicker = { viewModel.showLanguageDialog(true) }
                            )

                            // Manual Message & Quick Chips
                            ManualMessageInput(
                                onSendMessage = { viewModel.setManualMessage(it) },
                                onSelectPreset = { viewModel.selectPreset(it) },
                                presets = presets
                            )
                        }
                    }
                    1 -> {
                        CustomizationPanel(
                            settings = settings,
                            onSettingsChanged = { viewModel.updateSettings(it) }
                        )
                    }
                }
            }

            // Bottom Spacing & Footer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "LIVE SPEECH RECOGNITION • REAL-TIME LED ENGINE • 60 FPS",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF4E4E64),
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Dialogs
    if (uiState.showLanguageDialog) {
        LanguageSelectorDialog(
            currentLanguageCode = settings.languageCode,
            onLanguageSelected = { viewModel.setLanguage(it) },
            onDismiss = { viewModel.showLanguageDialog(false) }
        )
    }

    if (uiState.showPresetsDialog) {
        PresetsDialog(
            presets = presets,
            currentMessage = displayMessage,
            currentColorHex = settings.selectedColorHex,
            currentSpeed = settings.speed,
            onSelectPreset = { viewModel.selectPreset(it) },
            onSaveNewPreset = { viewModel.savePreset(it) },
            onDeletePreset = { viewModel.deletePreset(it) },
            onDismiss = { viewModel.showPresetsDialog(false) }
        )
    }
}
