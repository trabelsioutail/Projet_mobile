package com.edunova.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edunova.mobile.data.repository.AdminRepository
import com.edunova.mobile.data.repository.SystemStats
import com.edunova.mobile.data.repository.AdminUser
import com.edunova.mobile.data.repository.AdminCourse
import com.edunova.mobile.data.repository.AdminQuiz
import com.edunova.mobile.data.repository.ActivityReport
import com.edunova.mobile.data.repository.AdminBackup
import com.edunova.mobile.data.repository.AdminEnrollment
import com.edunova.mobile.data.repository.AdminTeacher
import com.edunova.mobile.domain.model.QuizQuestion as DomainQuizQuestion
import com.edunova.mobile.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    // System Statistics
    private val _systemStatsState = MutableStateFlow<Resource<SystemStats>?>(null)
    val systemStatsState: StateFlow<Resource<SystemStats>?> = _systemStatsState.asStateFlow()

    // Users Management
    private val _usersState = MutableStateFlow<Resource<List<AdminUser>>?>(null)
    val usersState: StateFlow<Resource<List<AdminUser>>?> = _usersState.asStateFlow()

    // Courses Management
    private val _coursesState = MutableStateFlow<Resource<List<AdminCourse>>?>(null)
    val coursesState: StateFlow<Resource<List<AdminCourse>>?> = _coursesState.asStateFlow()

    // Quizzes Management
    private val _quizzesState = MutableStateFlow<Resource<List<AdminQuiz>>?>(null)
    val quizzesState: StateFlow<Resource<List<AdminQuiz>>?> = _quizzesState.asStateFlow()

    // Reports
    private val _reportsState = MutableStateFlow<Resource<ActivityReport>?>(null)
    val reportsState: StateFlow<Resource<ActivityReport>?> = _reportsState.asStateFlow()

    // Enrollments Management
    private val _enrollmentsState = MutableStateFlow<Resource<List<AdminEnrollment>>?>(null)
    val enrollmentsState: StateFlow<Resource<List<AdminEnrollment>>?> = _enrollmentsState.asStateFlow()

    // Quiz Statistics
    private val _quizStatisticsState = MutableStateFlow<Resource<com.edunova.mobile.data.repository.QuizStatistics>?>(null)
    val quizStatisticsState: StateFlow<Resource<com.edunova.mobile.data.repository.QuizStatistics>?> = _quizStatisticsState.asStateFlow()

    // Quiz Details
    private val _quizDetailsState = MutableStateFlow<Resource<com.edunova.mobile.data.repository.AdminQuizDetails>?>(null)
    val quizDetailsState: StateFlow<Resource<com.edunova.mobile.data.repository.AdminQuizDetails>?> = _quizDetailsState.asStateFlow()

    // Teachers
    private val _teachersState = MutableStateFlow<Resource<List<AdminTeacher>>?>(null)
    val teachersState: StateFlow<Resource<List<AdminTeacher>>?> = _teachersState.asStateFlow()

    // Action States for user feedback
    private val _userActionState = MutableStateFlow<Resource<String>?>(null)
    val userActionState: StateFlow<Resource<String>?> = _userActionState.asStateFlow()

    private val _courseActionState = MutableStateFlow<Resource<String>?>(null)
    val courseActionState: StateFlow<Resource<String>?> = _courseActionState.asStateFlow()

    private val _enrollmentActionState = MutableStateFlow<Resource<String>?>(null)
    val enrollmentActionState: StateFlow<Resource<String>?> = _enrollmentActionState.asStateFlow()

    private val _quizActionState = MutableStateFlow<Resource<String>?>(null)
    val quizActionState: StateFlow<Resource<String>?> = _quizActionState.asStateFlow()

    // Backup Actions
    private val _backupActionState = MutableStateFlow<Resource<AdminBackup>?>(null)
    val backupActionState: StateFlow<Resource<AdminBackup>?> = _backupActionState.asStateFlow()

    private val _backupsState = MutableStateFlow<Resource<List<AdminBackup>>?>(null)
    val backupsState: StateFlow<Resource<List<AdminBackup>>?> = _backupsState.asStateFlow()

    // ==================== SYSTEM STATISTICS ====================
    
    fun loadSystemStats() {
        viewModelScope.launch {
            adminRepository.getSystemStats().collect { resource ->
                _systemStatsState.value = resource
            }
        }
    }

    // ==================== USERS MANAGEMENT ====================
    
    fun loadAllUsers() {
        viewModelScope.launch {
            adminRepository.getAllUsers().collect { resource ->
                _usersState.value = resource
            }
        }
    }

    fun createUser(firstName: String, lastName: String, email: String, password: String, role: String) {
        viewModelScope.launch {
            adminRepository.createUser(firstName, lastName, email, password, role).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _userActionState.value = Resource.Success("Utilisateur créé avec succès")
                        loadAllUsers() // Refresh the list
                    }
                    is Resource.Error -> {
                        _userActionState.value = Resource.Error(resource.message ?: "Erreur lors de la création")
                    }
                    is Resource.Loading -> {
                        _userActionState.value = Resource.Loading()
                    }
                }
            }
        }
    }

    fun updateUser(userId: Int, firstName: String, lastName: String, email: String, role: String) {
        viewModelScope.launch {
            adminRepository.updateUser(userId, firstName, lastName, email, role).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _userActionState.value = Resource.Success("Utilisateur modifié avec succès")
                        loadAllUsers() // Refresh the list
                    }
                    is Resource.Error -> {
                        _userActionState.value = Resource.Error(resource.message ?: "Erreur lors de la modification")
                    }
                    is Resource.Loading -> {
                        _userActionState.value = Resource.Loading()
                    }
                }
            }
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            adminRepository.deleteUser(userId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _userActionState.value = Resource.Success("Utilisateur supprimé avec succès")
                        loadAllUsers() // Refresh the list
                    }
                    is Resource.Error -> {
                        _userActionState.value = Resource.Error(resource.message ?: "Erreur lors de la suppression")
                    }
                    is Resource.Loading -> {
                        _userActionState.value = Resource.Loading()
                    }
                }
            }
        }
    }

    // ==================== COURSES MANAGEMENT ====================
    
    fun loadAllCourses() {
        viewModelScope.launch {
            adminRepository.getAllCourses().collect { resource ->
                _coursesState.value = resource
            }
        }
    }

    fun createCourse(
        title: String, 
        description: String, 
        teacherId: Int, 
        status: String = "active",
        isPublic: Boolean = true,
        enrollmentOpen: Boolean = true
    ) {
        viewModelScope.launch {
            adminRepository.createCourse(title, description, teacherId, status, isPublic, enrollmentOpen).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _courseActionState.value = Resource.Success("Cours créé avec succès")
                        loadAllCourses() // Refresh the list
                    }
                    is Resource.Error -> {
                        _courseActionState.value = Resource.Error(resource.message ?: "Erreur lors de la création")
                    }
                    is Resource.Loading -> {
                        _courseActionState.value = Resource.Loading()
                    }
                }
            }
        }
    }

    fun updateCourse(
        courseId: Int, 
        title: String, 
        description: String, 
        teacherId: Int, 
        status: String = "active",
        isPublic: Boolean = true,
        enrollmentOpen: Boolean = true
    ) {
        viewModelScope.launch {
            adminRepository.updateCourse(courseId, title, description, teacherId, status, isPublic, enrollmentOpen).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _courseActionState.value = Resource.Success("Cours modifié avec succès")
                        loadAllCourses() // Refresh the list
                    }
                    is Resource.Error -> {
                        _courseActionState.value = Resource.Error(resource.message ?: "Erreur lors de la modification")
                    }
                    is Resource.Loading -> {
                        _courseActionState.value = Resource.Loading()
                    }
                }
            }
        }
    }

    fun deleteCourse(courseId: Int) {
        viewModelScope.launch {
            adminRepository.deleteCourse(courseId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _courseActionState.value = Resource.Success("Cours supprimé avec succès")
                        loadAllCourses() // Refresh the list
                    }
                    is Resource.Error -> {
                        _courseActionState.value = Resource.Error(resource.message ?: "Erreur lors de la suppression")
                    }
                    is Resource.Loading -> {
                        _courseActionState.value = Resource.Loading()
                    }
                }
            }
        }
    }

    fun toggleCourseStatus(courseId: Int, newStatus: String) {
        viewModelScope.launch {
            adminRepository.toggleCourseStatus(courseId, newStatus).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _courseActionState.value = Resource.Success("Statut du cours modifié avec succès")
                        loadAllCourses() // Refresh the list
                    }
                    is Resource.Error -> {
                        _courseActionState.value = Resource.Error(resource.message ?: "Erreur lors du changement de statut")
                    }
                    is Resource.Loading -> {
                        _courseActionState.value = Resource.Loading()
                    }
                }
            }
        }
    }

    // ==================== TEACHERS MANAGEMENT ====================
    
    fun loadAllTeachers() {
        viewModelScope.launch {
            adminRepository.getAllTeachers().collect { resource ->
                _teachersState.value = resource
            }
        }
    }

    // ==================== ENROLLMENT MANAGEMENT ====================
    
    fun loadAllEnrollments() {
        viewModelScope.launch {
            adminRepository.getAllEnrollments().collect { resource ->
                _enrollmentsState.value = resource
            }
        }
    }

    fun createEnrollment(courseId: Int, studentId: Int, status: String = "active") {
        viewModelScope.launch {
            adminRepository.createEnrollment(courseId, studentId, status).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _enrollmentActionState.value = Resource.Success("Inscription créée avec succès")
                        loadAllEnrollments() // Refresh the list
                    }
                    is Resource.Error -> {
                        _enrollmentActionState.value = Resource.Error(resource.message ?: "Erreur lors de la création")
                    }
                    is Resource.Loading -> {
                        _enrollmentActionState.value = Resource.Loading()
                    }
                }
            }
        }
    }

    fun deleteEnrollment(enrollmentId: Int) {
        viewModelScope.launch {
            adminRepository.deleteEnrollment(enrollmentId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _enrollmentActionState.value = Resource.Success("Inscription supprimée avec succès")
                        loadAllEnrollments() // Refresh the list
                    }
                    is Resource.Error -> {
                        _enrollmentActionState.value = Resource.Error(resource.message ?: "Erreur lors de la suppression")
                    }
                    is Resource.Loading -> {
                        _enrollmentActionState.value = Resource.Loading()
                    }
                }
            }
        }
    }

    fun updateEnrollment(enrollmentId: Int, status: String) {
        viewModelScope.launch {
            adminRepository.updateEnrollment(enrollmentId, status).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _enrollmentActionState.value = Resource.Success("Inscription mise à jour avec succès")
                        loadAllEnrollments() // Refresh the list
                    }
                    is Resource.Error -> {
                        _enrollmentActionState.value = Resource.Error(resource.message ?: "Erreur lors de la mise à jour")
                    }
                    is Resource.Loading -> {
                        _enrollmentActionState.value = Resource.Loading()
                    }
                }
            }
        }
    }

    // ==================== QUIZZES MANAGEMENT ====================
    
    fun loadAllQuizzes() {
        viewModelScope.launch {
            adminRepository.getAllQuizzes().collect { resource ->
                _quizzesState.value = resource
            }
        }
    }
    
    fun createQuiz(
        title: String,
        description: String,
        courseId: Int,
        timeLimit: Int = 30,
        maxAttempts: Int = 3,
        passingScore: Int = 60,
        questions: List<DomainQuizQuestion> = emptyList()
    ) {
        viewModelScope.launch {
            adminRepository.createQuiz(title, description, courseId, timeLimit, maxAttempts, passingScore, questions).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _quizActionState.value = Resource.Success("Quiz créé avec succès")
                        loadAllQuizzes() // Refresh the list
                    }
                    is Resource.Error -> {
                        _quizActionState.value = Resource.Error(resource.message ?: "Erreur lors de la création")
                    }
                    is Resource.Loading -> {
                        _quizActionState.value = Resource.Loading()
                    }
                }
            }
        }
    }
    
    fun loadQuizStatistics(quizId: Int) {
        viewModelScope.launch {
            adminRepository.getQuizStatistics(quizId).collect { resource ->
                _quizStatisticsState.value = resource
            }
        }
    }
    
    fun getQuizDetails(quizId: Int) {
        viewModelScope.launch {
            adminRepository.getQuizDetails(quizId).collect { resource ->
                _quizDetailsState.value = resource
            }
        }
    }
    
    fun updateQuiz(
        quizId: Int,
        title: String,
        description: String,
        timeLimit: Int,
        maxAttempts: Int,
        passingScore: Int
    ) {
        viewModelScope.launch {
            adminRepository.updateQuiz(quizId, title, description, timeLimit, maxAttempts, passingScore).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _quizActionState.value = Resource.Success("Quiz modifié avec succès")
                        loadAllQuizzes() // Refresh the list
                    }
                    is Resource.Error -> {
                        _quizActionState.value = Resource.Error(resource.message ?: "Erreur lors de la modification")
                    }
                    is Resource.Loading -> {
                        _quizActionState.value = Resource.Loading()
                    }
                }
            }
        }
    }
    
    fun deleteQuiz(quizId: Int) {
        viewModelScope.launch {
            adminRepository.deleteQuiz(quizId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _quizActionState.value = Resource.Success("Quiz supprimé avec succès")
                        loadAllQuizzes() // Refresh the list
                    }
                    is Resource.Error -> {
                        _quizActionState.value = Resource.Error(resource.message ?: "Erreur lors de la suppression")
                    }
                    is Resource.Loading -> {
                        _quizActionState.value = Resource.Loading()
                    }
                }
            }
        }
    }
    
    fun toggleQuizStatus(quizId: Int) {
        viewModelScope.launch {
            adminRepository.toggleQuizStatus(quizId).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _quizActionState.value = Resource.Success("Statut du quiz modifié avec succès")
                        loadAllQuizzes() // Refresh the list
                    }
                    is Resource.Error -> {
                        _quizActionState.value = Resource.Error(resource.message ?: "Erreur lors du changement de statut")
                    }
                    is Resource.Loading -> {
                        _quizActionState.value = Resource.Loading()
                    }
                }
            }
        }
    }

    // ==================== REPORTS ====================
    
    fun loadActivityReport() {
        viewModelScope.launch {
            adminRepository.getActivityReport().collect { resource ->
                _reportsState.value = resource
            }
        }
    }

    // ==================== BACKUP MANAGEMENT ====================
    
    fun loadAllBackups() {
        viewModelScope.launch {
            adminRepository.getAllBackups().collect { resource ->
                _backupsState.value = resource
            }
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            adminRepository.createBackup().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _backupActionState.value = resource
                        loadAllBackups() // Refresh the list
                    }
                    is Resource.Error -> {
                        _backupActionState.value = Resource.Error(resource.message ?: "Erreur lors de la création de la sauvegarde")
                    }
                    is Resource.Loading -> {
                        _backupActionState.value = Resource.Loading()
                    }
                }
            }
        }
    }

    // ==================== UTILITY FUNCTIONS ====================
    
    fun clearUserActionState() {
        _userActionState.value = null
    }

    fun clearCourseActionState() {
        _courseActionState.value = null
    }

    fun clearEnrollmentActionState() {
        _enrollmentActionState.value = null
    }
    
    fun clearQuizActionState() {
        _quizActionState.value = null
    }

    fun clearBackupActionState() {
        _backupActionState.value = null
    }
}