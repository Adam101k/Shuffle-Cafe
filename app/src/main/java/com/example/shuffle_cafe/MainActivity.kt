package com.example.shuffle_cafe

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigationevent.NavigationEventInfo
import coil.compose.AsyncImage
import com.example.shuffle_cafe.ui.screens.LoginScreen
import com.example.shuffle_cafe.ui.theme.CafeBrown
import com.example.shuffle_cafe.ui.theme.CafeDark
import com.example.shuffle_cafe.ui.theme.Shuffle_CafeTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.Serializable

val supabase = createSupabaseClient(
    supabaseUrl = "https://sknyfkgltazosjmyjfhs.supabase.co",
    supabaseKey = "sb_publishable_dCrTJjMXS6bw1WDaqTtewg_amytqZMf"
) {
    install(Auth) {
        // Keep mobile sessions restored and refreshed between app launches.
        alwaysAutoRefresh = true
        autoLoadFromStorage = true
        autoSaveToStorage = true
    }
    install(Postgrest)
    install(Storage)
}

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
    val heroImageBitmap: Bitmap? = null,
    val photoMetadatas: List<PhotoMetadata> = emptyList(),
    val photoBitmaps: List<Bitmap?> = List(photoMetadatas.size) { null }
)

private fun Cafe.primaryImageModel(): Any = heroImageBitmap ?: imageUrl ?: imageResId

private fun Cafe.photoPageCount(): Int = when {
    photoMetadatas.isNotEmpty() -> photoMetadatas.size
    else -> 1
}

private fun Cafe.photoPageModel(index: Int): Any? {
    val loadedPhoto = photoBitmaps.getOrNull(index)
    if (loadedPhoto != null) return loadedPhoto
    if (index == 0) return primaryImageModel()
    return null
}

private fun Cafe.withLoadedPhoto(index: Int, bitmap: Bitmap): Cafe {
    if (photoMetadatas.isEmpty()) {
        return copy(heroImageBitmap = bitmap)
    }

    val updatedPhotos = if (photoBitmaps.size == photoMetadatas.size) {
        photoBitmaps.toMutableList()
    } else {
        MutableList(photoMetadatas.size) { photoBitmaps.getOrNull(it) }
    }

    if (index in updatedPhotos.indices) {
        updatedPhotos[index] = bitmap
    }

    return copy(
        heroImageBitmap = if (index == 0) bitmap else heroImageBitmap ?: updatedPhotos.firstOrNull { it != null },
        photoBitmaps = updatedPhotos
    )
}

private data class SelectedCafeTransitionState(
    val cafeId: String,
    val sourceBounds: Rect,
    val heroModel: Any
)

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}

private fun sCurve(progress: Float): Float {
    val t = progress.coerceIn(0f, 1f)
    return t * t * t * (t * (t * 6f - 15f) + 10f)
}

private fun transitionCardFadeAlpha(progress: Float): Float {
    return 1f - sCurve((progress / 0.26f).coerceIn(0f, 1f))
}

private fun transitionStackFadeAlpha(progress: Float): Float {
    return 1f - sCurve((progress / 0.24f).coerceIn(0f, 1f))
}

private fun transitionRevealAlpha(progress: Float): Float {
    return sCurve(((progress - 0.08f) / 0.38f).coerceIn(0f, 1f))
}

private fun transitionDetailSurfaceAlpha(progress: Float): Float {
    return sCurve(((progress - 0.16f) / 0.24f).coerceIn(0f, 1f))
}

private data class DetailOverlayLayoutSpec(
    val horizontalInset: androidx.compose.ui.unit.Dp = 16.dp,
    val topInset: androidx.compose.ui.unit.Dp = 34.dp,
    val bottomInset: androidx.compose.ui.unit.Dp = 52.dp,
    val collapsedCornerRadius: androidx.compose.ui.unit.Dp = 16.dp,
    val expandedCornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    val collapsedHeroHeight: androidx.compose.ui.unit.Dp = 340.dp,
    val expandedHeroHeight: androidx.compose.ui.unit.Dp = 445.dp
)

private data class VisibleCafeStack(
    val cafes: List<Cafe>,
    val nextCafeIndex: Int
)

private fun buildVisibleCafeStack(cafes: List<Cafe>): VisibleCafeStack {
    if (cafes.isEmpty()) {
        return VisibleCafeStack(emptyList(), 0)
    }

    val visibleCount = minOf(3, cafes.size)
    return VisibleCafeStack(
        cafes = cafes.take(visibleCount),
        nextCafeIndex = visibleCount % cafes.size
    )
}

private fun advanceVisibleCafeStack(
    visibleCafes: List<Cafe>,
    swipedCafe: Cafe,
    allCafes: List<Cafe>,
    nextCafeIndex: Int
): VisibleCafeStack {
    if (visibleCafes.isEmpty()) {
        return VisibleCafeStack(emptyList(), nextCafeIndex)
    }

    val swipedIndex = visibleCafes.indexOfFirst { it.id == swipedCafe.id }
    if (swipedIndex == -1) {
        return VisibleCafeStack(visibleCafes, nextCafeIndex)
    }

    val remainingCafes = visibleCafes.filterNot { it.id == swipedCafe.id }
    if (allCafes.size <= visibleCafes.size) {
        return VisibleCafeStack(
            cafes = remainingCafes + swipedCafe,
            nextCafeIndex = nextCafeIndex
        )
    }

    val nextCafe = allCafes[nextCafeIndex]
    return VisibleCafeStack(
        cafes = remainingCafes + nextCafe,
        nextCafeIndex = (nextCafeIndex + 1) % allCafes.size
    )
}

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

object BookmarkRepository {
    // store just IDs
    private val bookmarkedIds = mutableStateListOf<String>()

    fun isBookmarked(cafeId: String): Boolean = bookmarkedIds.contains(cafeId)

    fun toggle(cafeId: String) {
        if (bookmarkedIds.contains(cafeId)) bookmarkedIds.remove(cafeId)
        else bookmarkedIds.add(cafeId)
    }

    fun remove(cafeId: String) {
        bookmarkedIds.remove(cafeId)
    }

    // Expose as List so callers can display it
    fun ids(): List<String> = bookmarkedIds

    fun cafes(): List<Cafe> = bookmarkedIds
        .mapNotNull { id -> CafeRepository.getCafe(id) }
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
    val sessionStatus by supabase.auth.sessionStatus.collectAsState()

    LaunchedEffect(sessionStatus) {
        when (sessionStatus) {
            is SessionStatus.Authenticated -> {
                if (navController.currentDestination?.route == Screen.LoginScreen.route) {
                    navController.navigate(Screen.MainScreen.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            }
            is SessionStatus.NotAuthenticated -> {
                val currentRoute = navController.currentDestination?.route
                if (currentRoute != null && currentRoute != Screen.LoginScreen.route) {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            }
            SessionStatus.Initializing,
            is SessionStatus.RefreshFailure -> Unit
        }
    }

    if (sessionStatus is SessionStatus.Initializing) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(12.dp))
                Text("Checking your session...")
            }
        }
        return
    }

    val startDestination = when (sessionStatus) {
        is SessionStatus.Authenticated -> Screen.MainScreen.route
        else -> Screen.LoginScreen.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.LoginScreen.route) { LoginScreen(navController) }
        composable(Screen.MainScreen.route) { MainScreen(navController) }
        composable(Screen.MapScreen.route) { MapScreen(navController) }
        composable(Screen.BookmarkScreen.route) { BookmarkScreen(navController) }
        composable(Screen.ProfileScreen.route) { ProfileScreen(navController) }
        composable(Screen.Preferences.route) { PreferencesScreen(navController) }
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
    val scope = rememberCoroutineScope()

    var allCafes by remember { mutableStateOf<List<Cafe>>(emptyList()) }
    var currentVisibleCafes by remember { mutableStateOf<List<Cafe>>(emptyList()) }
    var nextCafeIndex by rememberSaveable { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var expandedCafeId by rememberSaveable { mutableStateOf<String?>(null) }
    var overlayCafe by remember { mutableStateOf<Cafe?>(null) }
    var transitionState by remember { mutableStateOf<SelectedCafeTransitionState?>(null) }
    var overlayHostBounds by remember { mutableStateOf<Rect?>(null) }
    var isPreparingDetailTransition by remember { mutableStateOf(false) }
    val detailProgress = remember { Animatable(0f) }
    val detailOverlayLayoutSpec = remember { DetailOverlayLayoutSpec() }

    val expandedCafe = remember(allCafes, expandedCafeId) {
        expandedCafeId?.let { cafeId ->
            allCafes.firstOrNull { it.id == cafeId } ?: CafeRepository.getCafe(cafeId)
        }
    }
    val isDetailExpanded = expandedCafe != null
    val loadedExpandedPhotoCount = expandedCafe?.photoBitmaps?.count { it != null } ?: 0

    LaunchedEffect(expandedCafe) {
        if (expandedCafe != null) {
            overlayCafe = expandedCafe
        }
    }

    LaunchedEffect(expandedCafeId) {
        if (expandedCafeId != null) {
            isPreparingDetailTransition = false
            detailProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 460, easing = FastOutSlowInEasing)
            )
        } else if (overlayCafe != null || detailProgress.value > 0f) {
            detailProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )
            overlayCafe = null
            transitionState = null
            isPreparingDetailTransition = false
        }
    }

    val updateCafePhoto: (String, Int, Bitmap) -> Unit = { cafeId, photoIndex, bitmap ->
        val updatedCafes = allCafes.map { cafe ->
            if (cafe.id == cafeId) cafe.withLoadedPhoto(photoIndex, bitmap) else cafe
        }
        allCafes = updatedCafes
        CafeRepository.setLiveCafes(updatedCafes)

        val replacement = updatedCafes.firstOrNull { it.id == cafeId }
        if (replacement != null && currentVisibleCafes.any { it.id == cafeId }) {
            currentVisibleCafes = currentVisibleCafes.map { visibleCafe ->
                if (visibleCafe.id == cafeId) replacement else visibleCafe
            }
        }
    }

    val setVisibleStack: (List<Cafe>) -> Unit = { cafes ->
        val visibleStack = buildVisibleCafeStack(cafes)
        currentVisibleCafes = visibleStack.cafes
        nextCafeIndex = visibleStack.nextCafeIndex
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
            onCafePhotoLoaded = updateCafePhoto
        )
    }

    BackHandler(enabled = isDetailExpanded) {
        expandedCafeId = null
    }

    LaunchedEffect(expandedCafe?.id, loadedExpandedPhotoCount, placesClient) {
        val cafe = expandedCafe ?: return@LaunchedEffect
        val client = placesClient ?: return@LaunchedEffect
        if (cafe.photoMetadatas.isEmpty()) return@LaunchedEffect

        val nextPhotoIndex = cafe.photoBitmaps.indexOfFirst { it == null }
        if (nextPhotoIndex == -1) return@LaunchedEffect

        val bitmap = fetchCafePhotoBitmap(client, cafe.photoMetadatas[nextPhotoIndex]) ?: return@LaunchedEffect
        updateCafePhoto(cafe.id, nextPhotoIndex, bitmap)
    }

    LaunchedEffect(
        currentVisibleCafes.firstOrNull()?.id,
        currentVisibleCafes.firstOrNull()?.photoBitmaps?.count { it != null } ?: 0,
        placesClient,
        expandedCafeId
    ) {
        if (expandedCafeId != null) return@LaunchedEffect
        val cafe = currentVisibleCafes.firstOrNull() ?: return@LaunchedEffect
        val client = placesClient ?: return@LaunchedEffect
        if (cafe.photoMetadatas.size <= 1) return@LaunchedEffect

        val preloadIndices = cafe.photoMetadatas.indices.filter { index ->
            index <= 1 && cafe.photoBitmaps.getOrNull(index) == null
        }

        for (photoIndex in preloadIndices) {
            val bitmap = fetchCafePhotoBitmap(client, cafe.photoMetadatas[photoIndex]) ?: break
            updateCafePhoto(cafe.id, photoIndex, bitmap)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                overlayHostBounds = coordinates.boundsInRoot()
            }
    ) {
        Scaffold(
            topBar = {
                TopSearchBar(
                    modifier = Modifier.graphicsLayer {
                        alpha = 1f - detailProgress.value
                    },
                    enabled = detailProgress.value < 0.01f
                )
            },
            bottomBar = {
                BottomNavBar(
                    navController = navController,
                    enabled = detailProgress.value < 0.01f,
                    dimFraction = detailProgress.value
                )
            },
            containerColor = Color(0xFFC79A87)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                val stackAlpha by animateFloatAsState(
                    targetValue = transitionStackFadeAlpha(detailProgress.value),
                    animationSpec = tween(durationMillis = 90, easing = FastOutSlowInEasing),
                    label = "stackAlpha"
                )
                val stackScale by animateFloatAsState(
                    targetValue = 1f - (0.015f * detailProgress.value),
                    animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
                    label = "stackScale"
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = stackAlpha
                            scaleX = stackScale
                            scaleY = stackScale
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
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
                            val renderTopCardOnly = isPreparingDetailTransition ||
                                    transitionState != null ||
                                    expandedCafeId != null ||
                                    detailProgress.value > 0f
                            val onCafeSwiped: (Cafe) -> Unit = { swipedCafe ->
                                if (expandedCafeId == swipedCafe.id) {
                                    expandedCafeId = null
                                }
                                val visibleStack = advanceVisibleCafeStack(
                                    visibleCafes = currentVisibleCafes,
                                    swipedCafe = swipedCafe,
                                    allCafes = allCafes,
                                    nextCafeIndex = nextCafeIndex
                                )
                                currentVisibleCafes = visibleStack.cafes
                                nextCafeIndex = visibleStack.nextCafeIndex
                            }

                            SwipeableCafeStack(
                                cafes = currentVisibleCafes,
                                transitioningCafeId = transitionState?.cafeId,
                                transitionProgress = detailProgress.value,
                                renderTopCardOnly = renderTopCardOnly,
                                onCafeSelected = { cafe, sourceBounds, heroModel ->
                                    transitionState = SelectedCafeTransitionState(
                                        cafeId = cafe.id,
                                        sourceBounds = sourceBounds,
                                        heroModel = heroModel
                                    )
                                    overlayCafe = cafe
                                    isPreparingDetailTransition = true
                                    scope.launch {
                                        detailProgress.snapTo(0f)
                                        withFrameNanos { }
                                        expandedCafeId = cafe.id
                                    }
                                },
                                enabled = !isDetailExpanded,
                                onSwipeLeft = onCafeSwiped,
                                onSwipeRight = onCafeSwiped
                            )
                        }
                    }
                }
            }
        }

        if (overlayCafe != null && transitionState != null && (expandedCafeId != null || detailProgress.value > 0f)) {
            val scrimAlpha by animateFloatAsState(
                targetValue = 0.18f * detailProgress.value,
                animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
                label = "detailScrimAlpha"
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
            ) {
                BoxWithConstraints(
                    modifier = Modifier.matchParentSize()
                ) {
                    val density = LocalDensity.current
                    val sourceBounds = transitionState?.sourceBounds
                    val hostBounds = overlayHostBounds

                    val endHorizontalInsetPx = with(density) { detailOverlayLayoutSpec.horizontalInset.toPx() }
                    val endTopInsetPx = with(density) { detailOverlayLayoutSpec.topInset.toPx() }
                    val endBottomInsetPx = with(density) { detailOverlayLayoutSpec.bottomInset.toPx() }
                    val endWidthPx = with(density) { maxWidth.toPx() } - (endHorizontalInsetPx * 2f)
                    val endHeightPx = with(density) { maxHeight.toPx() } - endTopInsetPx - endBottomInsetPx
                    val endLeftPx = endHorizontalInsetPx
                    val endTopPx = endTopInsetPx

                    val animatedCornerRadius = lerp(
                        detailOverlayLayoutSpec.collapsedCornerRadius,
                        detailOverlayLayoutSpec.expandedCornerRadius,
                        detailProgress.value
                    )
                    val animatedHeroHeight = lerp(
                        detailOverlayLayoutSpec.collapsedHeroHeight,
                        detailOverlayLayoutSpec.expandedHeroHeight,
                        detailProgress.value
                    )

                    val fallbackStartWidthPx = with(density) { (maxWidth * 0.9f).toPx() }
                    val fallbackStartHeightPx = fallbackStartWidthPx / 0.58f
                    val hasValidSourceBounds = sourceBounds != null &&
                            hostBounds != null &&
                            sourceBounds.width > 0f &&
                            sourceBounds.height > 0f

                    val startLeftPx = if (hasValidSourceBounds) {
                        sourceBounds.left - hostBounds.left
                    } else {
                        endLeftPx
                    }
                    val startTopPx = if (hasValidSourceBounds) {
                        sourceBounds.top - hostBounds.top
                    } else {
                        endTopPx
                    }
                    val startWidthPx = if (hasValidSourceBounds) sourceBounds.width else fallbackStartWidthPx
                    val startHeightPx = if (hasValidSourceBounds) sourceBounds.height else fallbackStartHeightPx

                    val animatedLeftPx = lerpFloat(startLeftPx, endLeftPx, detailProgress.value)
                    val animatedTopPx = lerpFloat(startTopPx, endTopPx, detailProgress.value)
                    val animatedWidthPx = lerpFloat(startWidthPx, endWidthPx, detailProgress.value)
                    val animatedHeightPx = lerpFloat(startHeightPx, endHeightPx, detailProgress.value)

                    overlayCafe?.let { cafe ->
                        transitionState?.let { transition ->
                            ExpandedCafeDetailOverlay(
                                navController = navController,
                                cafe = cafe,
                                onClose = { expandedCafeId = null },
                                cornerRadius = animatedCornerRadius,
                                heroHeight = animatedHeroHeight,
                                sharedHeroModel = transition.heroModel,
                                transitionProgress = detailProgress.value,
                                modifier = Modifier
                                    .graphicsLayer {
                                        translationX = animatedLeftPx
                                        translationY = animatedTopPx
                                    }
                                    .width(with(density) { animatedWidthPx.toDp() })
                                    .height(with(density) { animatedHeightPx.toDp() })
                            )
                        }
                    }
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
    onCafePhotoLoaded: (String, Int, Bitmap) -> Unit
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

@SuppressLint("MissingPermission")
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
    onCafePhotoLoaded: (String, Int, Bitmap) -> Unit
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
                    photoIndex = 0,
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
    photoIndex: Int,
    photoMetadata: PhotoMetadata,
    onSuccess: (String, Int, Bitmap) -> Unit
) {
    val request = FetchPhotoRequest.builder(photoMetadata)
        .setMaxWidth(1200)
        .setMaxHeight(900)
        .build()

    placesClient.fetchPhoto(request)
        .addOnSuccessListener { response ->
            onSuccess(placeId, photoIndex, response.bitmap)
        }
}

private suspend fun fetchCafePhotoBitmap(
    placesClient: PlacesClient,
    photoMetadata: PhotoMetadata
): Bitmap? {
    return runCatching {
        val request = FetchPhotoRequest.builder(photoMetadata)
            .setMaxWidth(1400)
            .setMaxHeight(1100)
            .build()
        placesClient.fetchPhoto(request).await().bitmap
    }.getOrNull()
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
        userRatingCount = ratingCount,
        photoMetadatas = photoMetadatas.orEmpty()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(navController: NavHostController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val defaultCamera = rememberCameraPositionState()
    var searchQuery by remember { mutableStateOf("") }


    LaunchedEffect(hasLocationPermission){
        if(hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { it?.let{ defaultCamera.move(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 17f)) } }
        }
    }
    Scaffold(topBar = { MapSearchBar(searchQuery = searchQuery, onQueryChanged = { searchQuery = it },onPlaceSelected = { latLng -> defaultCamera.move(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
    } ) },
        bottomBar = { BottomNavBar(navController) }) { innerPadding ->
        GoogleMap(modifier = Modifier.padding(innerPadding).fillMaxSize(), cameraPositionState = defaultCamera, properties = MapProperties(isMyLocationEnabled = hasLocationPermission), uiSettings = MapUiSettings(myLocationButtonEnabled = true))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(navController: NavHostController) {
    Scaffold(topBar = { TopSearchBar() }, bottomBar = { BottomNavBar(navController) }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(16.dp))
            PlaceSaved(navController)
        }
    }
}

@Composable
fun SavedCafeRow(
    cafe: Cafe,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Small thumbnail
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF0F0F0)
            ) {
                AsyncImage(
                    model = cafe.primaryImageModel(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(cafe.name, fontWeight = FontWeight.SemiBold)
                Text(cafe.address, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Bookmark, contentDescription = "Remove bookmark")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSearchBar(modifier: Modifier = Modifier, enabled: Boolean = true) {
    var text by remember { mutableStateOf("") }
    Surface(modifier = modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        TextField(value = text, onValueChange = { text = it }, placeholder = { Text("Search cafes...") }, leadingIcon = { Icon(Icons.Filled.Menu, null) }, trailingIcon = { Icon(Icons.Filled.Search, null) }, singleLine = true, enabled = enabled, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, disabledContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSearchBar(searchQuery: String, onQueryChanged: (String) -> Unit, onPlaceSelected: (LatLng) -> Unit) {
    var recommended by remember { mutableStateOf<List<AutocompletePrediction>> (emptyList()) }
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }

    Column() {
        Surface(modifier = Modifier.fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceVariant)
        {
            TextField(
                value = searchQuery,
                onValueChange = { it ->
                    onQueryChanged(it)

                    if(it.isNotEmpty())
                    {
                        val request = com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest.builder()
                            .setQuery(it)
                            .build()
                        placesClient.findAutocompletePredictions(request)
                            .addOnSuccessListener { response ->
                                recommended = response.autocompletePredictions
                            } .addOnFailureListener { recommended = emptyList() }


                    }else recommended = emptyList()
                },
                placeholder = { Text("Search location...") },
                leadingIcon = { Icon(Icons.Filled.Menu, null) },
                trailingIcon = { IconButton(onClick = { searchQuery }) {Icon(Icons.Filled.Search, null) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, disabledContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
            )

        }
        DropdownMenu( expanded = recommended.isNotEmpty(), onDismissRequest = {recommended = emptyList()},  properties = androidx.compose.ui.window.PopupProperties( focusable = false)) {
            recommended.forEach { prediction -> DropdownMenuItem(text = {Text(prediction.getFullText(null).toString()) },
                onClick = {
                    val request = com.google.android.libraries.places.api.net.FetchPlaceRequest
                        .builder(
                            prediction.placeId,
                            listOf(Place.Field.LAT_LNG)
                        )
                        .build()

                    placesClient.fetchPlace(request)
                        .addOnSuccessListener { response ->
                            response.place.latLng?.let { latLng ->
                                onPlaceSelected(latLng)
                            }
                        }
                })
            }
        }
    }

}

@Composable
fun CafeDetailsScreen(navController: NavHostController, cafeId: String) {
    val cafe = remember(cafeId) { CafeRepository.getCafe(cafeId) }
    val reviews = ReviewRepository.reviewsFor(cafeId)

    val isBookmarked = BookmarkRepository.isBookmarked(cafeId)

    Scaffold(
        bottomBar = { BottomNavBar(navController) },
        containerColor = Color.White
    ) { innerPadding ->
        if (cafe == null) {
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("Cafe not found") }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.width(40.dp)) // keeps title centered-ish

                    Text(
                        cafe.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = { BookmarkRepository.toggle(cafeId) }
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark"
                        )
                    }
                }
            }

            item {
                Text("Address: ${cafe.address}")
                Text("Phone: ${cafe.phone}")
                Text(cafe.status)
                HoursDropdown(cafe.hours)
            }

            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Menu", modifier = Modifier.weight(1f))
                        Icon(Icons.Filled.PlayArrow, null)
                    }
                }
            }

            item { Text("Features:", fontWeight = FontWeight.SemiBold) }
            items(cafe.features) { Text("• $it") }

            item { Text("Ambience:", fontWeight = FontWeight.SemiBold) }
            items(cafe.ambience) { Text("• $it") }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Reviews:", modifier = Modifier.weight(1f))
                    TextButton(onClick = { navController.navigate(Screen.WriteReview.createRoute(cafeId)) }) {
                        Text("Write review")
                    }
                }
            }

            if (reviews.isEmpty()) {
                item { Text("No reviews yet.", color = Color.Gray) }
            } else {
                items(reviews) {
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Text(it, modifier = Modifier.padding(12.dp))
                    }
                }
            }
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
    Scaffold(bottomBar = { BottomNavBar(navController) }, containerColor = Color(0xFFC79A87)) { innerPadding ->
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
fun PlaceSaved(navController: NavHostController) {
    val outlineColor = Color(0xFFE6E6E6)
    val savedCafes = BookmarkRepository.cafes()
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(22.dp)) {
        Column {
            Text("Saved", fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(4.dp)); Box(Modifier.width(80.dp).height(1.dp).background(outlineColor))
            Spacer(Modifier.height(10.dp)); Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (savedCafes.isEmpty()) { SavedTile(Modifier.weight(1f)); SavedTile(Modifier.weight(1f)); SavedTile(Modifier.weight(1f), true) }
            else { savedCafes.take(3).forEach { cafe -> SavedCafeTile(cafe = cafe, navController = navController, modifier = Modifier.weight(1f)) }; if (savedCafes.size < 3) { repeat(3 - savedCafes.size) { if (savedCafes.size + it == 2) SavedTile(Modifier.weight(1f), true) else SavedTile(Modifier.weight(1f)) } } }
        }
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
fun SavedCafeTile(cafe: Cafe, navController: NavHostController, modifier: Modifier = Modifier) {
    val outlineColor = Color(0xFFE6E6E6)
    OutlinedCard(modifier = modifier.aspectRatio(1f), shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, outlineColor)) { Box(modifier = Modifier.fillMaxSize()) { Box(modifier = Modifier.fillMaxSize().clickable { navController.navigate(Screen.CafeDetails.createRoute(cafe.id)) }) { AsyncImage(model = cafe.primaryImageModel(), contentDescription = cafe.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }; IconButton(onClick = { BookmarkRepository.toggle(cafe.id) }, modifier = Modifier.align(Alignment.TopEnd)) { Icon(Icons.Filled.Bookmark, contentDescription = "Remove", tint = Color.White) } } }
}
@Composable
fun PlaceCard(
    cafe: Cafe,
    onOpen: (Rect, Any) -> Unit,
    modifier: Modifier = Modifier,
    transitionAlpha: Float = 1f
) {
    var cardBounds by remember(cafe.id) { mutableStateOf<Rect?>(null) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = transitionAlpha
            }
            .onGloballyPositioned { coordinates ->
                cardBounds = coordinates.boundsInRoot()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(340.dp).clickable {
                onOpen(cardBounds ?: Rect.Zero, cafe.primaryImageModel())
            }) {
                AsyncImage(
                    model = cafe.primaryImageModel(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                val isBookmarked = BookmarkRepository.isBookmarked(cafe.id)
                IconButton(onClick = { BookmarkRepository.toggle(cafe.id) }, modifier = Modifier.align(Alignment.TopStart).padding(10.dp).background(Color.Black.copy(alpha = 0.35f), CircleShape)) {
                    Icon(imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder, contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark", tint = Color.White)
                }
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
                        Text(String.format(Locale.US, "%.1f (%d Reviews)", rating, reviewCount), color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    Text("No ratings yet", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
                Text(cafe.address, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandedCafeDetailOverlay(
    navController: NavHostController,
    cafe: Cafe,
    onClose: () -> Unit,
    cornerRadius: androidx.compose.ui.unit.Dp,
    heroHeight: androidx.compose.ui.unit.Dp,
    sharedHeroModel: Any,
    transitionProgress: Float,
    modifier: Modifier = Modifier
) {
    val reviews = ReviewRepository.reviewsFor(cafe.id)
    val isBookmarked = BookmarkRepository.isBookmarked(cafe.id)
    val pageCount = cafe.photoPageCount()
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val detailRevealAlpha = transitionRevealAlpha(transitionProgress)
    val detailSurfaceAlpha = transitionDetailSurfaceAlpha(transitionProgress)
    val pagerAlpha = ((transitionProgress - 0.22f) / 0.18f).coerceIn(0f, 1f)
    val overlayAlpha = sCurve(((transitionProgress - 0.04f) / 0.34f).coerceIn(0f, 1f))

    Card(
        modifier = modifier.graphicsLayer { alpha = overlayAlpha },
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = detailSurfaceAlpha)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(2.dp, Color(0xFF4A231C).copy(alpha = 0.75f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = sharedHeroModel,
                contentDescription = cafe.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroHeight)
                    .graphicsLayer {
                        // Keep the tapped card's image visible until the detail content has actually appeared.
                        alpha = 1f - detailRevealAlpha
                    },
                contentScale = ContentScale.Crop
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = detailRevealAlpha
                    },
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(heroHeight)
                    ) {
                        AsyncImage(
                            model = sharedHeroModel,
                            contentDescription = cafe.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = 1f - pagerAlpha
                                },
                            contentScale = ContentScale.Crop
                        )

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = pagerAlpha
                                }
                        ) { page ->
                            val pageModel = cafe.photoPageModel(page)
                            if (pageModel != null) {
                                AsyncImage(
                                    model = pageModel,
                                    contentDescription = cafe.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = sharedHeroModel,
                                        contentDescription = cafe.name,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer { alpha = 0.35f },
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF4A231C).copy(alpha = 0.12f))
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = Color(0xFF4A231C))
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("Loading photo...", color = Color(0xFF4A231C))
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                                    )
                                )
                        )

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close details", tint = Color.White)
                        }

                        IconButton(
                            onClick = { BookmarkRepository.toggle(cafe.id) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                                tint = Color.White
                            )
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(horizontal = 20.dp, vertical = 18.dp)
                        ) {
                            Text(
                                text = cafe.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (pageCount > 1) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    repeat(pageCount) { page ->
                                        Box(
                                            modifier = Modifier
                                                .size(if (page == pagerState.currentPage) 9.dp else 7.dp)
                                                .background(
                                                    color = if (page == pagerState.currentPage) Color.White else Color.White.copy(alpha = 0.45f),
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }


                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (cafe.rating != null && (cafe.userRatingCount ?: 0) > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RatingStars(cafe.rating)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = String.format(Locale.US, "%.1f (%d reviews)", cafe.rating, cafe.userRatingCount ?: 0),
                                    color = Color.Gray
                                )
                            }
                        }

                        Text(cafe.status, color = Color(0xFF4A231C), style = MaterialTheme.typography.bodyMedium)
                        Text("Address: ${cafe.address}", style = MaterialTheme.typography.bodyLarge)
                        Text("Phone: ${cafe.phone}", style = MaterialTheme.typography.bodyLarge)
                        HoursDropdown(cafe.hours)
                    }
                }

                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Features", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        cafe.features.forEach { feature ->
                            Text("- $feature", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Ambience", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        cafe.ambience.forEach { vibe ->
                            Text("- $vibe", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        TextButton(onClick = { navController.navigate(Screen.WriteReview.createRoute(cafe.id)) }) {
                            Text("Write review")
                        }
                    }
                }

                if (reviews.isEmpty()) {
                    item {
                        Text(
                            text = "No reviews yet.",
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                } else {
                    items(reviews) { review ->
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {
                            Text(review, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun RatingStars(rating: Float) {
    Row { repeat(rating.toInt()) { Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107)) }; repeat(5 - rating.toInt()) { Icon(Icons.Filled.Star, contentDescription = null, tint = Color.LightGray) } }
}

@Composable
fun BottomNavBar(navController: NavHostController, enabled: Boolean = true, dimFraction: Float = if (enabled) 0f else 1f) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val route = navBackStackEntry?.destination?.route
    NavigationBar(
        modifier = Modifier.graphicsLayer { alpha = 1f - (0.55f * dimFraction) }
    ) {
        NavigationBarItem(selected = route == Screen.MainScreen.route, enabled = enabled, onClick = { navController.navigate(Screen.MainScreen.route) }, icon = { Icon(Icons.Filled.Search, null) })
        NavigationBarItem(selected = route == Screen.MapScreen.route, enabled = enabled, onClick = { navController.navigate(Screen.MapScreen.route) }, icon = { Icon(Icons.Filled.Place, null) })
        NavigationBarItem(selected = route == Screen.BookmarkScreen.route, enabled = enabled, onClick = { navController.navigate(Screen.BookmarkScreen.route) }, icon = { Icon(Icons.Filled.Bookmark, null) })
        NavigationBarItem(selected = route == Screen.ProfileScreen.route, enabled = enabled, onClick = { navController.navigate(Screen.ProfileScreen.route) }, icon = { Icon(Icons.Filled.Person, null) })
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

@Composable
fun SwipeableCafeStack(
    cafes: List<Cafe>,
    transitioningCafeId: String?,
    transitionProgress: Float,
    renderTopCardOnly: Boolean,
    onCafeSelected: (Cafe, Rect, Any) -> Unit,
    enabled: Boolean,
    onSwipeLeft: (Cafe) -> Unit,
    onSwipeRight: (Cafe) -> Unit
) {
    if (cafes.isEmpty()) return
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val swipeThresholdPx = with(density) { 110.dp.toPx() }
    val offscreenTargetPx = with(density) { configuration.screenWidthDp.dp.toPx() * 1.2f }
    val offsetX = remember { Animatable(0f) }
    val cardRotation = remember { Animatable(0f) }
    var exitingCafe by remember { mutableStateOf<Cafe?>(null) }
    var isSwipeTransitionRunning by remember { mutableStateOf(false) }

    val stackCafes = remember(cafes, renderTopCardOnly) {
        if (renderTopCardOnly) cafes.take(1) else cafes
    }

    val renderedCafes = remember(stackCafes, exitingCafe, isSwipeTransitionRunning) {
        if (isSwipeTransitionRunning) {
            stackCafes.filterNot { it.id == exitingCafe?.id }
        } else {
            stackCafes
        }
    }

    if (renderedCafes.isEmpty() && exitingCafe == null) return

    Box(modifier = Modifier.fillMaxWidth(0.9f).aspectRatio(0.58f), contentAlignment = Alignment.Center) {
        // Draw from back to front
        for (i in renderedCafes.indices.reversed()) {
            val cafe = renderedCafes[i]
            key(cafe.id) {
                val isTopCard = i == 0
                val stackScaleTarget = when (i) {
                    0 -> 1f
                    1 -> 0.97f
                    else -> 0.94f
                }
                val stackYOffsetTarget = when (i) {
                    0 -> 0.dp
                    1 -> 6.dp
                    else -> 12.dp
                }
                val animatedScale by animateFloatAsState(
                    targetValue = stackScaleTarget,
                    animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
                    label = "stackScale-${cafe.id}"
                )
                val animatedYOffset by animateDpAsState(
                    targetValue = stackYOffsetTarget,
                    animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
                    label = "stackYOffset-${cafe.id}"
                )
                val transitionAlpha = if (cafe.id == transitioningCafeId) {
                    transitionCardFadeAlpha(transitionProgress)
                } else {
                    1f
                }
                val baseModifier = Modifier
                    .zIndex(if (isTopCard) 100f else 10f - i.toFloat())
                    .offset(y = animatedYOffset)
                    .scale(animatedScale)

                val modifier = if (isTopCard && enabled && !isSwipeTransitionRunning) {
                    baseModifier
                        .graphicsLayer {
                            translationX = offsetX.value
                            rotationZ = cardRotation.value
                        }
                        .pointerInput(cafe.id, isSwipeTransitionRunning, enabled) {
                            if (enabled && !isSwipeTransitionRunning) {
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
                                                isSwipeTransitionRunning = true
                                                exitingCafe = cafe
                                                if (currentOffset > 0) onSwipeRight(cafe) else onSwipeLeft(cafe)
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
                                                exitingCafe = null
                                                offsetX.snapTo(0f)
                                                cardRotation.snapTo(0f)
                                                isSwipeTransitionRunning = false
                                            } else {
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
                        }
                } else {
                    baseModifier
                }
                PlaceCard(
                    cafe = cafe,
                    onOpen = { bounds, heroModel -> onCafeSelected(cafe, bounds, heroModel) },
                    modifier = modifier,
                    transitionAlpha = transitionAlpha
                )
            }
        }

        exitingCafe?.let { overlayCafe ->
            key("overlay-${overlayCafe.id}") {
                PlaceCard(
                    cafe = overlayCafe,
                    onOpen = { bounds, heroModel -> onCafeSelected(overlayCafe, bounds, heroModel) },
                    modifier = Modifier
                        .zIndex(200f)
                        .graphicsLayer {
                            translationX = offsetX.value
                            rotationZ = cardRotation.value
                        },
                    transitionAlpha = 1f
                )
            }
        }
    }
}

// For the user preferences
@Serializable
data class UserPreferences(
    val user_id: String,
    val noise_level: Int,
    val outlet_importance: Int,
    val seating_importance: Int,
    val wifi_importance: Int
)

@Composable
fun PreferencesScreen(navController: NavHostController) {
    val scope = rememberCoroutineScope()

    var noise by remember { mutableIntStateOf(3) }
    var outlet by remember { mutableIntStateOf(3) }
    var seating by remember { mutableIntStateOf(3) }
    var wifi by remember { mutableIntStateOf(3) }

    var isLoading by remember { mutableStateOf(true) }

    // Fonts
    val playfairDisplay = FontFamily(Font(R.font.playfair_display))
    val headlineStyle = MaterialTheme.typography.headlineMedium.copy(fontFamily = playfairDisplay)
    val bodyStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = playfairDisplay)

    // To match the theme
    val backgroundColor = CafeBrown
    val cardColor = Color(0xFFEAD7CE)
    val textColor = Color.Black

    // Load existing preferences
    LaunchedEffect(Unit) {
        val user = supabase.auth.currentUserOrNull()
        if (user != null) {
            try {
                val prefs = supabase
                    .from("user_preferences")
                    .select { filter { eq("user_id", user.id) } }
                    .decodeSingleOrNull<UserPreferences>()

                prefs?.let {
                    noise = it.noise_level
                    outlet = it.outlet_importance
                    seating = it.seating_importance
                    wifi = it.wifi_importance
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = CafeDark)
        }
        return
    }

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(
                text = "Your Preferences",
                style = headlineStyle,
                color = textColor
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Help us find your perfect cafe!☕",
                style = bodyStyle,
                color = textColor.copy(alpha = 0.9f)
            )

            Spacer(Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PreferenceSlider("Noise Level", noise) { noise = it }
                    PreferenceSlider("Outlet Importance", outlet) { outlet = it }
                    PreferenceSlider("Seating Comfort", seating) { seating = it }
                    PreferenceSlider("WiFi Quality", wifi) { wifi = it }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        val user = supabase.auth.currentUserOrNull()
                        if (user != null) {
                            val prefs = UserPreferences(
                                user_id = user.id,
                                noise_level = noise,
                                outlet_importance = outlet,
                                seating_importance = seating,
                                wifi_importance = wifi
                            )
                            try {
                                supabase.from("user_preferences").upsert(prefs)
                                navController.navigate(Screen.MainScreen.route) {
                                    popUpTo(0)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                },
                // Design
                shape = RoundedCornerShape(20.dp),

                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 3.dp
                ),

                colors = ButtonDefaults.buttonColors(
                    containerColor = CafeDark,
                    contentColor = Color.White
                ),

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) // spacing
                    .height(56.dp)
            ) {

                Text("Save Preferences",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
@Composable
fun PreferenceSlider(
    label: String,
    value: Int,
    onChange: (Int) -> Unit
) {
    val playfairDisplay = FontFamily(Font(R.font.playfair_display))

    Column(modifier = Modifier.padding(vertical = 10.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = playfairDisplay)
            )

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = playfairDisplay),
                color = CafeDark
            )
        }

        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = CafeDark,
                activeTrackColor = CafeDark,
                inactiveTrackColor = Color.LightGray
            )
        )
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