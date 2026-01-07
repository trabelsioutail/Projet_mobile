package com.edunova.mobile.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Extensions pour simplifier la gestion du cycle de vie dans les fragments
 */

/**
 * Collecte un Flow de manière sécurisée avec gestion automatique du cycle de vie
 */
fun <T> Flow<T>.collectIn(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
): Job {
    return lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(state) {
            collect(action)
        }
    }
}

/**
 * Collecte un Flow avec accès sécurisé au ViewBinding
 */
fun <T, VB : ViewBinding> Flow<T>.collectWithBinding(
    fragment: Fragment,
    binding: () -> VB?,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend VB.(T) -> Unit
): Job {
    return fragment.viewLifecycleOwner.lifecycleScope.launch {
        fragment.viewLifecycleOwner.repeatOnLifecycle(state) {
            collect { value ->
                binding()?.let { vb ->
                    vb.action(value)
                }
            }
        }
    }
}

/**
 * Exécute une action de manière sécurisée avec le ViewBinding
 */
inline fun <VB : ViewBinding> Fragment.safeWithBinding(
    binding: () -> VB?,
    action: (VB) -> Unit
) {
    if (isAdded && !isDetached && !isRemoving && view != null) {
        binding()?.let(action)
    }
}

/**
 * Lance une coroutine liée au cycle de vie de la vue du fragment
 */
fun Fragment.launchViewLifecycle(
    state: Lifecycle.State = Lifecycle.State.STARTED,
    block: suspend CoroutineScope.() -> Unit
): Job {
    return viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(state) {
            block()
        }
    }
}

/**
 * Observe un Resource avec gestion automatique des états
 */
fun <T> Flow<Resource<T>>.observeResource(
    lifecycleOwner: LifecycleOwner,
    onLoading: () -> Unit = {},
    onSuccess: (T) -> Unit,
    onError: (String) -> Unit = {}
): Job {
    return collectIn(lifecycleOwner) { resource ->
        when (resource) {
            is Resource.Loading -> onLoading()
            is Resource.Success -> resource.data?.let(onSuccess)
            is Resource.Error -> onError(resource.message ?: "Erreur inconnue")
        }
    }
}

/**
 * Observe un Resource avec ViewBinding
 */
fun <T, VB : ViewBinding> Flow<Resource<T>>.observeResourceWithBinding(
    fragment: Fragment,
    binding: () -> VB?,
    onLoading: VB.() -> Unit = {},
    onSuccess: VB.(T) -> Unit,
    onError: VB.(String) -> Unit = {}
): Job {
    return collectWithBinding(fragment, binding) { resource ->
        when (resource) {
            is Resource.Loading -> onLoading()
            is Resource.Success -> resource.data?.let { onSuccess(it) }
            is Resource.Error -> onError(resource.message ?: "Erreur inconnue")
        }
    }
}

/**
 * Vérifie si un fragment est dans un état sain pour les opérations UI
 */
fun Fragment.isViewHealthy(): Boolean {
    return isAdded && 
           !isDetached && 
           !isRemoving && 
           view != null &&
           context != null
}

/**
 * Exécute une action seulement si le fragment est dans un état sain
 */
inline fun Fragment.ifViewHealthy(action: () -> Unit) {
    if (isViewHealthy()) {
        action()
    }
}

/**
 * Affiche un Snackbar de manière sécurisée
 */
fun Fragment.showSnackbar(
    message: String,
    duration: Int = com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
) {
    ifViewHealthy {
        view?.let { v ->
            com.google.android.material.snackbar.Snackbar.make(v, message, duration).show()
        }
    }
}

/**
 * Affiche un message d'erreur avec Snackbar
 */
fun Fragment.showError(message: String) {
    showSnackbar(message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
}

/**
 * Affiche un message de succès avec Snackbar
 */
fun Fragment.showSuccess(message: String) {
    showSnackbar(message, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
}

/**
 * Collecte plusieurs Flows en parallèle de manière sécurisée
 */
fun Fragment.collectFlows(
    state: Lifecycle.State = Lifecycle.State.STARTED,
    vararg flows: suspend CoroutineScope.() -> Unit
) {
    launchViewLifecycle(state) {
        flows.forEach { flow ->
            launch { flow() }
        }
    }
}

/**
 * Délégué pour ViewBinding avec nettoyage automatique
 */
class ViewBindingDelegate<VB : ViewBinding>(
    private val bindingFactory: (android.view.View) -> VB
) {
    private var _binding: VB? = null
    
    fun bind(view: android.view.View): VB {
        _binding = bindingFactory(view)
        return _binding!!
    }
    
    fun get(): VB? = _binding
    
    fun clear() {
        _binding = null
    }
}

/**
 * Crée un délégué ViewBinding
 */
fun <VB : ViewBinding> Fragment.viewBinding(
    bindingFactory: (android.view.View) -> VB
): ViewBindingDelegate<VB> {
    return ViewBindingDelegate(bindingFactory)
}