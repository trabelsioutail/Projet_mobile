package com.edunova.mobile.data.remote.api

import com.edunova.mobile.data.remote.dto.*
import retrofit2.http.*

interface AdminApiService {
    
    // ==================== STATISTIQUES ====================
    
    @GET("admin/stats")
    @Headers("user-email: admin@edunova.tn")
    suspend fun getAdminStats(): ApiResponse<AdminStatsDto>
    
    // ==================== GESTION DES UTILISATEURS ====================
    
    @GET("admin/users")
    @Headers("user-email: admin@edunova.tn")
    suspend fun getAdminUsers(): ApiResponse<List<AdminUserDto>>
    
    @POST("admin/users")
    @Headers("user-email: admin@edunova.tn")
    suspend fun createUser(@Body request: CreateUserRequestDto): ApiResponse<CreateUserResponseDto>
    
    @PUT("admin/users/{id}")
    @Headers("user-email: admin@edunova.tn")
    suspend fun updateUser(@Path("id") userId: Int, @Body request: UpdateUserRequestDto): ApiResponse<UpdateUserResponseDto>
    
    @DELETE("admin/users/{id}")
    @Headers("user-email: admin@edunova.tn")
    suspend fun deleteUser(@Path("id") userId: Int): ApiResponse<MessageResponseDto>
    
    // ==================== GESTION DES COURS ====================
    
    @GET("admin/courses")
    @Headers("user-email: admin@edunova.tn")
    suspend fun getAdminCourses(): ApiResponse<List<AdminCourseDto>>
    
    @POST("admin/courses")
    @Headers("user-email: admin@edunova.tn")
    suspend fun createCourse(@Body request: CreateCourseRequestDto): ApiResponse<CreateCourseResponseDto>
    
    @PUT("admin/courses/{id}")
    @Headers("user-email: admin@edunova.tn")
    suspend fun updateCourse(@Path("id") courseId: Int, @Body request: UpdateCourseRequestDto): ApiResponse<UpdateCourseResponseDto>
    
    @DELETE("admin/courses/{id}")
    @Headers("user-email: admin@edunova.tn")
    suspend fun deleteCourse(@Path("id") courseId: Int): ApiResponse<MessageResponseDto>
    
    @PUT("admin/courses/{id}/status")
    @Headers("user-email: admin@edunova.tn")
    suspend fun toggleCourseStatus(@Path("id") courseId: Int, @Body request: ToggleCourseStatusDto): ApiResponse<MessageResponseDto>
    
    @GET("admin/teachers")
    @Headers("user-email: admin@edunova.tn")
    suspend fun getAdminTeachers(): ApiResponse<List<AdminTeacherDto>>
    
    // ==================== GESTION DES INSCRIPTIONS ====================
    
    @GET("admin/enrollments")
    @Headers("user-email: admin@edunova.tn")
    suspend fun getAdminEnrollments(): ApiResponse<List<AdminEnrollmentDto>>
    
    @POST("admin/enrollments")
    @Headers("user-email: admin@edunova.tn")
    suspend fun createEnrollment(@Body request: CreateEnrollmentRequestDto): ApiResponse<CreateEnrollmentResponseDto>
    
    @DELETE("admin/enrollments/{id}")
    @Headers("user-email: admin@edunova.tn")
    suspend fun deleteEnrollment(@Path("id") enrollmentId: Int): ApiResponse<MessageResponseDto>
    
    // ==================== GESTION DES QUIZ ====================
    
    @GET("admin/quizzes")
    @Headers("user-email: admin@edunova.tn")
    suspend fun getAdminQuizzes(): ApiResponse<List<AdminQuizDto>>
    
    @GET("admin/quizzes/{id}")
    @Headers("user-email: admin@edunova.tn")
    suspend fun getQuizDetails(@Path("id") quizId: Int): ApiResponse<AdminQuizDetailsDto>
    
    @POST("admin/quizzes")
    @Headers("user-email: admin@edunova.tn")
    suspend fun createQuiz(@Body request: CreateQuizRequestDto): ApiResponse<CreateEnrollmentResponseDto>
    
    @PUT("admin/quizzes/{id}")
    @Headers("user-email: admin@edunova.tn")
    suspend fun updateQuiz(@Path("id") quizId: Int, @Body request: UpdateQuizRequestDto): ApiResponse<AdminQuizDto>
    
    @PUT("admin/quizzes/{id}/toggle-status")
    @Headers("user-email: admin@edunova.tn")
    suspend fun toggleQuizStatus(@Path("id") quizId: Int): ApiResponse<AdminQuizDto>
    
    @DELETE("admin/quizzes/{id}")
    @Headers("user-email: admin@edunova.tn")
    suspend fun deleteQuiz(@Path("id") quizId: Int): ApiResponse<MessageResponseDto>
    
    @GET("admin/quizzes/{id}/statistics")
    @Headers("user-email: admin@edunova.tn")
    suspend fun getQuizStatistics(@Path("id") quizId: Int): ApiResponse<QuizStatisticsDto>
    
    @PUT("admin/enrollments/{id}")
    @Headers("user-email: admin@edunova.tn")
    suspend fun updateEnrollment(@Path("id") enrollmentId: Int, @Body request: UpdateEnrollmentRequestDto): ApiResponse<MessageResponseDto>
    
    // ==================== RAPPORTS ====================
    
    @GET("admin/reports/activity")
    @Headers("user-email: admin@edunova.tn")
    suspend fun getActivityReport(): ApiResponse<ActivityReportDto>
    
    // ==================== SAUVEGARDES ====================
    
    @POST("admin/backup/create")
    @Headers("user-email: admin@edunova.tn")
    suspend fun createBackup(): ApiResponse<AdminBackupDto>
    
    @GET("admin/backups")
    @Headers("user-email: admin@edunova.tn")
    suspend fun getBackups(): ApiResponse<List<AdminBackupDto>>
}