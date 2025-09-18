// ProfileSetupScreen.kt
package com.example.madassignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.madassignment.components.SkillsSelectionDialog
import com.example.madassignment.data.JobViewModel
import com.example.madassignment.data.UserProfile
import com.example.madassignment.data.skillsToString
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    navController: NavController,
    jobViewModel: JobViewModel,
    onSetupComplete: () -> Unit
) {
    // CHANGE: Set default empty values instead of pre-filled ones

    val registeredName by jobViewModel.registeredName.collectAsState()
    var name by remember { mutableStateOf(registeredName ?: "") }
    var age by remember { mutableStateOf("") }
    var aboutMe by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var showSkillsDialog by remember { mutableStateOf(false) }
    var showQuitDialog by remember { mutableStateOf(false) } // Added quit dialog state

    val isNameValid = remember(name) { isValidName(name) || name.isEmpty() }
    val isAgeValid = remember(age) { isValidAge(age) || age.isEmpty() }

    val currentUser by jobViewModel.currentUser.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setup Your Profile") },


                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
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
                singleLine = true,
                isError = !isNameValid && name.isNotBlank(),
                supportingText = {
                    if (!isNameValid && name.isNotBlank()) {
                        Text(
                            text = "Please enter a valid name (letters only, min 2 characters)",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
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
                singleLine = true,
                isError = !isAgeValid && age.isNotBlank(),
                supportingText = {
                    if (!isAgeValid && age.isNotBlank()) {
                        Text(
                            text = "Please enter a valid age (1-150)",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            // Company Input
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
                    if (isValidName(name) && isValidAge(age)) {
                        currentUser?.let { user ->
                            val profile = UserProfile(
                                userId = user.id,
                                name = name,
                                age = age,
                                aboutMe = aboutMe,
                                skills = skills,
                                company = company
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
                            snackbarHostState.showSnackbar("Please fix validation errors before continuing")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = name.isNotBlank() && age.isNotBlank() && isValidName(name) && isValidAge(age)
            ) {
                Text("Complete Setup", fontSize = 16.sp)
            }

            // Skip Button for optional setup
            TextButton(
                onClick = {
                    // Allow users to skip profile setup
                    onSetupComplete()
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "Skip for now",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                jobViewModel.clearRegisteredName()
            }
        }
    }

    // Quit Confirmation Dialog
    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = {
                Text("Quit Setup?")
            },
            text = {
                Text("Are you sure you want to quit profile setup? You will be logged out.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showQuitDialog = false
                        jobViewModel.logout()
                        // Navigate back to welcome screen, clearing the back stack
                        navController.navigate("welcome") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Quit")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showQuitDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}