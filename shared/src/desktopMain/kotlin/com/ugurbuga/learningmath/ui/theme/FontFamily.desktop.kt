package com.ugurbuga.learningmath.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.ugurbuga.learningmath.shared.generated.resources.Res
import com.ugurbuga.learningmath.shared.generated.resources.fredoka_bold
import com.ugurbuga.learningmath.shared.generated.resources.fredoka_light
import com.ugurbuga.learningmath.shared.generated.resources.fredoka_medium
import com.ugurbuga.learningmath.shared.generated.resources.fredoka_regular
import com.ugurbuga.learningmath.shared.generated.resources.fredoka_semibold
import org.jetbrains.compose.resources.Font

@Composable
actual fun fredokaFontFamily(): FontFamily {
    val light = Font(Res.font.fredoka_light, FontWeight.Light)
    val regular = Font(Res.font.fredoka_regular, FontWeight.Normal)
    val medium = Font(Res.font.fredoka_medium, FontWeight.Medium)
    val semiBold = Font(Res.font.fredoka_semibold, FontWeight.SemiBold)
    val bold = Font(Res.font.fredoka_bold, FontWeight.Bold)

    return remember(light, regular, medium, semiBold, bold) {
        FontFamily(light, regular, medium, semiBold, bold)
    }
}

