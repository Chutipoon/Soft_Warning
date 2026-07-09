package com.softwarn.app.ui.screen

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.softwarn.app.util.PermissionChecker

/**
 * Google's Prominent Disclosure requirement for sensitive permissions (usage-stats
 * access): explain why *before* the OS grant screen, with an explicit accept path and
 * an explicit, equally-visible decline path — no pre-checked defaults, no hidden skip.
 */
@Composable
fun PermissionOnboardingScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasUsageAccess by remember { mutableStateOf(PermissionChecker.hasUsageAccess(context)) }
    var hasOverlay by remember { mutableStateOf(PermissionChecker.hasOverlayPermission(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsageAccess = PermissionChecker.hasUsageAccess(context)
                hasOverlay = PermissionChecker.hasOverlayPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("ก่อนเริ่มใช้งาน Soft Warning", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "แอปนี้ทำงานแบบออฟไลน์ 100% ข้อมูลการใช้งานทั้งหมดถูกประมวลผลและเก็บไว้ในเครื่องของคุณเท่านั้น " +
                "ไม่มีการส่งข้อมูลออกไปที่ใดทั้งสิ้น เพื่อให้แอปแจ้งเตือนเมื่อคุณใช้แอปอื่นนานเกินไปได้ " +
                "จำเป็นต้องขออนุญาต 2 อย่างดังนี้",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        PermissionCard(
            title = "การเข้าถึงข้อมูลการใช้งาน",
            description = "ใช้ตรวจสอบว่าแอปไหนกำลังเปิดอยู่และใช้งานมานานเท่าไหร่ เพื่อคำนวณเวลาหน้าจอให้คุณ",
            granted = hasUsageAccess,
            onRequestClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        PermissionCard(
            title = "แสดงหน้าต่างทับแอปอื่น",
            description = "ใช้แสดงกล่องแจ้งเตือนเมื่อคุณใช้งานแอปที่ตั้งขีดจำกัดไว้นานเกินเวลาที่กำหนด",
            granted = hasOverlay,
            onRequestClick = {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                )
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = hasUsageAccess && hasOverlay
        ) {
            Text(if (hasUsageAccess && hasOverlay) "ดำเนินการต่อ" else "อนุญาตให้ครบเพื่อดำเนินการต่อ")
        }
        Spacer(modifier = Modifier.height(4.dp))
        TextButton(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("ข้ามไปก่อน (แอปจะทำงานได้ไม่ครบถ้วน)")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    granted: Boolean,
    onRequestClick: () -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (granted) "✓" else "○",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (granted) Color(0xFF12B886) else MaterialTheme.colorScheme.outline
                )
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!granted) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onRequestClick) { Text("อนุญาต") }
            }
        }
    }
}
