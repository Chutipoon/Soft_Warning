package com.softwarn.app.data.local

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase

@Entity
data class WarningEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appPackageName: String,
    val warningMessage: String
)

@Database(entities = [WarningEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase()
