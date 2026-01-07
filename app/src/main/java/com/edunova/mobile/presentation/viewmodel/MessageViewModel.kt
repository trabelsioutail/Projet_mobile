package com.edunova.mobile.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edunova.mobile.data.repository.MessageRepository
import com.edunova.mobile.domain.model.Conversation
import com.edunova.mobile.domain.model.Message
import com.edunova.mobile.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _conversations = MutableLiveData<Resource<List<Conversation>>>()
    val conversations: LiveData<Resource<List<Conversation>>> = _conversations

    private val _messages = MutableLiveData<Resource<List<Message>>>()
    val messages: LiveData<Resource<List<Message>>> = _messages

    private val _sendMessageResult = MutableLiveData<Resource<Message>>()
    val sendMessageResult: LiveData<Resource<Message>> = _sendMessageResult

    private val _createConversationResult = MutableLiveData<Resource<Conversation>>()
    val createConversationResult: LiveData<Resource<Conversation>> = _createConversationResult

    private val _currentConversationId = MutableLiveData<Int>()
    val currentConversationId: LiveData<Int> = _currentConversationId

    private val _currentUserId = MutableLiveData<Int>()
    val currentUserId: LiveData<Int> = _currentUserId

    // État des conversations pour les enseignants
    private val _conversationsState = MutableLiveData<Resource<List<Conversation>>>()
    val conversationsState: LiveData<Resource<List<Conversation>>> = _conversationsState
    
    // Charger les conversations avec état
    fun loadConversationsWithState() {
        viewModelScope.launch {
            messageRepository.getConversations().collect { resource ->
                _conversationsState.value = resource
            }
        }
    }

    fun loadConversations() {
        viewModelScope.launch {
            messageRepository.getConversations().collect { resource ->
                _conversations.value = resource
            }
        }
    }

    fun loadMessages(conversationId: Int) {
        _currentConversationId.value = conversationId
        val userId = _currentUserId.value ?: return
        
        viewModelScope.launch {
            messageRepository.getMessages(conversationId, userId).collect { resource ->
                _messages.value = resource
            }
        }
    }

    fun sendMessage(conversationId: Int, receiverId: Int, messageText: String) {
        viewModelScope.launch {
            messageRepository.sendMessage(conversationId, receiverId, messageText).collect { resource ->
                _sendMessageResult.value = resource
                
                // Recharger les messages après envoi réussi
                if (resource is Resource.Success) {
                    loadMessages(conversationId)
                }
            }
        }
    }

    fun createConversation(studentId: Int, courseId: Int? = null) {
        viewModelScope.launch {
            messageRepository.createConversation(studentId, courseId).collect { resource ->
                _createConversationResult.value = resource
                
                // Recharger les conversations après création réussie
                if (resource is Resource.Success) {
                    loadConversations()
                }
            }
        }
    }

    fun markAsRead(conversationId: Int) {
        viewModelScope.launch {
            messageRepository.markAsRead(conversationId).collect { resource ->
                // Recharger les conversations pour mettre à jour le statut lu
                if (resource is Resource.Success) {
                    loadConversations()
                }
            }
        }
    }

    fun refreshMessages() {
        val conversationId = _currentConversationId.value ?: return
        loadMessages(conversationId)
    }

    // Messages d'erreur
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // Nettoyer les messages d'erreur
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun refreshConversations() {
        loadConversations()
    }
}