package com.softwarn.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.softwarn.app.service.UsageMonitorService
import com.softwarn.app.service.WarningOverlayService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            context.startForegroundService(Intent(context, UsageMonitorService::class.java))
            context.startService(Intent(context, WarningOverlayService::class.java))
        }
    }
}
