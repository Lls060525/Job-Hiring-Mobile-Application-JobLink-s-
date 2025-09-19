// components/ViewOnlyCommunityPost.kt
package com.example.madassignment.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madassignment.data.CommunityPost
import com.example.madassignment.utils.TimeUtils
import kotlinx.coroutines.delay
import java.util.Date
import java.util.concurrent.TimeUnit

@Composable
fun ViewOnlyCommunityPost(
    post: CommunityPost,
    modifier: Modifier = Modifier
) {
    // Calculate time ago dynamically from createdAt timestamp
    val timeAgo = TimeUtils.getTimeAgo(post.createdAt)

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row with author and time
            Text(
                text = "${post.author} • $timeAgo",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

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

            // Likes count
            Text(
                text = "${post.likes} likes",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }

    Divider(
        color = Color.LightGray,
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}