package com.example.madassignment.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EmployerViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmployerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmployerViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}