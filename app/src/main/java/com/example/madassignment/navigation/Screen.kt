package com.example.madassignment.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.madassignment.screens.CommunityScreen




// Screen.kt
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object MyActivity : Screen("activity", "My Activity", Icons.Default.AccountBox)

    // Add to your Screen sealed class
    object AdminDashboard : Screen("adminDashboard", "Admin Dashboard", Icons.Default.Build)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Community : Screen("community", "Community", Icons.Default.Email)
    object Admin : Screen("admin", "Admin", Icons.Default.Build)
    object AdminUserManagement : Screen("adminUserManagement", "User Management", Icons.Default.Person)
    object AdminJobListings : Screen("adminJobListings", "Job Listings", Icons.Default.Menu)
    object AdminCommunityPosts : Screen("adminCommunityPosts", "Community Posts", Icons.Default.List)
}

val screens = listOf(
    Screen.Home,
    Screen.MyActivity,
    Screen.Community,
    Screen.Profile
)

val adminScreens = listOf(
    Screen.AdminUserManagement,
    Screen.AdminJobListings,
    Screen.AdminCommunityPosts
)
// Sealed class for employer navigation
sealed class EmployerScreen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : EmployerScreen("employer_dashboard", "Dashboard", Icons.Default.Home)
    data object Applications : EmployerScreen("employer_applications", "Applications", Icons.Default.List) // Changed from AutoMirrored.Filled.List to Default.List
    data object Community : EmployerScreen("employer_community", "Community", Icons.Default.Face) // Changed from Chat to People
    data object Profile : EmployerScreen("employer_profile", "Profile", Icons.Default.Person)
}