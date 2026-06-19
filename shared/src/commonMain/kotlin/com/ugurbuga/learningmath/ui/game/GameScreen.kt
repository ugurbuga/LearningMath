package com.ugurbuga.learningmath.ui.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
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
import com.ugurbuga.learningmath.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.getString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import kotlin.random.Random

// TODO: Set this to 30 for release
private const val SHOW_SOLUTION_DELAY_SECONDS = 3
private const val SOLUTION_STEP_DELAY_MS = 3000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    operation: Operation,
    onBack: () -> Unit,
    onCorrectAnswer: (Operation) -> Unit,
    inputMode: InputMode,
    onInputModeChange: (InputMode) -> Unit
) {
    val initialNumbers = remember(operation) {
        when (operation) {
            Operation.ADDITION -> {
                val a = Random.nextInt(1, 50)
                val b = Random.nextInt(1, 50)
                maxOf(a, b) to minOf(a, b)
            }
            Operation.SUBTRACTION -> {
                val a = Random.nextInt(1, 99)
                var b = Random.nextInt(1, a + 1)
                if (a == b && a > 1) b -= 1
                maxOf(a, b) to minOf(a, b)
            }
            Operation.MULTIPLICATION -> {
                val a = Random.nextInt(1, 10)
                val b = Random.nextInt(1, 10)
                maxOf(a, b) to minOf(a, b)
            }
            Operation.DIVISION -> {
                val n2 = Random.nextInt(1, 6)
                val factor = Random.nextInt(1, 5)
                (n2 * factor) to n2
            }
        }
    }

    var num1 by remember(operation) { mutableIntStateOf(initialNumbers.first) }
    var num2 by remember(operation) { mutableIntStateOf(initialNumbers.second) }
    var userInput by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }
    var showConfetti by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var questionTimer by remember { mutableIntStateOf(0) }
    var isShowingSolution by remember { mutableStateOf(false) }
    var solutionStep by remember { mutableIntStateOf(0) }
    val solutionExplanations = remember { mutableStateListOf<String>() }
    var isSolutionFinished by remember { mutableStateOf(false) }
    val carries = remember { mutableStateMapOf<Int, Int>() }
    val borrows = remember { mutableStateMapOf<Int, Boolean>() }
    val num1Overrides = remember { mutableStateMapOf<Int, String>() }

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
                if (num1 == num2 && num1 > 1) num2 -= 1
            }
            Operation.MULTIPLICATION -> {
                num1 = Random.nextInt(1, 10)
                num2 = Random.nextInt(1, 10)
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
        isSolutionFinished = false
        solutionStep = 0
        solutionExplanations.clear()
        carries.clear()
        borrows.clear()
        num1Overrides.clear()
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
        isSolutionFinished = false
        userInput = ""
        solutionStep = 0
        solutionExplanations.clear()
        carries.clear()
        borrows.clear()
        num1Overrides.clear()

        suspend fun addSolutionExplanation(text: String, holdMs: Long = SOLUTION_STEP_DELAY_MS) {
            solutionExplanations.add(text)
            delay(holdMs)
        }

        val n1Str = num1.toString().reversed()
        val n2Str = num2.toString().reversed()

        when (operation) {
            Operation.ADDITION -> {
                var carry = 0
                for (i in 0 until displayLength) {
                    solutionStep = i
                    val d1 = n1Str.getOrNull(i)?.digitToInt() ?: 0
                    val d2 = n2Str.getOrNull(i)?.digitToInt() ?: 0
                    
                    val stepSum = d1 + d2
                    addSolutionExplanation(getString(Res.string.solution_step_add, d1, d2, stepSum))

                    val totalSum = stepSum + carry
                    if (carry > 0) {
                        addSolutionExplanation(getString(Res.string.solution_carry_add, stepSum, carry, totalSum))
                    }
                    
                    val digit = totalSum % 10
                    userInput += digit.toString()
                    addSolutionExplanation(getString(Res.string.solution_write_number, digit))

                    carry = totalSum / 10
                    if (carry > 0) {
                        if (i + 1 < displayLength) {
                            carries[i + 1] = carry
                            addSolutionExplanation(getString(Res.string.solution_carry_next, carry))
                        } else {
                            userInput += carry.toString()
                            addSolutionExplanation(getString(Res.string.solution_carry_final, carry))
                        }
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
                        addSolutionExplanation(getString(Res.string.solution_sub_not_enough, d1, d2))
                        borrows[i + 1] = true
                        
                        val neighborOriginal = n1Digits.getOrNull(i + 1) ?: 0
                        if (i + 1 < n1Digits.size) {
                            n1Digits[i+1] -= 1
                            num1Overrides[i+1] = n1Digits[i+1].toString()
                        }
                        addSolutionExplanation(getString(Res.string.solution_borrow_neighbor, neighborOriginal, n1Digits.getOrElse(i + 1) { 0 }))
                        
                        val oldD1 = d1
                        d1 += 10
                        num1Overrides[i] = d1.toString()
                        addSolutionExplanation(getString(Res.string.solution_borrow_add, oldD1, d1))
                    }
                    
                    val diff = d1 - d2
                    addSolutionExplanation(getString(Res.string.solution_step_sub, d1, d2, diff))
                    userInput += diff.toString()
                    addSolutionExplanation(getString(Res.string.solution_write_number, diff))
                }
                while (userInput.length > 1 && userInput.endsWith("0")) {
                    userInput = userInput.dropLast(1)
                }
            }
            Operation.MULTIPLICATION -> {
                addSolutionExplanation(getString(Res.string.solution_mul_explanation, num1, num2, num1, num2))
                var currentSum = 0
                val steps = mutableListOf<String>()
                repeat(num1) {
                    currentSum += num2
                    steps.add(num2.toString())
                    addSolutionExplanation("➜ " + steps.joinToString(" + ") + " = $currentSum")
                    userInput = currentSum.toString().reversed()
                }
            }
            Operation.DIVISION -> {
                addSolutionExplanation(getString(Res.string.solution_div_explanation, num1, num2, num1, num2))
                var count = 0
                var remaining = num1
                val steps = mutableListOf<Int>()
                while (remaining >= num2) {
                    count++
                    steps.add(num2 * count)
                    addSolutionExplanation(getString(Res.string.solution_div_counting, num2, steps.joinToString(", ")))
                    userInput = count.toString().reversed()
                    remaining -= num2
                }
                addSolutionExplanation(getString(Res.string.solution_div_result, count))
            }
        }
        
        delay(1000)
        addSolutionExplanation(getString(Res.string.solution_finished))
        isSolutionFinished = true
        isCorrect = true
        showConfetti = true
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
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(operation.titleRes),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = operation.color
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = operation.symbol,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = operation.color
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.back),
                                tint = operation.color
                            )
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
                                        text = stringResource(mode.titleRes),
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
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Solution Explanation (Moved here, just above the operation)
                        AnimatedVisibility(
                            visible = isShowingSolution,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            val scrollState = rememberScrollState()
                            LaunchedEffect(solutionExplanations.size, scrollState.maxValue) {
                                scrollState.scrollTo(scrollState.maxValue)
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = operation.color.copy(alpha = 0.1f),
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(2.dp, operation.color.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .verticalScroll(scrollState)
                                        .animateContentSize()
                                ) {
                                    solutionExplanations.forEachIndexed { index, line ->
                                        val isLast = index == solutionExplanations.size - 1
                                        val isSuccess = line.contains("Harikasın")
                                        val textColor = when {
                                            isSuccess -> Color(0xFF4CAF50)
                                            isLast -> operation.color
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                        }
                                        Text(
                                            text = line,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = if (isLast || isSuccess) FontWeight.Bold else FontWeight.Medium,
                                                color = textColor
                                            ),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.width(IntrinsicSize.Min)
                        ) {
                            // Carries/Borrows indicators
                            Row(horizontalArrangement = Arrangement.End) {
                                Spacer(modifier = Modifier.width(40.dp))
                                repeat(displayLength) { index ->
                                    val indexFromRight = displayLength - 1 - index
                                    Box(
                                        modifier = Modifier.size(60.dp),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        AnimatedContent(
                                            targetState = carries.containsKey(indexFromRight) to (carries[indexFromRight] ?: 0),
                                            transitionSpec = {
                                                (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
                                            },
                                            label = "carry"
                                        ) { (hasCarry, carryValue) ->
                                            if (hasCarry) {
                                                Surface(
                                                    modifier = Modifier.padding(bottom = 4.dp),
                                                    shape = RoundedCornerShape(50),
                                                    color = Color(0xFFFFEB3B),
                                                    tonalElevation = 4.dp,
                                                    shadowElevation = 2.dp,
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFBC02D))
                                                ) {
                                                    Text(
                                                        text = carryValue.toString(),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFFD32F2F),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.size(1.dp))
                                            }
                                        }
                                        AnimatedContent(
                                            targetState = borrows.containsKey(indexFromRight),
                                            transitionSpec = {
                                                (slideInVertically { -it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                                            },
                                            label = "borrow"
                                        ) { hasBorrow ->
                                            if (hasBorrow) {
                                                Text(
                                                    text = "↘",
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Red
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.size(1.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            // Number 1
                            DigitDisplayRow(
                                number = num1.toString(),
                                maxLength = displayLength,
                                activeDigitIndexFromRight = if (isShowingSolution) solutionStep else activeColumnIndex,
                                highlightColor = operation.color,
                                digitOverrides = num1Overrides
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
                            text = if (isCorrect == true) stringResource(Res.string.correct_msg) else stringResource(Res.string.wrong_msg),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect == true) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }

                // Buttons and Keypad Area
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Step-by-Step Solution Button
                    if ((questionTimer >= SHOW_SOLUTION_DELAY_SECONDS || isCorrect == false) && isCorrect != true && !isShowingSolution) {
                        Button(
                            onClick = {
                                isShowingSolution = true
                                scope.launch {
                                    showStepByStepSolution()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(Res.string.show_solution), fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isShowingSolution && isSolutionFinished) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    showStepByStepSolution()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.secondary
                            ),
                            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(Res.string.replay), fontWeight = FontWeight.Bold)
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

                    if (isCorrect == true && (!isShowingSolution || isSolutionFinished)) {
                        Button(
                            onClick = { generateQuestion() },
                            modifier = Modifier
                                .height(56.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text(stringResource(Res.string.next_question), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
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
    highlightColor: Color = Color.Unspecified,
    digitOverrides: Map<Int, String> = emptyMap()
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
                    val displayChar = digitOverrides[indexFromRight] ?: char.toString()
                    Text(
                        text = displayChar,
                        fontSize = if (displayChar.length > 1) 32.sp else 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) highlightColor else MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (digitOverrides.containsKey(indexFromRight) && displayChar != char.toString()) {
                        // Draw a strike-through or similar to show original value was changed
                        // For now just showing the new value is a good start.
                    }
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
