package xyz.zyxwonderland.chart.fhir

import kotlinx.serialization.Serializable

/**
 * Minimal subset of the FHIR R4 Patient resource this app displays. Deliberately
 * not a full FHIR model — CHART is a demonstration viewer, not a general-purpose
 * FHIR client, and only needs enough fields to prove the SMART launch round-trip.
 */
@Serializable
data class FhirPatient(
    val id: String? = null,
    val name: List<HumanName> = emptyList(),
    val gender: String? = null,
    val birthDate: String? = null,
) {
    val displayName: String
        get() = name.firstOrNull()?.let { n ->
            (n.given.orEmpty() + listOfNotNull(n.family)).joinToString(" ").ifBlank { null }
        } ?: "Unknown patient"
}

@Serializable
data class HumanName(
    val family: String? = null,
    val given: List<String>? = null,
)
