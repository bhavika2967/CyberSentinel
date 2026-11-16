package com.cybersentinel.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cybersentinel.app.data.local.dao.GuardianDao
import com.cybersentinel.app.data.local.dao.IncidentDao
import com.cybersentinel.app.data.local.entity.GuardianEntity
import com.cybersentinel.app.data.local.entity.IncidentEntity

/**
 * Cyber Sentinel Room Database.
 * Starts completely empty with ZERO pre-seeded records.
 */
@Database(
    entities = [GuardianEntity::class, IncidentEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun guardianDao(): GuardianDao
    abstract fun incidentDao(): IncidentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cybersentinel_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

