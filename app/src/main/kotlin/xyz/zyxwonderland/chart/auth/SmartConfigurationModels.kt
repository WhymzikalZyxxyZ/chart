package xyz.zyxwonderland.chart.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Subset of a SMART `.well-known/smart-configuration` response this app needs. */
@Serializable
data class SmartConfigurationResponse(
    @SerialName("authorization_endpoint") val authorizationEndpoint: String,
    @SerialName("token_endpoint") val tokenEndpoint: String,
)
