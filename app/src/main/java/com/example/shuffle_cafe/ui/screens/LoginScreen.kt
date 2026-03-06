package com.example.shuffle_cafe.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.shuffle_cafe.R
import com.example.shuffle_cafe.Screen
import com.example.shuffle_cafe.supabase
import com.example.shuffle_cafe.ui.theme.Shuffle_CafeTheme
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LoginScreen(navController: NavHostController) {

    // Login fields
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Signup dialog fields
    var showSignUpDialog by remember { mutableStateOf(false) }

    var signUpFirstName by remember { mutableStateOf("") }
    var signUpLastName by remember { mutableStateOf("") }
    var signUpEmail by remember { mutableStateOf("") }
    var signUpPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val pagerState = rememberPagerState { 2 }
    val coroutineScope = rememberCoroutineScope()

    Scaffold { innerPadding ->

        VerticalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            userScrollEnabled = false
        ) { page ->

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                if (page == 0) {

                    // Welcome Page
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Image(
                            painter = painterResource(id = R.drawable.shuffle_cafe_logo),
                            contentDescription = "Shuffle Cafe Logo",
                            modifier = Modifier.size(150.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "Welcome to Shuffle Cafe!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(64.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                        page = 1,
                                        animationSpec = tween(
                                            durationMillis = 1000,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Continue", color = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Scroll Down",
                                    tint = Color.Black
                                )
                            }
                        }
                    }

                } else {

                    // Login Page
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Text(
                            text = "Login",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Email") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        supabase.auth.signInWith(Email) {
                                            this.email = username
                                            this.password = password
                                        }

                                        navController.navigate(Screen.MainScreen.route) {
                                            popUpTo(navController.graph.id) { inclusive = true }
                                        }

                                    } catch (e: Exception) {
                                        errorMessage = e.message
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Login", color = Color.Black)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { showSignUpDialog = true }
                        ) {
                            Text("Create Account")
                        }

                        errorMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = it, color = Color.Red)
                        }
                    }
                }
            }
        }
    }

    // Sign up popup
    if (showSignUpDialog) {
        AlertDialog(
            onDismissRequest = { showSignUpDialog = false },
            title = { Text("Create Account") },
            text = {
                Column {
                    OutlinedTextField(
                        value = signUpFirstName,
                        onValueChange = { signUpFirstName = it },
                        label = { Text("First Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = signUpLastName,
                        onValueChange = { signUpLastName = it },
                        label = { Text("Last Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = signUpEmail,
                        onValueChange = { signUpEmail = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = signUpPassword,
                        onValueChange = { signUpPassword = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {

                        if (signUpPassword != confirmPassword) {
                            errorMessage = "Passwords do not match"
                            return@TextButton
                        }

                        coroutineScope.launch {
                            try {
                                supabase.auth.signUpWith(Email) {
                                    this.email = signUpEmail
                                    this.password = signUpPassword
                                }

                                val user = supabase.auth.currentUserOrNull()

                                user?.let {
                                    supabase.from("profiles").insert(
                                        mapOf(
                                            "id" to it.id,
                                            "first_name" to signUpFirstName,
                                            "last_name" to signUpLastName,
                                            "avatar_url" to null,
                                            "bio" to null
                                        )
                                    )
                                }

                                showSignUpDialog = false

                                navController.navigate(Screen.MainScreen.route) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }

                            } catch (e: Exception) {
                                errorMessage = e.message
                            }
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSignUpDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    Shuffle_CafeTheme {
        LoginScreen(rememberNavController())
    }
}