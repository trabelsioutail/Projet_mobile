package com.edunova.mobile.data.remote.api

import retrofit2.http.*

interface AiChatApiService {
    
    @POST("ai/chat")
    suspend fun sendMessage(@Body request: AiChatRequestDto): AiChatResponseDto
    
    @GET("ai/suggestions/{role}")
    suspend fun getSuggestions(@Path("role") role: String): AiSuggestionsResponseDto
    
    @POST("ai/session")
    suspend fun createSession(@Body request: CreateAiSessionDto): AiSessionResponseDto
    
    @GET("ai/session/{sessionId}")
    suspend fun getSession(@Path("sessionId") sessionId: String): AiSessionResponseDto
}

data class AiChatRequestDto(
    val message: String,
    val sessionId: String,
    val userRole: String,
    val context: Map<String, Any> = emptyMap()
)

data class AiChatResponseDto(
    val success: Boolean,
    val data: AiChatMessageDto,
    val suggestions: List<String> = emptyList(),
    val quickActions: List<AiQuickActionDto> = emptyList()
)

data class AiChatMessageDto(
    val id: String,
    val content: String,
    val timestamp: Long,
    val messageType: String = "text"
)

data class AiSuggestionsResponseDto(
    val success: Boolean,
    val data: List<AiSuggestionDto>
)

data class AiSuggestionDto(
    val id: String,
    val text: String,
    val action: String,
    val icon: String
)

data class CreateAiSessionDto(
    val userRole: String,
    val userId: Int
)

data class AiSessionResponseDto(
    val success: Boolean,
    val data: AiSessionDto
)

data class AiSessionDto(
    val sessionId: String,
    val userRole: String,
    val messages: List<AiChatMessageDto>,
    val isActive: Boolean
)

data class AiQuickActionDto(
    val id: String,
    val title: String,
    val action: String,
    val icon: String
)