package com.edunova.mobile.data.repository

import android.content.Context
import com.edunova.mobile.data.local.dao.QuizDao
import com.edunova.mobile.data.remote.api.QuizApiService
import com.edunova.mobile.data.remote.dto.QuizDto
import com.edunova.mobile.domain.model.*
import com.edunova.mobile.utils.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor(
    private val quizApiService: QuizApiService,
    private val quizDao: QuizDao,
    @ApplicationContext private val context: Context
) {
    
    // SharedPreferences pour la sauvegarde permanente
    private val sharedPrefs = context.getSharedPreferences("quiz_storage", Context.MODE_PRIVATE)
    private val gson = com.google.gson.Gson()
    
    // Liste temporaire des quiz créés localement
    private val _localQuizzes = mutableListOf<Quiz>()
    
    init {
        // Charger les quiz sauvegardés au démarrage
        loadSavedQuizzes()
    }
    
    // Charger les quiz depuis SharedPreferences
    private fun loadSavedQuizzes() {
        try {
            val savedQuizzesJson = sharedPrefs.getString("saved_quizzes", null)
            if (savedQuizzesJson != null) {
                val type = object : com.google.gson.reflect.TypeToken<List<Quiz>>() {}.type
                val savedQuizzes: List<Quiz> = gson.fromJson(savedQuizzesJson, type)
                _localQuizzes.clear()
                _localQuizzes.addAll(savedQuizzes)
            }
        } catch (e: Exception) {
            // En cas d'erreur, continuer avec une liste vide
            _localQuizzes.clear()
        }
    }
    
    // Sauvegarder les quiz dans SharedPreferences
    private fun saveQuizzesToStorage() {
        try {
            val quizzesJson = gson.toJson(_localQuizzes)
            sharedPrefs.edit()
                .putString("saved_quizzes", quizzesJson)
                .apply()
        } catch (e: Exception) {
            // Log l'erreur mais ne pas crasher
        }
    }
    
    // Ajouter un quiz créé localement
    fun addLocalQuiz(quiz: Quiz) {
        _localQuizzes.add(0, quiz) // Ajouter en premier
        saveQuizzesToStorage() // Sauvegarder immédiatement
    }
    
    // Supprimer un quiz local
    fun removeLocalQuiz(quizId: Int) {
        _localQuizzes.removeAll { it.id == quizId }
        saveQuizzesToStorage() // Sauvegarder immédiatement
    }
    
    // Mettre à jour un quiz local
    fun updateLocalQuiz(updatedQuiz: Quiz) {
        val index = _localQuizzes.indexOfFirst { it.id == updatedQuiz.id }
        if (index != -1) {
            _localQuizzes[index] = updatedQuiz
            saveQuizzesToStorage() // Sauvegarder immédiatement
        }
    }
    
    // Obtenir un quiz local par ID
    fun getLocalQuizById(quizId: Int): Quiz? {
        return _localQuizzes.find { it.id == quizId }
    }

    fun getQuizzesByCourse(courseId: Int): Flow<Resource<List<Quiz>>> = flow {
        emit(Resource.Loading())
        
        try {
            // Essayer de récupérer depuis l'API
            val response = quizApiService.getStudentQuizzes()
            if (response.isSuccessful) {
                val apiQuizzes = response.body()?.map { it.toDomainModel() } ?: emptyList()
                
                // Combiner avec les quiz locaux
                val allQuizzes = _localQuizzes + apiQuizzes
                
                // Sauvegarder en local (seulement les quiz de l'API)
                if (apiQuizzes.isNotEmpty()) {
                    quizDao.insertQuizzes(apiQuizzes.map { it.toEntity() })
                }
                
                emit(Resource.Success(allQuizzes))
            } else {
                // Fallback vers les données locales + quiz temporaires
                val localQuizzes = quizDao.getCourseQuizzes(courseId).map { it.toDomainModel() }
                val allQuizzes = _localQuizzes + localQuizzes
                emit(Resource.Success(allQuizzes))
            }
        } catch (e: Exception) {
            // Fallback vers les données locales + quiz temporaires
            try {
                val localQuizzes = quizDao.getCourseQuizzes(courseId).map { it.toDomainModel() }
                val allQuizzes = _localQuizzes + localQuizzes
                emit(Resource.Success(allQuizzes))
            } catch (localException: Exception) {
                // Au minimum, retourner les quiz temporaires
                emit(Resource.Success(_localQuizzes))
            }
        }
    }

    fun getQuizById(quizId: Int): Flow<Resource<Quiz>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = quizApiService.getQuizById(quizId)
            if (response.isSuccessful) {
                val quiz = response.body()?.toDomainModel()
                if (quiz != null) {
                    quizDao.insertQuiz(quiz.toEntity())
                    emit(Resource.Success(quiz))
                } else {
                    emit(Resource.Error("Quiz non trouvé"))
                }
            } else {
                val localQuiz = quizDao.getQuizById(quizId)?.toDomainModel()
                if (localQuiz != null) {
                    emit(Resource.Success(localQuiz))
                } else {
                    emit(Resource.Error("Quiz non trouvé"))
                }
            }
        } catch (e: Exception) {
            try {
                val localQuiz = quizDao.getQuizById(quizId)?.toDomainModel()
                if (localQuiz != null) {
                    emit(Resource.Success(localQuiz))
                } else {
                    emit(Resource.Error("Quiz non trouvé"))
                }
            } catch (localException: Exception) {
                emit(Resource.Error("Erreur: ${e.message}"))
            }
        }
    }

    fun submitQuiz(quizId: Int, answers: Map<String, String>): Flow<Resource<com.edunova.mobile.domain.model.QuizSubmission>> = flow {
        emit(Resource.Loading())
        
        try {
            val request = com.edunova.mobile.data.remote.dto.SubmitQuizRequest(answers)
            val response = quizApiService.submitQuiz(quizId, request)
            if (response.isSuccessful) {
                val submission = response.body()?.toDomainModel()
                if (submission != null) {
                    emit(Resource.Success(submission))
                } else {
                    emit(Resource.Error("Erreur lors de la soumission"))
                }
            } else {
                emit(Resource.Error("Erreur lors de la soumission"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Erreur de connexion: ${e.message}"))
        }
    }

    fun getMySubmissions(): Flow<Resource<List<com.edunova.mobile.domain.model.QuizSubmission>>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = quizApiService.getStudentBadges() // This needs to be changed to proper submissions endpoint
            if (response.isSuccessful) {
                // For now, return empty list since we don't have proper submissions endpoint
                emit(Resource.Success(emptyList<com.edunova.mobile.domain.model.QuizSubmission>()))
            } else {
                emit(Resource.Error("Erreur lors du chargement des soumissions"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Erreur de connexion: ${e.message}"))
        }
    }
    
    // Nouvelles méthodes utilisant l'API mobile (simplifiées)
    fun getStudentQuizzes(): Flow<Resource<List<Quiz>>> = flow {
        emit(Resource.Loading())
        
        // Pour l'instant, utiliser les quiz locaux existants
        val localQuizzes = _localQuizzes.toList()
        if (localQuizzes.isNotEmpty()) {
            emit(Resource.Success(localQuizzes))
        } else {
            // Créer des quiz de démonstration
            val demoQuizzes = createDemoQuizzes()
            _localQuizzes.addAll(demoQuizzes)
            saveQuizzesToStorage()
            emit(Resource.Success(demoQuizzes))
        }
    }
    
    private fun createDemoQuizzes(): List<Quiz> {
        return listOf(
            Quiz(
                id = 1,
                title = "Quiz Mathématiques",
                description = "Test sur les équations du second degré",
                courseId = 1,
                teacherId = 1,
                timeLimit = 30,
                maxAttempts = 3,
                passingScore = 70.0,
                questions = listOf(
                    QuizQuestion(
                        id = 1,
                        assignmentId = 1,
                        questionText = "Résoudre l'équation: x² - 5x + 6 = 0",
                        questionType = QuestionType.MULTIPLE_CHOICE,
                        options = listOf(
                            QuizOption(id = "a", text = "x = 2 et x = 3", isCorrect = true),
                            QuizOption(id = "b", text = "x = 1 et x = 6", isCorrect = false),
                            QuizOption(id = "c", text = "x = -2 et x = -3", isCorrect = false)
                        ),
                        correctAnswer = "a",
                        points = 10,
                        orderIndex = 1
                    )
                ),
                createdAt = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            ),
            Quiz(
                id = 2,
                title = "Quiz JavaScript",
                description = "Variables et fonctions de base",
                courseId = 2,
                teacherId = 1,
                timeLimit = 25,
                maxAttempts = 2,
                passingScore = 60.0,
                questions = listOf(
                    QuizQuestion(
                        id = 2,
                        assignmentId = 2,
                        questionText = "Comment déclarer une variable en JavaScript ?",
                        questionType = QuestionType.MULTIPLE_CHOICE,
                        options = listOf(
                            QuizOption(id = "a", text = "var nom;", isCorrect = true),
                            QuizOption(id = "b", text = "variable nom;", isCorrect = false),
                            QuizOption(id = "c", text = "declare nom;", isCorrect = false)
                        ),
                        correctAnswer = "a",
                        points = 5,
                        orderIndex = 1
                    )
                ),
                createdAt = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            )
        )
    }
}