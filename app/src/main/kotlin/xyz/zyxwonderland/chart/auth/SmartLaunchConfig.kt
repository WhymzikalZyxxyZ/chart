package xyz.zyxwonderland.chart.auth

/**
 * SMART App Launch constants for the SMART Health IT public sandbox.
 * See docs/adr/002-fhir-source-and-auth.md — this is a public/native client
 * against a sandbox that explicitly allows unregistered client_ids, so
 * CLIENT_ID is just a self-chosen identifier, not a secret or a registration.
 */
object SmartLaunchConfig {
    const val FHIR_BASE_URL = "https://launch.smarthealthit.org/v/r4/fhir"
    const val CLIENT_ID = "chart-android"
    const val REDIRECT_URI = "xyz.zyxwonderland.chart:/oauth2redirect"

    // launch/patient requests standalone-launch patient context (the sandbox's
    // own patient-picker screen); offline_access requests the refresh token
    // TokenStore persists per docs/adr/004-no-persistent-storage.md.
    const val SCOPE = "launch/patient openid fhirUser offline_access " +
        "patient/Patient.read patient/Condition.read " +
        "patient/MedicationRequest.read patient/Observation.read"
}
