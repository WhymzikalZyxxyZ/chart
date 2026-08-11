package xyz.zyxwonderland.chart.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.GrantTypeValues
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Owns the SMART App Launch OAuth2/PKCE flow via AppAuth-Android. See
 * docs/adr/002-fhir-source-and-auth.md for why AppAuth rather than hand-rolled
 * PKCE/state handling, and docs/architecture/overview.md for the full sequence.
 *
 * This is a public client (docs/adr/003-no-backend-required.md) — no client
 * secret exists anywhere in this flow, PKCE is what stands in for one.
 */
class SmartAuthManager(context: Context) {
    private val authService = AuthorizationService(context)
    private val tokenStore = TokenStore(context)

    data class LaunchResult(val accessToken: String, val patientId: String)

    /** Discovers the sandbox's endpoints and builds the Custom Tabs authorization intent. */
    suspend fun buildAuthorizationIntent(): Intent {
        val serviceConfig = SmartConfigurationClient.fetchServiceConfig(SmartLaunchConfig.FHIR_BASE_URL)
        val request = AuthorizationRequest.Builder(
            serviceConfig,
            SmartLaunchConfig.CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(SmartLaunchConfig.REDIRECT_URI),
        )
            // PKCE code_verifier/code_challenge and the state parameter are both
            // generated automatically by this builder — see ADR-002.
            .setScope(SmartLaunchConfig.SCOPE)
            .setAdditionalParameters(mapOf("aud" to SmartLaunchConfig.FHIR_BASE_URL))
            .build()
        return authService.getAuthorizationRequestIntent(request)
    }

    /** Exchanges the redirect intent's authorization code for tokens and persists the refresh token. */
    suspend fun handleAuthorizationResponse(intent: Intent): LaunchResult {
        val response = AuthorizationResponse.fromIntent(intent)
        val exception = AuthorizationException.fromIntent(intent)
        if (response == null) {
            throw exception ?: IllegalStateException("No authorization response or exception in redirect intent")
        }

        val tokenEndpoint = response.request.configuration.tokenEndpoint.toString()
        val tokenResponse = performTokenRequest(response.createTokenExchangeRequest())
        return persistAndExtract(tokenResponse, tokenEndpoint)
    }

    /**
     * Silently resumes a prior session using the stored refresh token, if any.
     * Returns null (and clears the stored session) if there is none or it's no
     * longer valid — the caller falls back to showing the Connect button.
     */
    suspend fun resumeSession(): LaunchResult? {
        val session = tokenStore.load() ?: return null
        // AppAuth's TokenRequest.Builder requires a full service configuration,
        // but a refresh grant only ever reads its tokenEndpoint — the
        // authorization endpoint is never used on this path, so reusing the
        // token endpoint there is harmless rather than re-fetching discovery.
        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse(session.tokenEndpoint),
            Uri.parse(session.tokenEndpoint),
        )
        val request = TokenRequest.Builder(serviceConfig, SmartLaunchConfig.CLIENT_ID)
            .setGrantType(GrantTypeValues.REFRESH_TOKEN)
            .setRefreshToken(session.refreshToken)
            .build()

        return try {
            val tokenResponse = performTokenRequest(request)
            val accessToken = tokenResponse.accessToken ?: return null
            // Persist again in case the sandbox rotated the refresh token.
            (tokenResponse.refreshToken ?: session.refreshToken).let {
                tokenStore.save(it, session.patientId, session.tokenEndpoint)
            }
            LaunchResult(accessToken, session.patientId)
        } catch (e: AuthorizationException) {
            // Refresh token expired or was revoked server-side.
            tokenStore.clear()
            null
        }
    }

    fun logout() = tokenStore.clear()

    fun dispose() = authService.dispose()

    private suspend fun performTokenRequest(request: TokenRequest): TokenResponse =
        suspendCancellableCoroutine { cont ->
            authService.performTokenRequest(request) { response, exception ->
                when {
                    response != null -> cont.resume(response)
                    exception != null -> cont.resumeWithException(exception)
                    else -> cont.resumeWithException(
                        IllegalStateException("Token request returned neither a response nor an exception"),
                    )
                }
            }
        }

    private fun persistAndExtract(tokenResponse: TokenResponse, tokenEndpoint: String): LaunchResult {
        val accessToken = tokenResponse.accessToken
            ?: throw IllegalStateException("Token response missing access_token")
        val patientId = tokenResponse.additionalParameters["patient"]
            ?: throw IllegalStateException("Token response missing SMART 'patient' launch context")
        tokenResponse.refreshToken?.let { tokenStore.save(it, patientId, tokenEndpoint) }
        return LaunchResult(accessToken, patientId)
    }
}
