package com.softwarn.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softwarn.app.data.AppSessionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class DayUsage(val label: String, val totalMs: Long)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val appSessionDao: AppSessionDao
) : ViewModel() {

    private val _weeklyUsage = MutableStateFlow<List<DayUsage>>(emptyList())
    val weeklyUsage: StateFlow<List<DayUsage>> = _weeklyUsage.asStateFlow()

    private val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dayLabelFormat = SimpleDateFormat("EEE", Locale("th", "TH"))

    init {
        viewModelScope.launch {
            appSessionDao.getDailyUsageSince(startOfDay(daysAgo = DAYS_BACK - 1)).collect { rows ->
                val totalsByDay = rows.associate { it.day to it.totalMs }
                _weeklyUsage.value = (DAYS_BACK - 1 downTo 0).map { offset ->
                    val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }
                    val key = dayKeyFormat.format(calendar.time)
                    DayUsage(label = dayLabelFormat.format(calendar.time), totalMs = totalsByDay[key] ?: 0L)
                }
            }
        }
    }

    private fun startOfDay(daysAgo: Int): Long {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    companion object {
        private const val DAYS_BACK = 7
    }
}
