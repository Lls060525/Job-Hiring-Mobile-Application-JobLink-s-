package com.example.madassignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.madassignment.components.EmployerBottomNavigationBar
import com.example.madassignment.components.PurpleTopAppBar
import com.example.madassignment.components.ViewOnlyCommunityPost
import com.example.madassignment.data.EmployerViewModel
import com.example.madassignment.data.JobViewModel
import kotlinx.coroutines.launch

@Composable
fun EmployerCommunityScreen(
    navController: NavController,
    employerViewModel: EmployerViewModel,
    jobViewModel: JobViewModel
) {
    var searchQuery by remember { mutableStateOf("") }

    val allPosts by employerViewModel.allCommunityPosts.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load posts when screen loads
    LaunchedEffect(Unit) {
        employerViewModel.loadCommunityPosts()
        employerViewModel.setupCommunityPostsListener()
    }

    val filteredPosts = remember(searchQuery, allPosts) {
        if (searchQuery.isBlank()) {
            allPosts
        } else {
            allPosts.filter { post ->
                post.author.contains(searchQuery, ignoreCase = true) ||
                        post.company.contains(searchQuery, ignoreCase = true) ||
                        post.content.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            PurpleTopAppBar(title = "Community Posts")
        },
        bottomBar = {
            EmployerBottomNavigationBar(navController = navController)
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search posts...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {})
            )

            // Title
            Text(
                text = "User Community Posts",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Description
            Text(
                text = "View what users are discussing in the community",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (filteredPosts.isEmpty()) {
                if (searchQuery.isNotBlank()) {
                    Text(
                        text = "No posts found for \"$searchQuery\"",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "No community posts yet",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredPosts) { post ->
                    // Use ViewOnlyCommunityPost instead of CommunityPost
                    ViewOnlyCommunityPost(
                        post = post,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}