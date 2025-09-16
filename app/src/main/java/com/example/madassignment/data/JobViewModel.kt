package com.example.madassignment.data


import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.madassignment.utils.ValidationUtils


class JobViewModel(private val context: Context) : ViewModel() {


    var newJobTitle by mutableStateOf("")
    var newJobCompany by mutableStateOf("")
    var newJobLocation by mutableStateOf("")
    var newJobType by mutableStateOf("Full time")
    var newJobSalary by mutableStateOf("")
    var newJobCategory by mutableStateOf("")
    var newJobSkills by mutableStateOf("")

    private val repository = AppRepository(context)
    private val firebaseService = FirebaseService(context)


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

    private val _showAddJobDialog = MutableStateFlow(false)
    val showAddJobDialog: StateFlow<Boolean> = _showAddJobDialog


    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    private val _allUsers = mutableStateListOf<User>()
    val allUsers: List<User> get() = _allUsers

    private val _allJobs = mutableStateListOf<Job>()
    val allJobs: List<Job> get() = _allJobs

    private val _allUsersWithProfiles = mutableStateListOf<Pair<User, UserProfile?>>()
    val allUsersWithProfiles: List<Pair<User, UserProfile?>> get() = _allUsersWithProfiles




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
            // Set up real-time listener with duplicate prevention
            firebaseService.addPostsRealTimeListener { updatedPosts ->
                // Remove duplicates by ID and ensure proper order
                val uniquePosts = updatedPosts.distinctBy { it.id }


                allCommunityPosts.clear()
                allCommunityPosts.addAll(uniquePosts)


                // Also update communityPosts (user-specific) if needed
                currentUser.value?.let { user ->
                    val userPosts = uniquePosts.filter { it.userId == user.id }
                    communityPosts.clear()
                    communityPosts.addAll(userPosts)
                }
            }
            repository.createDefaultAdmin()
        }
    }

    // Add this method to JobViewModel.kt
    suspend fun checkEmailExists(email: String): Boolean {
        return try {
            val user = repository.getUserByEmail(email)
            user != null
        } catch (e: Exception) {
            false
        }
    }


    // JobViewModel.kt - Add this method
    fun updateCommunityPost(postId: String, newContent: String) {
        viewModelScope.launch {
            try {
                val success = firebaseService.updatePostContent(postId, newContent)
                if (success) {
                    // Update local list immediately for better UX
                    val index = allCommunityPosts.indexOfFirst { it.id == postId }
                    if (index != -1) {
                        val updatedPost = allCommunityPosts[index].copy(content = newContent)
                        allCommunityPosts[index] = updatedPost

                        // Also update in user's posts if it exists there
                        val userIndex = communityPosts.indexOfFirst { it.id == postId }
                        if (userIndex != -1) {
                            communityPosts[userIndex] = updatedPost
                        }
                    }
                    _errorMessage.value = "Post updated successfully"
                } else {
                    _errorMessage.value = "Failed to update post"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error updating post: ${e.message}"
            }
        }
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            try {
                // You'll need to add a method to repository to get all users
                // For now, we'll simulate this - in real app, implement proper user fetching
                val current = currentUser.value
                if (current != null) {
                    // This is a placeholder - implement proper user loading from repository
                    _allUsers.clear()
                    _allUsers.add(current) // Add current user
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadAllUsersWithProfiles() {
        viewModelScope.launch {
            try {
                val usersWithProfiles = repository.getAllUsersWithProfiles()
                _allUsersWithProfiles.clear()
                _allUsersWithProfiles.addAll(usersWithProfiles)
                Log.d("JobViewModel", "Loaded ${usersWithProfiles.size} users with profiles")
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Failed to load users: ${e.message}"
            }
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            try {
                val success = repository.deleteUserFromFirestore(userId)
                if (success) {
                    // Remove from local list
                    _allUsersWithProfiles.removeAll { it.first.id == userId }
                    _errorMessage.value = "User deleted successfully"
                } else {
                    _errorMessage.value = "Failed to delete user"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Error deleting user: ${e.message}"
            }
        }
    }

    fun promoteToAdmin(userId: Int) {
        viewModelScope.launch {
            try {
                val success = repository.promoteUserToAdminInFirestore(userId)
                if (success) {
                    // Update local list
                    val index = _allUsersWithProfiles.indexOfFirst { it.first.id == userId }
                    if (index != -1) {
                        val (user, profile) = _allUsersWithProfiles[index]
                        val updatedUser = user.copy(isAdmin = true)
                        _allUsersWithProfiles[index] = Pair(updatedUser, profile)
                    }
                    _errorMessage.value = "User promoted to admin successfully"
                } else {
                    _errorMessage.value = "Failed to promote user to admin"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Error promoting user: ${e.message}"
            }
        }
    }



    fun loadAllJobs() {
        viewModelScope.launch {
            try {
                // For now, use sample jobs - you'll need to implement proper job loading
                _allJobs.clear()
                _allJobs.addAll(sampleRecommendedJobs + sampleNewJobs)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadAllJobsFromDatabase() {
        viewModelScope.launch {
            try {
                val dbJobs = repository.getAllSystemJobs()

                // Always ensure we have the sample jobs
                if (dbJobs.size < 40) { // Check if we have less than expected
                    Log.d("JobViewModel", "Only ${dbJobs.size} jobs in DB, reloading sample data")

                    // Clear existing jobs (optional)
                    dbJobs.forEach { job ->
                        repository.deleteJob(job.id)
                    }

                    // Load and save sample jobs
                    val sampleJobs = sampleRecommendedJobs + sampleNewJobs
                    sampleJobs.forEach { job ->
                        repository.addJob(job)
                    }

                    _allJobs.clear()
                    _allJobs.addAll(sampleJobs)
                } else {
                    // Load from database
                    _allJobs.clear()
                    _allJobs.addAll(dbJobs)
                    Log.d("JobViewModel", "Loaded ${dbJobs.size} jobs from database")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to sample data
                _allJobs.clear()
                _allJobs.addAll(sampleRecommendedJobs + sampleNewJobs)
            }
        }
    }

    fun addNewJob() {
        viewModelScope.launch {
            try {
                val newJob = Job(
                    title = newJobTitle,
                    company = newJobCompany,
                    location = newJobLocation,
                    type = newJobType,
                    salary = newJobSalary,
                    category = newJobCategory,
                    requiredSkills = newJobSkills,
                    originalJobId = System.currentTimeMillis().toInt(), // Unique ID
                    userId = 0 // System job
                )

                // Use repository to save to database
                val result = repository.addJob(newJob)

                if (result > 0) {
                    // Add to local list with the actual ID from database
                    val jobWithId = newJob.copy(id = result.toInt())
                    _allJobs.add(jobWithId)

                    // Clear form and close dialog
                    resetJobForm()
                    _showAddJobDialog.value = false

                    Log.d("JobViewModel", "Job added successfully. Total jobs: ${_allJobs.size}")
                } else {
                    _errorMessage.value = "Failed to add job to database"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Error adding job: ${e.message}"
            }
        }
    }

    fun deleteJob(job: Job) {
        viewModelScope.launch {
            try {
                val success = repository.deleteJob(job.id)
                if (success) {
                    _allJobs.removeAll { it.id == job.id }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setShowAddJobDialog(value: Boolean) {
        _showAddJobDialog.value = value
    }

    private fun resetJobForm() {
        newJobTitle = ""
        newJobCompany = ""
        newJobLocation = ""
        newJobType = "Full time"
        newJobSalary = ""
        newJobCategory = ""
        newJobSkills = ""
    }


    // Method to get user name by ID
    fun getUserNameById(userId: Int): String {
        return allUsers.find { it.id == userId }?.name ?: "User $userId"
    }


    // Method to get user names from likedBy string
    fun getLikerNames(likedBy: String): List<String> {
        if (likedBy.isBlank()) return emptyList()


        return try {
            likedBy.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .map { entry ->
                    // Parse format: "userId:userName"
                    val parts = entry.split(":")
                    if (parts.size >= 2) {
                        parts[1] // Return the user name
                    } else {
                        "Unknown User"
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
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

    fun resetJobsDatabase() {
        viewModelScope.launch {
            try {
                // Clear all jobs from database
                _allJobs.forEach { job ->
                    repository.deleteJob(job.id)
                }

                // Clear local list
                _allJobs.clear()

                // Reload sample data
                loadAllJobsFromDatabase()

                Log.d("JobViewModel", "Database reset, reloading sample jobs")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun migrateJobsToFirestore() {
        viewModelScope.launch {
            try {
                val success = repository.migrateJobsToFirestore()
                if (success) {
                    _errorMessage.value = "Jobs migrated successfully!"
                } else {
                    _errorMessage.value = "Migration failed"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Migration error: ${e.message}"
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


    // JobViewModel.kt - Add admin delete method
    fun deleteCommunityPostAsAdmin(post: CommunityPost) {
        viewModelScope.launch {
            try {
                // Delete from Firebase
                val firebaseSuccess = firebaseService.deletePost(post.id)


                if (firebaseSuccess) {
                    // Remove from local lists
                    communityPosts.removeAll { it.id == post.id }
                    allCommunityPosts.removeAll { it.id == post.id }


                    // Also delete from Room as fallback
                    // You might want to add a repository method for this
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun deleteCommunityPost(post: CommunityPost) {
        viewModelScope.launch {
            try {
                // Delete from Firebase
                val firebaseSuccess = firebaseService.deletePost(post.id)


                if (firebaseSuccess) {
                    // Remove from local lists
                    communityPosts.removeAll { it.id == post.id }
                    allCommunityPosts.removeAll { it.id == post.id }


                    // Also delete from Room as fallback
                    currentUser.value?.let { user ->
                        // This would require adding a delete method to repository
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    // JobViewModel.kt - Fixed addCommunityPost method
    fun addCommunityPost(post: CommunityPost) {
        viewModelScope.launch {
            try {
                val user = currentUser.value
                if (user != null) {
                    val postWithUser = post.copy(userId = user.id)
                    val firebasePostId = firebaseService.addPost(postWithUser)


                    if (firebasePostId.isNotBlank()) {
                        val newPost = postWithUser.copy(id = firebasePostId)


                        // Don't add to local lists here - let the real-time listener handle it
                        // This prevents duplicates
                        println("DEBUG: Post added to Firebase, waiting for real-time update")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    // Also update the isPostLiked method to take postId instead of post
    fun isPostLiked(postId: String): Boolean {
        val user = currentUser.value ?: return false
        val post = allCommunityPosts.find { it.id == postId } ?: return false


        println("DEBUG: Checking if user ${user.id} liked post ${post.id}")
        println("DEBUG: likedBy content: '${post.likedBy}'")


        if (post.likedBy.isBlank()) {
            println("DEBUG: No likes found")
            return false
        }


        return try {
            val entries = post.likedBy.split(",")
            println("DEBUG: Like entries: $entries")


            val isLiked = entries.any { entry ->
                val trimmed = entry.trim()
                val startsWithId = trimmed.startsWith("${user.id}:")
                println("DEBUG: Entry '$trimmed' starts with user ID? $startsWithId")
                startsWithId
            }


            println("DEBUG: User liked this post? $isLiked")
            isLiked
        } catch (e: Exception) {
            e.printStackTrace()
            println("DEBUG: Error checking like: ${e.message}")
            false
        }
    }






    fun togglePostLike(postId: String) {
        viewModelScope.launch {
            try {
                val user = currentUser.value
                val userProfile = userProfile.value
                if (user != null) {
                    val userName = userProfile?.name ?: user.name
                    println("DEBUG: Toggling like for user ${user.id}:$userName on post $postId")


                    val success = firebaseService.togglePostLike(postId, user.id, userName)


                    println("DEBUG: Toggle result: $success")


                    if (success) {
                        loadCommunityPosts()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("DEBUG: Error in togglePostLike: ${e.message}")
            }
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



