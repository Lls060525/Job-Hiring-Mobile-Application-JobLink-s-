// AppNavigation.kt
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
import com.example.madassignment.screens.WelcomeScreen
import com.example.madassignment.screens.EmployerWelcomeScreen
import com.example.madassignment.data.EmployerViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    jobViewModel: JobViewModel,
    employerViewModel: EmployerViewModel,
    modifier: Modifier = Modifier
) {
    val currentUserState = jobViewModel.currentUser.collectAsState()
    val userProfileState = jobViewModel.userProfile.collectAsState()
    val currentEmployerState = employerViewModel.currentEmployer.collectAsState()
    val employerProfileState = employerViewModel.employerProfile.collectAsState()

    val currentUser = currentUserState.value
    val userProfile = userProfileState.value
    val currentEmployer = currentEmployerState.value
    val employerProfile = employerProfileState.value

    // Determine start destination based on authentication state
    val startDestination = when {
        currentUser != null -> {
            if (userProfile?.isSetupComplete == true) Screen.Home.route else "profileSetup"
        }
        currentEmployer != null -> {
            if (employerProfile?.isSetupComplete == true) "employer_dashboard" else "employer_profile_setup"
        }
        else -> "welcome"
    }

    // Debug logging
    LaunchedEffect(currentUser, userProfile, currentEmployer, employerProfile) {
        println("DEBUG: Current user = $currentUser")
        println("DEBUG: User profile = $userProfile")
        println("DEBUG: Current employer = $currentEmployer")
        println("DEBUG: Employer profile = $employerProfile")
        println("DEBUG: Start destination = $startDestination")
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("welcome") {
            WelcomeScreen(navController = navController)
        }

        composable("employer_welcome") {
            EmployerWelcomeScreen(navController = navController)
        }

        composable("employer_auth") {
            EmployerLoginScreen(
                navController = navController,
                employerViewModel = employerViewModel,
                onLoginSuccess = {
                    // Check if profile is complete
                    val profile = employerViewModel.employerProfile.value
                    if (profile != null && profile.isSetupComplete) {
                        navController.navigate("employer_dashboard") {
                            popUpTo("employer_auth") { inclusive = true }
                        }
                    } else {
                        navController.navigate("employer_profile_setup") {
                            popUpTo("employer_auth") { inclusive = true }
                        }
                    }
                }
            )
        }


        // AppNavigation.kt - Add these composables
        // Add these to your navigation graph
        composable("adminLogin") {
            AdminLoginScreen(navController, jobViewModel)
        }

        composable("adminDashboard") {
            AdminScreen(jobViewModel) // Your existing admin screen
        }

        composable(Screen.AdminUserManagement.route) {
            AdminUserManagementScreen(
                jobViewModel = jobViewModel,
                navController = navController
            )
        }

        composable(Screen.AdminJobListings.route) {
            AdminJobListingsScreen(
                jobViewModel = jobViewModel,
                navController = navController
            )
        }

        composable(Screen.AdminCommunityPosts.route) {
            AdminCommunityPostsScreen(
                jobViewModel = jobViewModel,
                navController = navController
            )
        }

        composable("auth") {
            LoginScreen(
                navController = navController,
                jobViewModel = jobViewModel,
                onLoginSuccess = {
                    // Navigation is handled by the NavHost based on state
                }
            )
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

        composable("employer_dashboard") {
            EmployerDashboardScreen(
                navController = navController,
                employerViewModel = employerViewModel
            )
        }

        composable("employer_applications") {
            EmployerApplicationsScreen(
                navController = navController,
                employerViewModel = employerViewModel
            )
        }

        composable("employer_community") {
            EmployerCommunityScreen(
                navController = navController,
                employerViewModel = employerViewModel
            )
        }

        composable("employer_profile") {
            EmployerProfileScreen(
                navController = navController,
                employerViewModel = employerViewModel
            )
        }

        // Add employer profile setup route
        composable("employer_profile_setup") {
            EmployerProfileSetupScreen(
                navController = navController,
                employerViewModel = employerViewModel,
                onSetupComplete = {
                    navController.navigate("employer_dashboard") {
                        popUpTo("employer_profile_setup") { inclusive = true }
                    }
                }
            )
        }
    }
}