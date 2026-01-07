package com.edunova.mobile.data.remote.api

import com.edunova.mobile.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface MessageApiService {
    
    @GET("messaging/conversations")
    suspend fun getConversations(): Response<List<ConversationDto>>
    
    @GET("messaging/conversations/{conversationId}/messages")
    suspend fun getConversationMessages(@Path("conversationId") conversationId: Int): Response<List<MessageDto>>
    
    @POST("messaging/messages")
    suspend fun sendMessage(@Body request: SendMessageDto): Response<MessageDto>
    
    @POST("messaging/conversations")
    suspend fun createConversation(@Body request: CreateConversationDto): Response<ConversationDto>
    
    @PUT("messaging/conversations/{conversationId}/read")
    suspend fun markConversationAsRead(@Path("conversationId") conversationId: Int): Response<ApiResponse<Unit>>
}