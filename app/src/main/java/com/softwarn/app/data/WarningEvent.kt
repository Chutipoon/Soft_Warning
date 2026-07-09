package com.softwarn.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "warning_events")
data class WarningEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val firedAt: Long,
    val sessionDurationMs: Long
)
