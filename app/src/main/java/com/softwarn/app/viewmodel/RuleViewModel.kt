package com.softwarn.app.viewmodel

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softwarn.app.data.WarningRule
import com.softwarn.app.data.WarningRuleDao
import com.softwarn.app.service.UsageMonitorService
import com.softwarn.app.service.WarningOverlayService
import com.softwarn.app.util.InstalledApp
import com.softwarn.app.util.InstalledAppsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppRuleUiState(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap,
    val isEnabled: Boolean,
    val intervalMinutes: Int
)

@HiltViewModel
class RuleViewModel @Inject constructor(
    private val warningRuleDao: WarningRuleDao,
    private val installedAppsProvider: InstalledAppsProvider,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())

    val uiState: StateFlow<List<AppRuleUiState>> =
        combine(installedApps, warningRuleDao.getAll()) { apps, rules ->
            val rulesByPackage = rules.associateBy { it.packageName }
            apps.map { app ->
                val rule = rulesByPackage[app.packageName]
                AppRuleUiState(
                    packageName = app.packageName,
                    label = app.label,
                    icon = app.icon,
                    isEnabled = rule?.isEnabled ?: false,
                    intervalMinutes = rule?.intervalMinutes ?: DEFAULT_INTERVAL_MINUTES
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            installedApps.value = installedAppsProvider.getLaunchableApps()
        }
    }

    fun setEnabled(app: AppRuleUiState, enabled: Boolean) {
        viewModelScope.launch {
            warningRuleDao.upsert(app.toRule(isEnabled = enabled))
        }
        if (enabled) ensureServicesRunning()
    }

    fun setIntervalMinutes(app: AppRuleUiState, minutes: Int) {
        viewModelScope.launch {
            warningRuleDao.upsert(app.toRule(intervalMinutes = minutes))
        }
    }

    // Rules are useless until UsageMonitorService is actually polling — today the only
    // other place that starts it is BootReceiver (on a real reboot). Starting an
    // already-running foreground service just re-invokes onStartCommand, which is a
    // harmless no-op, so it's safe to call this on every enable.
    private fun ensureServicesRunning() {
        ContextCompat.startForegroundService(context, Intent(context, UsageMonitorService::class.java))
        context.startService(Intent(context, WarningOverlayService::class.java))
    }

    private fun AppRuleUiState.toRule(
        isEnabled: Boolean = this.isEnabled,
        intervalMinutes: Int = this.intervalMinutes
    ) = WarningRule(
        packageName = packageName,
        appName = label,
        intervalMinutes = intervalMinutes,
        isEnabled = isEnabled
    )

    companion object {
        const val DEFAULT_INTERVAL_MINUTES = 20
    }
}
