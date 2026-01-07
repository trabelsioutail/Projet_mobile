package com.edunova.mobile.data.remote.api

import com.edunova.mobile.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("auth/google-login")
    suspend fun googleLogin(@Body request: GoogleAuthRequest): Response<AuthResponse>
    
    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>
    
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<Unit>>
    
    @POST("auth/reset-password/{token}")
    suspend fun resetPassword(
        @Path("token") token: String,
        @Body request: ResetPasswordRequest
    ): Response<ApiResponse<Unit>>
    
    @GET("auth/verify-email/{token}")
    suspend fun verifyEmail(@Path("token") token: String): Response<ApiResponse<Unit>>
    
    @POST("auth/resend-verification")
    suspend fun resendVerification(@Body request: ForgotPasswordRequest): Response<ApiResponse<Unit>>
    
    // Nouveaux endpoints pour le système d'email
    @POST("auth/forgot-password")
    suspend fun sendPasswordResetEmail(@Body request: Map<String, String>): Response<Map<String, Any>>
    
    @POST("auth/reset-password")
    suspend fun resetPasswordWithCode(@Body request: Map<String, String>): Response<Map<String, Any>>
    
    @POST("auth/verify-reset-token")
    suspend fun verifyResetToken(@Body request: Map<String, String>): Response<Map<String, Any>>
    
    @POST("auth/verify-email")
    suspend fun verifyEmailWithCode(@Body request: Map<String, String>): Response<Map<String, Any>>
    
    @POST("auth/resend-verification")
    suspend fun resendVerificationEmail(@Body request: Map<String, String>): Response<Map<String, Any>>
}