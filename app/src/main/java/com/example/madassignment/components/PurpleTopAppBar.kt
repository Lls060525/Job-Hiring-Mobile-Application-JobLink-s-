// components/PurpleTopAppBar.kt
package com.example.madassignment.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurpleTopAppBar(
    title: String,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            androidx.compose.material3.Text(
                text = title,
                color = Color.White
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF6A0DAD), // Purple color
            titleContentColor = Color.White
        ),
        modifier = modifier.height(100.dp)
    )
}