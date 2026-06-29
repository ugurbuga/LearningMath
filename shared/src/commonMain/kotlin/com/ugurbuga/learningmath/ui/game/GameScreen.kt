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
import com.ugurbuga.learningmath.ui.components.ConfettiType
import com.ugurbuga.learningmath.ui.components.CustomKeypad
import com.ugurbuga.learningmath.ui.theme.LearningMathTheme
import com.ugurbuga.learningmath.util.rememberTextToSpeechHelper
import com.ugurbuga.learningmath.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.getString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import com.ugurbuga.learningmath.ui.theme.*
import kotlin.random.Random

private const val SHOW_SOLUTION_DELAY_SECONDS = 15
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
    var hasHadError by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var confettiType by remember { mutableStateOf(ConfettiType.random()) }
    val scope = rememberCoroutineScope()
    val ttsHelper = rememberTextToSpeechHelper()

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

    val effectiveInputMode = if (operation == Operation.ADDITION || operation == Operation.SUBTRACTION) {
        InputMode.STEP_BY_STEP
    } else {
        inputMode
    }

    fun generateQuestion() {
        ttsHelper.stop()
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
        hasHadError = false
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
            ttsHelper.speak(text)
            delay(holdMs)
        }

        val n1Str = num1.toString().reversed()
        val n2Str = num2.toString().reversed()
        val displayLength = maxOf(num1.toString().length, num2.toString().length, answerLength)

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
                    val plusSeparator = getString(Res.string.plus_connector)
                    val joinString = steps.joinToString(plusSeparator)
                    addSolutionExplanation(getString(Res.string.solution_mul_step, joinString, currentSum))
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
                    val listSeparator = getString(Res.string.list_separator)
                    addSolutionExplanation(getString(Res.string.solution_div_counting, num2, steps.joinToString(listSeparator)))
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
        confettiType = ConfettiType.random()
        showConfetti = true
    }

    val shakeOffset = remember { Animatable(0f) }
    
    fun checkAnswer() {
        val processedInput = if (effectiveInputMode == InputMode.STEP_BY_STEP) {
            userInput.reversed()
        } else {
            userInput
        }

        if (processedInput.isNotEmpty() && processedInput.toInt() == answerValue) {
            isCorrect = true
            confettiType = ConfettiType.random()
            showConfetti = true
            onCorrectAnswer(operation)
        } else {
            isCorrect = false
            hasHadError = true
            scope.launch {
                repeat(3) {
                    shakeOffset.animateTo(20f, animationSpec = tween(50))
                    shakeOffset.animateTo(-20f, animationSpec = tween(50))
                }
                shakeOffset.animateTo(0f)
            }
        }
    }

    if (isShowingSolution) {
        GameSolutionContent(
            operation = operation,
            num1 = num1,
            num2 = num2,
            userInput = userInput,
            isSolutionFinished = isSolutionFinished,
            solutionStep = solutionStep,
            solutionExplanations = solutionExplanations,
            carries = carries,
            borrows = borrows,
            num1Overrides = num1Overrides,
            onBack = onBack,
            onReplaySolution = { scope.launch { showStepByStepSolution() } },
            onNextQuestion = { generateQuestion() }
        )
    } else {
        GamePlayContent(
            operation = operation,
            num1 = num1,
            num2 = num2,
            userInput = userInput,
            isCorrect = isCorrect,
            hasHadError = hasHadError,
            inputMode = effectiveInputMode,
            questionTimer = questionTimer,
            shakeOffset = shakeOffset.value,
            onBack = onBack,
            onShowSolution = { scope.launch { showStepByStepSolution() } },
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
            },
            onNextQuestion = { generateQuestion() }
        )
    }

    if (showConfetti) {
        ConfettiEffect(
            type = confettiType,
            onAnimationFinished = {
                showConfetti = false
                confettiType = ConfettiType.random()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePlayContent(
    operation: Operation,
    num1: Int,
    num2: Int,
    userInput: String,
    isCorrect: Boolean?,
    hasHadError: Boolean,
    inputMode: InputMode,
    questionTimer: Int,
    shakeOffset: Float,
    onBack: () -> Unit,
    onShowSolution: () -> Unit,
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onTickClick: () -> Unit,
    onNextQuestion: () -> Unit
) {
    val answerValue = when (operation) {
        Operation.ADDITION -> num1 + num2
        Operation.SUBTRACTION -> num1 - num2
        Operation.MULTIPLICATION -> num1 * num2
        Operation.DIVISION -> num1 / num2
    }
    val answerLength = answerValue.toString().length
    val displayLength = maxOf(num1.toString().length, num2.toString().length, answerLength)
    val activeColumnIndex = if (isCorrect != null || inputMode == InputMode.DIRECT) -1 else userInput.length

    Scaffold(
        topBar = { GameTopBar(operation, onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    MathOperationLayout(
                        operation = operation,
                        num1 = num1,
                        num2 = num2,
                        userInput = userInput,
                        isCorrect = isCorrect,
                        displayLength = displayLength,
                        answerLength = answerLength,
                        activeColumnIndex = activeColumnIndex,
                        carries = emptyMap(),
                        borrows = emptyMap(),
                        num1Overrides = emptyMap(),
                        inputMode = inputMode,
                        shakeOffset = shakeOffset,
                        isFinished = isCorrect == true
                    )

                    FeedbackMessage(isCorrect = isCorrect)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (questionTimer >= SHOW_SOLUTION_DELAY_SECONDS && hasHadError && isCorrect != true) {
                    ShowSolutionButton(onClick = onShowSolution)
                }

                CustomKeypad(onNumberClick = onNumberClick, onDeleteClick = onDeleteClick, onTickClick = onTickClick)

                if (isCorrect == true) {
                    NextQuestionButton(onClick = onNextQuestion)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSolutionContent(
    operation: Operation,
    num1: Int,
    num2: Int,
    userInput: String,
    isSolutionFinished: Boolean,
    solutionStep: Int,
    solutionExplanations: List<String>,
    carries: Map<Int, Int>,
    borrows: Map<Int, Boolean>,
    num1Overrides: Map<Int, String>,
    onBack: () -> Unit,
    onReplaySolution: () -> Unit,
    onNextQuestion: () -> Unit
) {
    val answerValue = when (operation) {
        Operation.ADDITION -> num1 + num2
        Operation.SUBTRACTION -> num1 - num2
        Operation.MULTIPLICATION -> num1 * num2
        Operation.DIVISION -> num1 / num2
    }
    val answerLength = answerValue.toString().length
    val displayLength = maxOf(num1.toString().length, num2.toString().length, answerLength)

    Scaffold(
        topBar = { GameTopBar(operation, onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    SolutionExplanationCard(
                        operation = operation,
                        explanations = solutionExplanations
                    )

                    MathOperationLayout(
                        operation = operation,
                        num1 = num1,
                        num2 = num2,
                        userInput = userInput,
                        isCorrect = true,
                        displayLength = displayLength,
                        answerLength = answerLength,
                        activeColumnIndex = solutionStep,
                        carries = carries,
                        borrows = borrows,
                        num1Overrides = num1Overrides,
                        inputMode = InputMode.STEP_BY_STEP,
                        isFinished = isSolutionFinished
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isSolutionFinished) {
                    ReplaySolutionButton(onClick = onReplaySolution)
                    NextQuestionButton(onClick = onNextQuestion)
                }
            }
        }
    }
}

@Composable
fun FeedbackMessage(isCorrect: Boolean?) {
    Box(modifier = Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
        if (isCorrect != null) {
            Text(
                text = if (isCorrect == true) stringResource(Res.string.correct_msg) else stringResource(Res.string.wrong_msg),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCorrect == true) ColorCorrect else ColorWrong
            )
        }
    }
}

@Composable
fun SolutionExplanationCard(
    operation: Operation,
    explanations: List<String>
) {
    val scrollState = rememberScrollState()
    
    // Scroll to the bottom every time a new explanation is added
    LaunchedEffect(explanations.size) {
        if (explanations.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    val finishedMsg = stringResource(Res.string.solution_finished)

    Card(
        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = operation.color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(2.dp, operation.color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            explanations.forEachIndexed { index, line ->
                val isLast = index == explanations.size - 1
                val isSuccess = line == finishedMsg
                Text(
                    text = line,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isLast || isSuccess) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSuccess) ColorCorrect else if (isLast) operation.color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ShowSolutionButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
    ) {
        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(Res.string.show_solution), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ReplaySolutionButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
    ) {
        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(Res.string.replay), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NextQuestionButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(56.dp).fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ColorCorrect)
    ) {
        Text(stringResource(Res.string.next_question), fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTopBar(operation: Operation, onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(operation.titleRes), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = operation.color))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = operation.symbol, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = operation.color))
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back), tint = operation.color)
            }
        }
    )
}

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

@Preview @Composable fun GameScreenAdditionPreview() { LearningMathTheme { GameScreen(Operation.ADDITION, {}, {}, InputMode.DIRECT, {}) } }
@Preview @Composable fun GameScreenSubtractionPreview() { LearningMathTheme { GameScreen(Operation.SUBTRACTION, {}, {}, InputMode.DIRECT, {}) } }
@Preview @Composable fun GameScreenMultiplicationPreview() { LearningMathTheme { GameScreen(Operation.MULTIPLICATION, {}, {}, InputMode.DIRECT, {}) } }
@Preview @Composable fun GameScreenDivisionPreview() { LearningMathTheme { GameScreen(Operation.DIVISION, {}, {}, InputMode.DIRECT, {}) } }

@Preview
@Composable
fun GameScreenAdditionSolutionPreview() {
    LearningMathTheme {
        GameSolutionContent(
            operation = Operation.ADDITION, num1 = 45, num2 = 27, userInput = "27",
            isSolutionFinished = true, solutionStep = 1,
            solutionExplanations = listOf(
                "5 + 7 = 12.",
                "We write 2 of 12, there is 1 carry.",
                "We add the carry of 1 to the tens.",
                "4 + 2 = 6, with 1 carry, total is 7.",
                stringResource(Res.string.solution_finished)
            ),
            carries = mapOf(1 to 1), borrows = emptyMap(), num1Overrides = emptyMap(),
            onBack = {}, onReplaySolution = {}, onNextQuestion = {}
        )
    }
}

@Preview
@Composable
fun GameScreenSubtractionSolutionPreview() {
    LearningMathTheme {
        GameSolutionContent(
            operation = Operation.SUBTRACTION, num1 = 52, num2 = 18, userInput = "43",
            isSolutionFinished = true, solutionStep = 1,
            solutionExplanations = listOf(
                "Cannot subtract 8 from 2, we borrow a ten from the neighbor.",
                "The neighbor had 5, now 4 is left.",
                "Our 2 becomes 12.",
                "12 - 8 = 4.",
                "4 - 1 = 3.",
                stringResource(Res.string.solution_finished)
            ),
            carries = emptyMap(), borrows = mapOf(1 to true), num1Overrides = mapOf(0 to "12", 1 to "4"),
            onBack = {}, onReplaySolution = {}, onNextQuestion = {}
        )
    }
}

@Preview
@Composable
fun GameScreenMultiplicationSolutionPreview() {
    LearningMathTheme {
        GameSolutionContent(
            operation = Operation.MULTIPLICATION, num1 = 4, num2 = 3, userInput = "21",
            isSolutionFinished = true, solutionStep = 0,
            solutionExplanations = listOf(
                "We are adding 4 threes.",
                "➜ 3 = 3",
                "➜ 3 + 3 = 6",
                "➜ 3 + 3 + 3 = 9",
                "➜ 3 + 3 + 3 + 3 = 12",
                stringResource(Res.string.solution_finished)
            ),
            carries = emptyMap(), borrows = emptyMap(), num1Overrides = emptyMap(),
            onBack = {}, onReplaySolution = {}, onNextQuestion = {}
        )
    }
}

@Preview
@Composable
fun GameScreenDivisionSolutionPreview() {
    LearningMathTheme {
        GameSolutionContent(
            operation = Operation.DIVISION, num1 = 12, num2 = 3, userInput = "4",
            isSolutionFinished = true, solutionStep = 0,
            solutionExplanations = listOf(
                "Let's count how many 3s are in 12.",
                "3, 6, 9, 12",
                "We counted exactly 4.",
                stringResource(Res.string.solution_finished)
            ),
            carries = emptyMap(), borrows = emptyMap(), num1Overrides = emptyMap(),
            onBack = {}, onReplaySolution = {}, onNextQuestion = {}
        )
    }
}
