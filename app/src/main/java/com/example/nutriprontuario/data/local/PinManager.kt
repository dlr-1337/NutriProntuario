package com.example.nutriprontuario.data.local

import android.content.Context

class PinManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasPin(): Boolean = prefs.contains(KEY_PIN)

    fun savePin(pin: String) {
        prefs.edit().putString(KEY_PIN, pin).apply()
    }

    fun validate(pin: String): Boolean {
        val stored = prefs.getString(KEY_PIN, null)
        return stored != null && stored == pin
    }

    fun clearPin() {
        prefs.edit().remove(KEY_PIN).apply()
    }

    companion object {
        private const val PREFS_NAME = "pin_prefs"
        private const val KEY_PIN = "pin_value"
    }
}
