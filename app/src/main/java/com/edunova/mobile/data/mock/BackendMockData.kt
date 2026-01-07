package com.edunova.mobile.data.mock

import com.edunova.mobile.domain.model.Course
import com.edunova.mobile.domain.model.EnrollmentStatus
import com.edunova.mobile.domain.model.User
import com.edunova.mobile.domain.model.UserRole

object BackendMockData {
    
    val users = listOf(
        User(
            id = 27,
            firstName = "Ahmed",
            lastName = "Ben Ali",
            email = "outailtrabelsi79@gmail.com",
            role = UserRole.ETUDIANT,
            isVerified = true,
            isActive = true,
            createdAt = "2024-01-01",
            lastLogin = "2024-01-07",
            profileImage = null
        ),
        User(
            id = 1,
            firstName = "Admin",
            lastName = "System",
            email = "admin@edunova.com",
            role = UserRole.ADMIN,
            isVerified = true,
            isActive = true,
            createdAt = "2024-01-01",
            lastLogin = "2024-01-07",
            profileImage = null
        )
    )
    
    val courses = listOf(
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
            studentsCount = 25,
            contentsCount = 12,
            enrollmentStatus = EnrollmentStatus.ENROLLED,
            progress = 0.6f
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
            studentsCount = 18,
            contentsCount = 15,
            enrollmentStatus = EnrollmentStatus.ENROLLED,
            progress = 0.4f
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
            studentsCount = 22,
            contentsCount = 10,
            enrollmentStatus = EnrollmentStatus.ENROLLED,
            progress = 0.8f
        )
    )
    
    fun getUserByEmail(email: String): User? {
        return users.find { it.email == email }
    }
    
    fun getUserById(id: Int): User? {
        return users.find { it.id == id }
    }
    
    fun getCoursesForUser(userId: Int): List<Course> {
        return courses
    }
}