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

@Composable
fun HomeScreen(jobViewModel: JobViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedJob by remember { mutableStateOf<Job?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val userProfile by jobViewModel.userProfile.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val currentUser by jobViewModel.currentUser.collectAsState()

    val allJobs = remember { jobViewModel.getAllAvailableJobs() }
    val savedJobs by remember { derivedStateOf { jobViewModel.savedJobs } }
    val appliedJobs by remember { derivedStateOf { jobViewModel.appliedJobs } }

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


    val filteredJobs = remember(searchQuery, selectedFilter, allJobs, savedJobs, appliedJobs) {
        var result = if (searchQuery.isBlank()) {
            allJobs
        } else {
            allJobs.filter { job ->
                job.title.contains(searchQuery, ignoreCase = true) ||
                        job.subtitle.contains(searchQuery, ignoreCase = true) ||
                        job.type.contains(searchQuery, ignoreCase = true) ||
                        job.location.contains(searchQuery, ignoreCase = true)
            }
        }

        if (selectedFilter == "New") {
            result = result.filter { job -> jobViewModel.sampleNewJobs.any { it.id == job.id } }
        }

        result
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

            // Filter Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
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
            }

            // Title
            Text(
                text = "Start your job search",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            if (searchQuery.isBlank()) {
                Text(
                    text = if (selectedFilter == "New") "New Jobs" else "Recommended Jobs",
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
            if (filteredJobs.isEmpty() && searchQuery.isNotBlank()) {
                Text(
                    text = "No jobs found for \"$searchQuery\"",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(vertical = 32.dp)
                        .fillMaxWidth()
                )
            }

            // Job Listings
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredJobs) { job ->
                    val isSaved = savedJobs.any { it.originalJobId == job.originalJobId }
                    val isApplied = appliedJobs.any { it.originalJobId == job.originalJobId }

                    JobCard(
                        job = job,
                        isSaved = isSaved,
                        isApplied = isApplied,
                        onClick = {
                            selectedJob = job
                            showDialog = true
                        },
                        onUnsave = {
                            if (isSaved) {
                                jobViewModel.removeSavedJob(job)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Job removed from saved list")
                                }
                            }
                        },
                        onUnapply = {
                            if (isApplied) {
                                jobViewModel.removeAppliedJob(job)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Job application cancelled")
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Show Job Dialog
    if (showDialog && selectedJob != null) {
        val job = selectedJob!!
        val isSaved = savedJobs.any { it.originalJobId == job.id }
        val isApplied = appliedJobs.any { it.originalJobId == job.id }

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
                scope.launch {
                    if (isSaved) {
                        snackbarHostState.showSnackbar("Job removed from saved!")
                    } else {
                        snackbarHostState.showSnackbar("Job saved successfully!")
                    }
                }
            },
            isSaved = isSaved,
            isApplied = isApplied
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