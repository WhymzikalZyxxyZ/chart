package xyz.zyxwonderland.chart.auth

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

private const val PREFS_NAME = "chart_auth_secure"
private const val KEY_REFRESH_TOKEN = "refresh_token"
private const val KEY_PATIENT_ID = "patient_id"
private const val KEY_TOKEN_ENDPOINT = "token_endpoint"

/**
 * Keystore-backed storage for OAuth session-resumption data only.
 *
 * Per docs/adr/004-no-persistent-storage.md: this store holds exactly what's
 * needed to silently resume a SMART session on next launch (a refresh token,
 * the patient ID it's scoped to, and the token endpoint to refresh against)
 * and NOTHING clinical. Fetched FHIR resources must never be written here —
 * they live in ViewModel state only and are discarded on logout/app close.
 */
class TokenStore(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    data class Session(val refreshToken: String, val patientId: String, val tokenEndpoint: String)

    fun save(refreshToken: String, patientId: String, tokenEndpoint: String) {
        prefs.edit {
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putString(KEY_PATIENT_ID, patientId)
            putString(KEY_TOKEN_ENDPOINT, tokenEndpoint)
        }
    }

    fun load(): Session? {
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null) ?: return null
        val patientId = prefs.getString(KEY_PATIENT_ID, null) ?: return null
        val tokenEndpoint = prefs.getString(KEY_TOKEN_ENDPOINT, null) ?: return null
        return Session(refreshToken, patientId, tokenEndpoint)
    }

    fun clear() {
        prefs.edit { clear() }
    }
}
