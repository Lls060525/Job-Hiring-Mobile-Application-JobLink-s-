// FilterChip.kt - Fixed version
package com.example.madassignment.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.small,
        border = if (!selected) {
            ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.dp
            )
        } else {
            null
        },
        modifier = Modifier
            .height(36.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            label()
        }
    }
}