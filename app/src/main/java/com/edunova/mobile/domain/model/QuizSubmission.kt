package com.edunova.mobile.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizSubmission(
    val id: Int,
    val quizId: Int,
    val studentId: Int,
    val answers: Map<Int, String>,
    val score: Int,
    val earnedPoints: Int,
    val totalPoints: Int,
    val passed: Boolean,
    val submittedAt: Long,
    val attemptNumber: Int = 1
) : Parcelable