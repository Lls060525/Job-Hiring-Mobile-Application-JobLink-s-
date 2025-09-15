// CommunityScreen.kt - Updated version
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
import com.example.madassignment.components.*
import com.example.madassignment.data.JobViewModel
import kotlinx.coroutines.launch
import com.example.madassignment.data.CommunityPost
import java.util.Date

@Composable
fun CommunityScreen(jobViewModel: JobViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("all") } // "all" or "mine"
    var showLikesDialog by remember { mutableStateOf(false) }
    var selectedPostForLikes by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedPostForDelete by remember { mutableStateOf<com.example.madassignment.data.CommunityPost?>(null) }

    val currentUser by jobViewModel.currentUser.collectAsState()
    val userProfile by jobViewModel.userProfile.collectAsState()
    val allPosts = remember { jobViewModel.allCommunityPosts }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filteredPosts = remember(searchQuery, allPosts, selectedFilter, currentUser) {
        var result = if (searchQuery.isBlank()) {
            allPosts
        } else {
            allPosts.filter { post ->
                post.author.contains(searchQuery, ignoreCase = true) ||
                        post.company.contains(searchQuery, ignoreCase = true) ||
                        post.content.contains(searchQuery, ignoreCase = true)
            }
        }

        // Apply user filter
        if (selectedFilter == "mine" && currentUser != null) {
            result = result.filter { it.userId == currentUser!!.id } // Change authorId to userId
        }

        result
    }
    LaunchedEffect(Unit) {
        jobViewModel.loadAllUsers()
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

            // Filter buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = selectedFilter == "all",
                    onClick = { selectedFilter = "all" },
                    label = { Text("All Posts") }
                )
                FilterChip(
                    selected = selectedFilter == "mine",
                    onClick = { selectedFilter = "mine" },
                    label = { Text("My Posts") }
                )
            }

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
                } else if (selectedFilter == "mine") {
                    Text(
                        text = "You haven't posted anything yet.",
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
                    val isOwnPost = currentUser != null && post.userId == currentUser!!.id

                    CommunityPost(
                        post = post,
                        jobViewModel = jobViewModel,
                        onLikeClick = {
                            jobViewModel.togglePostLike(post.id)
                        },
                        onLongPress = if (isOwnPost) {
                            {
                                selectedPostForDelete = post
                                showDeleteDialog = true
                            }
                        } else {
                            null
                        },
                        isLiked = jobViewModel.isPostLiked(post.id), // This should return true/false correctly
                        isOwnPost = isOwnPost,
                        onSeeMoreLikes = {
                            selectedPostForLikes = post.id
                            showLikesDialog = true
                        }
                    )
                }
            }
        }
    }

    // Create Post Dialog
    if (showCreateDialog && currentUser != null) {
        CreatePostDialog(
            onDismiss = { showCreateDialog = false },
            onPostCreated = { content ->
                val newPost = CommunityPost(
                    id = "",
                    author = userProfile?.name ?: currentUser?.name ?: "Anonymous",
                    timeAgo = "Just now", // This will be recalculated
                    company = userProfile?.company ?: "",
                    content = content,
                    likes = 0,
                    userId = currentUser!!.id,
                    createdAt = Date() // Set current timestamp
                )
                jobViewModel.addCommunityPost(newPost)
                scope.launch {
                    snackbarHostState.showSnackbar("Post created successfully!")
                }
                showCreateDialog = false
            },
            userName = userProfile?.name ?: currentUser?.name ?: "",
            userCompany = userProfile?.company ?: ""
        )
    }

    // Likes Dialog
    if (showLikesDialog && selectedPostForLikes != null) {
        LikesDialog(
            postId = selectedPostForLikes!!,
            jobViewModel = jobViewModel,
            onDismiss = {
                showLikesDialog = false
                selectedPostForLikes = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && selectedPostForDelete != null) {
        DeletePostDialog(
            post = selectedPostForDelete!!,
            onDismiss = {
                showDeleteDialog = false
                selectedPostForDelete = null
            },
            onConfirm = {
                jobViewModel.deleteCommunityPost(selectedPostForDelete!!)
                showDeleteDialog = false
                selectedPostForDelete = null

                scope.launch {
                    snackbarHostState.showSnackbar("Post deleted successfully")
                }
            }
        )
    }

}

