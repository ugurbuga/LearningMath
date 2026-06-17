package com.ugurbuga.learningmath.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.ugurbuga.learningmath.shared.R

@Composable
actual fun fredokaFontFamily(): FontFamily = remember {
    FontFamily(
        Font(R.font.fredoka_light, FontWeight.Light),
        Font(R.font.fredoka_regular, FontWeight.Normal),
        Font(R.font.fredoka_medium, FontWeight.Medium),
        Font(R.font.fredoka_semibold, FontWeight.SemiBold),
        Font(R.font.fredoka_bold, FontWeight.Bold)
    )
}

