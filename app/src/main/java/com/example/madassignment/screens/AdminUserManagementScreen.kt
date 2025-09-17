package com.example.madassignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.madassignment.data.JobViewModel
import com.example.madassignment.data.User
import com.example.madassignment.data.UserProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(
    jobViewModel: JobViewModel,
    navController: NavController
) {
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedUserForDelete by remember { mutableStateOf<User?>(null) }
    var showPromoteDialog by remember { mutableStateOf(false) }
    var selectedUserForPromote by remember { mutableStateOf<User?>(null) }

    // Load users from Firestore when screen is created
    LaunchedEffect(Unit) {
        jobViewModel.loadAllUsersWithProfiles()
    }

    // Use the actual data from ViewModel
    val allUsersWithProfiles = remember { jobViewModel.allUsersWithProfiles }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filteredUsers = remember(searchQuery, allUsersWithProfiles) {
        if (searchQuery.isBlank()) {
            allUsersWithProfiles
        } else {
            allUsersWithProfiles.filter { (user, profile) ->
                user.name.contains(searchQuery, ignoreCase = true) ||
                        user.email.contains(searchQuery, ignoreCase = true) ||
                        profile?.company?.contains(searchQuery, ignoreCase = true) == true ||
                        profile?.skills?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }

                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search users...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )

            Text(
                text = "Users (${filteredUsers.size})",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            if (filteredUsers.isEmpty()) {
                Text(
                    text = if (searchQuery.isNotBlank()) {
                        "No users found for \"$searchQuery\""
                    } else {
                        "No users found"
                    },
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredUsers) { (user, profile) ->
                    AdminUserListItem(
                        user = user,
                        profile = profile,
                        onDelete = {
                            selectedUserForDelete = user
                            showDeleteDialog = true
                        },
                        onPromote = {
                            selectedUserForPromote = user
                            showPromoteDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && selectedUserForDelete != null) {
        AdminDeleteUserDialog(
            user = selectedUserForDelete!!,
            onDismiss = {
                showDeleteDialog = false
                selectedUserForDelete = null
            },
            onConfirm = {
                jobViewModel.deleteUser(selectedUserForDelete!!.id)
                showDeleteDialog = false
                selectedUserForDelete = null

                scope.launch {
                    snackbarHostState.showSnackbar("User deleted successfully")
                }
            }
        )
    }

    // Promote Confirmation Dialog
    if (showPromoteDialog && selectedUserForPromote != null) {
        AdminPromoteUserDialog(
            user = selectedUserForPromote!!,
            onDismiss = {
                showPromoteDialog = false
                selectedUserForPromote = null
            },
            onConfirm = {
                jobViewModel.promoteToAdmin(selectedUserForPromote!!.id)
                showPromoteDialog = false
                selectedUserForPromote = null

                scope.launch {
                    snackbarHostState.showSnackbar("User promoted to admin")
                }
            }
        )
    }
}

@Composable
fun AdminUserListItem(
    user: com.example.madassignment.data.User,
    profile: com.example.madassignment.data.UserProfile?,
    onDelete: () -> Unit,
    onPromote: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // User info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "ID: ${user.id} • ${if (user.isAdmin) "Admin" else "User"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    profile?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Company: ${it.company}",
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Skills: ${it.skills.take(50)}${if (it.skills.length > 50) "..." else ""}",
                            fontSize = 12.sp
                        )
                    }
                }

                // Action buttons
                Row {

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete User",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDeleteUserDialog(
    user: com.example.madassignment.data.User,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete User") },
        text = {
            Column {
                Text("Are you sure you want to delete this user?")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Name: ${user.name}")
                Text("Email: ${user.email}")
                Text("ID: ${user.id}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("This action cannot be undone.", color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Delete User")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminPromoteUserDialog(
    user: com.example.madassignment.data.User,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Promote to Admin") },
        text = {
            Column {
                Text("Are you sure you want to promote this user to admin?")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Name: ${user.name}")
                Text("Email: ${user.email}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("This user will gain full administrative privileges.")
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Promote to Admin")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}