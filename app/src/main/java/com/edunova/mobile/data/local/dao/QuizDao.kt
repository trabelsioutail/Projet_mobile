package com.edunova.mobile.data.local.dao

import androidx.room.*
import com.edunova.mobile.data.local.entity.QuizEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    
    @Query("SELECT * FROM quizzes ORDER BY createdAt DESC")
    fun getAllQuizzesFlow(): Flow<List<QuizEntity>>
    
    @Query("SELECT * FROM quizzes ORDER BY createdAt DESC")
    suspend fun getAllQuizzes(): List<QuizEntity>
    
    @Query("SELECT * FROM quizzes WHERE teacherId = :teacherId ORDER BY createdAt DESC")
    fun getTeacherQuizzesFlow(teacherId: Int): Flow<List<QuizEntity>>
    
    @Query("SELECT * FROM quizzes WHERE teacherId = :teacherId ORDER BY createdAt DESC")
    suspend fun getTeacherQuizzes(teacherId: Int): List<QuizEntity>
    
    @Query("SELECT * FROM quizzes WHERE courseId = :courseId ORDER BY createdAt DESC")
    fun getCourseQuizzesFlow(courseId: Int): Flow<List<QuizEntity>>
    
    @Query("SELECT * FROM quizzes WHERE courseId = :courseId ORDER BY createdAt DESC")
    suspend fun getCourseQuizzes(courseId: Int): List<QuizEntity>
    
    @Query("SELECT * FROM quizzes WHERE id = :quizId")
    suspend fun getQuizById(quizId: Int): QuizEntity?
    
    @Query("SELECT * FROM quizzes WHERE id = :quizId")
    fun getQuizByIdFlow(quizId: Int): Flow<QuizEntity?>
    
    @Query("SELECT * FROM quizzes WHERE status = 'PUBLISHED' ORDER BY dueDate ASC")
    fun getAvailableQuizzesFlow(): Flow<List<QuizEntity>>
    
    @Query("SELECT * FROM quizzes WHERE status = 'PUBLISHED' ORDER BY dueDate ASC")
    suspend fun getAvailableQuizzes(): List<QuizEntity>
    
    @Query("SELECT * FROM quizzes WHERE isCompleted = 1 ORDER BY createdAt DESC")
    fun getCompletedQuizzesFlow(): Flow<List<QuizEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizzes(quizzes: List<QuizEntity>)
    
    @Update
    suspend fun updateQuiz(quiz: QuizEntity)
    
    @Query("UPDATE quizzes SET isCompleted = :isCompleted WHERE id = :quizId")
    suspend fun updateCompletionStatus(quizId: Int, isCompleted: Boolean)
    
    @Delete
    suspend fun deleteQuiz(quiz: QuizEntity)
    
    @Query("DELETE FROM quizzes WHERE id = :quizId")
    suspend fun deleteQuizById(quizId: Int)
    
    @Query("DELETE FROM quizzes")
    suspend fun deleteAllQuizzes()
    
    @Query("SELECT * FROM quizzes WHERE isOfflineAvailable = 1")
    suspend fun getOfflineAvailableQuizzes(): List<QuizEntity>
    
    @Query("UPDATE quizzes SET isOfflineAvailable = :isAvailable WHERE id = :quizId")
    suspend fun updateOfflineAvailability(quizId: Int, isAvailable: Boolean)
}