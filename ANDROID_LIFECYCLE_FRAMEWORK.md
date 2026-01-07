# Framework de Gestion du Cycle de Vie Android

## Vue d'ensemble

Ce framework fournit une solution complète et réutilisable pour gérer les cycles de vie des fragments et activités Android, évitant les crashes liés aux NullPointerException et aux fuites mémoire.

## Architecture du Framework

### 1. Classes de Base

#### BaseFragment<VB : ViewBinding>
Fragment de base avec gestion automatique du ViewBinding et du cycle de vie.

**Fonctionnalités :**
- ✅ Gestion automatique du ViewBinding avec nettoyage
- ✅ Protection contre les accès après destruction
- ✅ Collecte sécurisée des Flows avec cycle de vie
- ✅ Gestion automatique des coroutines
- ✅ Utilitaires pour affichage d'erreurs/succès
- ✅ Vérification de l'état de santé du fragment

#### BaseViewModel
ViewModel de base avec gestion d'état standardisée.

**Fonctionnalités :**
- ✅ États communs (loading, error, success)
- ✅ Gestion automatique des erreurs
- ✅ Opérations avec loading automatique
- ✅ Retry automatique avec backoff
- ✅ Debounce pour recherches
- ✅ Combinaison de StateFlows

#### BaseActivity<VB : ViewBinding>
Activity de base avec les mêmes principes que BaseFragment.

### 2. Utilitaires et Extensions

#### LifecycleExtensions.kt
Extensions pour simplifier la gestion du cycle de vie.

#### SafeCollector.kt
Collecteur sécurisé pour les Flows avec gestion automatique du cycle de vie.

## Guide d'Utilisation

### 1. Créer un Fragment

```kotlin
@AndroidEntryPoint
class MyFragment : BaseFragment<FragmentMyBinding>() {
    
    private val viewModel: MyViewModel by viewModels()
    
    override fun createBinding(
        inflater: LayoutInflater, 
        container: ViewGroup?
    ): FragmentMyBinding {
        return FragmentMyBinding.inflate(inflater, container, false)
    }
    
    override fun setupView() {
        // Configuration initiale de la vue
        setupRecyclerView()
        loadData()
    }
    
    override fun observeData() {
        // Observer les données avec protection automatique
        viewModel.dataState.collectResourceSafely(
            lifecycleOwner = viewLifecycleOwner,
            onLoading = {
                safeWithBinding { binding ->
                    binding.progressBar.visibility = View.VISIBLE
                }
            },
            onSuccess = { data ->
                safeWithBinding { binding ->
                    binding.progressBar.visibility = View.GONE
                    updateUI(data)
                }
            },
            onError = { message ->
                showError(message)
            }
        )
    }
    
    override fun setupListeners() {
        safeWithBinding { binding ->
            binding.button.setOnClickListener {
                ifViewHealthy {
                    // Action sécurisée
                    performAction()
                }
            }
        }
    }
}
```

### 2. Créer un ViewModel

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository
) : BaseViewModel() {
    
    private val _dataState = createResourceStateFlow<List<MyData>>()
    val dataState: StateFlow<Resource<List<MyData>>?> = _dataState.asStateFlow()
    
    fun loadData() {
        _dataState.updateResource {
            repository.getData()
        }
    }
    
    fun performAction() {
        executeWithLoading(
            operation = { repository.performAction() },
            onSuccess = { result ->
                setSuccess("Action réussie!")
            },
            onError = { error ->
                setError("Erreur: ${error.message}")
            }
        )
    }
}
```

### 3. Créer une Activity

```kotlin
@AndroidEntryPoint
class MyActivity : BaseActivity<ActivityMyBinding>() {
    
    private val viewModel: MyViewModel by viewModels()
    
    override fun createBinding(): ActivityMyBinding {
        return ActivityMyBinding.inflate(layoutInflater)
    }
    
    override fun setupView() {
        // Configuration de la vue
    }
    
    override fun observeData() {
        viewModel.dataState.collectSafely { resource ->
            // Gestion des états
        }
    }
}
```

## Bonnes Pratiques

### 1. Gestion du ViewBinding

```kotlin
// ✅ CORRECT - Utilisation sécurisée
safeWithBinding { binding ->
    binding.textView.text = "Hello"
}

// ✅ CORRECT - Vérification d'état
ifViewHealthy {
    // Opération UI sécurisée
}

// ❌ INCORRECT - Accès direct
binding.textView.text = "Hello" // Peut crasher
```

### 2. Collecte de Flows

```kotlin
// ✅ CORRECT - Collecte sécurisée avec Resource
viewModel.dataState.collectResourceSafely(
    lifecycleOwner = viewLifecycleOwner,
    onSuccess = { data -> updateUI(data) },
    onError = { error -> showError(error) }
)

// ✅ CORRECT - Collecte simple
viewModel.simpleFlow.collectSafely(viewLifecycleOwner) { value ->
    // Traitement sécurisé
}

// ❌ INCORRECT - Collecte non sécurisée
lifecycleScope.launch {
    viewModel.flow.collect { value ->
        binding.textView.text = value // Peut crasher
    }
}
```

### 3. Gestion des Erreurs

```kotlin
// ✅ CORRECT - Dans le ViewModel
executeWithLoading(
    operation = { riskyOperation() },
    onSuccess = { result -> setSuccess("Succès!") },
    onError = { error -> handleError(error) }
)

// ✅ CORRECT - Dans le Fragment
showError("Message d'erreur")
showSuccess("Opération réussie")
```

## Templates de Code

### Template Fragment Complet

```kotlin
@AndroidEntryPoint
class TemplateFragment : BaseFragment<FragmentTemplateBinding>() {
    
    private val viewModel: TemplateViewModel by viewModels()
    private lateinit var adapter: TemplateAdapter
    
    override fun createBinding(
        inflater: LayoutInflater, 
        container: ViewGroup?
    ): FragmentTemplateBinding {
        return FragmentTemplateBinding.inflate(inflater, container, false)
    }
    
    override fun setupView() {
        setupRecyclerView()
        setupSwipeRefresh()
        loadInitialData()
    }
    
    override fun observeData() {
        // Observer les données principales
        viewModel.itemsState.collectResourceSafely(
            lifecycleOwner = viewLifecycleOwner,
            onLoading = { showLoading() },
            onSuccess = { items -> showItems(items) },
            onError = { error -> showError(error) }
        )
        
        // Observer les messages
        viewModel.successMessage.collectSafely(viewLifecycleOwner) { message ->
            message?.let {
                showSuccess(it)
                viewModel.clearSuccess()
            }
        }
        
        viewModel.errorMessage.collectSafely(viewLifecycleOwner) { message ->
            message?.let {
                showError(it)
                viewModel.clearError()
            }
        }
    }
    
    override fun setupListeners() {
        safeWithBinding { binding ->
            binding.swipeRefresh.setOnRefreshListener {
                viewModel.refreshData()
            }
            
            binding.fab.setOnClickListener {
                ifViewHealthy {
                    navigateToCreate()
                }
            }
        }
    }
    
    private fun setupRecyclerView() {
        adapter = TemplateAdapter(
            onItemClick = { item ->
                ifViewHealthy {
                    navigateToDetail(item.id)
                }
            },
            onItemDelete = { item ->
                viewModel.deleteItem(item.id)
            }
        )
        
        safeWithBinding { binding ->
            binding.recyclerView.apply {
                adapter = this@TemplateFragment.adapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }
    
    private fun showLoading() {
        safeWithBinding { binding ->
            binding.progressBar.visibility = View.VISIBLE
            binding.swipeRefresh.isRefreshing = false
        }
    }
    
    private fun showItems(items: List<TemplateItem>) {
        safeWithBinding { binding ->
            binding.progressBar.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false
            adapter.submitList(items)
            
            binding.emptyState.visibility = 
                if (items.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
```

### Template ViewModel Complet

```kotlin
@HiltViewModel
class TemplateViewModel @Inject constructor(
    private val repository: TemplateRepository
) : BaseViewModel() {
    
    // États des données
    private val _itemsState = createResourceStateFlow<List<TemplateItem>>()
    val itemsState: StateFlow<Resource<List<TemplateItem>>?> = _itemsState.asStateFlow()
    
    private val _selectedItem = createResourceStateFlow<TemplateItem>()
    val selectedItem: StateFlow<Resource<TemplateItem>?> = _selectedItem.asStateFlow()
    
    // Recherche avec debounce
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Résultats de recherche
    val searchResults = searchQuery
        .debounceSearch()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.searchItems(query)
                    .retryWithBackoff()
                    .catch { handleError(it) }
            }
        }
        .asStateFlowWithError(emptyList())
    
    init {
        loadItems()
    }
    
    fun loadItems() {
        _itemsState.updateResource {
            repository.getItems()
        }
    }
    
    fun refreshData() {
        executeWithLoading(
            operation = { repository.refreshItems() },
            onSuccess = { items ->
                _itemsState.value = Resource.Success(items)
                setSuccess("Données actualisées")
            }
        )
    }
    
    fun deleteItem(itemId: Int) {
        executeWithLoading(
            operation = { repository.deleteItem(itemId) },
            onSuccess = {
                setSuccess("Élément supprimé")
                loadItems() // Recharger la liste
            }
        )
    }
    
    fun searchItems(query: String) {
        _searchQuery.value = query
    }
    
    fun selectItem(itemId: Int) {
        _selectedItem.updateResource {
            repository.getItemById(itemId)
        }
    }
}
```

## Migration d'un Fragment Existant

### Étapes de Migration

1. **Hériter de BaseFragment**
```kotlin
// Avant
class MyFragment : Fragment() {
    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!
}

// Après  
class MyFragment : BaseFragment<FragmentMyBinding>() {
    // Plus besoin de gérer _binding manuellement
}
```

2. **Implémenter les méthodes abstraites**
```kotlin
override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMyBinding {
    return FragmentMyBinding.inflate(inflater, container, false)
}

override fun setupView() {
    // Code d'initialisation
}
```

3. **Migrer les observers**
```kotlin
// Avant
viewLifecycleOwner.lifecycleScope.launch {
    viewModel.data.collect { resource ->
        when (resource) {
            is Resource.Loading -> { /* ... */ }
            is Resource.Success -> { /* ... */ }
            is Resource.Error -> { /* ... */ }
        }
    }
}

// Après
viewModel.data.collectResourceSafely(
    lifecycleOwner = viewLifecycleOwner,
    onLoading = { /* ... */ },
    onSuccess = { data -> /* ... */ },
    onError = { error -> /* ... */ }
)
```

4. **Utiliser les utilitaires sécurisés**
```kotlin
// Remplacer les accès directs au binding
safeWithBinding { binding ->
    binding.textView.text = "Hello"
}

// Utiliser les vérifications d'état
ifViewHealthy {
    // Opérations UI
}
```

## Avantages du Framework

### 1. Sécurité
- ✅ Élimination des NullPointerException
- ✅ Gestion automatique du cycle de vie
- ✅ Protection contre les fuites mémoire

### 2. Productivité
- ✅ Code boilerplate réduit
- ✅ Patterns cohérents
- ✅ Réutilisabilité maximale

### 3. Maintenabilité
- ✅ Code plus lisible
- ✅ Gestion d'erreurs centralisée
- ✅ Tests plus faciles

### 4. Performance
- ✅ Annulation automatique des coroutines
- ✅ Gestion optimisée des ressources
- ✅ Collecte intelligente des Flows

## Intégration dans un Projet Existant

### 1. Ajout Progressif
- Commencer par les nouveaux fragments
- Migrer progressivement les fragments existants
- Utiliser les utilitaires indépendamment

### 2. Configuration Gradle
```kotlin
// Aucune dépendance supplémentaire requise
// Le framework utilise les APIs Android standard
```

### 3. Tests
```kotlin
// Les classes de base facilitent les tests
class MyFragmentTest {
    @Test
    fun testFragmentCreation() {
        // Tests simplifiés grâce au framework
    }
}
```

---

**Ce framework est conçu pour être :**
- 🔧 **Adaptable** : Fonctionne avec tout projet Android
- 🚀 **Performant** : Optimisé pour les meilleures pratiques
- 🛡️ **Sécurisé** : Élimine les crashes courants
- 📚 **Documenté** : Guide complet et exemples
- 🔄 **Évolutif** : Facilement extensible

**Utilisez ce framework pour créer des applications Android robustes et maintenables !**