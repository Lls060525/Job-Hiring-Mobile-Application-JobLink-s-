package com.example.madassignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.madassignment.components.PurpleTopAppBar
import com.example.madassignment.components.SkillsSelectionDialog
import com.example.madassignment.data.JobViewModel
import com.example.madassignment.data.UserProfile
import com.example.madassignment.data.skillsToString
import kotlinx.coroutines.launch

@Composable
fun ProfileSetupScreen(
    navController: NavController,
    jobViewModel: JobViewModel,
    onSetupComplete: () -> Unit
) {
    // CHANGE: Set default empty values instead of pre-filled ones
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var aboutMe by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var showSkillsDialog by remember { mutableStateOf(false) }

    val currentUser by jobViewModel.currentUser.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            PurpleTopAppBar(title = "Setup Your Profile")
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Complete Your Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Help us recommend the best jobs for you based on your skills and experience",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                label = { Text("Full Name *") },
                placeholder = { Text("Enter your full name") },
                singleLine = true
            )

            // Age Input
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                label = { Text("Age *") },
                placeholder = { Text("Enter your age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Company Input (ADD THIS)
            OutlinedTextField(
                value = company,
                onValueChange = { company = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                label = { Text("Company") },
                placeholder = { Text("Enter your company name (optional)") },
                singleLine = true
            )

            // About Me Input
            OutlinedTextField(
                value = aboutMe,
                onValueChange = { aboutMe = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                label = { Text("About Me") },
                placeholder = { Text("Tell us about yourself...") },
                maxLines = 3
            )

            // Skills Input
            OutlinedTextField(
                value = skills,
                onValueChange = { skills = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                label = { Text("Skills (Comma separated)") },
                placeholder = { Text("e.g., Python, Java, React, Marketing") },
                maxLines = 3
            )

            Button(
                onClick = { showSkillsDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Select Skills")
            }

            if (showSkillsDialog) {
                SkillsSelectionDialog(
                    currentSkills = emptyList(),
                    onDismiss = { showSkillsDialog = false },
                    onSkillsSelected = { newSkills ->
                        skills = skillsToString(newSkills)
                    }
                )
            }

            // Setup Button
            Button(
                onClick = {
                    if (name.isNotBlank() && age.isNotBlank()) {
                        currentUser?.let { user ->
                            val profile = UserProfile(
                                userId = user.id,
                                name = name,
                                age = age,
                                aboutMe = aboutMe,
                                skills = skills,
                                company = company // ADD THIS
                            )
                            scope.launch {
                                jobViewModel.updateUserProfile(profile).onSuccess {
                                    snackbarHostState.showSnackbar("Profile setup complete!")
                                    onSetupComplete()
                                }.onFailure {
                                    snackbarHostState.showSnackbar("Failed to save profile: ${it.message}")
                                }
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please fill in required fields (*)")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = name.isNotBlank() && age.isNotBlank()
            ) {
                Text("Complete Setup", fontSize = 16.sp)
            }
        }
    }
}