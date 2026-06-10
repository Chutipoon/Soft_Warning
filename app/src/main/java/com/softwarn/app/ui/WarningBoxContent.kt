package com.softwarn.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softwarn.app.util.MicrocopyProvider

@Composable
fun WarningBoxContent(
    packageName: String,
    message: String,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    onAnimationFinished: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 350, easing = EaseOutCubic)
        ) + fadeIn(animationSpec = tween(durationMillis = 350)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 350, easing = EaseOutCubic)
        ) + fadeOut(animationSpec = tween(durationMillis = 350))
    ) {
        // Use DisposableEffect to detect when this specific content is removed from the composition
        // after the exit animation completes.
        DisposableEffect(Unit) {
            onDispose {
                onAnimationFinished()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Color(0xFF1A1A2E))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Content area placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF7F77DD)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        isVisible = false
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = androidx.compose.ui.graphics.SolidColor(Color.White))
                ) {
                    Text("Close", color = Color.White)
                }

                Button(
                    onClick = {
                        isVisible = false
                        onSnooze()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F77DD))
                ) {
                    Text("+5 min", color = Color.White)
                }
            }
        }
    }
}
