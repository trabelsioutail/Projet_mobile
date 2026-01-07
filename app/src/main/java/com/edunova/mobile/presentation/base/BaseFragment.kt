package com.edunova.mobile.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Fragment de base qui gère automatiquement le cycle de vie et le view binding
 * Utilise les bonnes pratiques Android pour éviter les crashes et fuites mémoire
 * 
 * @param VB Type du ViewBinding utilisé par le fragment
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    
    private var _binding: VB? = null
    
    /**
     * Accès sécurisé au binding
     * Lance une exception si appelé après onDestroyView
     */
    protected val binding: VB
        get() = _binding ?: throw IllegalStateException(
            "ViewBinding accessed after onDestroyView. Use safeBinding instead."
        )
    
    /**
     * Accès sécurisé au binding qui retourne null si la vue est détruite
     */
    protected val safeBinding: VB?
        get() = _binding
    
    /**
     * Scope de coroutine lié au cycle de vie de la vue
     * Automatiquement annulé quand la vue est détruite
     */
    protected val viewScope: CoroutineScope
        get() = viewLifecycleOwner.lifecycleScope
    
    /**
     * Jobs de coroutines à annuler manuellement si nécessaire
     */
    private val jobs = mutableListOf<Job>()
    
    /**
     * Méthode abstraite pour créer le ViewBinding
     */
    abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    
    /**
     * Méthode appelée après la création de la vue pour l'initialisation
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
    
    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = createBinding(inflater, container)
        return binding.root
    }
    
    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupListeners()
        observeData()
    }
    
    final override fun onDestroyView() {
        // Annuler tous les jobs manuels
        jobs.forEach { it.cancel() }
        jobs.clear()
        
        // Nettoyer le binding
        _binding = null
        super.onDestroyView()
    }
    
    /**
     * Exécute une action de manière sécurisée avec le binding
     * Ne fait rien si le binding est null
     */
    protected fun safeWithBinding(action: (VB) -> Unit) {
        _binding?.let(action)
    }
    
    /**
     * Collecte un Flow de manière sécurisée avec gestion du cycle de vie
     * Utilise repeatOnLifecycle(STARTED) pour éviter les fuites
     */
    protected fun <T> Flow<T>.collectSafely(
        lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
        action: suspend (T) -> Unit
    ) {
        viewScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(lifecycleState) {
                collect { value ->
                    if (_binding != null) {
                        action(value)
                    }
                }
            }
        }
    }
    
    /**
     * Collecte un Flow avec mise à jour automatique de l'UI
     * Vérifie automatiquement que le binding existe
     */
    protected fun <T> Flow<T>.collectWithBinding(
        lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
        action: suspend VB.(T) -> Unit
    ) {
        collectSafely(lifecycleState) { value ->
            safeWithBinding { binding ->
                viewScope.launch {
                    binding.action(value)
                }
            }
        }
    }
    
    /**
     * Lance une coroutine liée au cycle de vie de la vue
     * Automatiquement annulée quand la vue est détruite
     */
    protected fun launchViewScope(
        lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return viewScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(lifecycleState) {
                block()
            }
        }
    }
    
    /**
     * Lance une coroutine avec gestion manuelle
     * Ajoutée à la liste des jobs pour annulation automatique
     */
    protected fun launchManaged(block: suspend CoroutineScope.() -> Unit): Job {
        val job = viewScope.launch(block = block)
        jobs.add(job)
        return job
    }
    
    /**
     * Affiche un message d'erreur de manière sécurisée
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
     * Affiche un message de succès de manière sécurisée
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
    
    /**
     * Vérifie si le fragment est dans un état sain pour les opérations UI
     */
    protected fun isViewHealthy(): Boolean {
        return _binding != null && 
               isAdded && 
               !isDetached && 
               !isRemoving && 
               view != null
    }
    
    /**
     * Exécute une action seulement si la vue est dans un état sain
     */
    protected inline fun ifViewHealthy(action: () -> Unit) {
        if (isViewHealthy()) {
            action()
        }
    }
}