package com.softwarn.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.softwarn.app.ui.nav.SoftWarningNavHost
import com.softwarn.app.ui.screen.PermissionOnboardingScreen
import com.softwarn.app.ui.theme.SoftWarningTheme
import com.softwarn.app.util.PermissionChecker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SoftWarningTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val context = LocalContext.current
                    var showOnboarding by remember {
                        mutableStateOf(
                            !PermissionChecker.hasUsageAccess(context) || !PermissionChecker.hasOverlayPermission(context)
                        )
                    }
                    if (showOnboarding) {
                        PermissionOnboardingScreen(onContinue = { showOnboarding = false })
                    } else {
                        SoftWarningNavHost()
                    }
                }
            }
        }
    }
}
