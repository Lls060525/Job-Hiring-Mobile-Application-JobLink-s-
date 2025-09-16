package com.example.madassignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.madassignment.data.JobViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminJobListingsScreen(
    jobViewModel: JobViewModel,
    navController: NavController
) {
    val allJobs = remember { jobViewModel.allJobs }
    val appliedJobs = remember { jobViewModel.appliedJobs }
    val savedJobs = remember { jobViewModel.savedJobs }
    val showAddDialog by jobViewModel.showAddJobDialog.collectAsState()
    var showMigrationDialog by remember { mutableStateOf(false) } // Add this line

    LaunchedEffect(Unit) {

        jobViewModel.loadAllJobsFromDatabase()
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Listings Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Migration button
                    IconButton(onClick = { showMigrationDialog = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Migrate to Firestore")
                    }
                    // Add job button
                    IconButton(onClick = { jobViewModel.setShowAddJobDialog(true) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Job")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Migration FAB
                ExtendedFloatingActionButton(
                    onClick = { showMigrationDialog = true },
                    icon = { Icon(Icons.Default.Refresh, contentDescription = "Migrate") },
                    text = { Text("Migrate Jobs") }
                )
                // Add Job FAB
                ExtendedFloatingActionButton(
                    onClick = { jobViewModel.setShowAddJobDialog(true) },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Job") },
                    text = { Text("Add Job") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Statistics Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    title = "Total Jobs",
                    value = allJobs.size.toString(),
                    icon = Icons.Default.MailOutline
                )
                StatCard(
                    title = "Applied",
                    value = appliedJobs.size.toString(),
                    icon = Icons.Default.Send
                )
                StatCard(
                    title = "Saved",
                    value = savedJobs.size.toString(),
                    icon = Icons.Default.Favorite
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "All Job Listings (${allJobs.size})",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                items(allJobs) { job ->
                    AdminJobListItem(
                        job = job,
                        onDelete = { jobViewModel.deleteJob(job) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Add Job Dialog
    if (showAddDialog) {
        AddJobDialog(jobViewModel = jobViewModel)
    }

    // Migration Confirmation Dialog
    if (showMigrationDialog) {
        AlertDialog(
            onDismissRequest = { showMigrationDialog = false },
            title = { Text("Migrate Jobs to Firestore") },
            text = { Text("This will save all existing jobs to Firestore. This action should only be done once. Continue?") },
            confirmButton = {
                Button(
                    onClick = {
                        // You'll need to implement this in JobViewModel
                        jobViewModel.migrateJobsToFirestore()
                        showMigrationDialog = false
                    }
                ) {
                    Text("Migrate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMigrationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AddJobDialog(jobViewModel: JobViewModel) {
    AlertDialog(
        onDismissRequest = { jobViewModel.setShowAddJobDialog(false) },
        title = { Text("Add New Job") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = jobViewModel.newJobTitle,
                    onValueChange = { jobViewModel.newJobTitle = it },
                    label = { Text("Job Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = jobViewModel.newJobCompany,
                    onValueChange = { jobViewModel.newJobCompany = it },
                    label = { Text("Company") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = jobViewModel.newJobLocation,
                    onValueChange = { jobViewModel.newJobLocation = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = jobViewModel.newJobType,
                    onValueChange = { jobViewModel.newJobType = it },
                    label = { Text("Job Type") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = jobViewModel.newJobSalary,
                    onValueChange = { jobViewModel.newJobSalary = it },
                    label = { Text("Salary") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = jobViewModel.newJobCategory,
                    onValueChange = { jobViewModel.newJobCategory = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = jobViewModel.newJobSkills,
                    onValueChange = { jobViewModel.newJobSkills = it },
                    label = { Text("Required Skills") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { jobViewModel.addNewJob() },
                enabled = jobViewModel.newJobTitle.isNotBlank() && jobViewModel.newJobCompany.isNotBlank()
            ) {
                Text("Add Job")
            }
        },
        dismissButton = {
            TextButton(onClick = { jobViewModel.setShowAddJobDialog(false) }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminJobListItem(job: com.example.madassignment.data.Job, onDelete: () -> Unit = {}) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = job.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = job.company,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "${job.type} • ${job.location}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = job.salary,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "Category: ${job.category}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (job.requiredSkills.isNotBlank()) {
                Text(
                    text = "Skills: ${job.requiredSkills.take(60)}${if (job.requiredSkills.length > 60) "..." else ""}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Delete")
            }

        }
    }
}





