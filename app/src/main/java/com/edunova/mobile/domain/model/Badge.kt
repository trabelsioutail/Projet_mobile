package com.edunova.mobile.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Badge(
    val id: Int,
    val name: String,
    val description: String?,
    val icon: String? = null,
    val criteriaType: BadgeCriteriaType,
    val criteriaValue: Int = 0,
    val color: String = "#FFD700",
    val createdAt: String? = null,
    val earnedAt: String? = null,
    val isEarned: Boolean = false
) : Parcelable

@Parcelize
data class StudentBadge(
    val id: Int,
    val studentId: Int,
    val badgeId: Int,
    val quizId: Int? = null,
    val earnedAt: String,
    val badge: Badge? = null
) : Parcelable

enum class BadgeCriteriaType {
    QUIZ_COUNT,
    SCORE_AVERAGE,
    STREAK,
    PERFECT_SCORE;
    
    companion object {
        fun fromString(type: String): BadgeCriteriaType {
            return when (type.lowercase()) {
                "quiz_count" -> QUIZ_COUNT
                "score_average" -> SCORE_AVERAGE
                "streak" -> STREAK
                "perfect_score" -> PERFECT_SCORE
                else -> QUIZ_COUNT
            }
        }
    }
}