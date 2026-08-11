package xyz.zyxwonderland.chart.ui.launch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import xyz.zyxwonderland.chart.fhir.FhirPatient

@Composable
fun LaunchScreen(
    state: LaunchUiState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "⚠️ Synthetic test data only — not a real patient record.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.padding(top = 16.dp))

        when (state) {
            is LaunchUiState.Idle -> {
                Text("Not connected", style = MaterialTheme.typography.titleMedium)
                Button(onClick = onConnect) {
                    Text("Connect to SMART Sandbox")
                }
            }

            is LaunchUiState.Connecting -> {
                CircularProgressIndicator()
                Text("Connecting…", style = MaterialTheme.typography.bodyMedium)
            }

            is LaunchUiState.Connected -> {
                PatientSummary(state.patient)
                OutlinedButton(onClick = onDisconnect) {
                    Text("Disconnect")
                }
            }

            is LaunchUiState.Error -> {
                Text("Something went wrong", style = MaterialTheme.typography.titleMedium)
                Text(state.message, style = MaterialTheme.typography.bodySmall)
                Button(onClick = onConnect) {
                    Text("Try again")
                }
            }
        }
    }
}

@Composable
private fun PatientSummary(patient: FhirPatient) {
    Text("Connected", style = MaterialTheme.typography.titleMedium)
    Text(patient.displayName, style = MaterialTheme.typography.headlineSmall)
    patient.gender?.let { Text("Gender: $it") }
    patient.birthDate?.let { Text("Born: $it") }
}
