package com.edunova.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateCourseRequest(
    val title: String,
    val description: String?
)

data class UpdateCourseRequest(
    val title: String,
    val description: String?
)

data class EnrollmentRequest(
    @SerializedName("course_id")
    val courseId: Int
)

data class CreateQuizRequest(
    @SerializedName("course_id")
    val courseId: Int,
    val title: String,
    val description: String?,
    val type: String = "quiz",
    @SerializedName("time_limit")
    val timeLimit: Int? = null,
    @SerializedName("max_attempts")
    val maxAttempts: Int = 1,
    @SerializedName("passing_score")
    val passingScore: Double = 0.0,
    val questions: List<CreateQuizQuestionRequest> = emptyList()
)

data class CreateQuizQuestionRequest(
    @SerializedName("question_text")
    val questionText: String,
    @SerializedName("question_type")
    val questionType: String,
    val options: List<String> = emptyList(),
    @SerializedName("correct_answer")
    val correctAnswer: String?,
    val points: Int = 1,
    @SerializedName("order_index")
    val orderIndex: Int = 0
)