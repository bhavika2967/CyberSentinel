package com.cybersentinel.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cybersentinel.app.data.local.entity.GuardianEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Room Guardian table. Full CRUD operations.
 */
@Dao
interface GuardianDao {

    @Query("SELECT * FROM guardians ORDER BY createdAt DESC")
    fun getAllGuardians(): Flow<List<GuardianEntity>>

    @Query("SELECT * FROM guardians ORDER BY createdAt DESC")
    suspend fun getAllGuardiansSync(): List<GuardianEntity>

    @Query("SELECT * FROM guardians WHERE id = :id LIMIT 1")
    suspend fun getGuardianById(id: Long): GuardianEntity?

    @Query("SELECT COUNT(*) FROM guardians")
    fun getGuardianCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(guardian: GuardianEntity): Long

    @Update
    suspend fun update(guardian: GuardianEntity): Int

    @Delete
    suspend fun delete(guardian: GuardianEntity): Int

    @Query("DELETE FROM guardians WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM guardians")
    suspend fun clearAll(): Int
}

