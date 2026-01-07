package com.edunova.mobile.data.remote.api

import com.edunova.mobile.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface CourseApiService {
    
    @GET("courses")
    suspend fun getCourses(): Response<List<CourseDto>>
    
    @GET("courses/{id}")
    suspend fun getCourseById(@Path("id") courseId: Int): Response<CourseDto>
    
    @POST("courses")
    suspend fun createCourse(@Body request: CreateCourseRequest): Response<CourseDto>
    
    @PUT("courses/{id}")
    suspend fun updateCourse(
        @Path("id") courseId: Int,
        @Body request: UpdateCourseRequest
    ): Response<CourseDto>
    
    @DELETE("courses/{id}")
    suspend fun deleteCourse(@Path("id") courseId: Int): Response<ApiResponse<Unit>>
    
    // Endpoints pour les étudiants
    @GET("student/courses")
    suspend fun getStudentCourses(): Response<List<CourseDto>>
    
    @POST("student/courses/enroll")
    suspend fun enrollInCourse(@Body request: EnrollmentRequest): Response<ApiResponse<Unit>>
    
    @DELETE("student/courses/{id}/unenroll")
    suspend fun unenrollFromCourse(@Path("id") courseId: Int): Response<ApiResponse<Unit>>
    
    // Dashboard et statistiques
    @GET("courses/stats/teacher")
    suspend fun getTeacherStats(): Response<ApiResponse<Map<String, Any>>>
    
    @GET("courses/dashboard/stats")
    suspend fun getDashboardStats(): Response<ApiResponse<Map<String, Any>>>
}