package com.ugurbuga.learningmath.model

import androidx.compose.ui.graphics.Color
import com.ugurbuga.learningmath.res.Strings
import kotlinx.serialization.Serializable

@Serializable
enum class Operation(val title: String, val symbol: String, val color: Color) {
    ADDITION(Strings.OP_ADDITION, "+", Color(0xFFFFB74D)),
    SUBTRACTION(Strings.OP_SUBTRACTION, "-", Color(0xFF81C784)),
    MULTIPLICATION(Strings.OP_MULTIPLICATION, "×", Color(0xFF64B5F6)),
    DIVISION(Strings.OP_DIVISION, "÷", Color(0xFFBA68C8))
}
