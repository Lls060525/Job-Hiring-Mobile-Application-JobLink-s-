package com.example.madassignment.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "employer_jobs")
data class EmployerJobPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employerId: Int,
    val title: String,
    val description: String,
    val requirements: String,
    val location: String,
    val salaryRange: String,
    val jobType: String, // Full-time, Part-time, Contract, etc.
    val category: String,
    val applicationDeadline: Date? = null,
    val createdAt: Date = Date(),
    val isActive: Boolean = true,
    val applicants: String = "" // Comma-separated list of user IDs who applied
)