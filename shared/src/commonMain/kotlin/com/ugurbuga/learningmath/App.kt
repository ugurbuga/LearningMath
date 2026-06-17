package com.ugurbuga.learningmath

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.ugurbuga.learningmath.model.Operation
import com.ugurbuga.learningmath.model.Screen
import com.ugurbuga.learningmath.model.InputMode
import com.ugurbuga.learningmath.ui.game.GameScreen
import com.ugurbuga.learningmath.ui.selection.SelectionScreen
import com.ugurbuga.learningmath.ui.stats.StatsDetailScreen
import com.ugurbuga.learningmath.ui.theme.LearningMathTheme
import com.ugurbuga.learningmath.util.getCurrentDate
import com.ugurbuga.learningmath.util.Settings
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Composable
fun LearningMathApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Selection) }
    
    val inputModes = remember {
        val saved = Settings.getString("input_modes", "{}")
        val mutableModes = mutableStateMapOf<Operation, InputMode>()
        
        // Default values
        Operation.entries.forEach { op ->
            mutableModes[op] = when (op) {
                Operation.ADDITION, Operation.SUBTRACTION -> InputMode.STEP_BY_STEP
                else -> InputMode.DIRECT
            }
        }
        
        try {
            val decoded = Json.decodeFromString<Map<Operation, InputMode>>(saved)
            decoded.forEach { (op, mode) -> mutableModes[op] = mode }
        } catch (ignored: Exception) {
            // Use defaults
        }
        mutableModes
    }

    val stats = remember {
        val saved = Settings.getString("stats", "{}")
        val mutableStats = mutableStateMapOf<String, MutableMap<Operation, Int>>()
        try {
            val decoded = Json.decodeFromString<Map<String, Map<Operation, Int>>>(saved)
            decoded.forEach { (date, ops) ->
                val dayMap = mutableStateMapOf<Operation, Int>()
                ops.forEach { (op, count) -> dayMap[op] = count }
                mutableStats[date] = dayMap
            }
        } catch (ignored: Exception) {
            // Handle error or empty stats
        }
        mutableStats
    }

    LearningMathTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val screen = currentScreen) {
                is Screen.Selection -> {
                    SelectionScreen(
                        onOperationSelected = {
                            currentScreen = Screen.Game(it)
                        },
                        onStatsDetailClick = {
                            currentScreen = Screen.StatsDetail
                        },
                        stats = stats
                    )
                }
                is Screen.Game -> {
                    GameScreen(
                        operation = screen.operation,
                        onBack = { currentScreen = Screen.Selection },
                        onCorrectAnswer = { op ->
                            val dateKey = getCurrentDate().toString()
                            val dayStats = stats.getOrPut(dateKey) { mutableStateMapOf() }
                            dayStats[op] = (dayStats[op] ?: 0) + 1
                            
                            // Save stats
                            try {
                                val statsToSave = stats.mapValues { it.value.toMap() }
                                val encoded = Json.encodeToString(statsToSave)
                                Settings.setString("stats", encoded)
                            } catch (ignored: Exception) {
                                // Handle error
                            }
                        },
                        inputMode = inputModes[screen.operation] ?: InputMode.DIRECT,
                        onInputModeChange = { newMode ->
                            inputModes[screen.operation] = newMode
                            try {
                                val modesToSave = inputModes.toMap()
                                val encoded = Json.encodeToString(modesToSave)
                                Settings.setString("input_modes", encoded)
                            } catch (ignored: Exception) {
                                // Handle error
                            }
                        }
                    )
                }
                is Screen.StatsDetail -> {
                    StatsDetailScreen(
                        stats = stats,
                        onBack = { currentScreen = Screen.Selection }
                    )
                }
            }
        }
    }
}
