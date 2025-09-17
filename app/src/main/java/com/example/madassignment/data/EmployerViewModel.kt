package com.example.madassignment.data

import android.content.Context
import android.util.Log // Add this import
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.madassignment.utils.ValidationUtils
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class EmployerViewModel(context: Context) : ViewModel() { // Remove private from context parameter

    private val _applicantDetails = MutableStateFlow<List<ApplicantDetail>>(emptyList())
    val applicantDetails: StateFlow<List<ApplicantDetail>> = _applicantDetails

    data class ApplicantDetail(
        val user: User,
        val profile: UserProfile?
    )

    private val repository = AppRepository(context)
    private val firebaseService = FirebaseService(context)

    private val _currentEmployer = MutableStateFlow<Employer?>(null)
    val currentEmployer: StateFlow<Employer?> = _currentEmployer

    private val _employerProfile = MutableStateFlow<EmployerProfile?>(null)
    val employerProfile: StateFlow<EmployerProfile?> = _employerProfile

    private val _authState = MutableStateFlow(AuthState.IDLE)
    val authState: StateFlow<AuthState> = _authState

    // Remove errorMessage as it's not used
    // private val _errorMessage = MutableStateFlow<String?>(null)
    // val errorMessage: StateFlow<String?> = _errorMessage

    private val _employerJobPosts = MutableStateFlow<List<EmployerJobPost>>(emptyList())
    val employerJobPosts: StateFlow<List<EmployerJobPost>> = _employerJobPosts

    // Add community posts functionality
    private val _allCommunityPosts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val allCommunityPosts: StateFlow<List<CommunityPost>> = _allCommunityPosts.asStateFlow()

    private val _likedPosts = MutableStateFlow<Set<String>>(emptySet())
    val likedPosts: StateFlow<Set<String>> = _likedPosts.asStateFlow()

    fun loadApplicantDetails(jobPost: EmployerJobPost) {
        viewModelScope.launch {
            try {
                if (jobPost.applicants.isBlank()) {
                    _applicantDetails.value = emptyList()
                    return@launch
                }

                val applicantIds = jobPost.applicants.split(",").mapNotNull {
                    it.trim().toIntOrNull()
                }

                val users = repository.getUsersByIds(applicantIds)
                val profiles = repository.getUserProfilesByIds(applicantIds)

                val applicantDetails = users.map { user ->
                    val profile = profiles.find { it.userId == user.id }
                    ApplicantDetail(user, profile)
                }

                _applicantDetails.value = applicantDetails
                Log.d("EmployerViewModel", "Loaded ${applicantDetails.size} applicant details")

            } catch (e: Exception) {
                Log.e("EmployerViewModel", "Error loading applicant details: ${e.message}")
                _applicantDetails.value = emptyList()
            }
        }
    }

    // Method to clear applicant details
    fun clearApplicantDetails() {
        _applicantDetails.value = emptyList()
    }

    fun createJobPost(jobPost: EmployerJobPost) {
        viewModelScope.launch {
            try {
                val result = repository.createEmployerJobPost(jobPost)
                if (result > 0) {
                    loadEmployerJobPosts()
                }
            } catch (e: Exception) {
                Log.e("EmployerViewModel", "Error creating job post: ${e.message}")
            }
        }
    }

    fun loadEmployerJobPosts() {
        viewModelScope.launch {
            try {
                currentEmployer.value?.let { employer ->
                    val jobPosts = repository.getEmployerJobPosts(employer.id)
                    _employerJobPosts.value = jobPosts
                }
            } catch (e: Exception) {
                Log.e("EmployerViewModel", "Error loading job posts: ${e.message}")
            }
        }
    }

    // Remove updateJobPost function as it's not used
    // fun updateJobPost(jobPost: EmployerJobPost) { ... }

    fun deleteJobPost(jobId: Int) {
        viewModelScope.launch {
            try {
                currentEmployer.value?.let { employer ->
                    val success = repository.deleteEmployerJobPost(employer.id, jobId)
                    if (success) {
                        loadEmployerJobPosts()
                    }
                }
            } catch (e: Exception) {
                Log.e("EmployerViewModel", "Error deleting job post: ${e.message}")
            }
        }
    }

    suspend fun register(email: String, password: String, companyName: String): Result<Unit> {
        return try {
            _authState.value = AuthState.LOADING
            // _errorMessage.value = null // Remove this line

            if (!ValidationUtils.isValidEmail(email)) {
                throw Exception("Please enter a valid email address")
            }

            val existingEmployer = repository.getEmployerByEmail(email)
            if (existingEmployer != null) {
                throw Exception("Employer with this email already exists")
            }

            val employerId = repository.registerEmployer(email, password, companyName)
            if (employerId > 0) {
                val newEmployer = repository.getEmployerById(employerId.toInt())
                _currentEmployer.value = newEmployer
                loadCommunityPosts()
                _authState.value = AuthState.SUCCESS
                Result.success(Unit)
            } else {
                throw Exception("Registration failed")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.ERROR
            // _errorMessage.value = e.message ?: "Registration failed" // Remove this line
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            _authState.value = AuthState.LOADING
            // _errorMessage.value = null // Remove this line

            if (!ValidationUtils.isValidEmail(email)) {
                throw Exception("Please enter a valid email address")
            }

            val employer = repository.loginEmployer(email, password)
            if (employer != null) {
                _currentEmployer.value = employer
                loadEmployerProfile(employer.id)
                loadCommunityPosts()
                _authState.value = AuthState.SUCCESS
                Result.success(Unit)
            } else {
                throw Exception("Invalid email or password")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.ERROR
            // _errorMessage.value = e.message ?: "Login failed" // Remove this line
            Result.failure(e)
        }
    }

    private suspend fun loadEmployerProfile(employerId: Int) {
        try {
            val profile = repository.getEmployerProfile(employerId)
            _employerProfile.value = profile
        } catch (e: Exception) {
            Log.e("EmployerViewModel", "Error loading employer profile: ${e.message}")
        }
    }

    suspend fun updateEmployerProfile(profile: EmployerProfile): Result<Unit> {
        return try {
            val profileToSave = profile.copy(isSetupComplete = true)
            val result = repository.saveEmployerProfile(profileToSave)
            if (result > 0) {
                _employerProfile.value = profileToSave
                Result.success(Unit)
            } else {
                throw Exception("Failed to update profile")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        _currentEmployer.value = null
        _employerProfile.value = null
        _authState.value = AuthState.IDLE
        // _errorMessage.value = null // Remove this line
    }

    fun addCommunityPost(post: CommunityPost) {
        viewModelScope.launch {
            try {
                val postId = firebaseService.addPost(post)
                // Update local state with the new post including its Firebase ID
                val newPost = post.copy(id = postId)
                _allCommunityPosts.value += newPost // Use += instead of = ... + ...
            } catch (e: Exception) {
                Log.e("EmployerViewModel", "Error adding community post: ${e.message}")
                // Fallback to local storage if Firebase fails
                val postWithId = if (post.id.isBlank()) {
                    post.copy(id = UUID.randomUUID().toString())
                } else {
                    post
                }
                _allCommunityPosts.value += postWithId // Use += instead of = ... + ...
            }
        }
    }

    fun loadCommunityPosts() {
        viewModelScope.launch {
            try {
                // Try Firebase first
                val firebasePosts = firebaseService.getAllPosts()
                if (firebasePosts.isNotEmpty()) {
                    _allCommunityPosts.value = firebasePosts
                } else {
                    // Fallback to Room if Firebase has no posts
                    val roomPosts = repository.getAllCommunityPosts()
                    _allCommunityPosts.value = roomPosts
                }

                // Also load liked posts for current employer
                currentEmployer.value?.id?.let { employerId ->
                    val likedPosts = mutableSetOf<String>()
                    _allCommunityPosts.value.forEach { post ->
                        if (post.likedBy.contains(employerId.toString())) {
                            likedPosts.add(post.id)
                        }
                    }
                    _likedPosts.value = likedPosts
                }
            } catch (e: Exception) {
                Log.e("EmployerViewModel", "Error loading community posts: ${e.message}")
                // If Firebase fails, try Room as fallback
                try {
                    val roomPosts = repository.getAllCommunityPosts()
                    _allCommunityPosts.value = roomPosts
                } catch (roomError: Exception) {
                    Log.e("EmployerViewModel", "Error loading community posts from Room: ${roomError.message}")
                    // Keep existing posts if both fail
                }
            }
        }
    }

    fun togglePostLike(postId: String) {
        viewModelScope.launch {
            try {
                val employerId = currentEmployer.value?.id ?: return@launch
                val success = firebaseService.togglePostLike(postId, employerId)

                if (success) {
                    // Update local state
                    val currentLikes = _likedPosts.value.toMutableSet()
                    if (currentLikes.contains(postId)) {
                        currentLikes.remove(postId)
                    } else {
                        currentLikes.add(postId)
                    }
                    _likedPosts.value = currentLikes

                    // Update the post like count in the list
                    _allCommunityPosts.value = _allCommunityPosts.value.map { post ->
                        if (post.id == postId) {
                            val newLikes = if (currentLikes.contains(postId)) post.likes + 1 else post.likes - 1
                            post.copy(likes = newLikes.coerceAtLeast(0))
                        } else {
                            post
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("EmployerViewModel", "Error toggling post like: ${e.message}")
                // Fallback to local toggling
                val currentLikes = _likedPosts.value.toMutableSet()
                if (currentLikes.contains(postId)) {
                    currentLikes.remove(postId)
                } else {
                    currentLikes.add(postId)
                }
                _likedPosts.value = currentLikes

                // Update the post like count in the list
                _allCommunityPosts.value = _allCommunityPosts.value.map { post ->
                    if (post.id == postId) {
                        val newLikes = if (currentLikes.contains(postId)) post.likes + 1 else post.likes - 1
                        post.copy(likes = newLikes.coerceAtLeast(0))
                    } else {
                        post
                    }
                }
            }
        }
    }
    // Add this function to your existing EmployerViewModel class

    fun updateJobPost(jobPost: EmployerJobPost) {
        viewModelScope.launch {
            try {
                val success = repository.updateEmployerJobPost(jobPost)
                if (success) {
                    loadEmployerJobPosts() // Refresh the list
                    Log.d("EmployerViewModel", "Job post updated successfully: ${jobPost.title}")
                } else {
                    Log.e("EmployerViewModel", "Failed to update job post: ${jobPost.title}")
                }
            } catch (e: Exception) {
                Log.e("EmployerViewModel", "Error updating job post: ${e.message}")
            }
        }
    }

    // Add this function to set up real-time listener
    fun setupCommunityPostsListener() {
        firebaseService.addPostsRealTimeListener { posts ->
            _allCommunityPosts.value = posts
        }
    }

    fun isPostLiked(post: CommunityPost): Boolean {
        return _likedPosts.value.contains(post.id)
    }
}