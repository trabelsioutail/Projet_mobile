package com.edunova.mobile.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Notification(
    val id: Int,
    val userId: Int,
    val title: String,
    val message: String,
    val type: NotificationType = NotificationType.INFO,
    val isRead: Boolean = false,
    val actionUrl: String? = null,
    val createdAt: String
) : Parcelable

enum class NotificationType {
    INFO,
    SUCCESS,
    WARNING,
    ERROR,
    REMINDER;
    
    companion object {
        fun fromString(type: String): NotificationType {
            return when (type.lowercase()) {
                "info" -> INFO
                "success" -> SUCCESS
                "warning" -> WARNING
                "error" -> ERROR
                "reminder" -> REMINDER
                else -> INFO
            }
        }
    }
}