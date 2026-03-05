package com.example.shuffle_cafe

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // Added import for clickable
import androidx.compose.foundation.layout.* 
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.shuffle_cafe.ui.theme.Shuffle_CafeTheme
import androidx.navigation.compose.composable
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.CameraPositionState // Added import for CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.runtime.LaunchedEffect
import com.google.android.gms.maps.model.LatLng
import android.location.Location
import android.provider.ContactsContract
import androidx.compose.material.icons.filled.Photo
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import coil.compose.AsyncImage
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import com.example.shuffle_cafe.ui.screens.LoginScreen // Moved import to top
// Removed: import com.example.shuffle_cafe.ui.screens.BookmarkScreen // Removed incorrect import
// Removed: import com.example.shuffle_cafe.ui.screens.ProfileScreen
// Removed: import com.example.shuffle_cafe.ui.screens.MapScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.Storage


val supabase = createSupabaseClient(
    supabaseUrl = "https://sknyfkgltazosjmyjfhs.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNrbnlma2dsdGF6b3NqbXlqZmhzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzEzNjgzNTMsImV4cCI6MjA4Njk0NDM1M30.p3dCwqQz8iQnRsFw1VkllaWv2BGkc3K-ugqHfxaMiPE"
) {
    install(Auth)
    install(Postgrest)
    install(Storage)
}

data class Cafe(
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val status: String, // "Busy" / "Quiet" / "Closed" etc.
    val hours: LinkedHashMap<String, String>,
    val features: List<String>,
    val ambience: List<String>,
    val imageResId: Int = R.drawable.ic_launcher_foreground, // fallback local image
    val imageUrl: String? = null
)

object CafeRepository {
    // Testing data. Replace with backend data later
    val cafes: List<Cafe> = listOf(
        Cafe(
            id = "1",
            name = "Cafe Name",
            address = "123 something ave",
            phone = "(123) 123-1234",
            status = "Busy/Quiet/Closed",
            hours = linkedMapOf(
                "Thursday" to "7:00AM - 5:00 PM",
                "Friday" to "7:00AM - 5:00 PM",
                "Saturday" to "8:00AM - 4:00 PM",
                "Sunday" to "8:00AM - 2:00 PM"
            ),
            features = listOf(
                "Parking availability",
                "Bike parking",
                "ADA accessibility",
                "Restroom availability",
                "Bathroom password",
                "Wifi Availability/Quality"
            ),
            ambience = listOf("Quiet", "Bright"),
            imageResId = R.drawable.ic_launcher_foreground
        ),
        Cafe(
            id = "2",
            name = "Klatch Coffee",
            address = "13855 City Center Dr #3015",
            phone = "(555) 555-5555",
            status = "Quiet",
            hours = linkedMapOf(
                "Thursday" to "6:30AM - 6:00 PM",
                "Friday" to "6:30AM - 6:00 PM"
            ),
            features = listOf("Parking availability", "Wifi Availability/Quality"),
            ambience = listOf("Quiet"),
            imageResId = R.drawable.ic_launcher_foreground
        )
    )

    fun getCafe(id: String): Cafe? = cafes.firstOrNull { it.id == id }
}

object ReviewRepository {
    private val reviewsByCafe = mutableStateMapOf<String, SnapshotStateList<String>>()

    fun reviewsFor(cafeId: String): SnapshotStateList<String> =
        reviewsByCafe.getOrPut(cafeId) { mutableStateListOf() }

    fun addReview(cafeId: String, review: String) {
        reviewsFor(cafeId).add(review)
    }
}

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            // You can handle result here if needed
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyC7QTmdJE2fnRXMiKWrMZftkXIG20gNWrA")
        }
        requestPermissionLauncher.launch(
            Manifest.permission.ACCESS_FINE_LOCATION
        )

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
    // Placeholder for login status. In a real app, this would come from a data source.
    //val currentUser = supabase.auth.currentUserOrNull()
    //val isUserLoggedIn = currentUser != null

    val isUserLoggedIn by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = if (isUserLoggedIn) Screen.MainScreen.route else Screen.LoginScreen.route
    ) {
        composable(route = Screen.LoginScreen.route) {
            LoginScreen(navController = navController)
        }
        composable(route = Screen.MainScreen.route) {
            MainScreen(navController = navController)
        }
        composable(route = Screen.MapScreen.route) {
            MapScreen(navController = navController)
        }
        composable(route = Screen.BookmarkScreen.route) {
            BookmarkScreen(navController = navController)
        }
        composable(route = Screen.ProfileScreen.route) {
            ProfileScreen(navController)
        }

        composable(
            route = Screen.CafeDetails.route,
            arguments = listOf(navArgument("cafeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cafeId = backStackEntry.arguments?.getString("cafeId") ?: return@composable
            CafeDetailsScreen(navController = navController, cafeId = cafeId)
        }

        composable(
            route = Screen.WriteReview.route,
            arguments = listOf(navArgument("cafeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cafeId = backStackEntry.arguments?.getString("cafeId") ?: return@composable
            WriteReviewScreen(navController = navController, cafeId = cafeId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val cafe = CafeRepository.cafes.first() // sample

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
            PlaceCard(navController = navController, cafe = cafe)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavHostController) {
    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val defaultCamera = rememberCameraPositionState()

    LaunchedEffect(hasLocationPermission){
        if(hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location -> location?.let{
                val userLatlng = LatLng(it.latitude, it.longitude)

                defaultCamera.move(CameraUpdateFactory.newLatLngZoom(userLatlng, 17f)
                )
            }
            }
        }
    }
    Scaffold(
        topBar = {  MapSearchBar(defaultCamera) {latLng -> /* destination = latLng */ }  }, // Removed 'destination = latLng' as it's an unresolved reference
        bottomBar = {BottomNavBar(navController) },
    ) { innerPadding ->
        GoogleMap(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            cameraPositionState = defaultCamera,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = true
            )
        )
        {

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
fun TopSearchBar() {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSearchBar(cameraPosition: CameraPositionState, onLocationFound: (LatLng) -> Unit) { // Removed private
    val context = LocalContext.current
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
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onDone = {
                    val placesClient = Places.createClient(context)
                }
            ),
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
fun CafeDetailsScreen(navController: NavHostController, cafeId: String) {
    val cafe = remember(cafeId) { CafeRepository.getCafe(cafeId) } // When we have a real cafeID from backend, plug in here
    val reviews = ReviewRepository.reviewsFor(cafeId)

    Scaffold(
        bottomBar = { BottomNavBar(navController) },
        containerColor = Color.White
    ) { innerPadding ->

        if (cafe == null) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Cafe not found")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(
                    text = cafe.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            item { Text("Address: ${cafe.address}", style = MaterialTheme.typography.bodyMedium) }
            item { Text("Phone: ${cafe.phone}", style = MaterialTheme.typography.bodyMedium) }
            item { Text(cafe.status, style = MaterialTheme.typography.bodyMedium) }

            // Hours dropdown row
            item { HoursDropdown(hours = cafe.hours) }

            // Menu row
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Menu",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Open Menu")
                    }
                }
            }

            // Features
            item {
                Text(
                    text = "Features:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(cafe.features) { feature ->
                Text(text = "• $feature", style = MaterialTheme.typography.bodyMedium)
            }

            // Ambience
            item {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Ambience:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(cafe.ambience) { amb ->
                Text(text = "• $amb", style = MaterialTheme.typography.bodyMedium)
            }

            // Reviews header + button
            item {
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reviews:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )

                    TextButton(
                        onClick = { navController.navigate(Screen.WriteReview.createRoute(cafeId)) }
                    ) {
                        Text("Write review")
                    }
                }
            }

            // Reviews list
            if (reviews.isEmpty()) {
                item {
                    Text(
                        text = "No reviews yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                items(reviews) { review ->
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = review,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun HoursDropdown(hours: LinkedHashMap<String, String>) {
    var expanded by remember { mutableStateOf(false) }
    val days = remember(hours) { hours.keys.toList() }
    var selectedDay by rememberSaveable { mutableStateOf(days.firstOrNull() ?: "Thursday") }

    val selectedHours = hours[selectedDay] ?: ""
    val label = if (selectedHours.isNotBlank()) {
        "Hours: $selectedDay: $selectedHours"
    } else {
        "Hours"
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Expand Hours")
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            days.forEach { day ->
                DropdownMenuItem(
                    text = { Text("$day: ${hours[day]}") },
                    onClick = {
                        selectedDay = day
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun WriteReviewScreen(navController: NavHostController, cafeId: String) {
    var reviewText by rememberSaveable { mutableStateOf("") }

    Scaffold(
        bottomBar = { BottomNavBar(navController) },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            OutlinedTextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                placeholder = { Text("Write a review...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                singleLine = false,
                maxLines = Int.MAX_VALUE
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Cancel")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val trimmed = reviewText.trim()
                        if (trimmed.isNotEmpty()) {
                            ReviewRepository.addReview(cafeId, trimmed)
                        }
                        navController.popBackStack()
                    },
                    enabled = reviewText.trim().isNotEmpty()
                ) {
                    Text("Post")
                }
            }
        }
    }
}


@Composable
fun PlaceSaved() {
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
fun PlaceCard(navController: NavHostController, cafe: Cafe) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(Screen.CafeDetails.createRoute(cafe.id))
            },
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
                AsyncImage(
                    model = cafe.imageUrl ?: cafe.imageResId,
                    contentDescription = "${cafe.name} photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

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
                    ) { // Removed trailing ')' and '+'
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
                    text = cafe.name,
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
                Text(text = cafe.address, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
            }
        }
    }
}

@Composable
fun RatingStars(rating: Float) {
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
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Screen.MainScreen.route,
            onClick = {navController.navigate(Screen.MainScreen.route) },
            icon = { Icon(Icons.Filled.Search, contentDescription = "Search") }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.MapScreen.route,
            onClick = { navController.navigate(Screen.MapScreen.route) },
            icon = { Icon(Icons.Filled.Place, contentDescription = "Map") }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.BookmarkScreen.route,
            onClick = { navController.navigate(Screen.BookmarkScreen.route) },
            icon = { Icon(Icons.Filled.Bookmark, contentDescription = "Bookmarks") }
        )

        NavigationBarItem(
            selected = currentRoute == Screen.ProfileScreen.route,
            onClick = { navController.navigate(Screen.ProfileScreen.route) },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") }
        )
    }
}
@Serializable
data class Profile(
    val id: String,
    val first_name: String,
    val last_name: String,
    val avatar_url: String? = null,
    val bio: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var profile by remember { mutableStateOf<Profile?>(null) }
    var bioText by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }

    var isLoading by remember { mutableStateOf(true) }

    // Pick image + upload to DB
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->

        if (uri != null) {
            avatarUri = uri

            scope.launch {

                val user = supabase.auth.currentUserOrNull()
                if (user == null) return@launch

                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes() ?: return@launch

                    val fileName = "${user.id}.png"

                    // Upload to storage
                    supabase.storage
                        .from("avatars")
                        .upload(
                            path = fileName,
                            data = bytes
                        ) {
                            upsert = true
                        }

                    println("Upload success!")

                    val publicUrl = supabase.storage
                        .from("avatars")
                        .publicUrl(fileName)

                    // Save URL in profiles table
                    supabase.from("profiles")
                        .update(mapOf("avatar_url" to publicUrl)) {
                            filter {
                                eq("id", user.id)
                            }
                        }

                    // update profile?? might not be needed
                    profile = profile?.copy(avatar_url = publicUrl)

                } catch (e: Exception) {
                    println("Upload failed: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    // Fetch the profile
    LaunchedEffect(Unit) {
        val user = supabase.auth.currentUserOrNull()

        if (user != null) {
            try {
                val result = supabase
                    .from("profiles")
                    .select {
                        filter {
                            eq("id", user.id)
                        }
                    }
                    .decodeSingle<Profile>()

                profile = result
                bioText = result.bio ?: ""

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        isLoading = false;
    }

    if (isLoading) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(12.dp))
                Text("Loading profile...")
            }
        }

    }

    else {
        Scaffold(
            bottomBar = { BottomNavBar(navController) },
            containerColor = Color.White
        ) { innerPadding ->

            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // Notifications
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { }) {
                            Icon(
                                Icons.Filled.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    }
                }

                // Avatar + Name
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Surface(
                            shape = CircleShape,
                            modifier = Modifier.size(72.dp),
                            color = Color(0xFFEDE7FF),
                            onClick = { pickImage.launch("image/*") }
                        ) {

                            when {
                                avatarUri != null -> {
                                    AsyncImage(
                                        model = avatarUri,
                                        contentDescription = "Profile picture",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                profile?.avatar_url != null -> {
                                    AsyncImage(
                                        model = "${profile?.avatar_url}?t=${System.currentTimeMillis()}",
                                        contentDescription = "Profile picture",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                else -> {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Filled.Person,
                                            contentDescription = "Profile picture",
                                            modifier = Modifier.size(32.dp),
                                            tint = Color(0xFF6B4EFF)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = profile?.let {
                                val first = it.first_name.takeIf { it.isNotBlank() } ?: ""
                                val last = it.last_name.takeIf { it.isNotBlank() } ?: ""
                                if (first.isNotEmpty() || last.isNotEmpty()) "$first $last" else "User"
                            } ?: "User",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Quick Actions
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileAction(Icons.Filled.Star, "Review")
                        ProfileAction(Icons.Filled.CameraAlt, "Photos")
                        ProfileAction(Icons.Filled.Group, "Groups")
                    }
                }

                // Experience Title
                item {
                    Text(
                        text = "Experience",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Bio/ experience
                item {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {

                        TextField(
                            value = bioText,
                            onValueChange = { bioText = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            placeholder = { Text("Tell us about yourself...") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    }
                }

                // Save Bio
                item {
                    Button(
                        onClick = {
                            scope.launch {
                                val user = supabase.auth.currentUserOrNull()
                                if (user != null) {
                                    supabase.from("profiles")
                                        .update(mapOf("bio" to bioText)) {
                                            filter {
                                                eq("id", user.id)
                                            }
                                        }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save")
                    }
                }

                // Recently viewed
                item {
                    Text(
                        text = "Recently View",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(
                    listOf(
                        "Klatch Coffee" to "13855 City Center Dr #3015...",
                        "Aroma Craft Coffee" to "20265 Valley Blvd Ste Q..."
                    )
                ) { item ->
                    RecentItemRow(item.first, item.second)
                }

                item {

                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                supabase.auth.signOut()
                                navController.navigate(Screen.LoginScreen.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.Red),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person, // or Icons.Filled.ExitToApp
                            contentDescription = "Logout",
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Logout",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

//@Composable
//fun AsyncImage(model: Uri?, contentDescription: String, modifier: Modifier) {
  //  TODO("Not yet implemented")
//}

@Composable
fun ProfileAction( // Removed private
    icon: ImageVector,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun RecentItemRow(name: String, address: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF0F0F0)
        ) {}

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.SemiBold)
            Text(
                address,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        IconButton(onClick = { }) {
            Icon(
                Icons.Filled.BookmarkBorder,
                contentDescription = "Bookmark"
            )
        }
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    Shuffle_CafeTheme {
        AppNav()
    }
}
