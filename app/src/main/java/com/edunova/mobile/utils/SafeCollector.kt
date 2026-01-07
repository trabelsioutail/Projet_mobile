package com.edunova.mobile.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

/**
 * Collecteur sécurisé pour les Flows qui gère automatiquement le cycle de vie
 * et évite les crashes liés aux vues détruites
 */
open class SafeCollector<T>(
    private val lifecycleOwner: LifecycleOwner,
    private val minActiveState: Lifecycle.State = Lifecycle.State.STARTED
) : FlowCollector<T> {
    
    private var collector: (suspend (T) -> Unit)? = null
    private var job: Job? = null
    
    /**
     * Démarre la collecte avec l'action spécifiée
     */
    fun startCollecting(
        flow: Flow<T>,
        action: suspend (T) -> Unit
    ): Job {
        collector = action
        job?.cancel()
        
        job = lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(minActiveState) {
                flow.collect(this@SafeCollector)
            }
        }
        
        return job!!
    }
    
    /**
     * Arrête la collecte
     */
    fun stopCollecting() {
        job?.cancel()
        job = null
        collector = null
    }
    
    override suspend fun emit(value: T) {
        // Vérifier que le lifecycle est encore actif
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(minActiveState)) {
            collector?.invoke(value)
        }
    }
}

/**
 * Extension pour créer un SafeCollector facilement
 */
fun <T> Flow<T>.collectSafely(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
): Job {
    val safeCollector = SafeCollector<T>(lifecycleOwner, minActiveState)
    return safeCollector.startCollecting(this, action)
}

/**
 * Gestionnaire de collecteurs multiples pour un LifecycleOwner
 */
class SafeCollectorManager(private val lifecycleOwner: LifecycleOwner) {
    
    private val collectors = mutableMapOf<String, SafeCollector<*>>()
    private val jobs = mutableMapOf<String, Job>()
    
    /**
     * Ajoute un collecteur avec un identifiant unique
     */
    fun <T> addCollector(
        id: String,
        flow: Flow<T>,
        minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
        action: suspend (T) -> Unit
    ): Job {
        // Arrêter le collecteur précédent s'il existe
        stopCollector(id)
        
        val collector = SafeCollector<T>(lifecycleOwner, minActiveState)
        val job = collector.startCollecting(flow, action)
        
        collectors[id] = collector
        jobs[id] = job
        
        return job
    }
    
    /**
     * Arrête un collecteur spécifique
     */
    fun stopCollector(id: String) {
        collectors[id]?.stopCollecting()
        jobs[id]?.cancel()
        collectors.remove(id)
        jobs.remove(id)
    }
    
    /**
     * Arrête tous les collecteurs
     */
    fun stopAllCollectors() {
        collectors.values.forEach { it.stopCollecting() }
        jobs.values.forEach { it.cancel() }
        collectors.clear()
        jobs.clear()
    }
    
    /**
     * Vérifie si un collecteur est actif
     */
    fun isCollectorActive(id: String): Boolean {
        return jobs[id]?.isActive == true
    }
    
    /**
     * Obtient la liste des collecteurs actifs
     */
    fun getActiveCollectors(): List<String> {
        return jobs.filter { it.value.isActive }.keys.toList()
    }
}

/**
 * Extension pour créer un SafeCollectorManager
 */
fun LifecycleOwner.createSafeCollectorManager(): SafeCollectorManager {
    return SafeCollectorManager(this)
}

/**
 * Collecteur spécialisé pour les Resource
 */
class ResourceSafeCollector<T>(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
) : SafeCollector<Resource<T>>(lifecycleOwner, minActiveState) {
    
    fun startCollectingResource(
        flow: Flow<Resource<T>>,
        onLoading: suspend () -> Unit = {},
        onSuccess: suspend (T) -> Unit,
        onError: suspend (String) -> Unit = {}
    ): Job {
        return startCollecting(flow) { resource ->
            when (resource) {
                is Resource.Loading -> onLoading()
                is Resource.Success -> resource.data?.let { onSuccess(it) }
                is Resource.Error -> onError(resource.message ?: "Erreur inconnue")
            }
        }
    }
}

/**
 * Extension pour collecter des Resource de manière sécurisée
 */
fun <T> Flow<Resource<T>>.collectResourceSafely(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onLoading: suspend () -> Unit = {},
    onSuccess: suspend (T) -> Unit,
    onError: suspend (String) -> Unit = {}
): Job {
    val collector = ResourceSafeCollector<T>(lifecycleOwner, minActiveState)
    return collector.startCollectingResource(this, onLoading, onSuccess, onError)
}

/**
 * Utilitaire pour déboguer les collecteurs
 */
object CollectorDebugger {
    private val activeCollectors = mutableMapOf<String, Long>()
    
    fun logCollectorStart(id: String) {
        activeCollectors[id] = System.currentTimeMillis()
        println("SafeCollector [$id] started at ${activeCollectors[id]}")
    }
    
    fun logCollectorStop(id: String) {
        val startTime = activeCollectors.remove(id)
        val duration = startTime?.let { System.currentTimeMillis() - it }
        println("SafeCollector [$id] stopped after ${duration}ms")
    }
    
    fun getActiveCollectors(): Map<String, Long> {
        return activeCollectors.toMap()
    }
    
    fun printActiveCollectors() {
        println("Active SafeCollectors: ${activeCollectors.size}")
        activeCollectors.forEach { (id, startTime) ->
            val duration = System.currentTimeMillis() - startTime
            println("  - $id: ${duration}ms")
        }
    }
}