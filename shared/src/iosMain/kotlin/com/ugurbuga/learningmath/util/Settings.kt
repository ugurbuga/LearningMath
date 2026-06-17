package com.ugurbuga.learningmath.util

import platform.Foundation.NSUserDefaults

actual object Settings {
    actual fun setString(key: String, value: String) {
        NSUserDefaults.standardUserDefaults.setObject(value, key)
    }

    actual fun getString(key: String, defaultValue: String): String {
        return NSUserDefaults.standardUserDefaults.stringForKey(key) ?: defaultValue
    }
}
