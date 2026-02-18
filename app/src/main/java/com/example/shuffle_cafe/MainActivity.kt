package com.example.shuffle_cafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.shuffle_cafe.ui.theme.Shuffle_CafeTheme
import androidx.navigation.compose.composable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Shuffle_CafeTheme {
                AppNav()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNav(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(route = Screen.MainScreen.route) {
            MainScreen(navController = navController)
        }
        composable(route = Screen.MapScreen.route) {
            MapScreen(navController = navController)
        }
        composable(route = Screen.BookmarkScreen.route) {
            BookmarkScreen(navController = navController)
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    Scaffold(
        topBar = { TopSearchBar() },
        bottomBar = { BottomNavBar(navController) },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            PlaceCard()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavHostController) {
    Scaffold(
        topBar = { TopSearchBar() },
        bottomBar = {BottomNavBar(navController) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(navController: NavHostController) {
    Scaffold(
        topBar = { TopSearchBar() },
        bottomBar = {BottomNavBar(navController) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            PlaceSaved()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopSearchBar() {
    var text by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Hinted search text") },
            leadingIcon = { Icon(Icons.Filled.Menu, contentDescription = "Menu") },
            trailingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun PlaceSaved() {
    val outlineColor = Color(0xFFE6E6E6)
    val itemSpacing = 12.dp
    val sectionSpacing = 22.dp
    val shape = RoundedCornerShape(4.dp)

    @Composable
    fun Tile(
        modifier: Modifier = Modifier,
        showPlus: Boolean = false
    ) {
        OutlinedCard(
            modifier = modifier.aspectRatio(1f),
            shape = shape,
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, outlineColor)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (showPlus) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
        }
    }

    @Composable
    fun SectionTitle(title: String) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(1.dp)
                .background(outlineColor)
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(sectionSpacing)
    ) {
        // --- Saved ---
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Saved")
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                Tile(modifier = Modifier.weight(1f))
                Tile(modifier = Modifier.weight(1f))
                Tile(modifier = Modifier.weight(1f), showPlus = true)
            }
        }

        // --- Collection ---
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Collection")
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Tile(modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Good Coffee",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Tile(modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Quiet Area",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Tile(modifier = Modifier.fillMaxWidth(), showPlus = true)
                    Spacer(Modifier.height(6.dp))
                    // Keep height consistent with the other columns that have labels
                    Text(text = "", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // --- Study Plan ---
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Study Plan")
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                Tile(modifier = Modifier.weight(1f))
                Tile(modifier = Modifier.weight(1f))
                Tile(modifier = Modifier.weight(1f), showPlus = true)
            }
        }
    }
}


@Composable
private fun PlaceCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                // Put an image named "starbucks.jpg/png/webp" in:
                // app/src/main/res/drawable/
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Place photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // gradient at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0x80000000))
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "More like this",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        IconButton(onClick = { /* TODO share */ }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Starbucks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingStars(rating = 4.1f)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "4.1 (100 Reviews)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Fullerton",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun RatingStars(rating: Float) {
    val fullStars = rating.toInt().coerceIn(0, 5)
    Row {
        repeat(fullStars) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107))
        }
        repeat(5 - fullStars) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0x33FFC107))
        }
    }
}

@Composable
private fun BottomNavBar(navController: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = {navController.navigate(Screen.MainScreen.route) },
            icon = { Icon(Icons.Filled.Search, contentDescription = "Search") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Screen.MapScreen.route) },
            icon = { Icon(Icons.Filled.Place, contentDescription = "Map") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Screen.BookmarkScreen.route) },
            icon = { Icon(Icons.Filled.Bookmark, contentDescription = "Bookmarks") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    Shuffle_CafeTheme {
        AppNav()
    }
}
