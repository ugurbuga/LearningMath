package com.ugurbuga.learningmath.model

sealed class Screen {
    data object Selection : Screen()
    data class Game(val operation: Operation) : Screen()
    data object StatsDetail : Screen()
}
