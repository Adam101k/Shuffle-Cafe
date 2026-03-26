package com.example.shuffle_cafe.ui.screens

import android.graphics.Matrix
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.shuffle_cafe.R
import com.example.shuffle_cafe.Screen
import com.example.shuffle_cafe.UserPreferences
import com.example.shuffle_cafe.supabase
import com.example.shuffle_cafe.ui.theme.Shuffle_CafeTheme
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.io.IOException
import kotlinx.coroutines.launch

private enum class SupabaseAction {
    Login,
    SignUp,
    ProfileSetup
}

private fun Throwable.toUserFacingMessage(action: SupabaseAction): String {
    return when (this) {
        is AuthWeakPasswordException -> {
            val details = reasons.joinToString(separator = ", ").ifBlank { "Try a longer password with a mix of letters, numbers, and symbols." }
            "That password is too weak. $details"
        }
        is AuthRestException -> when (errorCode) {
            AuthErrorCode.InvalidCredentials -> "That email or password does not match our records."
            AuthErrorCode.EmailNotConfirmed -> "Your account exists, but the email address still needs to be confirmed."
            AuthErrorCode.EmailExists, AuthErrorCode.UserAlreadyExists -> "An account with that email already exists. Try logging in instead."
            AuthErrorCode.SignupDisabled, AuthErrorCode.EmailProviderDisabled -> "Email sign-in is currently disabled in Supabase."
            AuthErrorCode.EmailAddressInvalid -> "Please enter a valid email address."
            AuthErrorCode.OverRequestRateLimit, AuthErrorCode.OverEmailSendRateLimit -> "Too many attempts were made. Please wait a moment and try again."
            AuthErrorCode.RequestTimeout -> "Supabase took too long to respond. Please try again."
            AuthErrorCode.UserBanned -> "This account has been disabled."
            AuthErrorCode.SessionExpired,
            AuthErrorCode.SessionNotFound,
            AuthErrorCode.RefreshTokenAlreadyUsed,
            AuthErrorCode.RefreshTokenNotFound -> "Your session is no longer valid. Please sign in again."
            else -> errorDescription.ifBlankOrNull()
                ?: when (action) {
                    SupabaseAction.Login -> "Supabase could not sign you in right now."
                    SupabaseAction.SignUp -> "Supabase could not create your account right now."
                    SupabaseAction.ProfileSetup -> "Your account was created, but the profile setup step failed."
                }
        }
        is HttpRequestTimeoutException -> "The request timed out. Check your connection and make sure the Supabase project is active."
        is HttpRequestException, is IOException -> "The app could not reach Supabase. Check your internet connection, then verify the Supabase project is still active."
        is RestException -> when {
            statusCode == 401 || statusCode == 403 -> "Supabase rejected this request. If login worked, this usually means a table policy or permissions issue."
            statusCode == 404 -> "The Supabase project or endpoint could not be found. Double-check the project URL and API key."
            statusCode >= 500 -> "Supabase is having a server problem right now. Please try again shortly."
            else -> description.ifBlankOrNull() ?: "Supabase returned an unexpected error."
        }
        else -> message.ifBlankOrNull()
            ?: when (action) {
                SupabaseAction.Login -> "Something went wrong while signing in."
                SupabaseAction.SignUp -> "Something went wrong while creating your account."
                SupabaseAction.ProfileSetup -> "Your account was created, but the profile setup step failed."
            }
    }
}

private fun String?.ifBlankOrNull(): String? = this?.takeIf { it.isNotBlank() }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    val pageBackgroundColor = Color(0xFFC79A87)
    val primaryButtonColor = Color(0xFF4A231C)
    val playfairDisplay = FontFamily(Font(R.font.playfair_display))
    val defaultTextStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = playfairDisplay)
    val welcomeTitleStyle = MaterialTheme.typography.headlineSmall.copy(
        fontFamily = playfairDisplay,
        fontSize = 40.sp,
        lineHeight = 44.sp
    )
    val subtitleStyle = MaterialTheme.typography.bodyMedium.copy(
        fontFamily = playfairDisplay,
        fontSize = 20.sp,
        lineHeight = 28.sp
    )
    val loginTitleStyle = MaterialTheme.typography.headlineMedium.copy(fontFamily = playfairDisplay)

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
    var isSubmitting by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState { 2 }
    val coroutineScope = rememberCoroutineScope()
    val loginFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = primaryButtonColor,
        unfocusedTextColor = primaryButtonColor,
        focusedLabelColor = primaryButtonColor,
        unfocusedLabelColor = primaryButtonColor,
        focusedBorderColor = primaryButtonColor,
        unfocusedBorderColor = primaryButtonColor,
        cursorColor = primaryButtonColor
    )

    CompositionLocalProvider(LocalTextStyle provides defaultTextStyle) {
        Scaffold(containerColor = pageBackgroundColor) { innerPadding ->

            VerticalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
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
                        Box(modifier = Modifier.fillMaxSize()) {
                            WelcomeVideoBackground(modifier = Modifier.matchParentSize())
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.White.copy(alpha = 0.28f))
                            )

                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.shuffle_cafe_logo),
                                    contentDescription = "Shuffle Cafe Logo",
                                    modifier = Modifier.size(150.dp)
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                Text(
                                    text = "Shuffel Cafe:",
                                    style = welcomeTitleStyle,
                                    color = Color.Black

                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Find your space.\nDon't settle for the closest.\nFit how you study, work, and hang out.",
                                    style = subtitleStyle,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 16.dp)
                                )
                            }

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
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 32.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryButtonColor,
                                    contentColor = Color.White
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Get started!", color = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Scroll Down",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    } else {

                        // Login Page
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(pageBackgroundColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {

                                Text(
                                    text = "Login",
                                    style = loginTitleStyle,
                                    color = primaryButtonColor
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                OutlinedTextField(
                                    value = username,
                                    onValueChange = { username = it },
                                    label = { Text("Email") },
                                    textStyle = defaultTextStyle,
                                    colors = loginFieldColors,
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Password") },
                                    textStyle = defaultTextStyle,
                                    colors = loginFieldColors,
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                Button(
                                    enabled = !isSubmitting,
                                    onClick = {
                                        errorMessage = null

                                        if (username.isBlank() || password.isBlank()) {
                                            errorMessage = "Enter both your email and password."
                                            return@Button
                                        }

                                        coroutineScope.launch {
                                            isSubmitting = true
                                            try {
                                                supabase.auth.signInWith(Email) {
                                                    this.email = username
                                                    this.password = password
                                                }

                                                val user = supabase.auth.currentUserOrNull()

                                                if (user != null) {
                                                    val prefs = supabase
                                                        .from("user_preferences")
                                                        .select {
                                                            filter { eq("user_id", user.id) }
                                                        }
                                                        .decodeSingleOrNull<UserPreferences>()

                                                    if (prefs == null) {
                                                        // No preferences → go to setup
                                                        navController.navigate(Screen.Preferences.route) {
                                                            popUpTo(navController.graph.id) { inclusive = true }
                                                        }
                                                    } else {
                                                        // Has preferences → go to main
                                                        navController.navigate(Screen.MainScreen.route) {
                                                            popUpTo(navController.graph.id) { inclusive = true }
                                                        }
                                                    }
                                                }

                                            } catch (e: Exception) {
                                                errorMessage = e.toUserFacingMessage(SupabaseAction.Login)
                                            } finally {
                                                isSubmitting = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryButtonColor,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(if (isSubmitting) "Signing in..." else "Login", color = Color.White)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                TextButton(
                                    onClick = { showSignUpDialog = true }
                                ) {
                                    Text("Create Account")
                                }

                                errorMessage?.let {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = it,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }

                            Button(
                                enabled = !isSubmitting,
                                onClick = {
                                    errorMessage = null
                                    navController.navigate(Screen.MainScreen.route) {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 32.dp)
                                    .widthIn(min = 180.dp, max = 220.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryButtonColor,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Continue as Guest", color = Color.White)
                            }
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
                    enabled = !isSubmitting,
                    onClick = {
                        errorMessage = null

                        if (
                            signUpFirstName.isBlank() ||
                            signUpLastName.isBlank() ||
                            signUpEmail.isBlank() ||
                            signUpPassword.isBlank() ||
                            confirmPassword.isBlank()
                        ) {
                            errorMessage = "Fill out all sign-up fields before creating an account."
                            return@TextButton
                        }

                        if (signUpPassword != confirmPassword) {
                            errorMessage = "Passwords do not match"
                            return@TextButton
                        }

                        coroutineScope.launch {
                            isSubmitting = true
                            try {
                                supabase.auth.signUpWith(Email) {
                                    this.email = signUpEmail
                                    this.password = signUpPassword
                                }

                                val user = supabase.auth.currentUserOrNull()

                                user?.let {
                                    try {
                                        supabase.from("profiles").insert(
                                            mapOf(
                                                "id" to it.id,
                                                "first_name" to signUpFirstName,
                                                "last_name" to signUpLastName,
                                                "avatar_url" to null,
                                                "bio" to null
                                            )
                                        )
                                    } catch (profileError: Exception) {
                                        errorMessage = profileError.toUserFacingMessage(SupabaseAction.ProfileSetup)
                                    }
                                }

                                showSignUpDialog = false

                                if (user != null) {
                                    navController.navigate(Screen.Preferences.route) {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                    }
                                } else {
                                    errorMessage = "Check your email for a confirmation link, then sign in once your account is verified."
                                }

                            } catch (e: Exception) {
                                errorMessage = e.toUserFacingMessage(SupabaseAction.SignUp)
                            } finally {
                                isSubmitting = false
                            }
                        }
                    }
                ) {
                    Text(if (isSubmitting) "Creating..." else "Create")
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

@Composable
private fun WelcomeVideoBackground(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            TextureView(viewContext).apply {
                var surface: Surface? = null
                var mediaPlayer: MediaPlayer? = null
                var videoWidth = 0
                var videoHeight = 0

                fun applyCenterCrop() {
                    if (width == 0 || height == 0 || videoWidth == 0 || videoHeight == 0) return

                    val viewWidth = width.toFloat()
                    val viewHeight = height.toFloat()
                    val videoAspectRatio = videoWidth.toFloat() / videoHeight.toFloat()
                    val viewAspectRatio = viewWidth / viewHeight

                    val scaleX: Float
                    val scaleY: Float
                    if (videoAspectRatio > viewAspectRatio) {
                        scaleX = videoAspectRatio / viewAspectRatio
                        scaleY = 1f
                    } else {
                        scaleX = 1f
                        scaleY = viewAspectRatio / videoAspectRatio
                    }

                    val transform = Matrix().apply {
                        setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
                    }
                    setTransform(transform)
                }

                addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                    applyCenterCrop()
                }

                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(surfaceTexture: android.graphics.SurfaceTexture, width: Int, height: Int) {
                        surface = Surface(surfaceTexture)
                        mediaPlayer = MediaPlayer().apply {
                            setSurface(surface)
                            isLooping = true
                            setVolume(0f, 0f)
                            setDataSource(
                                context,
                                Uri.parse("android.resource://${context.packageName}/${R.raw.shuffel_cafe_welcome_page_video}")
                            )
                            setOnVideoSizeChangedListener { _, widthPx, heightPx ->
                                videoWidth = widthPx
                                videoHeight = heightPx
                                applyCenterCrop()
                            }
                            setOnPreparedListener { player ->
                                videoWidth = player.videoWidth
                                videoHeight = player.videoHeight
                                applyCenterCrop()
                                player.start()
                            }
                            prepareAsync()
                        }
                    }

                    override fun onSurfaceTextureSizeChanged(surfaceTexture: android.graphics.SurfaceTexture, width: Int, height: Int) {
                        applyCenterCrop()
                    }

                    override fun onSurfaceTextureDestroyed(surfaceTexture: android.graphics.SurfaceTexture): Boolean {
                        mediaPlayer?.run {
                            runCatching { stop() }
                            runCatching { reset() }
                            release()
                        }
                        mediaPlayer = null
                        surface?.release()
                        surface = null
                        return true
                    }

                    override fun onSurfaceTextureUpdated(surfaceTexture: android.graphics.SurfaceTexture) = Unit
                }
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    Shuffle_CafeTheme {
        LoginScreen(rememberNavController())
    }
}