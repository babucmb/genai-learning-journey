package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        UserProfile::class,
        SkillGap::class,
        RoadmapWeek::class,
        TaskItem::class,
        JobItem::class,
        PracticeItem::class,
        QuizItem::class,
        QuizLog::class,
        InterviewSession::class,
        ProjectItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PathAIDatabase : RoomDatabase() {
    abstract fun dao(): PathAIDao

    companion object {
        @Volatile
        private var INSTANCE: PathAIDatabase? = null

        fun getDatabase(context: Context): PathAIDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PathAIDatabase::class.java,
                    "pathai_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
