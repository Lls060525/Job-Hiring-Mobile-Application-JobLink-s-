package com.example.madassignment.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.madassignment.data.JobViewModel
import com.example.madassignment.screens.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    jobViewModel: JobViewModel,
    modifier: Modifier = Modifier
) {
    val currentUserState = jobViewModel.currentUser.collectAsState()
    val userProfileState = jobViewModel.userProfile.collectAsState()
    val isAdminState = jobViewModel.isAdmin.collectAsState()
    val currentUser = currentUserState.value
    val userProfile = userProfileState.value
    val isAdmin = isAdminState.value

    // Debug logging
    LaunchedEffect(currentUser, userProfile) {
        println("DEBUG: Current user = $currentUser")
        println("DEBUG: User profile = $userProfile")
        println("DEBUG: Should show profile setup = ${currentUser != null && userProfile == null}")
    }

    NavHost(
        navController = navController,
        startDestination = when {
            currentUser == null -> "auth"
            isAdmin -> Screen.Admin.route // Redirect to admin menu if admin
            userProfile?.isSetupComplete == null -> "profileSetup"
            else -> Screen.Home.route
        },
        modifier = modifier
    ) {
        composable("auth") {
            LoginScreen(
                navController = navController,
                jobViewModel = jobViewModel,
                onLoginSuccess = {
                    // Navigation is handled by the NavHost based on state
                }
            )
        }

        composable(Screen.Admin.route) {
            AdminMenuScreen(jobViewModel = jobViewModel)
        }

        composable("profileSetup") {
            ProfileSetupScreen(
                navController = navController,
                jobViewModel = jobViewModel,
                onSetupComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo("profileSetup") { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(jobViewModel = jobViewModel)
        }
        composable(Screen.MyActivity.route) {
            MyActivityScreen(jobViewModel = jobViewModel)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(jobViewModel = jobViewModel)
        }
        composable(Screen.Community.route) {
            CommunityScreen(jobViewModel = jobViewModel)
        }
    }
}