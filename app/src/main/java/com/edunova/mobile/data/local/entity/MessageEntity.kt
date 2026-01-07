package com.edunova.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.edunova.mobile.domain.model.Message
import com.edunova.mobile.domain.model.MessageType

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: Int,
    val conversationId: Int,
    val senderId: Int,
    val senderName: String,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val isFromCurrentUser: Boolean = false,
    val messageType: String = "TEXT",
    val attachmentUrl: String? = null,
    val lastSyncAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true
) {
    fun toDomainModel(): Message {
        return Message(
            id = id,
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            content = content,
            timestamp = timestamp,
            isRead = isRead,
            isFromCurrentUser = isFromCurrentUser,
            messageType = when (messageType) {
                "TEXT" -> MessageType.TEXT
                "IMAGE" -> MessageType.IMAGE
                "FILE" -> MessageType.FILE
                "SYSTEM" -> MessageType.SYSTEM
                else -> MessageType.TEXT
            },
            attachmentUrl = attachmentUrl
        )
    }
    
    companion object {
        fun fromDomainModel(message: Message, currentUserId: Int): MessageEntity {
            return MessageEntity(
                id = message.id,
                conversationId = message.conversationId,
                senderId = message.senderId,
                senderName = message.senderName,
                content = message.content,
                timestamp = message.timestamp,
                isRead = message.isRead,
                isFromCurrentUser = message.senderId == currentUserId,
                messageType = message.messageType.name,
                attachmentUrl = message.attachmentUrl
            )
        }
    }
}