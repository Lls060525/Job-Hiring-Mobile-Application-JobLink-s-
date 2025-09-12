package com.example.madassignment.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    suspend fun togglePostLike(postId: String, userId: Int): Boolean {
        return try {
            val postRef = postsCollection.document(postId)

            // Use a transaction to ensure atomic operations
            db.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentLikes = snapshot.getLong("likes")?.toInt() ?: 0
                var likedBy = snapshot.getString("likedBy") ?: ""

                val likedByList = if (likedBy.isBlank()) {
                    mutableListOf()
                } else {
                    likedBy.split(",").toMutableList()
                }

                val userIdStr = userId.toString()
                val wasLiked = likedByList.contains(userIdStr)

                if (wasLiked) {
                    // Unlike: remove user ID and decrement count
                    likedByList.remove(userIdStr)
                    transaction.update(postRef,
                        "likes", currentLikes - 1,
                        "likedBy", likedByList.joinToString(",")
                    )
                    false
                } else {
                    // Like: add user ID and increment count
                    likedByList.add(userIdStr)
                    transaction.update(postRef,
                        "likes", currentLikes + 1,
                        "likedBy", likedByList.joinToString(",")
                    )
                    true
                }
            }.await()

            true // Success
        } catch (e: Exception) {
            e.printStackTrace()
            false // Failure
        }
    }

    suspend fun getPostLikes(postId: String): Pair<Int, List<String>> {
        return try {
            val document = postsCollection.document(postId).get().await()
            val likes = document.getLong("likes")?.toInt() ?: 0
            val likedBy = document.getString("likedBy") ?: ""
            val likedByList = if (likedBy.isBlank()) emptyList() else likedBy.split(",")
            Pair(likes, likedByList)
        } catch (e: Exception) {
            Pair(0, emptyList())
        }
    }


    fun addPostsRealTimeListener(onPostsUpdated: (List<CommunityPost>) -> Unit) {
        postsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                error?.let {
                    it.printStackTrace()
                    return@addSnapshotListener
                }

                snapshot?.let { querySnapshot ->
                    val posts = querySnapshot.documents.map { document ->
                        CommunityPost(
                            id = document.id,
                            author = document.getString("author") ?: "",
                            timeAgo = document.getString("timeAgo") ?: "",
                            company = document.getString("company") ?: "",
                            content = document.getString("content") ?: "",
                            likes = document.getLong("likes")?.toInt() ?: 0,
                            likedBy = document.getString("likedBy") ?: "",
                            userId = document.getLong("userId")?.toInt() ?: 0
                        )
                    }
                    onPostsUpdated(posts)
                }
            }
    }
}