package com.edunova.mobile.data.repository

import android.content.SharedPreferences
import com.edunova.mobile.BuildConfig
import com.edunova.mobile.data.local.dao.UserDao
import com.edunova.mobile.data.local.entity.UserEntity
import com.edunova.mobile.data.mock.MockAuthRepository
import com.edunova.mobile.data.remote.api.AuthApiService
import com.edunova.mobile.data.remote.dto.*
import com.edunova.mobile.domain.model.User
import com.edunova.mobile.utils.NetworkUtils
import com.edunova.mobile.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val userDao: UserDao,
    private val sharedPreferences: SharedPreferences,
    private val networkUtils: NetworkUtils,
    private val mockAuthRepository: MockAuthRepository
) {
    
    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    // Observer l'utilisateur connecté
    fun getCurrentUserFlow(): Flow<User?> {
        return userDao.getCurrentUserFlow().map { it?.toDomainModel() }
    }
    
    // Obtenir l'utilisateur connecté
    suspend fun getCurrentUser(): User? {
        return userDao.getCurrentUser()?.toDomainModel()
    }
    
    // Vérifier si l'utilisateur est connecté
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) && getToken() != null
    }
    
    // Obtenir le token
    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }
    
    // Connexion
    suspend fun login(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        
        try {
            // TOUJOURS utiliser l'API réelle pour la connexion
            // Cela garantit que les modifications admin sont prises en compte
            val response = authApiService.login(LoginRequest(email, password))
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.success == true && authResponse.token != null && authResponse.user != null) {
                    val user = authResponse.user.toDomainModel()
                    val token = authResponse.token
                    
                    // Sauvegarder en local
                    saveUserSession(user, token)
                    
                    emit(Resource.Success(user))
                } else {
                    emit(Resource.Error(authResponse?.error ?: "Erreur de connexion"))
                }
            } else {
                // Si l'API échoue, utiliser les données mockées en dernier recours
                if (BuildConfig.USE_MOCK_DATA || !networkUtils.isNetworkAvailable()) {
                    val mockResult = mockAuthRepository.login(email, password)
                    
                    if (mockResult.isSuccess) {
                        val authResponse = mockResult.getOrNull()!!
                        val user = authResponse.user
                        val token = authResponse.token
                        
                        // Sauvegarder en local
                        saveUserSession(user, token)
                        
                        emit(Resource.Success(user))
                    } else {
                        emit(Resource.Error("Identifiants incorrects"))
                    }
                } else {
                    emit(Resource.Error("Erreur de connexion au serveur"))
                }
            }
        } catch (e: Exception) {
            // En cas d'exception, essayer le mode mock seulement si activé
            if (BuildConfig.USE_MOCK_DATA) {
                val mockResult = mockAuthRepository.login(email, password)
                
                if (mockResult.isSuccess) {
                    val authResponse = mockResult.getOrNull()!!
                    val user = authResponse.user
                    val token = authResponse.token
                    
                    // Sauvegarder en local
                    saveUserSession(user, token)
                    
                    emit(Resource.Success(user))
                } else {
                    emit(Resource.Error("Identifiants incorrects"))
                }
            } else {
                emit(Resource.Error(e.message ?: "Erreur de connexion"))
            }
        }
    }
    
    // Inscription
    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        role: String = "etudiant"
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        
        try {
            if (!networkUtils.isNetworkAvailable()) {
                emit(Resource.Error("Connexion internet requise pour l'inscription"))
                return@flow
            }
            
            val response = authApiService.register(
                RegisterRequest(email, password, firstName, lastName, role)
            )
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.success == true && authResponse.token != null && authResponse.user != null) {
                    val user = authResponse.user.toDomainModel()
                    val token = authResponse.token
                    
                    // Sauvegarder en local
                    saveUserSession(user, token)
                    
                    emit(Resource.Success(user))
                } else {
                    emit(Resource.Error(authResponse?.error ?: "Erreur d'inscription"))
                }
            } else {
                emit(Resource.Error("Erreur réseau: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    // Connexion Google
    suspend fun googleLogin(code: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        
        try {
            if (!networkUtils.isNetworkAvailable()) {
                emit(Resource.Error("Connexion internet requise pour Google Auth"))
                return@flow
            }
            
            val response = authApiService.googleLogin(GoogleAuthRequest(code))
            
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.success == true && authResponse.token != null && authResponse.user != null) {
                    val user = authResponse.user.toDomainModel()
                    val token = authResponse.token
                    
                    // Sauvegarder en local
                    saveUserSession(user, token)
                    
                    emit(Resource.Success(user))
                } else {
                    emit(Resource.Error(authResponse?.error ?: "Erreur Google Auth"))
                }
            } else {
                emit(Resource.Error("Erreur réseau: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    // Déconnexion
    suspend fun logout(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        
        try {
            if (networkUtils.isNetworkAvailable()) {
                // Tentative de déconnexion côté serveur
                authApiService.logout()
            }
            
            // Nettoyer les données locales
            clearUserSession()
            
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            // Même en cas d'erreur réseau, on nettoie localement
            clearUserSession()
            emit(Resource.Success(Unit))
        }
    }
    
    // Mot de passe oublié - Envoyer le code de réinitialisation
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            if (!networkUtils.isNetworkAvailable()) {
                Result.failure(Exception("Connexion internet requise"))
            } else {
                val response = authApiService.sendPasswordResetEmail(mapOf("email" to email))
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.get("success") == true) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(apiResponse?.get("error")?.toString() ?: "Erreur lors de l'envoi"))
                    }
                } else {
                    Result.failure(Exception("Erreur réseau: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Réinitialiser le mot de passe avec email, code et nouveau mot de passe
    suspend fun resetPassword(email: String, token: String, newPassword: String): Result<Unit> {
        return try {
            if (!networkUtils.isNetworkAvailable()) {
                Result.failure(Exception("Connexion internet requise"))
            } else {
                val response = authApiService.resetPasswordWithCode(mapOf(
                    "email" to email,
                    "token" to token,
                    "newPassword" to newPassword
                ))
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.get("success") == true) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(apiResponse?.get("error")?.toString() ?: "Erreur lors de la réinitialisation"))
                    }
                } else {
                    Result.failure(Exception("Erreur réseau: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Mot de passe oublié (ancienne méthode - conservée pour compatibilité)
    suspend fun forgotPassword(email: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        
        try {
            val result = sendPasswordResetEmail(email)
            if (result.isSuccess) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error(result.exceptionOrNull()?.message ?: "Erreur"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    // Réinitialiser le mot de passe
    suspend fun resetPassword(token: String, password: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        
        try {
            if (!networkUtils.isNetworkAvailable()) {
                emit(Resource.Error("Connexion internet requise"))
                return@flow
            }
            
            val response = authApiService.resetPassword(token, ResetPasswordRequest(token, password))
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    emit(Resource.Success(Unit))
                } else {
                    emit(Resource.Error(apiResponse?.message ?: "Erreur"))
                }
            } else {
                emit(Resource.Error("Erreur réseau: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    // Sauvegarder la session utilisateur
    private suspend fun saveUserSession(user: User, token: String) {
        // Sauvegarder dans Room
        userDao.insertUser(UserEntity.fromDomainModel(user, token))
        
        // Sauvegarder dans SharedPreferences
        sharedPreferences.edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_USER_ID, user.id)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }
    
    // Nettoyer la session utilisateur
    private suspend fun clearUserSession() {
        // Nettoyer Room
        userDao.clearAllTokens()
        
        // Nettoyer SharedPreferences
        sharedPreferences.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }
    
    // Mettre à jour le statut de connexion
    private fun updateLoginStatus(isLoggedIn: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            .apply()
    }
}