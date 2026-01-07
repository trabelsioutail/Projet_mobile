package com.edunova.mobile.domain.model

data class Course(
    val id: Int,
    val title: String,
    val description: String?,
    val teacherId: Int,
    val status: String = "active",
    val isPublic: Boolean = true,
    val enrollmentOpen: Boolean = true,
    val teacherName: String? = null,
    val isEnrolled: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val contentsCount: Int = 0,
    val studentsCount: Int = 0,
    val contents: List<CourseContent> = emptyList(),
    val enrollmentStatus: EnrollmentStatus = EnrollmentStatus.NOT_ENROLLED,
    val progress: Float = 0f
)

enum class EnrollmentStatus {
    NOT_ENROLLED,
    ENROLLED,
    COMPLETED,
    DROPPED;
    
    companion object {
        fun fromString(status: String?): EnrollmentStatus {
            return when (status?.uppercase()) {
                "ENROLLED" -> ENROLLED
                "COMPLETED" -> COMPLETED
                "DROPPED" -> DROPPED
                else -> NOT_ENROLLED
            }
        }
    }
}