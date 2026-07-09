package com.softwarn.app.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap
)

@Singleton
class InstalledAppsProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val pm: PackageManager get() = context.packageManager
    private val iconCache = mutableMapOf<String, ImageBitmap>()

    suspend fun getLaunchableApps(): List<InstalledApp> = withContext(Dispatchers.Default) {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(launcherIntent, PackageManager.MATCH_DEFAULT_ONLY)
            .asSequence()
            .map { it.activityInfo.packageName }
            .distinct()
            .filterNot { it == context.packageName }
            .mapNotNull { pkg -> runCatching { toInstalledApp(pkg) }.getOrNull() }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    suspend fun resolveLabel(packageName: String): String = withContext(Dispatchers.Default) {
        runCatching { pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString() }
            .getOrDefault(packageName)
    }

    suspend fun resolveIcon(packageName: String): ImageBitmap? = withContext(Dispatchers.Default) {
        iconCache[packageName] ?: runCatching {
            pm.getApplicationIcon(packageName).toImageBitmap().also { iconCache[packageName] = it }
        }.getOrNull()
    }

    private fun toInstalledApp(packageName: String): InstalledApp {
        val appInfo = pm.getApplicationInfo(packageName, 0)
        val label = pm.getApplicationLabel(appInfo).toString()
        val icon = iconCache.getOrPut(packageName) { pm.getApplicationIcon(packageName).toImageBitmap() }
        return InstalledApp(packageName, label, icon)
    }
}

private fun Drawable.toImageBitmap(size: Int = 128): ImageBitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, size, size)
    draw(canvas)
    return bitmap.asImageBitmap()
}
