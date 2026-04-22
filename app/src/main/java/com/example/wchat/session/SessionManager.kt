package com.example.wchat.session

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveJwtToken(token: String) {
        prefs.edit().putString(KEY_JWT_TOKEN, token).apply()
    }

    fun getJwtToken(): String? {
        return prefs.getString(KEY_JWT_TOKEN, null)
    }

    fun saveBackendUserId(userId: String) {
        prefs.edit().putString(KEY_BACKEND_USER_ID, userId).apply()
    }

    fun getBackendUserId(): String? {
        return prefs.getString(KEY_BACKEND_USER_ID, null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "wchat_session"
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_BACKEND_USER_ID = "backend_user_id"
    }
}