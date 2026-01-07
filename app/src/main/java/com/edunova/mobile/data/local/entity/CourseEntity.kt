package com.edunova.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.edunova.mobile.domain.model.Course
import com.edunova.mobile.domain.model.EnrollmentStatus

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val description: String?,
    val teacherId: Int,
    val teacherName: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val contentsCount: Int = 0,
    val studentsCount: Int = 0,
    val isEnrolled: Boolean = false,
    val enrollmentStatus: String = EnrollmentStatus.NOT_ENROLLED.name,
    val progress: Int = 0,
    val lastSyncAt: Long = System.currentTimeMillis(),
    val isOfflineAvailable: Boolean = false
) {
    fun toDomainModel(): Course {
        return Course(
            id = id,
            title = title,
            description = description,
            teacherId = teacherId,
            teacherName = teacherName,
            createdAt = createdAt,
            updatedAt = updatedAt,
            contentsCount = contentsCount,
            studentsCount = studentsCount,
            isEnrolled = isEnrolled,
            enrollmentStatus = EnrollmentStatus.fromString(enrollmentStatus),
            progress = progress.toFloat(),
            status = "active",
            isPublic = true,
            enrollmentOpen = true
        )
    }
    
    companion object {
        fun fromDomainModel(course: Course): CourseEntity {
            return CourseEntity(
                id = course.id,
                title = course.title,
                description = course.description,
                teacherId = course.teacherId,
                teacherName = course.teacherName,
                createdAt = course.createdAt,
                updatedAt = course.updatedAt,
                contentsCount = course.contentsCount,
                studentsCount = course.studentsCount,
                isEnrolled = course.isEnrolled,
                enrollmentStatus = course.enrollmentStatus.name,
                progress = course.progress.toInt()
            )
        }
    }
}