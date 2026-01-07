package com.edunova.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edunova.mobile.data.repository.AuthRepository
import com.edunova.mobile.domain.model.User
import com.edunova.mobile.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    // État de l'authentification
    private val _authState = MutableStateFlow<Resource<User>?>(null)
    val authState: StateFlow<Resource<User>?> = _authState.asStateFlow()
    
    // Utilisateur connecté
    val authenticatedUser: Flow<User?> = authRepository.getCurrentUserFlow()
    
    // État de chargement
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Messages d'erreur
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Messages de succès
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    init {
        checkAuthStatus()
    }
    
    // Vérifier le statut d'authentification au démarrage
    private fun checkAuthStatus() {
        viewModelScope.launch {
            if (authRepository.isLoggedIn()) {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    _authState.value = Resource.Success(user)
                }
            }
        }
    }
    
    // Connexion
    fun login(email: String, password: String) {
        viewModelScope.launch {
            authRepository.login(email, password).collect { resource ->
                _authState.value = resource
                _isLoading.value = resource is Resource.Loading
                
                when (resource) {
                    is Resource.Success -> {
                        _successMessage.value = "Connexion réussie"
                        _errorMessage.value = null
                    }
                    is Resource.Error -> {
                        _errorMessage.value = resource.message
                        _successMessage.value = null
                    }
                    is Resource.Loading -> {
                        _errorMessage.value = null
                        _successMessage.value = null
                    }
                }
            }
        }
    }
    
    // Inscription
    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        role: String = "etudiant"
    ) {
        viewModelScope.launch {
            authRepository.register(email, password, firstName, lastName, role).collect { resource ->
                _authState.value = resource
                _isLoading.value = resource is Resource.Loading
                
                when (resource) {
                    is Resource.Success -> {
                        _successMessage.value = "Inscription réussie"
                        _errorMessage.value = null
                    }
                    is Resource.Error -> {
                        _errorMessage.value = resource.message
                        _successMessage.value = null
                    }
                    is Resource.Loading -> {
                        _errorMessage.value = null
                        _successMessage.value = null
                    }
                }
            }
        }
    }
    
    // Connexion Google
    fun googleLogin(code: String) {
        viewModelScope.launch {
            authRepository.googleLogin(code).collect { resource ->
                _authState.value = resource
                _isLoading.value = resource is Resource.Loading
                
                when (resource) {
                    is Resource.Success -> {
                        _successMessage.value = "Connexion Google réussie"
                        _errorMessage.value = null
                    }
                    is Resource.Error -> {
                        _errorMessage.value = resource.message
                        _successMessage.value = null
                    }
                    is Resource.Loading -> {
                        _errorMessage.value = null
                        _successMessage.value = null
                    }
                }
            }
        }
    }
    
    // Déconnexion
    fun logout() {
        viewModelScope.launch {
            authRepository.logout().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _authState.value = null
                        _successMessage.value = "Déconnexion réussie"
                        _errorMessage.value = null
                    }
                    is Resource.Error -> {
                        _errorMessage.value = resource.message
                    }
                    is Resource.Loading -> {
                        _isLoading.value = true
                    }
                }
            }
        }
    }
    
    // Envoyer un email de réinitialisation de mot de passe
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return authRepository.sendPasswordResetEmail(email)
    }
    
    // Réinitialiser le mot de passe avec email, code et nouveau mot de passe
    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<Unit> {
        return authRepository.resetPassword(email, code, newPassword)
    }
    
    // Mot de passe oublié (ancienne méthode - conservée pour compatibilité)
    fun forgotPassword(email: String) {
        viewModelScope.launch {
            authRepository.forgotPassword(email).collect { resource ->
                _isLoading.value = resource is Resource.Loading
                
                when (resource) {
                    is Resource.Success -> {
                        _successMessage.value = "Email de réinitialisation envoyé"
                        _errorMessage.value = null
                    }
                    is Resource.Error -> {
                        _errorMessage.value = resource.message
                        _successMessage.value = null
                    }
                    is Resource.Loading -> {
                        _errorMessage.value = null
                        _successMessage.value = null
                    }
                }
            }
        }
    }
    
    // Réinitialiser le mot de passe
    fun resetPassword(token: String, password: String) {
        viewModelScope.launch {
            authRepository.resetPassword(token, password).collect { resource ->
                _isLoading.value = resource is Resource.Loading
                
                when (resource) {
                    is Resource.Success -> {
                        _successMessage.value = "Mot de passe réinitialisé avec succès"
                        _errorMessage.value = null
                    }
                    is Resource.Error -> {
                        _errorMessage.value = resource.message
                        _successMessage.value = null
                    }
                    is Resource.Loading -> {
                        _errorMessage.value = null
                        _successMessage.value = null
                    }
                }
            }
        }
    }
    
    // Vérifier si l'utilisateur est connecté
    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }
    
    // Obtenir le token
    fun getToken(): String? {
        return authRepository.getToken()
    }
    
    // Nettoyer les messages
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
    
    // Nettoyer l'état d'authentification
    fun clearAuthState() {
        _authState.value = null
    }
}