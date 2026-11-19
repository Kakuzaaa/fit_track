package com.example.fitnesstracker.data.repository

import com.example.fitnesstracker.data.local.dao.BestStatsRow
import com.example.fitnesstracker.data.local.dao.ExerciseDao
import com.example.fitnesstracker.data.local.dao.WorkoutLogDao
import com.example.fitnesstracker.data.local.dao.WorkoutTemplateDao
import com.example.fitnesstracker.data.local.entity.WorkoutTemplateEntity
import com.example.fitnesstracker.data.local.entity.WorkoutTemplateItemEntity
import com.example.fitnesstracker.data.mapper.toDomain
import com.example.fitnesstracker.data.mapper.toEntity
import com.example.fitnesstracker.domain.model.BestExerciseStats
import com.example.fitnesstracker.domain.model.DayStats
import com.example.fitnesstracker.domain.model.WorkoutLog
import com.example.fitnesstracker.domain.model.WorkoutTemplate
import com.example.fitnesstracker.domain.model.WorkoutTemplateItem
import com.example.fitnesstracker.domain.repository.WorkoutRepository
import com.example.fitnesstracker.util.Formatters
import com.example.fitnesstracker.util.OneRepMaxCalculator
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class WorkoutRepositoryImpl(
    private val dao: WorkoutLogDao,
    private val templateDao: WorkoutTemplateDao? = null,
    private val exerciseDao: ExerciseDao? = null
) : WorkoutRepository {

    override fun observeLogsForDay(dateEpochDay: Long): Flow<List<WorkoutLog>> =
        dao.observeLogsForDay(dateEpochDay).map { list -> list.map { it.toDomain() } }

    override fun observeLogsForExercise(exerciseId: Long): Flow<List<WorkoutLog>> =
        dao.observeLogsForExercise(exerciseId).map { list -> list.map { it.toDomain() } }

    override suspend fun getLogsForExercise(exerciseId: Long): List<WorkoutLog> =
        dao.getLogsForExercise(exerciseId).map { it.toDomain() }

    override fun observeDayStats(): Flow<List<DayStats>> =
        dao.observeDayStats().map { list -> list.map { it.toDomain() } }

    override suspend fun getBestStats(): List<BestExerciseStats> {
        val baseList = dao.getBestStats().map { row: BestStatsRow -> row.toDomain() }
        val allLogs = dao.getAllLogs()
        val oneRmsByExercise = allLogs.groupBy { it.exerciseId }.mapValues { (_, logs) ->
            logs.mapNotNull { log ->
                val w = log.weightKg
                val r = log.reps
                if (w != null && w > 0f && r != null && r > 0) {
                    OneRepMaxCalculator.calculate1RmEpley(w, r)
                } else null
            }.maxOrNull()
        }
        return baseList.map { stats ->
            stats.copy(best1RmKg = oneRmsByExercise[stats.exerciseId])
        }
    }

    override suspend fun getAllLogs(): List<WorkoutLog> =
        dao.getAllLogs().map { it.toDomain() }

    override fun observeAllLogs(): Flow<List<WorkoutLog>> =
        dao.observeAllLogs().map { list -> list.map { it.toDomain() } }

    override fun searchLogs(query: String): Flow<List<WorkoutLog>> =
        observeAllLogs().map { logs ->
            val q = query.trim().lowercase()
            if (q.isBlank()) {
                emptyList()
            } else {
                logs.filter { log ->
                    log.exerciseNameSnapshot.lowercase().contains(q) ||
                    Formatters.dayTitle(log.dateEpochDay).lowercase().contains(q) ||
                    LocalDate.ofEpochDay(log.dateEpochDay).toString().contains(q) ||
                    (log.weightKg != null && (Formatters.number(log.weightKg).contains(q) || "${log.weightKg.toInt()}".contains(q))) ||
                    (log.reps != null && "${log.reps}".contains(q)) ||
                    (log.distanceKm != null && Formatters.number(log.distanceKm).contains(q))
                }
            }
        }

    override suspend fun getLastUsedWeight(exerciseId: Long): Float? =
        dao.getLastUsedWeight(exerciseId)

    override suspend fun insert(log: WorkoutLog): Long =
        dao.insert(log.toEntity())

    override suspend fun update(log: WorkoutLog) {
        dao.update(log.toEntity())
    }

    override suspend fun delete(log: WorkoutLog) {
        dao.delete(log.toEntity())
    }

    override suspend fun clearAllLogs() {
        dao.clearAll()
    }

    // ── Шаблоны тренировок ──────────────────────────────────────

    override fun observeTemplates(): Flow<List<WorkoutTemplate>> {
        val tDao = templateDao ?: return flowOf(emptyList())
        return tDao.observeAllTemplates().map { templates ->
            templates.map { entity ->
                val items = tDao.getItemsForTemplate(entity.id).map { item ->
                    val exercise = exerciseDao?.getById(item.exerciseId)
                    val name = exercise?.toDomain()?.displayName ?: "Упражнение #${item.exerciseId}"
                    WorkoutTemplateItem(
                        id = item.id,
                        templateId = item.templateId,
                        exerciseId = item.exerciseId,
                        exerciseName = name,
                        orderIndex = item.orderIndex,
                        targetSets = item.targetSets,
                        targetReps = item.targetReps,
                        targetWeightKg = item.targetWeightKg,
                        targetDurationMinutes = item.targetDurationMinutes
                    )
                }
                WorkoutTemplate(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    iconName = entity.iconName,
                    isDefault = entity.isDefault,
                    items = items
                )
            }
        }
    }

    override suspend fun getTemplate(id: Long): WorkoutTemplate? {
        val tDao = templateDao ?: return null
        val entity = tDao.getTemplateById(id) ?: return null
        val items = tDao.getItemsForTemplate(entity.id).map { item ->
            val exercise = exerciseDao?.getById(item.exerciseId)
            val name = exercise?.toDomain()?.displayName ?: "Упражнение #${item.exerciseId}"
            WorkoutTemplateItem(
                id = item.id,
                templateId = item.templateId,
                exerciseId = item.exerciseId,
                exerciseName = name,
                orderIndex = item.orderIndex,
                targetSets = item.targetSets,
                targetReps = item.targetReps,
                targetWeightKg = item.targetWeightKg,
                targetDurationMinutes = item.targetDurationMinutes
            )
        }
        return WorkoutTemplate(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            iconName = entity.iconName,
            isDefault = entity.isDefault,
            items = items
        )
    }

    override suspend fun saveTemplate(template: WorkoutTemplate): Long {
        val tDao = templateDao ?: return 0L
        val entity = WorkoutTemplateEntity(
            id = template.id,
            title = template.title,
            description = template.description,
            iconName = template.iconName,
            isDefault = template.isDefault,
            createdAtEpochDay = System.currentTimeMillis() / (1000L * 60 * 60 * 24)
        )
        val items = template.items.mapIndexed { index, item ->
            WorkoutTemplateItemEntity(
                id = item.id,
                templateId = template.id,
                exerciseId = item.exerciseId,
                orderIndex = index,
                targetSets = item.targetSets,
                targetReps = item.targetReps,
                targetWeightKg = item.targetWeightKg,
                targetDurationMinutes = item.targetDurationMinutes
            )
        }
        return if (template.id == 0L) {
            tDao.insertTemplateWithItems(entity, items)
        } else {
            tDao.updateTemplate(entity)
            tDao.deleteItemsForTemplate(template.id)
            tDao.insertItems(items.map { it.copy(templateId = template.id) })
            template.id
        }
    }

    override suspend fun deleteTemplate(id: Long) {
        templateDao?.deleteTemplateById(id)
    }
}
