package com.ugurbuga.learningmath.util

expect object Settings {
    fun setString(key: String, value: String)
    fun getString(key: String, defaultValue: String): String
}
