package xyz.zyxwonderland.chart.auth

import android.net.Uri
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import net.openid.appauth.AuthorizationServiceConfiguration
import xyz.zyxwonderland.chart.network.ChartHttpClient

/**
 * Fetches a SMART server's `.well-known/smart-configuration` and builds an
 * AppAuth [AuthorizationServiceConfiguration] from it directly, rather than
 * using AppAuth's own OIDC discovery — the sandbox's config document has
 * SMART-specific fields AppAuth's discovery client doesn't need to parse,
 * and we only care about two of its fields anyway.
 */
object SmartConfigurationClient {
    suspend fun fetchServiceConfig(
        fhirBaseUrl: String,
        client: HttpClient = ChartHttpClient.instance,
    ): AuthorizationServiceConfiguration {
        val config: SmartConfigurationResponse =
            client.get("$fhirBaseUrl/.well-known/smart-configuration").body()
        return AuthorizationServiceConfiguration(
            Uri.parse(config.authorizationEndpoint),
            Uri.parse(config.tokenEndpoint),
        )
    }
}
