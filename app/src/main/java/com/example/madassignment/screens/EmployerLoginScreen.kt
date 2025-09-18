// EmployerLoginScreen.kt
package com.example.madassignment.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.madassignment.R
import com.example.madassignment.data.AuthState
import com.example.madassignment.data.EmployerViewModel
import com.example.madassignment.utils.ValidationUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployerLoginScreen(
    navController: NavController,
    employerViewModel: EmployerViewModel,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }
    val authState by employerViewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState() // Added scroll state

    // Validation states
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var companyNameError by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Welcome",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState) // Added vertical scroll
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // Changed to Top for better scrolling
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .height(200.dp)
                    .width(300.dp)
                    .padding(bottom = 32.dp)
            )

            Text(
                text = if (isLoginMode) "Employer Login" else "Create Employer Account",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (!isLoginMode) {
                OutlinedTextField(
                    value = companyName,
                    onValueChange = {
                        companyName = it
                        companyNameError = if (it.isBlank()) "Company name is required" else ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    label = { Text("Company Name *") },
                    leadingIcon = {
                        // Using text instead of icon
                        Text("🏢", modifier = Modifier.padding(start = 16.dp, end = 8.dp))
                    },
                    isError = companyNameError.isNotBlank(),
                    supportingText = {
                        if (companyNameError.isNotBlank()) {
                            Text(companyNameError)
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
                label = { Text("Email *") },
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
                label = { Text("Password *") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Password")
                },
                trailingIcon = {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
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
                        companyNameError = if (companyName.isBlank()) "Company name is required" else ""
                    }

                    val hasErrors = emailError.isNotBlank() ||
                            passwordError.isNotBlank() ||
                            (!isLoginMode && companyNameError.isNotBlank())

                    if (!hasErrors) {
                        scope.launch {
                            if (isLoginMode) {
                                employerViewModel.login(email, password).onSuccess {
                                    onLoginSuccess()
                                }.onFailure {
                                    snackbarHostState.showSnackbar("Login failed: ${it.message}")
                                }
                            } else {
                                employerViewModel.register(email, password, companyName).onSuccess {
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
                        (isLoginMode || companyName.isNotBlank())
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
                    companyNameError = ""
                    // Reset password visibility when switching modes
                    passwordVisible = false
                },
                modifier = Modifier.padding(top = 16.dp, bottom = 32.dp) // Added bottom padding
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