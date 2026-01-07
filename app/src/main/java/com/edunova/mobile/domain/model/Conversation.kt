package com.edunova.mobile.domain.model

data class Conversation(
    val id: Int,
    val participantId: Int,
    val participantName: String,
    val participantRole: UserRole,
    val courseId: Int? = null,
    val courseName: String? = null,
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long? = null,
    val unreadCount: Int = 0,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String? = null
)