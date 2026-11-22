package com.example.fitnesstracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fitnesstracker.data.local.dao.ExerciseDao
import com.example.fitnesstracker.data.local.dao.WeightLogDao
import com.example.fitnesstracker.data.local.dao.WorkoutLogDao
import com.example.fitnesstracker.data.local.dao.WorkoutTemplateDao
import com.example.fitnesstracker.data.local.entity.ExerciseEntity
import com.example.fitnesstracker.data.local.entity.WeightLogEntity
import com.example.fitnesstracker.data.local.entity.WorkoutLogEntity
import com.example.fitnesstracker.data.local.entity.WorkoutTemplateEntity
import com.example.fitnesstracker.data.local.entity.WorkoutTemplateItemEntity
import com.example.fitnesstracker.data.prepopulate.DefaultExercises
import com.example.fitnesstracker.data.prepopulate.DefaultWorkoutTemplates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutLogEntity::class,
        WorkoutTemplateEntity::class,
        WorkoutTemplateItemEntity::class,
        WeightLogEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao

    abstract fun workoutLogDao(): WorkoutLogDao

    abstract fun workoutTemplateDao(): WorkoutTemplateDao

    abstract fun weightLogDao(): WeightLogDao

    companion object {

        /** v1 → v2: добавлено поле расстояния для кардио (durationMinutes стал необязательным). */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workout_logs ADD COLUMN distanceKm REAL")
            }
        }

        /** v2 → v3: добавлены шаблоны тренировок и их упражнения с предустановленными комплексами. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `workout_templates` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `iconName` TEXT NOT NULL,
                        `isDefault` INTEGER NOT NULL,
                        `createdAtEpochDay` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `workout_template_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `templateId` INTEGER NOT NULL,
                        `exerciseId` INTEGER NOT NULL,
                        `orderIndex` INTEGER NOT NULL,
                        `targetSets` INTEGER NOT NULL,
                        `targetReps` INTEGER,
                        `targetWeightKg` REAL,
                        `targetDurationMinutes` REAL,
                        FOREIGN KEY(`templateId`) REFERENCES `workout_templates`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_template_items_templateId` ON `workout_template_items`(`templateId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_template_items_exerciseId` ON `workout_template_items`(`exerciseId`)")

                db.execSQL("INSERT OR IGNORE INTO `workout_templates` (`id`, `title`, `description`, `iconName`, `isDefault`, `createdAtEpochDay`) VALUES (1, 'Базовый воркаут', 'Комплекс со своим весом: турник, брусья, отжимания и приседания', 'fitness', 1, 0)")
                db.execSQL("INSERT OR IGNORE INTO `workout_template_items` (`id`, `templateId`, `exerciseId`, `orderIndex`, `targetSets`, `targetReps`, `targetWeightKg`, `targetDurationMinutes`) VALUES (1, 1, 8, 0, 4, 8, NULL, NULL)")
                db.execSQL("INSERT OR IGNORE INTO `workout_template_items` (`id`, `templateId`, `exerciseId`, `orderIndex`, `targetSets`, `targetReps`, `targetWeightKg`, `targetDurationMinutes`) VALUES (2, 1, 4, 1, 4, 10, NULL, NULL)")
                db.execSQL("INSERT OR IGNORE INTO `workout_template_items` (`id`, `templateId`, `exerciseId`, `orderIndex`, `targetSets`, `targetReps`, `targetWeightKg`, `targetDurationMinutes`) VALUES (3, 1, 1, 2, 4, 15, NULL, NULL)")
                db.execSQL("INSERT OR IGNORE INTO `workout_template_items` (`id`, `templateId`, `exerciseId`, `orderIndex`, `targetSets`, `targetReps`, `targetWeightKg`, `targetDurationMinutes`) VALUES (4, 1, 5, 3, 4, 20, NULL, NULL)")
                db.execSQL("INSERT OR IGNORE INTO `workout_template_items` (`id`, `templateId`, `exerciseId`, `orderIndex`, `targetSets`, `targetReps`, `targetWeightKg`, `targetDurationMinutes`) VALUES (5, 1, 24, 4, 3, NULL, NULL, 1.0)")

                db.execSQL("INSERT OR IGNORE INTO `workout_templates` (`id`, `title`, `description`, `iconName`, `isDefault`, `createdAtEpochDay`) VALUES (2, 'Верх тела & Гантели', 'Грудь, плечи, бицепс и спина с гантелями', 'fire', 1, 0)")
                db.execSQL("INSERT OR IGNORE INTO `workout_template_items` (`id`, `templateId`, `exerciseId`, `orderIndex`, `targetSets`, `targetReps`, `targetWeightKg`, `targetDurationMinutes`) VALUES (6, 2, 16, 0, 4, 10, 16.0, NULL)")
                db.execSQL("INSERT OR IGNORE INTO `workout_template_items` (`id`, `templateId`, `exerciseId`, `orderIndex`, `targetSets`, `targetReps`, `targetWeightKg`, `targetDurationMinutes`) VALUES (7, 2, 18, 1, 4, 10, 16.0, NULL)")
                db.execSQL("INSERT OR IGNORE INTO `workout_template_items` (`id`, `templateId`, `exerciseId`, `orderIndex`, `targetSets`, `targetReps`, `targetWeightKg`, `targetDurationMinutes`) VALUES (8, 2, 19, 2, 3, 12, 8.0, NULL)")
                db.execSQL("INSERT OR IGNORE INTO `workout_template_items` (`id`, `templateId`, `exerciseId`, `orderIndex`, `targetSets`, `targetReps`, `targetWeightKg`, `targetDurationMinutes`) VALUES (9, 2, 13, 3, 3, 10, 12.0, NULL)")
                db.execSQL("INSERT OR IGNORE INTO `workout_template_items` (`id`, `templateId`, `exerciseId`, `orderIndex`, `targetSets`, `targetReps`, `targetWeightKg`, `targetDurationMinutes`) VALUES (10, 2, 14, 4, 3, 10, 12.0, NULL)")

                db.execSQL("INSERT OR IGNORE INTO `workout_templates` (`id`, `title`, `description`, `iconName`, `isDefault`, `createdAtEpochDay`) VALUES (3, 'Пресс & Кор', 'Интенсивная тренировка мышц живота и стабилизаторов', 'abs', 1, 0)")
                db.execSQL("INSERT OR IGNORE INTO `workout_template_items` (`id`, `templateId`, `exerciseId`, `orderIndex`, `targetSets`, `targetReps`, `targetWeightKg`, `targetDurationMinutes`) VALUES (11, 3, 22, 0, 3, 15, NULL, NULL)")
                db.execSQL("INSERT OR IGNORE INTO `workout_template_items` (`id`, `templateId`, `exerciseId`, `orderIndex`, `targetSets`, `targetReps`, `targetWeightKg`, `targetDurationMinutes`) VALUES (12, 3, 21, 1, 3, 20, NULL, NULL)")
                db.execSQL("INSERT OR IGNORE INTO `workout_template_items` (`id`, `templateId`, `exerciseId`, `orderIndex`, `targetSets`, `targetReps`, `targetWeightKg`, `targetDurationMinutes`) VALUES (13, 3, 23, 2, 3, 25, NULL, NULL)")
                db.execSQL("INSERT OR IGNORE INTO `workout_template_items` (`id`, `templateId`, `exerciseId`, `orderIndex`, `targetSets`, `targetReps`, `targetWeightKg`, `targetDurationMinutes`) VALUES (14, 3, 24, 3, 3, NULL, NULL, 1.5)")
            }
        }

        /** v3 → v4: добавлен дневник взвешиваний веса тела. */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `weight_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `dateEpochDay` INTEGER NOT NULL,
                        `weightKg` REAL NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val seedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context).also { INSTANCE = it }
            }

        private fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "fitness_tracker.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Предзаполнение справочника упражнений и базовых шаблонов при первом запуске.
                        seedScope.launch {
                            INSTANCE?.exerciseDao()?.insertAll(DefaultExercises.all)
                            val templateDao = INSTANCE?.workoutTemplateDao()
                            if (templateDao != null && templateDao.count() == 0) {
                                for ((tmpl, items) in DefaultWorkoutTemplates.templates) {
                                    templateDao.insertTemplateWithItems(tmpl, items)
                                }
                            }
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Синхронизация новых упражнений при обновлении приложения (OnConflictStrategy.IGNORE)
                        seedScope.launch {
                            INSTANCE?.exerciseDao()?.insertAll(DefaultExercises.all)
                        }
                    }
                })
                .build()
    }
}
