package com.example.madassignment.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AppDao {
    // User operations
    @Insert
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun getUser(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    // Admin operations
    @Query("SELECT * FROM users WHERE email = :email AND isAdmin = 1")
    suspend fun getAdminByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE isAdmin = 1")
    suspend fun getAllAdmins(): List<User>

    @Update
    suspend fun updateUser(user: User)

    // User profile operations
    @Insert
    suspend fun insertUserProfile(profile: UserProfile): Long

    @Update
    suspend fun updateUserProfile(profile: UserProfile)

    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    suspend fun getUserProfile(userId: Int): UserProfile?

    // Job operations
    @Insert
    suspend fun insertJob(job: Job): Long

    @Query("SELECT * FROM jobs WHERE userId = :userId")
    suspend fun getUserJobs(userId: Int): List<Job>

    @Query("SELECT * FROM jobs WHERE userId = :userId AND isSaved = 1")
    suspend fun getUserSavedJobs(userId: Int): List<Job>

    @Query("SELECT * FROM jobs WHERE userId = :userId AND isApplied = 1")
    suspend fun getUserAppliedJobs(userId: Int): List<Job>

    @Update
    suspend fun updateJob(job: Job)



    @Query("DELETE FROM jobs WHERE userId = :userId AND id = :jobId AND isSaved = 0 AND isApplied = 0")
    suspend fun deleteUserJob(userId: Int, jobId: Int)

    @Query("SELECT * FROM jobs WHERE userId = :userId AND originalJobId = :originalJobId")
    suspend fun getJobByOriginalId(userId: Int, originalJobId: Int): Job?

    // Community Post operations
    @Insert
    suspend fun insertPost(post: CommunityPost): Long

    @Query("SELECT * FROM community_posts WHERE userId = :userId ORDER BY id DESC")
    suspend fun getUserPosts(userId: Int): List<CommunityPost>

    @Query("SELECT * FROM community_posts ORDER BY id DESC")
    suspend fun getAllPosts(): List<CommunityPost>

    @Update
    suspend fun updatePost(post: CommunityPost)

    @Query("SELECT * FROM community_posts WHERE id = :postId")
    suspend fun getPostById(postId: Int): CommunityPost?

    @Query("SELECT * FROM community_posts ORDER BY id DESC")
    suspend fun getAllCommunityPosts(): List<CommunityPost>

    @Query("UPDATE jobs SET isSaved = 0 WHERE userId = :userId AND id = :jobId")
    suspend fun removeSavedJob(userId: Int, jobId: Int)

    @Query("UPDATE jobs SET isApplied = 0 WHERE userId = :userId AND id = :jobId")
    suspend fun removeAppliedJob(userId: Int, jobId: Int)


}