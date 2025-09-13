package com.example.madassignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.madassignment.components.PurpleTopAppBar
import com.example.madassignment.data.JobViewModel

@Composable
fun AdminMenuScreen(
    jobViewModel: JobViewModel,
    navController: NavController? = null
) {
    // Get the current user from the ViewModel
    val currentUser by jobViewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            PurpleTopAppBar(title = "Admin Dashboard")
        },
        floatingActionButton = {
            // Add logout FAB
            ExtendedFloatingActionButton(
                onClick = {
                    jobViewModel.logout()
                    navController?.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                icon = {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                },
                text = { Text("Logout") },
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Welcome message - use the currentUser from ViewModel
            currentUser?.let { user ->
                Text(
                    text = "Welcome, Admin ${user.name}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // Admin options
            AdminOptionCard(
                title = "User Management",
                description = "View and manage all users",
                onClick = { /* Handle user management */ }
            )

            AdminOptionCard(
                title = "Job Listings",
                description = "Manage job postings",
                onClick = { /* Handle job management */ }
            )

            AdminOptionCard(
                title = "Community Posts",
                description = "Moderate community content",
                onClick = { /* Handle community moderation */ }
            )

            AdminOptionCard(
                title = "Statistics",
                description = "View app analytics and metrics",
                onClick = { /* Handle statistics */ }
            )

            AdminOptionCard(
                title = "System Settings",
                description = "Configure application settings",
                onClick = { /* Handle system settings */ }
            )
        }
    }
}

@Composable
fun AdminOptionCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}