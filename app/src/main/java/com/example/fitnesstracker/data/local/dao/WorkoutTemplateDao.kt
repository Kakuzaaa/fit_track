package com.example.fitnesstracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.fitnesstracker.data.local.entity.WorkoutTemplateEntity
import com.example.fitnesstracker.data.local.entity.WorkoutTemplateItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutTemplateDao {

    @Query("SELECT * FROM workout_templates ORDER BY isDefault DESC, id ASC")
    fun observeAllTemplates(): Flow<List<WorkoutTemplateEntity>>

    @Query("SELECT * FROM workout_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): WorkoutTemplateEntity?

    @Query("SELECT * FROM workout_template_items WHERE templateId = :templateId ORDER BY orderIndex ASC")
    fun observeItemsForTemplate(templateId: Long): Flow<List<WorkoutTemplateItemEntity>>

    @Query("SELECT * FROM workout_template_items WHERE templateId = :templateId ORDER BY orderIndex ASC")
    suspend fun getItemsForTemplate(templateId: Long): List<WorkoutTemplateItemEntity>

    @Query("SELECT COUNT(*) FROM workout_templates")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkoutTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<WorkoutTemplateItemEntity>)

    @Update
    suspend fun updateTemplate(template: WorkoutTemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: WorkoutTemplateEntity)

    @Query("DELETE FROM workout_templates WHERE id = :id")
    suspend fun deleteTemplateById(id: Long)

    @Query("DELETE FROM workout_template_items WHERE templateId = :templateId")
    suspend fun deleteItemsForTemplate(templateId: Long)

    @Transaction
    suspend fun insertTemplateWithItems(
        template: WorkoutTemplateEntity,
        items: List<WorkoutTemplateItemEntity>
    ): Long {
        val templateId = insertTemplate(template)
        val itemsWithId = items.map { it.copy(templateId = templateId) }
        insertItems(itemsWithId)
        return templateId
    }
}
