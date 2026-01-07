package com.edunova.mobile.domain.model

data class Enrollment(
    val id: Int,
    val studentId: Int,
    val courseId: Int,
    val enrolledAt: String
)