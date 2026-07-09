package com.softwarn.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softwarn.app.data.WarningEventDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class WarningLogItem(
    val appName: String,
    val firedAt: Long,
    val sessionDurationMs: Long
)

@HiltViewModel
class NotificationLogViewModel @Inject constructor(
    warningEventDao: WarningEventDao
) : ViewModel() {

    val events: StateFlow<List<WarningLogItem>> = warningEventDao.getRecent()
        .map { events -> events.map { WarningLogItem(it.appName, it.firedAt, it.sessionDurationMs) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
