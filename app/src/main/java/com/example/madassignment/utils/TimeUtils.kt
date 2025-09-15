// TimeUtils.kt
package com.example.madassignment.utils

import java.util.Date
import java.util.concurrent.TimeUnit

object TimeUtils {
    fun getTimeAgo(createdAt: Date): String {
        val now = Date()
        val diffInMillis = now.time - createdAt.time

        val seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hr ago"
            days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
            else -> {
                val weeks = days / 7
                if (weeks < 4) "$weeks week${if (weeks > 1) "s" else ""} ago"
                else {
                    val months = days / 30
                    if (months < 12) "$months month${if (months > 1) "s" else ""} ago"
                    else {
                        val years = days / 365
                        "$years year${if (years > 1) "s" else ""} ago"
                    }
                }
            }
        }
    }
}