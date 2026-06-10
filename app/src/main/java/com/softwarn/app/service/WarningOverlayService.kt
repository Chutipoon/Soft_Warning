package com.softwarn.app.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.softwarn.app.ui.PermissionRequestActivity
import com.softwarn.app.ui.WarningBoxContent
import com.softwarn.app.data.WarningRuleDao
import com.softwarn.app.util.SoundManager
import com.softwarn.app.ui.theme.SoftWarningTheme
import com.softwarn.app.util.MicrocopyProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

@AndroidEntryPoint
class WarningOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    @Inject lateinit var warningRuleDao: WarningRuleDao
    @Inject lateinit var soundManager: SoundManager

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val warningReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.softwarn.ACTION_WARNING") {
                val packageName = intent.getStringExtra("package_name") ?: return
                showOverlay(packageName)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        LocalBroadcastManager.getInstance(this).registerReceiver(
            warningReceiver,
            IntentFilter("com.softwarn.ACTION_WARNING")
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showOverlay(packageName: String) {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(this, PermissionRequestActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            return
        }

        if (overlayView != null) return

        val params = buildLayoutParams()
        val message = MicrocopyProvider.random()
        val view = ComposeView(this).apply {
            setContent {
                SoftWarningTheme {
                    WarningBoxContent(
                        packageName = packageName,
                        message = message,
                        onDismiss = { /* AnimatedVisibility will trigger onAnimationFinished */ },
                        onSnooze = { snooze(packageName) },
                        onAnimationFinished = { removeOverlay() }
                    )
                }
            }
        }

        // Set required owners for ComposeView
        view.setViewTreeLifecycleOwner(this)
        view.setViewTreeViewModelStoreOwner(this)
        view.setViewTreeSavedStateRegistryOwner(this)

        windowManager.addView(view, params)
        overlayView = view
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        serviceScope.launch {
            val rule = warningRuleDao.getEnabledRule(packageName) ?: return@launch
            soundManager.play(rule.soundResId)
        }
    }

    private fun snooze(packageName: String) {
        serviceScope.launch {
            val rule = warningRuleDao.getEnabledRule(packageName) ?: return@launch
            warningRuleDao.upsert(rule.copy(intervalMinutes = rule.intervalMinutes + 5))
        }
    }

    private fun removeOverlay() {
        Handler(Looper.getMainLooper()).post {
            overlayView?.let {
                try {
                    windowManager.removeView(it)
                } catch (e: Exception) {
                    // View might have been already removed or never added
                }
                overlayView = null
            }
        }
    }

    private fun buildLayoutParams() = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.BOTTOM
        y = 0
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
        serviceScope.cancel()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(warningReceiver)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}
