package xyz.zyxwonderland.chart.fhir

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import xyz.zyxwonderland.chart.network.ChartHttpClient

/**
 * Read-only FHIR REST client. Talks directly to the SMART sandbox with the
 * access token obtained via [xyz.zyxwonderland.chart.auth.SmartAuthManager] —
 * no backend in between, per docs/adr/003-no-backend-required.md. Results are
 * returned to the caller only; nothing here writes to disk (ADR-004).
 */
object FhirClient {
    suspend fun fetchPatient(
        fhirBaseUrl: String,
        accessToken: String,
        patientId: String,
        client: HttpClient = ChartHttpClient.instance,
    ): FhirPatient =
        client.get("$fhirBaseUrl/Patient/$patientId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            header(HttpHeaders.Accept, "application/fhir+json")
        }.body()
}
