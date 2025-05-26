package ru.fitquest.android.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class TokensManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_tokens", Context.MODE_PRIVATE)

    fun save(accessToken: String, refreshToken: String) {
        prefs.edit {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
        }
    }

    fun getAccess(): String? = prefs.getString("access_token", null)
    fun getRefresh(): String? = prefs.getString("refresh_token", null)

    fun clear() {
        prefs.edit {
            remove("access_token")
            remove("refresh_token")
        }
    }

    fun isLoggedIn(): Boolean {
        return getAccess() != null && getRefresh() != null
    }
}
