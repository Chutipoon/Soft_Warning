package com.softwarn.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.softwarn.app.service.UsageMonitorService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, UsageMonitorService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
