package com.example.madassignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madassignment.components.PurpleTopAppBar
import com.example.madassignment.data.JobViewModel
import kotlinx.coroutines.launch

@Composable
fun AdminScreen(jobViewModel: JobViewModel) {
    val users by remember { mutableStateOf<List<com.example.madassignment.data.User>>(emptyList()) }
    val stats by remember { mutableStateOf(AdminStats()) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        // Load admin data when screen is opened
        scope.launch {
            // You might want to add methods to get all users and stats
            // users = jobViewModel.getAllUsers()
            // stats = jobViewModel.getAdminStats()
        }
    }

    Scaffold(
        topBar = {
            PurpleTopAppBar(title = "Admin Dashboard")
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Statistics Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    title = "Total Users",
                    value = stats.totalUsers.toString(),
                    icon = Icons.Default.Person
                )
                StatCard(
                    title = "Total Jobs",
                    value = stats.totalJobs.toString(),
                    icon = Icons.Default.Star
                )
                StatCard(
                    title = "Total Posts",
                    value = stats.totalPosts.toString(),
                    icon = Icons.Default.Build
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "User Management",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (users.isEmpty()) {
                Text("No users found", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn {
                    items(users) { user ->
                        UserListItem(user = user, onPromoteToAdmin = {
                            scope.launch {
                                // jobViewModel.promoteToAdmin(user.id)
                                snackbarHostState.showSnackbar("User promoted to admin")
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(24.dp))
            Text(value, fontWeight = FontWeight.Bold)
            Text(title, fontSize = 12.sp)
        }
    }
}

@Composable
fun UserListItem(user: com.example.madassignment.data.User, onPromoteToAdmin: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(user.name, fontWeight = FontWeight.Bold)
                Text(user.email, fontSize = 12.sp)
                Text(if (user.isAdmin) "Admin" else "User", fontSize = 12.sp)
            }
            if (!user.isAdmin) {
                Button(
                    onClick = onPromoteToAdmin,
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Make Admin", fontSize = 12.sp)
                }
            }
        }
    }
}

data class AdminStats(
    val totalUsers: Int = 0,
    val totalJobs: Int = 0,
    val totalPosts: Int = 0
)