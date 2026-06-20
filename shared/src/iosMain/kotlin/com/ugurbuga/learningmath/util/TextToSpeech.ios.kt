package com.ugurbuga.learningmath.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class TextToSpeechHelper {
    actual fun speak(text: String) {}
    actual fun stop() {}
}

@Composable
actual fun rememberTextToSpeechHelper(): TextToSpeechHelper = remember { TextToSpeechHelper() }
