package com.example.shuffle_cafe

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.net.Uri
import android.text.TextPaint
import android.text.TextUtils
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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.platform.LocalInspectionMode
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
import com.example.shuffle_cafe.ui.theme.CoffeeDark
import com.example.shuffle_cafe.ui.theme.CoffeeLight
import com.example.shuffle_cafe.ui.theme.Shuffle_CafeTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val supabase by lazy(LazyThreadSafetyMode.NONE) {
    createSupabaseClient(
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
    val distanceMeters: Float? = null,
    val imageResId: Int = R.drawable.ic_launcher_foreground,
    val imageUrl: String? = null,
    val heroImageBitmap: Bitmap? = null,
    val latLng: LatLng? = null,
    val photoMetadatas: List<PhotoMetadata> = emptyList(),
    val photoBitmaps: List<Bitmap?> = List(photoMetadatas.size) { null }
)

@Serializable
internal data class CachedCafeDto(
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val status: String,
    val hours: Map<String, String>,
    val features: List<String>,
    val ambience: List<String>,
    val rating: Float? = null,
    val userRatingCount: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
internal data class CachedCafeEnvelope(
    val cityKey: String,
    val savedAtEpochMillis: Long,
    val cafes: List<CachedCafeDto>
)

data class CafeFeedUiState(
    val cafes: List<Cafe> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val loadError: String? = null,
    val cityKey: String? = null,
    val cityName: String? = null,
    val lastUpdatedEpochMillis: Long? = null
)

private data class ResolvedViewportCoffeeContext(
    val searchLocation: Location,
    val cityName: String?,
    val cityKey: String
)

private data class ResolvedCityCoffeeContext(
    val location: Location,
    val cityName: String?,
    val cityKey: String
)

private data class MapViewportLoadRequest(
    val center: LatLng,
    val searchRadiusMeters: Double,
    val cacheKey: String
)

private data class MarkerDescriptorRequestKey(
    val cafeId: String,
    val title: String,
    val zoomBucket: Int
)

private const val CITY_COFFEE_SEARCH_RADIUS_METERS = 35_000.0
private const val CITY_COFFEE_SEARCH_MAX_RESULTS = 20
private const val CAFE_CACHE_PREFS_NAME = "shuffle_cafe_city_cache"
private const val CAFE_CACHE_ENTRY_PREFIX = "city_cache_"
private const val UNKNOWN_CITY_CACHE_KEY = "nearby_unknown_city"
private const val CAFE_CACHE_MAX_AGE_MILLIS = 7L * 24L * 60L * 60L * 1000L
private const val MAP_VIEWPORT_QUERY_DEBOUNCE_MILLIS = 650L
private const val MAP_VIEWPORT_MIN_QUERY_ZOOM = 6f
private const val MAP_VIEWPORT_SEARCH_MIN_RADIUS_METERS = 1_500.0
private const val MAP_VIEWPORT_SEARCH_MAX_RADIUS_METERS = 18_000.0

private fun LatLng.toLocation(provider: String = "map_viewport"): Location {
    return Location(provider).apply {
        latitude = this@toLocation.latitude
        longitude = this@toLocation.longitude
    }
}

private fun Location.toLatLng(): LatLng = LatLng(latitude, longitude)

internal fun distanceBetweenMeters(start: LatLng, end: LatLng): Float {
    val startLatitudeRadians = Math.toRadians(start.latitude)
    val endLatitudeRadians = Math.toRadians(end.latitude)
    val latitudeDeltaRadians = Math.toRadians(end.latitude - start.latitude)
    val longitudeDeltaRadians = Math.toRadians(end.longitude - start.longitude)

    val haversine = sin(latitudeDeltaRadians / 2).let { it * it } +
        cos(startLatitudeRadians) * cos(endLatitudeRadians) *
        sin(longitudeDeltaRadians / 2).let { it * it }
    val clampedHaversine = haversine.coerceIn(0.0, 1.0)
    val centralAngle = 2 * atan2(sqrt(clampedHaversine), sqrt(1 - clampedHaversine))
    return (6_371_000.0 * centralAngle).toFloat()
}

private fun distanceBetween(start: LatLng, end: LatLng): Float = distanceBetweenMeters(start, end)

internal fun computeCafeDistanceMeters(cafeLatLng: LatLng?, distanceReference: LatLng?): Float? {
    if (cafeLatLng == null || distanceReference == null) return null
    return distanceBetweenMeters(distanceReference, cafeLatLng)
}

internal fun isCafeWithinMapArea(cafeLatLng: LatLng?, searchCenter: LatLng, maxDistanceMeters: Float): Boolean {
    val coordinates = cafeLatLng ?: return false
    return distanceBetweenMeters(searchCenter, coordinates) <= maxDistanceMeters
}

private fun inferCurrentViewportSourceKey(
    currentViewportCafes: List<Cafe>,
    activeViewportKey: String?,
    previousViewportCafeIds: List<String>,
    previousViewportSourceKey: String?,
    previousActiveViewportKey: String?,
    isViewportLoadInFlight: Boolean
): String? {
    if (currentViewportCafes.isEmpty()) {
        return null
    }

    if (activeViewportKey == null) {
        return previousViewportSourceKey
    }

    if (!isViewportLoadInFlight || activeViewportKey == previousActiveViewportKey) {
        return activeViewportKey
    }

    val currentViewportCafeIds = currentViewportCafes.map { cafe -> cafe.id }
    return if (currentViewportCafeIds != previousViewportCafeIds) {
        activeViewportKey
    } else {
        previousViewportSourceKey ?: activeViewportKey
    }
}

private fun estimateViewportSearchRadiusMeters(cameraPositionState: CameraPositionState): Double {
    val zoom = cameraPositionState.position.zoom
    val defaultRadius = when {
        zoom < 8f -> 18_000.0
        zoom < 10f -> 14_000.0
        zoom < 12f -> 10_000.0
        zoom < 14f -> 7_000.0
        zoom < 16f -> 4_500.0
        else -> 2_500.0
    }

    val projection = cameraPositionState.projection ?: return defaultRadius
    val center = cameraPositionState.position.target
    val visibleRegion = projection.visibleRegion
    val farthestCornerDistance = listOf(
        visibleRegion.nearLeft,
        visibleRegion.nearRight,
        visibleRegion.farLeft,
        visibleRegion.farRight
    ).maxOfOrNull { corner -> distanceBetween(center, corner).toDouble() } ?: return defaultRadius

    return (farthestCornerDistance * 1.1)
        .coerceIn(MAP_VIEWPORT_SEARCH_MIN_RADIUS_METERS, MAP_VIEWPORT_SEARCH_MAX_RADIUS_METERS)
}

private fun viewportCoordinateStepDegrees(searchRadiusMeters: Double): Double {
    return when {
        searchRadiusMeters < 3_000.0 -> 0.01
        searchRadiusMeters < 8_000.0 -> 0.02
        else -> 0.05
    }
}

private fun roundViewportCoordinate(value: Double, searchRadiusMeters: Double): Double {
    val step = viewportCoordinateStepDegrees(searchRadiusMeters)
    return (value / step).roundToInt() * step
}

private fun roundViewportSearchRadiusMeters(searchRadiusMeters: Double): Double {
    val bucketSize = when {
        searchRadiusMeters < 3_000.0 -> 500.0
        searchRadiusMeters < 8_000.0 -> 1_000.0
        else -> 2_000.0
    }
    return (searchRadiusMeters / bucketSize).roundToInt() * bucketSize
}

private fun normalizeViewportCacheKey(center: LatLng, searchRadiusMeters: Double): String {
    val roundedLatitude = roundViewportCoordinate(center.latitude, searchRadiusMeters)
    val roundedLongitude = roundViewportCoordinate(center.longitude, searchRadiusMeters)
    val roundedRadius = roundViewportSearchRadiusMeters(searchRadiusMeters).roundToInt()
    return "viewport_${roundedLatitude}_${roundedLongitude}_${roundedRadius}"
}

private fun buildMapViewportLoadRequest(cameraPositionState: CameraPositionState): MapViewportLoadRequest? {
    if (cameraPositionState.position.zoom < MAP_VIEWPORT_MIN_QUERY_ZOOM) return null

    val center = cameraPositionState.position.target
    val rawRadius = estimateViewportSearchRadiusMeters(cameraPositionState)
    val roundedRadius = roundViewportSearchRadiusMeters(rawRadius)
    val roundedCenter = LatLng(
        roundViewportCoordinate(center.latitude, roundedRadius),
        roundViewportCoordinate(center.longitude, roundedRadius)
    )

    return MapViewportLoadRequest(
        center = roundedCenter,
        searchRadiusMeters = roundedRadius,
        cacheKey = normalizeViewportCacheKey(roundedCenter, roundedRadius)
    )
}

private val coffeeHouseQueryTemplates = listOf(
    "coffee house in %s",
    "coffee roasters in %s",
    "espresso bar in %s"
)

private val fallbackCoffeeHouseQueries = listOf(
    "coffee house",
    "coffee roasters",
    "espresso bar"
)

private val coffeeHouseNameKeywords = listOf(
    "coffee",
    "espresso",
    "roast",
    "roastery",
    "latte",
    "brew",
    "bean"
)

private val excludedCoffeeHouseTypes = setOf(
    "bakery",
    "restaurant",
    "meal_takeaway",
    "meal_delivery",
    "bar",
    "lodging",
    "hotel",
    "supermarket",
    "grocery_store"
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

private fun Cafe.mergeLoadedMedia(existing: Cafe?): Cafe {
    val existingCafe = existing ?: return this
    val mergedPhotoMetadatas = if (photoMetadatas.isNotEmpty()) photoMetadatas else existingCafe.photoMetadatas
    val mergedPhotoBitmaps = if (mergedPhotoMetadatas.isNotEmpty()) {
        List(mergedPhotoMetadatas.size) { index ->
            photoBitmaps.getOrNull(index) ?: existingCafe.photoBitmaps.getOrNull(index)
        }
    } else {
        emptyList()
    }

    return copy(
        photoMetadatas = mergedPhotoMetadatas,
        heroImageBitmap = heroImageBitmap
            ?: mergedPhotoBitmaps.firstOrNull { it != null }
            ?: existingCafe.heroImageBitmap,
        photoBitmaps = mergedPhotoBitmaps
    )
}

private fun Cafe.toCachedDto(): CachedCafeDto {
    return CachedCafeDto(
        id = id,
        name = name,
        address = address,
        phone = phone,
        status = status,
        hours = LinkedHashMap(hours),
        features = features,
        ambience = ambience,
        rating = rating,
        userRatingCount = userRatingCount,
        latitude = latLng?.latitude,
        longitude = latLng?.longitude
    )
}

internal fun CachedCafeDto.toCafe(distanceReference: LatLng? = null): Cafe {
    val cafeLatLng = if (latitude != null && longitude != null) LatLng(latitude, longitude) else null
    return Cafe(
        id = id,
        name = name,
        address = address,
        phone = phone,
        status = status,
        hours = LinkedHashMap(hours),
        features = features,
        ambience = ambience,
        rating = rating,
        userRatingCount = userRatingCount,
        distanceMeters = computeCafeDistanceMeters(cafeLatLng, distanceReference),
        latLng = cafeLatLng
    )
}

private fun normalizeCityCacheKey(cityName: String?): String {
    val normalized = cityName
        ?.trim()
        ?.lowercase(Locale.US)
        ?.replace(Regex("[^a-z0-9]+"), "_")
        ?.trim('_')

    return normalized?.takeIf { it.isNotBlank() } ?: UNKNOWN_CITY_CACHE_KEY
}

private fun CachedCafeEnvelope.isFresh(nowMillis: Long = System.currentTimeMillis()): Boolean {
    return nowMillis - savedAtEpochMillis < CAFE_CACHE_MAX_AGE_MILLIS
}

private object MapMarkerDescriptorCache {
    private val descriptors = mutableMapOf<MarkerDescriptorRequestKey, BitmapDescriptor>()

    fun get(key: MarkerDescriptorRequestKey): BitmapDescriptor? = synchronized(this) {
        descriptors[key]
    }

    fun put(key: MarkerDescriptorRequestKey, descriptor: BitmapDescriptor) = synchronized(this) {
        descriptors[key] = descriptor
    }
}

private object CafeCacheStore {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(CAFE_CACHE_PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun cacheEntryKey(cityKey: String): String = "$CAFE_CACHE_ENTRY_PREFIX$cityKey"

    fun load(context: Context, cityKey: String): CachedCafeEnvelope? {
        val encoded = prefs(context).getString(cacheEntryKey(cityKey), null) ?: return null
        return runCatching { json.decodeFromString<CachedCafeEnvelope>(encoded) }.getOrNull()
    }

    fun save(context: Context, cityKey: String, cafes: List<Cafe>) {
        val envelope = CachedCafeEnvelope(
            cityKey = cityKey,
            savedAtEpochMillis = System.currentTimeMillis(),
            cafes = cafes.map { it.toCachedDto() }
        )

        prefs(context)
            .edit()
            .putString(cacheEntryKey(cityKey), json.encodeToString(envelope))
            .apply()
    }
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
    private var homeFeedState by mutableStateOf(CafeFeedUiState())
    private var mapFeedState by mutableStateOf(CafeFeedUiState())
    private var cafeIndexById by mutableStateOf<Map<String, Cafe>>(emptyMap())
    private val homeLoadMutex = Mutex()
    private val mapLoadMutex = Mutex()

    val homeUiState: CafeFeedUiState
        get() = homeFeedState

    val mapUiState: CafeFeedUiState
        get() = mapFeedState

    private fun indexCafes(cafes: List<Cafe>): List<Cafe> {
        if (cafes.isEmpty()) return emptyList()

        val updatedCatalog = cafeIndexById.toMutableMap()
        val indexedCafes = cafes.map { cafe ->
            val mergedCafe = cafe.mergeLoadedMedia(updatedCatalog[cafe.id])
            updatedCatalog[cafe.id] = mergedCafe
            mergedCafe
        }
        cafeIndexById = updatedCatalog
        return indexedCafes
    }

    private fun replaceHomeFeed(
        cafes: List<Cafe>,
        cityKey: String? = homeFeedState.cityKey,
        cityName: String? = homeFeedState.cityName,
        lastUpdatedEpochMillis: Long? = homeFeedState.lastUpdatedEpochMillis
    ) {
        homeFeedState = homeFeedState.copy(
            cafes = indexCafes(cafes),
            cityKey = cityKey,
            cityName = cityName,
            lastUpdatedEpochMillis = lastUpdatedEpochMillis
        )
    }

    private fun replaceMapFeed(
        cafes: List<Cafe>,
        cityKey: String? = mapFeedState.cityKey,
        cityName: String? = mapFeedState.cityName,
        lastUpdatedEpochMillis: Long? = mapFeedState.lastUpdatedEpochMillis
    ) {
        mapFeedState = mapFeedState.copy(
            cafes = indexCafes(cafes),
            cityKey = cityKey,
            cityName = cityName,
            lastUpdatedEpochMillis = lastUpdatedEpochMillis
        )
    }

    private fun updateFeedCafe(cafes: List<Cafe>, cafeId: String, transform: (Cafe) -> Cafe): List<Cafe> {
        var changed = false
        val updatedCafes = cafes.map { cafe ->
            if (cafe.id == cafeId) {
                changed = true
                transform(cafe)
            } else {
                cafe
            }
        }
        return if (changed) updatedCafes else cafes
    }

    private fun applyCafeUpdate(cafeId: String, transform: (Cafe) -> Cafe) {
        cafeIndexById[cafeId]?.let { existingCafe ->
            cafeIndexById = cafeIndexById + (cafeId to transform(existingCafe))
        }

        val updatedHomeCafes = updateFeedCafe(homeFeedState.cafes, cafeId, transform)
        if (updatedHomeCafes !== homeFeedState.cafes) {
            homeFeedState = homeFeedState.copy(cafes = updatedHomeCafes)
        }

        val updatedMapCafes = updateFeedCafe(mapFeedState.cafes, cafeId, transform)
        if (updatedMapCafes !== mapFeedState.cafes) {
            mapFeedState = mapFeedState.copy(cafes = updatedMapCafes)
        }
    }

    fun getCafe(id: String): Cafe? = cafeIndexById[id] ?: fallbackCafes.firstOrNull { it.id == id }

    fun updateCafePhoto(cafeId: String, photoIndex: Int, bitmap: Bitmap) {
        applyCafeUpdate(cafeId) { cafe -> cafe.withLoadedPhoto(photoIndex, bitmap) }
    }

    fun setHomePreviewCafes() {
        if (homeFeedState.cafes.isNotEmpty()) return
        replaceHomeFeed(
            cafes = fallbackCafes,
            cityKey = "preview_home",
            cityName = "Preview"
        )
        homeFeedState = homeFeedState.copy(
            isLoading = false,
            isRefreshing = false,
            loadError = null
        )
    }

    fun setMapPreviewCafes() {
        if (mapFeedState.cafes.isNotEmpty()) return
        replaceMapFeed(
            cafes = fallbackCafes,
            cityKey = "preview_map",
            cityName = "Preview"
        )
        mapFeedState = mapFeedState.copy(
            isLoading = false,
            isRefreshing = false,
            loadError = null
        )
    }

    suspend fun ensureHomeLoaded(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        placesClient: PlacesClient?,
        hasLocationPermission: Boolean
    ) {
        homeLoadMutex.withLock {
            if (!hasLocationPermission) {
                homeFeedState = CafeFeedUiState(
                    cafes = emptyList(),
                    isLoading = false,
                    isRefreshing = false,
                    loadError = "Location permission is required to show coffee houses in your city."
                )
                return
            }

            if (placesClient == null) {
                if (homeFeedState.cafes.isEmpty()) {
                    homeFeedState = CafeFeedUiState(
                        cafes = emptyList(),
                        isLoading = false,
                        isRefreshing = false,
                        loadError = "Places SDK is not initialized."
                    )
                }
                return
            }

            val queryContext = resolveCurrentCityCoffeeContext(context, fusedLocationClient)
            val distanceReference = queryContext.location.toLatLng()
            val cachedEnvelope = CafeCacheStore.load(context, queryContext.cityKey)
            val cachedCafes = cachedEnvelope?.cafes?.map { dto ->
                dto.toCafe(distanceReference = distanceReference)
            }.orEmpty()

            if (
                homeFeedState.cityKey == queryContext.cityKey &&
                homeFeedState.cafes.isNotEmpty() &&
                !homeFeedState.isLoading &&
                !homeFeedState.isRefreshing
            ) {
                return
            }

            if (cachedCafes.isNotEmpty()) {
                replaceHomeFeed(
                    cafes = cachedCafes,
                    cityKey = queryContext.cityKey,
                    cityName = queryContext.cityName,
                    lastUpdatedEpochMillis = cachedEnvelope?.savedAtEpochMillis
                )
                homeFeedState = homeFeedState.copy(
                    isLoading = false,
                    isRefreshing = true,
                    loadError = null
                )
            } else {
                homeFeedState = CafeFeedUiState(
                    cafes = emptyList(),
                    isLoading = true,
                    isRefreshing = false,
                    loadError = null,
                    cityKey = queryContext.cityKey,
                    cityName = queryContext.cityName
                )
            }

            try {
                val freshCafes = fetchCoffeeHousesForResolvedCity(
                    placesClient = placesClient,
                    location = queryContext.location,
                    cityName = queryContext.cityName,
                    onCafePhotoLoaded = ::updateCafePhoto
                ).distinctBy { it.id }

                replaceHomeFeed(
                    cafes = freshCafes,
                    cityKey = queryContext.cityKey,
                    cityName = queryContext.cityName,
                    lastUpdatedEpochMillis = System.currentTimeMillis()
                )
                homeFeedState = homeFeedState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    loadError = null
                )
                CafeCacheStore.save(context, queryContext.cityKey, freshCafes)
            } catch (error: Exception) {
                val errorMessage = error.localizedMessage ?: "Failed to fetch coffee houses in your city."
                if (cachedCafes.isNotEmpty()) {
                    homeFeedState = homeFeedState.copy(
                        isLoading = false,
                        isRefreshing = false,
                        loadError = errorMessage
                    )
                } else {
                    homeFeedState = CafeFeedUiState(
                        cafes = emptyList(),
                        isLoading = false,
                        isRefreshing = false,
                        loadError = errorMessage,
                        cityKey = queryContext.cityKey,
                        cityName = queryContext.cityName
                    )
                }
            }
        }
    }

    suspend fun ensureMapLoadedForViewport(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        placesClient: PlacesClient?,
        hasLocationPermission: Boolean,
        center: LatLng,
        searchRadiusMeters: Double
    ) {
        mapLoadMutex.withLock {
            if (!hasLocationPermission) {
                mapFeedState = CafeFeedUiState(
                    cafes = emptyList(),
                    isLoading = false,
                    isRefreshing = false,
                    loadError = "Location permission is required to show coffee houses on the map."
                )
                return
            }

            if (placesClient == null) {
                if (mapFeedState.cafes.isEmpty()) {
                    mapFeedState = CafeFeedUiState(
                        cafes = emptyList(),
                        isLoading = false,
                        isRefreshing = false,
                        loadError = "Places SDK is not initialized."
                    )
                }
                return
            }

            val queryContext = resolveViewportCoffeeContext(
                context = context,
                center = center,
                searchRadiusMeters = searchRadiusMeters
            )
            val distanceReference = runCatching {
                getCurrentOrLastLocation(fusedLocationClient).toLatLng()
            }.getOrNull()
            val cachedEnvelope = CafeCacheStore.load(context, queryContext.cityKey)
            val cachedCafes = cachedEnvelope?.cafes?.map { dto ->
                dto.toCafe(distanceReference = distanceReference)
            }.orEmpty()

            if (
                mapFeedState.cityKey == queryContext.cityKey &&
                mapFeedState.cafes.isNotEmpty() &&
                !mapFeedState.isLoading &&
                !mapFeedState.isRefreshing
            ) {
                return
            }

            when {
                cachedCafes.isNotEmpty() -> {
                    replaceMapFeed(
                        cafes = cachedCafes,
                        cityKey = queryContext.cityKey,
                        cityName = queryContext.cityName,
                        lastUpdatedEpochMillis = cachedEnvelope?.savedAtEpochMillis
                    )
                    mapFeedState = mapFeedState.copy(
                        isLoading = false,
                        isRefreshing = true,
                        loadError = null
                    )
                }

                mapFeedState.cafes.isNotEmpty() -> {
                    mapFeedState = mapFeedState.copy(
                        isLoading = false,
                        isRefreshing = true,
                        loadError = null,
                        cityKey = queryContext.cityKey,
                        cityName = queryContext.cityName
                    )
                }

                else -> {
                    mapFeedState = CafeFeedUiState(
                        cafes = emptyList(),
                        isLoading = true,
                        isRefreshing = false,
                        loadError = null,
                        cityKey = queryContext.cityKey,
                        cityName = queryContext.cityName
                    )
                }
            }

            try {
                val freshCafes = fetchCoffeeHousesForViewport(
                    placesClient = placesClient,
                    searchLocation = queryContext.searchLocation,
                    distanceReference = distanceReference,
                    cityName = queryContext.cityName,
                    searchRadiusMeters = searchRadiusMeters,
                    onCafePhotoLoaded = ::updateCafePhoto
                ).distinctBy { it.id }

                replaceMapFeed(
                    cafes = freshCafes,
                    cityKey = queryContext.cityKey,
                    cityName = queryContext.cityName,
                    lastUpdatedEpochMillis = System.currentTimeMillis()
                )
                mapFeedState = mapFeedState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    loadError = null
                )
                CafeCacheStore.save(context, queryContext.cityKey, freshCafes)
            } catch (error: Exception) {
                val errorMessage = error.localizedMessage ?: "Failed to load coffee houses for this map area."
                when {
                    cachedCafes.isNotEmpty() -> {
                        mapFeedState = mapFeedState.copy(
                            isLoading = false,
                            isRefreshing = false,
                            loadError = errorMessage
                        )
                    }

                    mapFeedState.cafes.isNotEmpty() -> {
                        mapFeedState = mapFeedState.copy(
                            isLoading = false,
                            isRefreshing = false,
                            loadError = errorMessage
                        )
                    }

                    else -> {
                        mapFeedState = CafeFeedUiState(
                            cafes = emptyList(),
                            isLoading = false,
                            isRefreshing = false,
                            loadError = errorMessage,
                            cityKey = queryContext.cityKey,
                            cityName = queryContext.cityName
                        )
                    }
                }
            }
        }
    }

    internal fun resetForTest() {
        homeFeedState = CafeFeedUiState()
        mapFeedState = CafeFeedUiState()
        cafeIndexById = emptyMap()
    }

    internal fun setHomeFeedForTest(
        cafes: List<Cafe>,
        cityKey: String = "home_test",
        cityName: String = "Home Test"
    ) {
        replaceHomeFeed(cafes = cafes, cityKey = cityKey, cityName = cityName)
        homeFeedState = homeFeedState.copy(isLoading = false, isRefreshing = false, loadError = null)
    }

    internal fun setMapFeedForTest(
        cafes: List<Cafe>,
        cityKey: String = "map_test",
        cityName: String = "Map Test"
    ) {
        replaceMapFeed(cafes = cafes, cityKey = cityKey, cityName = cityName)
        mapFeedState = mapFeedState.copy(isLoading = false, isRefreshing = false, loadError = null)
    }

    internal fun updateCafeForTest(cafeId: String, transform: (Cafe) -> Cafe) {
        applyCafeUpdate(cafeId, transform)
    }
}

object BookmarkRepository {
    // store just IDs
    private val bookmarkedIds = mutableStateListOf<String>()

    fun isBookmarked(cafeId: String): Boolean = bookmarkedIds.contains(cafeId)

    fun add(cafeId: String) {
        bookmarkedIds.remove(cafeId)
        bookmarkedIds.add(0, cafeId)
    }

    fun toggle(cafeId: String) {
        if (bookmarkedIds.contains(cafeId)) bookmarkedIds.remove(cafeId)
        else add(cafeId)
    }

    fun remove(cafeId: String) {
        bookmarkedIds.remove(cafeId)
    }

    // Expose as List so callers can display it
    fun ids(): List<String> = bookmarkedIds.toList()

    fun cafes(): List<Cafe> = bookmarkedIds.toList()
        .mapNotNull { id -> CafeRepository.getCafe(id) }

    internal fun resetForTest() {
        bookmarkedIds.clear()
    }
}

object RecentRepository {
    private val recentIds = mutableStateListOf<String>()

    fun add(cafeId: String) {
        recentIds.remove(cafeId)
        recentIds.add(0, cafeId)
        if (recentIds.size > 20) {
            recentIds.removeAt(recentIds.lastIndex)
        }
    }

    fun cafes(): List<Cafe> {
        return recentIds.mapNotNull { id -> CafeRepository.getCafe(id) }
    }

    fun previewCafes(limit: Int = 3): List<Cafe> {
        return cafes().take(limit)
    }
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
    val inspectionMode = LocalInspectionMode.current
    val scope = rememberCoroutineScope()

    val cafeFeedState = CafeRepository.homeUiState
    val allCafes = cafeFeedState.cafes
    val isLoading = cafeFeedState.isLoading && allCafes.isEmpty()
    val loadError = cafeFeedState.loadError.takeIf { allCafes.isEmpty() }
    var currentVisibleCafes by remember { mutableStateOf<List<Cafe>>(emptyList()) }
    var nextCafeIndex by rememberSaveable { mutableIntStateOf(0) }
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

    LaunchedEffect(hasLocationPermission, placesClient, inspectionMode) {
        if (inspectionMode) {
            CafeRepository.setHomePreviewCafes()
            return@LaunchedEffect
        }

        CafeRepository.ensureHomeLoaded(
            context = context,
            fusedLocationClient = fusedLocationClient,
            placesClient = placesClient,
            hasLocationPermission = hasLocationPermission
        )
    }

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

    val setVisibleStack: (List<Cafe>) -> Unit = { cafes ->
        val visibleStack = buildVisibleCafeStack(cafes)
        currentVisibleCafes = visibleStack.cafes
        nextCafeIndex = visibleStack.nextCafeIndex
    }

    LaunchedEffect(allCafes) {
        if (allCafes.isEmpty()) {
            setVisibleStack(emptyList())
            return@LaunchedEffect
        }

        if (currentVisibleCafes.isEmpty() || nextCafeIndex >= allCafes.size) {
            setVisibleStack(allCafes)
            return@LaunchedEffect
        }

        val updatedVisibleCafes = currentVisibleCafes.mapNotNull { visibleCafe ->
            allCafes.firstOrNull { it.id == visibleCafe.id }
        }
        val expectedVisibleCount = minOf(3, allCafes.size)

        if (updatedVisibleCafes.isEmpty() || updatedVisibleCafes.size != currentVisibleCafes.size) {
            setVisibleStack(allCafes)
            return@LaunchedEffect
        }

        currentVisibleCafes = if (updatedVisibleCafes.size < expectedVisibleCount) {
            val visibleIds = updatedVisibleCafes.map { it.id }.toSet()
            updatedVisibleCafes + allCafes
                .filterNot { it.id in visibleIds }
                .take(expectedVisibleCount - updatedVisibleCafes.size)
        } else {
            updatedVisibleCafes
        }
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
        CafeRepository.updateCafePhoto(cafe.id, nextPhotoIndex, bitmap)
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
            CafeRepository.updateCafePhoto(cafe.id, photoIndex, bitmap)
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
                            Text("Loading coffee houses in your city...")
                        }
                        loadError != null -> {
                            Text(loadError ?: "Unable to load cafes.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                        }
                        allCafes.isEmpty() -> {
                            Text("No coffee houses found in your city.", style = MaterialTheme.typography.headlineSmall)
                        }
                        else -> {
                            val renderTopCardOnly = isPreparingDetailTransition ||
                                    transitionState != null ||
                                    expandedCafeId != null ||
                                    detailProgress.value > 0f
                            val advanceCafeStack: (Cafe) -> Unit = { swipedCafe ->
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
                            val onCafeSwipedLeft: (Cafe) -> Unit = { swipedCafe ->
                                advanceCafeStack(swipedCafe)
                            }
                            val onCafeSwipedRight: (Cafe) -> Unit = { swipedCafe ->
                                BookmarkRepository.add(swipedCafe.id)
                                advanceCafeStack(swipedCafe)
                            }

                            SwipeableCafeStack(
                                cafes = currentVisibleCafes,
                                transitioningCafeId = transitionState?.cafeId,
                                transitionProgress = detailProgress.value,
                                renderTopCardOnly = renderTopCardOnly,
                                onCafeSelected = { cafe, sourceBounds, heroModel ->
                                    RecentRepository.add(cafe.id)
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
                                onSwipeLeft = onCafeSwipedLeft,
                                onSwipeRight = onCafeSwipedRight
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

private suspend fun resolveCurrentCityCoffeeContext(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient
): ResolvedCityCoffeeContext {
    val location = getCurrentOrLastLocation(fusedLocationClient)
    val cityName = resolveCurrentCityName(context, location)
    return ResolvedCityCoffeeContext(
        location = location,
        cityName = cityName,
        cityKey = normalizeCityCacheKey(cityName)
    )
}

private suspend fun resolveViewportCoffeeContext(
    context: Context,
    center: LatLng,
    searchRadiusMeters: Double
): ResolvedViewportCoffeeContext {
    val searchLocation = center.toLocation()
    val cityName = resolveCurrentCityName(context, searchLocation)
    return ResolvedViewportCoffeeContext(
        searchLocation = searchLocation,
        cityName = cityName,
        cityKey = normalizeViewportCacheKey(center, searchRadiusMeters)
    )
}

private suspend fun fetchCoffeeHousesForResolvedCity(
    placesClient: PlacesClient,
    location: Location,
    cityName: String?,
    onCafePhotoLoaded: (String, Int, Bitmap) -> Unit
): List<Cafe> {
    val places = searchCoffeeHousesNearLocation(
        placesClient = placesClient,
        location = location,
        cityName = cityName,
        searchRadiusMeters = CITY_COFFEE_SEARCH_RADIUS_METERS,
        useCitySpecificQueries = !cityName.isNullOrBlank()
    )
    val filteredPlaces = places
        .filter { it.isLikelyCoffeeHouse() }
        .distinctBy { it.id }

    if (filteredPlaces.isEmpty()) {
        throw IllegalStateException(
            if (cityName.isNullOrBlank()) "No coffee houses found near your current city."
            else "No coffee houses found in $cityName."
        )
    }

    filteredPlaces.forEach { place ->
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

    return filteredPlaces
        .mapNotNull { place -> place.toCafe(distanceReference = location.toLatLng()) }
        .sortedBy { it.distanceMeters ?: Float.MAX_VALUE }
}

private suspend fun fetchCoffeeHousesForViewport(
    placesClient: PlacesClient,
    searchLocation: Location,
    distanceReference: LatLng?,
    cityName: String?,
    searchRadiusMeters: Double,
    onCafePhotoLoaded: (String, Int, Bitmap) -> Unit
): List<Cafe> {
    val maxDistanceMeters = (searchRadiusMeters * 1.2).toFloat()
    val searchCenter = searchLocation.toLatLng()
    val places = searchCoffeeHousesNearLocation(
        placesClient = placesClient,
        location = searchLocation,
        cityName = cityName,
        searchRadiusMeters = searchRadiusMeters,
        useCitySpecificQueries = false
    )

    val filteredCafes = places
        .filter { it.isLikelyCoffeeHouse() }
        .distinctBy { it.id }
        .mapNotNull { place -> place.toCafe(distanceReference = distanceReference) }
        .filter { cafe -> isCafeWithinMapArea(cafe.latLng, searchCenter, maxDistanceMeters) }
        .sortedBy { it.distanceMeters ?: Float.MAX_VALUE }

    if (filteredCafes.isEmpty()) {
        throw IllegalStateException(
            if (cityName.isNullOrBlank()) "No coffee houses found in this map area."
            else "No coffee houses found near ${cityName} in this map area."
        )
    }

    filteredCafes.forEach { cafe ->
        val metadata = cafe.photoMetadatas.firstOrNull() ?: return@forEach
        fetchCafePhoto(
            placesClient = placesClient,
            placeId = cafe.id,
            photoIndex = 0,
            photoMetadata = metadata,
            onSuccess = onCafePhotoLoaded
        )
    }

    return filteredCafes
}

@SuppressLint("MissingPermission")
private suspend fun getCurrentOrLastLocation(
    fusedLocationClient: FusedLocationProviderClient
): Location {
    val cancellationTokenSource = CancellationTokenSource()
    return try {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationTokenSource.token).await()
            ?: fusedLocationClient.lastLocation.await()
            ?: throw IllegalStateException("Could not determine current location.")
    } catch (error: Exception) {
        fusedLocationClient.lastLocation.await()
            ?: throw IllegalStateException(error.localizedMessage ?: "Could not determine current location.")
    }
}

private suspend fun resolveCurrentCityName(
    context: Context,
    location: Location
): String? = withContext(Dispatchers.IO) {
    runCatching {
        val geocoder = Geocoder(context, Locale.getDefault())
        @Suppress("DEPRECATION")
        geocoder.getFromLocation(location.latitude, location.longitude, 1)
            ?.firstOrNull()
            ?.let { address ->
                listOf(address.locality, address.subAdminArea, address.adminArea)
                    .firstOrNull { !it.isNullOrBlank() }
                    ?.trim()
            }
    }.getOrNull()
}

private suspend fun searchCoffeeHousesNearLocation(
    placesClient: PlacesClient,
    location: Location,
    cityName: String?,
    searchRadiusMeters: Double,
    useCitySpecificQueries: Boolean
): List<Place> = coroutineScope {
    val placeFields = listOf(
        Place.Field.ID,
        Place.Field.NAME,
        Place.Field.ADDRESS,
        Place.Field.RATING,
        Place.Field.USER_RATINGS_TOTAL,
        Place.Field.PHOTO_METADATAS,
        Place.Field.LAT_LNG,
        Place.Field.PRIMARY_TYPE,
        Place.Field.TYPES,
        Place.Field.OPENING_HOURS,
        Place.Field.CURRENT_OPENING_HOURS
    )
    val locationBias = CircularBounds.newInstance(
        LatLng(location.latitude, location.longitude),
        searchRadiusMeters
    )
    val queries = if (useCitySpecificQueries && !cityName.isNullOrBlank()) {
        coffeeHouseQueryTemplates.map { template -> String.format(Locale.US, template, cityName) }
    } else {
        fallbackCoffeeHouseQueries
    }

    queries
        .map { query ->
            async {
                val request = com.google.android.libraries.places.api.net.SearchByTextRequest
                    .builder(query, placeFields)
                    .setIncludedType("cafe")
                    .setStrictTypeFiltering(true)
                    .setLocationBias(locationBias)
                    .setMaxResultCount(CITY_COFFEE_SEARCH_MAX_RESULTS)
                    .setRankPreference(com.google.android.libraries.places.api.net.SearchByTextRequest.RankPreference.DISTANCE)
                    .build()
                placesClient.searchByText(request).await().places
            }
        }
        .awaitAll()
        .flatten()
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

private fun Place.isLikelyCoffeeHouse(): Boolean {
    val normalizedName = name?.lowercase(Locale.US).orEmpty()
    val primaryTypeValue = primaryType?.lowercase(Locale.US)
    val typeValues = placeTypes.orEmpty().map { it.lowercase(Locale.US) }.toSet()
    val nameLooksCoffeeFocused = coffeeHouseNameKeywords.any { keyword -> normalizedName.contains(keyword) }
    val isCafeTyped = primaryTypeValue == "cafe" || "cafe" in typeValues
    val looksLikeNonCoffeeVenue = primaryTypeValue in excludedCoffeeHouseTypes && !nameLooksCoffeeFocused

    return isCafeTyped && !looksLikeNonCoffeeVenue
}

private fun formatDistanceAway(distanceMeters: Float?): String? {
    if (distanceMeters == null) return null

    return if (distanceMeters < 1609.344f) {
        "${distanceMeters.toInt()} m away"
    } else {
        String.format(Locale.US, "%.1f mi away", distanceMeters / 1609.344f)
    }
}

private fun Cafe.directionsDestination(): String? {
    latLng?.let { coordinates ->
        return "${coordinates.latitude},${coordinates.longitude}"
    }

    return address
        .takeUnless { it.isBlank() || it.equals("Address unavailable", ignoreCase = true) }
        ?: name.takeIf { it.isNotBlank() }
}

private fun openDirectionsInGoogleMaps(context: Context, cafe: Cafe) {
    val destination = cafe.directionsDestination() ?: return
    val encodedDestination = Uri.encode(destination)
    val packageManager = context.packageManager

    val appIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("google.navigation:q=$encodedDestination")
    ).apply {
        setPackage("com.google.android.apps.maps")
    }

    val fallbackIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$encodedDestination")
    )

    val launchIntent = when {
        appIntent.resolveActivity(packageManager) != null -> appIntent
        fallbackIntent.resolveActivity(packageManager) != null -> fallbackIntent
        else -> null
    }

    if (launchIntent != null) {
        try {
            context.startActivity(launchIntent)
        } catch (_: ActivityNotFoundException) {
        }
    }
}

private fun createCafeMarkerDescriptor(
    context: Context,
    title: String,
    markerStyle: CafeMarkerVisualStyle
): BitmapDescriptor {
    val resources = context.resources
    val density = resources.displayMetrics.density
    val scaledDensity = resources.displayMetrics.scaledDensity
    val sourceBitmap = BitmapFactory.decodeResource(resources, R.drawable.shuffel_cafe_coffee_house)
    val markerScale = markerStyle.scale.coerceIn(0.35f, 1f)

    if (sourceBitmap == null) {
        return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
    }

    val iconTargetHeight = (58f * density * markerScale).toInt().coerceAtLeast(1)
    val iconScale = iconTargetHeight.toFloat() / sourceBitmap.height.toFloat()
    val iconWidth = (sourceBitmap.width * iconScale).toInt().coerceAtLeast(1)
    val iconBitmap = Bitmap.createScaledBitmap(sourceBitmap, iconWidth, iconTargetHeight, true)

    if (!markerStyle.showLabel) {
        return BitmapDescriptorFactory.fromBitmap(iconBitmap)
    }

    val horizontalPadding = 12f * density * markerScale
    val verticalPadding = 8f * density * markerScale
    val labelMaxWidth = 190f * density * markerScale
    val labelGap = 6f * density * markerScale
    val bubbleRadius = 16f * density * markerScale
    val strokeWidth = 1.25f * density * markerScale

    val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#4A231C")
        textSize = 13f * scaledDensity * markerScale
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    val maxTextWidth = (labelMaxWidth - (horizontalPadding * 2f)).coerceAtLeast(0f)
    val displayTitle = TextUtils.ellipsize(
        title,
        textPaint,
        maxTextWidth,
        TextUtils.TruncateAt.END
    ).toString()

    val textWidth = textPaint.measureText(displayTitle)
    val bubbleWidth = maxOf(iconWidth.toFloat(), textWidth + (horizontalPadding * 2f))
    val bubbleHeight = textPaint.fontMetrics.let { metrics ->
        (metrics.bottom - metrics.top) + (verticalPadding * 2f)
    }

    val totalWidth = bubbleWidth.toInt().coerceAtLeast(iconWidth)
    val totalHeight = (bubbleHeight + labelGap + iconBitmap.height).toInt().coerceAtLeast(iconBitmap.height)
    val outputBitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(outputBitmap)

    val bubbleLeft = (totalWidth - bubbleWidth) / 2f
    val bubbleTop = 0f
    val bubbleRight = bubbleLeft + bubbleWidth
    val bubbleBottom = bubbleTop + bubbleHeight

    val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
        setShadowLayer(6f * density * markerScale, 0f, 2f * density * markerScale, android.graphics.Color.argb(40, 0, 0, 0))
    }
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#4A231C")
        style = Paint.Style.STROKE
        this.strokeWidth = strokeWidth
    }

    canvas.drawRoundRect(
        bubbleLeft,
        bubbleTop,
        bubbleRight,
        bubbleBottom,
        bubbleRadius,
        bubbleRadius,
        bubblePaint
    )
    canvas.drawRoundRect(
        bubbleLeft,
        bubbleTop,
        bubbleRight,
        bubbleBottom,
        bubbleRadius,
        bubbleRadius,
        borderPaint
    )

    val textBaseline = bubbleTop + verticalPadding - textPaint.fontMetrics.top
    canvas.drawText(
        displayTitle,
        bubbleLeft + ((bubbleWidth - textWidth) / 2f),
        textBaseline,
        textPaint
    )

    val iconLeft = ((totalWidth - iconBitmap.width) / 2f).toFloat()
    val iconTop = bubbleBottom + labelGap
    canvas.drawBitmap(iconBitmap, iconLeft, iconTop, null)

    return BitmapDescriptorFactory.fromBitmap(outputBitmap)
}

private suspend fun buildCafeMarkerDescriptorAsync(
    context: Context,
    key: MarkerDescriptorRequestKey,
    markerStyle: CafeMarkerVisualStyle
): BitmapDescriptor = withContext(Dispatchers.Default) {
    MapMarkerDescriptorCache.get(key) ?: createCafeMarkerDescriptor(context, key.title, markerStyle).also { descriptor ->
        MapMarkerDescriptorCache.put(key, descriptor)
    }
}

private fun parseWeekdayTextToHoursMap(lines: List<String>): LinkedHashMap<String, String> {
    val result = linkedMapOf<String, String>()

    lines.forEach { line ->
        val separatorIndex = line.indexOfFirst { it == ':' || it == '：' }
        if (separatorIndex > 0) {
            val day = line.substring(0, separatorIndex).trim()
            val value = line.substring(separatorIndex + 1).trim()
            if (day.isNotBlank() && value.isNotBlank()) {
                result[day] = value
            }
        }
    }

    return LinkedHashMap(result)
}

private fun Place.toHoursMap(): LinkedHashMap<String, String> {
    val weekdayText = openingHours?.weekdayText
        ?.takeIf { it.isNotEmpty() }
        ?: currentOpeningHours?.weekdayText
            ?.takeIf { it.isNotEmpty() }
        ?: emptyList()

    val parsed = parseWeekdayTextToHoursMap(weekdayText)

    return if (parsed.isNotEmpty()) {
        parsed
    } else {
        linkedMapOf("Hours" to "Hours unavailable")
    }
}

private fun Place.toCafe(distanceReference: LatLng?): Cafe? {
    val placeId = id ?: return null
    val cafeName = name?.trim().takeIf { !it.isNullOrBlank() } ?: return null
    val cafeAddress = address?.trim().takeIf { !it.isNullOrBlank() } ?: "Address unavailable"
    val ratingValue = rating?.toFloat()
    val ratingCount = userRatingsTotal
    val cafeLatLng = latLng
    val distanceToCafe = computeCafeDistanceMeters(cafeLatLng, distanceReference)
    val statusText = if (ratingValue != null && ratingCount != null && ratingCount > 0) {
        String.format(Locale.US, "%.1f (%d reviews)", ratingValue, ratingCount)
    } else {
        "No ratings yet"
    }

    val cafeHours = toHoursMap()

    return Cafe(
        id = placeId,
        name = cafeName,
        address = cafeAddress,
        phone = "Phone unavailable",
        status = statusText,
        hours = cafeHours,
        features = listOf("Coffee house"),
        ambience = listOf("Coffee"),
        rating = ratingValue,
        userRatingCount = ratingCount,
        distanceMeters = distanceToCafe,
        latLng = cafeLatLng,
        photoMetadatas = photoMetadatas.orEmpty()
    )
}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun MapScreen(navController: NavHostController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val placesClient = remember(context) { if (Places.isInitialized()) Places.createClient(context) else null }
    val hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val defaultCamera = rememberCameraPositionState()
    var searchQuery by remember { mutableStateOf("") }
    val cafeFeedState = CafeRepository.mapUiState
    val allCafes = cafeFeedState.cafes
    val isLoading = cafeFeedState.isLoading && allCafes.isEmpty()
    val loadError = cafeFeedState.loadError.takeIf { allCafes.isEmpty() }
    var selectedCafeId by rememberSaveable { mutableStateOf<String?>(null) }
    var overlayCafe by remember { mutableStateOf<Cafe?>(null) }
    val detailProgress = remember { Animatable(0f) }
    val detailOverlayLayoutSpec = remember { DetailOverlayLayoutSpec() }
    val interactionSource = remember { MutableInteractionSource() }
    val inspectionMode = LocalInspectionMode.current
    var isMapLoaded by remember { mutableStateOf(false) }
    val markerDescriptors = remember { mutableStateMapOf<String, BitmapDescriptor>() }
    val currentMarkerStyle by remember(defaultCamera) {
        derivedStateOf { markerVisualStyleForZoom(defaultCamera.position.zoom) }
    }
    val activeViewportKey = cafeFeedState.cityKey
    val isViewportLoadInFlight = cafeFeedState.isLoading || cafeFeedState.isRefreshing
    var renderedMapCafeEntries by remember { mutableStateOf<List<RenderedMapCafeEntry>>(emptyList()) }
    var lastObservedViewportCafeIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var lastObservedViewportSourceKey by remember { mutableStateOf<String?>(null) }
    var lastObservedActiveViewportKey by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(
        allCafes,
        activeViewportKey,
        isViewportLoadInFlight,
        hasLocationPermission,
        inspectionMode
    ) {
        val shouldResetRetainedMapCache = inspectionMode ||
            !hasLocationPermission ||
            (allCafes.isEmpty() && !isViewportLoadInFlight)
        val clearRenderedMapEntries = !hasLocationPermission ||
            (allCafes.isEmpty() && !isViewportLoadInFlight)
        val currentViewportSourceKey = inferCurrentViewportSourceKey(
            currentViewportCafes = allCafes,
            activeViewportKey = activeViewportKey,
            previousViewportCafeIds = if (shouldResetRetainedMapCache) emptyList() else lastObservedViewportCafeIds,
            previousViewportSourceKey = if (shouldResetRetainedMapCache) null else lastObservedViewportSourceKey,
            previousActiveViewportKey = if (shouldResetRetainedMapCache) null else lastObservedActiveViewportKey,
            isViewportLoadInFlight = isViewportLoadInFlight
        )

        renderedMapCafeEntries = reconcileRenderedMapCafeEntries(
            previousEntries = if (shouldResetRetainedMapCache) emptyList() else renderedMapCafeEntries,
            currentViewportCafes = allCafes,
            currentViewportSourceKey = currentViewportSourceKey,
            activeViewportKey = activeViewportKey,
            isViewportLoadInFlight = isViewportLoadInFlight,
            clearEntries = clearRenderedMapEntries
        )

        if (clearRenderedMapEntries) {
            lastObservedViewportCafeIds = emptyList()
            lastObservedViewportSourceKey = null
            lastObservedActiveViewportKey = null
        } else {
            lastObservedViewportCafeIds = allCafes.map { cafe -> cafe.id }
            lastObservedViewportSourceKey = currentViewportSourceKey
            lastObservedActiveViewportKey = activeViewportKey
        }
    }

    val renderedMapCafes = remember(renderedMapCafeEntries) {
        renderedMapCafeEntries.map { entry -> entry.cafe }
    }
    val selectedCafe = remember(allCafes, renderedMapCafes, selectedCafeId) {
        selectedCafeId?.let { cafeId ->
            allCafes.firstOrNull { it.id == cafeId }
                ?: renderedMapCafes.firstOrNull { it.id == cafeId }
                ?: CafeRepository.getCafe(cafeId)
        }
    }
    val loadedSelectedPhotoCount = selectedCafe?.photoBitmaps?.count { it != null } ?: 0
    val cafesWithCoordinates = remember(renderedMapCafes) { renderedMapCafes.filter { it.latLng != null } }

    LaunchedEffect(selectedCafe) {
        if (selectedCafe != null) {
            overlayCafe = selectedCafe
        }
    }

    LaunchedEffect(cafesWithCoordinates, isMapLoaded, currentMarkerStyle) {
        if (!isMapLoaded) return@LaunchedEffect

        val cafeIds = cafesWithCoordinates.map { it.id }.toSet()
        markerDescriptors.keys
            .filterNot { it in cafeIds }
            .forEach(markerDescriptors::remove)

        cafesWithCoordinates.forEach { cafe ->
            val key = MarkerDescriptorRequestKey(
                cafeId = cafe.id,
                title = cafe.name,
                zoomBucket = currentMarkerStyle.zoomBucket
            )
            MapMarkerDescriptorCache.get(key)?.let { descriptor ->
                markerDescriptors[cafe.id] = descriptor
            }
        }

        cafesWithCoordinates.forEach { cafe ->
            val key = MarkerDescriptorRequestKey(
                cafeId = cafe.id,
                title = cafe.name,
                zoomBucket = currentMarkerStyle.zoomBucket
            )
            if (MapMarkerDescriptorCache.get(key) != null) return@forEach

            // Wait until the map is ready before touching BitmapDescriptorFactory.
            val descriptor = runCatching {
                buildCafeMarkerDescriptorAsync(
                    context = context,
                    key = key,
                    markerStyle = currentMarkerStyle
                )
            }.getOrNull()
                ?: return@forEach
            markerDescriptors[cafe.id] = descriptor
        }
    }

    LaunchedEffect(selectedCafeId) {
        if (selectedCafeId != null) {
            detailProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )
        } else if (overlayCafe != null || detailProgress.value > 0f) {
            detailProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            )
            overlayCafe = null
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                it?.let { location ->
                    defaultCamera.move(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude),
                            17f
                        )
                    )
                }
            }
        }
    }

    LaunchedEffect(hasLocationPermission, placesClient, inspectionMode, isMapLoaded) {
        if (inspectionMode) {
            CafeRepository.setMapPreviewCafes()
            return@LaunchedEffect
        }

        if (!hasLocationPermission || placesClient == null) {
            CafeRepository.ensureMapLoadedForViewport(
                context = context,
                fusedLocationClient = fusedLocationClient,
                placesClient = placesClient,
                hasLocationPermission = hasLocationPermission,
                center = defaultCamera.position.target,
                searchRadiusMeters = MAP_VIEWPORT_SEARCH_MAX_RADIUS_METERS
            )
            return@LaunchedEffect
        }

        if (!isMapLoaded) return@LaunchedEffect

        snapshotFlow {
            if (defaultCamera.isMoving) null else buildMapViewportLoadRequest(defaultCamera)
        }
            .debounce(MAP_VIEWPORT_QUERY_DEBOUNCE_MILLIS)
            .filterNotNull()
            .collect { request ->
                if (selectedCafeId != null) {
                    selectedCafeId = null
                }

                CafeRepository.ensureMapLoadedForViewport(
                    context = context,
                    fusedLocationClient = fusedLocationClient,
                    placesClient = placesClient,
                    hasLocationPermission = hasLocationPermission,
                    center = request.center,
                    searchRadiusMeters = request.searchRadiusMeters
                )
            }
    }

    BackHandler(enabled = selectedCafeId != null) {
        selectedCafeId = null
    }

    LaunchedEffect(selectedCafe?.id, loadedSelectedPhotoCount, placesClient) {
        val cafe = selectedCafe ?: return@LaunchedEffect
        val client = placesClient ?: return@LaunchedEffect
        if (cafe.photoMetadatas.isEmpty()) return@LaunchedEffect

        val nextPhotoIndex = cafe.photoBitmaps.indexOfFirst { it == null }
        if (nextPhotoIndex == -1) return@LaunchedEffect

        val bitmap = fetchCafePhotoBitmap(client, cafe.photoMetadatas[nextPhotoIndex]) ?: return@LaunchedEffect
        CafeRepository.updateCafePhoto(cafe.id, nextPhotoIndex, bitmap)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                MapSearchBar(
                    searchQuery = searchQuery,
                    onQueryChanged = { searchQuery = it },
                    onPlaceSelected = { latLng ->
                        defaultCamera.move(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                    }
                )
            },
            bottomBar = {
                BottomNavBar(
                    navController = navController,
                    enabled = detailProgress.value < 0.01f,
                    dimFraction = detailProgress.value
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = defaultCamera,
                    properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                    uiSettings = MapUiSettings(myLocationButtonEnabled = true),
                    onMapLoaded = { isMapLoaded = true },
                    onMapClick = {
                        if (selectedCafeId != null) {
                            selectedCafeId = null
                        }
                    }
                ) {
                    cafesWithCoordinates.forEach { cafe ->
                        key(cafe.id) {
                            val markerState = remember(cafe.id) {
                                com.google.maps.android.compose.MarkerState(position = cafe.latLng!!)
                            }
                            LaunchedEffect(cafe.latLng) {
                                markerState.position = cafe.latLng!!
                            }
                            Marker(
                                state = markerState,
                                icon = markerDescriptors[cafe.id],
                                title = cafe.name,
                                anchor = androidx.compose.ui.geometry.Offset(0.5f, 1f),
                                onClick = {
                                    selectedCafeId = cafe.id
                                    true
                                }
                            )
                        }
                    }
                }

                when {
                    isLoading -> {
                        Surface(
                            modifier = Modifier.align(Alignment.Center),
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White.copy(alpha = 0.94f),
                            tonalElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(Modifier.width(14.dp))
                                Text("Loading coffee houses in your city...")
                            }
                        }
                    }

                    loadError != null -> {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 24.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.96f),
                            tonalElevation = 8.dp
                        ) {
                            Text(
                                text = loadError ?: "Unable to load cafes.",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    allCafes.isEmpty() -> {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 24.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.96f),
                            tonalElevation = 8.dp
                        ) {
                            Text(
                                text = "No coffee houses found in your city.",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }

        if (overlayCafe != null && detailProgress.value > 0f) {
            val scrimAlpha by animateFloatAsState(
                targetValue = 0.2f * detailProgress.value,
                animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
                label = "mapDetailScrimAlpha"
            )
            val overlayScale by animateFloatAsState(
                targetValue = 0.94f + (0.06f * detailProgress.value),
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "mapDetailOverlayScale"
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        selectedCafeId = null
                    }
            )

            ExpandedCafeDetailOverlay(
                navController = navController,
                cafe = overlayCafe!!,
                onClose = { selectedCafeId = null },
                cornerRadius = lerp(
                    detailOverlayLayoutSpec.collapsedCornerRadius,
                    detailOverlayLayoutSpec.expandedCornerRadius,
                    detailProgress.value
                ),
                heroHeight = lerp(
                    detailOverlayLayoutSpec.collapsedHeroHeight,
                    detailOverlayLayoutSpec.expandedHeroHeight,
                    detailProgress.value
                ),
                sharedHeroModel = overlayCafe!!.primaryImageModel(),
                transitionProgress = detailProgress.value,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(
                        start = detailOverlayLayoutSpec.horizontalInset,
                        end = detailOverlayLayoutSpec.horizontalInset,
                        top = detailOverlayLayoutSpec.topInset,
                        bottom = detailOverlayLayoutSpec.bottomInset
                    )
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = detailProgress.value
                        scaleX = overlayScale
                        scaleY = overlayScale
                    }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(navController: NavHostController) {
    val context = LocalContext.current
    val placesClient = remember(context) { if (Places.isInitialized()) Places.createClient(context) else null }
    val savedCafes = BookmarkRepository.cafes()
    var showAllSaved by rememberSaveable { mutableStateOf(false) }
    var selectedSavedCafeId by rememberSaveable { mutableStateOf<String?>(null) }
    var overlayCafe by remember { mutableStateOf<Cafe?>(null) }
    val detailProgress = remember { Animatable(0f) }
    val detailOverlayLayoutSpec = remember { DetailOverlayLayoutSpec() }
    val interactionSource = remember { MutableInteractionSource() }
    val selectedSavedCafe = remember(savedCafes, selectedSavedCafeId) {
        selectedSavedCafeId?.let { cafeId ->
            savedCafes.firstOrNull { it.id == cafeId } ?: CafeRepository.getCafe(cafeId)
        }
    }
    val loadedSelectedPhotoCount = selectedSavedCafe?.photoBitmaps?.count { it != null } ?: 0

    LaunchedEffect(selectedSavedCafe) {
        if (selectedSavedCafe != null) {
            overlayCafe = selectedSavedCafe
        }
    }

    LaunchedEffect(selectedSavedCafeId) {
        if (selectedSavedCafeId != null) {
            detailProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )
        } else if (overlayCafe != null || detailProgress.value > 0f) {
            detailProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            )
            overlayCafe = null
        }
    }

    BackHandler(enabled = selectedSavedCafeId != null) {
        selectedSavedCafeId = null
    }

    LaunchedEffect(selectedSavedCafe?.id, loadedSelectedPhotoCount, placesClient) {
        val cafe = selectedSavedCafe ?: return@LaunchedEffect
        val client = placesClient ?: return@LaunchedEffect
        if (cafe.photoMetadatas.isEmpty()) return@LaunchedEffect

        val nextPhotoIndex = cafe.photoBitmaps.indexOfFirst { it == null }
        if (nextPhotoIndex == -1) return@LaunchedEffect

        val bitmap = fetchCafePhotoBitmap(client, cafe.photoMetadatas[nextPhotoIndex]) ?: return@LaunchedEffect
        CafeRepository.updateCafePhoto(cafe.id, nextPhotoIndex, bitmap)
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
            containerColor = CoffeeLight
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                item {
                    PlaceSaved(
                        savedCafes = savedCafes,
                        showAllSaved = showAllSaved,
                        onToggleShowAllSaved = { showAllSaved = !showAllSaved },
                        onSavedCafeSelected = { cafe ->
                            selectedSavedCafeId = cafe.id
                        }
                    )
                }
            }
        }

        if (overlayCafe != null && detailProgress.value > 0f) {
            val scrimAlpha by animateFloatAsState(
                targetValue = 0.2f * detailProgress.value,
                animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
                label = "savedDetailScrimAlpha"
            )
            val overlayScale by animateFloatAsState(
                targetValue = 0.94f + (0.06f * detailProgress.value),
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "savedDetailOverlayScale"
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        selectedSavedCafeId = null
                    }
            )

            ExpandedCafeDetailOverlay(
                navController = navController,
                cafe = overlayCafe!!,
                onClose = { selectedSavedCafeId = null },
                cornerRadius = lerp(
                    detailOverlayLayoutSpec.collapsedCornerRadius,
                    detailOverlayLayoutSpec.expandedCornerRadius,
                    detailProgress.value
                ),
                heroHeight = lerp(
                    detailOverlayLayoutSpec.collapsedHeroHeight,
                    detailOverlayLayoutSpec.expandedHeroHeight,
                    detailProgress.value
                ),
                sharedHeroModel = overlayCafe!!.primaryImageModel(),
                transitionProgress = detailProgress.value,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(
                        start = detailOverlayLayoutSpec.horizontalInset,
                        end = detailOverlayLayoutSpec.horizontalInset,
                        top = detailOverlayLayoutSpec.topInset,
                        bottom = detailOverlayLayoutSpec.bottomInset
                    )
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = detailProgress.value
                        scaleX = overlayScale
                        scaleY = overlayScale
                    }
            )
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
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
                val reviewCount = cafe.userRatingCount ?: 0
                if (cafe.rating != null && reviewCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RatingStars(cafe.rating)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f", cafe.rating),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                } else {
                    Text("No ratings yet", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Text(
                    text = formatDistanceAway(cafe.distanceMeters) ?: "Distance unavailable",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4A231C)
                )
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Bookmark, contentDescription = "Remove bookmark")
            }
        }
    }
}

@Composable
private fun SavedSectionHeading(title: String) {
    val outlineColor = Color(0xFFE6E6E6)
    Text(title, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(4.dp))
    Box(
        Modifier
            .width(80.dp)
            .height(1.dp)
            .background(outlineColor)
    )
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
    val placesClient = remember(context) { if (Places.isInitialized()) Places.createClient(context) else null }

    Column() {
        Surface(modifier = Modifier.fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceVariant)
        {
            TextField(
                value = searchQuery,
                onValueChange = { it ->
                    onQueryChanged(it)

                    if(it.isNotEmpty() && placesClient != null)
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
                enabled = placesClient != null,
                modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, disabledContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
            )

        }
        DropdownMenu( expanded = recommended.isNotEmpty(), onDismissRequest = {recommended = emptyList()},  properties = androidx.compose.ui.window.PopupProperties( focusable = false)) {
            recommended.forEach { prediction -> DropdownMenuItem(text = {Text(prediction.getFullText(null).toString()) },
                onClick = {
                    val client = placesClient ?: return@DropdownMenuItem
                    val request = com.google.android.libraries.places.api.net.FetchPlaceRequest
                        .builder(
                            prediction.placeId,
                            listOf(Place.Field.LAT_LNG)
                        )
                        .build()

                    client.fetchPlace(request)
                        .addOnSuccessListener { response ->
                            response.place.latLng?.let { latLng ->
                                onPlaceSelected(latLng)
                                recommended = emptyList()
                            }
                        }
                })
            }
        }
    }

}

@Composable
fun CafeDetailsScreen(navController: NavHostController, cafeId: String) {
    val context = LocalContext.current

    LaunchedEffect(cafeId) {
        RecentRepository.add(cafeId)
    }

    val cafe = CafeRepository.getCafe(cafeId)
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
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { openDirectionsInGoogleMaps(context, cafe) },
                    enabled = cafe.directionsDestination() != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Directions, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Directions")
                }
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
    val displayHours = remember(hours) {
        if (hours.isEmpty()) linkedMapOf("Hours" to "Hours unavailable") else hours
    }

    val days = displayHours.keys.toList()
    var expanded by remember { mutableStateOf(false) }
    var selectedDay by remember(displayHours) {
        mutableStateOf(days.firstOrNull().orEmpty())
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = days.isNotEmpty()) { expanded = true },
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val summaryText = if (selectedDay == "Hours") {
                    displayHours[selectedDay].orEmpty()
                } else {
                    "$selectedDay: ${displayHours[selectedDay].orEmpty()}"
                }

                Text(
                    text = summaryText,
                    modifier = Modifier.weight(1f)
                )

                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            days.forEach { day ->
                val label = if (day == "Hours") {
                    displayHours[day].orEmpty()
                } else {
                    "$day: ${displayHours[day].orEmpty()}"
                }

                DropdownMenuItem(
                    text = { Text(label) },
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
fun RecentCafeRow(
    cafe: Cafe,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF0F0F0)
        ) {
            AsyncImage(
                model = cafe.heroImageBitmap ?: cafe.imageUrl ?: cafe.imageResId,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(cafe.name, fontWeight = FontWeight.SemiBold)
            Text(
                cafe.address,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1
            )
        }

        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = "Open"
        )
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
fun PlaceSaved(
    savedCafes: List<Cafe>,
    showAllSaved: Boolean,
    onToggleShowAllSaved: () -> Unit,
    onSavedCafeSelected: (Cafe) -> Unit
) {
    val outlineColor = Color(0xFFE6E6E6)
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(22.dp)) {
        Column {
            SavedSectionHeading("Saved")
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (savedCafes.isEmpty()) {
                    SavedTile(Modifier.weight(1f))
                    SavedTile(Modifier.weight(1f))
                    SavedTile(Modifier.weight(1f), true)
                } else {
                    savedCafes.take(3).forEach { cafe ->
                        SavedCafeTile(
                            cafe = cafe,
                            onOpen = { onSavedCafeSelected(cafe) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (savedCafes.size < 3) {
                        repeat(3 - savedCafes.size) { index ->
                            if (savedCafes.size + index == 2) {
                                SavedTile(Modifier.weight(1f), true)
                            } else {
                                SavedTile(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onToggleShowAllSaved,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CoffeeDark,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, CoffeeDark)
            ) {
                Text(if (showAllSaved) "Hide saved" else "Show all saved")
            }
            if (showAllSaved) {
                Spacer(Modifier.height(12.dp))
                if (savedCafes.isEmpty()) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, outlineColor)
                    ) {
                        Text(
                            text = "No saved places yet. Swipe right on a cafe to add it here.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        savedCafes.forEach { cafe ->
                            SavedCafeRow(
                                cafe = cafe,
                                onOpen = { onSavedCafeSelected(cafe) },
                                onRemove = { BookmarkRepository.remove(cafe.id) }
                            )
                        }
                    }
                }
            }
        }
        Column {
            SavedSectionHeading("Collection")
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    SavedTile()
                    Text("Good Coffee", style = MaterialTheme.typography.bodySmall)
                }
                Column(Modifier.weight(1f)) {
                    SavedTile()
                    Text("Quiet Area", style = MaterialTheme.typography.bodySmall)
                }
                SavedTile(Modifier.weight(1f), true)
            }
        }
        Column {
            SavedSectionHeading("Study Plan")
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SavedTile(Modifier.weight(1f))
                SavedTile(Modifier.weight(1f))
                SavedTile(Modifier.weight(1f), true)
            }
        }
    }
}

@Composable
fun SavedTile(modifier: Modifier = Modifier, plus: Boolean = false) {
    val outlineColor = Color(0xFFE6E6E6)
    OutlinedCard(modifier.aspectRatio(1f), shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, outlineColor)) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { if (plus) Text("+", style = MaterialTheme.typography.headlineLarge) } }
}

@Composable
fun SavedCafeTile(cafe: Cafe, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val outlineColor = Color(0xFFE6E6E6)
    OutlinedCard(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, outlineColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().clickable { onOpen() }) {
                AsyncImage(
                    model = cafe.primaryImageModel(),
                    contentDescription = cafe.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            IconButton(
                onClick = { BookmarkRepository.toggle(cafe.id) },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Filled.Bookmark, contentDescription = "Remove", tint = Color.White)
            }
        }
    }
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
                cafe.distanceMeters?.let { distanceMeters ->
                    Text(
                        text = formatDistanceAway(distanceMeters) ?: "",
                        color = Color(0xFF4A231C),
                        style = MaterialTheme.typography.bodySmall
                    )
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
    val context = LocalContext.current
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
                        cafe.distanceMeters?.let { distanceMeters ->
                            Text(
                                "Distance: ${formatDistanceAway(distanceMeters)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Text("Address: ${cafe.address}", style = MaterialTheme.typography.bodyLarge)
                        Text("Phone: ${cafe.phone}", style = MaterialTheme.typography.bodyLarge)
                        HoursDropdown(cafe.hours)
                        Button(
                            onClick = { openDirectionsInGoogleMaps(context, cafe) },
                            enabled = cafe.directionsDestination() != null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Directions, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Directions")
                        }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recently View",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.width(4.dp))

                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "View more",
                            modifier = Modifier.clickable { }
                        )
                    }
                }

                val recentCafes = RecentRepository.previewCafes()

                if (recentCafes.isEmpty()) {
                    item {
                        Text("No recently viewed cafes yet.", color = Color.Gray)
                    }
                } else {
                    items(recentCafes, key = { it.id }) { cafe ->
                        RecentCafeRow(
                            cafe = cafe,
                            onClick = {
                                navController.navigate(Screen.CafeDetails.createRoute(cafe.id))
                            }
                        )
                    }
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
