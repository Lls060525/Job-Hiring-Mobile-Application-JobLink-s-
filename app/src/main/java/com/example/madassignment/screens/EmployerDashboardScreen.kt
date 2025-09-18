package com.example.madassignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.madassignment.components.EmployerBottomNavigationBar
import com.example.madassignment.components.PurpleTopAppBar
import com.example.madassignment.data.EmployerJobPost
import com.example.madassignment.data.EmployerViewModel
import kotlinx.coroutines.launch

@Composable
fun EmployerDashboardScreen(
    navController: NavController,
    employerViewModel: EmployerViewModel
) {
    val employerJobPosts by employerViewModel.employerJobPosts.collectAsState()
    val currentEmployer by employerViewModel.currentEmployer.collectAsState()
    var showCreateJobDialog by remember { mutableStateOf(false) }
    var showEditJobDialog by remember { mutableStateOf(false) }
    var selectedJobPost by remember { mutableStateOf<EmployerJobPost?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentEmployer) {
        if (currentEmployer != null) {
            employerViewModel.loadEmployerJobPosts()
        }
    }

    Scaffold(
        topBar = {
            PurpleTopAppBar(title = "Employer Dashboard")
        },
        bottomBar = {
            EmployerBottomNavigationBar(navController = navController)
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateJobDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Job Post")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "My Job Posts",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (employerJobPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No job posts yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Click the + button to create your first job post",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(employerJobPosts) { jobPost ->
                        EmployerJobPostCard(
                            jobPost = jobPost,
                            onEditClick = {
                                selectedJobPost = jobPost
                                showEditJobDialog = true
                            },
                            onViewApplicantsClick = {
                                navController.navigate("employer_applications")

                            },
                            onDeleteClick = {
                                employerViewModel.deleteJobPost(jobPost.id)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Job post deleted successfully")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Create Job Dialog
    if (showCreateJobDialog) {
        val employer = currentEmployer
        if (employer != null) {
            CreateJobPostDialog(
                employerId = employer.id,
                onDismiss = { showCreateJobDialog = false },
                onCreateJobPost = { jobPost ->
                    employerViewModel.createJobPost(jobPost)
                    scope.launch {
                        snackbarHostState.showSnackbar("Job post created successfully!")
                    }
                }
            )
        }
    }

    // Edit Job Dialog
    if (showEditJobDialog && selectedJobPost != null) {
        val employer = currentEmployer
        if (employer != null) {
            EditJobPostDialog(
                jobPost = selectedJobPost!!,
                onDismiss = {
                    showEditJobDialog = false
                    selectedJobPost = null
                },
                onUpdateJobPost = { updatedJobPost ->
                    employerViewModel.updateJobPost(updatedJobPost)
                    scope.launch {
                        snackbarHostState.showSnackbar("Job post updated successfully!")
                    }
                }
            )
        }
    }
}

@Composable
fun EmployerJobPostCard(
    jobPost: EmployerJobPost,
    onEditClick: () -> Unit,
    onViewApplicantsClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val applicantCount = if (jobPost.applicants.isBlank()) 0 else jobPost.applicants.split(",").size

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Job Title and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = jobPost.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = jobPost.category,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Active Status Indicator
                if (jobPost.isActive) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Active",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Job Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    JobDetailItem("Location", jobPost.location)
                    JobDetailItem("Salary", jobPost.salaryRange)
                    JobDetailItem("Type", jobPost.jobType)
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$applicantCount",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (applicantCount == 1) "Applicant" else "Applicants",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // In your EmployerDashboardScreen.kt, update the EmployerJobPostCard's action buttons section:
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // View Applicants Button - Primary Action
                FilledTonalButton(
                    onClick = onViewApplicantsClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "View Applicants",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View ($applicantCount)", fontSize = 14.sp)
                }

                // Edit Button - Secondary Action
                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier.weight(0.7f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Delete Button - Destructive Action
                OutlinedButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(0.7f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}

@Composable
private fun JobDetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 4.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Add this Edit Dialog component
@Composable
fun EditJobPostDialog(
    jobPost: EmployerJobPost,
    onDismiss: () -> Unit,
    onUpdateJobPost: (EmployerJobPost) -> Unit
) {
    var title by remember { mutableStateOf(jobPost.title) }
    var description by remember { mutableStateOf(jobPost.description) }
    var requirements by remember { mutableStateOf(jobPost.requirements) }
    var location by remember { mutableStateOf(jobPost.location) }
    var salaryRange by remember { mutableStateOf(jobPost.salaryRange) }
    var jobType by remember { mutableStateOf(jobPost.jobType) }
    var category by remember { mutableStateOf(jobPost.category) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Job Post", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Job Title *") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Job Description *") },
                    maxLines = 3
                )

                OutlinedTextField(
                    value = requirements,
                    onValueChange = { requirements = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Requirements *") },
                    maxLines = 3
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Location *") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = salaryRange,
                    onValueChange = { salaryRange = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Salary Range *") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = jobType,
                    onValueChange = { jobType = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Job Type *") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Category *") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank() && requirements.isNotBlank() &&
                        location.isNotBlank() && salaryRange.isNotBlank() && jobType.isNotBlank() && category.isNotBlank()) {

                        val updatedJobPost = jobPost.copy(
                            title = title,
                            description = description,
                            requirements = requirements,
                            location = location,
                            salaryRange = salaryRange,
                            jobType = jobType,
                            category = category
                        )
                        onUpdateJobPost(updatedJobPost)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank() && description.isNotBlank() && requirements.isNotBlank() &&
                        location.isNotBlank() && salaryRange.isNotBlank() && jobType.isNotBlank() && category.isNotBlank()
            ) {
                Text("Update Job Post")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}