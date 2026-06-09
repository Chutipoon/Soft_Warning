package com.softwarn.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "warning_rules")
data class WarningRule(
    @PrimaryKey val packageName: String,
    val appName: String,
    val appIconBase64: String = "",
    val intervalMinutes: Int = 20,
    val isEnabled: Boolean = true,
    val soundResId: Int = 0,
    val contentPackId: String = "free_minimal",
    val createdAt: Long = System.currentTimeMillis()
)
