package xyz.zyxwonderland.chart

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.zyxwonderland.chart.ui.launch.LaunchScreen
import xyz.zyxwonderland.chart.ui.launch.SmartLaunchViewModel
import xyz.zyxwonderland.chart.ui.theme.ChartTheme
import xyz.zyxwonderland.chart.ui.update.UpdateBanner
import xyz.zyxwonderland.chart.update.UpdateViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ChartTheme {
                val updateViewModel: UpdateViewModel = viewModel()
                val updateInfo by updateViewModel.updateInfo.collectAsState()
                val context = LocalContext.current

                val launchViewModel: SmartLaunchViewModel = viewModel()
                val launchState by launchViewModel.uiState.collectAsState()
                val authLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult(),
                ) { result ->
                    launchViewModel.onAuthorizationResult(result.data)
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    LaunchScreen(
                        state = launchState,
                        onConnect = { launchViewModel.connect(authLauncher::launch) },
                        onDisconnect = launchViewModel::disconnect,
                    )

                    updateInfo?.let { info ->
                        UpdateBanner(
                            info = info,
                            onView = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(info.htmlUrl)))
                            },
                            onDismiss = updateViewModel::dismiss,
                            modifier = Modifier.align(Alignment.TopCenter),
                        )
                    }
                }
            }
        }
    }
}
