package com.example.fitnesstracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fitnesstracker.data.local.dao.ExerciseDao
import com.example.fitnesstracker.data.local.dao.WorkoutLogDao
import com.example.fitnesstracker.data.local.entity.ExerciseEntity
import com.example.fitnesstracker.data.local.entity.WorkoutLogEntity
import com.example.fitnesstracker.data.prepopulate.DefaultExercises
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [ExerciseEntity::class, WorkoutLogEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao

    abstract fun workoutLogDao(): WorkoutLogDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val seedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context).also { INSTANCE = it }
            }

        private fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "fitness_tracker.db")
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Предзаполнение справочника упражнений при первом запуске.
                        seedScope.launch {
                            INSTANCE?.exerciseDao()?.insertAll(DefaultExercises.all)
                        }
                    }
                })
                .build()
    }
}
