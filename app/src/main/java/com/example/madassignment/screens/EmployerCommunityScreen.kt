package com.example.madassignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.madassignment.components.CommunityPost
import com.example.madassignment.components.CreatePostDialog
import com.example.madassignment.components.EmployerBottomNavigationBar
import com.example.madassignment.components.PurpleTopAppBar
import com.example.madassignment.data.EmployerViewModel
import kotlinx.coroutines.launch

@Composable
fun EmployerCommunityScreen(
    navController: NavController,
    employerViewModel: EmployerViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val currentEmployer by employerViewModel.currentEmployer.collectAsState()
    val employerProfile by employerViewModel.employerProfile.collectAsState()
    val allPosts by employerViewModel.allCommunityPosts.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Add this LaunchedEffect to load posts when screen loads
    LaunchedEffect(currentEmployer) {
        if (currentEmployer != null) {
            employerViewModel.loadCommunityPosts()
            employerViewModel.setupCommunityPostsListener() // For real-time updates
        }
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
            PurpleTopAppBar(title = "Employer Community")
        },
        bottomBar = {
            EmployerBottomNavigationBar(navController = navController)
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (currentEmployer != null) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Post")
                }
            }
        }
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
                placeholder = { Text("Search by name or company...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {})
            )

            // Title
            Text(
                text = "Community Discussions",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
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
                        text = "No posts yet. Be the first to share!",
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
                    CommunityPost(
                        post = post,
                        onLikeClick = {
                            employerViewModel.togglePostLike(post.id)
                        },
                        isLiked = employerViewModel.isPostLiked(post)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showCreateDialog && currentEmployer != null) {
        CreatePostDialog(
            onDismiss = { showCreateDialog = false },
            onPostCreated = { content ->
                // Create a new post with the correct parameters
                val newPost = com.example.madassignment.data.CommunityPost(
                    id = "", // Firebase will assign the real ID
                    author = employerProfile?.companyName ?: currentEmployer?.email ?: "Anonymous Employer",
                    timeAgo = "Just now",
                    company = employerProfile?.companyName ?: "Company",
                    content = content,
                    likes = 0,
                    likedBy = "",
                    userId = currentEmployer?.id ?: 0
                )
                employerViewModel.addCommunityPost(newPost)
                scope.launch {
                    snackbarHostState.showSnackbar("Post created successfully!")
                }
            },
            userName = employerProfile?.companyName ?: currentEmployer?.email ?: "",
            userCompany = employerProfile?.companyName ?: ""
        )
    }
}