package com.edunova.mobile.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizQuestion(
    val id: Int,
    val assignmentId: Int,
    val questionText: String,
    val questionType: QuestionType,
    val options: List<QuizOption> = emptyList(),
    val correctAnswer: String? = null,
    val points: Int = 1,
    val orderIndex: Int = 0
) : Parcelable