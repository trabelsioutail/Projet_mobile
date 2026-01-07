package com.edunova.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edunova.mobile.data.repository.AuthRepository
import com.edunova.mobile.data.repository.CourseRepository
import com.edunova.mobile.domain.model.Course
import com.edunova.mobile.domain.model.User
import com.edunova.mobile.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardStats(
    val totalCourses: Int = 0,
    val totalStudents: Int = 0,
    val totalResources: Int = 0,
    val pendingEvaluations: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    // État des statistiques
    private val _dashboardStats = MutableStateFlow(DashboardStats())
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats.asStateFlow()
    
    // État de chargement
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Messages d'erreur
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Utilisateur actuel
    val currentUser: StateFlow<User?> = authRepository.getCurrentUserFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // Cours récents (limités à 3)
    @OptIn(ExperimentalCoroutinesApi::class)
    val recentCourses: Flow<List<Course>> = authRepository.getCurrentUserFlow()
        .filterNotNull()
        .flatMapLatest { user ->
            courseRepository.getTeacherCoursesFlow(user.id)
                .map { courses -> courses.take(3) }
        }
    
    init {
        loadDashboardData()
    }
    
    fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                // Charger les cours pour calculer les statistiques
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    courseRepository.getTeacherCourses(currentUser.id, forceRefresh = true)
                        .collect { resource ->
                            when (resource) {
                                is Resource.Success -> {
                                    val courses = resource.data ?: emptyList()
                                    updateStats(courses)
                                }
                                is Resource.Error -> {
                                    _errorMessage.value = resource.message
                                }
                                is Resource.Loading -> {
                                    // Géré par _isLoading
                                }
                            }
                        }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Erreur inconnue"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun updateStats(courses: List<Course>) {
        val stats = DashboardStats(
            totalCourses = courses.size,
            totalStudents = courses.sumOf { it.studentsCount },
            totalResources = courses.sumOf { it.contentsCount },
            pendingEvaluations = 0 // TODO: Implémenter quand l'API sera disponible
        )
        _dashboardStats.value = stats
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}