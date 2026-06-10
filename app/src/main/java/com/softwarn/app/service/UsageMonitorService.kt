package com.softwarn.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.softwarn.app.data.AppSession
import com.softwarn.app.data.AppSessionDao
import com.softwarn.app.data.WarningRuleDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

@AndroidEntryPoint
class UsageMonitorService : Service() {

    @Inject lateinit var appSessionDao: AppSessionDao
    @Inject lateinit var warningRuleDao: WarningRuleDao

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val handler = Handler(Looper.getMainLooper())
    private val pollingInterval = 60_000L

    @Volatile private var currentPackage: String? = null
    @Volatile private var sessionStartTime: Long = 0
    private val lastWarningTime = AtomicLong(0L)

    private val pollRunnable = object : Runnable {
        override fun run() {
            checkForegroundApp()
            handler.postDelayed(this, pollingInterval)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.removeCallbacks(pollRunnable)
        handler.post(pollRunnable)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun checkForegroundApp() {
        val now = System.currentTimeMillis()
        val foregroundApp = getCurrentForegroundApp()

        if (foregroundApp != currentPackage) {
            saveSession(currentPackage, sessionStartTime, now)
            currentPackage = foregroundApp
            sessionStartTime = now
            lastWarningTime.set(0L)
        } else if (foregroundApp != null) {
            checkWarning(foregroundApp, now)
        }
    }

    private fun getCurrentForegroundApp(): String? {
        val usm = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 5000, now)
        return stats?.maxByOrNull { it.lastTimeUsed }?.packageName
    }

    private fun saveSession(packageName: String?, startTime: Long, endTime: Long) {
        if (packageName == null || startTime == 0L) return
        val duration = endTime - startTime
        if (duration <= 0) return

        serviceScope.launch {
            appSessionDao.insert(AppSession(packageName = packageName, startTime = startTime, endTime = endTime))
        }
    }

    private fun checkWarning(packageName: String, now: Long) {
        val capturedSessionStart = sessionStartTime
        val capturedLastWarning = lastWarningTime.get()
        serviceScope.launch {
            val rule = warningRuleDao.getEnabledRule(packageName) ?: return@launch
            val durationMs = now - capturedSessionStart
            val intervalMs = rule.intervalMinutes * 60_000L

            if (durationMs >= intervalMs && (now - capturedLastWarning) >= intervalMs) {
                fireWarning(packageName, durationMs)
                lastWarningTime.set(now)
            }
        }
    }

    private fun fireWarning(packageName: String, duration: Long) {
        val intent = Intent("com.softwarn.ACTION_WARNING").apply {
            putExtra("package_name", packageName)
            putExtra("session_duration_ms", duration)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "usage_monitor_channel",
                "Soft Warning",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, "usage_monitor_channel")
        .setContentTitle("Soft Warning")
        .setContentText("Monitoring app usage...")
        .setSmallIcon(android.R.drawable.ic_menu_recent_history)
        .setOngoing(true)
        .build()

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(pollRunnable)
        saveSession(currentPackage, sessionStartTime, System.currentTimeMillis())
        serviceScope.cancel()
    }
}
