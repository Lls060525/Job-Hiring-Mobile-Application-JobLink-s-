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
import com.example.madassignment.components.CommunityPost
import com.example.madassignment.components.CreatePostDialog
import com.example.madassignment.components.PurpleTopAppBar
import com.example.madassignment.data.JobViewModel
import kotlinx.coroutines.launch

@Composable
fun CommunityScreen(jobViewModel: JobViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val currentUser by jobViewModel.currentUser.collectAsState()
    val userProfile by jobViewModel.userProfile.collectAsState()
    val allPosts = remember { jobViewModel.allCommunityPosts }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
            PurpleTopAppBar(title = "Community")
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (currentUser != null) {
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
                            jobViewModel.togglePostLike(post.id) // ← Now passes String ID
                        },
                        isLiked = jobViewModel.isPostLiked(post)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showCreateDialog && currentUser != null) {
        CreatePostDialog(
            onDismiss = { showCreateDialog = false },
            onPostCreated = { content ->
                val newPost = com.example.madassignment.data.CommunityPost(
                    id = 0.toString(),
                    author = userProfile?.name ?: currentUser?.name ?: "Anonymous",
                    timeAgo = "Just now",
                    company = userProfile?.company ?: "",
                    content = content,
                    likes = 0
                )
                jobViewModel.addCommunityPost(newPost)
                scope.launch {
                    snackbarHostState.showSnackbar("Post created successfully!")
                }
            },
            userName = userProfile?.name ?: currentUser?.name ?: "",
            userCompany = userProfile?.company ?: ""
        )
    }
}