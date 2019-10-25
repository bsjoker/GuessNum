package com.truelove.kotlinplatinarush.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.funapps.gje.App

class PreferencesHelper {
    companion object {
        const val TAG = "PreferencesHelper"
    }

    fun getSharedPreferences(): SharedPreferences {
        return App.instance.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    }

    fun savePreference(key: String, value: Any?) {
        val sp = getSharedPreferences()
        val editor = sp.edit()

        when (value){
            is Boolean -> editor.putBoolean(key, value)
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Float -> editor.putFloat(key, value)
            is Long -> editor.putLong(key, value)
            is Set<*> -> editor.putStringSet(key, value as MutableSet<String>?)

            else -> {
                val msg = String.format("%s: %s", key, value)
                Log.e(TAG, String.format("Configuration error. InvalidClassException: %s", msg))
            }
        }

        editor.apply()
    }
}