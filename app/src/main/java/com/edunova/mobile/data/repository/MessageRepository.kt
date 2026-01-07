package com.edunova.mobile.data.repository

import com.edunova.mobile.data.remote.api.MessageApiService
import com.edunova.mobile.data.remote.dto.CreateConversationDto
import com.edunova.mobile.data.remote.dto.SendMessageDto
import com.edunova.mobile.domain.model.Conversation
import com.edunova.mobile.domain.model.Message
import com.edunova.mobile.domain.model.UserRole
import com.edunova.mobile.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val messageApiService: MessageApiService
) {

    fun getConversations(): Flow<Resource<List<Conversation>>> = flow {
        emit(Resource.Loading())
        
        // Retourner des conversations mockées pour Ahmed
        val mockConversations = listOf(
            Conversation(
                id = 1,
                participantId = 1,
                participantName = "Prof. Ghofrane Sebteoui",
                participantRole = UserRole.ENSEIGNANT,
                lastMessage = "Bonjour Ahmed, comment allez-vous dans vos études ?",
                lastMessageTimestamp = System.currentTimeMillis() - 3600000, // Il y a 1 heure
                unreadCount = 1,
                createdAt = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(System.currentTimeMillis() - 86400000))
            ),
            Conversation(
                id = 2,
                participantId = 2,
                participantName = "Prof. Martin Dubois",
                participantRole = UserRole.ENSEIGNANT,
                lastMessage = "N'hésitez pas si vous avez des questions sur le cours de JavaScript",
                lastMessageTimestamp = System.currentTimeMillis() - 7200000, // Il y a 2 heures
                unreadCount = 0,
                createdAt = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(System.currentTimeMillis() - 172800000))
            )
        )
        
        emit(Resource.Success(mockConversations))
    }

    fun getMessages(conversationId: Int, currentUserId: Int): Flow<Resource<List<Message>>> = flow {
        emit(Resource.Loading())
        
        // Retourner des messages mockés selon la conversation
        val mockMessages = when (conversationId) {
            1 -> listOf(
                Message(
                    id = 1,
                    conversationId = 1,
                    senderId = 1,
                    senderName = "Prof. Ghofrane Sebteoui",
                    content = "Bonjour Ahmed, comment allez-vous dans vos études ?",
                    timestamp = System.currentTimeMillis() - 3600000,
                    isFromCurrentUser = false,
                    isRead = true
                ),
                Message(
                    id = 2,
                    conversationId = 1,
                    senderId = currentUserId,
                    senderName = "Ahmed Ben Ali",
                    content = "Bonjour Professeur Ghofrane, ça va bien merci ! J'ai quelques questions sur le cours de mathématiques.",
                    timestamp = System.currentTimeMillis() - 3300000,
                    isFromCurrentUser = true,
                    isRead = true
                ),
                Message(
                    id = 3,
                    conversationId = 1,
                    senderId = 1,
                    senderName = "Prof. Ghofrane Sebteoui",
                    content = "Parfait ! N'hésitez pas à me poser vos questions. Je suis là pour vous aider.",
                    timestamp = System.currentTimeMillis() - 3000000,
                    isFromCurrentUser = false,
                    isRead = false
                )
            )
            2 -> listOf(
                Message(
                    id = 4,
                    conversationId = 2,
                    senderId = 2,
                    senderName = "Prof. Martin Dubois",
                    content = "Bonjour Ahmed ! Comment trouvez-vous le cours de JavaScript ?",
                    timestamp = System.currentTimeMillis() - 7200000,
                    isFromCurrentUser = false,
                    isRead = true
                ),
                Message(
                    id = 5,
                    conversationId = 2,
                    senderId = currentUserId,
                    senderName = "Ahmed Ben Ali",
                    content = "Bonjour ! Le cours est très intéressant, j'apprends beaucoup de choses.",
                    timestamp = System.currentTimeMillis() - 6900000,
                    isFromCurrentUser = true,
                    isRead = true
                )
            )
            else -> emptyList()
        }
        
        emit(Resource.Success(mockMessages))
    }

    fun sendMessage(conversationId: Int, receiverId: Int, message: String): Flow<Resource<Message>> = flow {
        emit(Resource.Loading())
        
        // Simuler l'envoi d'un message
        val newMessage = Message(
            id = System.currentTimeMillis().toInt(),
            conversationId = conversationId,
            senderId = 27, // Ahmed's ID
            senderName = "Ahmed Ben Ali",
            content = message,
            timestamp = System.currentTimeMillis(),
            isFromCurrentUser = true,
            isRead = false
        )
        
        // Simuler un délai d'envoi
        kotlinx.coroutines.delay(500)
        emit(Resource.Success(newMessage))
    }

    fun createConversation(studentId: Int, courseId: Int? = null): Flow<Resource<Conversation>> = flow {
        emit(Resource.Loading())
        
        try {
            val createConversationDto = CreateConversationDto(studentId, courseId)
            val response = messageApiService.createConversation(createConversationDto)
            if (response.isSuccessful) {
                val conversation = response.body()?.toDomainModel()
                if (conversation != null) {
                    emit(Resource.Success(conversation))
                } else {
                    emit(Resource.Error("Erreur lors de la création de la conversation"))
                }
            } else {
                emit(Resource.Error("Erreur lors de la création de la conversation"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Erreur de connexion: ${e.message}"))
        }
    }

    fun markAsRead(conversationId: Int): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = messageApiService.markConversationAsRead(conversationId)
            if (response.isSuccessful) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Erreur lors du marquage comme lu"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Erreur de connexion: ${e.message}"))
        }
    }
}