package com.softwarn.app

import android.app.Application
import com.softwarn.app.util.SoundManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SoftWarnApp : Application() {
    @Inject lateinit var soundManager: SoundManager

    override fun onCreate() {
        super.onCreate()
        soundManager.initialize()
    }
}
