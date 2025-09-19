package com.example.madassignment.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madassignment.data.CommunityPost
import com.example.madassignment.data.JobViewModel
import com.example.madassignment.utils.TimeUtils
import kotlinx.coroutines.delay
import java.util.Date
import java.util.concurrent.TimeUnit


// Update CommunityPost.kt - Add edit button in the rightmost corner
@Composable
fun CommunityPost(
    post: CommunityPost,
    jobViewModel: JobViewModel,
    onLikeClick: () -> Unit,
    onEditClick: (() -> Unit)? = null, // Add edit callback
    onLongPress: (() -> Unit)? = null,
    isLiked: Boolean,
    isOwnPost: Boolean,
    onSeeMoreLikes: () -> Unit
) {

// Calculate time ago dynamically and make it update over time
    val timeAgo by produceState(initialValue = TimeUtils.getTimeAgo(post.createdAt)) {
        // Update every minute for recent posts
        if (TimeUnit.MILLISECONDS.toMinutes(Date().time - post.createdAt.time) < 60) {
            while (true) {
                value = TimeUtils.getTimeAgo(post.createdAt)
                delay(60000) // Update every minute
            }
        }
    }
    // Get liker names
    val likerNames = remember(post.likedBy) {
        jobViewModel.getLikerNames(post.likedBy)
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .then(
                if (onLongPress != null) {
                    Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = onLongPress
                    )
                } else {
                    Modifier
                }
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row with author, time, and edit button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Author and time - show "Yourself" for own posts
                Text(
                    text = if (isOwnPost) {
                        "${post.author} - Yourself • $timeAgo"
                    } else {
                        "${post.author} • $timeAgo"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOwnPost) MaterialTheme.colorScheme.primary else Color.DarkGray
                )

                // Edit button for own posts - positioned at the rightmost corner
                if (isOwnPost && onEditClick != null) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Post",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Company name
            Text(
                text = post.company,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Content
            Text(
                text = post.content,
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )

            // Like button and count with "see more" functionality
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
            ) {
                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.Gray
                    )
                }

                // Enhanced likes display with "see more" functionality
                if (post.likes > 0) {
                    val annotatedString = buildAnnotatedString {
                        // Show first 2 likers' actual names
                        val firstTwoLikers = likerNames.take(2)
                        firstTwoLikers.forEachIndexed { index, userName ->
                            append(userName)
                            if (index < firstTwoLikers.size - 1) {
                                append(", ")
                            }
                        }

                        // Show "and X others" if there are more than 2
                        if (post.likes > 2) {
                            append(" and ${post.likes - 2} others")
                        }

                        // Always show "see more" if there are any likes
                        append(" • ")
                        pushStringAnnotation(tag = "see_more", annotation = "see_more")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append("see more")
                        }
                        pop()
                    }

                    ClickableText(
                        text = annotatedString,
                        onClick = { offset ->
                            annotatedString.getStringAnnotations(
                                tag = "see_more",
                                start = offset,
                                end = offset
                            ).firstOrNull()?.let {
                                onSeeMoreLikes()
                            }
                        },
                        modifier = Modifier.padding(start = 4.dp)
                    )
                } else {
                    Text(
                        text = "0 likes",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }

    Divider(
        color = Color.LightGray,
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}