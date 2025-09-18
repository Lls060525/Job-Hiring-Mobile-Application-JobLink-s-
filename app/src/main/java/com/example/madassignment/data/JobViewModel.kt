package com.example.madassignment.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.madassignment.utils.ValidationUtils
import kotlinx.coroutines.delay

class JobViewModel(context: Context) : ViewModel() {
    private val repository = AppRepository(context)
    private val firebaseService = FirebaseService(context)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _jobStates = MutableStateFlow<Map<Int, Pair<Boolean, Boolean>>>(emptyMap())
    val jobStates: StateFlow<Map<Int, Pair<Boolean, Boolean>>> = _jobStates
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _allAvailableJobs = MutableStateFlow<List<Job>>(emptyList())
    val allAvailableJobs: StateFlow<List<Job>> = _allAvailableJobs

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _isProfileSetupComplete = MutableStateFlow(false)
    val isProfileSetupComplete: StateFlow<Boolean> = _isProfileSetupComplete

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _authState = MutableStateFlow(AuthState.IDLE)
    val authState: StateFlow<AuthState> = _authState

    private val _savedJobs = MutableStateFlow<List<Job>>(emptyList())
    val savedJobs: StateFlow<List<Job>> = _savedJobs

    private val _appliedJobs = MutableStateFlow<List<Job>>(emptyList())
    val appliedJobs: StateFlow<List<Job>> = _appliedJobs

    private val _communityPosts = mutableStateListOf<CommunityPost>()
    val communityPosts: List<CommunityPost> get() = _communityPosts
    val allCommunityPosts = mutableStateListOf<CommunityPost>()

    // Admin-related state flows
    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin

    private val _allUsersWithProfiles = mutableStateListOf<Pair<User, UserProfile?>>()
    val allUsersWithProfiles: List<Pair<User, UserProfile?>> get() = _allUsersWithProfiles

    private val _allJobs = mutableStateListOf<Job>()
    val allJobs: List<Job> get() = _allJobs

    private val _showAddJobDialog = MutableStateFlow(false)
    val showAddJobDialog: StateFlow<Boolean> = _showAddJobDialog

    private val _registeredName = MutableStateFlow<String?>(null)
    val registeredName: StateFlow<String?> = _registeredName

    // Form fields for adding new jobs
    var newJobTitle by mutableStateOf("")
    var newJobCompany by mutableStateOf("")
    var newJobLocation by mutableStateOf("")
    var newJobType by mutableStateOf("Full time")
    var newJobSalary by mutableStateOf("")
    var newJobCategory by mutableStateOf("")
    var newJobSkills by mutableStateOf("")

    val sampleRecommendedJobs = getRecommendedJobs()
    val sampleNewJobs = getNewJobs()

    init {
        viewModelScope.launch {
            loadAllJobs()
            currentUser.value?.let { user ->
                loadAllUserData(user.id)
            }
            firebaseService.addPostsRealTimeListener { updatedPosts ->
                allCommunityPosts.clear()
                allCommunityPosts.addAll(updatedPosts)
            }
            repository.createDefaultAdmin()
        }
    }

    fun setRegisteredName(name: String) {
        _registeredName.value = name
    }

    fun clearRegisteredName() {
        _registeredName.value = null
    }

    fun setupFirebaseSync() {
        viewModelScope.launch {
            // Initial sync
            repository.syncAllDataToFirebase()

            // Set up periodic sync (every 5 minutes)
            while (true) {
                delay(5 * 60 * 1000) // 5 minutes
                repository.syncAllDataToFirebase()
            }
        }
    }

    // Add the deletePost function here, before it's used
    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                val success = firebaseService.deletePost(postId)
                if (success) {
                    // Remove from local lists
                    _communityPosts.removeAll { it.id == postId }
                    allCommunityPosts.removeAll { it.id == postId }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    // ADMIN METHODS
// ADMIN METHODS
    suspend fun loginAdmin(email: String, password: String): Result<Boolean> {
        return try {
            // FIX: Check against LOCAL database, not Firebase
            val admin = repository.getUserByEmail(email)

            Log.d("AdminLogin", "Found user: ${admin?.email}, isAdmin: ${admin?.isAdmin}")

            if (admin != null && admin.password == password && admin.isAdmin) {
                _currentUser.value = admin
                loadUserProfile(admin.id) // Load profile if exists
                Log.d("AdminLogin", "Admin login successful")
                Result.success(true)
            } else {
                Log.d("AdminLogin", "Login failed - admin: $admin, password match: ${admin?.password == password}, isAdmin: ${admin?.isAdmin}")
                Result.failure(Exception("Invalid admin credentials or user is not an admin"))
            }
        } catch (e: Exception) {
            Log.e("AdminLogin", "Error: ${e.message}")
            Result.failure(Exception("Login failed: ${e.message}"))
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

    fun deleteCommunityPost(post: CommunityPost) {
        viewModelScope.launch {
            try {
                // Delete from Firebase
                val firebaseSuccess = firebaseService.deletePost(post.id)

                if (firebaseSuccess) {
                    // Remove from local lists
                    _communityPosts.removeAll { it.id == post.id }
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

    fun checkAdminStatus() {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                val dbUser = repository.getUserById(user.id)
                _isAdmin.value = dbUser?.isAdmin ?: false
            }
        }
    }

    // CORE USER METHODS
    fun loadAllCommunityPosts() {
        viewModelScope.launch {
            try {
                val posts = repository.getAllCommunityPosts()
                allCommunityPosts.clear()
                allCommunityPosts.addAll(posts)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun loadAllJobs() {
        try {
            val sampleJobs = getRecommendedJobs() + getNewJobs()
            val employerJobs = repository.getAllEmployerJobsAsJobs()
            val allJobs = sampleJobs + employerJobs
            _allAvailableJobs.value = allJobs
        } catch (e: Exception) {
            e.printStackTrace()
            _allAvailableJobs.value = getRecommendedJobs() + getNewJobs()
        }
    }

    private suspend fun loadUserData(userId: Int) {
        try {
            repository.initializeUserSampleData(userId)
            loadAllUserData(userId)
            loadUserProfile(userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun refreshUserData() {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                loadAllUserData(user.id)
            }
        }
    }

    // FIXED: Real-time state update function
    private fun updateJobStateImmediately(jobId: Int, isSaved: Boolean? = null, isApplied: Boolean? = null) {
        val currentStates = _jobStates.value.toMutableMap()
        val currentState = currentStates[jobId] ?: Pair(false, false)
        val newState = Pair(
            isSaved ?: currentState.first,
            isApplied ?: currentState.second
        )
        currentStates[jobId] = newState
        _jobStates.value = currentStates
        Log.d("JobViewModel", "Updated job state for ID $jobId: saved=${newState.first}, applied=${newState.second}")
    }

    private suspend fun loadAllUserData(userId: Int) {
        try {
            val saved = repository.getUserSavedJobs(userId)
            val applied = repository.getUserAppliedJobs(userId)

            Log.d("JobViewModel", "Loading user data: ${saved.size} saved, ${applied.size} applied")

            // Update the StateFlow values first
            _savedJobs.value = saved
            _appliedJobs.value = applied

            // Create a new job states map based on actual database data
            val newJobStates = mutableMapOf<Int, Pair<Boolean, Boolean>>()

            // Update states for all available jobs based on database data
            allAvailableJobs.value.forEach { job ->
                val isSaved = saved.any {
                    it.originalJobId == job.originalJobId ||
                            it.id == job.id ||
                            (job.originalJobId > 10000 && it.originalJobId == job.originalJobId) ||
                            (it.originalJobId > 10000 && it.originalJobId == job.originalJobId)
                }
                val isApplied = applied.any {
                    it.originalJobId == job.originalJobId ||
                            it.id == job.id ||
                            (job.originalJobId > 10000 && it.originalJobId == job.originalJobId) ||
                            (it.originalJobId > 10000 && it.originalJobId == job.originalJobId)
                }
                newJobStates[job.originalJobId] = Pair(isSaved, isApplied)

                // Debug logging for each job
                if (isSaved || isApplied) {
                    Log.d("JobViewModel", "Job ${job.title} (ID: ${job.originalJobId}) - Saved: $isSaved, Applied: $isApplied")
                }
            }

            // Only update _jobStates if we actually have meaningful changes
            // This prevents overriding immediate UI updates
            if (newJobStates.isNotEmpty()) {
                _jobStates.value = newJobStates
                Log.d("JobViewModel", "Updated job states: ${newJobStates.size} entries")
            }

            Log.d("JobViewModel", "Loaded ${saved.size} saved and ${applied.size} applied jobs")
        } catch (e: Exception) {
            Log.e("JobViewModel", "Error loading user data: ${e.message}")
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
            val firebasePosts = firebaseService.getAllPosts()
            if (firebasePosts.isNotEmpty()) {
                allCommunityPosts.clear()
                allCommunityPosts.addAll(firebasePosts)
            } else {
                val posts = repository.getAllCommunityPosts()
                allCommunityPosts.clear()
                allCommunityPosts.addAll(posts)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val posts = repository.getAllCommunityPosts()
            allCommunityPosts.clear()
            allCommunityPosts.addAll(posts)
        }
    }

    // AUTH METHODS
    suspend fun register(email: String, password: String, name: String): Result<Unit> {
        return try {
            _authState.value = AuthState.LOADING

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
                    loadUserProfile(it.id)
                }
                _authState.value = AuthState.SUCCESS
                Result.success(Unit)
            } else {
                throw Exception("Registration failed")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.ERROR
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            _authState.value = AuthState.LOADING

            if (!ValidationUtils.isValidEmail(email)) {
                throw Exception("Please enter a valid email address")
            }

            val user = repository.loginUser(email, password)
            if (user != null) {
                afterLogin(user)
                Result.success(Unit)
            } else {
                throw Exception("Invalid email or password")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.ERROR
            Result.failure(e)
        }
    }

    private suspend fun afterLogin(user: User) {
        _currentUser.value = user
        println("DEBUG: User logged in: $user")
        loadUserData(user.id)
        loadUserProfile(user.id)
        loadAllJobs()
        checkAdminStatus() // Check if user is admin
        _authState.value = AuthState.SUCCESS
    }

    suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
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
        _savedJobs.value = emptyList()
        _appliedJobs.value = emptyList()
        _jobStates.value = emptyMap()
        _communityPosts.clear()
        _isAdmin.value = false
        _allUsersWithProfiles.clear()
        _allJobs.clear()
    }

    // JOB ACTION METHODS
    // FIXED: Apply to job with proper state persistence
    fun applyToJob(job: Job) {
        viewModelScope.launch {
            try {
                currentUser.value?.let { user ->
                    Log.d("JobViewModel", "Applying to job: ${job.title} (ID: ${job.originalJobId})")

                    // Update database FIRST
                    if (job.originalJobId > 10000) {
                        val employerJobId = job.originalJobId - 10000
                        repository.applyToEmployerJob(employerJobId, user.id)
                    }
                    repository.applyToUserJob(job, user.id)

                    // Then update UI state and keep it persistent
                    updateJobStateImmediately(job.originalJobId, isApplied = true)

                    // Reload user data but preserve the UI state
                    val saved = repository.getUserSavedJobs(user.id)
                    val applied = repository.getUserAppliedJobs(user.id)

                    // Update the StateFlow values
                    _savedJobs.value = saved
                    _appliedJobs.value = applied

                    // Rebuild job states based on fresh data
                    val newJobStates = _jobStates.value.toMutableMap()
                    allAvailableJobs.value.forEach { availableJob ->
                        val isSaved = saved.any {
                            it.originalJobId == availableJob.originalJobId || it.id == availableJob.id
                        }
                        val isApplied = applied.any {
                            it.originalJobId == availableJob.originalJobId || it.id == availableJob.id
                        }
                        newJobStates[availableJob.originalJobId] = Pair(isSaved, isApplied)
                    }
                    _jobStates.value = newJobStates

                    Log.d("JobViewModel", "Successfully applied to job: ${job.title}")
                }
            } catch (e: Exception) {
                Log.e("JobViewModel", "Error applying to job: ${e.message}")
                // Revert UI state on error
                updateJobStateImmediately(job.originalJobId, isApplied = false)
            }
        }
    }

    // FIXED: Save job with proper state persistence
    fun saveJob(job: Job) {
        viewModelScope.launch {
            try {
                currentUser.value?.let { user ->
                    Log.d("JobViewModel", "Saving job: ${job.title} (ID: ${job.originalJobId})")

                    // Update database FIRST
                    repository.saveUserJob(job, user.id)

                    // Then update UI state and keep it persistent
                    updateJobStateImmediately(job.originalJobId, isSaved = true)

                    // Reload user data but preserve the UI state
                    val saved = repository.getUserSavedJobs(user.id)
                    val applied = repository.getUserAppliedJobs(user.id)

                    // Update the StateFlow values
                    _savedJobs.value = saved
                    _appliedJobs.value = applied

                    // Rebuild job states based on fresh data
                    val newJobStates = _jobStates.value.toMutableMap()
                    allAvailableJobs.value.forEach { availableJob ->
                        val isSaved = saved.any {
                            it.originalJobId == availableJob.originalJobId || it.id == availableJob.id
                        }
                        val isApplied = applied.any {
                            it.originalJobId == availableJob.originalJobId || it.id == availableJob.id
                        }
                        newJobStates[availableJob.originalJobId] = Pair(isSaved, isApplied)
                    }
                    _jobStates.value = newJobStates

                    Log.d("JobViewModel", "Successfully saved job: ${job.title}")
                }
            } catch (e: Exception) {
                Log.e("JobViewModel", "Error saving job: ${e.message}")
                // Revert UI state on error
                updateJobStateImmediately(job.originalJobId, isSaved = false)
            }
        }
    }

    // FIXED: Remove saved job with proper state management
    fun removeSavedJob(job: Job) {
        viewModelScope.launch {
            try {
                currentUser.value?.let { user ->
                    Log.d("JobViewModel", "Removing saved job: ${job.title} (ID: ${job.originalJobId})")

                    // Find the actual saved job in the database that matches this job
                    val savedJobToRemove = _savedJobs.value.find { savedJob ->
                        savedJob.id == job.id ||
                                (savedJob.originalJobId != 0 && savedJob.originalJobId == job.originalJobId) ||
                                (job.originalJobId != 0 && savedJob.originalJobId == job.originalJobId)
                    }

                    if (savedJobToRemove != null) {
                        // Remove from database FIRST
                        repository.removeSavedJob(user.id, savedJobToRemove.id)
                        Log.d("JobViewModel", "Removed saved job with database ID: ${savedJobToRemove.id}")

                        // Update UI state immediately
                        updateJobStateImmediately(job.originalJobId, isSaved = false)

                        // Refresh data to ensure consistency
                        val saved = repository.getUserSavedJobs(user.id)
                        val applied = repository.getUserAppliedJobs(user.id)

                        // Update the StateFlow values
                        _savedJobs.value = saved
                        _appliedJobs.value = applied

                        // Rebuild job states
                        val newJobStates = _jobStates.value.toMutableMap()
                        allAvailableJobs.value.forEach { availableJob ->
                            val isSaved = saved.any {
                                it.originalJobId == availableJob.originalJobId || it.id == availableJob.id
                            }
                            val isApplied = applied.any {
                                it.originalJobId == availableJob.originalJobId || it.id == availableJob.id
                            }
                            newJobStates[availableJob.originalJobId] = Pair(isSaved, isApplied)
                        }
                        _jobStates.value = newJobStates

                        Log.d("JobViewModel", "Successfully removed saved job")
                    } else {
                        Log.d("JobViewModel", "Could not find saved job to remove")
                    }
                }
            } catch (e: Exception) {
                Log.e("JobViewModel", "Error removing saved job: ${e.message}")
                // Revert UI state on error
                updateJobStateImmediately(job.originalJobId, isSaved = true)
            }
        }
    }

    // FIXED: Remove applied job with proper state management
    fun removeAppliedJob(job: Job) {
        viewModelScope.launch {
            try {
                currentUser.value?.let { user ->
                    Log.d("JobViewModel", "Removing applied job: ${job.title} (ID: ${job.originalJobId})")

                    // Find the actual applied job in the database that matches this job
                    val appliedJobToRemove = _appliedJobs.value.find { appliedJob ->
                        appliedJob.id == job.id ||
                                (appliedJob.originalJobId != 0 && appliedJob.originalJobId == job.originalJobId) ||
                                (job.originalJobId != 0 && appliedJob.originalJobId == job.originalJobId)
                    }

                    if (appliedJobToRemove != null) {
                        // Remove from database FIRST
                        repository.removeAppliedJob(user.id, appliedJobToRemove.id)
                        Log.d("JobViewModel", "Removed applied job with database ID: ${appliedJobToRemove.id}")

                        // Update UI state immediately
                        updateJobStateImmediately(job.originalJobId, isApplied = false)

                        // Refresh data to ensure consistency
                        val saved = repository.getUserSavedJobs(user.id)
                        val applied = repository.getUserAppliedJobs(user.id)

                        // Update the StateFlow values
                        _savedJobs.value = saved
                        _appliedJobs.value = applied

                        // Rebuild job states
                        val newJobStates = _jobStates.value.toMutableMap()
                        allAvailableJobs.value.forEach { availableJob ->
                            val isSaved = saved.any {
                                it.originalJobId == availableJob.originalJobId || it.id == availableJob.id
                            }
                            val isApplied = applied.any {
                                it.originalJobId == availableJob.originalJobId || it.id == availableJob.id
                            }
                            newJobStates[availableJob.originalJobId] = Pair(isSaved, isApplied)
                        }
                        _jobStates.value = newJobStates

                        Log.d("JobViewModel", "Successfully removed applied job")
                    } else {
                        Log.d("JobViewModel", "Could not find applied job to remove")
                    }
                }
            } catch (e: Exception) {
                Log.e("JobViewModel", "Error removing applied job: ${e.message}")
                // Revert UI state on error
                updateJobStateImmediately(job.originalJobId, isApplied = true)
            }
        }
    }

    // PASSWORD RECOVERY
    suspend fun retrievePassword(email: String, name: String): Result<String> {
        return try {
            _authState.value = AuthState.LOADING

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
            Result.success(user.password)
        } catch (e: Exception) {
            _authState.value = AuthState.ERROR
            Result.failure(e)
        }
    }

    // COMMUNITY METHODS
    fun addCommunityPost(post: CommunityPost) {
        viewModelScope.launch {
            try {
                val firebasePostId = firebaseService.addPost(post)
                if (firebasePostId.isNotBlank()) {
                    val newPost = post.copy(id = firebasePostId)
                    _communityPosts.add(0, newPost)
                    allCommunityPosts.add(0, newPost)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val user = currentUser.value
                if (user != null) {
                    val postId = repository.addUserPost(post, user.id)
                    if (postId > 0) {
                        val newPost = post.copy(id = postId.toString(), userId = user.id)
                        _communityPosts.add(0, newPost)
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
                    val success = firebaseService.togglePostLike(postId, user.id)
                    if (success) {
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
        return post.likedBy.split(",").contains(user.id.toString())
    }

    // UTILITY METHODS
    fun getAllAvailableJobs(): List<Job> {
        return allAvailableJobs.value.ifEmpty {
            sampleRecommendedJobs + sampleNewJobs
        }
    }

    fun refreshAllData() {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                loadAllJobs()
                loadAllUserData(user.id)
            }
        }
    }

    fun refreshAllJobs() {
        viewModelScope.launch {
            loadAllJobs()
        }
    }

    suspend fun getSkillBasedJobs(userSkills: String): List<Job> {
        return repository.getRecommendedJobsBasedOnSkills(userSkills)
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // PRIVATE HELPER METHODS
    private fun getRecommendedJobs(): List<Job> {
        // Implementation for recommended jobs
        return listOf(
            Job(
                title = "Software Engineer",
                company = "Tech Corp",
                location = "Remote",
                type = "Full time",
                salary = "$80,000 - $120,000",
                category = "Technology",
                requiredSkills = "Java, Kotlin, Android",
                originalJobId = 1001,
                userId = 0
            ),
            Job(
                title = "Product Manager",
                company = "Innovation Inc",
                location = "New York",
                type = "Full time",
                salary = "$90,000 - $130,000",
                category = "Management",
                requiredSkills = "Product Management, Analytics, Strategy",
                originalJobId = 1002,
                userId = 0
            ),
            Job(
                title = "UX Designer",
                company = "Design Studio",
                location = "San Francisco",
                type = "Full time",
                salary = "$70,000 - $100,000",
                category = "Design",
                requiredSkills = "Figma, User Research, Prototyping",
                originalJobId = 1003,
                userId = 0
            ),
            Job(
                title = "Data Scientist",
                company = "Analytics Co",
                location = "Boston",
                type = "Full time",
                salary = "$95,000 - $140,000",
                category = "Data Science",
                requiredSkills = "Python, Machine Learning, Statistics",
                originalJobId = 1004,
                userId = 0
            ),
            Job(
                title = "Marketing Specialist",
                company = "Brand Agency",
                location = "Chicago",
                type = "Full time",
                salary = "$50,000 - $75,000",
                category = "Marketing",
                requiredSkills = "Digital Marketing, SEO, Content Creation",
                originalJobId = 1005,
                userId = 0
            ),
            Job(
                title = "DevOps Engineer",
                company = "Cloud Solutions",
                location = "Remote",
                type = "Full time",
                salary = "$85,000 - $125,000",
                category = "Technology",
                requiredSkills = "AWS, Docker, Kubernetes",
                originalJobId = 1006,
                userId = 0
            ),
            Job(
                title = "Sales Manager",
                company = "Sales Force Inc",
                location = "Miami",
                type = "Full time",
                salary = "$60,000 - $90,000",
                category = "Sales",
                requiredSkills = "Sales Management, CRM, Leadership",
                originalJobId = 1007,
                userId = 0
            ),
            Job(
                title = "Frontend Developer",
                company = "Web Solutions",
                location = "Remote",
                type = "Full time",
                salary = "$65,000 - $95,000",
                category = "Technology",
                requiredSkills = "React, JavaScript, HTML/CSS",
                originalJobId = 1008,
                userId = 0
            ),
            Job(
                title = "HR Specialist",
                company = "People First",
                location = "Austin",
                type = "Full time",
                salary = "$45,000 - $65,000",
                category = "Human Resources",
                requiredSkills = "Recruitment, Employee Relations, HRIS",
                originalJobId = 1009,
                userId = 0
            ),
            Job(
                title = "Financial Analyst",
                company = "Finance Pro",
                location = "New York",
                type = "Full time",
                salary = "$70,000 - $100,000",
                category = "Finance",
                requiredSkills = "Financial Modeling, Excel, Analysis",
                originalJobId = 1010,
                userId = 0
            ),
            Job(
                title = "Content Writer",
                company = "Content Hub",
                location = "Remote",
                type = "Part time",
                salary = "$30,000 - $50,000",
                category = "Writing",
                requiredSkills = "Content Writing, SEO, Research",
                originalJobId = 1011,
                userId = 0
            ),
            Job(
                title = "Mobile Developer",
                company = "App Studio",
                location = "Seattle",
                type = "Full time",
                salary = "$75,000 - $110,000",
                category = "Technology",
                requiredSkills = "Swift, Kotlin, React Native",
                originalJobId = 1012,
                userId = 0
            ),
            Job(
                title = "Project Coordinator",
                company = "Project Solutions",
                location = "Denver",
                type = "Full time",
                salary = "$50,000 - $70,000",
                category = "Management",
                requiredSkills = "Project Management, Communication, Organization",
                originalJobId = 1013,
                userId = 0
            ),
            Job(
                title = "Graphic Designer",
                company = "Creative Works",
                location = "Los Angeles",
                type = "Full time",
                salary = "$45,000 - $70,000",
                category = "Design",
                requiredSkills = "Adobe Creative Suite, Branding, Typography",
                originalJobId = 1014,
                userId = 0
            ),
            Job(
                title = "Quality Assurance Analyst",
                company = "Test Labs",
                location = "Remote",
                type = "Full time",
                salary = "$55,000 - $80,000",
                category = "Technology",
                requiredSkills = "Testing, Automation, Bug Tracking",
                originalJobId = 1015,
                userId = 0
            ),
            Job(
                title = "Business Analyst",
                company = "Business Intelligence",
                location = "Washington DC",
                type = "Full time",
                salary = "$65,000 - $90,000",
                category = "Business",
                requiredSkills = "Requirements Analysis, Documentation, Process Improvement",
                originalJobId = 1016,
                userId = 0
            ),
            Job(
                title = "Customer Success Manager",
                company = "Customer First",
                location = "San Diego",
                type = "Full time",
                salary = "$60,000 - $85,000",
                category = "Customer Service",
                requiredSkills = "Customer Relations, Account Management, Problem Solving",
                originalJobId = 1017,
                userId = 0
            ),
            Job(
                title = "Social Media Manager",
                company = "Social Buzz",
                location = "Remote",
                type = "Full time",
                salary = "$40,000 - $60,000",
                category = "Marketing",
                requiredSkills = "Social Media Marketing, Content Creation, Analytics",
                originalJobId = 1018,
                userId = 0
            ),
            Job(
                title = "Network Administrator",
                company = "IT Infrastructure",
                location = "Phoenix",
                type = "Full time",
                salary = "$60,000 - $85,000",
                category = "Technology",
                requiredSkills = "Network Management, Security, Troubleshooting",
                originalJobId = 1019,
                userId = 0
            ),
            Job(
                title = "Operations Manager",
                company = "Efficient Ops",
                location = "Atlanta",
                type = "Full time",
                salary = "$70,000 - $100,000",
                category = "Operations",
                requiredSkills = "Operations Management, Process Optimization, Leadership",
                originalJobId = 1020,
                userId = 0
            )
        )
    }

    private fun getNewJobs(): List<Job> {
        // Implementation for new jobs
        return listOf(
            Job(
                title = "Senior Software Architect",
                company = "Enterprise Tech",
                location = "Remote",
                type = "Full time",
                salary = "$130,000 - $180,000",
                category = "Technology",
                requiredSkills = "System Architecture, Microservices, Leadership",
                originalJobId = 2001,
                userId = 0
            ),
            Job(
                title = "AI/ML Engineer",
                company = "AI Innovations",
                location = "Silicon Valley",
                type = "Full time",
                salary = "$120,000 - $170,000",
                category = "Technology",
                requiredSkills = "Machine Learning, TensorFlow, Python",
                originalJobId = 2002,
                userId = 0
            ),
            Job(
                title = "Blockchain Developer",
                company = "Crypto Solutions",
                location = "Remote",
                type = "Full time",
                salary = "$100,000 - $150,000",
                category = "Technology",
                requiredSkills = "Blockchain, Solidity, Smart Contracts",
                originalJobId = 2003,
                userId = 0
            ),
            Job(
                title = "Cybersecurity Analyst",
                company = "SecureNet",
                location = "Washington DC",
                type = "Full time",
                salary = "$80,000 - $120,000",
                category = "Security",
                requiredSkills = "Cybersecurity, Incident Response, Risk Assessment",
                originalJobId = 2004,
                userId = 0
            ),
            Job(
                title = "Cloud Solutions Architect",
                company = "CloudTech",
                location = "Remote",
                type = "Full time",
                salary = "$110,000 - $160,000",
                category = "Technology",
                requiredSkills = "AWS, Azure, Cloud Architecture",
                originalJobId = 2005,
                userId = 0
            ),
            Job(
                title = "Product Designer",
                company = "Design Forward",
                location = "Portland",
                type = "Full time",
                salary = "$75,000 - $110,000",
                category = "Design",
                requiredSkills = "Product Design, User Experience, Prototyping",
                originalJobId = 2006,
                userId = 0
            ),
            Job(
                title = "Growth Marketing Manager",
                company = "Growth Hackers",
                location = "Remote",
                type = "Full time",
                salary = "$70,000 - $100,000",
                category = "Marketing",
                requiredSkills = "Growth Marketing, A/B Testing, Analytics",
                originalJobId = 2007,
                userId = 0
            ),
            Job(
                title = "Site Reliability Engineer",
                company = "Scale Systems",
                location = "San Francisco",
                type = "Full time",
                salary = "$95,000 - $140,000",
                category = "Technology",
                requiredSkills = "SRE, Monitoring, Automation",
                originalJobId = 2008,
                userId = 0
            ),
            Job(
                title = "Technical Writer",
                company = "Documentation Pro",
                location = "Remote",
                type = "Full time",
                salary = "$55,000 - $80,000",
                category = "Writing",
                requiredSkills = "Technical Writing, Documentation, API Documentation",
                originalJobId = 2009,
                userId = 0
            ),
            Job(
                title = "Full Stack Developer",
                company = "Web Innovations",
                location = "Austin",
                type = "Full time",
                salary = "$70,000 - $105,000",
                category = "Technology",
                requiredSkills = "Full Stack Development, Node.js, React",
                originalJobId = 2010,
                userId = 0
            ),
            Job(
                title = "Digital Product Manager",
                company = "Digital First",
                location = "New York",
                type = "Full time",
                salary = "$85,000 - $125,000",
                category = "Management",
                requiredSkills = "Digital Products, Agile, User Stories",
                originalJobId = 2011,
                userId = 0
            ),
            Job(
                title = "Data Engineer",
                company = "Big Data Corp",
                location = "Remote",
                type = "Full time",
                salary = "$90,000 - $130,000",
                category = "Data Science",
                requiredSkills = "Data Engineering, ETL, Apache Spark",
                originalJobId = 2012,
                userId = 0
            ),
            Job(
                title = "UX Researcher",
                company = "Research Labs",
                location = "Chicago",
                type = "Full time",
                salary = "$70,000 - $95,000",
                category = "Design",
                requiredSkills = "User Research, Usability Testing, Analytics",
                originalJobId = 2013,
                userId = 0
            ),
            Job(
                title = "Sales Development Representative",
                company = "Sales Growth",
                location = "Remote",
                type = "Full time",
                salary = "$45,000 - $70,000",
                category = "Sales",
                requiredSkills = "Sales Development, Lead Generation, CRM",
                originalJobId = 2014,
                userId = 0
            ),
            Job(
                title = "Backend Developer",
                company = "API Masters",
                location = "Seattle",
                type = "Full time",
                salary = "$75,000 - $110,000",
                category = "Technology",
                requiredSkills = "Backend Development, APIs, Databases",
                originalJobId = 2015,
                userId = 0
            ),
            Job(
                title = "Digital Marketing Specialist",
                company = "Online Growth",
                location = "Remote",
                type = "Full time",
                salary = "$50,000 - $75,000",
                category = "Marketing",
                requiredSkills = "Digital Marketing, PPC, Email Marketing",
                originalJobId = 2016,
                userId = 0
            ),
            Job(
                title = "Business Intelligence Analyst",
                company = "Data Insights",
                location = "Boston",
                type = "Full time",
                salary = "$65,000 - $90,000",
                category = "Business",
                requiredSkills = "BI Tools, SQL, Data Visualization",
                originalJobId = 2017,
                userId = 0
            ),
            Job(
                title = "IT Support Specialist",
                company = "Tech Support Plus",
                location = "Remote",
                type = "Full time",
                salary = "$40,000 - $60,000",
                category = "Technology",
                requiredSkills = "IT Support, Troubleshooting, Help Desk",
                originalJobId = 2018,
                userId = 0
            ),
            Job(
                title = "Account Executive",
                company = "Enterprise Sales",
                location = "Dallas",
                type = "Full time",
                salary = "$60,000 - $90,000",
                category = "Sales",
                requiredSkills = "Account Management, B2B Sales, Relationship Building",
                originalJobId = 2019,
                userId = 0
            ),
            Job(
                title = "Systems Administrator",
                company = "Infrastructure Pro",
                location = "Denver",
                type = "Full time",
                salary = "$65,000 - $90,000",
                category = "Technology",
                requiredSkills = "System Administration, Linux, Windows Server",
                originalJobId = 2020,
                userId = 0
            )
        )
    }
}