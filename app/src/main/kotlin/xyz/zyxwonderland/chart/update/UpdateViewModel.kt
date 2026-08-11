package xyz.zyxwonderland.chart.update

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.zyxwonderland.chart.BuildConfig
import xyz.zyxwonderland.chart.network.ChartHttpClient

class UpdateViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("chart_update", Context.MODE_PRIVATE)
    private val checker = UpdateChecker(ChartHttpClient.instance, prefs)

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    init {
        viewModelScope.launch {
            _updateInfo.value = checker.checkForUpdate(BuildConfig.VERSION_NAME)
        }
    }

    fun dismiss() {
        val info = _updateInfo.value ?: return
        checker.dismiss(info.versionTag)
        _updateInfo.value = null
    }
}
