package com.example.madassignment.screens

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
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.madassignment.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.madassignment.components.PurpleTopAppBar
import com.example.madassignment.data.UserProfile
import kotlinx.coroutines.launch
import com.example.madassignment.data.JobViewModel
import com.example.madassignment.components.SkillsSelectionDialog
import com.example.madassignment.data.UserSkill
import com.example.madassignment.data.stringToSkills
import com.example.madassignment.data.skillsToString
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ProfileScreen(jobViewModel: JobViewModel) {
    var isEditing by remember { mutableStateOf(false) }
    var showSkillsDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) } // Add this line
    val currentUser by jobViewModel.currentUser.collectAsState()
    val userProfile by jobViewModel.userProfile.collectAsState()

    var name by remember { mutableStateOf(userProfile?.name ?: "") }
    var age by remember { mutableStateOf(userProfile?.age ?: "") }
    var aboutMe by remember { mutableStateOf(userProfile?.aboutMe ?: "") }
    var skills by remember { mutableStateOf(userProfile?.skills ?: "") }
    var company by remember { mutableStateOf(userProfile?.company ?: "") }
    var profileImageUri by remember { mutableStateOf(userProfile?.profileImageUri) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Parse skills from stored string
    val currentSkills = remember(skills) {
        stringToSkills(skills)
    }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            name = it.name
            age = it.age
            aboutMe = it.aboutMe
            skills = it.skills
            company = it.company
            profileImageUri = it.profileImageUri
        }
    }

    Scaffold(
        topBar = {
            PurpleTopAppBar(title = "Profile")
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
                    text = "Profile",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row {
                    // Logout Button - UPDATED to show dialog
                    IconButton(
                        onClick = { showLogoutDialog = true }, // Show confirmation dialog
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Edit Button
                    IconButton(
                        onClick = {
                            if (isEditing) {
                                // Save changes
                                currentUser?.let { user ->
                                    val updatedProfile = UserProfile(
                                        userId = user.id,
                                        name = name,
                                        age = age,
                                        aboutMe = aboutMe,
                                        skills = skills,
                                        company = company,
                                        profileImageUri = profileImageUri
                                    )
                                    scope.launch {
                                        jobViewModel.updateUserProfile(updatedProfile).onSuccess {
                                            snackbarHostState.showSnackbar("Profile saved successfully!")
                                            isEditing = false
                                        }.onFailure {
                                            snackbarHostState.showSnackbar("Failed to save profile: ${it.message}")
                                        }
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

            // Display current user email if available
            currentUser?.let { user ->
                Text(
                    text = "Logged in as: ${user.email}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Picture Section
            ProfilePictureSection(
                imageUri = profileImageUri,
                onImageSelected = { uri ->
                    profileImageUri = uri
                },
                isEditing = isEditing
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Personal Information Section
            PersonalInfoSection(
                name = name,
                age = age,
                aboutMe = aboutMe,
                skills = skills,
                company = company,
                isEditing = isEditing,
                onNameChange = { name = it },
                onAgeChange = { age = it },
                onAboutMeChange = { aboutMe = it },
                onSkillsChange = { skills = it },
                onCompanyChange = { company = it },
                onSkillsClick = { showSkillsDialog = true }
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
                            userProfile?.let {
                                name = it.name
                                age = it.age
                                aboutMe = it.aboutMe
                                skills = it.skills
                                company = it.company
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
                            currentUser?.let { user ->
                                val updatedProfile = UserProfile(
                                    userId = user.id,
                                    name = name,
                                    age = age,
                                    aboutMe = aboutMe,
                                    skills = skills,
                                    company = company,
                                    profileImageUri = profileImageUri
                                )
                                scope.launch {
                                    jobViewModel.updateUserProfile(updatedProfile).onSuccess {
                                        snackbarHostState.showSnackbar("Profile saved successfully!")
                                        isEditing = false
                                    }.onFailure {
                                        snackbarHostState.showSnackbar("Failed to save profile: ${it.message}")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }

    // Skills Selection Dialog
    if (showSkillsDialog) {
        SkillsSelectionDialog(
            currentSkills = currentSkills,
            onDismiss = { showSkillsDialog = false },
            onSkillsSelected = { newSkills ->
                skills = skillsToString(newSkills)
            }
        )
    }

    // Logout Confirmation Dialog - ADD THIS NEW DIALOG
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
                        jobViewModel.logout()
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
fun ProfilePictureSection(
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
fun PersonalInfoSection(
    name: String,
    age: String,
    aboutMe: String,
    skills: String,
    company: String,
    isEditing: Boolean,
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onAboutMeChange: (String) -> Unit,
    onSkillsChange: (String) -> Unit,
    onCompanyChange: (String) -> Unit,
    onSkillsClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoItem(
            label = "Name",
            value = name,
            isEditing = isEditing,
            onValueChange = onNameChange,
            icon = Icons.Default.Person
        )

        InfoItem(
            label = "Age",
            value = age,
            isEditing = isEditing,
            onValueChange = onAgeChange,
            icon = Icons.Default.Person,
            keyboardType = KeyboardType.Number
        )

        InfoItem(
            label = "Company",
            value = company,
            isEditing = isEditing,
            onValueChange = onCompanyChange,
            icon = Icons.Default.AccountBox,
            isMultiline = false
        )

        // Skills Item with special handling
        SkillsInfoItem(
            label = "Skills",
            skillsString = skills,
            isEditing = isEditing,
            onSkillsClick = onSkillsClick
        )

        InfoItem(
            label = "About Me",
            value = aboutMe,
            isEditing = isEditing,
            onValueChange = onAboutMeChange,
            icon = Icons.Default.Person,
            isMultiline = true
        )
    }
}

@Composable
fun SkillsInfoItem(
    label: String,
    skillsString: String,
    isEditing: Boolean,
    onSkillsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Person,
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
                Button(
                    onClick = onSkillsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select Skills")
                }
            } else {
                val skills = stringToSkills(skillsString)
                if (skills.isEmpty()) {
                    Text(
                        text = "Not specified",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        skills.forEach { skill ->
                            SkillChipDisplay(skill = skill)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isMultiline: Boolean = false
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
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your $label") },
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    singleLine = !isMultiline,
                    maxLines = if (isMultiline) 3 else 1
                )
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

@Composable
fun SkillChipDisplay(skill: UserSkill) {
    AssistChip(
        onClick = {},
        label = { Text("${skill.name} (${skill.level})") }
    )
}