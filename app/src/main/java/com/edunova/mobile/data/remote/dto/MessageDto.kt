package com.edunova.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.edunova.mobile.domain.model.Conversation
import com.edunova.mobile.domain.model.Message
import com.edunova.mobile.domain.model.UserRole

data class ConversationDto(
    val id: Int,
    @SerializedName("participant_id")
    val participantId: Int,
    @SerializedName("participant_name")
    val participantName: String,
    @SerializedName("participant_role")
    val participantRole: String = "ENSEIGNANT",
    @SerializedName("course_id")
    val courseId: Int? = null,
    @SerializedName("course_name")
    val courseName: String? = null,
    @SerializedName("last_message")
    val lastMessage: String? = null,
    @SerializedName("last_message_timestamp")
    val lastMessageTimestamp: Long? = null,
    @SerializedName("unread_count")
    val unreadCount: Int = 0,
    @SerializedName("is_active")
    val isActive: Boolean = true,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String? = null
) {
    fun toDomainModel(): Conversation {
        return Conversation(
            id = id,
            participantId = participantId,
            participantName = participantName,
            participantRole = when (participantRole) {
                "ENSEIGNANT" -> UserRole.ENSEIGNANT
                "ETUDIANT" -> UserRole.ETUDIANT
                "ADMIN" -> UserRole.ADMIN
                else -> UserRole.ETUDIANT
            },
            courseId = courseId,
            courseName = courseName,
            lastMessage = lastMessage,
            lastMessageTimestamp = lastMessageTimestamp,
            unreadCount = unreadCount,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

data class MessageDto(
    val id: Int,
    @SerializedName("conversation_id")
    val conversationId: Int,
    @SerializedName("sender_id")
    val senderId: Int,
    @SerializedName("sender_name")
    val senderName: String,
    @SerializedName("receiver_id")
    val receiverId: Int,
    @SerializedName("receiver_name")
    val receiverName: String,
    val content: String,
    val timestamp: Long,
    @SerializedName("is_read")
    val isRead: Boolean = false,
    @SerializedName("message_type")
    val messageType: String = "TEXT",
    @SerializedName("attachment_url")
    val attachmentUrl: String? = null,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("read_at")
    val readAt: String? = null
) {
    fun toDomainModel(currentUserId: Int): Message {
        return Message(
            id = id,
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            content = content,
            timestamp = timestamp,
            isRead = isRead,
            isFromCurrentUser = senderId == currentUserId,
            messageType = when (messageType) {
                "TEXT" -> com.edunova.mobile.domain.model.MessageType.TEXT
                "IMAGE" -> com.edunova.mobile.domain.model.MessageType.IMAGE
                "FILE" -> com.edunova.mobile.domain.model.MessageType.FILE
                "SYSTEM" -> com.edunova.mobile.domain.model.MessageType.SYSTEM
                else -> com.edunova.mobile.domain.model.MessageType.TEXT
            },
            attachmentUrl = attachmentUrl
        )
    }
}

data class SendMessageDto(
    @SerializedName("conversation_id")
    val conversationId: Int,
    @SerializedName("receiver_id")
    val receiverId: Int,
    val message: String
)

data class CreateConversationDto(
    @SerializedName("student_id")
    val studentId: Int,
    @SerializedName("course_id")
    val courseId: Int? = null
)