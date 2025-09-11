package com.example.madassignment.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madassignment.data.Job
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun JobCard(
    job: Job,
    isSaved: Boolean = false,
    isApplied: Boolean = false,
    onClick: () -> Unit,
    onUnsave: (() -> Unit)? = null,
    onUnapply: (() -> Unit)? = null
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() }, // Handle regular click
                    onLongPress = {
                        // Handle long press based on job status
                        if (isSaved && onUnsave != null) {
                            onUnsave()
                        } else if (isApplied && onUnapply != null) {
                            onUnapply()
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with status icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // ADD THIS: Display company name if available
                    if (job.company.isNotBlank()) {
                        Text(
                            text = job.company,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Row {
                    if (isApplied) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Applied",
                            tint = Color.Green,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (isSaved) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Saved",
                            tint = Color.Blue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (job.subtitle.isNotEmpty()) {
                Text(
                    text = job.subtitle,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Text(
                text = job.type,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = job.location,
                fontSize = 14.sp,
            )

            Text(
                text = job.salary,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}