package com.ugurbuga.learningmath.util

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import java.util.Locale

actual class TextToSpeechHelper(private val tts: TextToSpeech?) {
    actual fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    actual fun stop() {
        tts?.stop()
    }
}

@Composable
actual fun rememberTextToSpeechHelper(): TextToSpeechHelper {
    if (LocalInspectionMode.current) {
        return remember { TextToSpeechHelper(null) }
    }
    val context = LocalContext.current
    val tts = remember {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale.getDefault()
            }
        }
        ttsInstance
    }

    DisposableEffect(tts) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    return remember(tts) { TextToSpeechHelper(tts) }
}
