package com.ugurbuga.learningmath.util

import kotlinx.datetime.LocalDate
import platform.Foundation.*

actual fun getDayOfWeekName(date: LocalDate): String {
    val calendar = NSCalendar.currentCalendar
    val components = NSDateComponents()
    components.year = date.year.toLong()
    components.month = date.monthNumber.toLong()
    components.day = date.dayOfMonth.toLong()
    val nsDate = calendar.dateFromComponents(components) ?: return ""
    
    val formatter = NSDateFormatter()
    formatter.setLocale(NSLocale.localeWithLocaleIdentifier("tr_TR"))
    formatter.setDateFormat("EEE")
    return formatter.stringFromDate(nsDate)
}

actual fun getMonthName(date: LocalDate): String {
    val calendar = NSCalendar.currentCalendar
    val components = NSDateComponents()
    components.year = date.year.toLong()
    components.month = date.monthNumber.toLong()
    components.day = date.dayOfMonth.toLong()
    val nsDate = calendar.dateFromComponents(components) ?: return ""
    
    val formatter = NSDateFormatter()
    formatter.setLocale(NSLocale.localeWithLocaleIdentifier("tr_TR"))
    formatter.setDateFormat("MMM")
    return formatter.stringFromDate(nsDate)
}
