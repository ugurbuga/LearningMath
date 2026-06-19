package com.ugurbuga.learningmath.model

import androidx.compose.ui.graphics.Color
import com.ugurbuga.learningmath.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import kotlinx.serialization.Serializable

@Serializable
enum class Operation(val titleRes: StringResource, val symbol: String, val color: Color) {
    ADDITION(Res.string.op_addition, "+", Color(0xFFFFB74D)),
    SUBTRACTION(Res.string.op_subtraction, "-", Color(0xFF81C784)),
    MULTIPLICATION(Res.string.op_multiplication, "×", Color(0xFF64B5F6)),
    DIVISION(Res.string.op_division, "÷", Color(0xFFBA68C8))
}
