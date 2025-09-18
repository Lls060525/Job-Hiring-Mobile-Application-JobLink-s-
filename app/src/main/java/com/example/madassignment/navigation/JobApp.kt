package com.example.madassignment.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import com.example.madassignment.components.BottomNavigationBar
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import com.example.madassignment.data.JobViewModel
import com.example.madassignment.data.EmployerViewModel

@Composable
fun JobApp(jobViewModel: JobViewModel, employerViewModel: EmployerViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentUserState = jobViewModel.currentUser.collectAsState()
    val currentUser = currentUserState.value

    // Define admin routes where bottom bar should be hidden
    val adminRoutes = listOf(
        "adminDashboard",
        Screen.AdminUserManagement.route,
        Screen.AdminJobListings.route,
        Screen.AdminCommunityPosts.route,
        "adminLogin"
    )

    // Define other routes where bottom bar should be hidden
    val noBottomBarRoutes = listOf(
        "auth",
        "profileSetup",
        "welcome",
        "employer_welcome",
        "employer_auth",
        "employer_profile_setup"
    ) + adminRoutes

    val showBottomBar = currentUser != null &&
            currentRoute != null &&
            currentRoute !in noBottomBarRoutes

    // Debug logging
    println("DEBUG: Current user = $currentUser")
    println("DEBUG: Current route = $currentRoute")
    println("DEBUG: Show bottom bar = $showBottomBar")

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
            employerViewModel = employerViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}