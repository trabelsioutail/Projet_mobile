package com.edunova.mobile.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CourseContent(
    val id: Int,
    val courseId: Int,
    val contentType: ContentType,
    val title: String,
    val filePath: String? = null,
    val fileName: String? = null,
    val mimeType: String? = null,
    val url: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) : Parcelable

enum class ContentType(val displayName: String) {
    PDF("Document PDF"),
    VIDEO("Vidéo"),
    DOCUMENT("Document"),
    LINK("Lien");
    
    companion object {
        fun fromString(value: String): ContentType {
            return when (value.uppercase()) {
                "PDF" -> PDF
                "VIDEO" -> VIDEO
                "DOCUMENT" -> DOCUMENT
                "LINK" -> LINK
                else -> DOCUMENT
            }
        }
    }
}