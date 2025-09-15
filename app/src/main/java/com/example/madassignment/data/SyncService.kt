// SyncService.kt
package com.example.madassignment.data

import android.content.Context
import android.util.Log
import com.example.madassignment.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncService(private val context: Context) {
    private val repository = AppRepository(context)
    private val firebaseService = FirebaseService()
    private val scope = CoroutineScope(Dispatchers.IO)



    suspend fun syncAllData() {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            Log.d("SyncService", "No network available, skipping sync")
            return
        }

        try {
            syncUsers()
            syncUserProfiles()
            syncJobs()
            syncCommunityPosts()
            Log.d("SyncService", "All data synchronized successfully")
        } catch (e: Exception) {
            Log.e("SyncService", "Sync failed: ${e.message}")
        }
    }

    // Add retry logic for failed syncs
    private suspend fun <T> withRetry(
        maxRetries: Int = 3,
        delay: Long = 1000,
        block: suspend () -> T
    ): T {
        var currentRetry = 0
        var lastException: Exception? = null

        while (currentRetry < maxRetries) {
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                currentRetry++
                if (currentRetry < maxRetries) {
                    kotlinx.coroutines.delay(delay * currentRetry)
                }
            }
        }
        throw lastException ?: Exception("Unknown error in retry")
    }

    private suspend fun syncUsers() {
        val localUsers = repository.getAllUsers()
        val firestoreUsers = withContext(Dispatchers.IO) {
            // You'll need to implement getUser methods in FirebaseService
            emptyList<User>() // Placeholder
        }

        // Sync logic here
    }

    private suspend fun syncUserProfiles() {
        // Similar sync logic for user profiles
    }

    private suspend fun syncJobs() {
        // Sync jobs
    }

    private suspend fun syncCommunityPosts() {
        // Sync community posts
        val localPosts = repository.getAllCommunityPosts()
        val firestorePosts = firebaseService.getAllPosts()

        // Merge logic
        val postsToSync = localPosts.filter { !it.isSynced }
        postsToSync.forEach { post ->
            try {
                val firestoreId = firebaseService.addPost(post)
                if (firestoreId.isNotBlank()) {
                    // Update local post with sync status and Firestore ID
                    repository.updatePost(post.copy(isSynced = true))
                }
            } catch (e: Exception) {
                Log.e("SyncService", "Failed to sync post: ${post.id}")
            }
        }
    }

    fun startPeriodicSync() {
        // Schedule periodic sync (every 5 minutes)
        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(5 * 60 * 1000) // 5 minutes
                syncAllData()
            }
        }
    }
}