package com.ugurbuga.learningmath.util

import kotlinx.datetime.LocalDate
import java.time.format.TextStyle
import java.util.Locale

actual fun getDayOfWeekName(date: LocalDate): String {
    val javaDate = java.time.LocalDate.of(date.year, date.monthNumber, date.dayOfMonth)
    return javaDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("tr"))
}

actual fun getMonthName(date: LocalDate): String {
    val javaDate = java.time.LocalDate.of(date.year, date.monthNumber, date.dayOfMonth)
    return javaDate.month.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("tr"))
}
