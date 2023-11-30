package com.example.texter.repository

import android.app.Activity
import android.content.Context
class AppPeferences(context: Activity) {
    private val preferences = context.getSharedPreferences("email", Context.MODE_PRIVATE)

    fun setEmail(email: String) {
        with (preferences.edit()) {
            putString("email", email)
            apply()
        }
    }
    fun getEmail(): String? {
        return preferences.getString("email", "")
    }

}
