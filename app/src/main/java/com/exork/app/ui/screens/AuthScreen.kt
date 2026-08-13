package com.exork.app.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.exork.app.viewmodel.AuthViewModel
import com.exork.app.ui.theme.MonarchSlate

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleLegacySignInResult(result.data)
    }

    var showEmailDialog by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        if (user != null) {
            Log.d("AuthScreen", "User detected: ${user?.email}. Navigating to Home.")
            onLoginSuccess()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is com.exork.app.viewmodel.AuthUiEvent.ShowToast -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_LONG).show()
                }
                is com.exork.app.viewmodel.AuthUiEvent.LaunchLegacySignIn -> {
                    googleSignInLauncher.launch(viewModel.getGoogleSignInIntent(context))
                }
            }
        }
    }

    LaunchedEffect(error) {
        error?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MonarchSlate)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SYSTEM AUTHENTICATION",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            
            Text(
                text = "IDENTIFY YOURSELF, PLAYER",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                letterSpacing = 4.sp
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Button(
                onClick = {
                    Log.d("AuthScreen", "Google Auth Clicked")
                    viewModel.triggerGoogleSignIn()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                Text("CONTINUE WITH GOOGLE", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = {
                    viewModel.clearError()
                    showEmailDialog = true
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(8.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
            ) {
                Text("EMAIL AUTHENTICATION", color = Color.White)
            }

            error?.let {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }
    }

    if (showEmailDialog) {
        EmailAuthDialog(
            onDismiss = { 
                viewModel.clearError()
                showEmailDialog = false 
            },
            onAuth = { email, pass, isSignUp ->
                viewModel.signInWithEmail(email, pass, isSignUp) {
                    showEmailDialog = false
                }
            },
            onForgotPassword = { email -> viewModel.sendPasswordReset(email) },
            onTabSwitch = { viewModel.clearError() },
            isLoading = isLoading
        )
    }
}

@Composable
fun EmailAuthDialog(
    onDismiss: () -> Unit,
    onAuth: (String, String, Boolean) -> Unit,
    onForgotPassword: (String) -> Unit,
    onTabSwitch: () -> Unit,
    isLoading: Boolean
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MonarchSlate,
            modifier = Modifier.padding(16.dp).border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isSignUp) "NEW PLAYER REGISTRY" else "EXISTING PLAYER LOGIN",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("EMAIL ADDRESS") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("PASSWORD") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                if (!isSignUp) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(onClick = { onForgotPassword(email) }) {
                            Text(
                                "FORGOT PASSWORD?",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(if (isSignUp) 24.dp else 12.dp))
                
                Button(
                    onClick = { onAuth(email, password, isSignUp) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = email.isNotEmpty() && password.length >= 6 && !isLoading
                ) {
                    Text(if (isSignUp) "REGISTER" else "LOGIN")
                }
                
                TextButton(onClick = { 
                    onTabSwitch()
                    isSignUp = !isSignUp 
                }) {
                    Text(
                        text = if (isSignUp) "ALREADY A PLAYER? LOGIN" else "NEW PLAYER? REGISTER",
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
