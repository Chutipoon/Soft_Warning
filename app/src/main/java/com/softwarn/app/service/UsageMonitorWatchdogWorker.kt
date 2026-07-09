package com.softwarn.app.service

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Periodic self-heal check: if [UsageMonitorService] or [WarningOverlayService] got
 * killed outright (not just throttled — that's a separate, acknowledged idle-polling
 * nuance), restart it. Runs every 15min (WorkManager's periodic-work floor).
 */
class UsageMonitorWatchdogWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        if (!UsageMonitorService.isRunning) {
            Log.d(TAG, "doWork: UsageMonitorService not running, restarting")
            tryStart {
                ContextCompat.startForegroundService(
                    applicationContext,
                    Intent(applicationContext, UsageMonitorService::class.java)
                )
            }
        }
        if (!WarningOverlayService.isRunning) {
            Log.d(TAG, "doWork: WarningOverlayService not running, restarting")
            tryStart {
                applicationContext.startService(Intent(applicationContext, WarningOverlayService::class.java))
            }
        }
        return Result.success()
    }

    private inline fun tryStart(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            Log.e(TAG, "doWork: restart failed", e)
        }
    }

    companion object {
        private const val TAG = "SoftWarnWatchdog"
    }
}
