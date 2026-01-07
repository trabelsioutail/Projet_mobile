package com.edunova.mobile.data.remote.api

import com.edunova.mobile.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface QuizApiService {
    
    // Endpoints pour les enseignants
    @GET("assignments")
    suspend fun getTeacherQuizzes(): Response<List<QuizDto>>
    
    @GET("assignments/{id}")
    suspend fun getQuizById(@Path("id") quizId: Int): Response<QuizDto>
    
    @POST("assignments")
    suspend fun createQuiz(@Body request: CreateQuizRequest): Response<QuizDto>
    
    @DELETE("assignments/{id}")
    suspend fun deleteQuiz(@Path("id") quizId: Int): Response<ApiResponse<Unit>>
    
    @GET("assignments/submissions/{submissionId}/grade")
    suspend fun getSubmissionDetails(@Path("submissionId") submissionId: Int): Response<StudentQuizSubmissionDto>
    
    // Endpoints pour les étudiants
    @GET("student/quizzes")
    suspend fun getStudentQuizzes(): Response<List<QuizDto>>
    
    @GET("student/quizzes/{id}")
    suspend fun getStudentQuizById(@Path("id") quizId: Int): Response<QuizDto>
    
    @POST("student/quizzes/{id}/submit")
    suspend fun submitQuiz(
        @Path("id") quizId: Int,
        @Body request: SubmitQuizRequest
    ): Response<StudentQuizSubmissionDto>
    
    @GET("student/badges")
    suspend fun getStudentBadges(): Response<List<BadgeDto>>
}