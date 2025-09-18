package com.example.madassignment.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AppDao {
    // Add these functions to your AppDao interface

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Int)

    @Query("DELETE FROM user_profiles WHERE userId = :userId")
    suspend fun deleteUserProfile(userId: Int)

    @Query("DELETE FROM community_posts WHERE userId = :userId")
    suspend fun deleteUserPosts(userId: Int)

    @Query("DELETE FROM jobs WHERE userId = :userId")
    suspend fun deleteUserJobs(userId: Int)

    @Query("UPDATE users SET isAdmin = 1 WHERE id = :userId")
    suspend fun promoteUserToAdmin(userId: Int)

    @Query("SELECT * FROM jobs WHERE userId = 0") // System jobs have userId = 0
    suspend fun getAllSystemJobs(): List<Job>

    @Query("DELETE FROM jobs WHERE id = :jobId")
    suspend fun deleteJob(jobId: Int)

    @Update
    suspend fun updateUser(user: User)
    // FIXED: Simplified saved jobs query - no need for originalJobIds parameter
    @Query("SELECT * FROM jobs WHERE userId = :userId AND isSaved = 1")
    suspend fun getUserSavedJobs(userId: Int): List<Job>

    // FIXED: Simplified applied jobs query - no need for originalJobIds parameter
    @Query("SELECT * FROM jobs WHERE userId = :userId AND isApplied = 1")
    suspend fun getUserAppliedJobs(userId: Int): List<Job>

    @Query("SELECT * FROM jobs WHERE userId = :userId AND originalJobId = :originalJobId")
    suspend fun getJobByOriginalId(userId: Int, originalJobId: Int): Job?

    @Query("SELECT * FROM jobs WHERE userId = :userId AND (id = :jobId OR originalJobId = :jobId)")
    suspend fun getJobByAnyId(userId: Int, jobId: Int): Job?

    @Query("UPDATE jobs SET isSaved = :isSaved WHERE userId = :userId AND originalJobId = :originalJobId")
    suspend fun updateJobSavedStatus(userId: Int, originalJobId: Int, isSaved: Boolean)

    @Query("UPDATE jobs SET isApplied = :isApplied WHERE userId = :userId AND originalJobId = :originalJobId")
    suspend fun updateJobAppliedStatus(userId: Int, originalJobId: Int, isApplied: Boolean)

    @Query("SELECT * FROM employer_jobs WHERE isActive = 1 ORDER BY createdAt DESC")
    suspend fun getAllEmployerJobPosts(): List<EmployerJobPost>

    // Method to get multiple users by IDs
    @Query("SELECT * FROM users WHERE id IN (:userIds)")
    suspend fun getUsersByIds(userIds: List<Int>): List<User>

    // Method to get multiple user profiles by user IDs
    @Query("SELECT * FROM user_profiles WHERE userId IN (:userIds)")
    suspend fun getUserProfilesByIds(userIds: List<Int>): List<UserProfile>

    @Insert
    suspend fun insertEmployeeJobPost(employeeJobPost: EmployeeJobPost): Long

    @Query("SELECT * FROM employee_jobs WHERE employeeId = :employeeId")
    suspend fun getEmployeeJobPosts(employeeId: Int): List<EmployeeJobPost>

    @Query("SELECT * FROM employee_jobs WHERE id = :jobId")
    suspend fun getEmployeeJobPostById(jobId: Int): EmployeeJobPost?

    @Query("DELETE FROM employee_jobs WHERE id = :jobId")
    suspend fun deleteEmployeeJobPost(jobId: Int)

    // Employer job post operations
    @Insert
    suspend fun insertEmployerJobPost(jobPost: EmployerJobPost): Long

    @Update
    suspend fun updateEmployerJobPost(jobPost: EmployerJobPost)

    @Query("SELECT * FROM employer_jobs WHERE employerId = :employerId ORDER BY createdAt DESC")
    suspend fun getEmployerJobPosts(employerId: Int): List<EmployerJobPost>

    @Query("SELECT * FROM employer_jobs WHERE id = :jobId")
    suspend fun getEmployerJobPostById(jobId: Int): EmployerJobPost?

    @Query("DELETE FROM employer_jobs WHERE id = :jobId AND employerId = :employerId")
    suspend fun deleteEmployerJobPost(employerId: Int, jobId: Int)

    // Employer operations
    @Insert
    suspend fun insertEmployer(employer: Employer): Long

    @Query("SELECT * FROM employers WHERE email = :email AND password = :password")
    suspend fun getEmployer(email: String, password: String): Employer?

    @Query("SELECT * FROM employers WHERE email = :email")
    suspend fun getEmployerByEmail(email: String): Employer?

    @Query("SELECT * FROM employers WHERE id = :employerId")
    suspend fun getEmployerById(employerId: Int): Employer?

    // Employer profile operations
    @Insert
    suspend fun insertEmployerProfile(profile: EmployerProfile): Long

    @Update
    suspend fun updateEmployerProfile(profile: EmployerProfile)

    @Query("SELECT * FROM employer_profiles WHERE employerId = :employerId")
    suspend fun getEmployerProfile(employerId: Int): EmployerProfile?

    // User operations
    @Insert
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun getUser(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

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

    @Update
    suspend fun updateJob(job: Job)

    @Query("DELETE FROM jobs WHERE userId = :userId AND id = :jobId AND isSaved = 0 AND isApplied = 0")
    suspend fun deleteUserJob(userId: Int, jobId: Int)

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

    // FIXED: These should update the flags, not set them to 0
    @Query("UPDATE jobs SET isSaved = 0 WHERE userId = :userId AND id = :jobId")
    suspend fun removeSavedJob(userId: Int, jobId: Int)

    @Query("UPDATE jobs SET isApplied = 0 WHERE userId = :userId AND id = :jobId")
    suspend fun removeAppliedJob(userId: Int, jobId: Int)

    // ADDED: Check if job already exists for user to prevent duplicates
    @Query("SELECT * FROM jobs WHERE userId = :userId AND originalJobId = :originalJobId LIMIT 1")
    suspend fun getExistingUserJob(userId: Int, originalJobId: Int): Job?
}