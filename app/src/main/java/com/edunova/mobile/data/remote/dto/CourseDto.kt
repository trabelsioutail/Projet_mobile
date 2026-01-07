package com.edunova.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.edunova.mobile.domain.model.Course
import com.edunova.mobile.domain.model.CourseContent
import com.edunova.mobile.domain.model.ContentType
import com.edunova.mobile.domain.model.EnrollmentStatus

data class CourseDto(
    val id: Int,
    val title: String,
    val description: String?,
    @SerializedName("teacherId")
    val teacherId: Int,
    @SerializedName("first_name")
    val teacherFirstName: String? = null,
    @SerializedName("last_name")
    val teacherLastName: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("contents_count")
    val contentsCount: Int = 0,
    @SerializedName("students_count")
    val studentsCount: Int = 0,
    val contents: List<CourseContentDto>? = emptyList(),
    @SerializedName("is_enrolled")
    val isEnrolled: Boolean = false,
    @SerializedName("enrollment_status")
    val enrollmentStatus: String? = "NOT_ENROLLED",
    val progress: Int = 0,
    val status: String? = "active",
    @SerializedName("is_public")
    val isPublic: Boolean? = true,
    @SerializedName("enrollment_open")
    val enrollmentOpen: Boolean? = true
) {
    fun toDomainModel(): Course {
        val teacherName = if (teacherFirstName != null && teacherLastName != null) {
            "$teacherFirstName $teacherLastName"
        } else null
        
        return Course(
            id = id,
            title = title,
            description = description,
            teacherId = teacherId,
            teacherName = teacherName,
            createdAt = createdAt,
            updatedAt = updatedAt,
            contentsCount = contentsCount,
            studentsCount = studentsCount,
            contents = contents?.map { it.toDomainModel() } ?: emptyList(),
            isEnrolled = isEnrolled,
            enrollmentStatus = EnrollmentStatus.fromString(enrollmentStatus),
            progress = progress.toFloat(),
            status = status ?: "active",
            isPublic = isPublic ?: true,
            enrollmentOpen = enrollmentOpen ?: true
        )
    }
}

data class CourseContentDto(
    val id: Int,
    @SerializedName("course_id")
    val courseId: Int,
    @SerializedName("content_type")
    val contentType: String,
    val title: String,
    @SerializedName("file_path")
    val filePath: String? = null,
    @SerializedName("file_name")
    val fileName: String? = null,
    @SerializedName("mime_type")
    val mimeType: String? = null,
    val url: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
) {
    fun toDomainModel(): CourseContent {
        return CourseContent(
            id = id,
            courseId = courseId,
            contentType = ContentType.fromString(contentType),
            title = title,
            filePath = filePath,
            fileName = fileName,
            mimeType = mimeType,
            url = url,
            createdAt = createdAt
        )
    }
}