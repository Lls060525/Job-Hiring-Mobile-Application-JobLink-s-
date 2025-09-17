package com.example.madassignment.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.madassignment.data.EmployerJobPost

@Composable
fun CreateJobPostDialog(
    onDismiss: () -> Unit,
    onCreateJobPost: (EmployerJobPost) -> Unit,
    employerId: Int
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var salaryRange by remember { mutableStateOf("") }
    var jobType by remember { mutableStateOf("Full-time") }
    var category by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create Job Post",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Form fields
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Job Title") },
                    placeholder = { Text("e.g., Senior Android Developer") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Job Description") },
                    placeholder = { Text("Describe the role and responsibilities...") },
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = requirements,
                    onValueChange = { requirements = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Requirements") },
                    placeholder = { Text("List the required skills and qualifications...") },
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Location") },
                    placeholder = { Text("e.g., Kuala Lumpur, Malaysia") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = salaryRange,
                    onValueChange = { salaryRange = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Salary Range") },
                    placeholder = { Text("e.g., RM 5,000 - RM 8,000") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = jobType,
                    onValueChange = { jobType = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Job Type") },
                    placeholder = { Text("e.g., Full-time, Part-time, Contract") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Category") },
                    placeholder = { Text("e.g., Technology, Marketing, Finance") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Create Button
                Button(
                    onClick = {
                        if (title.isNotBlank() && description.isNotBlank() && requirements.isNotBlank() &&
                            location.isNotBlank() && salaryRange.isNotBlank() && jobType.isNotBlank() && category.isNotBlank()) {

                            val jobPost = EmployerJobPost(
                                employerId = employerId,
                                title = title,
                                description = description,
                                requirements = requirements,
                                location = location,
                                salaryRange = salaryRange,
                                jobType = jobType,
                                category = category
                            )
                            onCreateJobPost(jobPost)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotBlank() && description.isNotBlank() && requirements.isNotBlank() &&
                            location.isNotBlank() && salaryRange.isNotBlank() && jobType.isNotBlank() && category.isNotBlank()
                ) {
                    Text("Create Job Post")
                }
            }
        }
    }
}
@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onPostCreated: (String) -> Unit,
    userName: String,
    userCompany: String
) {
    var content by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create Post",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User info
                Text(
                    text = "Posted by: $userName from $userCompany",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Content field
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    label = { Text("What's on your mind?") },
                    placeholder = { Text("Share your thoughts with the community...") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Post Button
                Button(
                    onClick = {
                        if (content.isNotBlank()) {
                            onPostCreated(content)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = content.isNotBlank()
                ) {
                    Text("Post to Community")
                }
            }
        }
    }
}