package com.ugurbuga.learningmath.util

import androidx.compose.runtime.Composable

expect class TextToSpeechHelper {
    fun speak(text: String)
    fun stop()
}

@Composable
expect fun rememberTextToSpeechHelper(): TextToSpeechHelper
