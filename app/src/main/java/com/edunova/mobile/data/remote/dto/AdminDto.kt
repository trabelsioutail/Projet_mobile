package com.edunova.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

// ==================== RESPONSE WRAPPER ====================

data class ApiResponse<T>(
    val success: Boolean,
    val data: T,
    val message: String? = null
)

data class MessageResponseDto(
    val message: String
)

// ==================== STATISTIQUES ====================

data class AdminStatsDto(
    @SerializedName("totalUsers") val totalUsers: Int,
    @SerializedName("totalCourses") val totalCourses: Int,
    @SerializedName("totalQuizzes") val totalQuizzes: Int,
    @SerializedName("totalEnrollments") val totalEnrollments: Int
)

// ==================== UTILISATEURS ====================

data class AdminUserDto(
    val id: Int,
    val first_name: String,
    val last_name: String,
    val email: String,
    val role: String,
    val created_at: String,
    val updated_at: String
)

data class CreateUserRequestDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val role: String
)

data class CreateUserResponseDto(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String
)

data class UpdateUserRequestDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String
)

data class UpdateUserResponseDto(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String
)

// ==================== COURS ====================

data class AdminCourseDto(
    val id: Int,
    val title: String,
    val description: String,
    val first_name: String?,
    val last_name: String?,
    val teacher_id: Int?,
    val enrollment_count: Int,
    val status: String?,
    val is_public: Boolean?,
    val enrollment_open: Boolean?,
    val created_at: String
)

data class CreateCourseRequestDto(
    val title: String,
    val description: String,
    val teacherId: Int,
    val status: String = "active",
    val isPublic: Boolean = true,
    val enrollmentOpen: Boolean = true
)

data class CreateCourseResponseDto(
    val id: Int,
    val title: String,
    val description: String,
    val teacherId: Int,
    val status: String
)

data class UpdateCourseRequestDto(
    val title: String,
    val description: String,
    val teacherId: Int,
    val status: String = "active",
    val isPublic: Boolean = true,
    val enrollmentOpen: Boolean = true
)

data class UpdateCourseResponseDto(
    val id: Int,
    val title: String,
    val description: String,
    val teacherId: Int,
    val status: String,
    val isPublic: Boolean,
    val enrollmentOpen: Boolean
)

data class ToggleCourseStatusDto(
    val status: String
)

data class AdminTeacherDto(
    val id: Int,
    val first_name: String,
    val last_name: String,
    val email: String
)

// ==================== INSCRIPTIONS ====================

data class AdminEnrollmentDto(
    val id: Int,
    val course_id: Int,
    val student_id: Int,
    val enrolled_at: String,
    val status: String?,
    val course_title: String,
    val course_description: String?,
    val student_first_name: String,
    val student_last_name: String,
    val student_email: String,
    val teacher_first_name: String?,
    val teacher_last_name: String?
)

data class CreateEnrollmentRequestDto(
    val courseId: Int,
    val studentId: Int,
    val status: String = "active"
)

data class UpdateEnrollmentRequestDto(
    val status: String
)

data class CreateEnrollmentResponseDto(
    val id: Int,
    val courseId: Int,
    val studentId: Int,
    val status: String
)

// ==================== QUIZ ====================

data class AdminQuizDto(
    val id: Int,
    val title: String,
    val description: String?,
    val course_id: Int,
    val time_limit: Int?,
    val max_attempts: Int?,
    val passing_score: Int?,
    val created_at: String?,
    val status: String?,
    val course_title: String?,
    val teacher_first_name: String?,
    val teacher_last_name: String?,
    val total_submissions: Int,
    val unique_students: Int,
    val average_score: Int,
    val question_count: Int,
    val teacher_name: String?
)

data class CreateQuizRequestDto(
    val title: String,
    val description: String,
    val courseId: Int,
    val timeLimit: Int = 30,
    val maxAttempts: Int = 3,
    val passingScore: Int = 60,
    val questions: List<QuizQuestionRequestDto> = emptyList()
)

data class QuizQuestionRequestDto(
    val text: String,
    val type: String = "multiple_choice",
    val points: Int = 1,
    val options: List<QuizOptionRequestDto> = emptyList(),
    val correctAnswer: String? = null
)

data class QuizOptionRequestDto(
    val text: String,
    val isCorrect: Boolean = false
)

data class UpdateQuizRequestDto(
    val title: String,
    val description: String,
    val timeLimit: Int,
    val maxAttempts: Int,
    val passingScore: Int
)

data class AdminQuizDetailsDto(
    val id: Int,
    val title: String,
    val description: String?,
    val course_id: Int,
    val time_limit: Int?,
    val max_attempts: Int?,
    val passing_score: Int?,
    val created_at: String?,
    val course_title: String?,
    val teacher_name: String?,
    val total_submissions: Int,
    val unique_students: Int,
    val average_score: Int,
    val question_count: Int,
    val questions: List<QuizQuestionDto>?,
    val recent_submissions: List<QuizSubmissionDto>?
)

data class QuizQuestionDto(
    val id: Int,
    val question_text: String,
    val question_type: String,
    val points: Int,
    val order_index: Int,
    val options: List<QuizQuestionOptionDto>?
)

data class QuizQuestionOptionDto(
    val id: Int,
    val option_text: String,
    val is_correct: Boolean,
    val order_index: Int
)

data class QuizSubmissionDto(
    val first_name: String,
    val last_name: String,
    val score: Int,
    val submitted_at: String
)

data class QuizStatisticsDto(
    val general: QuizGeneralStatsDto,
    val students: List<QuizStudentStatsDto>
)

data class QuizGeneralStatsDto(
    val total_attempts: Int,
    val unique_students: Int,
    val average_score: Double,
    val highest_score: Int,
    val lowest_score: Int,
    val passed_count: Int
)

data class QuizStudentStatsDto(
    val first_name: String,
    val last_name: String,
    val email: String,
    val score: Int,
    val submitted_at: String,
    val time_taken: Int?
)

// ==================== RAPPORTS ====================

data class ActivityReportDto(
    val dailyActivity: List<DailyActivityDto>,
    val topCourses: List<TopCourseDto>
)

data class DailyActivityDto(
    val date: String,
    val count: Int
)

data class TopCourseDto(
    val title: String,
    val enrollment_count: Int
)

// ==================== SAUVEGARDES ====================

data class AdminBackupDto(
    val id: Int,
    val name: String,
    val size: String,
    val created_at: String,
    val status: String
)