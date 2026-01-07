package com.edunova.mobile.data.local.dao

import androidx.room.*
import com.edunova.mobile.data.local.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    
    @Query("SELECT * FROM courses ORDER BY createdAt DESC")
    fun getAllCoursesFlow(): Flow<List<CourseEntity>>
    
    @Query("SELECT * FROM courses ORDER BY createdAt DESC")
    suspend fun getAllCourses(): List<CourseEntity>
    
    @Query("SELECT * FROM courses WHERE teacherId = :teacherId ORDER BY createdAt DESC")
    fun getTeacherCoursesFlow(teacherId: Int): Flow<List<CourseEntity>>
    
    @Query("SELECT * FROM courses WHERE teacherId = :teacherId ORDER BY createdAt DESC")
    suspend fun getTeacherCourses(teacherId: Int): List<CourseEntity>
    
    @Query("SELECT * FROM courses WHERE isEnrolled = 1 ORDER BY createdAt DESC")
    fun getEnrolledCoursesFlow(): Flow<List<CourseEntity>>
    
    @Query("SELECT * FROM courses WHERE isEnrolled = 1 ORDER BY createdAt DESC")
    suspend fun getEnrolledCourses(): List<CourseEntity>
    
    @Query("SELECT * FROM courses WHERE id = :courseId")
    suspend fun getCourseById(courseId: Int): CourseEntity?
    
    @Query("SELECT * FROM courses WHERE id = :courseId")
    fun getCourseByIdFlow(courseId: Int): Flow<CourseEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)
    
    @Update
    suspend fun updateCourse(course: CourseEntity)
    
    @Query("UPDATE courses SET isEnrolled = :isEnrolled, enrollmentStatus = :status WHERE id = :courseId")
    suspend fun updateEnrollmentStatus(courseId: Int, isEnrolled: Boolean, status: String)
    
    @Query("UPDATE courses SET progress = :progress WHERE id = :courseId")
    suspend fun updateProgress(courseId: Int, progress: Int)
    
    @Delete
    suspend fun deleteCourse(course: CourseEntity)
    
    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteCourseById(courseId: Int)
    
    @Query("DELETE FROM courses")
    suspend fun deleteAllCourses()
    
    @Query("SELECT * FROM courses WHERE isOfflineAvailable = 1")
    suspend fun getOfflineAvailableCourses(): List<CourseEntity>
    
    @Query("UPDATE courses SET isOfflineAvailable = :isAvailable WHERE id = :courseId")
    suspend fun updateOfflineAvailability(courseId: Int, isAvailable: Boolean)
}