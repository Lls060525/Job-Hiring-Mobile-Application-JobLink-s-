// EmployerProfileSetupScreen.kt
package com.example.madassignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madassignment.data.EmployerProfile
import com.example.madassignment.data.EmployerViewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployerProfileSetupScreen(
    navController: NavController,
    employerViewModel: EmployerViewModel,
    onSetupComplete: () -> Unit
) {
    var companyName by remember { mutableStateOf("") }
    var industry by remember { mutableStateOf("") }
    var companySize by remember { mutableStateOf("") }
    var aboutCompany by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var showQuitDialog by remember { mutableStateOf(false) } // Added quit dialog state

    val currentEmployer by employerViewModel.currentEmployer.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setup Your Company Profile") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // Show quit confirmation dialog instead of just going back
                            showQuitDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp, // Changed to exit icon
                            contentDescription = "Quit Setup",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Optional: Add a skip button on the right side
                    TextButton(
                        onClick = {
                            // Allow employers to skip profile setup
                            onSetupComplete()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text("Skip")
                    }
                },
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
                text = "Complete Your Company Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Tell job seekers about your company to attract the best talent",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Company Name Input
            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                label = { Text("Company Name *") },
                placeholder = { Text("Enter your company name") },
                leadingIcon = {
                    Text("🏢", modifier = Modifier.padding(start = 16.dp, end = 8.dp))
                },
                singleLine = true
            )

            // Industry Input
            OutlinedTextField(
                value = industry,
                onValueChange = { industry = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                label = { Text("Industry *") },
                placeholder = { Text("e.g., Technology, Finance, Healthcare") },
                singleLine = true
            )

            // Company Size Input
            OutlinedTextField(
                value = companySize,
                onValueChange = { companySize = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                label = { Text("Company Size *") },
                placeholder = { Text("e.g., 1-10, 11-50, 51-200, 201-500, 500+") },
                singleLine = true
            )

            // Location Input
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                label = { Text("Location *") },
                placeholder = { Text("e.g., Kuala Lumpur, Malaysia") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = "Location")
                },
                singleLine = true
            )

            // Website Input
            OutlinedTextField(
                value = website,
                onValueChange = { website = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                label = { Text("Website") },
                placeholder = { Text("https://yourcompany.com") },
                leadingIcon = {
                    Text("🌐", modifier = Modifier.padding(start = 16.dp, end = 8.dp))
                },
                singleLine = true
            )

            // Contact Email Input
            OutlinedTextField(
                value = contactEmail,
                onValueChange = { contactEmail = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                label = { Text("Contact Email *") },
                placeholder = { Text("contact@yourcompany.com") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = "Contact Email")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            // Phone Input
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                label = { Text("Phone") },
                placeholder = { Text("+60 12 345 6789") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = "Phone")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            // About Company Input
            OutlinedTextField(
                value = aboutCompany,
                onValueChange = { aboutCompany = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                label = { Text("About Company *") },
                placeholder = { Text("Describe your company culture, mission, and values...") },
                maxLines = 4
            )

            // Setup Button
            Button(
                onClick = {
                    if (companyName.isNotBlank() && industry.isNotBlank() &&
                        companySize.isNotBlank() && location.isNotBlank() &&
                        contactEmail.isNotBlank() && aboutCompany.isNotBlank()) {

                        currentEmployer?.let { employer ->
                            val profile = EmployerProfile(
                                employerId = employer.id,
                                companyName = companyName,
                                industry = industry,
                                companySize = companySize,
                                aboutCompany = aboutCompany,
                                website = website,
                                location = location,
                                contactEmail = contactEmail,
                                phone = phone
                            )
                            scope.launch {
                                employerViewModel.updateEmployerProfile(profile).onSuccess {
                                    snackbarHostState.showSnackbar("Profile setup complete!")
                                    onSetupComplete()
                                }.onFailure {
                                    snackbarHostState.showSnackbar("Failed to save profile: ${it.message}")
                                }
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please fill in all required fields")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = companyName.isNotBlank() && industry.isNotBlank() &&
                        companySize.isNotBlank() && location.isNotBlank() &&
                        contactEmail.isNotBlank() && aboutCompany.isNotBlank()
            ) {
                Text("Complete Setup", fontSize = 16.sp)
            }

            // Skip Button for optional setup
            TextButton(
                onClick = {
                    // Allow employers to skip profile setup
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
                        employerViewModel.logout()
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