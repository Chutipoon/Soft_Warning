package com.softwarn.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.softwarn.app.util.formatDurationThai
import com.softwarn.app.viewmodel.NotificationLogViewModel
import com.softwarn.app.viewmodel.WarningLogItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationLogScreen(viewModel: NotificationLogViewModel = hiltViewModel()) {
    val events by viewModel.events.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("บันทึกแจ้งเตือน", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (events.isEmpty()) {
            Text(
                "ยังไม่มีการแจ้งเตือน",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(events) { event -> LogRow(event) }
            }
        }
    }
}

@Composable
private fun LogRow(event: WarningLogItem) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.US) }

    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                timeFormat.format(Date(event.firedAt)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${event.appName} ใช้งานครบ ${formatDurationThai(event.sessionDurationMs)}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
