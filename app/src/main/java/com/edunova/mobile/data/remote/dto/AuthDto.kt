package com.edunova.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.edunova.mobile.domain.model.User
import com.edunova.mobile.domain.model.UserRole

// Requêtes d'authentification
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    val role: String = "etudiant"
)

data class GoogleAuthRequest(
    val code: String
)

// Réponses d'authentification
data class AuthResponse(
    val success: Boolean,
    val error: String? = null,
    val token: String? = null,
    val user: UserDto? = null
)

data class UserDto(
    val id: Int,
    val email: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    val role: String,
    @SerializedName("is_verified")
    val isVerified: Boolean = false,
    @SerializedName("is_active")
    val isActive: Boolean = true,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("last_login")
    val lastLogin: String? = null,
    @SerializedName("profile_image")
    val profileImage: String? = null
) {
    fun toDomainModel(): User {
        return User(
            id = id,
            email = email,
            firstName = firstName,
            lastName = lastName,
            role = UserRole.fromString(role),
            isVerified = isVerified,
            isActive = isActive,
            createdAt = createdAt,
            lastLogin = lastLogin,
            profileImage = profileImage
        )
    }
}

// Réinitialisation de mot de passe
data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val token: String,
    val password: String
)