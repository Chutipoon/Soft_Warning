package com.softwarn.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.softwarn.app.viewmodel.AppRuleUiState
import com.softwarn.app.viewmodel.RuleViewModel

@Composable
fun AppLimitsScreen(viewModel: RuleViewModel = hiltViewModel()) {
    val apps by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("กำหนดขีดจำกัดแอป", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(apps, key = { it.packageName }) { app ->
                AppLimitRow(
                    app = app,
                    onEnabledChange = { viewModel.setEnabled(app, it) },
                    onIntervalChangeFinished = { viewModel.setIntervalMinutes(app, it) }
                )
            }
        }
    }
}

@Composable
private fun AppLimitRow(
    app: AppRuleUiState,
    onEnabledChange: (Boolean) -> Unit,
    onIntervalChangeFinished: (Int) -> Unit
) {
    var sliderValue by remember(app.packageName, app.intervalMinutes) {
        mutableFloatStateOf(app.intervalMinutes.toFloat())
    }

    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(bitmap = app.icon, contentDescription = app.label, modifier = Modifier.size(36.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(app.label, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "${sliderValue.toInt()} นาที",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = app.isEnabled, onCheckedChange = onEnabledChange)
            }

            if (app.isEnabled) {
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onIntervalChangeFinished(sliderValue.toInt()) },
                    valueRange = MIN_MINUTES..MAX_MINUTES,
                    steps = ((MAX_MINUTES - MIN_MINUTES) / STEP_MINUTES).toInt() - 1
                )
            }
        }
    }
}

private const val MIN_MINUTES = 5f
private const val MAX_MINUTES = 120f
private const val STEP_MINUTES = 5f
