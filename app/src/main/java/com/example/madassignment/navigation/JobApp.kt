package com.example.madassignment.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import com.example.madassignment.components.BottomNavigationBar
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import com.example.madassignment.data.JobViewModel

@Composable
fun JobApp(jobViewModel: JobViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentUserState = jobViewModel.currentUser.collectAsState()
    val currentUser = currentUserState.value

    // Debug logging
    println("DEBUG: Current user = $currentUser")
    println("DEBUG: Current route = $currentRoute")
    println("DEBUG: Show bottom bar = ${currentUser != null && currentRoute != "auth"}")

    val showBottomBar = currentUser != null &&
            currentRoute != "auth" &&
            currentRoute != "profileSetup"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            jobViewModel = jobViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}