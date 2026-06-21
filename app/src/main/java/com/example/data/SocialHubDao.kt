package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialHubDao {
    // === Creators ===
    @Query("SELECT * FROM creators")
    fun getAllCreators(): Flow<List<Creator>>

    @Query("SELECT * FROM creators WHERE id = :id LIMIT 1")
    fun getCreatorById(id: String): Flow<Creator?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreator(creator: Creator)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreators(creators: List<Creator>)

    @Update
    suspend fun updateCreator(creator: Creator)

    // === Posts ===
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<Post>>

    @Query("SELECT * FROM posts")
    suspend fun getPostsList(): List<Post>

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: Int): Post?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<Post>)

    @Update
    suspend fun updatePost(post: Post)

    // === Subscriptions ===
    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptions(): Flow<List<Subscription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: Subscription)

    @Query("DELETE FROM subscriptions WHERE creatorId = :creatorId")
    suspend fun deleteSubscription(creatorId: String)

    // === Transactions ===
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    // === Chat Messages ===
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    @Update
    suspend fun updateChatMessage(message: ChatMessage)

    // === Events ===
    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<Event>)

    @Update
    suspend fun updateEvent(event: Event)
}
