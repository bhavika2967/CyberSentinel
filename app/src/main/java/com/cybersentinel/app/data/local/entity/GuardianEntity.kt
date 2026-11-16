package com.cybersentinel.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity representing a trusted emergency guardian configured by the user.
 * Begins completely empty with ZERO pre-seeded records.
 */
@Entity(tableName = "guardians")
data class GuardianEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "relationship")
    val relationship: String,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,

    @ColumnInfo(name = "is_emergency_contact")
    val isEmergencyContact: Boolean = true,

    @ColumnInfo(name = "notify_on_scam_detected")
    val notifyOnScamDetected: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

