package com.edunova.mobile.data.repository

import com.edunova.mobile.data.remote.api.AdminApiService
import com.edunova.mobile.data.remote.dto.*
import com.edunova.mobile.domain.model.*
import com.edunova.mobile.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val adminApiService: AdminApiService
) {
    
    // ==================== STATISTIQUES ====================
    
    fun getSystemStats(): Flow<Resource<SystemStats>> = flow {
        try {
            emit(Resource.Loading())
            val response = adminApiService.getAdminStats()
            if (response.success) {
                val stats = SystemStats(
                    totalUsers = response.data.totalUsers,
                    totalCourses = response.data.totalCourses,
                    totalQuizzes = response.data.totalQuizzes,
                    totalEnrollments = response.data.totalEnrollments
                )
                emit(Resource.Success(stats))
            } else {
                emit(Resource.Error("Erreur lors de la récupération des statistiques"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    // ==================== GESTION DES UTILISATEURS ====================
    
    fun getAllUsers(): Flow<Resource<List<AdminUser>>> = flow {
        try {
            emit(Resource.Loading())
            val response = adminApiService.getAdminUsers()
            if (response.success) {
                val users = response.data.map { userDto ->
                    AdminUser(
                        id = userDto.id,
                        firstName = userDto.first_name,
                        lastName = userDto.last_name,
                        email = userDto.email,
                        role = userDto.role,
                        createdAt = userDto.created_at,
                        updatedAt = userDto.updated_at
                    )
                }
                emit(Resource.Success(users))
            } else {
                emit(Resource.Error("Erreur lors de la récupération des utilisateurs"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    fun createUser(firstName: String, lastName: String, email: String, password: String, role: String): Flow<Resource<AdminUser>> = flow {
        try {
            emit(Resource.Loading())
            val request = CreateUserRequestDto(firstName, lastName, email, password, role)
            val response = adminApiService.createUser(request)
            if (response.success) {
                val user = AdminUser(
                    id = response.data.id,
                    firstName = response.data.firstName,
                    lastName = response.data.lastName,
                    email = response.data.email,
                    role = response.data.role,
                    createdAt = "",
                    updatedAt = ""
                )
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("Erreur lors de la création de l'utilisateur"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    fun updateUser(userId: Int, firstName: String, lastName: String, email: String, role: String): Flow<Resource<AdminUser>> = flow {
        try {
            emit(Resource.Loading())
            val request = UpdateUserRequestDto(firstName, lastName, email, role)
            val response = adminApiService.updateUser(userId, request)
            if (response.success) {
                val user = AdminUser(
                    id = response.data.id,
                    firstName = response.data.firstName,
                    lastName = response.data.lastName,
                    email = response.data.email,
                    role = response.data.role,
                    createdAt = "",
                    updatedAt = ""
                )
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("Erreur lors de la modification de l'utilisateur"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    fun deleteUser(userId: Int): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            val response = adminApiService.deleteUser(userId)
            if (response.success) {
                emit(Resource.Success("Utilisateur supprimé avec succès"))
            } else {
                emit(Resource.Error("Erreur lors de la suppression de l'utilisateur"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    // ==================== GESTION DES COURS ====================
    
    fun getAllCourses(): Flow<Resource<List<AdminCourse>>> = flow {
        try {
            emit(Resource.Loading())
            val response = adminApiService.getAdminCourses()
            if (response.success) {
                val courses = response.data.map { courseDto ->
                    AdminCourse(
                        id = courseDto.id,
                        title = courseDto.title,
                        description = courseDto.description,
                        teacherName = "${courseDto.first_name ?: ""} ${courseDto.last_name ?: ""}".trim(),
                        teacherId = courseDto.teacher_id ?: 0,
                        enrollmentCount = courseDto.enrollment_count,
                        status = courseDto.status ?: "active",
                        isPublic = courseDto.is_public ?: true,
                        enrollmentOpen = courseDto.enrollment_open ?: true,
                        createdAt = courseDto.created_at
                    )
                }
                emit(Resource.Success(courses))
            } else {
                emit(Resource.Error("Erreur lors de la récupération des cours"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    fun createCourse(
        title: String, 
        description: String, 
        teacherId: Int, 
        status: String = "active",
        isPublic: Boolean = true,
        enrollmentOpen: Boolean = true
    ): Flow<Resource<AdminCourse>> = flow {
        try {
            emit(Resource.Loading())
            val request = CreateCourseRequestDto(title, description, teacherId, status, isPublic, enrollmentOpen)
            val response = adminApiService.createCourse(request)
            if (response.success) {
                val course = AdminCourse(
                    id = response.data.id,
                    title = response.data.title,
                    description = response.data.description,
                    teacherName = "",
                    teacherId = response.data.teacherId,
                    enrollmentCount = 0,
                    status = response.data.status,
                    isPublic = isPublic,
                    enrollmentOpen = enrollmentOpen,
                    createdAt = ""
                )
                emit(Resource.Success(course))
            } else {
                emit(Resource.Error("Erreur lors de la création du cours"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
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
    ): Flow<Resource<AdminCourse>> = flow {
        try {
            emit(Resource.Loading())
            val request = UpdateCourseRequestDto(title, description, teacherId, status, isPublic, enrollmentOpen)
            val response = adminApiService.updateCourse(courseId, request)
            if (response.success) {
                val course = AdminCourse(
                    id = response.data.id,
                    title = response.data.title,
                    description = response.data.description,
                    teacherName = "",
                    teacherId = response.data.teacherId,
                    enrollmentCount = 0,
                    status = response.data.status,
                    isPublic = isPublic,
                    enrollmentOpen = enrollmentOpen,
                    createdAt = ""
                )
                emit(Resource.Success(course))
            } else {
                emit(Resource.Error("Erreur lors de la modification du cours"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    fun deleteCourse(courseId: Int): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            val response = adminApiService.deleteCourse(courseId)
            if (response.success) {
                emit(Resource.Success("Cours supprimé avec succès"))
            } else {
                emit(Resource.Error("Erreur lors de la suppression du cours"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    fun toggleCourseStatus(courseId: Int, newStatus: String): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            val response = adminApiService.toggleCourseStatus(courseId, ToggleCourseStatusDto(newStatus))
            if (response.success) {
                emit(Resource.Success("Statut du cours modifié avec succès"))
            } else {
                emit(Resource.Error("Erreur lors du changement de statut"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    fun getAllTeachers(): Flow<Resource<List<AdminTeacher>>> = flow {
        try {
            emit(Resource.Loading())
            val response = adminApiService.getAdminTeachers()
            if (response.success) {
                val teachers = response.data.map { teacherDto ->
                    AdminTeacher(
                        id = teacherDto.id,
                        firstName = teacherDto.first_name,
                        lastName = teacherDto.last_name,
                        email = teacherDto.email
                    )
                }
                emit(Resource.Success(teachers))
            } else {
                emit(Resource.Error("Erreur lors de la récupération des enseignants"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    // ==================== GESTION DES INSCRIPTIONS ====================
    
    fun getAllEnrollments(): Flow<Resource<List<AdminEnrollment>>> = flow {
        try {
            emit(Resource.Loading())
            val response = adminApiService.getAdminEnrollments()
            if (response.success) {
                val enrollments = response.data.map { enrollmentDto ->
                    AdminEnrollment(
                        id = enrollmentDto.id,
                        courseId = enrollmentDto.course_id,
                        studentId = enrollmentDto.student_id,
                        enrolledAt = enrollmentDto.enrolled_at ?: "",
                        status = enrollmentDto.status ?: "active",
                        courseTitle = enrollmentDto.course_title ?: "Cours sans titre",
                        courseDescription = enrollmentDto.course_description ?: "",
                        studentName = "${enrollmentDto.student_first_name ?: ""} ${enrollmentDto.student_last_name ?: ""}".trim().ifEmpty { "Étudiant inconnu" },
                        studentEmail = enrollmentDto.student_email ?: "",
                        teacherName = "${enrollmentDto.teacher_first_name ?: ""} ${enrollmentDto.teacher_last_name ?: ""}".trim().ifEmpty { "Enseignant non assigné" }
                    )
                }
                emit(Resource.Success(enrollments))
            } else {
                emit(Resource.Error("Erreur lors de la récupération des inscriptions"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    fun createEnrollment(courseId: Int, studentId: Int, status: String = "active"): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            val request = CreateEnrollmentRequestDto(courseId, studentId, status)
            val response = adminApiService.createEnrollment(request)
            if (response.success) {
                emit(Resource.Success("Inscription créée avec succès"))
            } else {
                emit(Resource.Error("Erreur lors de la création de l'inscription"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    fun updateEnrollment(enrollmentId: Int, status: String): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            val request = UpdateEnrollmentRequestDto(status)
            val response = adminApiService.updateEnrollment(enrollmentId, request)
            if (response.success) {
                emit(Resource.Success("Inscription mise à jour avec succès"))
            } else {
                emit(Resource.Error("Erreur lors de la mise à jour de l'inscription"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    fun deleteEnrollment(enrollmentId: Int): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            val response = adminApiService.deleteEnrollment(enrollmentId)
            if (response.success) {
                emit(Resource.Success("Inscription supprimée avec succès"))
            } else {
                emit(Resource.Error("Erreur lors de la suppression de l'inscription"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    // ==================== GESTION DES QUIZ ====================
    
    fun getAllQuizzes(): Flow<Resource<List<AdminQuiz>>> = flow {
        try {
            emit(Resource.Loading())
            val response = adminApiService.getAdminQuizzes()
            if (response.success) {
                val quizzes = response.data.map { quizDto ->
                    AdminQuiz(
                        id = quizDto.id,
                        title = quizDto.title,
                        description = quizDto.description ?: "Aucune description",
                        courseId = quizDto.course_id,
                        courseTitle = quizDto.course_title ?: "Cours inconnu",
                        teacherName = quizDto.teacher_name ?: "Enseignant inconnu",
                        timeLimit = quizDto.time_limit ?: 30,
                        maxAttempts = quizDto.max_attempts ?: 3,
                        passingScore = quizDto.passing_score ?: 60,
                        totalSubmissions = quizDto.total_submissions,
                        uniqueStudents = quizDto.unique_students,
                        averageScore = quizDto.average_score,
                        questionCount = quizDto.question_count,
                        createdAt = quizDto.created_at ?: "Date inconnue",
                        status = quizDto.status ?: "inactive"
                    )
                }
                emit(Resource.Success(quizzes))
            } else {
                emit(Resource.Error("Erreur lors de la récupération des quiz"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    fun createQuiz(
        title: String,
        description: String,
        courseId: Int,
        timeLimit: Int = 30,
        maxAttempts: Int = 3,
        passingScore: Int = 60,
        questions: List<com.edunova.mobile.domain.model.QuizQuestion> = emptyList()
    ): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            
            // Convertir les questions en DTOs
            val questionDtos = questions.map { question ->
                QuizQuestionRequestDto(
                    text = question.questionText,
                    type = when (question.questionType) {
                        QuestionType.MULTIPLE_CHOICE -> "multiple_choice"
                        QuestionType.TRUE_FALSE -> "true_false"
                        QuestionType.SHORT_ANSWER -> "short_answer"
                        QuestionType.ESSAY -> "essay"
                        else -> "multiple_choice"
                    },
                    points = question.points,
                    options = question.options.map { option ->
                        QuizOptionRequestDto(
                            text = option.text,
                            isCorrect = option.isCorrect
                        )
                    },
                    correctAnswer = question.correctAnswer
                )
            }
            
            val request = CreateQuizRequestDto(
                title = title, 
                description = description, 
                courseId = courseId, 
                timeLimit = timeLimit, 
                maxAttempts = maxAttempts, 
                passingScore = passingScore,
                questions = questionDtos
            )
            
            val response = adminApiService.createQuiz(request)
            if (response.success) {
                emit(Resource.Success("Quiz créé avec succès"))
            } else {
                emit(Resource.Error("Erreur lors de la création du quiz"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    fun getQuizDetails(quizId: Int): Flow<Resource<AdminQuizDetails>> = flow {
        try {
            emit(Resource.Loading())
            val response = adminApiService.getQuizDetails(quizId)
            if (response.success) {
                val quizDto = response.data
                val quizDetails = AdminQuizDetails(
                    id = quizDto.id,
                    title = quizDto.title,
                    description = quizDto.description ?: "Aucune description",
                    courseId = quizDto.course_id,
                    courseTitle = quizDto.course_title ?: "Cours inconnu",
                    teacherName = quizDto.teacher_name ?: "Enseignant inconnu",
                    timeLimit = quizDto.time_limit ?: 30,
                    maxAttempts = quizDto.max_attempts ?: 3,
                    passingScore = quizDto.passing_score ?: 60,
                    totalSubmissions = quizDto.total_submissions,
                    uniqueStudents = quizDto.unique_students,
                    averageScore = quizDto.average_score,
                    questionCount = quizDto.question_count,
                    createdAt = quizDto.created_at ?: "Date inconnue",
                    questions = quizDto.questions?.map { questionDto ->
                        QuizQuestion(
                            id = questionDto.id,
                            questionText = questionDto.question_text,
                            questionType = questionDto.question_type,
                            points = questionDto.points,
                            orderIndex = questionDto.order_index,
                            options = questionDto.options?.map { optionDto ->
                                QuizQuestionOption(
                                    id = optionDto.id,
                                    optionText = optionDto.option_text,
                                    isCorrect = optionDto.is_correct,
                                    orderIndex = optionDto.order_index
                                )
                            } ?: emptyList()
                        )
                    } ?: emptyList(),
                    recentSubmissions = quizDto.recent_submissions?.map { submissionDto ->
                        QuizSubmission(
                            firstName = submissionDto.first_name,
                            lastName = submissionDto.last_name,
                            score = submissionDto.score,
                            submittedAt = submissionDto.submitted_at
                        )
                    } ?: emptyList()
                )
                emit(Resource.Success(quizDetails))
            } else {
                emit(Resource.Error("Erreur lors de la récupération des détails du quiz"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    fun updateQuiz(
        quizId: Int,
        title: String,
        description: String,
        timeLimit: Int,
        maxAttempts: Int,
        passingScore: Int
    ): Flow<Resource<AdminQuiz>> = flow {
        try {
            emit(Resource.Loading())
            val request = UpdateQuizRequestDto(title, description, timeLimit, maxAttempts, passingScore)
            val response = adminApiService.updateQuiz(quizId, request)
            if (response.success) {
                val quizDto = response.data
                val updatedQuiz = AdminQuiz(
                    id = quizDto.id,
                    title = quizDto.title,
                    description = quizDto.description ?: "Aucune description",
                    courseId = quizDto.course_id,
                    courseTitle = quizDto.course_title ?: "Cours inconnu",
                    teacherName = quizDto.teacher_name ?: "Enseignant inconnu",
                    timeLimit = quizDto.time_limit ?: 30,
                    maxAttempts = quizDto.max_attempts ?: 3,
                    passingScore = quizDto.passing_score ?: 60,
                    totalSubmissions = quizDto.total_submissions,
                    uniqueStudents = quizDto.unique_students,
                    averageScore = quizDto.average_score,
                    questionCount = quizDto.question_count,
                    createdAt = quizDto.created_at ?: "Date inconnue",
                    status = quizDto.status ?: "inactive"
                )
                emit(Resource.Success(updatedQuiz))
            } else {
                emit(Resource.Error("Erreur lors de la modification du quiz"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    fun deleteQuiz(quizId: Int): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            val response = adminApiService.deleteQuiz(quizId)
            if (response.success) {
                emit(Resource.Success(response.data.message))
            } else {
                emit(Resource.Error("Erreur lors de la suppression du quiz"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    fun toggleQuizStatus(quizId: Int): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            val response = adminApiService.toggleQuizStatus(quizId)
            if (response.success) {
                emit(Resource.Success(response.message ?: "Statut du quiz modifié avec succès"))
            } else {
                emit(Resource.Error("Erreur lors du changement de statut du quiz"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    fun getQuizStatistics(quizId: Int): Flow<Resource<QuizStatistics>> = flow {
        try {
            emit(Resource.Loading())
            val response = adminApiService.getQuizStatistics(quizId)
            if (response.success) {
                val stats = QuizStatistics(
                    general = QuizGeneralStats(
                        totalAttempts = response.data.general.total_attempts,
                        uniqueStudents = response.data.general.unique_students,
                        averageScore = response.data.general.average_score,
                        highestScore = response.data.general.highest_score,
                        lowestScore = response.data.general.lowest_score,
                        passedCount = response.data.general.passed_count
                    ),
                    students = response.data.students.map { student ->
                        QuizStudentStats(
                            firstName = student.first_name,
                            lastName = student.last_name,
                            email = student.email,
                            score = student.score,
                            submittedAt = student.submitted_at,
                            timeTaken = student.time_taken
                        )
                    }
                )
                emit(Resource.Success(stats))
            } else {
                emit(Resource.Error("Erreur lors de la récupération des statistiques"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    // ==================== RAPPORTS ====================
    
    fun getActivityReport(): Flow<Resource<ActivityReport>> = flow {
        try {
            emit(Resource.Loading())
            val response = adminApiService.getActivityReport()
            if (response.success) {
                val report = ActivityReport(
                    dailyActivity = response.data.dailyActivity.map { 
                        DailyActivity(it.date, it.count) 
                    },
                    topCourses = response.data.topCourses.map { 
                        TopCourse(it.title, it.enrollment_count) 
                    }
                )
                emit(Resource.Success(report))
            } else {
                emit(Resource.Error("Erreur lors de la génération du rapport"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    // ==================== SAUVEGARDES ====================
    
    fun createBackup(): Flow<Resource<AdminBackup>> = flow {
        try {
            emit(Resource.Loading())
            val response = adminApiService.createBackup()
            if (response.success) {
                val backup = AdminBackup(
                    id = response.data.id,
                    name = response.data.name,
                    size = response.data.size,
                    createdAt = response.data.created_at,
                    status = response.data.status
                )
                emit(Resource.Success(backup))
            } else {
                emit(Resource.Error("Erreur lors de la création de la sauvegarde"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    fun getAllBackups(): Flow<Resource<List<AdminBackup>>> = flow {
        try {
            emit(Resource.Loading())
            val response = adminApiService.getBackups()
            if (response.success) {
                val backups = response.data.map { backupDto ->
                    AdminBackup(
                        id = backupDto.id,
                        name = backupDto.name,
                        size = backupDto.size,
                        createdAt = backupDto.created_at,
                        status = backupDto.status
                    )
                }
                emit(Resource.Success(backups))
            } else {
                emit(Resource.Error("Erreur lors de la récupération des sauvegardes"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
}

// ==================== DATA CLASSES ====================

data class SystemStats(
    val totalUsers: Int,
    val totalCourses: Int,
    val totalQuizzes: Int,
    val totalEnrollments: Int
)

data class AdminUser(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
    val createdAt: String,
    val updatedAt: String
)

data class AdminCourse(
    val id: Int,
    val title: String,
    val description: String,
    val teacherName: String,
    val teacherId: Int,
    val enrollmentCount: Int,
    val status: String,
    val isPublic: Boolean,
    val enrollmentOpen: Boolean,
    val createdAt: String
)

data class AdminQuiz(
    val id: Int,
    val title: String,
    val description: String,
    val courseId: Int,
    val courseTitle: String,
    val teacherName: String,
    val timeLimit: Int,
    val maxAttempts: Int,
    val passingScore: Int,
    val totalSubmissions: Int,
    val uniqueStudents: Int,
    val averageScore: Int,
    val questionCount: Int,
    val createdAt: String,
    val status: String
)

data class AdminQuizDetails(
    val id: Int,
    val title: String,
    val description: String,
    val courseId: Int,
    val courseTitle: String,
    val teacherName: String,
    val timeLimit: Int,
    val maxAttempts: Int,
    val passingScore: Int,
    val totalSubmissions: Int,
    val uniqueStudents: Int,
    val averageScore: Int,
    val questionCount: Int,
    val createdAt: String,
    val questions: List<QuizQuestion>,
    val recentSubmissions: List<QuizSubmission>
)

data class QuizQuestion(
    val id: Int,
    val questionText: String,
    val questionType: String,
    val points: Int,
    val orderIndex: Int,
    val options: List<QuizQuestionOption>
)

data class QuizQuestionOption(
    val id: Int,
    val optionText: String,
    val isCorrect: Boolean,
    val orderIndex: Int
)

data class QuizSubmission(
    val firstName: String,
    val lastName: String,
    val score: Int,
    val submittedAt: String
)

data class AdminEnrollment(
    val id: Int,
    val courseId: Int,
    val studentId: Int,
    val enrolledAt: String?,
    val status: String?,
    val courseTitle: String?,
    val courseDescription: String?,
    val studentName: String?,
    val studentEmail: String?,
    val teacherName: String?
)

data class QuizStatistics(
    val general: QuizGeneralStats,
    val students: List<QuizStudentStats>
)

data class QuizGeneralStats(
    val totalAttempts: Int,
    val uniqueStudents: Int,
    val averageScore: Double,
    val highestScore: Int,
    val lowestScore: Int,
    val passedCount: Int
)

data class QuizStudentStats(
    val firstName: String,
    val lastName: String,
    val email: String,
    val score: Int,
    val submittedAt: String,
    val timeTaken: Int?
)

data class ActivityReport(
    val dailyActivity: List<DailyActivity>,
    val topCourses: List<TopCourse>
)

data class DailyActivity(
    val date: String,
    val count: Int
)

data class TopCourse(
    val title: String,
    val enrollmentCount: Int
)

data class AdminBackup(
    val id: Int,
    val name: String,
    val size: String,
    val createdAt: String,
    val status: String
)

data class AdminTeacher(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String
)