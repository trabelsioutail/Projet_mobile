# Architecture EduNova Mobile - Kotlin

## Vue d'ensemble

Cette application mobile Android native suit strictement l'architecture **MVVM (Model-View-ViewModel)** avec une approche **hybride** (hors ligne + API) pour offrir une expérience utilisateur optimale même sans connexion Internet.

## Architecture MVVM Stricte

### 1. **View Layer (Presentation)**
- **Activities/Fragments** : Interface utilisateur uniquement
- **Data Binding** : Liaison bidirectionnelle avec les ViewModels
- **Navigation Component** : Navigation entre les écrans
- **Adapters** : Gestion des listes RecyclerView

**Règles strictes :**
- Les Views n'appellent QUE les méthodes du ViewModel
- Aucune logique métier dans les Views
- Observer uniquement les LiveData/Flow du ViewModel

### 2. **ViewModel Layer**
- **Gestion d'état** : LiveData et StateFlow pour l'UI
- **ViewModelScope** : Coroutines pour les opérations asynchrones
- **Injection Hilt** : ViewModels injectés automatiquement
- **Séparation claire** : Un ViewModel par écran principal

**Responsabilités :**
- Exposer les données à l'UI via LiveData/Flow
- Gérer l'état de chargement et les erreurs
- Appeler les méthodes du Repository
- Transformer les données pour l'affichage

### 3. **Repository Layer (Source de Vérité Unique)**
- **AuthRepository** : Gestion de l'authentification et session
- **CourseRepository** : CRUD des cours
- **QuizRepository** : Gestion des quiz et soumissions
- **MessageRepository** : Messagerie temps réel

**Logique hybride :**
```kotlin
suspend fun getData(): Flow<Resource<T>> = flow {
    // 1. Émettre les données locales d'abord
    val localData = localDao.getData()
    if (localData.isNotEmpty()) {
        emit(Resource.Success(localData))
    }
    
    // 2. Synchroniser avec l'API si réseau disponible
    if (networkUtils.isNetworkAvailable()) {
        val remoteData = apiService.getData()
        localDao.insertData(remoteData)
        emit(Resource.Success(remoteData))
    }
}
```

### 4. **Data Layer**
#### Local (Room Database)
- **Entities** : Tables de base de données
- **DAOs** : Accès aux données avec Flow
- **Database** : Configuration Room avec migrations

#### Remote (Retrofit API)
- **DTOs** : Modèles de données API
- **ApiServices** : Endpoints REST
- **Interceptors** : Authentification et logging

## Gestion de Session

### Authentification JWT
```kotlin
class AuthRepository {
    // Token stocké dans SharedPreferences ET Room
    fun saveUserSession(user: User, token: String) {
        // Room pour persistance
        userDao.insertUser(UserEntity.fromDomainModel(user, token))
        
        // SharedPreferences pour accès rapide
        sharedPreferences.edit()
            .putString(KEY_TOKEN, token)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }
}
```

### Intercepteur Automatique
```kotlin
@Provides
fun provideAuthInterceptor(sharedPreferences: SharedPreferences): Interceptor {
    return Interceptor { chain ->
        val token = sharedPreferences.getString("auth_token", null)
        val requestBuilder = chain.request().newBuilder()
        
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        
        chain.proceed(requestBuilder.build())
    }
}
```

## CRUD Complet avec Mode Hybride

### Exemple : Gestion des Cours

#### 1. **CREATE** (Nécessite connexion)
```kotlin
suspend fun createCourse(title: String, description: String?): Flow<Resource<Course>> = flow {
    if (!networkUtils.isNetworkAvailable()) {
        emit(Resource.Error("Connexion Internet requise"))
        return@flow
    }
    
    val response = courseApiService.createCourse(CreateCourseRequest(title, description))
    if (response.isSuccessful) {
        val newCourse = response.body()?.toDomainModel()
        // Sauvegarder immédiatement en local
        courseDao.insertCourse(CourseEntity.fromDomainModel(newCourse))
        emit(Resource.Success(newCourse))
    }
}
```

#### 2. **READ** (Mode hybride)
```kotlin
suspend fun getAllCourses(forceRefresh: Boolean = false): Flow<Resource<List<Course>>> = flow {
    // Toujours émettre les données locales d'abord
    val localCourses = courseDao.getAllCourses().map { it.toDomainModel() }
    if (localCourses.isNotEmpty() && !forceRefresh) {
        emit(Resource.Success(localCourses))
    }
    
    // Synchroniser avec l'API si possible
    if (networkUtils.isNetworkAvailable()) {
        val remoteCourses = courseApiService.getCourses().body()?.map { it.toDomainModel() }
        courseDao.insertCourses(remoteCourses.map { CourseEntity.fromDomainModel(it) })
        emit(Resource.Success(remoteCourses))
    }
}
```

#### 3. **UPDATE** (Nécessite connexion)
```kotlin
suspend fun updateCourse(courseId: Int, title: String, description: String?): Flow<Resource<Course>> = flow {
    val response = courseApiService.updateCourse(courseId, UpdateCourseRequest(title, description))
    if (response.isSuccessful) {
        val updatedCourse = response.body()?.toDomainModel()
        // Mettre à jour immédiatement en local
        courseDao.updateCourse(CourseEntity.fromDomainModel(updatedCourse))
        emit(Resource.Success(updatedCourse))
    }
}
```

#### 4. **DELETE** (Nécessite connexion)
```kotlin
suspend fun deleteCourse(courseId: Int): Flow<Resource<Unit>> = flow {
    val response = courseApiService.deleteCourse(courseId)
    if (response.isSuccessful) {
        // Supprimer immédiatement en local
        courseDao.deleteCourseById(courseId)
        emit(Resource.Success(Unit))
    }
}
```

## Injection de Dépendances (Hilt)

### Modules Principaux

#### DatabaseModule
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideEduNovaDatabase(@ApplicationContext context: Context): EduNovaDatabase
    
    @Provides
    fun provideUserDao(database: EduNovaDatabase): UserDao
}
```

#### NetworkModule
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideAuthInterceptor(sharedPreferences: SharedPreferences): Interceptor
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit
}
```

## Gestion des Erreurs

### Resource Wrapper
```kotlin
sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
```

### Gestion dans le ViewModel
```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    viewModel.coursesState.collect { resource ->
        when (resource) {
            is Resource.Loading -> showLoading(true)
            is Resource.Success -> {
                showLoading(false)
                updateUI(resource.data)
            }
            is Resource.Error -> {
                showLoading(false)
                showError(resource.message)
            }
        }
    }
}
```

## Synchronisation Hors Ligne

### WorkManager pour Sync
```kotlin
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val courseRepository: CourseRepository
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // Synchroniser toutes les données
            courseRepository.syncAllData()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
```

### Détection de Connectivité
```kotlin
@Singleton
class NetworkUtils @Inject constructor(private val context: Context) {
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
}
```

## Navigation par Rôle

### Configuration Dynamique
```kotlin
private fun setupNavigationForUserRole(userRole: UserRole) {
    val graphResId = when (userRole) {
        UserRole.ETUDIANT -> R.navigation.nav_student
        UserRole.ENSEIGNANT -> R.navigation.nav_teacher
        UserRole.ADMIN -> R.navigation.nav_admin
    }
    
    navController.setGraph(graphResId)
    binding.bottomNavigation.setupWithNavController(navController)
}
```

## Sécurité

### Token Management
- **Stockage sécurisé** : SharedPreferences + Room
- **Expiration automatique** : JWT avec refresh
- **Intercepteur transparent** : Ajout automatique du token

### Validation des Données
- **Côté client** : Validation en temps réel dans les Views
- **Côté serveur** : Validation via l'API
- **Sanitisation** : Nettoyage des entrées utilisateur

## Tests

### Architecture Testable
- **Repository Pattern** : Facilite les mocks
- **Dependency Injection** : Injection de fakes pour les tests
- **Separation of Concerns** : Chaque couche testable indépendamment

### Types de Tests
- **Unit Tests** : ViewModels et Repositories
- **Integration Tests** : Base de données Room
- **UI Tests** : Fragments et Activities avec Espresso

## Performance

### Optimisations
- **Pagination** : Chargement par pages pour les listes
- **Image Loading** : Glide avec cache
- **Database Indexing** : Index sur les colonnes fréquemment utilisées
- **Network Caching** : OkHttp cache pour les réponses API

### Monitoring
- **Crash Reporting** : Firebase Crashlytics
- **Performance Monitoring** : Firebase Performance
- **Analytics** : Firebase Analytics pour l'usage

## Conclusion

Cette architecture garantit :
- ✅ **MVVM strict** avec séparation claire des responsabilités
- ✅ **Mode hybride** fonctionnel hors ligne et en ligne
- ✅ **CRUD complet** pour toutes les entités
- ✅ **Gestion de session** robuste avec JWT
- ✅ **Injection de dépendances** avec Hilt
- ✅ **Gestion d'erreurs** centralisée
- ✅ **Tests** facilités par l'architecture
- ✅ **Performance** optimisée pour mobile