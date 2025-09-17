// AdminCommunityPostsScreen.kt
package com.example.madassignment.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.madassignment.components.PurpleTopAppBar
import com.example.madassignment.data.JobViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCommunityPostsScreen(
    jobViewModel: JobViewModel,
    navController: NavController? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedPostForDelete by remember { mutableStateOf<com.example.madassignment.data.CommunityPost?>(null) }


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
            TopAppBar(
                title = { Text("Manage Community Posts") },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
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


            Text(
                text = "Community Posts (${filteredPosts.size})",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )


            if (filteredPosts.isEmpty()) {
                Text(
                    text = if (searchQuery.isNotBlank()) {
                        "No posts found for \"$searchQuery\""
                    } else {
                        "No community posts yet"
                    },
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredPosts) { post ->
                    AdminPostListItem(
                        post = post,
                        onDelete = {
                            selectedPostForDelete = post
                            showDeleteDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }


    // Delete Confirmation Dialog
    if (showDeleteDialog && selectedPostForDelete != null) {
        AdminDeletePostDialog(
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


@Composable
fun AdminPostListItem(
    post: com.example.madassignment.data.CommunityPost,
    onDelete: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Author and user info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.author,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "User ID: ${post.userId}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Posted: ${post.timeAgo}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }


                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Post",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }


            // Company (if available)
            if (post.company.isNotBlank()) {
                Text(
                    text = post.company,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }


            // Content
            Text(
                text = post.content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )


            // Engagement stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${post.likes} likes",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )


                Text(
                    text = "Post ID: ${post.id.take(8)}...",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
fun AdminDeletePostDialog(
    post: com.example.madassignment.data.CommunityPost,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Delete Post")
        },
        text = {
            Column {
                Text("Are you sure you want to delete this post?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${post.content.take(50)}${if (post.content.length > 50) "..." else ""}\"",
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "By: ${post.author} (ID: ${post.userId})",
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Yes, Delete")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("No, Keep")
            }
        }
    )
}

