package com.edunova.mobile.data.local

import android.content.Context
import com.edunova.mobile.domain.model.Course
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseLocalStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPrefs = context.getSharedPreferences("course_storage", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // Cours par défaut pour Ahmed
    private val defaultCourses = listOf(
        Course(
            id = 1,
            title = "Mathématiques Avancées",
            description = "Cours de mathématiques niveau universitaire avec algèbre et analyse",
            teacherId = 1,
            status = "active",
            isPublic = true,
            enrollmentOpen = true,
            teacherName = "Prof. Ghofrane Sebteoui",
            isEnrolled = true,
            createdAt = "2024-01-03",
            studentsCount = 25
        ),
        Course(
            id = 2,
            title = "Programmation Web",
            description = "HTML, CSS, JavaScript et frameworks modernes (React, Vue.js)",
            teacherId = 1,
            status = "active",
            isPublic = true,
            enrollmentOpen = true,
            teacherName = "Prof. Ghofrane Sebteoui",
            isEnrolled = true,
            createdAt = "2024-01-02",
            studentsCount = 18
        ),
        Course(
            id = 3,
            title = "Base de Données",
            description = "SQL, MySQL et conception de bases de données relationnelles",
            teacherId = 1,
            status = "active",
            isPublic = true,
            enrollmentOpen = true,
            teacherName = "Prof. Ghofrane Sebteoui",
            isEnrolled = true,
            createdAt = "2024-01-01",
            studentsCount = 22
        )
    )
    
    fun getCourses(): List<Course> {
        return try {
            val coursesJson = sharedPrefs.getString("courses", null)
            if (coursesJson != null) {
                val type = object : TypeToken<List<Course>>() {}.type
                gson.fromJson(coursesJson, type)
            } else {
                // Première fois, sauvegarder les cours par défaut
                saveCourses(defaultCourses)
                defaultCourses
            }
        } catch (e: Exception) {
            defaultCourses
        }
    }
    
    fun saveCourses(courses: List<Course>) {
        try {
            val coursesJson = gson.toJson(courses)
            sharedPrefs.edit()
                .putString("courses", coursesJson)
                .apply()
        } catch (e: Exception) {
            // Ignorer les erreurs de sauvegarde
        }
    }
    
    fun updateCourseEnrollment(courseId: Int, isEnrolled: Boolean) {
        val courses = getCourses().toMutableList()
        val courseIndex = courses.indexOfFirst { it.id == courseId }
        if (courseIndex != -1) {
            courses[courseIndex] = courses[courseIndex].copy(isEnrolled = isEnrolled)
            saveCourses(courses)
        }
    }
    
    fun getCourseById(courseId: Int): Course? {
        return getCourses().find { it.id == courseId }
    }
}