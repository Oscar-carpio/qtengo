package com.example.qtengo.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("qtengo_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_EMAIL = "user_email"
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun clearSession() {
        prefs.edit().remove(KEY_USER_EMAIL).apply()
    }
}
