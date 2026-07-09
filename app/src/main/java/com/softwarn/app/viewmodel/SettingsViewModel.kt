package com.softwarn.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softwarn.app.data.AppSession
import com.softwarn.app.data.AppSessionDao
import com.softwarn.app.data.WarningRule
import com.softwarn.app.data.WarningRuleDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

sealed class ExportImportStatus {
    data object Idle : ExportImportStatus()
    data object Success : ExportImportStatus()
    data class Error(val message: String) : ExportImportStatus()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val warningRuleDao: WarningRuleDao,
    private val appSessionDao: AppSessionDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _status = MutableStateFlow<ExportImportStatus>(ExportImportStatus.Idle)
    val status: StateFlow<ExportImportStatus> = _status.asStateFlow()

    fun exportTo(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val rules = warningRuleDao.getAll().first()
                val sessions = appSessionDao.getAllSessions().first()

                val json = JSONObject().apply {
                    put("exportedAt", System.currentTimeMillis())
                    put("rules", JSONArray(rules.map { rule ->
                        JSONObject().apply {
                            put("packageName", rule.packageName)
                            put("appName", rule.appName)
                            put("intervalMinutes", rule.intervalMinutes)
                            put("isEnabled", rule.isEnabled)
                        }
                    }))
                    put("sessions", JSONArray(sessions.map { session ->
                        JSONObject().apply {
                            put("packageName", session.packageName)
                            put("startTime", session.startTime)
                            put("endTime", session.endTime)
                        }
                    }))
                }

                val out = context.contentResolver.openOutputStream(uri) ?: error("openOutputStream returned null")
                out.use { it.write(json.toString(2).toByteArray()) }
            }.onSuccess {
                _status.value = ExportImportStatus.Success
            }.onFailure {
                _status.value = ExportImportStatus.Error(it.message ?: "Export failed")
            }
        }
    }

    fun importFrom(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val input = context.contentResolver.openInputStream(uri) ?: error("openInputStream returned null")
                val text = input.use { it.readBytes().decodeToString() }
                val json = JSONObject(text)

                val rulesJson = json.optJSONArray("rules") ?: JSONArray()
                for (i in 0 until rulesJson.length()) {
                    val r = rulesJson.getJSONObject(i)
                    warningRuleDao.upsert(
                        WarningRule(
                            packageName = r.getString("packageName"),
                            appName = r.getString("appName"),
                            intervalMinutes = r.optInt("intervalMinutes", RuleViewModel.DEFAULT_INTERVAL_MINUTES),
                            isEnabled = r.optBoolean("isEnabled", false)
                        )
                    )
                }

                val sessionsJson = json.optJSONArray("sessions") ?: JSONArray()
                for (i in 0 until sessionsJson.length()) {
                    val s = sessionsJson.getJSONObject(i)
                    appSessionDao.insert(
                        AppSession(
                            packageName = s.getString("packageName"),
                            startTime = s.getLong("startTime"),
                            endTime = s.getLong("endTime")
                        )
                    )
                }
            }.onSuccess {
                _status.value = ExportImportStatus.Success
            }.onFailure {
                _status.value = ExportImportStatus.Error(it.message ?: "Import failed")
            }
        }
    }

    fun clearStatus() {
        _status.value = ExportImportStatus.Idle
    }
}
