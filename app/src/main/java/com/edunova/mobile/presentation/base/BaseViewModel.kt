package com.edunova.mobile.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edunova.mobile.utils.Resource
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

/**
 * ViewModel de base avec gestion d'état standardisée et gestion d'erreurs
 * Fournit des utilitaires pour les opérations asynchrones sécurisées
 */
abstract class BaseViewModel : ViewModel() {
    
    // États communs
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    /**
     * Gestionnaire d'exceptions global pour les coroutines
     */
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError(throwable)
    }
    
    /**
     * Scope avec gestion d'erreurs automatique
     */
    protected val safeViewModelScope = viewModelScope + exceptionHandler
    
    /**
     * Exécute une opération avec gestion automatique du loading et des erreurs
     */
    protected fun <T> executeWithLoading(
        operation: suspend () -> T,
        onSuccess: (T) -> Unit = {},
        onError: (Throwable) -> Unit = { handleError(it) },
        showLoading: Boolean = true
    ) {
        safeViewModelScope.launch {
            try {
                if (showLoading) _isLoading.value = true
                val result = operation()
                onSuccess(result)
            } catch (e: Exception) {
                onError(e)
            } finally {
                if (showLoading) _isLoading.value = false
            }
        }
    }
    
    /**
     * Collecte un Flow avec gestion automatique des états Resource
     */
    protected fun <T> Flow<Resource<T>>.collectResource(
        onLoading: () -> Unit = { _isLoading.value = true },
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit = { setError(it) }
    ) {
        safeViewModelScope.launch {
            collect { resource ->
                when (resource) {
                    is Resource.Loading -> onLoading()
                    is Resource.Success -> {
                        _isLoading.value = false
                        resource.data?.let(onSuccess)
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        onError(resource.message ?: "Erreur inconnue")
                    }
                }
            }
        }
    }
    
    /**
     * Transforme un Flow en StateFlow avec gestion d'erreurs
     */
    protected fun <T> Flow<T>.asStateFlowWithError(
        initialValue: T,
        onError: (Throwable) -> Unit = { handleError(it) }
    ): StateFlow<T> {
        return catch { throwable ->
            onError(throwable)
            emit(initialValue)
        }.stateIn(
            scope = safeViewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = initialValue
        )
    }
    
    /**
     * Crée un StateFlow pour un Resource avec état initial
     */
    protected fun <T> createResourceStateFlow(
        initialValue: Resource<T>? = null
    ): MutableStateFlow<Resource<T>?> {
        return MutableStateFlow(initialValue)
    }
    
    /**
     * Met à jour un StateFlow Resource de manière sécurisée
     */
    protected fun <T> MutableStateFlow<Resource<T>?>.updateResource(
        operation: suspend () -> T
    ) {
        safeViewModelScope.launch {
            try {
                value = Resource.Loading()
                val result = operation()
                value = Resource.Success(result)
            } catch (e: Exception) {
                value = Resource.Error(e.message ?: "Erreur inconnue")
                handleError(e)
            }
        }
    }
    
    /**
     * Gestion centralisée des erreurs
     */
    protected open fun handleError(throwable: Throwable) {
        val message = when (throwable) {
            is java.net.UnknownHostException -> "Pas de connexion internet"
            is java.net.SocketTimeoutException -> "Délai d'attente dépassé"
            is java.net.ConnectException -> "Impossible de se connecter au serveur"
            is kotlinx.coroutines.CancellationException -> return // Ne pas traiter les annulations
            else -> throwable.message ?: "Erreur inconnue"
        }
        setError(message)
    }
    
    /**
     * Définit un message d'erreur
     */
    protected fun setError(message: String) {
        _errorMessage.value = message
    }
    
    /**
     * Définit un message de succès
     */
    protected fun setSuccess(message: String) {
        _successMessage.value = message
    }
    
    /**
     * Efface les messages d'erreur et de succès
     */
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
    
    /**
     * Efface seulement le message d'erreur
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Efface seulement le message de succès
     */
    fun clearSuccess() {
        _successMessage.value = null
    }
    
    /**
     * Combine plusieurs StateFlow en un seul avec gestion d'erreurs
     */
    protected fun <T1, T2, R> combineStates(
        flow1: StateFlow<T1>,
        flow2: StateFlow<T2>,
        transform: (T1, T2) -> R
    ): StateFlow<R> {
        return combine(flow1, flow2) { value1, value2 ->
            transform(value1, value2)
        }.catch { throwable ->
            handleError(throwable)
        }.stateIn(
            scope = safeViewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = transform(flow1.value, flow2.value)
        )
    }
    
    /**
     * Retry automatique pour les opérations qui échouent
     */
    protected fun <T> Flow<T>.retryWithBackoff(
        maxRetries: Int = 3,
        initialDelay: Long = 1000,
        maxDelay: Long = 10000,
        factor: Double = 2.0
    ): Flow<T> {
        var currentDelay = initialDelay
        var retryCount = 0
        
        return retryWhen { cause, _ ->
            if (retryCount < maxRetries && cause !is kotlinx.coroutines.CancellationException) {
                retryCount++
                kotlinx.coroutines.delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
                true
            } else {
                false
            }
        }
    }
    
    /**
     * Débounce pour les recherches et saisies utilisateur
     */
    protected fun <T> Flow<T>.debounceSearch(timeoutMillis: Long = 300): Flow<T> {
        return debounce(timeoutMillis)
            .distinctUntilChanged()
    }
}