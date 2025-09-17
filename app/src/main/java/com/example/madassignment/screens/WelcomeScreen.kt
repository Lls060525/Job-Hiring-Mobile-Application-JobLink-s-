// WelcomeScreen.kt
package com.example.madassignment.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.madassignment.R


@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Admin login button at top right
        // We'll use a Row with a Spacer to push it to the top end
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = { navController.navigate("adminLogin") },
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Admin Login",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // Add space between the icon and the logo

        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .height(180.dp)
                .width(280.dp)
                .padding(bottom = 32.dp)
        )

        // "Looking for a job?" button
        Button(
            onClick = {
                navController.navigate("auth")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Looking for a job?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // "Hiring candidates?" button
        Button(
            onClick = {
                navController.navigate("employer_auth")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.LightGray,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Hiring candidates?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}