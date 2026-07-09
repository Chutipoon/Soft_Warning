package com.softwarn.app.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.softwarn.app.viewmodel.ExportImportStatus
import com.softwarn.app.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val status by viewModel.status.collectAsState()

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        uri?.let { viewModel.exportTo(it) }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { viewModel.importFrom(it) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("การตั้งค่า (ออฟไลน์)", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ข้อมูลเก็บออฟไลน์ 100%", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "ข้อมูลการใช้งานและการตั้งค่าทั้งหมดถูกเก็บไว้ในเครื่องของคุณเท่านั้น ไม่มีการส่งออกไปที่ใด",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val fileName = "soft_warning_export_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.json"
                exportLauncher.launch(fileName)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Export ข้อมูล (.json)")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { importLauncher.launch(arrayOf("application/json")) },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Import ข้อมูล")
        }

        Spacer(modifier = Modifier.height(12.dp))
        when (val current = status) {
            is ExportImportStatus.Success -> Text("สำเร็จ", color = Color(0xFF12B886))
            is ExportImportStatus.Error -> Text("ผิดพลาด: ${current.message}", color = MaterialTheme.colorScheme.error)
            ExportImportStatus.Idle -> {}
        }
    }
}
