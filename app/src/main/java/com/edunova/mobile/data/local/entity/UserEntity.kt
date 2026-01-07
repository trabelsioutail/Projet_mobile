package com.edunova.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.edunova.mobile.domain.model.User
import com.edunova.mobile.domain.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val lastLogin: String? = null,
    val profileImage: String? = null,
    val token: String? = null,
    val lastSyncAt: Long = System.currentTimeMillis()
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
    
    companion object {
        fun fromDomainModel(user: User, token: String? = null): UserEntity {
            return UserEntity(
                id = user.id,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                role = user.role.toApiString(),
                isVerified = user.isVerified,
                isActive = user.isActive,
                createdAt = user.createdAt,
                lastLogin = user.lastLogin,
                profileImage = user.profileImage,
                token = token
            )
        }
    }
}