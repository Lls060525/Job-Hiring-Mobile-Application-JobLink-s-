package com.example.madassignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madassignment.components.JobCard
import com.example.madassignment.components.PurpleTopAppBar
import com.example.madassignment.components.UnsaveJobDialog
import com.example.madassignment.components.UnapplyJobDialog
import com.example.madassignment.data.Job
import com.example.madassignment.data.JobViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState

@Composable
fun MyActivityScreen(jobViewModel: JobViewModel) {
    // Collect StateFlow as state with initial values
    val savedJobs by jobViewModel.savedJobs.collectAsState(initial = emptyList())
    val appliedJobs by jobViewModel.appliedJobs.collectAsState(initial = emptyList())

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showUnsaveDialog by remember { mutableStateOf(false) }
    var showUnapplyDialog by remember { mutableStateOf(false) }
    var selectedJob by remember { mutableStateOf<Job?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Log for debugging
    LaunchedEffect(savedJobs, appliedJobs) {
        println("DEBUG MyActivity: Saved jobs count: ${savedJobs.size}")
        println("DEBUG MyActivity: Applied jobs count: ${appliedJobs.size}")
        savedJobs.forEach { job ->
            println("DEBUG MyActivity: Saved job - ID: ${job.id}, OriginalID: ${job.originalJobId}, Title: ${job.title}")
        }
        appliedJobs.forEach { job ->
            println("DEBUG MyActivity: Applied job - ID: ${job.id}, OriginalID: ${job.originalJobId}, Title: ${job.title}")
        }
    }

    Scaffold(
        topBar = {
            PurpleTopAppBar(title = "My Activity")
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "My Activity",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            item {
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 }
                    ) {
                        Text("Saved (${savedJobs.size})", modifier = Modifier.padding(16.dp))
                    }
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 }
                    ) {
                        Text("Applied (${appliedJobs.size})", modifier = Modifier.padding(16.dp))
                    }
                }
            }

            // Show saved jobs
            if (selectedTabIndex == 0) {
                if (savedJobs.isEmpty()) {
                    item {
                        Text(
                            text = "No saved jobs yet",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(savedJobs) { job ->
                        JobCard(
                            job = job,
                            jobViewModel = jobViewModel,
                            onClick = {
                                // You can show job details here if needed
                            },
                            onUnsave = {
                                selectedJob = job
                                showUnsaveDialog = true
                            },
                            onUnapply = null // Saved jobs might not be applied
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Show applied jobs
            if (selectedTabIndex == 1) {
                if (appliedJobs.isEmpty()) {
                    item {
                        Text(
                            text = "No applied jobs yet",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(appliedJobs) { job ->
                        JobCard(
                            job = job,
                            jobViewModel = jobViewModel,
                            onClick = {
                                // You can show job details here if needed
                            },
                            onUnsave = null, // Applied jobs might not be saved
                            onUnapply = {
                                selectedJob = job
                                showUnapplyDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // Unsave Job Dialog
    if (showUnsaveDialog && selectedJob != null) {
        UnsaveJobDialog(
            job = selectedJob!!,
            onDismiss = {
                showUnsaveDialog = false
                selectedJob = null
            },
            onUnsave = {
                jobViewModel.removeSavedJob(selectedJob!!)
                scope.launch {
                    snackbarHostState.showSnackbar("Job removed from saved list")
                }
                showUnsaveDialog = false
                selectedJob = null
            }
        )
    }

    // Unapply Job Dialog
    if (showUnapplyDialog && selectedJob != null) {
        UnapplyJobDialog(
            job = selectedJob!!,
            onDismiss = {
                showUnapplyDialog = false
                selectedJob = null
            },
            onUnapply = {
                jobViewModel.removeAppliedJob(selectedJob!!)
                scope.launch {
                    snackbarHostState.showSnackbar("Job application cancelled")
                }
                showUnapplyDialog = false
                selectedJob = null
            }
        )
    }
}