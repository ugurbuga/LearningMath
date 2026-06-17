package com.ugurbuga.learningmath

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.ugurbuga.learningmath.shared.generated.resources.Res
import com.ugurbuga.learningmath.shared.generated.resources.app_icon
import org.jetbrains.compose.resources.painterResource
import java.io.File
import java.util.Properties

fun main() = application {
    val configFile = File(System.getProperty("user.home"), ".learningmath_config")
    val props = Properties()
    if (configFile.exists()) {
        configFile.inputStream().use { props.load(it) }
    }

    val savedWidth = props.getProperty("window_width")?.toFloatOrNull() ?: 450f
    val savedHeight = props.getProperty("window_height")?.toFloatOrNull() ?: 800f
    val savedX = props.getProperty("window_x")?.toFloatOrNull()
    val savedY = props.getProperty("window_y")?.toFloatOrNull()

    val initialPosition = if (savedX != null && savedY != null) {
        WindowPosition(savedX.dp, savedY.dp)
    } else {
        WindowPosition(Alignment.Center)
    }

    val windowState = rememberWindowState(
        size = DpSize(savedWidth.dp, savedHeight.dp),
        position = initialPosition
    )
    
    // Aspect Ratio 9:16
    val aspectRatio = 9f / 16f
    var lastSize by remember { mutableStateOf(windowState.size) }
    var lastPosition by remember { mutableStateOf(windowState.position) }

    // Track Size and Position Changes
    LaunchedEffect(windowState.size, windowState.position) {
        var changed = false
        
        // Handle Aspect Ratio and Size Save
        if (windowState.size != lastSize) {
            val newSize = windowState.size
            val widthChanged = newSize.width != lastSize.width
            val heightChanged = newSize.height != lastSize.height
            
            if (widthChanged) {
                val newHeight = (newSize.width.value / aspectRatio).dp
                windowState.size = DpSize(newSize.width, newHeight)
            } else if (heightChanged) {
                val newWidth = (newSize.height.value * aspectRatio).dp
                windowState.size = DpSize(newWidth, newSize.height)
            }
            lastSize = windowState.size
            props.setProperty("window_width", windowState.size.width.value.toString())
            props.setProperty("window_height", windowState.size.height.value.toString())
            changed = true
        }

        // Handle Position Save
        if (windowState.position != lastPosition) {
            val newPos = windowState.position
            if (newPos is WindowPosition.Absolute) {
                props.setProperty("window_x", newPos.x.value.toString())
                props.setProperty("window_y", newPos.y.value.toString())
                changed = true
            }
            lastPosition = newPos
        }

        if (changed) {
            configFile.outputStream().use { props.store(it, null) }
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Matematik Öğreniyorum",
        state = windowState,
        icon = painterResource(Res.drawable.app_icon),
        resizable = true
    ) {
        LearningMathApp()
    }
}
