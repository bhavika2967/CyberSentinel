package com.cybersentinel.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cybersentinel.app.data.local.entity.IncidentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Room Incident table.
 */
@Dao
interface IncidentDao {

    @Query("SELECT * FROM incidents ORDER BY timestamp DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentIncidents(limit: Int): Flow<List<IncidentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(incident: IncidentEntity): Long

    @Delete
    suspend fun delete(incident: IncidentEntity): Int

    @Query("DELETE FROM incidents WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM incidents")
    suspend fun clearAll(): Int
}

