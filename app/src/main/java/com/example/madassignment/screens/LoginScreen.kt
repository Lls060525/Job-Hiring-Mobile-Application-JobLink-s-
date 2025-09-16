package com.example.madassignment.screens

import android.annotation.SuppressLint
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
import androidx.compose.ui.graphics.Color
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

@SuppressLint("UnrememberedMutableState")
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
    var passwordVisible by remember { mutableStateOf(false) }
    val authState by jobViewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Track if admin email is detected
    val isAdminEmail by derivedStateOf {
        email.equals("admin@gmail.com", ignoreCase = true)
    }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var forgotPasswordName by remember { mutableStateOf("") }
    var forgotPasswordMessage by remember { mutableStateOf<String?>(null) }
    var retrievedPassword by remember { mutableStateOf<String?>(null) }

    // Validation states
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf("") }
    var emailExistsError by remember { mutableStateOf(false) }

    // Function to check if email already exists
    fun checkEmailExists(email: String) {
        if (!isLoginMode && email.isNotBlank() && ValidationUtils.isValidEmail(email)) {
            scope.launch {
                val existingUser = jobViewModel.currentUser.value?.let {
                    // Use the repository to check if email exists
                    // You'll need to add a method to check email existence
                    // For now, we'll handle this in the registration flow
                    null
                }
                emailExistsError = existingUser != null
                if (emailExistsError) {
                    emailError = "Email already registered"
                }
            }
        }
    }

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
                    emailExistsError = false // Reset email exists error when typing

                    // Check if email exists in real-time (optional)
                    if (!isLoginMode && it.isNotBlank()) {
                        checkEmailExists(it)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                label = { Text("Email") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = "Email")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError.isNotBlank() || emailExistsError,
                supportingText = {
                    if (emailError.isNotBlank()) {
                        Text(emailError)
                    } else if (emailExistsError) {
                        Text("Email already registered. Please use a different email or sign in.")
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

            // Show admin detection message
            if (isLoginMode && isAdminEmail) {
                Text(
                    text = "Admin account detected",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

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
                            (!isLoginMode && nameError.isNotBlank()) ||
                            emailExistsError

                    if (!hasErrors) {
                        scope.launch {
                            if (isLoginMode) {
                                // AUTO-DETECT ADMIN LOGIN
                                if (isAdminEmail && password == "admin123") {
                                    // ADMIN LOGIN
                                    jobViewModel.loginAdmin(email, password).onSuccess {
                                        println("DEBUG: Admin login successful, calling onLoginSuccess")
                                        onLoginSuccess()
                                    }.onFailure {
                                        snackbarHostState.showSnackbar("Admin login failed: ${it.message}")
                                    }
                                } else {
                                    // REGULAR USER LOGIN
                                    jobViewModel.login(email, password).onSuccess {
                                        println("DEBUG: Login successful, calling onLoginSuccess")
                                        onLoginSuccess()
                                    }.onFailure {
                                        snackbarHostState.showSnackbar("Login failed: ${it.message}")
                                    }
                                }
                            } else {
                                // REGISTRATION - Prevent admin registration and duplicate emails
                                if (isAdminEmail) {
                                    snackbarHostState.showSnackbar("Cannot register admin account")
                                    return@launch
                                }

                                // Additional check for duplicate email
                                val emailExists = jobViewModel.checkEmailExists(email) // Call the correct ViewModel method
                                if (emailExists) { // Check if the boolean is 'true'
                                    emailExistsError = true
                                    snackbarHostState.showSnackbar("Email already registered. Please use a different email.")
                                    return@launch
                                }

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
                        (isLoginMode || name.isNotBlank()) &&
                        !emailExistsError
            ) {
                if (authState == AuthState.LOADING) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(
                        if (isLoginMode) {
                            if (isAdminEmail) "Sign In as Admin" else "Sign In"
                        } else {
                            "Create Account"
                        },
                        fontSize = 16.sp
                    )
                }
            }

            TextButton(
                onClick = {
                    isLoginMode = !isLoginMode
                    // Clear errors when switching modes
                    emailError = ""
                    passwordError = ""
                    nameError = ""
                    emailExistsError = false
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

            if (isLoginMode) {
                TextButton(
                    onClick = {
                        showForgotPasswordDialog = true
                        retrievedPassword = null // Reset when opening dialog
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "Forgot Password?",
                        color = Color.Red
                    )
                }
            }

            // Forgot Password Dialog
            if (showForgotPasswordDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showForgotPasswordDialog = false
                        forgotPasswordMessage = null
                        forgotPasswordEmail = ""
                        forgotPasswordName = ""
                        retrievedPassword = null
                    },
                    title = {
                        Text(if (retrievedPassword != null) "Password Retrieved" else "Retrieve Password")
                    },
                    text = {
                        Column {
                            if (retrievedPassword != null) {
                                // Show the retrieved password
                                Text(
                                    text = "Your password has been retrieved successfully!",
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = "Password:",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = retrievedPassword ?: "",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Please keep your password secure.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            } else if (forgotPasswordMessage != null) {
                                // Show error message
                                Text(
                                    text = forgotPasswordMessage!!,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }

                            if (retrievedPassword == null) {
                                // Only show input fields if password hasn't been retrieved yet
                                OutlinedTextField(
                                    value = forgotPasswordEmail,
                                    onValueChange = { forgotPasswordEmail = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    label = { Text("Email Address") },
                                    placeholder = { Text("Enter your registered email") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = forgotPasswordName,
                                    onValueChange = { forgotPasswordName = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    label = { Text("Full Name") },
                                    placeholder = { Text("Enter your full name as registered") },
                                    singleLine = true
                                )
                            }
                        }
                    },
                    confirmButton = {
                        if (retrievedPassword != null) {
                            // Show "OK" button after password is retrieved
                            Button(
                                onClick = {
                                    showForgotPasswordDialog = false
                                    retrievedPassword = null
                                    forgotPasswordEmail = ""
                                    forgotPasswordName = ""
                                }
                            ) {
                                Text("OK")
                            }
                        } else {
                            // Show "Retrieve Password" button
                            Button(
                                onClick = {
                                    scope.launch {
                                        jobViewModel.retrievePassword(forgotPasswordEmail, forgotPasswordName)
                                            .onSuccess { password ->
                                                retrievedPassword = password
                                                forgotPasswordMessage = null
                                            }
                                            .onFailure { e ->
                                                forgotPasswordMessage = "Error: ${e.message}"
                                                retrievedPassword = null
                                            }
                                    }
                                },
                                enabled = forgotPasswordEmail.isNotBlank() && forgotPasswordName.isNotBlank()
                            ) {
                                Text("Retrieve Password")
                            }
                        }
                    },
                    dismissButton = {
                        if (retrievedPassword == null) {
                            // Show cancel button only when not showing retrieved password
                            TextButton(
                                onClick = {
                                    showForgotPasswordDialog = false
                                    forgotPasswordMessage = null
                                    forgotPasswordEmail = ""
                                    forgotPasswordName = ""
                                    retrievedPassword = null
                                }
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                )
            }
        }
    }
}

