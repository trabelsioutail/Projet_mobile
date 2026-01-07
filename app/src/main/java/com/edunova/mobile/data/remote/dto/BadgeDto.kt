package com.edunova.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.edunova.mobile.domain.model.Badge
import com.edunova.mobile.domain.model.BadgeCriteriaType
import com.edunova.mobile.domain.model.StudentBadge

data class BadgeDto(
    val id: Int,
    val name: String,
    val description: String?,
    val icon: String? = null,
    @SerializedName("criteria_type")
    val criteriaType: String,
    @SerializedName("criteria_value")
    val criteriaValue: Int = 0,
    val color: String = "#FFD700",
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("earned_at")
    val earnedAt: String? = null,
    @SerializedName("is_earned")
    val isEarned: Boolean = false
) {
    fun toDomainModel(): Badge {
        return Badge(
            id = id,
            name = name,
            description = description,
            icon = icon,
            criteriaType = BadgeCriteriaType.fromString(criteriaType),
            criteriaValue = criteriaValue,
            color = color,
            createdAt = createdAt,
            earnedAt = earnedAt,
            isEarned = isEarned
        )
    }
}

data class StudentBadgeDto(
    val id: Int,
    @SerializedName("student_id")
    val studentId: Int,
    @SerializedName("badge_id")
    val badgeId: Int,
    @SerializedName("quiz_id")
    val quizId: Int? = null,
    @SerializedName("earned_at")
    val earnedAt: String,
    val badge: BadgeDto? = null
) {
    fun toDomainModel(): StudentBadge {
        return StudentBadge(
            id = id,
            studentId = studentId,
            badgeId = badgeId,
            quizId = quizId,
            earnedAt = earnedAt,
            badge = badge?.toDomainModel()
        )
    }
}