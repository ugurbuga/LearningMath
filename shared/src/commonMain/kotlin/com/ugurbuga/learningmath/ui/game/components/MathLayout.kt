package com.ugurbuga.learningmath.ui.game.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ugurbuga.learningmath.model.InputMode
import com.ugurbuga.learningmath.model.Operation
import com.ugurbuga.learningmath.shared.generated.resources.*
import com.ugurbuga.learningmath.ui.theme.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun MathOperationLayout(
    operation: Operation,
    num1: Int,
    num2: Int,
    userInput: String,
    isCorrect: Boolean?,
    displayLength: Int,
    answerLength: Int,
    activeColumnIndex: Int,
    carries: Map<Int, Int>,
    borrows: Map<Int, Boolean>,
    num1Overrides: Map<Int, String>,
    inputMode: InputMode,
    shakeOffset: Float = 0f,
    isFinished: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(x = shakeOffset.dp)) {
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(IntrinsicSize.Min)) {
            CarryBorrowIndicators(
                displayLength = displayLength,
                carries = carries,
                borrows = borrows
            )

            MathOperationBody(
                operation = operation,
                num1 = num1.toString(),
                num2 = num2.toString(),
                displayLength = displayLength,
                activeColumnIndex = activeColumnIndex,
                num1Overrides = num1Overrides
            )
            
            AnswerInputArea(
                userInput = userInput,
                maxLength = displayLength,
                actualAnswerLength = answerLength,
                isCorrect = isCorrect,
                operationColor = operation.color,
                inputMode = inputMode,
                activeColumnIndex = activeColumnIndex,
                isFinished = isFinished
            )
        }
    }
}

@Composable
fun CarryBorrowIndicators(
    displayLength: Int,
    carries: Map<Int, Int>,
    borrows: Map<Int, Boolean>
) {
    Row(horizontalArrangement = Arrangement.End) {
        Spacer(modifier = Modifier.width(40.dp))
        repeat(displayLength) { index ->
            val indexFromRight = displayLength - 1 - index
            Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.BottomCenter) {
                if (carries.containsKey(indexFromRight)) {
                    Surface(
                        modifier = Modifier.padding(bottom = 4.dp),
                        shape = RoundedCornerShape(50),
                        color = ColorCarryBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ColorCarryBorder)
                    ) {
                        Text(
                            text = carries[indexFromRight].toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ColorCarryText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (borrows.containsKey(indexFromRight)) {
                    Text(text = stringResource(Res.string.borrow_symbol), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ColorBorrow)
                }
            }
        }
    }
}

@Composable
fun MathOperationBody(
    operation: Operation,
    num1: String,
    num2: String,
    displayLength: Int,
    activeColumnIndex: Int,
    num1Overrides: Map<Int, String> = emptyMap()
) {
    Column(horizontalAlignment = Alignment.End) {
        DigitDisplayRow(
            number = num1,
            maxLength = displayLength,
            activeDigitIndexFromRight = activeColumnIndex,
            highlightColor = operation.color,
            digitOverrides = num1Overrides
        )

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width((displayLength * 60 + 40).dp)) {
            Text(
                text = operation.symbol,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = operation.color,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center
            )
            DigitDisplayRow(
                number = num2,
                maxLength = displayLength,
                activeDigitIndexFromRight = activeColumnIndex,
                highlightColor = operation.color
            )
        }
        
        HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), thickness = 4.dp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun DigitDisplayRow(
    number: String,
    maxLength: Int,
    activeDigitIndexFromRight: Int = -1,
    highlightColor: Color = Color.Unspecified,
    digitOverrides: Map<Int, String> = emptyMap()
) {
    Row(horizontalArrangement = Arrangement.End) {
        val padded = number.padStart(maxLength, ' ')
        padded.forEachIndexed { index, char ->
            val indexFromRight = maxLength - 1 - index
            val isActive = indexFromRight == activeDigitIndexFromRight
            
            Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                if (char != ' ') {
                    val displayChar = digitOverrides[indexFromRight] ?: char.toString()
                    Text(
                        text = displayChar,
                        fontSize = if (displayChar.length > 1) 32.sp else 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive && highlightColor != Color.Unspecified) highlightColor else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun AnswerInputArea(
    userInput: String,
    maxLength: Int,
    actualAnswerLength: Int,
    isCorrect: Boolean?,
    operationColor: Color,
    inputMode: InputMode,
    activeColumnIndex: Int = -1,
    isFinished: Boolean = false
) {
    val alpha by rememberInfiniteTransition().animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(600, easing = LinearEasing), repeatMode = RepeatMode.Reverse)
    )

    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.padding(top = 8.dp)) {
        repeat(maxLength) { index ->
            val isPartOfAnswer = index >= (maxLength - actualAnswerLength)
            if (isPartOfAnswer) {
                val answerIndexFromRight = maxLength - 1 - index
                val charIndex = if (inputMode == InputMode.DIRECT) index - (maxLength - actualAnswerLength) else answerIndexFromRight
                val char = if (charIndex >= 0 && charIndex < userInput.length) userInput[charIndex].toString() else ""
                
                val isCurrent = if (activeColumnIndex != -1) {
                    answerIndexFromRight == activeColumnIndex && !isFinished
                } else {
                    (if (inputMode == InputMode.DIRECT) index - (maxLength - actualAnswerLength) else answerIndexFromRight) == userInput.length && isCorrect == null
                }

                val boxBorderColor = when {
                    isFinished -> ColorCorrect
                    isCorrect == false -> ColorWrong
                    isCurrent -> operationColor.copy(alpha = alpha)
                    char.isNotEmpty() -> operationColor
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                }

                val textColor = when {
                    isFinished -> ColorCorrect
                    isCorrect == false -> ColorWrong
                    isCurrent -> operationColor.copy(alpha = alpha)
                    else -> operationColor
                }

                Box(
                    modifier = Modifier.size(60.dp).padding(2.dp)
                        .background(
                            color = if (isCurrent) operationColor.copy(alpha = 0.1f * alpha) else Color.Transparent, 
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(width = 2.dp, color = boxBorderColor, shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = char, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                }
            } else {
                Spacer(modifier = Modifier.size(60.dp))
            }
        }
    }
}
