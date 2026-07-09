package com.softwarn.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WarningEventDao {
    @Insert suspend fun insert(event: WarningEvent)
    @Query("SELECT * FROM warning_events ORDER BY firedAt DESC LIMIT 200")
    fun getRecent(): Flow<List<WarningEvent>>
}
