package xyz.zyxwonderland.chart

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
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

                Box(modifier = Modifier.fillMaxSize()) {
                    Text("CHART — coming soon", modifier = Modifier.align(Alignment.Center))

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
