package com.example.madassignment.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.madassignment.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.madassignment.data.AuthState
import com.example.madassignment.data.JobViewModel
import com.example.madassignment.utils.ValidationUtils
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    jobViewModel: JobViewModel,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) } // Add this line
    val authState by jobViewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Validation states
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf("") }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.height(200.dp).width(300.dp)
            )

            Text(
                text = if (isLoginMode) "Welcome Back" else "Create Account",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (!isLoginMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = ValidationUtils.getNameErrorMessage(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    label = { Text("Full Name") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = "Name")
                    },
                    isError = nameError.isNotBlank(),
                    supportingText = {
                        if (nameError.isNotBlank()) {
                            Text(nameError)
                        }
                    },
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = ValidationUtils.getEmailErrorMessage(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                label = { Text("Email") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = "Email")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError.isNotBlank(),
                supportingText = {
                    if (emailError.isNotBlank()) {
                        Text(emailError)
                    }
                },
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = ValidationUtils.getPasswordErrorMessage(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                label = { Text("Password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Password")
                },
                // Add visibility toggle functionality
                trailingIcon = {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible }
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Info
                            else Icons.Default.Info,
                            contentDescription = if (passwordVisible) "Hide password"
                            else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = passwordError.isNotBlank(),
                supportingText = {
                    if (passwordError.isNotBlank()) {
                        Text(passwordError)
                    }
                },
                singleLine = true
            )

            Button(
                onClick = {
                    // Validate fields before proceeding
                    emailError = ValidationUtils.getEmailErrorMessage(email)
                    passwordError = ValidationUtils.getPasswordErrorMessage(password)

                    if (!isLoginMode) {
                        nameError = ValidationUtils.getNameErrorMessage(name)
                    }

                    val hasErrors = emailError.isNotBlank() ||
                            passwordError.isNotBlank() ||
                            (!isLoginMode && nameError.isNotBlank())

                    if (!hasErrors) {
                        scope.launch {
                            if (isLoginMode) {
                                jobViewModel.login(email, password).onSuccess {
                                    println("DEBUG: Login successful, calling onLoginSuccess")
                                    onLoginSuccess()
                                }.onFailure {
                                    snackbarHostState.showSnackbar("Login failed: ${it.message}")
                                }
                            } else {
                                jobViewModel.register(email, password, name).onSuccess {
                                    println("DEBUG: Registration successful, calling onLoginSuccess")
                                    onLoginSuccess()
                                }.onFailure {
                                    snackbarHostState.showSnackbar("Registration failed: ${it.message}")
                                }
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please fix the errors above")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = email.isNotBlank() && password.isNotBlank() &&
                        (isLoginMode || name.isNotBlank())
            ) {
                if (authState == AuthState.LOADING) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (isLoginMode) "Sign In" else "Create Account", fontSize = 16.sp)
                }
            }

            TextButton(
                onClick = {
                    isLoginMode = !isLoginMode
                    // Clear errors when switching modes
                    emailError = ""
                    passwordError = ""
                    nameError = ""
                    // Reset password visibility when switching modes
                    passwordVisible = false
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = if (isLoginMode) "Don't have an account? Sign up"
                    else "Already have an account? Sign in",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}