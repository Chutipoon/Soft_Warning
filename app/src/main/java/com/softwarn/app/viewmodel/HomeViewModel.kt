package com.softwarn.app.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softwarn.app.data.AppSessionDao
import com.softwarn.app.util.InstalledAppsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class TopAppUsage(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap?,
    val durationMs: Long
)

data class HomeUiState(
    val totalDurationMs: Long = 0L,
    val topApps: List<TopAppUsage> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appSessionDao: AppSessionDao,
    private val installedAppsProvider: InstalledAppsProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appSessionDao.getUsageSince(startOfTodayMillis()).collect { usage ->
                val topApps = usage.take(MAX_TOP_APPS).map { u ->
                    TopAppUsage(
                        packageName = u.packageName,
                        label = installedAppsProvider.resolveLabel(u.packageName),
                        icon = installedAppsProvider.resolveIcon(u.packageName),
                        durationMs = u.totalMs
                    )
                }
                _uiState.value = HomeUiState(
                    totalDurationMs = usage.sumOf { it.totalMs },
                    topApps = topApps
                )
            }
        }
    }

    private fun startOfTodayMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    companion object {
        private const val MAX_TOP_APPS = 5
    }
}
