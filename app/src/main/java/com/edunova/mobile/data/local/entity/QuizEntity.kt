package com.edunova.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.edunova.mobile.domain.model.AssignmentStatus
import com.edunova.mobile.domain.model.AssignmentType
import com.edunova.mobile.domain.model.Quiz

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey
    val id: Int,
    val courseId: Int,
    val teacherId: Int,
    val title: String,
    val description: String?,
    val type: String = AssignmentType.QUIZ.name,
    val status: String = AssignmentStatus.DRAFT.name,
    val dueDate: String? = null,
    val timeLimit: Int? = null,
    val maxAttempts: Int = 1,
    val passingScore: Double = 0.0,
    val totalPoints: Int = 0,
    val courseTitle: String? = null,
    val isCompleted: Boolean = false,
    val createdAt: String? = null,
    val lastSyncAt: Long = System.currentTimeMillis(),
    val isOfflineAvailable: Boolean = false
) {
    fun toDomainModel(): Quiz {
        return Quiz(
            id = id,
            courseId = courseId,
            teacherId = teacherId,
            title = title,
            description = description,
            type = AssignmentType.fromString(type),
            status = AssignmentStatus.fromString(status),
            dueDate = dueDate,
            timeLimit = timeLimit,
            maxAttempts = maxAttempts,
            passingScore = passingScore,
            totalPoints = totalPoints,
            courseTitle = courseTitle,
            isCompleted = isCompleted,
            createdAt = createdAt
        )
    }
    
    companion object {
        fun fromDomainModel(quiz: Quiz): QuizEntity {
            return QuizEntity(
                id = quiz.id,
                courseId = quiz.courseId,
                teacherId = quiz.teacherId,
                title = quiz.title,
                description = quiz.description,
                type = quiz.type.name,
                status = quiz.status.name,
                dueDate = quiz.dueDate,
                timeLimit = quiz.timeLimit,
                maxAttempts = quiz.maxAttempts,
                passingScore = quiz.passingScore,
                totalPoints = quiz.totalPoints,
                courseTitle = quiz.courseTitle,
                isCompleted = quiz.isCompleted,
                createdAt = quiz.createdAt
            )
        }
    }
}