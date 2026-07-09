package com.softwarn.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.softwarn.app.util.formatDurationThai
import com.softwarn.app.viewmodel.DayUsage
import com.softwarn.app.viewmodel.StatsViewModel

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val weeklyUsage by viewModel.weeklyUsage.collectAsState()
    val total = weeklyUsage.sumOf { it.totalMs }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("สถิติรายสัปดาห์", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "รวม ${formatDurationThai(total)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            WeeklyBarChart(
                days = weeklyUsage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(20.dp)
            )
        }
    }
}

@Composable
private fun WeeklyBarChart(days: List<DayUsage>, modifier: Modifier = Modifier) {
    val maxMs = (days.maxOfOrNull { it.totalMs } ?: 0L).coerceAtLeast(1L)

    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceEvenly) {
        days.forEach { day ->
            Column(modifier = Modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.weight(1f).width(24.dp), contentAlignment = Alignment.BottomCenter) {
                    val fraction = (day.totalMs.toFloat() / maxMs.toFloat()).coerceIn(0.03f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fraction)
                            .background(Color(0xFF4C6EF5), shape = RoundedCornerShape(6.dp))
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(day.label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
