package com.ugurbuga.learningmath.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.TextStyle
import java.util.Locale

actual fun getDayOfWeekName(date: LocalDate): String {
    return date.toJavaLocalDate().dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("tr"))
}

actual fun getMonthName(date: LocalDate): String {
    return date.toJavaLocalDate().month.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("tr"))
}
