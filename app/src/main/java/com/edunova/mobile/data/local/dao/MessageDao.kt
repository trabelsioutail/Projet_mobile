package com.edunova.mobile.data.local.dao

import androidx.room.*
import com.edunova.mobile.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getConversationMessagesFlow(conversationId: Int): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getConversationMessages(conversationId: Int): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: Int): MessageEntity?
    
    @Query("SELECT * FROM messages WHERE senderId != :userId AND isRead = 0")
    suspend fun getUnreadMessages(userId: Int): List<MessageEntity>
    
    @Query("SELECT COUNT(*) FROM messages WHERE senderId != :userId AND isRead = 0")
    fun getUnreadMessageCountFlow(userId: Int): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId AND senderId != :userId AND isRead = 0")
    suspend fun getUnreadMessageCount(conversationId: Int, userId: Int): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)
    
    @Update
    suspend fun updateMessage(message: MessageEntity)
    
    @Query("UPDATE messages SET isRead = 1 WHERE conversationId = :conversationId AND senderId != :userId")
    suspend fun markConversationAsRead(conversationId: Int, userId: Int)
    
    @Query("UPDATE messages SET isRead = 1 WHERE id = :messageId")
    suspend fun markMessageAsRead(messageId: Int)
    
    @Delete
    suspend fun deleteMessage(message: MessageEntity)
    
    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: Int)
    
    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteConversationMessages(conversationId: Int)
    
    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
    
    @Query("SELECT * FROM messages WHERE isSynced = 0")
    suspend fun getUnsyncedMessages(): List<MessageEntity>
    
    @Query("UPDATE messages SET isSynced = 1 WHERE id = :messageId")
    suspend fun markMessageAsSynced(messageId: Int)
}