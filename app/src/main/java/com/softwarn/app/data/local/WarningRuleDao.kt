package com.softwarn.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WarningRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(rule: WarningRule)
    @Delete suspend fun delete(rule: WarningRule)
    @Query("SELECT * FROM warning_rules ORDER BY appName ASC")
    fun getAll(): Flow<List<WarningRule>>
    @Query("SELECT * FROM warning_rules WHERE packageName = :pkg AND isEnabled = 1")
    suspend fun getEnabledRule(pkg: String): WarningRule?
    @Query("UPDATE warning_rules SET isEnabled = :enabled WHERE packageName = :pkg")
    suspend fun setEnabled(pkg: String, enabled: Boolean)
}
