package com.edunova.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AiChatMessageDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("isFromUser")
    val isFromUser: Boolean,
    
    @SerializedName("timestamp")
    val timestamp: Long,
    
    @SerializedName("suggestions")
    val suggestions: List<String>? = null
)

data class AiChatRequestDto(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("sessionId")
    val sessionId: String,
    
    @SerializedName("userRole")
    val userRole: String
)

data class AiChatResponseDto(
    @SerializedName("message")
    val message: AiChatMessageDto,
    
    @SerializedName("suggestions")
    val suggestions: List<AiSuggestionDto>? = null
)

data class AiSuggestionDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("text")
    val text: String,
    
    @SerializedName("category")
    val category: String
)