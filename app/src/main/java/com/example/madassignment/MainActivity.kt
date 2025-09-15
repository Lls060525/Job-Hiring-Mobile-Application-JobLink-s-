package com.example.madassignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.madassignment.data.JobViewModel
import com.example.madassignment.navigation.JobApp
import com.example.madassignment.ui.theme.MadAssignmentTheme
import com.google.firebase.Firebase
import com.google.firebase.initialize

// MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase
        Firebase.initialize(this)

        setContent {
            MadAssignmentTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val jobViewModel: JobViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return JobViewModel(applicationContext) as T
                            }
                        }
                    )

                    // Initial sync when app starts
//                    LaunchedEffect(Unit) {
//                        jobViewModel.syncData()
//                    }

                    JobApp(jobViewModel = jobViewModel)
                }
            }
        }
    }
}