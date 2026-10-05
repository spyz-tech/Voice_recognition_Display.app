package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

enum class ScrollDirection {
    RIGHT_TO_LEFT,
    LEFT_TO_RIGHT,
    STATIC_CENTER,
    BLINKING
}

enum class LedShape {
    CIRCLE,
    SQUARE,
    DIAMOND
}

data class LedColorPreset(
    val name: String,
    val hexColor: Long,
    val glowHexColor: Long = hexColor
) {
    val composeColor: Color get() = Color(hexColor)
    val glowColor: Color get() = Color(glowHexColor)
}

val DEFAULT_COLOR_PRESETS = listOf(
    LedColorPreset("Classic Red", 0xFFFF2222, 0xFFFF0033),
    LedColorPreset("Neon Green", 0xFF00FF66, 0xFF00E65B),
    LedColorPreset("Electric Blue", 0xFF00AAFF, 0xFF0088FF),
    LedColorPreset("Amber Gold", 0xFFFFB300, 0xFFFF9900),
    LedColorPreset("Pure White", 0xFFFFFFFF, 0xFFE0E0FF),
    LedColorPreset("Cyber Cyan", 0xFF00FFFF, 0xFF00D4FF),
    LedColorPreset("Neon Purple", 0xFFD400FF, 0xFFB700FF),
    LedColorPreset("Hot Pink", 0xFFFF1493, 0xFFFF007F),
    LedColorPreset("Solar Orange", 0xFFFF5500, 0xFFFF3300),
    LedColorPreset("Matrix Lime", 0xFF39FF14, 0xFF28D90B)
)

data class SpeechLanguage(
    val code: String,
    val displayName: String,
    val flagEmoji: String
)

val SUPPORTED_LANGUAGES = listOf(
    SpeechLanguage("en-US", "English (United States)", "🇺🇸"),
    SpeechLanguage("en-IN", "English (India)", "🇮🇳"),
    SpeechLanguage("en-GB", "English (United Kingdom)", "🇬🇧"),
    SpeechLanguage("hi-IN", "Hindi (हिन्दी)", "🇮🇳"),
    SpeechLanguage("mr-IN", "Marathi (मराठी)", "🇮🇳"),
    SpeechLanguage("es-ES", "Spanish (Español)", "🇪🇸"),
    SpeechLanguage("fr-FR", "French (Français)", "🇫🇷"),
    SpeechLanguage("de-DE", "German (Deutsch)", "🇩🇪"),
    SpeechLanguage("ja-JP", "Japanese (日本語)", "🇯🇵"),
    SpeechLanguage("zh-CN", "Chinese (Mandarin)", "🇨🇳"),
    SpeechLanguage("pt-BR", "Portuguese (Brasil)", "🇧🇷"),
    SpeechLanguage("it-IT", "Italian (Italiano)", "🇮🇹"),
    SpeechLanguage("ar-SA", "Arabic (العربية)", "🇸🇦"),
    SpeechLanguage("ru-RU", "Russian (Русский)", "🇷🇺"),
    SpeechLanguage("ko-KR", "Korean (한국어)", "🇰🇷")
)

data class PresetMessage(
    val id: String,
    val title: String,
    val message: String,
    val colorHex: Long = 0xFFFF2222,
    val speed: Int = 5,
    val iconEmoji: String = "💬"
)

val DEFAULT_PRESETS = listOf(
    PresetMessage("1", "Welcome", "WELCOME TO THE SHOW! ★ ENJOY YOUR NIGHT ★", 0xFF00FF66, 6, "👋"),
    PresetMessage("2", "Special Sale", "🔥 MEGA SALE 50% OFF TODAY ONLY 🔥", 0xFFFF2222, 7, "🏷️"),
    PresetMessage("3", "Airport Pickup", "WELCOME TO CITY ★ MR. SMITH ★ GATE 4", 0xFFFFB300, 5, "✈️"),
    PresetMessage("4", "Taxi Available", "TAXI AVAILABLE • ON CALL • 24/7", 0xFFFFB300, 5, "🚖"),
    PresetMessage("5", "Concert / Party", "★ ROCK ON! ★ LET'S GO CRAZY ★ 🎸 ⚡", 0xFFD400FF, 8, "🎸"),
    PresetMessage("6", "Quiet Mode", "🤫 PLEASE KEEP SILENCE • EXAM IN PROGRESS 🤫", 0xFF00AAFF, 4, "🤫"),
    PresetMessage("7", "Happy Birthday", "🎂 HAPPY BIRTHDAY! WISHING YOU THE BEST! 🎉", 0xFFFF1493, 6, "🎂"),
    PresetMessage("8", "Victory", "🏆 VICTORY! WE ARE THE CHAMPIONS! 🏆", 0xFF39FF14, 7, "🏆")
)

data class LedDisplaySettings(
    val selectedColorHex: Long = 0xFFFF2222,
    val speed: Int = 5, // 1 to 10
    val dotDensity: Int = 16, // 10 (coarse) to 24 (fine dots)
    val glowIntensity: Float = 0.75f, // 0.0f to 1.0f
    val brightness: Float = 1.0f, // 0.3f to 1.5f
    val showScanlines: Boolean = true,
    val showPixelGrid: Boolean = true,
    val mirrorMode: Boolean = false,
    val isBlinking: Boolean = false,
    val soundReactive: Boolean = true,
    val rainbowMode: Boolean = false,
    val scrollDirection: ScrollDirection = ScrollDirection.RIGHT_TO_LEFT,
    val ledShape: LedShape = LedShape.CIRCLE,
    val languageCode: String = "en-IN",
    val letterSpacing: Float = 1.0f
)
