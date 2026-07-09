package com.softwarn.app.util

fun formatDurationThai(ms: Long): String {
    val totalMinutes = ms / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 -> "$hours ชม. $minutes นาที"
        else -> "$minutes นาที"
    }
}
