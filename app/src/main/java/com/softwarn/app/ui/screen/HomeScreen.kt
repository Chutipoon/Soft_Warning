package com.softwarn.app.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.softwarn.app.util.formatDurationThai
import com.softwarn.app.viewmodel.HomeViewModel
import com.softwarn.app.viewmodel.TopAppUsage

private val SegmentColors = listOf(
    Color(0xFF4C6EF5), Color(0xFF12B886), Color(0xFFF59F00),
    Color(0xFFE64980), Color(0xFF7950F2)
)

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("การใช้งานวันนี้", style = MaterialTheme.typography.headlineSmall)

        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UsageDonutChart(topApps = uiState.topApps, totalDurationMs = uiState.totalDurationMs)
            }
        }

        Text("แอปที่ใช้เยอะสุด", style = MaterialTheme.typography.titleMedium)

        if (uiState.topApps.isEmpty()) {
            Text(
                "ยังไม่มีข้อมูลการใช้งานวันนี้",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(uiState.topApps) { index, app ->
                    TopAppRow(app = app, color = SegmentColors[index % SegmentColors.size])
                }
            }
        }
    }
}

@Composable
private fun UsageDonutChart(topApps: List<TopAppUsage>, totalDurationMs: Long) {
    Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 28.dp.toPx()
            if (totalDurationMs <= 0L) {
                drawArc(
                    color = Color(0xFFE9ECEF),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )
            } else {
                var startAngle = -90f
                topApps.forEachIndexed { index, app ->
                    val sweep = 360f * (app.durationMs.toFloat() / totalDurationMs.toFloat())
                    drawArc(
                        color = SegmentColors[index % SegmentColors.size],
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    startAngle += sweep
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("เวลา", style = MaterialTheme.typography.bodyMedium)
            Text(
                formatDurationThai(totalDurationMs),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TopAppRow(app: TopAppUsage, color: Color) {
    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, shape = CircleShape)
            )
            app.icon?.let {
                Image(bitmap = it, contentDescription = app.label, modifier = Modifier.size(36.dp))
            }
            Text(app.label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Text(formatDurationThai(app.durationMs), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
