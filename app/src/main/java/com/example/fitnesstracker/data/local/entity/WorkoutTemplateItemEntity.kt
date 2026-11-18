package com.example.fitnesstracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_template_items",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("templateId"), Index("exerciseId")]
)
data class WorkoutTemplateItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val templateId: Long,
    val exerciseId: Long,
    val orderIndex: Int,
    val targetSets: Int = 3,
    val targetReps: Int? = 10,
    val targetWeightKg: Float? = null,
    val targetDurationMinutes: Float? = null
)
