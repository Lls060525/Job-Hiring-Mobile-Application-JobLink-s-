package com.example.madassignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madassignment.data.EmployerJobPost

@Composable
fun CreateJobPostDialog(
    employerId: Int,
    onDismiss: () -> Unit,
    onCreateJobPost: (EmployerJobPost) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var salaryRange by remember { mutableStateOf("") }
    var jobType by remember { mutableStateOf("Full-time") }
    var category by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Job Post", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Job Title *") },
                    placeholder = { Text("e.g., Senior Android Developer") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Job Description *") },
                    placeholder = { Text("Describe the role and responsibilities...") },
                    maxLines = 3
                )

                OutlinedTextField(
                    value = requirements,
                    onValueChange = { requirements = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Requirements *") },
                    placeholder = { Text("List the required skills and qualifications...") },
                    maxLines = 3
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Location *") },
                    placeholder = { Text("e.g., Kuala Lumpur, Malaysia") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = "Location")
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = salaryRange,
                    onValueChange = { salaryRange = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Salary Range *") },
                    placeholder = { Text("e.g., RM 5,000 - RM 8,000") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = jobType,
                    onValueChange = { jobType = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Job Type *") },
                    placeholder = { Text("e.g., Full-time, Part-time, Contract") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Category *") },
                    placeholder = { Text("e.g., Technology, Marketing, Finance") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
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
                enabled = title.isNotBlank() && description.isNotBlank() && requirements.isNotBlank() &&
                        location.isNotBlank() && salaryRange.isNotBlank() && jobType.isNotBlank() && category.isNotBlank()
            ) {
                Text("Create Job Post")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}