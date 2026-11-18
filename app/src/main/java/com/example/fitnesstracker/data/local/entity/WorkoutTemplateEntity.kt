package com.example.fitnesstracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_templates")
data class WorkoutTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val iconName: String = "fitness",
    val isDefault: Boolean = false,
    val createdAtEpochDay: Long = 0L
)
