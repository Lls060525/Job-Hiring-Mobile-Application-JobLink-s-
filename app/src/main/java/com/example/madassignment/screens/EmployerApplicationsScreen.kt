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
import androidx.navigation.NavController
import com.example.madassignment.components.EmployerBottomNavigationBar
import com.example.madassignment.components.PurpleTopAppBar
import com.example.madassignment.data.EmployerViewModel
import kotlinx.coroutines.launch

@Composable
fun EmployerApplicationsScreen(
    navController: NavController,
    employerViewModel: EmployerViewModel
) {
    val employerJobPosts by employerViewModel.employerJobPosts.collectAsState()
    val applicantDetails by employerViewModel.applicantDetails.collectAsState()
    var selectedJobPost by remember { mutableStateOf<com.example.madassignment.data.EmployerJobPost?>(null) }
    var showApplicantsDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(employerViewModel) {
        employerViewModel.loadEmployerJobPosts()
    }

    Scaffold(
        topBar = {
            PurpleTopAppBar(title = "Job Applications")
        },
        bottomBar = {
            EmployerBottomNavigationBar(navController = navController)
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Your Job Posts",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (employerJobPosts.isEmpty()) {
                Text(
                    text = "You haven't posted any jobs yet.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                items(employerJobPosts) { jobPost ->
                    EmployerJobApplicationCard(
                        jobPost = jobPost,
                        onViewApplicantsClick = {
                            selectedJobPost = jobPost
                            employerViewModel.loadApplicantDetails(jobPost)
                            showApplicantsDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showApplicantsDialog && selectedJobPost != null) {
        ViewApplicantsDialog(
            jobPost = selectedJobPost!!,
            applicantDetails = applicantDetails,
            onDismiss = {
                showApplicantsDialog = false
                selectedJobPost = null
                employerViewModel.clearApplicantDetails()
            }
        )
    }
}

@Composable
fun EmployerJobApplicationCard(
    jobPost: com.example.madassignment.data.EmployerJobPost,
    onViewApplicantsClick: () -> Unit
) {
    val applicantCount = if (jobPost.applicants.isBlank()) 0 else jobPost.applicants.split(",").size

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = jobPost.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = jobPost.location,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = jobPost.salaryRange,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "$applicantCount applicant${if (applicantCount != 1) "s" else ""}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Smoother button with improved styling
            FilledTonalButton(
                onClick = onViewApplicantsClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = applicantCount > 0,
                shape = MaterialTheme.shapes.small
            ) {
                Text(if (applicantCount > 0) "View Applicants" else "No Applicants Yet")
            }
        }
    }
}

@Composable
fun ViewApplicantsDialog(
    jobPost: com.example.madassignment.data.EmployerJobPost,
    applicantDetails: List<EmployerViewModel.ApplicantDetail>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = {
            Text("Applicants for ${jobPost.title}", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                if (applicantDetails.isEmpty()) {
                    Text("No applicants yet for this job post.")
                } else {
                    Text(
                        text = "${applicantDetails.size} applicant${if (applicantDetails.size != 1) "s" else ""} found:",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn {
                        items(applicantDetails) { applicant ->
                            ApplicantDetailCard(applicant = applicant)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ApplicantDetailCard(applicant: EmployerViewModel.ApplicantDetail) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = applicant.user.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = applicant.user.email,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            // Add more applicant details as needed
        }
    }
}