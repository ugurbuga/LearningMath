package com.ugurbuga.learningmath.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

expect fun getDayOfWeekName(date: LocalDate): String
expect fun getMonthName(date: LocalDate): String

fun getCurrentDate(): LocalDate {
    return Clock.System.todayIn(TimeZone.currentSystemDefault())
}
