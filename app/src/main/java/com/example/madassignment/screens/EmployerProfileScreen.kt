package com.example.madassignment.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.madassignment.R
import com.example.madassignment.components.EmployerBottomNavigationBar
import com.example.madassignment.components.PurpleTopAppBar
import com.example.madassignment.data.EmployerViewModel
import kotlinx.coroutines.launch

internal fun isValidCompanyName(name: String): Boolean {
    return name.isNotBlank() && name.length >= 2
}

internal fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
    return email.matches(emailRegex.toRegex())
}

@Composable
fun EmployerProfileScreen(
    navController: NavController,
    employerViewModel: EmployerViewModel
) {
    val currentEmployer by employerViewModel.currentEmployer.collectAsState()
    val employerProfile by employerViewModel.employerProfile.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Form fields
    var companyName by remember { mutableStateOf(employerProfile?.companyName ?: "") }
    var industry by remember { mutableStateOf(employerProfile?.industry ?: "") }
    var companySize by remember { mutableStateOf(employerProfile?.companySize ?: "") }
    var location by remember { mutableStateOf(employerProfile?.location ?: "") }
    var contactEmail by remember { mutableStateOf(employerProfile?.contactEmail ?: "") }
    var aboutCompany by remember { mutableStateOf(employerProfile?.aboutCompany ?: "") }
    var profileImageUri by remember { mutableStateOf(employerProfile?.profileImageUri) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Update form fields when profile changes
    LaunchedEffect(employerProfile) {
        employerProfile?.let {
            companyName = it.companyName
            industry = it.industry
            companySize = it.companySize
            location = it.location
            contactEmail = it.contactEmail
            aboutCompany = it.aboutCompany ?: ""
            profileImageUri = it.profileImageUri
        }
    }

    Scaffold(
        topBar = {
            PurpleTopAppBar(title = "Employer Profile")
        },
        bottomBar = {
            EmployerBottomNavigationBar(navController = navController)
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header with Edit and Logout Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Company Profile",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row {
                    // Logout Button
                    IconButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Edit Button
                    IconButton(
                        onClick = {
                            if (isEditing) {
                                // Save changes
                                if (isValidCompanyName(companyName) && isValidEmail(contactEmail)) {
                                    currentEmployer?.let { employer ->
                                        val updatedProfile = com.example.madassignment.data.EmployerProfile(
                                            employerId = employer.id,
                                            companyName = companyName,
                                            industry = industry,
                                            companySize = companySize,
                                            location = location,
                                            contactEmail = contactEmail,
                                            aboutCompany = aboutCompany,
                                            profileImageUri = profileImageUri
                                        )
                                        scope.launch {
                                            employerViewModel.updateEmployerProfile(updatedProfile).onSuccess {
                                                snackbarHostState.showSnackbar("Profile saved successfully!")
                                                isEditing = false
                                            }.onFailure {
                                                snackbarHostState.showSnackbar("Failed to save profile: ${it.message}")
                                            }
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Please fix validation errors before saving")
                                    }
                                }
                            } else {
                                isEditing = true
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Save" else "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Display current employer email if available
            currentEmployer?.let { employer ->
                Text(
                    text = "Logged in as: ${employer.email}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Picture Section
            EmployerProfilePictureSection(
                imageUri = profileImageUri,
                onImageSelected = { uri ->
                    profileImageUri = uri
                },
                isEditing = isEditing
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Company Information Section
            CompanyInfoSection(
                companyName = companyName,
                industry = industry,
                companySize = companySize,
                location = location,
                contactEmail = contactEmail,
                aboutCompany = aboutCompany,
                isEditing = isEditing,
                onCompanyNameChange = { companyName = it },
                onIndustryChange = { industry = it },
                onCompanySizeChange = { companySize = it },
                onLocationChange = { location = it },
                onContactEmailChange = { contactEmail = it },
                onAboutCompanyChange = { aboutCompany = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            if (isEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            isEditing = false
                            // Reset to original values
                            employerProfile?.let {
                                companyName = it.companyName
                                industry = it.industry
                                companySize = it.companySize
                                location = it.location
                                contactEmail = it.contactEmail
                                aboutCompany = it.aboutCompany ?: ""
                                profileImageUri = it.profileImageUri
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            if (isValidCompanyName(companyName) && isValidEmail(contactEmail)) {
                                currentEmployer?.let { employer ->
                                    val updatedProfile = com.example.madassignment.data.EmployerProfile(
                                        employerId = employer.id,
                                        companyName = companyName,
                                        industry = industry,
                                        companySize = companySize,
                                        location = location,
                                        contactEmail = contactEmail,
                                        aboutCompany = aboutCompany,
                                        profileImageUri = profileImageUri
                                    )
                                    scope.launch {
                                        employerViewModel.updateEmployerProfile(updatedProfile).onSuccess {
                                            snackbarHostState.showSnackbar("Profile saved successfully!")
                                            isEditing = false
                                        }.onFailure {
                                            snackbarHostState.showSnackbar("Failed to save profile: ${it.message}")
                                        }
                                    }
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please fix validation errors before saving")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isValidCompanyName(companyName) && isValidEmail(contactEmail)
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text("Confirm Logout", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to logout from your account?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        employerViewModel.logout()
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EmployerProfilePictureSection(
    imageUri: String?,
    onImageSelected: (String) -> Unit,
    isEditing: Boolean
) {
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            onImageSelected(it.toString())
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    2.dp,
                    if (isEditing) MaterialTheme.colorScheme.primary else Color.Transparent,
                    CircleShape
                )
        ) {
            if (imageUri != null) {
                // Show selected image from gallery
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(imageUri)
                            .build()
                    ),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Show default profile image
                Image(
                    painter = painterResource(id = R.drawable.default_profil),
                    contentDescription = "Default Profile",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            if (isEditing) {
                IconButton(
                    onClick = {
                        // Open gallery to pick image
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Change Photo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (isEditing) {
            Text(
                text = "Tap camera to select from gallery",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun CompanyInfoSection(
    companyName: String,
    industry: String,
    companySize: String,
    location: String,
    contactEmail: String,
    aboutCompany: String,
    isEditing: Boolean,
    onCompanyNameChange: (String) -> Unit,
    onIndustryChange: (String) -> Unit,
    onCompanySizeChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onContactEmailChange: (String) -> Unit,
    onAboutCompanyChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EmployerInfoItem(
            label = "Company Name",
            value = companyName,
            isEditing = isEditing,
            onValueChange = onCompanyNameChange,
            icon = Icons.Default.AccountBox,
            isValid = isValidCompanyName(companyName),
            errorMessage = if (companyName.isBlank()) "Company name is required" else "Company name is too short"
        )

        EmployerInfoItem(
            label = "Industry",
            value = industry,
            isEditing = isEditing,
            onValueChange = onIndustryChange,
            icon = Icons.Default.Place
        )

        EmployerInfoItem(
            label = "Company Size",
            value = companySize,
            isEditing = isEditing,
            onValueChange = onCompanySizeChange,
            icon = Icons.Default.Person
        )

        EmployerInfoItem(
            label = "Location",
            value = location,
            isEditing = isEditing,
            onValueChange = onLocationChange,
            icon = Icons.Default.LocationOn
        )

        EmployerInfoItem(
            label = "Contact Email",
            value = contactEmail,
            isEditing = isEditing,
            onValueChange = onContactEmailChange,
            icon = Icons.Default.Email,
            isValid = isValidEmail(contactEmail),
            errorMessage = if (contactEmail.isBlank()) "Email is required" else "Invalid email format"
        )

        EmployerInfoItem(
            label = "About Company",
            value = aboutCompany,
            isEditing = isEditing,
            onValueChange = onAboutCompanyChange,
            icon = Icons.Default.Info,
            isMultiline = true
        )
    }
}

@Composable
fun EmployerInfoItem(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isMultiline: Boolean = false,
    isValid: Boolean = true,
    errorMessage: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 4.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (isEditing) {
                Column {
                    OutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your $label") },
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                        singleLine = !isMultiline,
                        maxLines = if (isMultiline) 5 else 1,
                        isError = !isValid,
                        supportingText = {
                            if (!isValid && errorMessage != null) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }
            } else {
                Text(
                    text = if (value.isBlank()) "Not specified" else value,
                    fontSize = 16.sp,
                    color = if (value.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}