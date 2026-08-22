package com.wakecalc.alarm.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WakeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: WakeLog)

    @Query("SELECT * FROM wake_log ORDER BY dayEpoch DESC")
    fun observeAll(): Flow<List<WakeLog>>

    @Query("SELECT * FROM wake_log ORDER BY dayEpoch DESC")
    suspend fun getAll(): List<WakeLog>

    @Query("SELECT * FROM wake_log WHERE dayEpoch >= :fromDay ORDER BY dayEpoch ASC")
    suspend fun since(fromDay: Long): List<WakeLog>

    @Query("SELECT COUNT(*) FROM wake_log")
    suspend fun count(): Int
}
