package com.example.madassignment.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.madassignment.data.Job
import com.example.madassignment.data.JobViewModel

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
    onSave: () -> Unit,
    isSaved: Boolean = false,
    isApplied: Boolean = false
) {
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
                Text(
                    text = job.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

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
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Save Button
                    Button(
                        onClick = {
                            if (isSaved) {
                                jobViewModel.removeSavedJob(job)  // Handle unsave
                            } else {
                                jobViewModel.saveJob(job)
                            }
                            onSave()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (isSaved) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = if (isSaved) "Unsave" else "Save",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSaved) "Unsave" else "Save")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Apply Button
                    Button(
                        onClick = {
                            if (!isApplied) {
                                jobViewModel.applyToJob(job)
                                onApply()
                            }
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isApplied
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = if (isApplied) "Applied" else "Apply",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isApplied) "Applied" else "Apply")
                    }
                }
            }
        }
    }
}