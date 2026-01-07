package com.edunova.mobile.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val lastLogin: String? = null,
    val profileImage: String? = null
) : Parcelable

enum class UserRole {
    ETUDIANT,
    ENSEIGNANT,
    ADMIN;
    
    companion object {
        fun fromString(role: String): UserRole {
            return when (role.lowercase()) {
                "etudiant" -> ETUDIANT
                "enseignant" -> ENSEIGNANT
                "admin" -> ADMIN
                else -> ETUDIANT
            }
        }
    }
    
    fun toApiString(): String {
        return when (this) {
            ETUDIANT -> "etudiant"
            ENSEIGNANT -> "enseignant"
            ADMIN -> "admin"
        }
    }
}