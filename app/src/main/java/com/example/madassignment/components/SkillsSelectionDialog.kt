package com.example.madassignment.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.madassignment.data.commonSkills
import com.example.madassignment.data.skillLevels
import com.example.madassignment.data.UserSkill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsSelectionDialog(
    currentSkills: List<UserSkill>,
    onDismiss: () -> Unit,
    onSkillsSelected: (List<UserSkill>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSkills by remember { mutableStateOf(currentSkills.toList()) }
    var selectedLevel by remember { mutableStateOf("Intermediate") }

    val filteredSkills = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            commonSkills
        } else {
            commonSkills.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp) // Fixed height but scrollable
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()) // Make it scrollable
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Skills",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search skills...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Skill Level Selection - SIMPLIFIED
                Text(
                    "Default Level:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    skillLevels.forEach { level ->
                        val isSelected = selectedLevel == level
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedLevel = level },
                            label = { Text(level) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Available Skills - FIXED HEIGHT to ensure visibility
                Text(
                    "Available Skills:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (filteredSkills.isEmpty()) {
                    Text("No skills found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .height(180.dp) // FIXED height for skills list
                            .fillMaxWidth()
                    ) {
                        items(filteredSkills) { skill ->
                            val isSelected = selectedSkills.any { it.name == skill }
                            SkillItem(
                                skill = skill,
                                isSelected = isSelected,
                                onSkillSelected = {
                                    if (isSelected) {
                                        selectedSkills = selectedSkills.filter { it.name != skill }
                                    } else {
                                        selectedSkills = selectedSkills + UserSkill(skill, selectedLevel)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selected Skills - LIMITED height
                if (selectedSkills.isNotEmpty()) {
                    Column {
                        Text(
                            "Selected (${selectedSkills.size}):",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(
                            modifier = Modifier
                                .height(80.dp) // LIMITED height
                                .fillMaxWidth()
                        ) {
                            items(selectedSkills) { skill ->
                                SkillChip(
                                    skill = skill,
                                    onRemove = {
                                        selectedSkills = selectedSkills.filter { it != skill }
                                    }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSkillsSelected(selectedSkills)
                            onDismiss()
                        },
                        enabled = selectedSkills.isNotEmpty()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
@Composable
fun SkillItem(
    skill: String,
    isSelected: Boolean,
    onSkillSelected: () -> Unit
) {
    Card(
        onClick = onSkillSelected,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = skill,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
            if (isSelected) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SkillChip(
    skill: UserSkill,
    onRemove: () -> Unit
) {
    InputChip(
        selected = false,
        onClick = {},
        enabled = true,
        label = {
            Text(
                "${skill.name} (${skill.level})",
                fontSize = 5.sp, // Smaller font
                maxLines = 1
            )
        },
        trailingIcon = {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(12.dp)
                )
            }
        },
        modifier = Modifier.height(28.dp) // Smaller chip height
    )
}