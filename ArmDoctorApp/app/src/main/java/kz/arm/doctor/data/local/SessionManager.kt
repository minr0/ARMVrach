// data/local/SessionManager.kt
package kz.arm.doctor.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Simple SharedPreferences wrapper to persist auth token and user info.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "arm_doctor_session"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_IIN = "iin"
    }

    fun saveSession(token: String, userId: Int, fullName: String, iin: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_FULL_NAME, fullName)
            .putString(KEY_IIN, iin)
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    fun getFullName(): String = prefs.getString(KEY_FULL_NAME, "") ?: ""

    fun getIin(): String = prefs.getString(KEY_IIN, "") ?: ""

    fun isLoggedIn(): Boolean = getToken() != null

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
