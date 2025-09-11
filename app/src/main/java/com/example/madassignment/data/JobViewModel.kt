package com.example.madassignment.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.madassignment.utils.ValidationUtils

class JobViewModel(private val context: Context) : ViewModel() {
    private val repository = AppRepository(context)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _isProfileSetupComplete = MutableStateFlow(false)
    val isProfileSetupComplete: StateFlow<Boolean> = _isProfileSetupComplete

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _authState = MutableStateFlow(AuthState.IDLE)
    val authState: StateFlow<AuthState> = _authState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    val savedJobs = mutableStateListOf<Job>()
    val appliedJobs = mutableStateListOf<Job>()
    val communityPosts = mutableStateListOf<CommunityPost>()
    val allCommunityPosts = mutableStateListOf<CommunityPost>()

    // FIXED: Use proper initialization
    val sampleRecommendedJobs = getRecommendedJobs()
    val sampleNewJobs = getNewJobs()

    init {
        viewModelScope.launch {
            try {
                // Load public community posts safely
                loadCommunityPosts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun loadUserData(userId: Int) {
        try {
            repository.initializeUserSampleData(userId) // This creates profile if it doesn't exist
            loadAllUserData(userId)
            loadUserProfile(userId) // Load the profile after initialization
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadAllUserData(userId: Int) {
        try {
            savedJobs.clear()
            appliedJobs.clear()
            communityPosts.clear()

            val saved = repository.getUserSavedJobs(userId)
            val applied = repository.getUserAppliedJobs(userId)
            val posts = repository.getUserPosts(userId)

            savedJobs.addAll(saved)
            appliedJobs.addAll(applied)
            communityPosts.addAll(posts)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadUserProfile(userId: Int) {
        try {
            val profile = repository.getUserProfile(userId)
            println("DEBUG: Loaded user profile for $userId: $profile")
            _userProfile.value = profile
        } catch (e: Exception) {
            println("DEBUG: Failed to load user profile: ${e.message}")
            _userProfile.value = null
            e.printStackTrace()
        }
    }

    private suspend fun loadCommunityPosts() {
        try {
            allCommunityPosts.clear()
            val posts = repository.getAllCommunityPosts()
            allCommunityPosts.addAll(posts)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun register(email: String, password: String, name: String): Result<Unit> {
        return try {
            _authState.value = AuthState.LOADING
            _errorMessage.value = null

            if (!ValidationUtils.isValidEmail(email)) {
                throw Exception("Please enter a valid email address")
            }

            val existingUser = repository.getUserByEmail(email)
            if (existingUser != null) {
                throw Exception("User with this email already exists")
            }

            val userId = repository.registerUser(email, password, name)
            if (userId > 0) {
                val newUser = repository.getUserById(userId.toInt())
                _currentUser.value = newUser
                println("DEBUG: User registered: $newUser")
                newUser?.let {
                    loadUserData(it.id)
                    loadUserProfile(it.id) // This should return null for new users
                }
                _authState.value = AuthState.SUCCESS
                Result.success(Unit)
            } else {
                throw Exception("Registration failed")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.ERROR
            _errorMessage.value = e.message ?: "Registration failed"
            Result.failure(e)
        }
    }

    // Update your login function:
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            _authState.value = AuthState.LOADING
            _errorMessage.value = null

            if (!ValidationUtils.isValidEmail(email)) {
                throw Exception("Please enter a valid email address")
            }

            val user = repository.loginUser(email, password)
            if (user != null) {
                _currentUser.value = user
                println("DEBUG: User logged in: $user")
                loadUserData(user.id)
                loadUserProfile(user.id) // Make sure this is called
                _authState.value = AuthState.SUCCESS
                Result.success(Unit)
            } else {
                throw Exception("Invalid email or password")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.ERROR
            _errorMessage.value = e.message ?: "Login failed"
            Result.failure(e)
        }
    }
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            // Mark profile as complete when updating from setup screen
            val profileToSave = profile.copy(isSetupComplete = true)
            val result = repository.saveUserProfile(profileToSave)
            if (result > 0) {
                _userProfile.value = profileToSave
                _isProfileSetupComplete.value = true
                Result.success(Unit)
            } else {
                throw Exception("Failed to update profile")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        _currentUser.value = null
        _userProfile.value = null
        _authState.value = AuthState.IDLE
        _errorMessage.value = null
        savedJobs.clear()
        appliedJobs.clear()
        communityPosts.clear()
    }

    fun saveJob(job: Job) {
        viewModelScope.launch {
            try {
                currentUser.value?.let { user ->
                    val result = repository.saveUserJob(job, user.id)
                    if (result > 0) {
                        loadAllUserData(user.id)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun applyToJob(job: Job) {
        viewModelScope.launch {
            try {
                currentUser.value?.let { user ->
                    val result = repository.applyToUserJob(job, user.id)
                    if (result > 0) {
                        loadAllUserData(user.id)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeSavedJob(job: Job) {
        viewModelScope.launch {
            try {
                currentUser.value?.let { user ->
                    // Only remove the saved flag, keep the job if it's applied
                    repository.removeSavedJob(user.id, job.id)
                    loadAllUserData(user.id)  // Refresh the data
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeAppliedJob(job: Job) {
        viewModelScope.launch {
            try {
                currentUser.value?.let { user ->
                    // Only remove the applied flag, keep the job if it's saved
                    repository.removeAppliedJob(user.id, job.id)
                    loadAllUserData(user.id)  // Refresh the data
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addCommunityPost(post: CommunityPost) {
        viewModelScope.launch {
            try {
                currentUser.value?.let { user ->
                    val postId = repository.addUserPost(post, user.id)
                    if (postId > 0) {
                        val newPost = post.copy(id = postId.toInt(), userId = user.id)
                        communityPosts.add(0, newPost)
                        allCommunityPosts.add(0, newPost)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ADD THESE MISSING FUNCTIONS:
    fun togglePostLike(postId: Int) {
        viewModelScope.launch {
            try {
                currentUser.value?.let { user ->
                    val post = repository.getPostById(postId)
                    post?.let {
                        val likedByList = it.likedBy.split(",").toMutableList()
                        if (likedByList.contains(user.id.toString())) {
                            likedByList.remove(user.id.toString())
                            val updatedPost = it.copy(
                                likes = it.likes - 1,
                                likedBy = likedByList.joinToString(",")
                            )
                            repository.updatePost(updatedPost)
                        } else {
                            likedByList.add(user.id.toString())
                            val updatedPost = it.copy(
                                likes = it.likes + 1,
                                likedBy = likedByList.joinToString(",")
                            )
                            repository.updatePost(updatedPost)
                        }
                        loadCommunityPosts()
                        loadAllUserData(user.id)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isPostLiked(post: CommunityPost): Boolean {
        return try {
            currentUser.value?.let { user ->
                post.likedBy.split(",").contains(user.id.toString())
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // FIXED: Get all available jobs safely
    fun getAllAvailableJobs(): List<Job> {
        return try {
            sampleRecommendedJobs + sampleNewJobs
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // In JobViewModel.kt, change this:
    suspend fun getSkillBasedJobs(userSkills: String): List<Job> {
        return repository.getRecommendedJobsBasedOnSkills(userSkills)
    }
}