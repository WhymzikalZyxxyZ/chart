package xyz.zyxwonderland.chart.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Single shared HTTP client for the app. Currently only used by [xyz.zyxwonderland.chart.update.UpdateChecker];
 * the FHIR client described in docs/architecture/overview.md will use its own client with
 * Bearer-token handling once the SMART App Launch flow is built (see ADR-002).
 */
object ChartHttpClient {
    val instance: HttpClient by lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 5_000
                connectTimeoutMillis = 5_000
            }
        }
    }
}
