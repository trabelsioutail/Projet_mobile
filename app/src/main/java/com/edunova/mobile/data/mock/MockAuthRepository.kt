package com.edunova.mobile.data.mock

import com.edunova.mobile.domain.model.User
import com.edunova.mobile.domain.model.UserRole
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

// DTOs pour les réponses mockées
data class MockLoginResponse(
    val token: String,
    val user: User,
    val expiresIn: Long
)

data class MockRegisterResponse(
    val message: String,
    val user: User
)

data class MockRefreshTokenResponse(
    val token: String,
    val expiresIn: Long
)

@Singleton
class MockAuthRepository @Inject constructor() {
    
    // Utilisation des données extraites du backend
    private val mockUsers = BackendMockData.users
    
    suspend fun login(email: String, password: String): Result<MockLoginResponse> {
        delay(1000) // Simulation délai réseau
        
        val user = BackendMockData.getUserByEmail(email)
        
        return if (user != null) {
            Result.success(
                MockLoginResponse(
                    token = "mock_token_${System.currentTimeMillis()}",
                    user = user,
                    expiresIn = 3600000L // 1 heure
                )
            )
        } else {
            Result.failure(Exception("Email ou mot de passe incorrect"))
        }
    }
    
    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        role: UserRole
    ): Result<MockRegisterResponse> {
        delay(1500) // Simulation délai réseau
        
        // Vérifier si l'email existe déjà
        if (BackendMockData.getUserByEmail(email) != null) {
            return Result.failure(Exception("Cet email est déjà utilisé"))
        }
        
        val newUser = User(
            id = (mockUsers.maxOfOrNull { it.id } ?: 0) + 1,
            email = email,
            firstName = firstName,
            lastName = lastName,
            role = role,
            isVerified = false
        )
        
        return Result.success(
            MockRegisterResponse(
                message = "Inscription réussie. Vérifiez votre email.",
                user = newUser
            )
        )
    }
    
    suspend fun refreshToken(token: String): Result<MockRefreshTokenResponse> {
        delay(500)
        
        return Result.success(
            MockRefreshTokenResponse(
                token = "refreshed_token_${System.currentTimeMillis()}",
                expiresIn = 3600000L
            )
        )
    }
    
    suspend fun logout(): Result<String> {
        delay(300)
        return Result.success("Déconnexion réussie")
    }
    
    suspend fun forgotPassword(email: String): Result<String> {
        delay(1000)
        
        val user = BackendMockData.getUserByEmail(email)
        return if (user != null) {
            Result.success("Instructions de réinitialisation envoyées par email")
        } else {
            Result.failure(Exception("Aucun compte trouvé avec cet email"))
        }
    }
    
    suspend fun resetPassword(token: String, newPassword: String): Result<String> {
        delay(1000)
        return Result.success("Mot de passe réinitialisé avec succès")
    }
    
    suspend fun verifyEmail(token: String): Result<String> {
        delay(800)
        return Result.success("Email vérifié avec succès")
    }
    
    suspend fun getCurrentUser(): Result<User> {
        delay(300)
        // Retourner le premier utilisateur admin comme utilisateur connecté
        val adminUser = mockUsers.find { it.role == UserRole.ADMIN }
        return if (adminUser != null) {
            Result.success(adminUser)
        } else {
            Result.failure(Exception("Utilisateur non trouvé"))
        }
    }
    
    suspend fun updateProfile(
        firstName: String,
        lastName: String,
        email: String
    ): Result<User> {
        delay(1000)
        
        val currentUser = mockUsers.find { it.role == UserRole.ADMIN }
        return if (currentUser != null) {
            val updatedUser = currentUser.copy(
                firstName = firstName,
                lastName = lastName,
                email = email
            )
            Result.success(updatedUser)
        } else {
            Result.failure(Exception("Erreur lors de la mise à jour"))
        }
    }
    
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<String> {
        delay(1000)
        return Result.success("Mot de passe modifié avec succès")
    }
}