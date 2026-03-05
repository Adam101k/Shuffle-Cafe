package com.example.shuffle_cafe

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.* 
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.shuffle_cafe.ui.screens.LoginScreen
import com.example.shuffle_cafe.ui.theme.Shuffle_CafeTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.util.Locale

data class Cafe(
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val status: String,
    val hours: LinkedHashMap<String, String>,
    val features: List<String>,
    val ambience: List<String>,
    val rating: Float? = null,
    val userRatingCount: Int? = null,
    val imageResId: Int = R.drawable.ic_launcher_foreground,
    val imageUrl: String? = null,
    val imageBitmap: Bitmap? = null
)

object CafeRepository {
    private val fallbackCafes: List<Cafe> = listOf(
        Cafe("1", "Cafe Name 1", "123 something ave", "(123) 123-1234", "Busy", linkedMapOf("Thursday" to "7:00AM - 5:00 PM"), listOf("Parking"), listOf("Quiet")),
        Cafe("2", "Klatch Coffee", "13855 City Center Dr #3015", "(555) 555-5555", "Quiet", linkedMapOf("Friday" to "6:30AM - 6:00 PM"), listOf("Wifi"), listOf("Quiet")),
        Cafe("3", "Cafe Name 3", "456 another street", "(111) 222-3333", "Closed", linkedMapOf("Monday" to "8:00AM - 4:00 PM"), listOf("Outdoor seating"), listOf("Lively")),
        Cafe("4", "Cafe Name 4", "789 last road", "(444) 555-6666", "Busy", linkedMapOf("Wednesday" to "9:00AM - 3:00 PM"), listOf("Pet friendly"), listOf("Cozy")),
        Cafe("5", "Brewed Awakening", "321 Coffee Lane", "(777) 888-9999", "Open", linkedMapOf("Saturday" to "8:00AM - 8:00 PM"), listOf("Live Music"), listOf("Hip")),
        Cafe("6", "The Daily Grind", "555 Bean St", "(000) 111-2222", "Busy", linkedMapOf("Sunday" to "9:00AM - 5:00 PM"), listOf("Pastries"), listOf("Bustling"))
    )
    private val liveCafes = mutableStateListOf<Cafe>()

    val cafes: List<Cafe>
        get() = if (liveCafes.isNotEmpty()) liveCafes else fallbackCafes

    fun setLiveCafes(cafes: List<Cafe>) {
        liveCafes.clear()
        liveCafes.addAll(cafes)
    }

    fun clearLiveCafes() {
        liveCafes.clear()
    }

    fun getCafe(id: String): Cafe? = cafes.firstOrNull { it.id == id }
}

object ReviewRepository {
    private val reviewsByCafe = mutableStateMapOf<String, SnapshotStateList<String>>()
    fun reviewsFor(cafeId: String): SnapshotStateList<String> = reviewsByCafe.getOrPut(cafeId) { mutableStateListOf() }
    fun addReview(cafeId: String, review: String) { reviewsFor(cafeId).add(review) }
}

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, "AIzaSyC7QTmdJE2fnRXMiKWrMZftkXIG20gNWrA")
        }
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        setContent { Shuffle_CafeTheme { AppNav() } }
    }
}

@Composable
fun AppNav(){
    val navController = rememberNavController()
    val isUserLoggedIn by remember { mutableStateOf(false) }
    NavHost(navController = navController, startDestination = if (isUserLoggedIn) Screen.MainScreen.route else Screen.LoginScreen.route) {
        composable(Screen.LoginScreen.route) { LoginScreen(navController) }
        composable(Screen.MainScreen.route) { MainScreen(navController) }
        composable(Screen.MapScreen.route) { MapScreen(navController) }
        composable(Screen.BookmarkScreen.route) { BookmarkScreen(navController) }
        composable(Screen.ProfileScreen.route) { ProfileScreen(navController) }
        composable(Screen.CafeDetails.route, arguments = listOf(navArgument("cafeId") { type = NavType.StringType })) { 
            CafeDetailsScreen(navController, it.arguments?.getString("cafeId") ?: "") 
        }
        composable(Screen.WriteReview.route, arguments = listOf(navArgument("cafeId") { type = NavType.StringType })) { 
            WriteReviewScreen(navController, it.arguments?.getString("cafeId") ?: "") 
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val placesClient = remember(context) { if (Places.isInitialized()) Places.createClient(context) else null }
    val hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    var allCafes by remember { mutableStateOf<List<Cafe>>(emptyList()) }
    val currentVisibleCafes = remember { mutableStateListOf<Cafe>() }
    var nextCafeIndex by rememberSaveable { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    val setVisibleStack: (List<Cafe>) -> Unit = { cafes ->
        currentVisibleCafes.clear()
        if (cafes.isNotEmpty()) {
            val visibleCount = minOf(3, cafes.size)
            currentVisibleCafes.addAll(cafes.take(visibleCount))
            nextCafeIndex = visibleCount % cafes.size
        } else {
            nextCafeIndex = 0
        }
    }

    LaunchedEffect(hasLocationPermission, placesClient) {
        if (!hasLocationPermission) {
            loadError = "Location permission is required to show nearby cafes."
            isLoading = false
            allCafes = emptyList()
            CafeRepository.clearLiveCafes()
            setVisibleStack(emptyList())
            return@LaunchedEffect
        }

        if (placesClient == null) {
            loadError = "Places SDK is not initialized."
            isLoading = false
            allCafes = emptyList()
            CafeRepository.clearLiveCafes()
            setVisibleStack(emptyList())
            return@LaunchedEffect
        }

        isLoading = true
        loadError = null
        fetchNearbyCafes(
            fusedLocationClient = fusedLocationClient,
            placesClient = placesClient,
            onSuccess = { fetchedCafes ->
                val uniqueCafes = fetchedCafes.distinctBy { it.id }
                allCafes = uniqueCafes
                CafeRepository.setLiveCafes(uniqueCafes)
                setVisibleStack(uniqueCafes)
                isLoading = false
            },
            onError = { message ->
                loadError = message
                allCafes = emptyList()
                CafeRepository.clearLiveCafes()
                setVisibleStack(emptyList())
                isLoading = false
            },
            onCafePhotoLoaded = { cafeId, bitmap ->
                val updatedCafes = allCafes.map { cafe ->
                    if (cafe.id == cafeId) cafe.copy(imageBitmap = bitmap) else cafe
                }
                allCafes = updatedCafes
                CafeRepository.setLiveCafes(updatedCafes)

                val visibleIndex = currentVisibleCafes.indexOfFirst { it.id == cafeId }
                if (visibleIndex >= 0) {
                    val replacement = updatedCafes.firstOrNull { it.id == cafeId } ?: return@fetchNearbyCafes
                    currentVisibleCafes[visibleIndex] = replacement
                }
            }
        )
    }

    Scaffold(topBar = { TopSearchBar() }, bottomBar = { BottomNavBar(navController) }, containerColor = Color.White) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Loading nearby cafes...")
                }
                loadError != null -> {
                    Text(loadError ?: "Unable to load cafes.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                }
                allCafes.isEmpty() -> {
                    Text("No nearby cafes found.", style = MaterialTheme.typography.headlineSmall)
                }
                else -> {
                val onCafeSwiped: (Cafe) -> Unit = { swipedCafe ->
                    val visibleSizeBeforeSwipe = currentVisibleCafes.size
                    currentVisibleCafes.remove(swipedCafe)

                    if (allCafes.size <= visibleSizeBeforeSwipe) {
                        currentVisibleCafes.add(swipedCafe)
                    } else {
                        currentVisibleCafes.add(allCafes[nextCafeIndex])
                        nextCafeIndex = (nextCafeIndex + 1) % allCafes.size
                    }
                }

                SwipeableCafeStack(
                    cafes = currentVisibleCafes,
                    navController = navController,
                    onSwipeLeft = onCafeSwiped,
                    onSwipeRight = onCafeSwiped
                )
            }
            }
        }
    }
}

private fun fetchNearbyCafes(
    fusedLocationClient: FusedLocationProviderClient,
    placesClient: PlacesClient,
    onSuccess: (List<Cafe>) -> Unit,
    onError: (String) -> Unit,
    onCafePhotoLoaded: (String, Bitmap) -> Unit
) {
    getCurrentOrLastLocation(
        fusedLocationClient = fusedLocationClient,
        onSuccess = { location ->
            searchNearbyCafes(
                placesClient = placesClient,
                location = location,
                onSuccess = onSuccess,
                onError = onError,
                onCafePhotoLoaded = onCafePhotoLoaded
            )
        },
        onError = onError
    )
}

private fun getCurrentOrLastLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onSuccess: (Location) -> Unit,
    onError: (String) -> Unit
) {
    val cancellationTokenSource = CancellationTokenSource()
    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationTokenSource.token)
        .addOnSuccessListener { currentLocation ->
            if (currentLocation != null) {
                onSuccess(currentLocation)
            } else {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { lastLocation ->
                        if (lastLocation != null) onSuccess(lastLocation) else onError("Could not determine current location.")
                    }
                    .addOnFailureListener { error ->
                        onError(error.localizedMessage ?: "Could not determine current location.")
                    }
            }
        }
        .addOnFailureListener { error ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { lastLocation ->
                    if (lastLocation != null) onSuccess(lastLocation) else onError(error.localizedMessage ?: "Could not determine current location.")
                }
                .addOnFailureListener {
                    onError(error.localizedMessage ?: "Could not determine current location.")
                }
        }
}

private fun searchNearbyCafes(
    placesClient: PlacesClient,
    location: Location,
    onSuccess: (List<Cafe>) -> Unit,
    onError: (String) -> Unit,
    onCafePhotoLoaded: (String, Bitmap) -> Unit
) {
    val locationRestriction = CircularBounds.newInstance(LatLng(location.latitude, location.longitude), 2_000.0)
    val placeFields = listOf(
        Place.Field.ID,
        Place.Field.NAME,
        Place.Field.ADDRESS,
        Place.Field.RATING,
        Place.Field.USER_RATINGS_TOTAL,
        Place.Field.PHOTO_METADATAS
    )

    val request = SearchNearbyRequest.builder(locationRestriction, placeFields)
        .setIncludedTypes(listOf("cafe"))
        .setMaxResultCount(20)
        .setRankPreference(SearchNearbyRequest.RankPreference.DISTANCE)
        .build()

    placesClient.searchNearby(request)
        .addOnSuccessListener { response ->
            val places = response.places
            val cafes = places.mapNotNull { place -> place.toCafe() }
            onSuccess(cafes)

            places.forEach { place ->
                val placeId = place.id ?: return@forEach
                val metadata = place.photoMetadatas?.firstOrNull() ?: return@forEach
                fetchCafePhoto(
                    placesClient = placesClient,
                    placeId = placeId,
                    photoMetadata = metadata,
                    onSuccess = onCafePhotoLoaded
                )
            }
        }
        .addOnFailureListener { error ->
            onError(error.localizedMessage ?: "Failed to fetch nearby cafes.")
        }
}

private fun fetchCafePhoto(
    placesClient: PlacesClient,
    placeId: String,
    photoMetadata: PhotoMetadata,
    onSuccess: (String, Bitmap) -> Unit
) {
    val request = FetchPhotoRequest.builder(photoMetadata)
        .setMaxWidth(1200)
        .setMaxHeight(900)
        .build()

    placesClient.fetchPhoto(request)
        .addOnSuccessListener { response ->
            onSuccess(placeId, response.bitmap)
        }
}

private fun Place.toCafe(): Cafe? {
    val placeId = id ?: return null
    val cafeName = name?.trim().takeIf { !it.isNullOrBlank() } ?: return null
    val cafeAddress = address?.trim().takeIf { !it.isNullOrBlank() } ?: "Address unavailable"
    val ratingValue = rating?.toFloat()
    val ratingCount = userRatingsTotal
    val statusText = if (ratingValue != null && ratingCount != null && ratingCount > 0) {
        String.format(Locale.US, "%.1f (%d reviews)", ratingValue, ratingCount)
    } else {
        "No ratings yet"
    }

    return Cafe(
        id = placeId,
        name = cafeName,
        address = cafeAddress,
        phone = "Phone unavailable",
        status = statusText,
        hours = linkedMapOf("Hours" to "Check Google Maps"),
        features = listOf("Nearby"),
        ambience = listOf("Coffee"),
        rating = ratingValue,
        userRatingCount = ratingCount
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavHostController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val defaultCamera = rememberCameraPositionState()
    LaunchedEffect(hasLocationPermission){
        if(hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { it?.let{ defaultCamera.move(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 17f)) } }
        }
    }
    Scaffold(topBar = { MapSearchBar() }, bottomBar = { BottomNavBar(navController) }) { innerPadding ->
        GoogleMap(modifier = Modifier.padding(innerPadding).fillMaxSize(), cameraPositionState = defaultCamera, properties = MapProperties(isMyLocationEnabled = hasLocationPermission), uiSettings = MapUiSettings(myLocationButtonEnabled = true))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(navController: NavHostController) {
    Scaffold(topBar = { TopSearchBar() }, bottomBar = { BottomNavBar(navController) }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(16.dp))
            PlaceSaved()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSearchBar() {
    var text by remember { mutableStateOf("") }
    Surface(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        TextField(value = text, onValueChange = { text = it }, placeholder = { Text("Search cafes...") }, leadingIcon = { Icon(Icons.Filled.Menu, null) }, trailingIcon = { Icon(Icons.Filled.Search, null) }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, disabledContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSearchBar() {
    var text by remember { mutableStateOf("") }
    Surface(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        TextField(value = text, onValueChange = { text = it }, placeholder = { Text("Search location...") }, leadingIcon = { Icon(Icons.Filled.Menu, null) }, trailingIcon = { Icon(Icons.Filled.Search, null) }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, disabledContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
    }
}

@Composable
fun CafeDetailsScreen(navController: NavHostController, cafeId: String) {
    val cafe = remember(cafeId) { CafeRepository.getCafe(cafeId) }
    val reviews = ReviewRepository.reviewsFor(cafeId)
    Scaffold(bottomBar = { BottomNavBar(navController) }, containerColor = Color.White) { innerPadding ->
        if (cafe == null) { Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) { Text("Cafe not found") }; return@Scaffold }
        LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Spacer(Modifier.height(8.dp)); Text(cafe.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
            item { Text("Address: ${cafe.address}"); Text("Phone: ${cafe.phone}"); Text(cafe.status); HoursDropdown(cafe.hours) }
            item { OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, Color.Black)) { Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Text("Menu", modifier = Modifier.weight(1f)); Icon(Icons.Filled.PlayArrow, null) } } }
            item { Text("Features:", fontWeight = FontWeight.SemiBold) }
            items(cafe.features) { Text("• $it") }
            item { Text("Ambience:", fontWeight = FontWeight.SemiBold) }
            items(cafe.ambience) { Text("• $it") }
            item { Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Reviews:", modifier = Modifier.weight(1f)); TextButton(onClick = { navController.navigate(Screen.WriteReview.createRoute(cafeId)) }) { Text("Write review") } } }
            if (reviews.isEmpty()) { item { Text("No reviews yet.", color = Color.Gray) } } else { items(reviews) { OutlinedCard(modifier = Modifier.fillMaxWidth()) { Text(it, modifier = Modifier.padding(12.dp)) } } }
        }
    }
}

@Composable
fun HoursDropdown(hours: LinkedHashMap<String, String>) {
    var expanded by remember { mutableStateOf(false) }
    val days = hours.keys.toList()
    var selectedDay by rememberSaveable { mutableStateOf(days.firstOrNull() ?: "") }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedCard(modifier = Modifier.fillMaxWidth().clickable { expanded = true }, shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, Color.Black)) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Text("Hours: $selectedDay: ${hours[selectedDay] ?: ""}", modifier = Modifier.weight(1f)); Icon(Icons.Filled.ArrowDropDown, null) }
        }
        DropdownMenu(expanded, { expanded = false }) { days.forEach { day -> DropdownMenuItem(text = { Text("$day: ${hours[day]}") }, onClick = { selectedDay = day; expanded = false }) } }
    }
}

@Composable
fun WriteReviewScreen(navController: NavHostController, cafeId: String) {
    var text by rememberSaveable { mutableStateOf("") }
    Scaffold(bottomBar = { BottomNavBar(navController) }, containerColor = Color.White) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(text, { text = it }, placeholder = { Text("Write a review...") }, modifier = Modifier.fillMaxWidth().weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { navController.popBackStack() }) { Text("Cancel") }
                Button(onClick = { ReviewRepository.addReview(cafeId, text); navController.popBackStack() }, enabled = text.isNotBlank()) { Text("Post") }
            }
        }
    }
}

@Composable
fun PlaceSaved() {
    val outlineColor = Color(0xFFE6E6E6)
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(22.dp)) {
        Column { 
            Text("Saved", fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(4.dp)); Box(Modifier.width(80.dp).height(1.dp).background(outlineColor))
            Spacer(Modifier.height(10.dp)); Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { SavedTile(Modifier.weight(1f)); SavedTile(Modifier.weight(1f)); SavedTile(Modifier.weight(1f), true) } 
        }
        Column { 
            Text("Collection", fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(4.dp)); Box(Modifier.width(80.dp).height(1.dp).background(outlineColor))
            Spacer(Modifier.height(10.dp)); Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { Column(Modifier.weight(1f)) { SavedTile(); Text("Good Coffee", style = MaterialTheme.typography.bodySmall) }; Column(Modifier.weight(1f)) { SavedTile(); Text("Quiet Area", style = MaterialTheme.typography.bodySmall) }; SavedTile(Modifier.weight(1f), true) } 
        }
        Column { 
            Text("Study Plan", fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(4.dp)); Box(Modifier.width(80.dp).height(1.dp).background(outlineColor))
            Spacer(Modifier.height(10.dp)); Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) { SavedTile(Modifier.weight(1f)); SavedTile(Modifier.weight(1f)); SavedTile(Modifier.weight(1f), true) } 
        }
    }
}

@Composable
fun SavedTile(modifier: Modifier = Modifier, plus: Boolean = false) {
    val outlineColor = Color(0xFFE6E6E6)
    OutlinedCard(modifier.aspectRatio(1f), shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, outlineColor)) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { if (plus) Text("+", style = MaterialTheme.typography.headlineLarge) } }
}

@Composable
fun PlaceCard(navController: NavHostController, cafe: Cafe, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().clickable { navController.navigate(Screen.CafeDetails.createRoute(cafe.id)) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(340.dp)) {
                AsyncImage(
                    model = cafe.imageBitmap ?: cafe.imageUrl ?: cafe.imageResId,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxWidth().height(72.dp).align(Alignment.BottomCenter).background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.5f)))))
                Row(modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = Color.White); Spacer(Modifier.width(8.dp)); Text("More like this", color = Color.White, modifier = Modifier.weight(1f))
                    Surface(modifier = Modifier.padding(0.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.25f)) { IconButton(onClick = { }) { Icon(Icons.Filled.Share, contentDescription = null, tint = Color.White) } }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                val rating = cafe.rating
                val reviewCount = cafe.userRatingCount ?: 0
                Text(cafe.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (rating != null && reviewCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RatingStars(rating)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            String.format(Locale.US, "%.1f (%d Reviews)", rating, reviewCount),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    Text("No ratings yet", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
                Text(cafe.address, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun RatingStars(rating: Float) {
    Row { repeat(rating.toInt()) { Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107)) }; repeat(5 - rating.toInt()) { Icon(Icons.Filled.Star, contentDescription = null, tint = Color.LightGray) } }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val route = navBackStackEntry?.destination?.route
    NavigationBar {
        NavigationBarItem(selected = route == Screen.MainScreen.route, onClick = { navController.navigate(Screen.MainScreen.route) }, icon = { Icon(Icons.Filled.Search, null) })
        NavigationBarItem(selected = route == Screen.MapScreen.route, onClick = { navController.navigate(Screen.MapScreen.route) }, icon = { Icon(Icons.Filled.Place, null) })
        NavigationBarItem(selected = route == Screen.BookmarkScreen.route, onClick = { navController.navigate(Screen.BookmarkScreen.route) }, icon = { Icon(Icons.Filled.Bookmark, null) })
        NavigationBarItem(selected = route == Screen.ProfileScreen.route, onClick = { navController.navigate(Screen.ProfileScreen.route) }, icon = { Icon(Icons.Filled.Person, null) })
    }
}

@Composable
fun ProfileScreen(navController: NavHostController) {
    var uri by remember { mutableStateOf<Uri?>(null) }
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri = it }
    Scaffold(bottomBar = { BottomNavBar(navController) }, containerColor = Color.White) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { IconButton(onClick = { }) { Icon(Icons.Filled.Notifications, null) } } }
            item {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(onClick = { pick.launch("image/*") }, modifier = Modifier.size(72.dp), shape = CircleShape, color = Color(0xFFEDE7FF)) {
                        if (uri == null) Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Person, null, Modifier.size(32.dp), Color(0xFF6B4EFF)) }
                        else AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    Text("User", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                }
            }
            item { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.Star, null); Text("Review") }; Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.CameraAlt, null); Text("Photos") }; Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Filled.Group, null); Text("Groups") } } }
            item { Text("Experience", fontWeight = FontWeight.SemiBold); OutlinedCard(modifier = Modifier.fillMaxWidth().height(80.dp)) { } }
            item { Text("Recently Viewed", fontWeight = FontWeight.SemiBold) }
            items(listOf("Klatch Coffee", "Aroma Craft Coffee")) { Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(44.dp).background(Color.LightGray, RoundedCornerShape(8.dp))); Spacer(Modifier.width(12.dp)); Text(it, modifier = Modifier.weight(1f)); IconButton(onClick = { }) { Icon(Icons.Filled.BookmarkBorder, null) } } }
        }
    }
}

@Composable
fun SwipeableCafeStack(cafes: SnapshotStateList<Cafe>, navController: NavHostController, onSwipeLeft: (Cafe) -> Unit, onSwipeRight: (Cafe) -> Unit) {
    if (cafes.isEmpty()) return
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val swipeThresholdPx = with(density) { 110.dp.toPx() }
    val offscreenTargetPx = with(density) { configuration.screenWidthDp.dp.toPx() * 1.2f }
    val offsetX = remember { Animatable(0f) }
    val cardRotation = remember { Animatable(0f) }

    LaunchedEffect(cafes.firstOrNull()?.id) { 
        offsetX.snapTo(0f)
        cardRotation.snapTo(0f) 
    }

    Box(modifier = Modifier.fillMaxWidth(0.9f).aspectRatio(0.58f), contentAlignment = Alignment.Center) {
        // Draw from back to front
        for (i in cafes.indices.reversed()) {
            val cafe = cafes[i]
            val stackIndex = i.toFloat()
            val scaleTarget = 1f - (stackIndex * 0.05f).coerceAtMost(0.1f)
            val yOffsetTarget = (stackIndex * 10).dp
            val animatedScale by animateFloatAsState(
                targetValue = scaleTarget,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "stackScale"
            )
            val animatedYOffset by animateDpAsState(
                targetValue = yOffsetTarget,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "stackYOffset"
            )
            key(cafe.id) {
                val modifier = if (i == 0) {
                    Modifier
                        .zIndex(100f)
                        .offset(y = animatedYOffset)
                        .scale(animatedScale)
                        .graphicsLayer {
                            translationX = offsetX.value
                            rotationZ = cardRotation.value
                        }
                        .pointerInput(cafe.id) {
                            detectDragGestures(
                                onDrag = { change, drag -> 
                                    change.consume()
                                    scope.launch { 
                                        val newOffset = offsetX.value + drag.x
                                        offsetX.snapTo(newOffset)
                                        cardRotation.snapTo((newOffset / 40f).coerceIn(-18f, 18f)) 
                                    } 
                                },
                                onDragEnd = {
                                    scope.launch {
                                        val currentOffset = offsetX.value
                                        if (currentOffset > swipeThresholdPx || currentOffset < -swipeThresholdPx) {
                                            val target = if (currentOffset > 0) offscreenTargetPx else -offscreenTargetPx
                                            launch {
                                                cardRotation.animateTo(
                                                    targetValue = (target / 40f).coerceIn(-18f, 18f),
                                                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                                                )
                                            }
                                            offsetX.animateTo(
                                                targetValue = target,
                                                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                                            )

                                            if (currentOffset > 0) onSwipeRight(cafe) else onSwipeLeft(cafe)
                                        }
                                        else { 
                                            launch {
                                                offsetX.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                                        stiffness = Spring.StiffnessMediumLow
                                                    )
                                                )
                                            }
                                            launch {
                                                cardRotation.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                                        stiffness = Spring.StiffnessMediumLow
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                } else {
                    Modifier
                        .zIndex(10f - stackIndex)
                        .offset(y = animatedYOffset)
                        .scale(animatedScale)
                }
                PlaceCard(navController = navController, cafe = cafe, modifier = modifier)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    Shuffle_CafeTheme {
        val navController = rememberNavController()
        MainScreen(navController = navController)
    }
}
