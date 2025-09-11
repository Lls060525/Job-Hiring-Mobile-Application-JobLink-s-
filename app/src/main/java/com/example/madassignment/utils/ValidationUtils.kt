package com.example.madassignment.utils

import android.util.Patterns

object ValidationUtils {
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun getEmailErrorMessage(email: String): String {
        return when {
            email.isBlank() -> "Email is required"
            !isValidEmail(email) -> "Please enter a valid email address"
            else -> ""
        }
    }

    fun getPasswordErrorMessage(password: String): String {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> ""
        }
    }

    fun getNameErrorMessage(name: String): String {
        return when {
            name.isBlank() -> "Name is required"
            name.length < 2 -> "Name must be at least 2 characters"
            else -> ""
        }
    }
}