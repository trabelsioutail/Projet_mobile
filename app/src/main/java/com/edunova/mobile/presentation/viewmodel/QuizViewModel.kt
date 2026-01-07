package com.edunova.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edunova.mobile.data.repository.AuthRepository
import com.edunova.mobile.data.repository.QuizRepository
import com.edunova.mobile.domain.model.*
import com.edunova.mobile.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    // État des quiz de l'enseignant
    private val _teacherQuizzesState = MutableStateFlow<Resource<List<Quiz>>?>(null)
    val teacherQuizzesState: StateFlow<Resource<List<Quiz>>?> = _teacherQuizzesState.asStateFlow()
    
    // Quiz sélectionné
    private val _selectedQuiz = MutableStateFlow<Resource<Quiz>?>(null)
    val selectedQuiz: StateFlow<Resource<Quiz>?> = _selectedQuiz.asStateFlow()
    
    // Résultat de soumission de quiz
    private val _submitQuizResult = MutableStateFlow<Resource<QuizSubmission>?>(null)
    val submitQuizResult: StateFlow<Resource<QuizSubmission>?> = _submitQuizResult.asStateFlow()
    
    // Soumissions de l'utilisateur
    private val _mySubmissions = MutableStateFlow<Resource<List<QuizSubmission>>?>(null)
    val mySubmissions: StateFlow<Resource<List<QuizSubmission>>?> = _mySubmissions.asStateFlow()
    
    // État de chargement
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Messages d'erreur
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Messages de succès
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    // Charger les quiz de l'enseignant (utilise getQuizzesByCourse pour simuler)
    fun loadTeacherQuizzes(courseId: Int = 1) {
        viewModelScope.launch {
            // Utiliser la nouvelle méthode simplifiée
            quizRepository.getStudentQuizzes().collect { resource ->
                _teacherQuizzesState.value = resource
                handleResourceState(resource)
            }
        }
    }
    
    // Charger les quiz pour les étudiants
    fun loadStudentQuizzes() {
        viewModelScope.launch {
            _teacherQuizzesState.value = Resource.Loading()
            _isLoading.value = true
            
            quizRepository.getStudentQuizzes().collect { resource ->
                _teacherQuizzesState.value = resource
                _isLoading.value = false
                handleResourceState(resource)
            }
        }
    }
    
    // Charger les détails d'un quiz
    fun loadQuizDetails(quizId: Int) {
        viewModelScope.launch {
            _selectedQuiz.value = Resource.Loading()
            
            // Chercher dans les quiz locaux
            val localQuiz = quizRepository.getLocalQuizById(quizId)
            if (localQuiz != null) {
                _selectedQuiz.value = Resource.Success(localQuiz)
            } else {
                _selectedQuiz.value = Resource.Error("Quiz non trouvé")
            }
        }
    }
    
    // Soumettre un quiz (pour étudiants)
    fun submitQuiz(quizId: Int, answers: Map<Int, String>) {
        viewModelScope.launch {
            _submitQuizResult.value = Resource.Loading()
            
            // Simuler la soumission avec calcul de score
            val quiz = quizRepository.getLocalQuizById(quizId)
            if (quiz != null) {
                var correctAnswers = 0
                var totalQuestions = quiz.questions.size
                
                quiz.questions.forEach { question ->
                    val userAnswer = answers[question.id]
                    if (userAnswer == question.correctAnswer) {
                        correctAnswers++
                    }
                }
                
                val score = if (totalQuestions > 0) (correctAnswers * 100) / totalQuestions else 0
                val submission = QuizSubmission(
                    id = System.currentTimeMillis().toInt(),
                    quizId = quizId,
                    studentId = 27, // Ahmed's ID
                    answers = answers,
                    score = score,
                    earnedPoints = correctAnswers * 10,
                    totalPoints = totalQuestions * 10,
                    passed = score >= (quiz.passingScore?.toInt() ?: 70),
                    submittedAt = System.currentTimeMillis(),
                    attemptNumber = 1
                )
                
                _submitQuizResult.value = Resource.Success(submission)
                _successMessage.value = "Quiz soumis avec succès ! Score: $score%"
            } else {
                _submitQuizResult.value = Resource.Error("Quiz non trouvé")
            }
        }
    }
    
    // Charger mes soumissions
    fun loadMySubmissions() {
        viewModelScope.launch {
            quizRepository.getMySubmissions().collect { resource ->
                _mySubmissions.value = resource
                handleResourceState(resource)
            }
        }
    }
    
    // Méthodes simplifiées pour les fonctionnalités non implémentées
    fun createQuiz(
        courseId: Int,
        title: String,
        description: String?,
        questions: List<QuizQuestion>,
        timeLimit: Int? = null,
        maxAttempts: Int = 1,
        passingScore: Double = 0.0
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Simuler la création avec un nouveau quiz
                kotlinx.coroutines.delay(1000)
                
                // Créer un quiz temporaire pour l'affichage
                val newQuiz = Quiz(
                    id = System.currentTimeMillis().toInt(), // ID temporaire
                    courseId = courseId,
                    teacherId = 1, // ID temporaire
                    title = title,
                    description = description,
                    type = AssignmentType.QUIZ,
                    status = AssignmentStatus.DRAFT,
                    dueDate = null,
                    timeLimit = timeLimit,
                    maxAttempts = maxAttempts,
                    passingScore = passingScore,
                    totalPoints = questions.sumOf { it.points },
                    courseTitle = "Cours $courseId",
                    questions = questions,
                    submissions = emptyList(),
                    userSubmission = null,
                    isCompleted = false,
                    createdAt = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                )
                
                // Ajouter le quiz au repository partagé
                quizRepository.addLocalQuiz(newQuiz)
                
                _isLoading.value = false
                _successMessage.value = "Quiz '$title' créé avec succès!"
                
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Erreur lors de la création du quiz: ${e.message}"
            }
        }
    }
    
    fun updateQuiz(
        quizId: Int,
        title: String,
        description: String?,
        questions: List<QuizQuestion>,
        timeLimit: Int? = null,
        maxAttempts: Int = 1,
        passingScore: Double = 0.0
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            kotlinx.coroutines.delay(1000)
            _isLoading.value = false
            _successMessage.value = "Quiz mis à jour avec succès! (Fonctionnalité en développement)"
        }
    }
    
    fun deleteQuiz(quizId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                kotlinx.coroutines.delay(500)
                
                // Supprimer le quiz du repository partagé
                quizRepository.removeLocalQuiz(quizId)
                
                // Recharger la liste
                loadTeacherQuizzes()
                
                _isLoading.value = false
                _successMessage.value = "Quiz supprimé avec succès!"
                
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Erreur lors de la suppression: ${e.message}"
            }
        }
    }
    
    fun publishQuiz(quizId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                kotlinx.coroutines.delay(1000)
                
                // Mettre à jour le statut du quiz local
                val localQuiz = quizRepository.getLocalQuizById(quizId)
                if (localQuiz != null) {
                    val updatedQuiz = localQuiz.copy(
                        status = if (localQuiz.status == com.edunova.mobile.domain.model.AssignmentStatus.DRAFT) {
                            com.edunova.mobile.domain.model.AssignmentStatus.PUBLISHED
                        } else {
                            com.edunova.mobile.domain.model.AssignmentStatus.DRAFT
                        }
                    )
                    quizRepository.updateLocalQuiz(updatedQuiz)
                    _selectedQuiz.value = Resource.Success(updatedQuiz)
                }
                
                _isLoading.value = false
                _successMessage.value = "Statut du quiz mis à jour avec succès!"
                
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Erreur lors de la mise à jour: ${e.message}"
            }
        }
    }
    
    // Nettoyer les messages d'erreur
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    // Réinitialiser les messages
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
    
    // Réinitialiser les résultats
    fun clearResults() {
        _submitQuizResult.value = null
    }
    
    // Gestion centralisée des états de ressource
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
}