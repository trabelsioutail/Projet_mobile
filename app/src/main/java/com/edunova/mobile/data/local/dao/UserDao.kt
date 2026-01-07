package com.edunova.mobile.data.local.dao

import androidx.room.*
import com.edunova.mobile.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE token IS NOT NULL LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?
    
    @Query("SELECT * FROM users WHERE token IS NOT NULL LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query("UPDATE users SET token = :token WHERE id = :userId")
    suspend fun updateUserToken(userId: Int, token: String?)
    
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Int)
    
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
    
    @Query("UPDATE users SET token = NULL")
    suspend fun clearAllTokens()
}