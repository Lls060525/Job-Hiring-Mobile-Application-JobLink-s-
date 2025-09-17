package com.example.madassignment.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "employee_jobs")
data class EmployeeJobPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val jobId: Int,
    val appliedDate: Date = Date(),
    val status: String = "Applied" // Applied, Interviewing, Rejected, Hired
)