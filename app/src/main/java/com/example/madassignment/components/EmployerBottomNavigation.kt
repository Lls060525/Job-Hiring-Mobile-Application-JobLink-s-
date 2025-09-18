package com.example.madassignment.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun EmployerBottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = {
                Icon(Icons.Default.Home, contentDescription = "Dashboard")
            },
            label = { Text("Dashboard") },
            selected = currentRoute == "employer_dashboard",
            onClick = {
                if (currentRoute != "employer_dashboard") {
                    navController.navigate("employer_dashboard") {
                        popUpTo("employer_dashboard") {
                            inclusive = true
                        }
                        launchSingleTop = true

                    }
                }
            }
        )

        NavigationBarItem(
            icon = {
                Icon(Icons.Default.List, contentDescription = "Applications")
            },
            label = { Text("Applications") },
            selected = currentRoute == "employer_applications",
            onClick = {
                if (currentRoute != "employer_applications") {
                    navController.navigate("employer_applications") {
                        // Don't pop up to start destination to preserve back stack
                        popUpTo("employer_dashboard") {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )

        NavigationBarItem(
            icon = {
                Icon(Icons.Default.Email, contentDescription = "Community")
            },
            label = { Text("Community") },
            selected = currentRoute == "employer_community",
            onClick = {
                if (currentRoute != "employer_community") {
                    navController.navigate("employer_community") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )

        NavigationBarItem(
            icon = {
                Icon(Icons.Default.Person, contentDescription = "Profile")
            },
            label = { Text("Profile") },
            selected = currentRoute == "employer_profile",
            onClick = {
                if (currentRoute != "employer_profile") {
                    navController.navigate("employer_profile") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        )
    }
}