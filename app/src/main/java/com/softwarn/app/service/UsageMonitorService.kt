package com.softwarn.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.softwarn.app.data.AppSession
import com.softwarn.app.data.AppSessionDao
import com.softwarn.app.data.WarningEvent
import com.softwarn.app.data.WarningEventDao
import com.softwarn.app.data.WarningRuleDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

@AndroidEntryPoint
class UsageMonitorService : Service() {

    @Inject lateinit var appSessionDao: AppSessionDao
    @Inject lateinit var warningRuleDao: WarningRuleDao
    @Inject lateinit var warningEventDao: WarningEventDao

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
        isRunning = true
        scheduleWatchdog()
    }

    // Runs every 15min (WorkManager's periodic-work floor) and, since it's backed by
    // WorkManager's own persistent DB, survives this service's process being killed
    // outright. KEEP means re-arming here on every service (re)creation doesn't reset
    // an already-running schedule. There's no "stop monitoring" action yet (Phase 4) —
    // when one exists, it must also call WorkManager.getInstance(context)
    // .cancelUniqueWork(WATCHDOG_WORK_NAME), or the watchdog will keep reviving a
    // service the user deliberately stopped.
    private fun scheduleWatchdog() {
        val request = PeriodicWorkRequestBuilder<UsageMonitorWatchdogWorker>(
            WATCHDOG_INTERVAL_MINUTES, TimeUnit.MINUTES
        ).build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            WATCHDOG_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
        Log.d(TAG, "scheduleWatchdog: enqueued periodic watchdog every ${WATCHDOG_INTERVAL_MINUTES}min")
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
        Log.d(TAG, "poll tick: foregroundApp=$foregroundApp currentPackage=$currentPackage")

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
        val events = usm.queryEvents(now - LOOKBACK_MS, now)
        var lastForeground: String? = null
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> lastForeground = event.packageName
                UsageEvents.Event.ACTIVITY_PAUSED, UsageEvents.Event.ACTIVITY_STOPPED ->
                    if (event.packageName == lastForeground) lastForeground = null
            }
        }
        return lastForeground
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
            val rule = warningRuleDao.getEnabledRule(packageName)
            if (rule == null) {
                Log.d(TAG, "checkWarning: no enabled rule for $packageName")
                return@launch
            }
            val durationMs = now - capturedSessionStart
            val intervalMs = rule.intervalMinutes * 60_000L
            Log.d(TAG, "checkWarning: $packageName durationMs=$durationMs intervalMs=$intervalMs")

            if (durationMs >= intervalMs && (now - capturedLastWarning) >= intervalMs) {
                fireWarning(packageName, rule.appName, durationMs)
                lastWarningTime.set(now)
            }
        }
    }

    private fun fireWarning(packageName: String, appName: String, duration: Long) {
        Log.d(TAG, "fireWarning: $packageName duration=$duration")
        serviceScope.launch {
            warningEventDao.insert(
                WarningEvent(
                    packageName = packageName,
                    appName = appName,
                    firedAt = System.currentTimeMillis(),
                    sessionDurationMs = duration
                )
            )
        }
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
        Log.d(TAG, "onDestroy")
        isRunning = false
        super.onDestroy()
        handler.removeCallbacks(pollRunnable)
        saveSession(currentPackage, sessionStartTime, System.currentTimeMillis())
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "SoftWarnMonitor"
        private const val LOOKBACK_MS = 10 * 60_000L
        private const val WATCHDOG_WORK_NAME = "usage_monitor_watchdog"
        private const val WATCHDOG_INTERVAL_MINUTES = 15L

        @Volatile var isRunning: Boolean = false
    }
}
