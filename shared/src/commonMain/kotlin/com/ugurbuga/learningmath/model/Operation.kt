package com.ugurbuga.learningmath.model

import androidx.compose.ui.graphics.Color
import com.ugurbuga.learningmath.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import kotlinx.serialization.Serializable

import com.ugurbuga.learningmath.ui.theme.*

@Serializable
enum class Operation(val titleRes: StringResource, val symbol: String, val color: Color) {
    ADDITION(Res.string.op_addition, "+", ColorAddition),
    SUBTRACTION(Res.string.op_subtraction, "-", ColorSubtraction),
    MULTIPLICATION(Res.string.op_multiplication, "×", ColorMultiplication),
    DIVISION(Res.string.op_division, "÷", ColorDivision)
}
