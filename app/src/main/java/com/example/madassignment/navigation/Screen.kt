// Screen.kt
package com.example.madassignment.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object MyActivity : Screen("activity", "My Activity", Icons.Default.AccountBox)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Community : Screen("community", "Community", Icons.Default.Email)
    object Admin : Screen("admin", "Admin", Icons.Default.Build) // Add Admin screen
}

val screens = listOf(
    Screen.Home,
    Screen.MyActivity,
    Screen.Community,
    Screen.Profile
)

val adminScreens = listOf(
    Screen.Admin, // Only show admin screen for admin users
    Screen.Home,
    Screen.Community
)