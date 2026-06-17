package com.ugurbuga.learningmath.util

import java.io.File
import java.util.Properties

actual object Settings {
    private val configFile = File(System.getProperty("user.home"), ".learningmath_prefs")
    private val props = Properties()

    init {
        if (configFile.exists()) {
            configFile.inputStream().use { props.load(it) }
        }
    }

    actual fun setString(key: String, value: String) {
        props.setProperty(key, value)
        configFile.outputStream().use { props.store(it, null) }
    }

    actual fun getString(key: String, defaultValue: String): String {
        return props.getProperty(key, defaultValue) ?: defaultValue
    }
}
