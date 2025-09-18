package com.example.madassignment.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.madassignment.data.Job
import com.example.madassignment.data.JobViewModel
import androidx.compose.runtime.remember
import android.util.Log

@Composable
fun JobDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun JobDialog(
    job: Job,
    jobViewModel: JobViewModel,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    onSave: () -> Unit
) {
    // FIXED: Get real-time states from multiple sources for reliability
    val jobStates = jobViewModel.jobStates.collectAsState().value
    val savedJobs = jobViewModel.savedJobs.collectAsState().value
    val appliedJobs = jobViewModel.appliedJobs.collectAsState().value

    // FIXED: Enhanced state checking with multiple methods
    val isSaved = remember(jobStates, savedJobs, job.originalJobId, job.id) {
        // Method 1: Check job states map
        val stateResult = jobStates[job.originalJobId]?.first ?: false
        // Method 2: Check saved jobs list
        val listResult = savedJobs.any { savedJob ->
            savedJob.originalJobId == job.originalJobId ||
                    savedJob.id == job.id ||
                    (job.originalJobId > 10000 && savedJob.originalJobId == job.originalJobId)
        }

        val result = stateResult || listResult
        Log.d("JobDialog", "Job ${job.title} (ID: ${job.originalJobId}) - isSaved: $result")
        result
    }

    val isApplied = remember(jobStates, appliedJobs, job.originalJobId, job.id) {
        // Method 1: Check job states map
        val stateResult = jobStates[job.originalJobId]?.second ?: false
        // Method 2: Check applied jobs list
        val listResult = appliedJobs.any { appliedJob ->
            appliedJob.originalJobId == job.originalJobId ||
                    appliedJob.id == job.id ||
                    (job.originalJobId > 10000 && appliedJob.originalJobId == job.originalJobId)
        }

        val result = stateResult || listResult
        Log.d("JobDialog", "Job ${job.title} (ID: ${job.originalJobId}) - isApplied: $result")
        result
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Job Title and Company
                Text(
                    text = job.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                if (job.company.isNotBlank()) {
                    Text(
                        text = job.company,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (job.subtitle.isNotEmpty()) {
                    Text(
                        text = job.subtitle,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Job Details
                Column(
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    JobDetailRow("Location", job.location)
                    JobDetailRow("Type", job.type)
                    JobDetailRow("Category", job.category)
                    JobDetailRow("Salary", job.salary)

                    if (job.requiredSkills.isNotBlank()) {
                        JobDetailRow("Skills", job.requiredSkills)
                    }
                }

                // Action Buttons - FIXED with better state handling
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Save Button - FIXED with proper icon and immediate feedback
                    Button(
                        onClick = {
                            Log.d("JobDialog", "Save button clicked. Current isSaved: $isSaved")
                            if (isSaved) {
                                jobViewModel.removeSavedJob(job)
                            } else {
                                jobViewModel.saveJob(job)
                            }
                            onSave()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSaved) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (isSaved) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isSaved) "Unsave" else "Save",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isSaved) "Unsave" else "Save")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Apply Button - FIXED with better state management
                    Button(
                        onClick = {
                            Log.d("JobDialog", "Apply button clicked. Current isApplied: $isApplied")
                            if (!isApplied) {
                                jobViewModel.applyToJob(job)
                                onApply()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isApplied,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isApplied) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                            contentColor = if (isApplied) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = if (isApplied) "Applied" else "Apply",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isApplied) "Applied" else "Apply")
                    }
                }

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Close")
                }
            }
        }
    }
}