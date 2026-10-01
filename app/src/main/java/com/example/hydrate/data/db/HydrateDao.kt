package com.example.hydrate.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.hydrate.data.model.CustomMessage
import com.example.hydrate.data.model.WaterLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HydrateDao {
    // Water Logs
    @Insert
    suspend fun insertWaterLog(log: WaterLog)

    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC")
    fun getAllWaterLogs(): Flow<List<WaterLog>>

    @Query("SELECT * FROM water_logs WHERE date = :date ORDER BY timestamp DESC")
    fun getWaterLogsForDate(date: String): Flow<List<WaterLog>>
    
    @Query("SELECT SUM(amountMl) FROM water_logs WHERE date = :date")
    fun getTotalWaterForDate(date: String): Flow<Int?>

    @Query("SELECT COUNT(*) FROM water_logs WHERE date = :date")
    fun getDrinkCountForDate(date: String): Flow<Int>

    @Query("DELETE FROM water_logs")
    suspend fun clearAllWaterLogs()

    // Custom Messages
    @Insert
    suspend fun insertCustomMessage(message: CustomMessage)

    @Update
    suspend fun updateCustomMessage(message: CustomMessage)

    @Delete
    suspend fun deleteCustomMessage(message: CustomMessage)

    @Query("SELECT * FROM custom_messages")
    fun getAllCustomMessages(): Flow<List<CustomMessage>>
}
