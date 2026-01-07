package com.edunova.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edunova.mobile.data.repository.AuthRepository
import com.edunova.mobile.data.repository.CourseRepository
import com.edunova.mobile.domain.model.Course
import com.edunova.mobile.domain.model.UserRole
import com.edunova.mobile.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    // État des cours
    private val _coursesState = MutableStateFlow<Resource<List<Course>>?>(null)
    val coursesState: StateFlow<Resource<List<Course>>?> = _coursesState.asStateFlow()
    
    // Cours sélectionné
    private val _selectedCourse = MutableStateFlow<Resource<Course>?>(null)
    val selectedCourse: StateFlow<Resource<Course>?> = _selectedCourse.asStateFlow()
    
    // État des quiz (pour admin)
    private val _quizzesState = MutableStateFlow<Resource<List<Any>>?>(null)
    val quizzesState: StateFlow<Resource<List<Any>>?> = _quizzesState.asStateFlow()
    
    // État de chargement
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Messages d'erreur
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Messages de succès
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    // Observer les cours selon le rôle de l'utilisateur
    val coursesFlow: Flow<List<Course>> = authRepository.getCurrentUserFlow()
        .filterNotNull()
        .flatMapLatest { user ->
            when (user.role) {
                UserRole.ENSEIGNANT -> courseRepository.getTeacherCoursesFlow(user.id)
                UserRole.ETUDIANT -> courseRepository.getEnrolledCoursesFlow()
                UserRole.ADMIN -> courseRepository.getAllCoursesFlow()
            }
        }
        .catch { emit(emptyList()) } // En cas d'erreur, émettre une liste vide
    
    // Observer les contenus d'un cours
    private val _courseContents = MutableStateFlow<List<com.edunova.mobile.domain.model.CourseContent>>(emptyList())
    val courseContents: StateFlow<List<com.edunova.mobile.domain.model.CourseContent>> = _courseContents.asStateFlow()
    
    // Observer les étudiants d'un cours
    private val _courseStudents = MutableStateFlow<List<com.edunova.mobile.domain.model.User>>(emptyList())
    val courseStudents: StateFlow<List<com.edunova.mobile.domain.model.User>> = _courseStudents.asStateFlow()
    
    init {
        loadCourses()
    }
    
    // Charger tous les cours (pour admin)
    fun loadAllCourses() {
        viewModelScope.launch {
            courseRepository.getAllCourses(forceRefresh = true).collect { resource ->
                _coursesState.value = resource
                handleResourceState(resource)
            }
        }
    }
    
    // Charger tous les quiz (pour admin)
    fun loadAllQuizzes() {
        // Cette méthode sera implémentée quand nous aurons un QuizRepository global
        // Pour l'instant, on peut la laisser vide ou retourner une liste vide
        _quizzesState.value = Resource.Success(emptyList())
    }
    
    // Charger les détails d'un cours
    fun loadCourseDetails(courseId: Int) {
        viewModelScope.launch {
            courseRepository.getCourseById(courseId).collect { resource ->
                _selectedCourse.value = resource
                handleResourceState(resource)
            }
        }
    }
    
    // Charger les contenus d'un cours
    fun loadCourseContents(courseId: Int) {
        viewModelScope.launch {
            // Simuler le chargement des contenus
            _courseContents.value = emptyList()
        }
    }
    
    // Charger les étudiants d'un cours
    fun loadCourseStudents(courseId: Int) {
        viewModelScope.launch {
            // Simuler le chargement des étudiants
            _courseStudents.value = emptyList()
        }
    }
    
    // Ouvrir un contenu de cours
    fun openContent(content: com.edunova.mobile.domain.model.CourseContent) {
        // TODO: Implémenter l'ouverture de contenu selon le type
        when (content.contentType) {
            com.edunova.mobile.domain.model.ContentType.PDF -> {
                // Ouvrir PDF
            }
            com.edunova.mobile.domain.model.ContentType.VIDEO -> {
                // Ouvrir vidéo
            }
            com.edunova.mobile.domain.model.ContentType.DOCUMENT -> {
                // Ouvrir document
            }
            com.edunova.mobile.domain.model.ContentType.LINK -> {
                // Ouvrir lien
            }
        }
    }

    // Charger les cours de l'étudiant
    fun loadStudentCourses(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _coursesState.value = Resource.Loading()
            _isLoading.value = true
            
            courseRepository.getEnrolledCoursesFlow().collect { courses ->
                _coursesState.value = Resource.Success(courses)
                _isLoading.value = false
            }
        }
    }
    
    // Nettoyer les messages d'erreur
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    // Charger les cours de l'enseignant
    fun loadTeacherCourses() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                courseRepository.getTeacherCourses(currentUser.id, forceRefresh = true).collect { resource ->
                    _coursesState.value = resource
                    handleResourceState(resource)
                }
            }
        }
    }
    
    // Charger les cours
    fun loadCourses(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                when (currentUser.role) {
                    UserRole.ENSEIGNANT -> {
                        courseRepository.getTeacherCourses(currentUser.id, forceRefresh).collect { resource ->
                            _coursesState.value = resource
                            handleResourceState(resource)
                        }
                    }
                    UserRole.ETUDIANT -> {
                        courseRepository.getAllCourses(forceRefresh).collect { resource ->
                            _coursesState.value = resource
                            handleResourceState(resource)
                        }
                    }
                    UserRole.ADMIN -> {
                        courseRepository.getAllCourses(forceRefresh).collect { resource ->
                            _coursesState.value = resource
                            handleResourceState(resource)
                        }
                    }
                }
            }
        }
    }
    
    // Charger un cours spécifique
    fun loadCourse(courseId: Int) {
        viewModelScope.launch {
            courseRepository.getCourseById(courseId).collect { resource ->
                _selectedCourse.value = resource
                handleResourceState(resource)
            }
        }
    }
    
    // Créer un cours (enseignants seulement)
    fun createCourse(title: String, description: String?) {
        viewModelScope.launch {
            courseRepository.createCourse(title, description).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _successMessage.value = "Cours créé avec succès"
                        loadCourses(forceRefresh = true) // Recharger la liste
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
    
    // Mettre à jour un cours (enseignants seulement)
    fun updateCourse(courseId: Int, title: String, description: String?) {
        viewModelScope.launch {
            courseRepository.updateCourse(courseId, title, description).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _successMessage.value = "Cours modifié avec succès"
                        loadCourse(courseId) // Recharger le cours
                        loadCourses(forceRefresh = true) // Recharger la liste
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
    
    // Supprimer un cours (enseignants seulement)
    fun deleteCourse(courseId: Int) {
        viewModelScope.launch {
            courseRepository.deleteCourse(courseId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _successMessage.value = "Cours supprimé avec succès"
                        loadCourses(forceRefresh = true) // Recharger la liste
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
    
    // S'inscrire à un cours (étudiants seulement)
    fun enrollInCourse(courseId: Int) {
        viewModelScope.launch {
            courseRepository.enrollInCourse(courseId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _successMessage.value = "Inscription réussie"
                        loadCourse(courseId) // Recharger le cours
                        loadCourses(forceRefresh = true) // Recharger la liste
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
    
    // Se désinscrire d'un cours (étudiants seulement)
    fun unenrollFromCourse(courseId: Int) {
        viewModelScope.launch {
            courseRepository.unenrollFromCourse(courseId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _successMessage.value = "Désinscription réussie"
                        loadCourse(courseId) // Recharger le cours
                        loadCourses(forceRefresh = true) // Recharger la liste
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
    
    // Marquer un cours comme disponible hors ligne
    fun toggleOfflineAvailability(courseId: Int, isAvailable: Boolean) {
        viewModelScope.launch {
            courseRepository.markCourseOfflineAvailable(courseId, isAvailable)
            val message = if (isAvailable) {
                "Cours disponible hors ligne"
            } else {
                "Cours retiré du mode hors ligne"
            }
            _successMessage.value = message
        }
    }
    
    // Actualiser les cours
    fun refreshCourses() {
        loadCourses(forceRefresh = true)
    }
    
    // Rechercher des cours
    fun searchCourses(query: String): Flow<List<Course>> {
        return coursesFlow.map { courses ->
            if (query.isBlank()) {
                courses
            } else {
                courses.filter { course ->
                    course.title.contains(query, ignoreCase = true) ||
                    course.description?.contains(query, ignoreCase = true) == true ||
                    course.teacherName?.contains(query, ignoreCase = true) == true
                }
            }
        }
    }
    
    // Filtrer les cours par statut d'inscription (pour les étudiants)
    fun filterCoursesByEnrollment(showEnrolledOnly: Boolean): Flow<List<Course>> {
        return coursesFlow.map { courses ->
            if (showEnrolledOnly) {
                courses.filter { it.isEnrolled }
            } else {
                courses
            }
        }
    }
    
    // Trier les cours
    fun sortCourses(sortBy: CourseSortOption): Flow<List<Course>> {
        return coursesFlow.map { courses ->
            when (sortBy) {
                CourseSortOption.TITLE_ASC -> courses.sortedBy { it.title }
                CourseSortOption.TITLE_DESC -> courses.sortedByDescending { it.title }
                CourseSortOption.DATE_ASC -> courses.sortedBy { it.createdAt }
                CourseSortOption.DATE_DESC -> courses.sortedByDescending { it.createdAt }
                CourseSortOption.STUDENTS_COUNT -> courses.sortedByDescending { it.studentsCount }
                CourseSortOption.PROGRESS -> courses.sortedByDescending { it.progress }
            }
        }
    }
    
    // Gérer l'état des ressources
    private fun <T> handleResourceState(resource: Resource<T>) {
        when (resource) {
            is Resource.Loading -> {
                _isLoading.value = true
                _errorMessage.value = null
            }
            is Resource.Success -> {
                _isLoading.value = false
                _errorMessage.value = null
            }
            is Resource.Error -> {
                _isLoading.value = false
                _errorMessage.value = resource.message
            }
        }
    }
    
    // Nettoyer les messages
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
    
    // Nettoyer le cours sélectionné
    fun clearSelectedCourse() {
        _selectedCourse.value = null
    }
}

enum class CourseSortOption {
    TITLE_ASC,
    TITLE_DESC,
    DATE_ASC,
    DATE_DESC,
    STUDENTS_COUNT,
    PROGRESS
}