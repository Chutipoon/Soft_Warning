package com.softwarn.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.softwarn.app.service.WarningOverlayService
import com.softwarn.app.ui.theme.SoftWarningTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SoftWarningTheme {
                Surface {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Hello Soft Warning!")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            // 1. Start the service
                            startService(Intent(this@MainActivity, WarningOverlayService::class.java))

                            // 2. Fire a test broadcast
                            val intent = Intent("com.softwarn.ACTION_WARNING").apply {
                                putExtra("package_name", "com.android.chrome")
                            }
                            LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(intent)
                        }) {
                            Text("Test Overlay")
                        }
                    }
                }
            }
        }
    }
}
