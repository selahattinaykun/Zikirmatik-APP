package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dhikr_sessions")
data class DhikrSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val count: Int,
    val timestamp: Long = System.currentTimeMillis()
)
