package com.edunova.mobile.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Quiz(
    val id: Int,
    val courseId: Int,
    val teacherId: Int,
    val title: String,
    val description: String?,
    val type: AssignmentType = AssignmentType.QUIZ,
    val status: AssignmentStatus = AssignmentStatus.DRAFT,
    val dueDate: String? = null,
    val timeLimit: Int? = null, // en minutes
    val maxAttempts: Int = 1,
    val passingScore: Double = 0.0,
    val totalPoints: Int = 0,
    val courseTitle: String? = null,
    val questions: List<QuizQuestion> = emptyList(),
    val submissions: List<QuizSubmission> = emptyList(),
    val userSubmission: QuizSubmission? = null,
    val isCompleted: Boolean = false,
    val createdAt: String? = null
) : Parcelable {
    fun toEntity(): com.edunova.mobile.data.local.entity.QuizEntity {
        return com.edunova.mobile.data.local.entity.QuizEntity.fromDomainModel(this)
    }
}

@Parcelize
data class QuizAnswer(
    val id: Int,
    val submissionId: Int,
    val questionId: Int,
    val answerText: String?,
    val isCorrect: Boolean = false,
    val pointsEarned: Double = 0.0
) : Parcelable

enum class SubmissionStatus {
    PENDING,
    SUBMITTED,
    GRADED;
    
    companion object {
        fun fromString(status: String): SubmissionStatus {
            return when (status.lowercase()) {
                "pending" -> PENDING
                "submitted" -> SUBMITTED
                "graded" -> GRADED
                else -> PENDING
            }
        }
    }
}