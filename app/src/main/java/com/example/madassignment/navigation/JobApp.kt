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

// JobApp.kt
@Composable
fun JobApp(jobViewModel: JobViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentUserState = jobViewModel.currentUser.collectAsState()
    val isAdminState = jobViewModel.isAdmin.collectAsState()
    val currentUser = currentUserState.value
    val isAdmin = isAdminState.value

    // Exclude admin route from showing bottom bar
    val showBottomBar = currentUser != null &&
            currentRoute != "auth" &&
            currentRoute != "profileSetup" &&
            currentRoute != Screen.Admin.route &&
            currentRoute != "adminCommunityPosts" &&
            currentRoute != "adminUserManagement" &&
            currentRoute != "adminJobListings"

    // Use different screens for admin users
    val bottomBarScreens = if (isAdmin) adminScreens else screens

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    screens = bottomBarScreens
                )
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