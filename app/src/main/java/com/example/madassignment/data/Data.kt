package com.example.madassignment.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date



@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val password: String,
    val name: String,
    val isAdmin: Boolean = false,
    val createdAt: Date = Date(),
    val firestoreId: String = "" // Add Firestore document ID
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val userId: Int,
    val name: String,
    val age: String,
    val aboutMe: String,
    val skills: String,
    val company: String = "",
    val profileImageUri: String? = null,
    val lastUpdated: Date = Date(),
    val isSetupComplete: Boolean = false,
    val firestoreId: String = "" // Add Firestore document ID
)

@Entity(tableName = "jobs")
data class Job(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subtitle: String = "",
    val type: String,
    val location: String,
    val salary: String = "RM 5000 - RM 6000",
    val category: String = "Management (Marketing & Communications)",
    val isSaved: Boolean = false,
    val isApplied: Boolean = false,
    val userId: Int = 0,
    val originalJobId: Int = 0,
    val requiredSkills: String = "",
    val company: String = "",
    val firestoreId: String = "" // Add Firestore document ID
)

@Entity(tableName = "community_posts")
data class CommunityPost(
    @PrimaryKey val id: String, // This should match Firestore ID
    val author: String,
    val timeAgo: String = "",
    val company: String,
    val content: String,
    var likes: Int = 0,
    val likedBy: String = "",
    val userId: Int = 0,
    val createdAt: Date = Date(),
    val isSynced: Boolean = false // Track sync status
)

enum class AuthState {
    IDLE, LOADING, SUCCESS, ERROR
}