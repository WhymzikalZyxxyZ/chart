package xyz.zyxwonderland.chart.ui.launch

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.zyxwonderland.chart.auth.SmartAuthManager
import xyz.zyxwonderland.chart.auth.SmartLaunchConfig
import xyz.zyxwonderland.chart.fhir.FhirClient
import xyz.zyxwonderland.chart.fhir.FhirPatient

sealed interface LaunchUiState {
    data object Idle : LaunchUiState
    data object Connecting : LaunchUiState
    data class Connected(val patient: FhirPatient) : LaunchUiState
    data class Error(val message: String) : LaunchUiState
}

class SmartLaunchViewModel(application: Application) : AndroidViewModel(application) {
    private val authManager = SmartAuthManager(application)

    private val _uiState = MutableStateFlow<LaunchUiState>(LaunchUiState.Idle)
    val uiState: StateFlow<LaunchUiState> = _uiState.asStateFlow()

    init {
        // Silently resume a prior session, if one was persisted — see
        // docs/adr/004-no-persistent-storage.md: only the refresh token and
        // patient id survive between launches, never the fetched resource.
        viewModelScope.launch {
            _uiState.value = LaunchUiState.Connecting
            val result = authManager.resumeSession()
            _uiState.value = if (result != null) {
                fetchPatientOrError(result.accessToken, result.patientId)
            } else {
                LaunchUiState.Idle
            }
        }
    }

    /** Starts a fresh SMART App Launch. [launchIntent] should call an ActivityResultLauncher. */
    fun connect(launchIntent: (Intent) -> Unit) {
        viewModelScope.launch {
            _uiState.value = LaunchUiState.Connecting
            try {
                launchIntent(authManager.buildAuthorizationIntent())
            } catch (e: Exception) {
                _uiState.value = LaunchUiState.Error(e.message ?: "Could not start SMART App Launch")
            }
        }
    }

    /** Call from the ActivityResultLauncher callback with the redirect intent. */
    fun onAuthorizationResult(intent: Intent?) {
        if (intent == null) {
            _uiState.value = LaunchUiState.Idle // user cancelled/backed out of the Custom Tab
            return
        }
        viewModelScope.launch {
            _uiState.value = LaunchUiState.Connecting
            _uiState.value = try {
                val result = authManager.handleAuthorizationResponse(intent)
                fetchPatientOrError(result.accessToken, result.patientId)
            } catch (e: Exception) {
                LaunchUiState.Error(e.message ?: "SMART App Launch failed")
            }
        }
    }

    fun disconnect() {
        authManager.logout()
        _uiState.value = LaunchUiState.Idle
    }

    private suspend fun fetchPatientOrError(accessToken: String, patientId: String): LaunchUiState =
        try {
            val patient = FhirClient.fetchPatient(SmartLaunchConfig.FHIR_BASE_URL, accessToken, patientId)
            LaunchUiState.Connected(patient)
        } catch (e: Exception) {
            LaunchUiState.Error(e.message ?: "Connected, but fetching the patient record failed")
        }

    override fun onCleared() {
        authManager.dispose()
    }
}
