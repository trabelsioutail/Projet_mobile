package com.edunova.mobile.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.edunova.mobile.data.local.dao.*
import com.edunova.mobile.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        CourseEntity::class,
        QuizEntity::class,
        MessageEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EduNovaDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao
    abstract fun quizDao(): QuizDao
    abstract fun messageDao(): MessageDao
    
    companion object {
        @Volatile
        private var INSTANCE: EduNovaDatabase? = null
        
        fun getDatabase(context: Context): EduNovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EduNovaDatabase::class.java,
                    "edunova_database"
                )
                .fallbackToDestructiveMigration() // Permet de recréer la DB en cas de changement de schéma
                .allowMainThreadQueries() // Temporaire pour éviter les crashes
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        // Méthode pour forcer la recréation de la base de données
        fun recreateDatabase(context: Context) {
            INSTANCE?.close()
            INSTANCE = null
            context.deleteDatabase("edunova_database")
        }
    }
}