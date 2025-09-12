package com.example.madassignment.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseService {
    private val db: FirebaseFirestore = Firebase.firestore
    private val postsCollection = db.collection("community_posts")

    suspend fun addPost(post: CommunityPost): String {
        val postData = hashMapOf(
            "author" to post.author,
            "timeAgo" to post.timeAgo,
            "company" to post.company,
            "content" to post.content,
            "likes" to post.likes,
            "likedBy" to post.likedBy,
            "userId" to post.userId,
            "createdAt" to Date()
        )

        val result = postsCollection.add(postData).await()
        return result.id
    }

    suspend fun getAllPosts(): List<CommunityPost> {
        return try {
            val querySnapshot = postsCollection
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            querySnapshot.documents.map { document ->
                CommunityPost(
                    id = document.id, // ← Store the ACTUAL Firebase document ID as string
                    author = document.getString("author") ?: "",
                    timeAgo = document.getString("timeAgo") ?: "",
                    company = document.getString("company") ?: "",
                    content = document.getString("content") ?: "",
                    likes = document.getLong("likes")?.toInt() ?: 0,
                    likedBy = document.getString("likedBy") ?: "",
                    userId = document.getLong("userId")?.toInt() ?: 0
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updatePostLikes(postId: String, likes: Int, likedBy: String) {
        val postRef = postsCollection.document(postId)
        postRef.update(
            mapOf(
                "likes" to likes,
                "likedBy" to likedBy
            )
        ).await()
    }

    suspend fun getPostById(postId: String): CommunityPost? {
        return try {
            val document = postsCollection.document(postId).get().await()
            if (document.exists()) {
                CommunityPost(
                    id = document.id, // ← Use actual document ID
                    author = document.getString("author") ?: "",
                    timeAgo = document.getString("timeAgo") ?: "",
                    company = document.getString("company") ?: "",
                    content = document.getString("content") ?: "",
                    likes = document.getLong("likes")?.toInt() ?: 0,
                    likedBy = document.getString("likedBy") ?: "",
                    userId = document.getLong("userId")?.toInt() ?: 0
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}