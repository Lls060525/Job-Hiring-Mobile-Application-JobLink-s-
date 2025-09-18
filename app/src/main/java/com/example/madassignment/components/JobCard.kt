package com.example.madassignment.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madassignment.data.Job
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.input.pointer.pointerInput
import com.example.madassignment.data.JobViewModel
import androidx.compose.runtime.remember
import android.util.Log

@Composable
fun JobCard(
    job: Job,
    jobViewModel: JobViewModel,
    onClick: () -> Unit,
    onUnsave: (() -> Unit)? = null,
    onUnapply: (() -> Unit)? = null
) {
    // FIXED: Get job states directly from StateFlow with proper key matching
    val jobStates = jobViewModel.jobStates.collectAsState().value
    val savedJobs = jobViewModel.savedJobs.collectAsState().value
    val appliedJobs = jobViewModel.appliedJobs.collectAsState().value

    // FIXED: Multiple ways to determine if job is saved/applied for better reliability
    val isSaved = remember(jobStates, savedJobs, job.originalJobId, job.id) {
        // Method 1: Check job states
        val stateResult = jobStates[job.originalJobId]?.first ?: false
        // Method 2: Check saved jobs list
        val listResult = savedJobs.any { savedJob ->
            savedJob.originalJobId == job.originalJobId ||
                    savedJob.id == job.id ||
                    (job.originalJobId > 10000 && savedJob.originalJobId == job.originalJobId)
        }

        val result = stateResult || listResult
        Log.d("JobCard", "Job ${job.title} (ID: ${job.originalJobId}) - isSaved: $result (state: $stateResult, list: $listResult)")
        result
    }

    val isApplied = remember(jobStates, appliedJobs, job.originalJobId, job.id) {
        // Method 1: Check job states
        val stateResult = jobStates[job.originalJobId]?.second ?: false
        // Method 2: Check applied jobs list
        val listResult = appliedJobs.any { appliedJob ->
            appliedJob.originalJobId == job.originalJobId ||
                    appliedJob.id == job.id ||
                    (job.originalJobId > 10000 && appliedJob.originalJobId == job.originalJobId)
        }

        val result = stateResult || listResult
        Log.d("JobCard", "Job ${job.title} (ID: ${job.originalJobId}) - isApplied: $result (state: $stateResult, list: $listResult)")
        result
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                        if (isSaved && onUnsave != null) {
                            onUnsave()
                        } else if (isApplied && onUnapply != null) {
                            onUnapply()
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with status icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (job.company.isNotBlank()) {
                        Text(
                            text = job.company,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // FIXED: Status icons with better visibility
                Row {
                    if (isApplied) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Applied",
                            tint = Color.Green,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (isSaved) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Saved",
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            if (job.subtitle.isNotEmpty()) {
                Text(
                    text = job.subtitle,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Text(
                text = job.type,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = job.location,
                fontSize = 14.sp,
            )

            Text(
                text = job.salary,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}