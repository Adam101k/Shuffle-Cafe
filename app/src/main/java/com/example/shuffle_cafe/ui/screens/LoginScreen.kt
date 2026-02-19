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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.shuffle_cafe.R
import com.example.shuffle_cafe.Screen
import com.example.shuffle_cafe.ui.theme.Shuffle_CafeTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val pagerState = rememberPagerState { 2 }
    val coroutineScope = rememberCoroutineScope()

    // Removed: val loginSectionOffset by remember { mutableIntStateOf(0) }

    Scaffold(
        // containerColor = Color.Transparent // Removed: No longer needed with solid background
    ) { innerPadding ->
        VerticalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White) // Changed: Solid white background
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            userScrollEnabled = false // We want to control scroll only with the button
        ) { page ->
            // Use a key for each page to help compose with managing state
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (page == 0) {
                    // --- Welcome Page ---
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
                            modifier = Modifier
                                .size(150.dp) // Adjust size as needed for your new logo
                                .align(Alignment.CenterHorizontally)
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
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Scroll Down", tint = Color.Black)
                            }
                        }
                    }
                } else {
                    // --- Login Page ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp) // Removed: .onGloballyPositioned { ... }
                            ,
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
                            label = { Text("Username", color = Color.Black) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password", color = Color.Black) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                navController.navigate(Screen.MainScreen.route) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Login", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    Shuffle_CafeTheme {
        LoginScreen(rememberNavController())
    }
}
