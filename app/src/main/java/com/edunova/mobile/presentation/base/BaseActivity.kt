package com.edunova.mobile.presentation.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Activity de base avec gestion automatique du ViewBinding et du cycle de vie
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    
    private var _binding: VB? = null
    
    /**
     * Accès sécurisé au binding
     */
    protected val binding: VB
        get() = _binding ?: throw IllegalStateException(
            "ViewBinding accessed after onDestroy"
        )
    
    /**
     * Accès sécurisé au binding qui retourne null si détruit
     */
    protected val safeBinding: VB?
        get() = _binding
    
    /**
     * Scope de coroutine lié au cycle de vie de l'activité
     */
    protected val activityScope: CoroutineScope
        get() = lifecycleScope
    
    /**
     * Jobs de coroutines à annuler manuellement
     */
    private val jobs = mutableListOf<Job>()
    
    /**
     * Méthode abstraite pour créer le ViewBinding
     */
    abstract fun createBinding(): VB
    
    /**
     * Méthode appelée après la création de la vue
     */
    abstract fun setupView()
    
    /**
     * Méthode optionnelle pour observer les données
     */
    open fun observeData() {}
    
    /**
     * Méthode optionnelle pour configurer les listeners
     */
    open fun setupListeners() {}
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = createBinding()
        setContentView(binding.root)
        
        setupView()
        setupListeners()
        observeData()
    }
    
    override fun onDestroy() {
        // Annuler tous les jobs manuels
        jobs.forEach { it.cancel() }
        jobs.clear()
        
        // Nettoyer le binding
        _binding = null
        super.onDestroy()
    }
    
    /**
     * Exécute une action de manière sécurisée avec le binding
     */
    protected fun safeWithBinding(action: (VB) -> Unit) {
        _binding?.let(action)
    }
    
    /**
     * Collecte un Flow de manière sécurisée
     */
    protected fun <T> Flow<T>.collectSafely(action: suspend (T) -> Unit) {
        activityScope.launch {
            collect { value ->
                if (_binding != null && !isDestroyed) {
                    action(value)
                }
            }
        }
    }
    
    /**
     * Lance une coroutine avec gestion manuelle
     */
    protected fun launchManaged(block: suspend CoroutineScope.() -> Unit): Job {
        val job = activityScope.launch(block = block)
        jobs.add(job)
        return job
    }
    
    /**
     * Affiche un message d'erreur
     */
    protected fun showError(message: String) {
        safeWithBinding { binding ->
            com.google.android.material.snackbar.Snackbar.make(
                binding.root,
                message,
                com.google.android.material.snackbar.Snackbar.LENGTH_LONG
            ).show()
        }
    }
    
    /**
     * Affiche un message de succès
     */
    protected fun showSuccess(message: String) {
        safeWithBinding { binding ->
            com.google.android.material.snackbar.Snackbar.make(
                binding.root,
                message,
                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
            ).show()
        }
    }
}