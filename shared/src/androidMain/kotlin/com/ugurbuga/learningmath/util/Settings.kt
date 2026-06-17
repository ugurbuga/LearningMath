package com.ugurbuga.learningmath.util

import android.content.Context
import android.content.SharedPreferences

actual object Settings {
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences("learningmath_prefs", Context.MODE_PRIVATE)
    }

    actual fun setString(key: String, value: String) {
        prefs?.edit()?.putString(key, value)?.apply()
    }

    actual fun getString(key: String, defaultValue: String): String {
        return prefs?.getString(key, defaultValue) ?: defaultValue
    }
}
