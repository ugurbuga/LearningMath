package com.ugurbuga.learningmath.ui.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ugurbuga.learningmath.model.Operation
import com.ugurbuga.learningmath.model.InputMode
import com.ugurbuga.learningmath.ui.components.ConfettiEffect
import com.ugurbuga.learningmath.ui.components.CustomKeypad
import com.ugurbuga.learningmath.ui.theme.LearningMathTheme
import com.ugurbuga.learningmath.res.Strings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    operation: Operation,
    onBack: () -> Unit,
    onCorrectAnswer: (Operation) -> Unit,
    inputMode: InputMode,
    onInputModeChange: (InputMode) -> Unit
) {
    var num1 by remember { 
        mutableIntStateOf(when(operation) {
            Operation.SUBTRACTION -> 10
            Operation.DIVISION -> 4
            else -> 5
        })
    }
    var num2 by remember { 
        mutableIntStateOf(when(operation) {
            Operation.SUBTRACTION -> 5
            Operation.DIVISION -> 2
            else -> 3
        })
    }
    var userInput by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }
    var showConfetti by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var questionTimer by remember { mutableIntStateOf(0) }
    var isShowingSolution by remember { mutableStateOf(false) }
    var solutionStep by remember { mutableIntStateOf(0) }
    var solutionExplanation by remember { mutableStateOf("") }
    val carries = remember { mutableStateMapOf<Int, Int>() }
    val borrows = remember { mutableStateMapOf<Int, Boolean>() }

    val answerValue = remember(num1, num2, operation) {
        when (operation) {
            Operation.ADDITION -> num1 + num2
            Operation.SUBTRACTION -> num1 - num2
            Operation.MULTIPLICATION -> num1 * num2
            Operation.DIVISION -> num1 / num2
        }
    }
    val answerLength = answerValue.toString().length
    val displayLength = maxOf(num1.toString().length, num2.toString().length, answerLength)

    fun generateQuestion() {
        when (operation) {
            Operation.ADDITION -> {
                val a = Random.nextInt(1, 50)
                val b = Random.nextInt(1, 50)
                num1 = maxOf(a, b)
                num2 = minOf(a, b)
            }
            Operation.SUBTRACTION -> {
                val a = Random.nextInt(1, 99)
                val b = Random.nextInt(1, a + 1)
                num1 = maxOf(a, b)
                num2 = minOf(a, b)
                // Ensure num1 and num2 are not the same for 1st grade challenge if needed, 
                // but usually num1 > num2 is better.
                if (num1 == num2 && num1 > 1) num2 -= 1
            }
            Operation.MULTIPLICATION -> {
                num1 = Random.nextInt(1, 10)
                num2 = Random.nextInt(1, 10)
                // Larger number on top
                if (num2 > num1) {
                    val temp = num1
                    num1 = num2
                    num2 = temp
                }
            }
            Operation.DIVISION -> {
                num2 = Random.nextInt(1, 6)
                val factor = Random.nextInt(1, 5)
                num1 = num2 * factor
            }
        }
        userInput = ""
        isCorrect = null
        showConfetti = false
        questionTimer = 0
        isShowingSolution = false
        solutionStep = 0
        solutionExplanation = ""
        carries.clear()
        borrows.clear()
    }

    LaunchedEffect(num1, num2) {
        questionTimer = 0
        while (isCorrect != true && !isShowingSolution) {
            delay(1000)
            questionTimer++
        }
    }

    suspend fun showStepByStepSolution() {
        isShowingSolution = true
        userInput = ""
        solutionStep = 0
        carries.clear()
        borrows.clear()

        val n1Str = num1.toString().reversed()
        val n2Str = num2.toString().reversed()

        when (operation) {
            Operation.ADDITION -> {
                var carry = 0
                for (i in 0 until displayLength) {
                    solutionStep = i
                    val d1 = n1Str.getOrNull(i)?.digitToInt() ?: 0
                    val d2 = n2Str.getOrNull(i)?.digitToInt() ?: 0
                    val sum = d1 + d2 + carry
                    solutionExplanation = "$d1 + $d2" + (if (carry > 0) " + $carry (elde)" else "") + " = $sum"
                    delay(2500)
                    
                    val digit = sum % 10
                    userInput += digit.toString()
                    solutionExplanation += ". $digit sayısını kutuya yazıyoruz."
                    delay(2000)

                    carry = sum / 10
                    if (carry > 0 && i + 1 < displayLength) {
                        carries[i + 1] = carry
                        solutionExplanation += " Elde var $carry, yana aktarıyoruz."
                        delay(2000)
                    } else if (carry > 0 && i + 1 == displayLength) {
                        userInput += carry.toString()
                        solutionExplanation += " En sona eldeyi ($carry) ekliyoruz."
                        delay(2000)
                    }
                }
            }
            Operation.SUBTRACTION -> {
                val n1Digits = n1Str.map { it.digitToInt() }.toIntArray()
                for (i in 0 until displayLength) {
                    solutionStep = i
                    var d1 = n1Digits.getOrElse(i) { 0 }
                    val d2 = n2Str.getOrNull(i)?.digitToInt() ?: 0
                    
                    if (d1 < d2) {
                        solutionExplanation = "$d1'den $d2 çıkmaz. Komşudan bir onluk alıyoruz."
                        borrows[i + 1] = true
                        n1Digits[i+1] -= 1
                        d1 += 10
                        delay(2500)
                    }
                    
                    val diff = d1 - d2
                    solutionExplanation = "$d1 - $d2 = $diff"
                    delay(2500)
                    userInput += diff.toString()
                    delay(2000)
                }
                // Remove leading zeros from the final result if any
                // Since it's reversed, they are at the end
                while (userInput.length > 1 && userInput.endsWith("0")) {
                    userInput = userInput.dropLast(1)
                }
            }
            Operation.MULTIPLICATION -> {
                solutionExplanation = "$num1 x $num2 işlemi, $num1 tane $num2'yi toplamak demektir."
                delay(3000)
                var currentSum = 0
                val steps = mutableListOf<String>()
                for (i in 1..num1) {
                    currentSum += num2
                    steps.add(num2.toString())
                    solutionExplanation = steps.joinToString(" + ") + " = $currentSum"
                    if (i == num1) {
                        userInput = currentSum.toString()
                    }
                    delay(2000)
                }
            }
            Operation.DIVISION -> {
                solutionExplanation = "$num1 ÷ $num2 işlemi, $num1'in içinde kaç tane $num2 olduğunu bulmaktır."
                delay(3000)
                var count = 0
                var remaining = num1
                val steps = mutableListOf<Int>()
                while (remaining >= num2) {
                    count++
                    steps.add(num2 * count)
                    solutionExplanation = "$num2'şer sayalım: " + steps.joinToString(", ")
                    userInput = count.toString()
                    remaining -= num2
                    delay(2000)
                }
                solutionExplanation += ". Tam $count tane var!"
                delay(2000)
            }
        }
        
        isCorrect = true
        showConfetti = true
        // onCorrectAnswer(operation) // Don't count automated solutions
        solutionExplanation = "İşte bu kadar! Harikasın! ✨"
    }

    val activeColumnIndex = remember(userInput, inputMode, isCorrect) {
        if (isCorrect != null || inputMode == InputMode.DIRECT) -1
        else userInput.length
    }

    val shakeOffset = remember { Animatable(0f) }
    
    fun checkAnswer() {
        val processedInput = if (inputMode == InputMode.STEP_BY_STEP) {
            userInput.reversed()
        } else {
            userInput
        }

        if (processedInput.isNotEmpty() && processedInput.toInt() == answerValue) {
            isCorrect = true
            showConfetti = true
            onCorrectAnswer(operation)
        } else {
            isCorrect = false
            scope.launch {
                repeat(3) {
                    shakeOffset.animateTo(20f, animationSpec = tween(50))
                    shakeOffset.animateTo(-20f, animationSpec = tween(50))
                }
                shakeOffset.animateTo(0f)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(operation.title) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = Strings.BACK)
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Input Mode Selection (Only for Addition/Subtraction)
                if (operation == Operation.ADDITION || operation == Operation.SUBTRACTION) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            InputMode.entries.forEach { mode ->
                                val isSelected = mode == inputMode
                                Button(
                                    onClick = { onInputModeChange(mode) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) operation.color else Color.Transparent,
                                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(0.dp),
                                    elevation = null
                                ) {
                                    Text(
                                        text = if (mode == InputMode.DIRECT) Strings.DIRECT_MODE else Strings.STEP_BY_STEP_MODE,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Question Display
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .offset(x = shakeOffset.value.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.width(IntrinsicSize.Min)
                    ) {
                        // Carries/Borrows indicators
                        Row(horizontalArrangement = Arrangement.End) {
                            Spacer(modifier = Modifier.width(40.dp)) // For operation symbol alignment
                            repeat(displayLength) { index ->
                                val indexFromRight = displayLength - 1 - index
                                Box(
                                    modifier = Modifier.size(60.dp),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    if (carries.containsKey(indexFromRight)) {
                                        Text(
                                            text = carries[indexFromRight].toString(),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Red,
                                            modifier = Modifier
                                                .background(Color.Yellow, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp)
                                        )
                                    }
                                    if (borrows.containsKey(indexFromRight)) {
                                        Text(
                                            text = "↘",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Red
                                        )
                                    }
                                }
                            }
                        }

                        // Number 1
                        DigitDisplayRow(
                            number = num1.toString(),
                            maxLength = displayLength,
                            activeDigitIndexFromRight = if (isShowingSolution) solutionStep else activeColumnIndex,
                            highlightColor = operation.color
                        )

                        // Number 2 with Operation
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.width((displayLength * 60 + 40).dp)
                        ) {
                            Text(
                                text = operation.symbol,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = operation.color,
                                modifier = Modifier.width(40.dp),
                                textAlign = TextAlign.Center
                            )
                            DigitDisplayRow(
                                number = num2.toString(),
                                maxLength = displayLength,
                                activeDigitIndexFromRight = if (isShowingSolution) solutionStep else activeColumnIndex,
                                highlightColor = operation.color
                            )
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            thickness = 4.dp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Answer boxes
                        AnswerInputArea(
                            userInput = userInput,
                            maxLength = displayLength,
                            actualAnswerLength = answerLength,
                            isCorrect = isCorrect,
                            operationColor = operation.color,
                            inputMode = if (isShowingSolution) InputMode.STEP_BY_STEP else inputMode
                        )
                    }
                }

                // Solution Explanation
                if (isShowingSolution) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text(
                            text = solutionExplanation,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Message Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCorrect != null && !isShowingSolution) {
                        Text(
                            text = if (isCorrect == true) Strings.CORRECT_MSG else Strings.WRONG_MSG,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect == true) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }

                // Step-by-Step Solution Button
                AnimatedVisibility(
                    visible = questionTimer >= 5 && isCorrect != true && !isShowingSolution,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                showStepByStepSolution()
                            }
                        },
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("💡 Adım Adım Çözümü Göster")
                    }
                }

                AnimatedVisibility(
                    visible = isShowingSolution && solutionExplanation == "İşte bu kadar! Harikasın! ✨",
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                showStepByStepSolution()
                            }
                        },
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("🔄 Tekrar Oynat")
                    }
                }

                // Custom Keypad
                if (!isShowingSolution) {
                    CustomKeypad(
                        onNumberClick = { num ->
                            if (isCorrect != true && userInput.length < answerLength) {
                                userInput += num
                            }
                        },
                        onDeleteClick = {
                            if (isCorrect != true && userInput.isNotEmpty()) {
                                userInput = userInput.dropLast(1)
                                isCorrect = null
                            }
                        },
                        onTickClick = {
                            if (isCorrect != true) {
                                checkAnswer()
                            }
                        }
                    )
                }

                AnimatedVisibility(
                    visible = isCorrect == true && (!isShowingSolution || solutionExplanation == "İşte bu kadar! Harikasın! ✨"),
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    Button(
                        onClick = { generateQuestion() },
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .height(56.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text(Strings.NEXT_QUESTION, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showConfetti) {
            ConfettiEffect(onAnimationFinished = { showConfetti = false })
        }
    }
}

@Composable
fun DigitDisplayRow(
    number: String,
    maxLength: Int,
    activeDigitIndexFromRight: Int = -1,
    highlightColor: Color = Color.Unspecified
) {
    Row(horizontalArrangement = Arrangement.End) {
        val padded = number.padStart(maxLength, ' ')
        padded.forEachIndexed { index, char ->
            val indexFromRight = maxLength - 1 - index
            val isActive = indexFromRight == activeDigitIndexFromRight
            
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                if (char != ' ') {
                    Text(
                        text = char.toString(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) highlightColor else MaterialTheme.colorScheme.onSurface
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
    inputMode: InputMode
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        repeat(maxLength) { index ->
            val isPartOfAnswer = index >= (maxLength - actualAnswerLength)
            
            if (isPartOfAnswer) {
                val answerIndexFromRight = maxLength - 1 - index
                
                val charIndex = if (inputMode == InputMode.DIRECT) {
                    val leftMostAnswerIndex = maxLength - actualAnswerLength
                    index - leftMostAnswerIndex
                } else {
                    answerIndexFromRight
                }
                
                val char = if (charIndex >= 0 && charIndex < userInput.length) userInput[charIndex].toString() else ""
                
                val isCurrent = if (inputMode == InputMode.DIRECT) {
                    val currentInputIndex = index - (maxLength - actualAnswerLength)
                    currentInputIndex == userInput.length
                } else {
                    answerIndexFromRight == userInput.length
                } && isCorrect == null

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .padding(2.dp)
                        .background(
                            color = if (isCurrent) operationColor.copy(alpha = 0.1f * alpha) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = when {
                                isCorrect == true -> Color(0xFF4CAF50)
                                isCorrect == false -> Color(0xFFF44336)
                                isCurrent -> operationColor.copy(alpha = alpha)
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when (isCorrect) {
                            true -> Color(0xFF4CAF50)
                            false -> Color(0xFFF44336)
                            null -> operationColor
                        }
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(60.dp))
            }
        }
    }
}

@Preview
@Composable
fun GameScreenPreview() {
    LearningMathTheme {
        GameScreen(Operation.ADDITION, {}, {}, InputMode.DIRECT, {})
    }
}
