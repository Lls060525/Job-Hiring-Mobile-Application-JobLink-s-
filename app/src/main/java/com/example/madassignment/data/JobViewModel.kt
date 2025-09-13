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
    private val firebaseService = FirebaseService()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _isProfileSetupComplete = MutableStateFlow(false)
    val isProfileSetupComplete: StateFlow<Boolean> = _isProfileSetupComplete

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _authState = MutableStateFlow(AuthState.IDLE)
    val authState: StateFlow<AuthState> = _authState

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    val savedJobs = mutableStateListOf<Job>()
    val appliedJobs = mutableStateListOf<Job>()
    val communityPosts = mutableStateListOf<CommunityPost>()
    val allCommunityPosts = mutableStateListOf<CommunityPost>()

    // FIXED: Use proper initialization
    val sampleRecommendedJobs = getRecommendedJobs()
    val sampleNewJobs = getNewJobs()

    // JobViewModel.kt
    init {
        viewModelScope.launch {
            // Set up real-time listener
            firebaseService.addPostsRealTimeListener { updatedPosts ->
                allCommunityPosts.clear()
                allCommunityPosts.addAll(updatedPosts)
            }

            // Create default admin
            repository.createDefaultAdmin()
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

    suspend fun loginAdmin(email: String, password: String): Result<Unit> {
        return try {
            _authState.value = AuthState.LOADING
            _errorMessage.value = null

            if (!ValidationUtils.isValidEmail(email)) {
                throw Exception("Please enter a valid email address")
            }

            val admin = repository.loginAdmin(email, password)
            if (admin != null) {
                _currentUser.value = admin
                _isAdmin.value = true
                println("DEBUG: Admin logged in: $admin")
                loadUserData(admin.id)
                loadUserProfile(admin.id)
                _authState.value = AuthState.SUCCESS
                Result.success(Unit)
            } else {
                throw Exception("Invalid admin credentials or not an admin")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.ERROR
            _errorMessage.value = e.message ?: "Admin login failed"
            Result.failure(e)
        }
    }

    suspend fun registerAdmin(email: String, password: String, name: String): Result<Unit> {
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

            val userId = repository.registerAdmin(email, password, name)
            if (userId > 0) {
                val newAdmin = repository.getUserById(userId.toInt())
                _currentUser.value = newAdmin
                _isAdmin.value = true
                println("DEBUG: Admin registered: $newAdmin")
                newAdmin?.let {
                    loadUserData(it.id)
                    loadUserProfile(it.id)
                }
                _authState.value = AuthState.SUCCESS
                Result.success(Unit)
            } else {
                throw Exception("Admin registration failed")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.ERROR
            _errorMessage.value = e.message ?: "Admin registration failed"
            Result.failure(e)
        }
    }

    fun checkAdminStatus() {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                val dbUser = repository.getUserById(user.id)
                _isAdmin.value = dbUser?.isAdmin ?: false
            }
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
            // Try Firebase first
            val firebasePosts = firebaseService.getAllPosts()
            if (firebasePosts.isNotEmpty()) {
                allCommunityPosts.clear()
                allCommunityPosts.addAll(firebasePosts)
            } else {
                // Fallback to Room if Firebase has no posts
                val posts = repository.getAllCommunityPosts()
                allCommunityPosts.clear()
                allCommunityPosts.addAll(posts)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to Room if Firebase fails
            val posts = repository.getAllCommunityPosts()
            allCommunityPosts.clear()
            allCommunityPosts.addAll(posts)
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
        _isAdmin.value = false // Reset admin status
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

    suspend fun retrievePassword(email: String, name: String): Result<String> {
        return try {
            _authState.value = AuthState.LOADING
            _errorMessage.value = null

            if (!ValidationUtils.isValidEmail(email)) {
                throw Exception("Please enter a valid email address")
            }

            if (name.isBlank()) {
                throw Exception("Please enter your name")
            }

            val user = repository.getUserByEmail(email)
            if (user == null) {
                throw Exception("No account found with this email address")
            }

            if (user.name != name) {
                throw Exception("Name does not match our records. Please enter the exact name used during registration.")
            }

            _authState.value = AuthState.SUCCESS
            Result.success(user.password) // Return the actual password
        } catch (e: Exception) {
            _authState.value = AuthState.ERROR
            _errorMessage.value = e.message ?: "Password retrieval failed"
            Result.failure(e)
        }
    }

    fun addCommunityPost(post: CommunityPost) {
        viewModelScope.launch {
            try {
                // Use Firebase to add post and get the real ID
                val firebasePostId = firebaseService.addPost(post)
                if (firebasePostId.isNotBlank()) {
                    // Create post with the actual Firebase ID
                    val newPost = post.copy(id = firebasePostId)
                    communityPosts.add(0, newPost)
                    allCommunityPosts.add(0, newPost)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to Room
                val user = currentUser.value
                if (user != null) {
                    val postId = repository.addUserPost(post, user.id)
                    if (postId > 0) {
                        val newPost = post.copy(id = postId.toString(), userId = user.id)
                        communityPosts.add(0, newPost)
                        allCommunityPosts.add(0, newPost)
                    }
                }
            }
        }
    }

    fun togglePostLike(postId: String) {
        viewModelScope.launch {
            try {
                val user = currentUser.value
                if (user != null && postId.isNotBlank()) {
                    // Use the atomic operation from FirebaseService
                    val success = firebaseService.togglePostLike(postId, user.id)

                    if (success) {
                        // Refresh the posts to show updated like counts
                        loadCommunityPosts()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun isPostLiked(post: CommunityPost): Boolean {
        val user = currentUser.value ?: return false
        if (post.likedBy.isBlank()) return false

        return try {
            post.likedBy.split(",").contains(user.id.toString())
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