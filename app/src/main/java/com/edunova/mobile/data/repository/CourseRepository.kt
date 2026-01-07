package com.edunova.mobile.data.repository

import com.edunova.mobile.data.local.CourseLocalStorage
import com.edunova.mobile.data.local.dao.CourseDao
import com.edunova.mobile.data.local.entity.CourseEntity
import com.edunova.mobile.data.remote.api.CourseApiService
import com.edunova.mobile.data.remote.dto.CreateCourseRequest
import com.edunova.mobile.data.remote.dto.UpdateCourseRequest
import com.edunova.mobile.data.remote.dto.EnrollmentRequest
import com.edunova.mobile.domain.model.Course
import com.edunova.mobile.utils.NetworkUtils
import com.edunova.mobile.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepository @Inject constructor(
    private val courseApiService: CourseApiService,
    private val courseDao: CourseDao,
    private val networkUtils: NetworkUtils,
    private val courseLocalStorage: CourseLocalStorage
) {
    
    // Observer tous les cours (mode hybride)
    fun getAllCoursesFlow(): Flow<List<Course>> {
        return courseDao.getAllCoursesFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    // Observer les cours de l'enseignant
    fun getTeacherCoursesFlow(teacherId: Int): Flow<List<Course>> {
        return courseDao.getTeacherCoursesFlow(teacherId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    // Observer les cours inscrits (pour les étudiants)
    fun getEnrolledCoursesFlow(): Flow<List<Course>> {
        // Utiliser le stockage local pour éviter les erreurs réseau
        return flowOf(courseLocalStorage.getCourses())
    }
    
    // Obtenir tous les cours avec synchronisation
    suspend fun getAllCourses(forceRefresh: Boolean = false): Flow<Resource<List<Course>>> = flow {
        emit(Resource.Loading())
        
        try {
            // Toujours émettre les données locales d'abord
            val localCourses = courseDao.getAllCourses().map { it.toDomainModel() }
            if (localCourses.isNotEmpty() && !forceRefresh) {
                emit(Resource.Success(localCourses))
            }
            
            // Tenter la synchronisation si réseau disponible
            if (networkUtils.isNetworkAvailable()) {
                val response = courseApiService.getCourses()
                
                if (response.isSuccessful) {
                    val remoteCourses = response.body()?.map { it.toDomainModel() } ?: emptyList()
                    
                    // Sauvegarder en local
                    val courseEntities = remoteCourses.map { CourseEntity.fromDomainModel(it) }
                    courseDao.insertCourses(courseEntities)
                    
                    emit(Resource.Success(remoteCourses))
                } else {
                    // Erreur API, utiliser les données locales si disponibles
                    if (localCourses.isNotEmpty()) {
                        emit(Resource.Success(localCourses))
                    } else {
                        emit(Resource.Error("Erreur réseau: ${response.code()}"))
                    }
                }
            } else {
                // Pas de réseau, utiliser les données locales
                if (localCourses.isNotEmpty()) {
                    emit(Resource.Success(localCourses))
                } else {
                    emit(Resource.Error("Aucune connexion Internet et aucune donnée locale"))
                }
            }
        } catch (e: Exception) {
            // En cas d'erreur, essayer de récupérer les données locales
            val localCourses = courseDao.getAllCourses().map { it.toDomainModel() }
            if (localCourses.isNotEmpty()) {
                emit(Resource.Success(localCourses))
            } else {
                emit(Resource.Error(e.message ?: "Erreur inconnue"))
            }
        }
    }
    
    // Obtenir les cours de l'enseignant avec synchronisation
    suspend fun getTeacherCourses(teacherId: Int, forceRefresh: Boolean = false): Flow<Resource<List<Course>>> = flow {
        emit(Resource.Loading())
        
        try {
            // Données locales d'abord
            val localCourses = courseDao.getTeacherCourses(teacherId).map { it.toDomainModel() }
            if (localCourses.isNotEmpty() && !forceRefresh) {
                emit(Resource.Success(localCourses))
            }
            
            // Synchronisation si réseau disponible
            if (networkUtils.isNetworkAvailable()) {
                val response = courseApiService.getCourses()
                
                if (response.isSuccessful) {
                    val remoteCourses = response.body()
                        ?.filter { it.teacherId == teacherId }
                        ?.map { it.toDomainModel() } ?: emptyList()
                    
                    // Sauvegarder en local
                    val courseEntities = remoteCourses.map { CourseEntity.fromDomainModel(it) }
                    courseDao.insertCourses(courseEntities)
                    
                    emit(Resource.Success(remoteCourses))
                } else {
                    if (localCourses.isNotEmpty()) {
                        emit(Resource.Success(localCourses))
                    } else {
                        emit(Resource.Error("Erreur réseau: ${response.code()}"))
                    }
                }
            } else {
                if (localCourses.isNotEmpty()) {
                    emit(Resource.Success(localCourses))
                } else {
                    emit(Resource.Error("Aucune connexion Internet"))
                }
            }
        } catch (e: Exception) {
            val localCourses = courseDao.getTeacherCourses(teacherId).map { it.toDomainModel() }
            if (localCourses.isNotEmpty()) {
                emit(Resource.Success(localCourses))
            } else {
                emit(Resource.Error(e.message ?: "Erreur inconnue"))
            }
        }
    }
    
    // Obtenir un cours par ID depuis le stockage local
    suspend fun getCourseById(courseId: Int): Flow<Resource<Course>> = flow {
        emit(Resource.Loading())
        
        try {
            val course = courseLocalStorage.getCourseById(courseId)
            if (course != null) {
                emit(Resource.Success(course))
            } else {
                emit(Resource.Error("Cours non trouvé"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    // Créer un cours (nécessite une connexion)
    suspend fun createCourse(title: String, description: String?): Flow<Resource<Course>> = flow {
        emit(Resource.Loading())
        
        try {
            if (!networkUtils.isNetworkAvailable()) {
                emit(Resource.Error("Connexion Internet requise pour créer un cours"))
                return@flow
            }
            
            val response = courseApiService.createCourse(
                CreateCourseRequest(title, description)
            )
            
            if (response.isSuccessful) {
                val newCourse = response.body()?.toDomainModel()
                if (newCourse != null) {
                    // Sauvegarder en local
                    courseDao.insertCourse(CourseEntity.fromDomainModel(newCourse))
                    emit(Resource.Success(newCourse))
                } else {
                    emit(Resource.Error("Erreur lors de la création du cours"))
                }
            } else {
                emit(Resource.Error("Erreur réseau: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    // Mettre à jour un cours (nécessite une connexion)
    suspend fun updateCourse(courseId: Int, title: String, description: String?): Flow<Resource<Course>> = flow {
        emit(Resource.Loading())
        
        try {
            if (!networkUtils.isNetworkAvailable()) {
                emit(Resource.Error("Connexion Internet requise pour modifier un cours"))
                return@flow
            }
            
            val response = courseApiService.updateCourse(
                courseId,
                UpdateCourseRequest(title, description)
            )
            
            if (response.isSuccessful) {
                val updatedCourse = response.body()?.toDomainModel()
                if (updatedCourse != null) {
                    // Mettre à jour en local
                    courseDao.updateCourse(CourseEntity.fromDomainModel(updatedCourse))
                    emit(Resource.Success(updatedCourse))
                } else {
                    emit(Resource.Error("Erreur lors de la modification du cours"))
                }
            } else {
                emit(Resource.Error("Erreur réseau: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    // Supprimer un cours (nécessite une connexion)
    suspend fun deleteCourse(courseId: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        
        try {
            if (!networkUtils.isNetworkAvailable()) {
                emit(Resource.Error("Connexion Internet requise pour supprimer un cours"))
                return@flow
            }
            
            val response = courseApiService.deleteCourse(courseId)
            
            if (response.isSuccessful) {
                // Supprimer en local
                courseDao.deleteCourseById(courseId)
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Erreur réseau: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur inconnue"))
        }
    }
    
    // S'inscrire à un cours (pour les étudiants)
    suspend fun enrollInCourse(courseId: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        
        try {
            // Mettre à jour localement
            courseLocalStorage.updateCourseEnrollment(courseId, true)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur lors de l'inscription"))
        }
    }
    
    // Se désinscrire d'un cours (pour les étudiants)
    suspend fun unenrollFromCourse(courseId: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        
        try {
            // Mettre à jour localement
            courseLocalStorage.updateCourseEnrollment(courseId, false)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Erreur lors de la désinscription"))
        }
    }
    
    // Marquer un cours comme disponible hors ligne
    suspend fun markCourseOfflineAvailable(courseId: Int, isAvailable: Boolean) {
        courseDao.updateOfflineAvailability(courseId, isAvailable)
    }
    
    // Obtenir les cours disponibles hors ligne
    suspend fun getOfflineAvailableCourses(): List<Course> {
        return courseDao.getOfflineAvailableCourses().map { it.toDomainModel() }
    }
}