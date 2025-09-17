package com.example.madassignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madassignment.components.JobCard
import com.example.madassignment.components.JobDialog
import com.example.madassignment.components.PurpleTopAppBar
import com.example.madassignment.data.Job
import com.example.madassignment.data.JobViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import android.util.Log

@Composable
fun HomeScreen(jobViewModel: JobViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedJob by remember { mutableStateOf<Job?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    // FIXED: Collect states properly with initial values
    val userProfile by jobViewModel.userProfile.collectAsState()
    val currentUser by jobViewModel.currentUser.collectAsState()
    val allJobsState by jobViewModel.allAvailableJobs.collectAsState()
    val savedJobs by jobViewModel.savedJobs.collectAsState()
    val appliedJobs by jobViewModel.appliedJobs.collectAsState()
    val jobStates by jobViewModel.jobStates.collectAsState() // ADDED: Direct access to job states

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // FIXED: Better data refresh management
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            Log.d("HomeScreen", "User logged in, refreshing data")
            jobViewModel.refreshAllJobs()
            jobViewModel.refreshUserData()
        }
    }

    // Get skill-based recommendations
    var skillBasedJobs by remember { mutableStateOf<List<Job>>(emptyList()) }

    LaunchedEffect(userProfile?.skills) {
        if (!userProfile?.skills.isNullOrBlank()) {
            skillBasedJobs = jobViewModel.getSkillBasedJobs(userProfile?.skills ?: "")
        }
    }

    // Use skill-based jobs if available, otherwise use default
    val recommendedJobs = if (skillBasedJobs.isNotEmpty()) {
        skillBasedJobs
    } else {
        jobViewModel.sampleRecommendedJobs
    }

    // FIXED: Improved job filtering logic with better state checking
    val filteredJobs = remember(searchQuery, selectedFilter, allJobsState, savedJobs, appliedJobs, jobStates) {
        var result = if (searchQuery.isBlank()) {
            allJobsState
        } else {
            allJobsState.filter { job ->
                job.title.contains(searchQuery, ignoreCase = true) ||
                        job.subtitle.contains(searchQuery, ignoreCase = true) ||
                        job.type.contains(searchQuery, ignoreCase = true) ||
                        job.location.contains(searchQuery, ignoreCase = true) ||
                        job.company.contains(searchQuery, ignoreCase = true)
            }
        }

        when (selectedFilter) {
            "New" -> result.filter { job ->
                jobViewModel.sampleNewJobs.any { sampleJob -> sampleJob.id == job.id }
            }
            "Active" -> result.filter { job ->
                job.originalJobId > 10000 // Employer jobs
            }
            "Archived" -> result.filter { job ->
                job.originalJobId <= 10000 // Sample jobs
            }
            "Saved" -> result.filter { job ->
                // Check both job states and saved jobs list
                val stateResult = jobStates[job.originalJobId]?.first ?: false
                val listResult = savedJobs.any { savedJob ->
                    savedJob.originalJobId == job.originalJobId ||
                            savedJob.id == job.id
                }
                stateResult || listResult
            }
            "Applied" -> result.filter { job ->
                // Check both job states and applied jobs list
                val stateResult = jobStates[job.originalJobId]?.second ?: false
                val listResult = appliedJobs.any { appliedJob ->
                    appliedJob.originalJobId == job.originalJobId ||
                            appliedJob.id == job.id
                }
                stateResult || listResult
            }
            else -> result // "All"
        }
    }

    Scaffold(
        topBar = {
            PurpleTopAppBar(title = "Home Page")
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search jobs...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        // Handle search action
                    }
                )
            )

            // Filter Buttons with horizontal scroll
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.Start
            ) {
                FilterButton(
                    text = "All",
                    isSelected = selectedFilter == "All",
                    onClick = { selectedFilter = "All" }
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilterButton(
                    text = "New",
                    isSelected = selectedFilter == "New",
                    onClick = { selectedFilter = "New" }
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilterButton(
                    text = "Active",
                    isSelected = selectedFilter == "Active",
                    onClick = { selectedFilter = "Active" }
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilterButton(
                    text = "Archived",
                    isSelected = selectedFilter == "Archived",
                    onClick = { selectedFilter = "Archived" }
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilterButton(
                    text = "Saved",
                    isSelected = selectedFilter == "Saved",
                    onClick = { selectedFilter = "Saved" }
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilterButton(
                    text = "Applied",
                    isSelected = selectedFilter == "Applied",
                    onClick = { selectedFilter = "Applied" }
                )
            }

            // Title with improved counts
            Text(
                text = "Start your job search",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            if (searchQuery.isBlank()) {
                Text(
                    text = when (selectedFilter) {
                        "New" -> "New Jobs (${filteredJobs.size})"
                        "Active" -> "Active Jobs (${filteredJobs.size})"
                        "Archived" -> "Archived Jobs (${filteredJobs.size})"
                        "Saved" -> "Saved Jobs (${filteredJobs.size})"
                        "Applied" -> "Applied Jobs (${filteredJobs.size})"
                        else -> "All Available Jobs (${filteredJobs.size})"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            } else {
                Text(
                    text = "Search Results (${filteredJobs.size} jobs found)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // No results message
            if (filteredJobs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            searchQuery.isNotBlank() -> "No jobs found for \"$searchQuery\""
                            selectedFilter == "Saved" -> "You haven't saved any jobs yet"
                            selectedFilter == "Applied" -> "You haven't applied to any jobs yet"
                            else -> "No jobs available"
                        },
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }

            LazyColumn {
                items(filteredJobs) { job ->
                    JobCard(
                        job = job,
                        jobViewModel = jobViewModel,
                        onClick = {
                            selectedJob = job
                            showDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // FIXED: Show Job Dialog with better state handling
    if (showDialog && selectedJob != null) {
        val job = selectedJob!!

        JobDialog(
            job = job,
            jobViewModel = jobViewModel,
            onDismiss = {
                showDialog = false
                selectedJob = null
            },
            onApply = {
                scope.launch {
                    snackbarHostState.showSnackbar("Job application submitted!")
                }
            },
            onSave = {
                // State is automatically updated in JobViewModel
                val isSaved = jobStates[job.originalJobId]?.first ?: false
                scope.launch {
                    if (isSaved) {
                        snackbarHostState.showSnackbar("Job saved successfully!")
                    } else {
                        snackbarHostState.showSnackbar("Job removed from saved!")
                    }
                }
            }
        )
    }
}

@Composable
fun FilterButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = if (isSelected) Color(0xFF6A0DAD) else Color.LightGray,
        contentColor = if (isSelected) Color.White else Color.Black
    )

    Button(
        onClick = onClick,
        colors = buttonColors,
        modifier = Modifier.height(36.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}