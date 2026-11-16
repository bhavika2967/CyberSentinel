package com.cybersentinel.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity logging threats, scam calls, and phishing attempts intercepted by Cyber Sentinel.
 */
@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "caller_or_sender")
    val callerOrSender: String,

    @ColumnInfo(name = "scam_type")
    val scamType: String,

    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Int,

    @ColumnInfo(name = "threat_level")
    val threatLevel: String,

    @ColumnInfo(name = "transcript_snippet")
    val transcriptSnippet: String,

    @ColumnInfo(name = "action_taken")
    val actionTaken: String,

    @ColumnInfo(name = "guardian_alert_sent")
    val guardianAlertSent: Boolean = false
)

