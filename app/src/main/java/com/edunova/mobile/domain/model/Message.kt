package com.edunova.mobile.domain.model

data class Message(
    val id: Int,
    val conversationId: Int,
    val senderId: Int,
    val senderName: String,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val isFromCurrentUser: Boolean = false,
    val messageType: MessageType = MessageType.TEXT,
    val attachmentUrl: String? = null
)

enum class MessageType {
    TEXT,
    IMAGE,
    FILE,
    SYSTEM
}