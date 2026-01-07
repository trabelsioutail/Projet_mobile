package com.edunova.mobile.di

import android.content.Context
import androidx.room.Room
import com.edunova.mobile.data.local.dao.*
import com.edunova.mobile.data.local.database.EduNovaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideEduNovaDatabase(@ApplicationContext context: Context): EduNovaDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            EduNovaDatabase::class.java,
            "edunova_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideUserDao(database: EduNovaDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun provideCourseDao(database: EduNovaDatabase): CourseDao {
        return database.courseDao()
    }
    
    @Provides
    fun provideQuizDao(database: EduNovaDatabase): QuizDao {
        return database.quizDao()
    }
    
    @Provides
    fun provideMessageDao(database: EduNovaDatabase): MessageDao {
        return database.messageDao()
    }
}