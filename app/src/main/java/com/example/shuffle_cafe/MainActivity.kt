package com.example.shuffle_cafe

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
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
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
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
import com.example.shuffle_cafe.ui.theme.CoffeeSurfaceLight
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
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.UUID
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
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
    val distanceMeters: Float? = null,
    val imageResId: Int? = null,
    val imageUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val expectedPhotoCount: Int? = null,
    val heroImageBase64: String? = null,
    val photoImageBase64s: List<String?> = emptyList()
)

@Serializable
internal data class CachedCafeEnvelope(
    val cityKey: String,
    val savedAtEpochMillis: Long,
    val cafes: List<CachedCafeDto>
)

@Serializable
internal data class CachedBookmarkEnvelope(
    val savedAtEpochMillis: Long,
    val cafes: List<CachedCafeDto>
)

private data class CafeCacheMetadata(
    val homeCityKey: String?,
    val homeCityName: String?,
    val mapViewportKey: String?,
    val mapCityName: String?
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

internal fun CafeFeedUiState.hasFreshLoadedCafes(
    nowMillis: Long = System.currentTimeMillis()
): Boolean {
    val lastUpdated = lastUpdatedEpochMillis ?: return false
    if (cafes.isEmpty() || isLoading || isRefreshing) return false
    return nowMillis - lastUpdated < CAFE_RESULT_CACHE_TTL_MILLIS
}

internal fun CafeFeedUiState.hasReusableLoadedCafesForCity(cityKey: String?): Boolean {
    if (cityKey.isNullOrBlank()) return false
    return this.cityKey == cityKey &&
        cafes.isNotEmpty() &&
        !isLoading &&
        !isRefreshing
}

internal fun CafeFeedUiState.shouldPersistActiveCacheForCafe(cafeId: String): Boolean {
    val key = cityKey ?: return false
    return isPersistableCafeCacheKey(key) && cafes.any { cafe -> cafe.id == cafeId }
}

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

private enum class CardsFilterOption(val label: String) {
    Nearby("Nearby"),
    Trending("Trending"),
    Preferences("Preferences")
}

internal enum class CacheImageDecodeMode {
    HeroOnly,
    All
}

private enum class CafeCacheMetadataTarget {
    Home,
    Map
}

private const val CITY_COFFEE_SEARCH_RADIUS_METERS = 35_000.0
private const val CITY_COFFEE_SEARCH_MAX_RESULTS = 20
private const val CAFE_CACHE_PREFS_NAME = "shuffle_cafe_city_cache"
private const val CAFE_CACHE_ENTRY_PREFIX = "city_cache_"
private const val CAFE_CACHE_METADATA_HOME_CITY_KEY = "metadata_home_city_key"
private const val CAFE_CACHE_METADATA_HOME_CITY_NAME = "metadata_home_city_name"
private const val CAFE_CACHE_METADATA_MAP_VIEWPORT_KEY = "metadata_map_viewport_key"
private const val CAFE_CACHE_METADATA_MAP_CITY_NAME = "metadata_map_city_name"
private const val BOOKMARK_CACHE_PREFS_NAME = "shuffle_cafe_bookmark_cache"
private const val BOOKMARK_CACHE_ENTRY_KEY = "saved_bookmark_cafes"
private const val CARD_STACK_PROGRESS_PREFS_NAME = "shuffle_cafe_card_stack_progress"
private const val CARD_STACK_TOP_CAFE_ID_KEY = "top_cafe_id"
private const val CAFE_CACHE_MAX_CAFES_PER_ENTRY = 20
private const val CAFE_CACHE_IMAGE_MAX_DIMENSION_PX = 360
private const val CAFE_CACHE_IMAGE_JPEG_QUALITY = 62
private const val CAFE_CACHE_IMAGE_BASE64_BUDGET_CHARS = 1_500_000
private const val CAFE_INDEX_MAX_ENTRIES = 80
private const val MAP_SESSION_CACHE_MAX_CAFES = 80
private const val MAP_SESSION_CACHE_MAX_VIEWPORTS = 8
private const val MAP_MARKER_DESCRIPTOR_CACHE_MAX_ENTRIES = 120
private const val CAFE_DETAIL_MAX_LOADED_PHOTOS = 4
private const val CAFE_CARD_PHOTO_MAX_WIDTH = 720
private const val CAFE_CARD_PHOTO_MAX_HEIGHT = 560
private const val CAFE_DETAIL_PHOTO_MAX_WIDTH = 1100
private const val CAFE_DETAIL_PHOTO_MAX_HEIGHT = 850
private const val UNKNOWN_CITY_CACHE_KEY = "nearby_unknown_city"
private const val ADDRESS_UNAVAILABLE_TEXT = "Address unavailable"
private const val PHONE_UNAVAILABLE_TEXT = "Phone unavailable"
private const val HOURS_UNAVAILABLE_TEXT = "Hours unavailable"
private const val NO_RATINGS_TEXT = "No ratings yet"
internal const val CAFE_RESULT_CACHE_TTL_MILLIS = 30L * 60L * 1000L
internal const val SAVED_CAFE_CACHE_ATTEMPT_TTL_MILLIS = CAFE_RESULT_CACHE_TTL_MILLIS
private const val BOOKMARK_CACHE_WRITE_DEBOUNCE_MILLIS = 350L
private const val ACTIVE_CAFE_CACHE_WRITE_DEBOUNCE_MILLIS = 500L
private const val MAP_VIEWPORT_QUERY_DEBOUNCE_MILLIS = 650L
private const val MAP_VIEWPORT_MIN_QUERY_ZOOM = 6f
private const val MAP_VIEWPORT_SEARCH_MIN_RADIUS_METERS = 1_500.0
private const val MAP_VIEWPORT_SEARCH_MAX_RADIUS_METERS = 18_000.0
internal const val MAP_SESSION_CACHE_TTL_MILLIS = CAFE_RESULT_CACHE_TTL_MILLIS

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

private val cafePhonePlaceFields = listOf(
    Place.Field.INTERNATIONAL_PHONE_NUMBER,
    Place.Field.NATIONAL_PHONE_NUMBER
)

private val cafeDetailPlaceFields = listOf(
    Place.Field.ID,
    Place.Field.DISPLAY_NAME,
    Place.Field.FORMATTED_ADDRESS,
    Place.Field.RATING,
    Place.Field.USER_RATING_COUNT,
    Place.Field.PHOTO_METADATAS,
    Place.Field.LOCATION,
    Place.Field.OPENING_HOURS,
    Place.Field.CURRENT_OPENING_HOURS
) + cafePhonePlaceFields

private fun Cafe.primaryImageModel(): Any = heroImageBitmap ?: imageUrl ?: imageResId

private fun Cafe.photoPageCount(): Int = when {
    photoMetadatas.isNotEmpty() -> minOf(photoMetadatas.size, CAFE_DETAIL_MAX_LOADED_PHOTOS)
    photoBitmaps.isNotEmpty() -> photoBitmaps.size
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

    val retainedPhotoCount = minOf(photoMetadatas.size, CAFE_DETAIL_MAX_LOADED_PHOTOS)
    val updatedPhotos = if (photoBitmaps.size == retainedPhotoCount) {
        photoBitmaps.toMutableList()
    } else {
        MutableList(retainedPhotoCount) { photoBitmaps.getOrNull(it) }
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
        List(minOf(mergedPhotoMetadatas.size, CAFE_DETAIL_MAX_LOADED_PHOTOS)) { index ->
            photoBitmaps.getOrNull(index) ?: existingCafe.photoBitmaps.getOrNull(index)
        }
    } else if (photoBitmaps.isNotEmpty() || existingCafe.photoBitmaps.isNotEmpty()) {
        val mergedPhotoCount = maxOf(photoBitmaps.size, existingCafe.photoBitmaps.size)
        List(mergedPhotoCount) { index ->
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

internal fun isCafeAddressUnavailable(address: String): Boolean {
    return address.isBlank() || address.equals(ADDRESS_UNAVAILABLE_TEXT, ignoreCase = true)
}

internal fun areCafeHoursUnavailable(hours: Map<String, String>): Boolean {
    return hours.isEmpty() || hours.values.all { value ->
        value.isBlank() || value.equals(HOURS_UNAVAILABLE_TEXT, ignoreCase = true)
    }
}

internal fun isCafeDetailDataIncomplete(cafe: Cafe): Boolean {
    return isPhoneUnavailable(cafe.phone) ||
        cafe.photoMetadatas.isEmpty() ||
        isCafeAddressUnavailable(cafe.address) ||
        cafe.latLng == null ||
        cafe.rating == null ||
        cafe.userRatingCount == null ||
        areCafeHoursUnavailable(cafe.hours)
}

private fun Cafe.hasCachedImages(): Boolean {
    return heroImageBitmap != null || photoBitmaps.any { bitmap -> bitmap != null } || !imageUrl.isNullOrBlank()
}

internal fun isBookmarkCacheDataIncomplete(cafe: Cafe): Boolean {
    return isPhoneUnavailable(cafe.phone) ||
        isCafeAddressUnavailable(cafe.address) ||
        cafe.latLng == null ||
        cafe.rating == null ||
        cafe.userRatingCount == null ||
        areCafeHoursUnavailable(cafe.hours) ||
        !cafe.hasCachedImages()
}

private fun Cafe.nextUnloadedPhotoIndex(): Int? {
    if (photoMetadatas.isEmpty()) return null
    return photoMetadatas.indices
        .take(CAFE_DETAIL_MAX_LOADED_PHOTOS)
        .firstOrNull { index -> photoBitmaps.getOrNull(index) == null }
}

internal fun mergeCafeDetailData(existingCafe: Cafe, detailCafe: Cafe): Cafe {
    val mergedPhotoMetadatas = if (detailCafe.photoMetadatas.isNotEmpty()) {
        detailCafe.photoMetadatas
    } else {
        existingCafe.photoMetadatas
    }
    val mergedPhotoBitmaps = if (mergedPhotoMetadatas.isNotEmpty()) {
        List(minOf(mergedPhotoMetadatas.size, CAFE_DETAIL_MAX_LOADED_PHOTOS)) { index ->
            detailCafe.photoBitmaps.getOrNull(index) ?: existingCafe.photoBitmaps.getOrNull(index)
        }
    } else if (detailCafe.photoBitmaps.isNotEmpty() || existingCafe.photoBitmaps.isNotEmpty()) {
        val mergedPhotoCount = maxOf(detailCafe.photoBitmaps.size, existingCafe.photoBitmaps.size)
        List(mergedPhotoCount) { index ->
            detailCafe.photoBitmaps.getOrNull(index) ?: existingCafe.photoBitmaps.getOrNull(index)
        }
    } else {
        emptyList()
    }
    val detailHasRating = detailCafe.rating != null && (detailCafe.userRatingCount ?: 0) > 0
    val mergedStatus = when {
        detailHasRating -> detailCafe.status
        existingCafe.status.isNotBlank() && !existingCafe.status.equals(NO_RATINGS_TEXT, ignoreCase = true) -> existingCafe.status
        detailCafe.status.isNotBlank() -> detailCafe.status
        else -> existingCafe.status
    }

    return existingCafe.copy(
        name = detailCafe.name.trim().takeIf { it.isNotBlank() } ?: existingCafe.name,
        address = detailCafe.address.takeUnless(::isCafeAddressUnavailable) ?: existingCafe.address,
        phone = detailCafe.phone.takeUnless(::isPhoneUnavailable) ?: existingCafe.phone,
        status = mergedStatus,
        hours = if (!areCafeHoursUnavailable(detailCafe.hours)) detailCafe.hours else existingCafe.hours,
        rating = detailCafe.rating ?: existingCafe.rating,
        userRatingCount = detailCafe.userRatingCount ?: existingCafe.userRatingCount,
        distanceMeters = existingCafe.distanceMeters ?: detailCafe.distanceMeters,
        imageResId = existingCafe.imageResId,
        imageUrl = detailCafe.imageUrl ?: existingCafe.imageUrl,
        heroImageBitmap = existingCafe.heroImageBitmap
            ?: detailCafe.heroImageBitmap
            ?: mergedPhotoBitmaps.firstOrNull { it != null },
        latLng = detailCafe.latLng ?: existingCafe.latLng,
        photoMetadatas = mergedPhotoMetadatas,
        photoBitmaps = mergedPhotoBitmaps
    )
}

private fun Cafe.withDistanceReference(distanceReference: LatLng?): Cafe {
    return copy(distanceMeters = computeCafeDistanceMeters(latLng, distanceReference) ?: distanceMeters)
}

private data class MapSessionCafeCacheEntry(
    val cafe: Cafe,
    val savedAtEpochMillis: Long
)

internal class MapSessionCafeCache(
    private val ttlMillis: Long = MAP_SESSION_CACHE_TTL_MILLIS,
    private val maxCafeEntries: Int = MAP_SESSION_CACHE_MAX_CAFES,
    private val maxViewportEntries: Int = MAP_SESSION_CACHE_MAX_VIEWPORTS,
    private val nowMillis: () -> Long = { System.currentTimeMillis() }
) {
    private val cafesById = linkedMapOf<String, MapSessionCafeCacheEntry>()
    private val viewportLoadedAtMillis = mutableMapOf<String, Long>()

    fun recordViewport(
        viewportKey: String,
        cafes: List<Cafe>,
        savedAtEpochMillis: Long = nowMillis()
    ) {
        if (viewportKey.isBlank() || cafes.isEmpty()) return

        val now = nowMillis()
        prune(now)
        viewportLoadedAtMillis[viewportKey] = savedAtEpochMillis
        cafes.distinctBy { cafe -> cafe.id }.forEach { cafe ->
            val existingCafe = cafesById.remove(cafe.id)?.cafe
            cafesById[cafe.id] = MapSessionCafeCacheEntry(
                cafe = cafe.mergeLoadedMedia(existingCafe),
                savedAtEpochMillis = savedAtEpochMillis
            )
        }
        trimToSize()
    }

    fun isViewportFresh(viewportKey: String): Boolean {
        if (viewportKey.isBlank()) return false

        val now = nowMillis()
        prune(now)
        val loadedAt = viewportLoadedAtMillis[viewportKey] ?: return false
        return isFresh(loadedAt, now)
    }

    fun latestLoadedAtEpochMillis(): Long? {
        val now = nowMillis()
        prune(now)
        return viewportLoadedAtMillis.values.maxOrNull()
    }

    fun snapshot(distanceReference: LatLng? = null): List<Cafe> {
        val now = nowMillis()
        prune(now)
        return cafesById.values
            .map { entry -> entry.cafe.withDistanceReference(distanceReference) }
            .sortedWith(
                compareBy<Cafe> { cafe -> cafe.distanceMeters ?: Float.MAX_VALUE }
                    .thenBy { cafe -> cafe.name }
            )
    }

    fun updateCafe(cafeId: String, transform: (Cafe) -> Cafe) {
        val existingEntry = cafesById[cafeId] ?: return
        cafesById[cafeId] = existingEntry.copy(cafe = transform(existingEntry.cafe))
    }

    fun clear() {
        cafesById.clear()
        viewportLoadedAtMillis.clear()
    }

    private fun prune(now: Long) {
        cafesById.entries.removeAll { (_, entry) -> !isFresh(entry.savedAtEpochMillis, now) }
        viewportLoadedAtMillis.entries.removeAll { (_, loadedAt) -> !isFresh(loadedAt, now) }
    }

    private fun trimToSize() {
        val maxCafes = maxCafeEntries.coerceAtLeast(1)
        while (cafesById.size > maxCafes) {
            val oldestCafeId = cafesById.entries.firstOrNull()?.key ?: break
            cafesById.remove(oldestCafeId)
        }

        val maxViewports = maxViewportEntries.coerceAtLeast(1)
        if (viewportLoadedAtMillis.size > maxViewports) {
            viewportLoadedAtMillis.entries
                .sortedBy { (_, loadedAt) -> loadedAt }
                .take(viewportLoadedAtMillis.size - maxViewports)
                .forEach { (viewportKey, _) -> viewportLoadedAtMillis.remove(viewportKey) }
        }
    }

    private fun isFresh(savedAt: Long, now: Long): Boolean {
        return now - savedAt < ttlMillis
    }
}

private fun Bitmap.toCacheBase64(): String? {
    return runCatching {
        val bitmapForCache = scaledForCafeCache()
        val output = ByteArrayOutputStream()
        val didCompress = bitmapForCache.compress(
            Bitmap.CompressFormat.JPEG,
            CAFE_CACHE_IMAGE_JPEG_QUALITY,
            output
        )
        if (!didCompress) return@runCatching null
        val encoded = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        if (bitmapForCache !== this) {
            bitmapForCache.recycle()
        }
        encoded
    }.getOrNull()
}

private fun Bitmap.scaledForCafeCache(): Bitmap {
    val sourceWidth = width
    val sourceHeight = height
    if (sourceWidth <= 0 || sourceHeight <= 0) return this

    val longestSide = maxOf(sourceWidth, sourceHeight)
    if (longestSide <= CAFE_CACHE_IMAGE_MAX_DIMENSION_PX) return this

    val scale = CAFE_CACHE_IMAGE_MAX_DIMENSION_PX.toFloat() / longestSide.toFloat()
    val scaledWidth = (sourceWidth * scale).roundToInt().coerceAtLeast(1)
    val scaledHeight = (sourceHeight * scale).roundToInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(this, scaledWidth, scaledHeight, true)
}

private fun String.toCachedBitmap(): Bitmap? {
    return runCatching {
        val decoded = Base64.decode(this, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
    }.getOrNull()
}

private fun CachedCafeDto.hasCompleteCachedCardImage(): Boolean {
    val expectedCount = expectedPhotoCount ?: return false
    if (expectedCount <= 0) return true
    return heroImageBase64 != null || photoImageBase64s.firstOrNull() != null
}

internal fun CachedCafeEnvelope.hasCompleteCachedCardImages(): Boolean {
    return cafes.isNotEmpty() && cafes.all { dto -> dto.hasCompleteCachedCardImage() }
}

internal fun CachedCafeEnvelope.canRestoreFreshFeed(nowMillis: Long = System.currentTimeMillis()): Boolean {
    return cafes.isNotEmpty() && isFresh(nowMillis)
}

internal fun CachedCafeEnvelope.needsHomeRefreshAfterRestore(
    nowMillis: Long = System.currentTimeMillis()
): Boolean {
    return !isFresh(nowMillis) || !hasCompleteCachedCardImages()
}

private fun Cafe.toCachedDto(): CachedCafeDto {
    val cachedHeroBitmap = heroImageBitmap ?: photoBitmaps.firstOrNull { it != null }
    val expectedPhotoCount = maxOf(
        photoMetadatas.size,
        if (cachedHeroBitmap != null) 1 else 0
    )
    val cachedHeroBase64 = cachedHeroBitmap?.toCacheBase64()

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
        distanceMeters = distanceMeters,
        imageResId = imageResId,
        imageUrl = imageUrl,
        latitude = latLng?.latitude,
        longitude = latLng?.longitude,
        expectedPhotoCount = expectedPhotoCount,
        heroImageBase64 = cachedHeroBase64,
        photoImageBase64s = emptyList()
    )
}

private fun List<Cafe>.toCachedDtosForStorage(
    maxCafes: Int,
    imageBase64BudgetChars: Int = CAFE_CACHE_IMAGE_BASE64_BUDGET_CHARS
): List<CachedCafeDto> {
    var remainingImageBudget = imageBase64BudgetChars
    return take(maxCafes).map { cafe ->
        val dto = cafe.toCachedDto()
        val imageCharCount = dto.cachedImageBase64CharCount()
        if (imageCharCount <= 0) {
            dto
        } else if (imageCharCount <= remainingImageBudget) {
            remainingImageBudget -= imageCharCount
            dto
        } else {
            dto.withoutCachedImages()
        }
    }
}

private fun CachedCafeDto.cachedImageBase64CharCount(): Int {
    return (heroImageBase64?.length ?: 0) + photoImageBase64s.sumOf { encoded -> encoded?.length ?: 0 }
}

private fun CachedCafeDto.withoutCachedImages(): CachedCafeDto {
    return copy(heroImageBase64 = null, photoImageBase64s = emptyList())
}

internal fun CachedCafeDto.toCafe(
    distanceReference: LatLng? = null,
    imageDecodeMode: CacheImageDecodeMode = CacheImageDecodeMode.All
): Cafe {
    val cafeLatLng = if (latitude != null && longitude != null) LatLng(latitude, longitude) else null
    val cachedHeroBitmap = when (imageDecodeMode) {
        CacheImageDecodeMode.HeroOnly -> (heroImageBase64 ?: photoImageBase64s.firstOrNull { encoded -> encoded != null })
            ?.toCachedBitmap()
        CacheImageDecodeMode.All -> heroImageBase64?.toCachedBitmap()
    }
    val restoredPhotoBitmaps = when (imageDecodeMode) {
        CacheImageDecodeMode.HeroOnly -> cachedHeroBitmap?.let { bitmap -> listOf(bitmap) }.orEmpty()
        CacheImageDecodeMode.All -> {
            val cachedPhotoBitmaps = photoImageBase64s.map { encoded -> encoded?.toCachedBitmap() }
            when {
                cachedPhotoBitmaps.isNotEmpty() -> cachedPhotoBitmaps
                cachedHeroBitmap != null -> listOf(cachedHeroBitmap)
                else -> emptyList()
            }
        }
    }

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
        distanceMeters = computeCafeDistanceMeters(cafeLatLng, distanceReference) ?: distanceMeters,
        imageResId = imageResId ?: R.drawable.ic_launcher_foreground,
        imageUrl = imageUrl,
        heroImageBitmap = cachedHeroBitmap ?: restoredPhotoBitmaps.firstOrNull { it != null },
        latLng = cafeLatLng,
        photoBitmaps = restoredPhotoBitmaps
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

private fun isPersistableCafeCacheKey(cityKey: String): Boolean {
    return cityKey.isNotBlank() && !cityKey.startsWith("preview_")
}

internal fun CachedCafeEnvelope.isFresh(nowMillis: Long = System.currentTimeMillis()): Boolean {
    return nowMillis - savedAtEpochMillis < CAFE_RESULT_CACHE_TTL_MILLIS
}

private object MapMarkerDescriptorCache {
    private val descriptors = object : java.util.LinkedHashMap<MarkerDescriptorRequestKey, BitmapDescriptor>(
        MAP_MARKER_DESCRIPTOR_CACHE_MAX_ENTRIES,
        0.75f,
        true
    ) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<MarkerDescriptorRequestKey, BitmapDescriptor>?
        ): Boolean {
            return size > MAP_MARKER_DESCRIPTOR_CACHE_MAX_ENTRIES
        }
    }

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
        return runCatching {
            prefs(context)
                .getString(cacheEntryKey(cityKey), null)
                ?.let { encoded -> json.decodeFromString<CachedCafeEnvelope>(encoded) }
        }.getOrNull()
    }

    fun save(context: Context, cityKey: String, cafes: List<Cafe>) {
        val envelope = CachedCafeEnvelope(
            cityKey = cityKey,
            savedAtEpochMillis = System.currentTimeMillis(),
            cafes = cafes.toCachedDtosForStorage(maxCafes = CAFE_CACHE_MAX_CAFES_PER_ENTRY)
        )

        prefs(context)
            .edit()
            .putString(cacheEntryKey(cityKey), json.encodeToString(envelope))
            .apply()
    }

    fun pruneToCurrentMetadata(context: Context, extraCityKeysToKeep: Set<String> = emptySet()) {
        val preferences = prefs(context)
        val metadata = CafeCacheMetadataStore.load(context)
        val cityKeysToKeep = buildSet {
            addAll(extraCityKeysToKeep.filter { cityKey -> cityKey.isNotBlank() })
            metadata.homeCityKey?.takeIf { it.isNotBlank() }?.let(::add)
            metadata.mapViewportKey?.takeIf { it.isNotBlank() }?.let(::add)
        }
        val cacheEntryKeysToKeep = cityKeysToKeep.map(::cacheEntryKey).toSet()
        val staleEntryKeys = preferences.all.keys.filter { key ->
            key.startsWith(CAFE_CACHE_ENTRY_PREFIX) && key !in cacheEntryKeysToKeep
        }
        if (staleEntryKeys.isEmpty()) return

        preferences.edit().apply {
            staleEntryKeys.forEach { key -> remove(key) }
        }.apply()
    }
}

private object CafeCacheMetadataStore {
    private fun prefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(CAFE_CACHE_PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun load(context: Context): CafeCacheMetadata {
        return runCatching {
            val preferences = prefs(context)
            CafeCacheMetadata(
                homeCityKey = preferences.getString(CAFE_CACHE_METADATA_HOME_CITY_KEY, null),
                homeCityName = preferences.getString(CAFE_CACHE_METADATA_HOME_CITY_NAME, null),
                mapViewportKey = preferences.getString(CAFE_CACHE_METADATA_MAP_VIEWPORT_KEY, null),
                mapCityName = preferences.getString(CAFE_CACHE_METADATA_MAP_CITY_NAME, null)
            )
        }.getOrDefault(
            CafeCacheMetadata(
                homeCityKey = null,
                homeCityName = null,
                mapViewportKey = null,
                mapCityName = null
            )
        )
    }

    fun saveHome(context: Context, cityKey: String, cityName: String?) {
        prefs(context)
            .edit()
            .putString(CAFE_CACHE_METADATA_HOME_CITY_KEY, cityKey)
            .putOptionalString(CAFE_CACHE_METADATA_HOME_CITY_NAME, cityName)
            .apply()
    }

    fun saveMap(context: Context, viewportKey: String, cityName: String?) {
        prefs(context)
            .edit()
            .putString(CAFE_CACHE_METADATA_MAP_VIEWPORT_KEY, viewportKey)
            .putOptionalString(CAFE_CACHE_METADATA_MAP_CITY_NAME, cityName)
            .apply()
    }

    private fun SharedPreferences.Editor.putOptionalString(
        key: String,
        value: String?
    ): SharedPreferences.Editor {
        val normalizedValue = value?.trim()?.takeIf { it.isNotBlank() }
        return if (normalizedValue == null) remove(key) else putString(key, normalizedValue)
    }
}

private suspend fun loadCachedCafeEnvelope(
    context: Context,
    cityKey: String
): CachedCafeEnvelope? = withContext(Dispatchers.IO) {
    CafeCacheStore.load(context, cityKey)
}

private suspend fun CachedCafeEnvelope.toCafeListOffMain(
    distanceReference: LatLng?,
    imageDecodeMode: CacheImageDecodeMode = CacheImageDecodeMode.All
): List<Cafe> {
    return withContext(Dispatchers.Default) {
        cafes.map { dto ->
            dto.toCafe(
                distanceReference = distanceReference,
                imageDecodeMode = imageDecodeMode
            )
        }
    }
}

private suspend fun saveCafeCacheOffMain(
    context: Context,
    cityKey: String,
    cafes: List<Cafe>,
    metadataTarget: CafeCacheMetadataTarget? = null,
    cityName: String? = null
) {
    withContext(Dispatchers.IO) {
        runCatching {
            CafeCacheStore.save(context, cityKey, cafes)
            when (metadataTarget) {
                CafeCacheMetadataTarget.Home -> CafeCacheMetadataStore.saveHome(context, cityKey, cityName)
                CafeCacheMetadataTarget.Map -> CafeCacheMetadataStore.saveMap(context, cityKey, cityName)
                null -> Unit
            }
            CafeCacheStore.pruneToCurrentMetadata(context, extraCityKeysToKeep = setOf(cityKey))
        }.onFailure { error ->
            error.printStackTrace()
        }
    }
}

private object BookmarkCacheStore {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(BOOKMARK_CACHE_PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun load(context: Context): List<Cafe> {
        return runCatching {
            val encoded = prefs(context).getString(BOOKMARK_CACHE_ENTRY_KEY, null)
                ?: return@runCatching emptyList()
            val envelope = json.decodeFromString<CachedBookmarkEnvelope>(encoded)

            envelope.cafes
                .distinctBy { dto -> dto.id }
                .map { dto -> dto.toCafe(distanceReference = null) }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, cafes: List<Cafe>) {
        if (cafes.isEmpty()) {
            clear(context)
            return
        }

        val envelope = CachedBookmarkEnvelope(
            savedAtEpochMillis = System.currentTimeMillis(),
            cafes = cafes.toCachedDtosForStorage(maxCafes = cafes.size)
        )

        prefs(context)
            .edit()
            .putString(BOOKMARK_CACHE_ENTRY_KEY, json.encodeToString(envelope))
            .apply()
    }

    fun clear(context: Context) {
        prefs(context)
            .edit()
            .remove(BOOKMARK_CACHE_ENTRY_KEY)
            .apply()
    }
}

private object CardStackProgressStore {
    private fun prefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(
            CARD_STACK_PROGRESS_PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }

    fun load(context: Context): String? {
        return prefs(context).getString(CARD_STACK_TOP_CAFE_ID_KEY, null)
    }

    fun save(context: Context, topCafeId: String) {
        prefs(context)
            .edit()
            .putString(CARD_STACK_TOP_CAFE_ID_KEY, topCafeId)
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

private fun buildVisibleCafeStack(
    cafes: List<Cafe>,
    topCafeId: String? = null
): VisibleCafeStack {
    if (cafes.isEmpty()) {
        return VisibleCafeStack(emptyList(), 0)
    }

    val visibleCount = minOf(3, cafes.size)
    val startIndex = topCafeId
        ?.let { cafeId -> cafes.indexOfFirst { cafe -> cafe.id == cafeId } }
        ?.takeIf { it >= 0 }
        ?: 0

    return VisibleCafeStack(
        cafes = List(visibleCount) { index ->
            cafes[(startIndex + index) % cafes.size]
        },
        nextCafeIndex = (startIndex + visibleCount) % cafes.size
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
    private val mapSessionCafeCache = MapSessionCafeCache()
    private val cachePersistenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var appContext: Context? = null
    private var isInitialized = false
    private var homeCachePersistJob: Job? = null
    private var mapCachePersistJob: Job? = null
    @Volatile
    private var homeCachePersistGeneration = 0
    @Volatile
    private var mapCachePersistGeneration = 0

    val homeUiState: CafeFeedUiState
        get() = homeFeedState

    val mapUiState: CafeFeedUiState
        get() = mapFeedState

    fun initialize(context: Context) {
        val applicationContext = context.applicationContext
        appContext = applicationContext
        if (isInitialized) return
        isInitialized = true

        cachePersistenceScope.launch {
            runCatching {
                restoreFreshCachedFeeds(applicationContext)
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    private suspend fun restoreFreshCachedFeeds(context: Context) {
        withContext(Dispatchers.IO) {
            CafeCacheStore.pruneToCurrentMetadata(context)
        }
        val metadata = withContext(Dispatchers.IO) {
            CafeCacheMetadataStore.load(context)
        }
        val homeEnvelope = metadata.homeCityKey
            ?.let { cityKey -> loadCachedCafeEnvelope(context, cityKey) }
            ?.takeIf { envelope -> envelope.canRestoreFreshFeed() }
        val mapEnvelope = metadata.mapViewportKey
            ?.let { viewportKey -> loadCachedCafeEnvelope(context, viewportKey) }
            ?.takeIf { envelope -> envelope.canRestoreFreshFeed() }
        val restoredHomeCafes = homeEnvelope
            ?.toCafeListOffMain(
                distanceReference = null,
                imageDecodeMode = CacheImageDecodeMode.HeroOnly
            )
            .orEmpty()
        val restoredMapCafes = mapEnvelope
            ?.toCafeListOffMain(
                distanceReference = null,
                imageDecodeMode = CacheImageDecodeMode.HeroOnly
            )
            .orEmpty()

        withContext(Dispatchers.Main.immediate) {
            if (homeEnvelope != null && restoredHomeCafes.isNotEmpty() && homeFeedState.cafes.isEmpty()) {
                replaceHomeFeed(
                    cafes = restoredHomeCafes,
                    cityKey = metadata.homeCityKey,
                    cityName = metadata.homeCityName,
                    lastUpdatedEpochMillis = homeEnvelope.savedAtEpochMillis
                )
                homeFeedState = homeFeedState.copy(
                    isLoading = false,
                    isRefreshing = homeEnvelope.needsHomeRefreshAfterRestore(),
                    loadError = null
                )
            }

            if (mapEnvelope != null && restoredMapCafes.isNotEmpty() && mapFeedState.cafes.isEmpty()) {
                metadata.mapViewportKey?.let { viewportKey ->
                    mapSessionCafeCache.recordViewport(
                        viewportKey = viewportKey,
                        cafes = restoredMapCafes,
                        savedAtEpochMillis = mapEnvelope.savedAtEpochMillis
                    )
                }
                replaceMapFeed(
                    cafes = restoredMapCafes,
                    cityKey = metadata.mapViewportKey,
                    cityName = metadata.mapCityName,
                    lastUpdatedEpochMillis = mapEnvelope.savedAtEpochMillis
                )
                mapFeedState = mapFeedState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    loadError = null
                )
            }
        }
    }

    private fun indexCafes(cafes: List<Cafe>): List<Cafe> {
        if (cafes.isEmpty()) return emptyList()

        val updatedCatalog = cafeIndexById.toMutableMap()
        val indexedCafes = cafes.map { cafe ->
            val mergedCafe = cafe.mergeLoadedMedia(updatedCatalog[cafe.id])
            updatedCatalog[cafe.id] = mergedCafe
            mergedCafe
        }
        cafeIndexById = updatedCatalog.trimCafeIndex(retainedCafeIds(cafes.map { cafe -> cafe.id }))
        return indexedCafes
    }

    private fun retainedCafeIds(additionalCafeIds: Iterable<String> = emptyList()): Set<String> {
        return buildSet {
            addAll(additionalCafeIds)
            addAll(homeFeedState.cafes.map { cafe -> cafe.id })
            addAll(mapFeedState.cafes.map { cafe -> cafe.id })
            addAll(BookmarkRepository.ids())
            addAll(RecentRepository.ids())
        }
    }

    private fun Map<String, Cafe>.trimCafeIndex(protectedCafeIds: Set<String>): Map<String, Cafe> {
        if (size <= CAFE_INDEX_MAX_ENTRIES) return this

        val trimmedCatalog = LinkedHashMap(this)
        val removableCafeIds = trimmedCatalog.keys.filterNot { cafeId -> cafeId in protectedCafeIds }
        val removeCount = (trimmedCatalog.size - CAFE_INDEX_MAX_ENTRIES).coerceAtMost(removableCafeIds.size)
        removableCafeIds.take(removeCount).forEach { cafeId -> trimmedCatalog.remove(cafeId) }
        return trimmedCatalog
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

    private suspend fun preloadCrowdAttributes(cafes: List<Cafe>) {
        if (cafes.isEmpty()) return

        runCatching {
            CrowdAttributeRepository.loadManyFromSupabase(cafes.map { cafe -> cafe.id })
        }.onFailure { error ->
            error.printStackTrace()
        }
    }

    private fun scheduleActiveCachePersistForCafe(cafeId: String) {
        val context = appContext ?: return

        if (homeFeedState.shouldPersistActiveCacheForCafe(cafeId)) {
            homeFeedState.cityKey?.let { homeCityKey ->
                scheduleHomeCachePersist(
                    context = context,
                    cityKey = homeCityKey,
                    cityName = homeFeedState.cityName,
                    cafes = homeFeedState.cafes
                )
            }
        }

        if (mapFeedState.shouldPersistActiveCacheForCafe(cafeId)) {
            mapFeedState.cityKey?.let { mapViewportKey ->
                scheduleMapCachePersist(
                    context = context,
                    viewportKey = mapViewportKey,
                    cityName = mapFeedState.cityName,
                    cafes = mapFeedState.cafes
                )
            }
        }
    }

    private fun scheduleHomeCachePersist(
        context: Context,
        cityKey: String,
        cityName: String?,
        cafes: List<Cafe>
    ) {
        val generation = ++homeCachePersistGeneration
        homeCachePersistJob?.cancel()
        homeCachePersistJob = cachePersistenceScope.launch {
            delay(ACTIVE_CAFE_CACHE_WRITE_DEBOUNCE_MILLIS)
            if (generation != homeCachePersistGeneration) return@launch
            saveCafeCacheOffMain(
                context = context,
                cityKey = cityKey,
                cafes = cafes,
                metadataTarget = CafeCacheMetadataTarget.Home,
                cityName = cityName
            )
        }
    }

    private fun scheduleMapCachePersist(
        context: Context,
        viewportKey: String,
        cityName: String?,
        cafes: List<Cafe>
    ) {
        val generation = ++mapCachePersistGeneration
        mapCachePersistJob?.cancel()
        mapCachePersistJob = cachePersistenceScope.launch {
            delay(ACTIVE_CAFE_CACHE_WRITE_DEBOUNCE_MILLIS)
            if (generation != mapCachePersistGeneration) return@launch
            saveCafeCacheOffMain(
                context = context,
                cityKey = viewportKey,
                cafes = cafes,
                metadataTarget = CafeCacheMetadataTarget.Map,
                cityName = cityName
            )
        }
    }

    private fun prefetchCardImagesInBackground(
        placesClient: PlacesClient,
        cafes: List<Cafe>
    ) {
        val cafesNeedingHeroImages = cafes.filter { cafe ->
            cafe.photoMetadatas.isNotEmpty() &&
                cafe.heroImageBitmap == null &&
                cafe.photoBitmaps.firstOrNull() == null
        }
        if (cafesNeedingHeroImages.isEmpty()) return

        cachePersistenceScope.launch {
            cafesNeedingHeroImages.forEach { cafe ->
                runCatching {
                    val metadata = cafe.photoMetadatas.firstOrNull() ?: return@runCatching
                    val bitmap = fetchCafeCardPhotoBitmap(placesClient, metadata) ?: return@runCatching
                    withContext(Dispatchers.Main.immediate) {
                        updateCafePhoto(cafe.id, 0, bitmap)
                    }
                }.onFailure { error ->
                    error.printStackTrace()
                }
            }
        }
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
            val updatedCatalog = cafeIndexById.toMutableMap()
            updatedCatalog[cafeId] = transform(existingCafe)
            cafeIndexById = updatedCatalog.trimCafeIndex(retainedCafeIds(listOf(cafeId)))
        }

        val updatedHomeCafes = updateFeedCafe(homeFeedState.cafes, cafeId, transform)
        if (updatedHomeCafes !== homeFeedState.cafes) {
            homeFeedState = homeFeedState.copy(cafes = updatedHomeCafes)
        }

        val updatedMapCafes = updateFeedCafe(mapFeedState.cafes, cafeId, transform)
        if (updatedMapCafes !== mapFeedState.cafes) {
            mapFeedState = mapFeedState.copy(cafes = updatedMapCafes)
        }

        mapSessionCafeCache.updateCafe(cafeId, transform)
        BookmarkRepository.persistIfBookmarked(cafeId)
        scheduleActiveCachePersistForCafe(cafeId)
    }

    fun cacheCafes(cafes: List<Cafe>) {
        if (cafes.isEmpty()) return

        val updatedCatalog = cafeIndexById.toMutableMap()
        cafes.forEach { cafe ->
            val mergedCafe = cafe.mergeLoadedMedia(updatedCatalog[cafe.id])
            updatedCatalog[cafe.id] = mergedCafe
        }
        cafeIndexById = updatedCatalog.trimCafeIndex(retainedCafeIds(cafes.map { cafe -> cafe.id }))
    }

    fun getCafe(id: String): Cafe? = cafeIndexById[id] ?: fallbackCafes.firstOrNull { it.id == id }

    fun updateCafePhoto(cafeId: String, photoIndex: Int, bitmap: Bitmap) {
        applyCafeUpdate(cafeId) { cafe -> cafe.withLoadedPhoto(photoIndex, bitmap) }
    }

    fun updateCafePhone(cafeId: String, phone: String) {
        if (isPhoneUnavailable(phone)) return

        applyCafeUpdate(cafeId) { cafe ->
            if (cafe.phone == phone) cafe else cafe.copy(phone = phone)
        }
    }

    fun updateCafeDetails(cafeId: String, detailCafe: Cafe) {
        applyCafeUpdate(cafeId) { cafe ->
            mergeCafeDetailData(cafe, detailCafe)
        }
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

            if (homeFeedState.hasFreshLoadedCafes()) {
                return
            }

            val queryContext = resolveCurrentCityCoffeeContext(context, fusedLocationClient)
            val distanceReference = queryContext.location.toLatLng()

            if (homeFeedState.hasReusableLoadedCafesForCity(queryContext.cityKey)) {
                return
            }

            val cachedEnvelope = loadCachedCafeEnvelope(context, queryContext.cityKey)
            val shouldRefreshCachedFeed = cachedEnvelope?.needsHomeRefreshAfterRestore() ?: true
            var cachedCafes = if (cachedEnvelope != null) {
                cachedEnvelope.toCafeListOffMain(
                    distanceReference = distanceReference,
                    imageDecodeMode = CacheImageDecodeMode.HeroOnly
                )
            } else {
                emptyList()
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
                    isRefreshing = shouldRefreshCachedFeed,
                    loadError = null
                )
                preloadCrowdAttributes(cachedCafes)
                if (!shouldRefreshCachedFeed) return
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
                    cityName = queryContext.cityName
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
                saveCafeCacheOffMain(
                    context = context,
                    cityKey = queryContext.cityKey,
                    cafes = freshCafes,
                    metadataTarget = CafeCacheMetadataTarget.Home,
                    cityName = queryContext.cityName
                )
                prefetchCardImagesInBackground(placesClient, freshCafes)
                preloadCrowdAttributes(freshCafes)
            } catch (error: Exception) {
                val errorMessage = error.localizedMessage ?: "Failed to fetch coffee houses in your city."
                if (cachedCafes.isEmpty() && cachedEnvelope != null) {
                    cachedCafes = cachedEnvelope.toCafeListOffMain(
                        distanceReference = distanceReference,
                        imageDecodeMode = CacheImageDecodeMode.HeroOnly
                    )
                }

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
            val sessionCachedCafes = mapSessionCafeCache.snapshot(distanceReference)
            val isViewportFresh = mapSessionCafeCache.isViewportFresh(queryContext.cityKey)
            val sessionLastUpdated = mapSessionCafeCache.latestLoadedAtEpochMillis()

            if (sessionCachedCafes.isNotEmpty()) {
                replaceMapFeed(
                    cafes = sessionCachedCafes,
                    cityKey = queryContext.cityKey,
                    cityName = queryContext.cityName,
                    lastUpdatedEpochMillis = sessionLastUpdated
                )
                mapFeedState = mapFeedState.copy(
                    isLoading = false,
                    isRefreshing = !isViewportFresh,
                    loadError = null,
                    cityKey = queryContext.cityKey,
                    cityName = queryContext.cityName,
                    lastUpdatedEpochMillis = sessionLastUpdated
                )
                preloadCrowdAttributes(sessionCachedCafes)

                if (isViewportFresh) return
            }

            val cachedEnvelope = loadCachedCafeEnvelope(context, queryContext.cityKey)
            val diskCachedCafes = cachedEnvelope
                ?.toCafeListOffMain(
                    distanceReference = distanceReference,
                    imageDecodeMode = CacheImageDecodeMode.HeroOnly
                )
                .orEmpty()
            val isDiskCacheFresh = cachedEnvelope?.isFresh() == true

            if (diskCachedCafes.isNotEmpty()) {
                if (isDiskCacheFresh) {
                    mapSessionCafeCache.recordViewport(
                        viewportKey = queryContext.cityKey,
                        cafes = diskCachedCafes,
                        savedAtEpochMillis = cachedEnvelope?.savedAtEpochMillis ?: System.currentTimeMillis()
                    )
                }
                replaceMapFeed(
                    cafes = diskCachedCafes,
                    cityKey = queryContext.cityKey,
                    cityName = queryContext.cityName,
                    lastUpdatedEpochMillis = cachedEnvelope?.savedAtEpochMillis
                )
                mapFeedState = mapFeedState.copy(
                    isLoading = false,
                    isRefreshing = !isDiskCacheFresh,
                    loadError = null,
                    cityKey = queryContext.cityKey,
                    cityName = queryContext.cityName,
                    lastUpdatedEpochMillis = cachedEnvelope?.savedAtEpochMillis
                )
                preloadCrowdAttributes(diskCachedCafes)

                if (isDiskCacheFresh) return
            } else {
                mapFeedState = CafeFeedUiState(
                    cafes = emptyList(),
                    isLoading = true,
                    isRefreshing = false,
                    loadError = null,
                    cityKey = queryContext.cityKey,
                    cityName = queryContext.cityName
                )
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

                mapSessionCafeCache.recordViewport(queryContext.cityKey, freshCafes)
                saveCafeCacheOffMain(
                    context = context,
                    cityKey = queryContext.cityKey,
                    cafes = freshCafes,
                    metadataTarget = CafeCacheMetadataTarget.Map,
                    cityName = queryContext.cityName
                )
                val updatedSessionCafes = mapSessionCafeCache.snapshot(distanceReference)
                val updatedLastUpdated = mapSessionCafeCache.latestLoadedAtEpochMillis()
                    ?: System.currentTimeMillis()

                replaceMapFeed(
                    cafes = updatedSessionCafes,
                    cityKey = queryContext.cityKey,
                    cityName = queryContext.cityName,
                    lastUpdatedEpochMillis = updatedLastUpdated
                )
                mapFeedState = mapFeedState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    loadError = null
                )
                preloadCrowdAttributes(updatedSessionCafes)
            } catch (error: Exception) {
                val errorMessage = error.localizedMessage ?: "Failed to load coffee houses for this map area."
                val fallbackSessionCafes = mapSessionCafeCache.snapshot(distanceReference)
                val fallbackCafes = fallbackSessionCafes.ifEmpty { diskCachedCafes }
                val fallbackLastUpdated = mapSessionCafeCache.latestLoadedAtEpochMillis()
                    ?: cachedEnvelope?.savedAtEpochMillis
                if (fallbackCafes.isNotEmpty()) {
                    replaceMapFeed(
                        cafes = fallbackCafes,
                        cityKey = queryContext.cityKey,
                        cityName = queryContext.cityName,
                        lastUpdatedEpochMillis = fallbackLastUpdated
                    )
                    mapFeedState = mapFeedState.copy(
                        isLoading = false,
                        isRefreshing = false,
                        loadError = errorMessage,
                        cityKey = queryContext.cityKey,
                        cityName = queryContext.cityName
                    )
                } else {
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

    internal fun resetForTest() {
        homeCachePersistJob?.cancel()
        mapCachePersistJob?.cancel()
        homeCachePersistJob = null
        mapCachePersistJob = null
        homeCachePersistGeneration++
        mapCachePersistGeneration++
        homeFeedState = CafeFeedUiState()
        mapFeedState = CafeFeedUiState()
        cafeIndexById = emptyMap()
        mapSessionCafeCache.clear()
        appContext = null
        isInitialized = false
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

    internal fun cacheMapViewportForTest(viewportKey: String, cafes: List<Cafe>) {
        mapSessionCafeCache.recordViewport(viewportKey, cafes)
    }

    internal fun mapSessionCacheSnapshotForTest(distanceReference: LatLng? = null): List<Cafe> {
        return mapSessionCafeCache.snapshot(distanceReference)
    }
}

object BookmarkRepository {
    // store just IDs
    private val bookmarkedIds = mutableStateListOf<String>()
    private val completedSavedCacheIds = mutableSetOf<String>()
    private val savedCacheAttemptedAtMillisById = mutableMapOf<String, Long>()
    private val persistenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var appContext: Context? = null
    private var isInitialized = false
    private var persistJob: Job? = null
    @Volatile
    private var persistGeneration = 0

    fun initialize(context: Context) {
        val applicationContext = context.applicationContext
        appContext = applicationContext
        if (isInitialized) return

        val cachedBookmarks = BookmarkCacheStore.load(applicationContext)
        if (cachedBookmarks.isNotEmpty()) {
            CafeRepository.cacheCafes(cachedBookmarks)
            bookmarkedIds.clear()
            bookmarkedIds.addAll(cachedBookmarks.map { cafe -> cafe.id })
            completedSavedCacheIds.clear()
            completedSavedCacheIds.addAll(
                cachedBookmarks
                    .filterNot { cafe -> isBookmarkCacheDataIncomplete(cafe) }
                    .map { cafe -> cafe.id }
            )
        }

        isInitialized = true
    }

    fun isBookmarked(cafeId: String): Boolean = bookmarkedIds.contains(cafeId)

    fun add(cafeId: String) {
        val wasBookmarked = bookmarkedIds.remove(cafeId)
        bookmarkedIds.add(0, cafeId)
        if (!wasBookmarked) {
            clearSavedCafeCacheState(cafeId)
        }
        persistBookmarks()
    }

    fun toggle(cafeId: String) {
        if (bookmarkedIds.contains(cafeId)) {
            bookmarkedIds.remove(cafeId)
            clearSavedCafeCacheState(cafeId)
            persistBookmarks()
        }
        else add(cafeId)
    }

    fun remove(cafeId: String) {
        bookmarkedIds.remove(cafeId)
        clearSavedCafeCacheState(cafeId)
        persistBookmarks()
    }

    // Expose as List so callers can display it
    fun ids(): List<String> = bookmarkedIds.toList()

    fun cafes(): List<Cafe> = bookmarkedIds.toList()
        .mapNotNull { id -> CafeRepository.getCafe(id) }

    fun persistIfBookmarked(cafeId: String) {
        if (bookmarkedIds.contains(cafeId)) {
            persistBookmarks()
        }
    }

    internal fun shouldEnrichSavedCafeCache(cafe: Cafe, nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (!bookmarkedIds.contains(cafe.id)) return false
        if (!isBookmarkCacheDataIncomplete(cafe)) return false
        completedSavedCacheIds.remove(cafe.id)

        val attemptedAt = savedCacheAttemptedAtMillisById[cafe.id] ?: return true
        return nowMillis - attemptedAt >= SAVED_CAFE_CACHE_ATTEMPT_TTL_MILLIS
    }

    internal fun markSavedCafeCacheEnrichmentStarted(
        cafeId: String,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (!bookmarkedIds.contains(cafeId)) return false

        val attemptedAt = savedCacheAttemptedAtMillisById[cafeId]
        if (attemptedAt != null && nowMillis - attemptedAt < SAVED_CAFE_CACHE_ATTEMPT_TTL_MILLIS) {
            return false
        }

        completedSavedCacheIds.remove(cafeId)
        savedCacheAttemptedAtMillisById[cafeId] = nowMillis
        return true
    }

    internal fun finishSavedCafeCacheEnrichment(cafe: Cafe, nowMillis: Long = System.currentTimeMillis()) {
        if (!bookmarkedIds.contains(cafe.id)) return

        if (isBookmarkCacheDataIncomplete(cafe)) {
            completedSavedCacheIds.remove(cafe.id)
            savedCacheAttemptedAtMillisById[cafe.id] = nowMillis
        } else {
            savedCacheAttemptedAtMillisById.remove(cafe.id)
            completedSavedCacheIds.add(cafe.id)
        }
    }

    internal fun markSavedCafeCacheEnrichmentFailed(
        cafeId: String,
        nowMillis: Long = System.currentTimeMillis()
    ) {
        if (!bookmarkedIds.contains(cafeId)) return
        completedSavedCacheIds.remove(cafeId)
        savedCacheAttemptedAtMillisById[cafeId] = nowMillis
    }

    private fun clearSavedCafeCacheState(cafeId: String) {
        completedSavedCacheIds.remove(cafeId)
        savedCacheAttemptedAtMillisById.remove(cafeId)
    }

    private fun persistBookmarks() {
        val context = appContext ?: return
        val cafesSnapshot = cafes()
        val generation = ++persistGeneration

        persistJob?.cancel()
        persistJob = persistenceScope.launch {
            try {
                delay(BOOKMARK_CACHE_WRITE_DEBOUNCE_MILLIS)
                if (generation != persistGeneration) return@launch
                BookmarkCacheStore.save(context, cafesSnapshot)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                error.printStackTrace()
            }
        }
    }

    internal fun savedCafeCacheSnapshotForTest(): List<Cafe> = cafes()

    internal fun isSavedCafeCacheEnrichmentCompleteForTest(cafeId: String): Boolean {
        return completedSavedCacheIds.contains(cafeId)
    }

    internal fun savedCafeCacheAttemptedAtForTest(cafeId: String): Long? {
        return savedCacheAttemptedAtMillisById[cafeId]
    }

    internal fun resetForTest() {
        persistJob?.cancel()
        persistJob = null
        persistGeneration++
        bookmarkedIds.clear()
        completedSavedCacheIds.clear()
        savedCacheAttemptedAtMillisById.clear()
        appContext = null
        isInitialized = false
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

    fun ids(): List<String> = recentIds.toList()

    fun cafes(): List<Cafe> {
        return recentIds.mapNotNull { id -> CafeRepository.getCafe(id) }
    }

    fun previewCafes(limit: Int = 3): List<Cafe> {
        return cafes().take(limit)
    }
}

@Serializable
data class Review(
    val id: String = "",
    val cafe_id: String,
    val user_id: String = "",
    val display_name: String? = null,
    val body: String,
    val created_at: String = ""
)

object ReviewRepository {
    private val reviewsByCafe = mutableStateMapOf<String, SnapshotStateList<Review>>()

    private val myReviewsList = mutableStateListOf<Review>()

    fun myReviews(): List<Review> = myReviewsList.toList()

    suspend fun loadMyReviews() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val fetched = supabase.from("reviews")
            .select { filter { eq("user_id", user.id) } }
            .decodeList<Review>()
        myReviewsList.clear()
        myReviewsList.addAll(fetched)
    }

    fun reviewsFor(cafeId: String): SnapshotStateList<Review> =
        reviewsByCafe.getOrPut(cafeId) { mutableStateListOf() }

    suspend fun loadReviews(cafeId: String) {
        val fetched = supabase.from("reviews")
            .select { filter { eq("cafe_id", cafeId) } }
            .decodeList<Review>()
        val list = reviewsByCafe.getOrPut(cafeId) { mutableStateListOf() }
        list.clear()
        list.addAll(fetched)
    }

    suspend fun addReview(cafeId: String, body: String, displayName: String?) {
        val user = supabase.auth.currentUserOrNull() ?: return
        val review = Review(
            cafe_id = cafeId,
            user_id = user.id,
            display_name = displayName,
            body = body
        )
        supabase.from("reviews").insert(review)
        loadReviews(cafeId) // refresh after posting
    }

    suspend fun deleteReview(review: Review) {
        supabase.from("reviews").delete { filter { eq("id", review.id) } }
        reviewsByCafe[review.cafe_id]?.remove(review)
    }
}

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, "AIzaSyC7QTmdJE2fnRXMiKWrMZftkXIG20gNWrA")
        }
        CafeRepository.initialize(applicationContext)
        BookmarkRepository.initialize(applicationContext)
        StudySessionRepository.initialize(applicationContext)
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
    val bottomNavMotionState = remember { BottomNavMotionState() }
    val bottomNavUiState = remember { BottomNavUiState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: startDestination
    val showBottomNav = currentRoute in bottomNavRoutes
    val bottomNavIsBehindOverlay = bottomNavUiState.dimFraction > 0.01f || bottomNavUiState.overlayCoversToolbar

    LaunchedEffect(currentRoute) {
        bottomNavUiState.enabled = true
        bottomNavUiState.dimFraction = 0f
        bottomNavUiState.overlayCoversToolbar = false
    }

    CompositionLocalProvider(
        LocalBottomNavMotionState provides bottomNavMotionState,
        LocalBottomNavUiState provides bottomNavUiState,
        LocalUsesPersistentBottomNav provides true
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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

            if (showBottomNav) {
                FloatingBottomNavBar(
                    navController = navController,
                    enabled = bottomNavUiState.enabled,
                    dimFraction = bottomNavUiState.dimFraction,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .zIndex(if (bottomNavIsBehindOverlay) -1f else 1f)
                )
            }
        }
    }
}

@Composable
private fun ShuffleCafeLoadingAnimation(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shuffleCafeLoader")
    val pulseScale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 820, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shuffleCafeLoaderPulse"
    )
    val ringAlpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 820, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shuffleCafeLoaderRing"
    )

    Box(
        modifier = modifier.size(96.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(82.dp)
                .graphicsLayer { alpha = ringAlpha },
            color = CafeDark,
            strokeWidth = 3.dp
        )
        Surface(
            modifier = Modifier
                .size(58.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                },
            shape = CircleShape,
            color = CoffeeLight.copy(alpha = 0.92f),
            shadowElevation = 6.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.shuffle_cafe_logo),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
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
    val showLoadError = loadError != null && !isNoCoffeeHousesMessage(loadError)
    var currentVisibleCafes by remember { mutableStateOf<List<Cafe>>(emptyList()) }
    var nextCafeIndex by rememberSaveable { mutableIntStateOf(0) }
    var expandedCafeId by remember { mutableStateOf<String?>(null) }
    var overlayCafe by remember { mutableStateOf<Cafe?>(null) }
    var transitionState by remember { mutableStateOf<SelectedCafeTransitionState?>(null) }
    var overlayHostBounds by remember { mutableStateOf<Rect?>(null) }
    var isPreparingDetailTransition by remember { mutableStateOf(false) }
    var selectedFilterOption by remember { mutableStateOf(CardsFilterOption.Nearby) }
    val detailProgress = remember { Animatable(0f) }
    val detailOverlayLayoutSpec = remember { DetailOverlayLayoutSpec() }

    val expandedCafe = remember(allCafes, expandedCafeId) {
        expandedCafeId?.let { cafeId ->
            allCafes.firstOrNull { it.id == cafeId } ?: CafeRepository.getCafe(cafeId)
        }
    }
    val isDetailExpanded = expandedCafe != null
    CafeDetailDataEffect(cafe = expandedCafe, placesClient = placesClient)

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

    val setVisibleStack: (List<Cafe>, String?) -> Unit = { cafes, topCafeId ->
        val visibleStack = buildVisibleCafeStack(cafes, topCafeId)
        currentVisibleCafes = visibleStack.cafes
        nextCafeIndex = visibleStack.nextCafeIndex
    }

    LaunchedEffect(currentVisibleCafes.firstOrNull()?.id) {
        currentVisibleCafes.firstOrNull()?.id?.let { topCafeId ->
            CardStackProgressStore.save(context, topCafeId)
        }
    }

    LaunchedEffect(allCafes) {
        val restoredTopCafeId = currentVisibleCafes.firstOrNull()?.id ?: CardStackProgressStore.load(context)

        if (allCafes.isEmpty()) {
            setVisibleStack(emptyList(), null)
            return@LaunchedEffect
        }

        if (currentVisibleCafes.isEmpty() || nextCafeIndex >= allCafes.size) {
            setVisibleStack(allCafes, restoredTopCafeId)
            return@LaunchedEffect
        }

        val updatedVisibleCafes = currentVisibleCafes.mapNotNull { visibleCafe ->
            allCafes.firstOrNull { it.id == visibleCafe.id }
        }
        val expectedVisibleCount = minOf(3, allCafes.size)

        if (updatedVisibleCafes.isEmpty() || updatedVisibleCafes.size != currentVisibleCafes.size) {
            setVisibleStack(allCafes, restoredTopCafeId)
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
                CardsExploreFilterBar(
                    selectedOption = selectedFilterOption,
                    onOptionSelected = { selectedFilterOption = it },
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
                            ShuffleCafeLoadingAnimation()
                        }
                        showLoadError -> {
                            Text(loadError ?: "Unable to load cafes.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                        }
                        allCafes.isEmpty() -> {
                            ShuffleCafeLoadingAnimation()
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
    cityName: String?
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
        Place.Field.DISPLAY_NAME,
        Place.Field.FORMATTED_ADDRESS,
        Place.Field.RATING,
        Place.Field.USER_RATING_COUNT,
        Place.Field.PHOTO_METADATAS,
        Place.Field.LOCATION,
        Place.Field.PRIMARY_TYPE,
        Place.Field.TYPES,
        Place.Field.OPENING_HOURS,
        Place.Field.CURRENT_OPENING_HOURS
    ) + cafePhonePlaceFields
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
        .setMaxWidth(CAFE_CARD_PHOTO_MAX_WIDTH)
        .setMaxHeight(CAFE_CARD_PHOTO_MAX_HEIGHT)
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
            .setMaxWidth(CAFE_DETAIL_PHOTO_MAX_WIDTH)
            .setMaxHeight(CAFE_DETAIL_PHOTO_MAX_HEIGHT)
            .build()
        placesClient.fetchPhoto(request).await().bitmap
    }.getOrNull()
}

private suspend fun fetchCafeCardPhotoBitmap(
    placesClient: PlacesClient,
    photoMetadata: PhotoMetadata
): Bitmap? {
    return runCatching {
        val request = FetchPhotoRequest.builder(photoMetadata)
            .setMaxWidth(CAFE_CARD_PHOTO_MAX_WIDTH)
            .setMaxHeight(CAFE_CARD_PHOTO_MAX_HEIGHT)
            .build()
        placesClient.fetchPhoto(request).await().bitmap
    }.getOrNull()
}

private suspend fun fetchPlaceSearchThumbnailBitmap(
    placesClient: PlacesClient,
    placeId: String
): Bitmap? {
    return runCatching {
        val placeRequest = com.google.android.libraries.places.api.net.FetchPlaceRequest
            .builder(
                placeId,
                listOf(Place.Field.PHOTO_METADATAS)
            )
            .build()
        val metadata = placesClient.fetchPlace(placeRequest)
            .await()
            .place
            .photoMetadatas
            ?.firstOrNull()
            ?: return@runCatching null
        val photoRequest = FetchPhotoRequest.builder(metadata)
            .setMaxWidth(180)
            .setMaxHeight(180)
            .build()
        placesClient.fetchPhoto(photoRequest).await().bitmap
    }.getOrNull()
}

private suspend fun loadCafeCardImages(
    placesClient: PlacesClient,
    cafes: List<Cafe>
): List<Cafe> = coroutineScope {
    cafes.map { cafe ->
        async {
            val metadata = cafe.photoMetadatas.firstOrNull() ?: return@async cafe
            if (cafe.heroImageBitmap != null || cafe.photoBitmaps.firstOrNull() != null) return@async cafe
            val bitmap = fetchCafePhotoBitmap(placesClient, metadata) ?: return@async cafe
            cafe.withLoadedPhoto(index = 0, bitmap = bitmap)
        }
    }.awaitAll()
}

private suspend fun loadCompleteCafeImages(
    placesClient: PlacesClient,
    cafe: Cafe
): Cafe {
    if (cafe.photoMetadatas.isEmpty()) return cafe

    var updatedCafe = cafe
    cafe.photoMetadatas.forEachIndexed { index, metadata ->
        if (updatedCafe.photoBitmaps.getOrNull(index) != null) return@forEachIndexed
        val bitmap = fetchCafePhotoBitmap(placesClient, metadata) ?: return@forEachIndexed
        updatedCafe = updatedCafe.withLoadedPhoto(index = index, bitmap = bitmap)
    }
    return updatedCafe
}

private suspend fun loadCafeCacheHeroImage(
    placesClient: PlacesClient,
    cafe: Cafe
): Cafe {
    if (cafe.heroImageBitmap != null || cafe.photoBitmaps.firstOrNull() != null) return cafe
    val metadata = cafe.photoMetadatas.firstOrNull() ?: return cafe
    val bitmap = fetchCafeCardPhotoBitmap(placesClient, metadata) ?: return cafe
    return cafe.withLoadedPhoto(index = 0, bitmap = bitmap)
}

private suspend fun fetchCafeDetails(
    placesClient: PlacesClient,
    placeId: String
): Cafe? {
    if (placeId.isBlank()) return null

    val request = FetchPlaceRequest
        .builder(placeId, cafeDetailPlaceFields)
        .build()
    val place = placesClient.fetchPlace(request).await().place

    return place.toCafe(distanceReference = null)
}

private fun Place.isLikelyCoffeeHouse(): Boolean {
    val normalizedName = displayName?.lowercase(Locale.US).orEmpty()
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
        "${distanceMeters.toInt()} m"
    } else {
        val milesTenths = ((distanceMeters / 1609.344f) * 10).roundToInt()
        val milesText = if (milesTenths % 10 == 0) {
            (milesTenths / 10).toString()
        } else {
            String.format(Locale.US, "%.1f", milesTenths / 10f)
        }
        "$milesText mi"
    }
}

internal fun selectCafePhoneNumber(
    internationalPhoneNumber: String?,
    nationalPhoneNumber: String?
): String {
    return internationalPhoneNumber?.trim()?.takeIf { it.isNotBlank() }
        ?: nationalPhoneNumber?.trim()?.takeIf { it.isNotBlank() }
        ?: PHONE_UNAVAILABLE_TEXT
}

internal fun isPhoneUnavailable(phone: String): Boolean {
    return phone.isBlank() || phone.equals(PHONE_UNAVAILABLE_TEXT, ignoreCase = true)
}

private fun Cafe.directionsDestination(): String? {
    latLng?.let { coordinates ->
        return "${coordinates.latitude},${coordinates.longitude}"
    }

    return address
        .takeUnless(::isCafeAddressUnavailable)
        ?: name.takeIf { it.isNotBlank() }
}

private fun isNoCoffeeHousesMessage(message: String?): Boolean {
    return message?.startsWith("No coffee houses found", ignoreCase = true) == true
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
        linkedMapOf("Hours" to HOURS_UNAVAILABLE_TEXT)
    }
}

private fun Place.toCafe(distanceReference: LatLng?): Cafe? {
    val placeId = id ?: return null
    val cafeName = displayName?.trim().takeIf { !it.isNullOrBlank() } ?: return null
    val cafeAddress = formattedAddress?.trim().takeIf { !it.isNullOrBlank() } ?: ADDRESS_UNAVAILABLE_TEXT
    val cafePhone = selectCafePhoneNumber(
        internationalPhoneNumber = internationalPhoneNumber,
        nationalPhoneNumber = nationalPhoneNumber
    )
    val ratingValue = rating?.toFloat()
    val ratingCount = userRatingCount
    val cafeLatLng = location
    val distanceToCafe = computeCafeDistanceMeters(cafeLatLng, distanceReference)
    val statusText = if (ratingValue != null && ratingCount != null && ratingCount > 0) {
        String.format(Locale.US, "%.1f (%d reviews)", ratingValue, ratingCount)
    } else {
        NO_RATINGS_TEXT
    }

    val cafeHours = toHoursMap()

    return Cafe(
        id = placeId,
        name = cafeName,
        address = cafeAddress,
        phone = cafePhone,
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
    var mapCafeFilters by remember { mutableStateOf(MapCafeFilters()) }
    var isMapFilterSheetVisible by remember { mutableStateOf(false) }
    val cafeFeedState = CafeRepository.mapUiState
    val allCafes = cafeFeedState.cafes
    val isLoading = cafeFeedState.isLoading && allCafes.isEmpty()
    val loadError = cafeFeedState.loadError.takeIf { allCafes.isEmpty() }
    val showLoadError = loadError != null && !isNoCoffeeHousesMessage(loadError)
    var selectedCafeId by remember { mutableStateOf<String?>(null) }
    var overlayCafe by remember { mutableStateOf<Cafe?>(null) }
    val detailProgress = remember { Animatable(0f) }
    val detailOverlayLayoutSpec = remember { DetailOverlayLayoutSpec() }
    val interactionSource = remember { MutableInteractionSource() }
    val inspectionMode = LocalInspectionMode.current
    var isMapLoaded by remember { mutableStateOf(false) }
    var hasRequestedInitialMapViewport by remember { mutableStateOf(false) }
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
    val filteredMapCafes by remember(renderedMapCafes, mapCafeFilters) {
        derivedStateOf {
            renderedMapCafes.filter { cafe ->
                cafeMatchesMapFilters(
                    cafe = cafe,
                    attributes = CrowdAttributeRepository.attributesFor(cafe.id),
                    filters = mapCafeFilters
                )
            }
        }
    }
    val selectedCafe = remember(allCafes, renderedMapCafes, selectedCafeId) {
        selectedCafeId?.let { cafeId ->
            allCafes.firstOrNull { it.id == cafeId }
                ?: renderedMapCafes.firstOrNull { it.id == cafeId }
                ?: CafeRepository.getCafe(cafeId)
        }
    }
    val cafesWithCoordinates = remember(filteredMapCafes) { filteredMapCafes.filter { it.latLng != null } }
    val hasFilteredOutAllMapCafes = mapCafeFilters.hasActiveFilters &&
        renderedMapCafes.isNotEmpty() &&
        filteredMapCafes.isEmpty() &&
        !isViewportLoadInFlight
    CafeDetailDataEffect(cafe = selectedCafe, placesClient = placesClient)

    LaunchedEffect(selectedCafe) {
        if (selectedCafe != null) {
            overlayCafe = selectedCafe
        }
    }

    LaunchedEffect(selectedCafeId, filteredMapCafes, mapCafeFilters) {
        val selectedId = selectedCafeId ?: return@LaunchedEffect
        if (mapCafeFilters.hasActiveFilters && filteredMapCafes.none { cafe -> cafe.id == selectedId }) {
            selectedCafeId = null
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
                            14f
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

        if (!isMapLoaded) {
            if (!hasRequestedInitialMapViewport) {
                val lastKnownLatLng = runCatching {
                    fusedLocationClient.lastLocation.await()?.toLatLng()
                }.getOrNull()
                if (lastKnownLatLng != null) {
                    hasRequestedInitialMapViewport = true
                    CafeRepository.ensureMapLoadedForViewport(
                        context = context,
                        fusedLocationClient = fusedLocationClient,
                        placesClient = placesClient,
                        hasLocationPermission = hasLocationPermission,
                        center = lastKnownLatLng,
                        searchRadiusMeters = MAP_VIEWPORT_SEARCH_MAX_RADIUS_METERS
                    )
                }
            }
            return@LaunchedEffect
        }

        snapshotFlow {
            if (defaultCamera.isMoving) null else buildMapViewportLoadRequest(defaultCamera)
        }
            .debounce(MAP_VIEWPORT_QUERY_DEBOUNCE_MILLIS)
            .filterNotNull()
            .collectLatest { request ->
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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                MapSearchBar(
                    searchQuery = searchQuery,
                    onQueryChanged = { searchQuery = it },
                    activeFilterCount = mapCafeFilters.activeCount,
                    onFilterClick = { isMapFilterSheetVisible = true },
                    onPlaceSelected = { latLng, _ ->
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
            },
            containerColor = CoffeeLight
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(CoffeeLight)
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
                        ShuffleCafeLoadingAnimation(modifier = Modifier.align(Alignment.Center))
                    }

                    showLoadError -> {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 24.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = CoffeeSurfaceLight.copy(alpha = 0.98f),
                            tonalElevation = 8.dp
                        ) {
                            Text(
                                text = loadError ?: "Unable to load cafes.",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge,
                                color = CafeDark
                            )
                        }
                    }

                    allCafes.isEmpty() -> {
                        ShuffleCafeLoadingAnimation(modifier = Modifier.align(Alignment.Center))
                    }
                }

                if (hasFilteredOutAllMapCafes) {
                    MapFilterEmptyMessage(
                        onClearFilters = { mapCafeFilters = MapCafeFilters() },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }
            }
        }

        if (isMapFilterSheetVisible) {
            MapFilterBottomSheet(
                filters = mapCafeFilters,
                onApplyFilters = { updatedFilters ->
                    mapCafeFilters = updatedFilters
                    isMapFilterSheetVisible = false
                },
                onClearFilters = {
                    mapCafeFilters = MapCafeFilters()
                    isMapFilterSheetVisible = false
                },
                onDismiss = { isMapFilterSheetVisible = false }
            )
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
    val scope = rememberCoroutineScope()
    val savedCafes = BookmarkRepository.cafes()
    var showAllSaved by rememberSaveable { mutableStateOf(false) }
    var selectedSavedCafeId by remember { mutableStateOf<String?>(null) }
    var selectedStudySession by remember { mutableStateOf<StudySession?>(null) }
    var studySessionCafe by remember { mutableStateOf<Cafe?>(null) }
    var loadingStudySessionCafeId by remember { mutableStateOf<String?>(null) }
    var overlayCafe by remember { mutableStateOf<Cafe?>(null) }
    val detailProgress = remember { Animatable(0f) }
    ReportBottomNavOverlayCoverage(isActive = selectedStudySession != null)
    val detailOverlayLayoutSpec = remember { DetailOverlayLayoutSpec() }
    val interactionSource = remember { MutableInteractionSource() }
    val selectedSavedCafe = remember(savedCafes, selectedSavedCafeId, studySessionCafe) {
        selectedSavedCafeId?.let { cafeId ->
            savedCafes.firstOrNull { it.id == cafeId }
                ?: studySessionCafe?.takeIf { cafe -> cafe.id == cafeId }
                ?: CafeRepository.getCafe(cafeId)
        }
    }
    SavedCafeCacheEffect(savedCafes = savedCafes, placesClient = placesClient)
    CafeDetailDataEffect(
        cafe = selectedSavedCafe?.takeUnless { cafe -> cafe.id == loadingStudySessionCafeId },
        placesClient = placesClient
    )

    fun openStudySessionCafeDetails(session: StudySession) {
        val cafeId = session.cafeId
        val existingCafe = savedCafes.firstOrNull { cafe -> cafe.id == cafeId }
            ?: CafeRepository.getCafe(cafeId)
            ?: studySessionCafe?.takeIf { cafe -> cafe.id == cafeId }
        val startingCafe = existingCafe ?: session.toCafePlaceholder()
        studySessionCafe = startingCafe
        CafeRepository.cacheCafes(listOf(startingCafe))
        selectedSavedCafeId = cafeId

        val client = placesClient ?: return
        if (loadingStudySessionCafeId == cafeId || !isCafeDetailDataIncomplete(startingCafe)) return

        loadingStudySessionCafeId = cafeId
        scope.launch {
            val detailCafe = runCatching {
                fetchCafeDetails(client, cafeId)
            }.getOrNull()

            if (detailCafe != null) {
                val mergedCafe = mergeCafeDetailData(
                    existingCafe = CafeRepository.getCafe(cafeId) ?: startingCafe,
                    detailCafe = detailCafe
                )
                CafeRepository.cacheCafes(listOf(mergedCafe))
                if (selectedSavedCafeId == cafeId) {
                    studySessionCafe = mergedCafe
                    overlayCafe = mergedCafe
                }
            }

            if (loadingStudySessionCafeId == cafeId) {
                loadingStudySessionCafeId = null
            }
        }
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
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
                        },
                        onStudySessionSelected = { session ->
                            selectedStudySession = session
                        }
                    )
                }
            }
        }

        selectedStudySession?.let { session ->
            StudySessionDetailOverlay(
                session = session,
                onDismiss = { selectedStudySession = null },
                onDelete = {
                    StudySessionRepository.remove(session.id)
                    selectedStudySession = null
                },
                onOpenCafeDetails = {
                    openStudySessionCafeDetails(session)
                },
                isOpeningCafeDetails = loadingStudySessionCafeId == session.cafeId,
                backHandlerEnabled = selectedSavedCafeId == null,
                modifier = Modifier.matchParentSize()
            )
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
                    .zIndex(20f)
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
                    .zIndex(21f)
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
    val cardColor = CoffeeSurfaceLight
    val primaryTextColor = CafeDark
    val secondaryTextColor = CoffeeDark.copy(alpha = 0.78f)
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = cardColor,
            contentColor = primaryTextColor
        ),
        border = BorderStroke(1.dp, CoffeeDark.copy(alpha = 0.14f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Small thumbnail
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(10.dp),
                color = CoffeeLight.copy(alpha = 0.9f)
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
                Text(
                    text = cafe.name,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryTextColor
                )
                val reviewCount = cafe.userRatingCount ?: 0
                if (cafe.rating != null && reviewCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RatingStars(cafe.rating)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f", cafe.rating),
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryTextColor
                        )
                    }
                } else {
                    Text(
                        text = "No ratings yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryTextColor
                    )
                }
                Text(
                    text = formatDistanceAway(cafe.distanceMeters) ?: "Distance unavailable",
                    style = MaterialTheme.typography.bodySmall,
                    color = primaryTextColor
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Bookmark,
                    contentDescription = "Remove bookmark",
                    tint = CoffeeDark
                )
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

@Composable
private fun CardsExploreFilterBar(
    selectedOption: CardsFilterOption,
    onOptionSelected: (CardsFilterOption) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val playfairDisplay = remember { FontFamily(Font(R.font.playfair_display)) }
    var expanded by remember { mutableStateOf(false) }
    val menuShape = RoundedCornerShape(22.dp)
    val toolbarBubbleColor = BottomNavToolbarColor
    val selectedLabelColor = if (selectedOption == CardsFilterOption.Nearby) {
        Color(0xFF4B3621)
    } else {
        Color.White.copy(alpha = 0.96f)
    }

    LaunchedEffect(enabled) {
        if (!enabled) expanded = false
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = toolbarBubbleColor
            ) {
                Row(
                    modifier = Modifier
                        .clickable(enabled = enabled) { expanded = true }
                        .padding(start = 26.dp, end = 18.dp, top = 14.dp, bottom = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedOption == CardsFilterOption.Nearby) {
                        Image(
                            painter = painterResource(id = R.drawable.nearby),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            colorFilter = null
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = selectedOption.label,
                        color = selectedLabelColor,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = playfairDisplay,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 28.sp,
                            lineHeight = 28.sp
                        )
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Choose cards filter",
                        tint = Color.White.copy(alpha = 0.96f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.widthIn(min = 196.dp),
                shape = menuShape,
                containerColor = toolbarBubbleColor,
                tonalElevation = 6.dp,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
            ) {
                CardsFilterOption.values().forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                color = Color.White.copy(alpha = 0.96f),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = playfairDisplay,
                                    fontWeight = if (option == selectedOption) FontWeight.SemiBold else FontWeight.Medium
                                )
                            )
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSearchBar(
    searchQuery: String,
    onQueryChanged: (String) -> Unit,
    activeFilterCount: Int,
    onFilterClick: () -> Unit,
    onPlaceSelected: (LatLng, String) -> Unit
) {
    var recommended by remember { mutableStateOf<List<AutocompletePrediction>> (emptyList()) }
    val context = LocalContext.current
    val placesClient = remember(context) { if (Places.isInitialized()) Places.createClient(context) else null }
    val predictionThumbnails = remember { mutableStateMapOf<String, Bitmap?>() }
    val searchSurfaceColor = BottomNavToolbarColor
    val searchContentColor = Color.Black
    val searchPlaceholderColor = Color.Black.copy(alpha = 0.62f)
    val visiblePredictions = recommended.take(5)
    val visiblePredictionKey = visiblePredictions.joinToString(separator = "|") { prediction ->
        prediction.placeId
    }

    LaunchedEffect(visiblePredictionKey, placesClient) {
        val client = placesClient ?: return@LaunchedEffect
        val visibleIds = visiblePredictions.map { prediction -> prediction.placeId }.toSet()
        predictionThumbnails.keys
            .filterNot { placeId -> placeId in visibleIds }
            .forEach { placeId -> predictionThumbnails.remove(placeId) }

        visiblePredictions.forEach { prediction ->
            if (prediction.placeId in predictionThumbnails) return@forEach
            predictionThumbnails[prediction.placeId] = fetchPlaceSearchThumbnailBitmap(
                placesClient = client,
                placeId = prediction.placeId
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CoffeeLight)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = searchSurfaceColor
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { query ->
                    onQueryChanged(query)

                    if (query.isNotBlank() && placesClient != null) {
                        val request = com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest.builder()
                            .setQuery(query)
                            .build()
                        placesClient.findAutocompletePredictions(request)
                            .addOnSuccessListener { response ->
                                recommended = response.autocompletePredictions
                            } .addOnFailureListener { recommended = emptyList() }
                    } else {
                        recommended = emptyList()
                    }
                },
                placeholder = { Text("Search location...", color = searchPlaceholderColor) },
                leadingIcon = {
                    IconButton(onClick = onFilterClick) {
                        BadgedBox(
                            badge = {
                                if (activeFilterCount > 0) {
                                    Badge(
                                        containerColor = CafeDark,
                                        contentColor = Color.White
                                    ) {
                                        Text(activeFilterCount.toString())
                                    }
                                }
                            }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.filter),
                                contentDescription = "Filter map cafes",
                                modifier = Modifier.size(19.2.dp),
                                colorFilter = null
                            )
                        }
                    }
                },
                trailingIcon = { IconButton(onClick = { searchQuery }) {Icon(Icons.Filled.Search, null, tint = searchContentColor) } },
                singleLine = true,
                enabled = placesClient != null,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = CafeDark,
                    focusedTextColor = searchContentColor,
                    unfocusedTextColor = searchContentColor,
                    disabledTextColor = searchContentColor.copy(alpha = 0.5f),
                    focusedPlaceholderColor = searchPlaceholderColor,
                    unfocusedPlaceholderColor = searchPlaceholderColor,
                    disabledPlaceholderColor = searchPlaceholderColor.copy(alpha = 0.55f),
                    focusedLeadingIconColor = searchContentColor,
                    unfocusedLeadingIconColor = searchContentColor,
                    disabledLeadingIconColor = searchContentColor.copy(alpha = 0.5f),
                    focusedTrailingIconColor = searchContentColor,
                    unfocusedTrailingIconColor = searchContentColor,
                    disabledTrailingIconColor = searchContentColor.copy(alpha = 0.5f)
                )
            )
        }

        if (recommended.isNotEmpty()) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = CoffeeSurfaceLight,
                    contentColor = CafeDark
                ),
                border = BorderStroke(1.dp, CoffeeDark.copy(alpha = 0.16f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    visiblePredictions.forEach { prediction ->
                        MapSearchPredictionRow(
                            prediction = prediction,
                            thumbnail = predictionThumbnails[prediction.placeId],
                            onClick = {
                                val client = placesClient ?: return@MapSearchPredictionRow
                                val placeTitle = prediction.getPrimaryText(null).toString()
                                val request = com.google.android.libraries.places.api.net.FetchPlaceRequest
                                    .builder(
                                        prediction.placeId,
                                        listOf(Place.Field.LOCATION)
                                    )
                                    .build()

                                client.fetchPlace(request)
                                    .addOnSuccessListener { response ->
                                        response.place.location?.let { latLng ->
                                            onQueryChanged(placeTitle)
                                            onPlaceSelected(latLng, placeTitle)
                                            recommended = emptyList()
                                        }
                                    }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapFilterBottomSheet(
    filters: MapCafeFilters,
    onApplyFilters: (MapCafeFilters) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draftFilters by remember(filters) { mutableStateOf(filters) }
    val maxListHeight = (LocalConfiguration.current.screenHeightDp.dp * 0.68f).coerceAtLeast(320.dp)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CoffeeSurfaceLight,
        contentColor = CafeDark,
        dragHandle = { BottomSheetDefaults.DragHandle(color = CoffeeDark.copy(alpha = 0.42f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Map filters",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = CafeDark
                    )
                    Text(
                        text = if (draftFilters.activeCount == 1) "1 active filter" else "${draftFilters.activeCount} active filters",
                        style = MaterialTheme.typography.bodySmall,
                        color = CoffeeDark.copy(alpha = 0.72f)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Close filters",
                        tint = CafeDark
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxListHeight),
                contentPadding = PaddingValues(bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    MapFilterChipGroup(
                        label = "Open now",
                        selected = draftFilters.openNow.takeUnless { it == OpenNowFilter.ANY },
                        options = listOf(OpenNowFilter.YES, OpenNowFilter.NO),
                        optionLabel = { it.label },
                        onSelected = { selected ->
                            draftFilters = draftFilters.copy(openNow = selected ?: OpenNowFilter.ANY)
                        }
                    )
                }
                item {
                    MapFilterChipGroup(
                        label = "WiFi speed",
                        selected = draftFilters.wifiSpeed,
                        options = listOf(WifiSpeed.FAST, WifiSpeed.DECENT, WifiSpeed.SLOW),
                        optionLabel = { it.label },
                        onSelected = { selected -> draftFilters = draftFilters.copy(wifiSpeed = selected) }
                    )
                }
                item {
                    MapFilterChipGroup(
                        label = "Bathroom",
                        selected = draftFilters.bathroomAvailability,
                        options = listOf(BathroomAvailability.AVAILABLE, BathroomAvailability.NONE),
                        optionLabel = { it.label },
                        onSelected = { selected -> draftFilters = draftFilters.copy(bathroomAvailability = selected) }
                    )
                }
                item {
                    MapFilterChipGroup(
                        label = "Seating",
                        selected = draftFilters.seatingAvailability,
                        options = listOf(
                            SeatingAvailability.PLENTY,
                            SeatingAvailability.FAIR,
                            SeatingAvailability.SCARCE
                        ),
                        optionLabel = { it.label },
                        onSelected = { selected -> draftFilters = draftFilters.copy(seatingAvailability = selected) }
                    )
                }
                item {
                    MapFilterChipGroup(
                        label = "Seating comfort",
                        selected = draftFilters.seatingComfort,
                        options = listOf(
                            SeatingComfort.VERY_COMFORTABLE,
                            SeatingComfort.COMFORTABLE,
                            SeatingComfort.OKAY,
                            SeatingComfort.UNCOMFORTABLE
                        ),
                        optionLabel = { it.label },
                        onSelected = { selected -> draftFilters = draftFilters.copy(seatingComfort = selected) }
                    )
                }
                item {
                    MapFilterChipGroup(
                        label = "Crowd level",
                        selected = draftFilters.crowdLevel,
                        options = listOf(
                            CrowdLevel.EMPTY,
                            CrowdLevel.LIGHT,
                            CrowdLevel.MODERATE,
                            CrowdLevel.BUSY,
                            CrowdLevel.PACKED
                        ),
                        optionLabel = { it.label },
                        onSelected = { selected -> draftFilters = draftFilters.copy(crowdLevel = selected) }
                    )
                }
                item {
                    MapFilterChipGroup(
                        label = "Noise level",
                        selected = draftFilters.noiseLevel,
                        options = listOf(
                            NoiseLevel.SILENT,
                            NoiseLevel.QUIET,
                            NoiseLevel.MODERATE,
                            NoiseLevel.LOUD,
                            NoiseLevel.VERY_LOUD
                        ),
                        optionLabel = { it.label },
                        onSelected = { selected -> draftFilters = draftFilters.copy(noiseLevel = selected) }
                    )
                }
                item {
                    MapFilterChipGroup(
                        label = "Pet friendly",
                        selected = draftFilters.petFriendly,
                        options = listOf(
                            PetFriendly.INDOOR_ALLOWED,
                            PetFriendly.PATIO_ONLY,
                            PetFriendly.NOT_ALLOWED
                        ),
                        optionLabel = { it.label },
                        onSelected = { selected -> draftFilters = draftFilters.copy(petFriendly = selected) }
                    )
                }
                item {
                    MapFilterChipGroup(
                        label = "Cleanliness",
                        selected = draftFilters.cleanlinessRating,
                        options = listOf(
                            CleanlinessRating.GREAT,
                            CleanlinessRating.GOOD,
                            CleanlinessRating.OKAY,
                            CleanlinessRating.POOR
                        ),
                        optionLabel = { it.label },
                        onSelected = { selected -> draftFilters = draftFilters.copy(cleanlinessRating = selected) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onClearFilters,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CafeDark),
                    border = BorderStroke(1.dp, CoffeeDark.copy(alpha = 0.38f))
                ) {
                    Icon(Icons.Filled.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear")
                }
                Button(
                    onClick = { onApplyFilters(draftFilters) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CafeDark,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply")
                }
            }
        }
    }
}

@Composable
private fun <T> MapFilterChipGroup(
    label: String,
    selected: T?,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelected: (T?) -> Unit
) {
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = CoffeeLight,
        labelColor = CafeDark,
        selectedContainerColor = BottomNavToolbarColor,
        selectedLabelColor = Color.Black
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = CafeDark
        )
        WrappingBubbleRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalSpacing = 8.dp,
            verticalSpacing = 8.dp
        ) {
            MapFilterChip(
                text = "Any",
                selected = selected == null,
                colors = chipColors,
                onClick = { onSelected(null) }
            )
            options.forEach { option ->
                val isSelected = selected == option
                MapFilterChip(
                    text = optionLabel(option),
                    selected = isSelected,
                    colors = chipColors,
                    onClick = { onSelected(if (isSelected) null else option) }
                )
            }
        }
    }
}

@Composable
private fun MapFilterChip(
    text: String,
    selected: Boolean,
    colors: SelectableChipColors,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        shape = RoundedCornerShape(8.dp),
        colors = colors,
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = CoffeeDark.copy(alpha = 0.24f),
            selectedBorderColor = CafeDark,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp
        )
    )
}

@Composable
private fun MapFilterEmptyMessage(
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = CoffeeSurfaceLight.copy(alpha = 0.98f),
        contentColor = CafeDark,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, CoffeeDark.copy(alpha = 0.14f))
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No cafes match these filters",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = CafeDark
            )
            TextButton(
                onClick = onClearFilters,
                colors = ButtonDefaults.textButtonColors(contentColor = CafeDark)
            ) {
                Text("Clear")
            }
        }
    }
}

@Composable
private fun MapSearchPredictionRow(
    prediction: AutocompletePrediction,
    thumbnail: Bitmap?,
    onClick: () -> Unit
) {
    val primaryTextColor = CafeDark
    val secondaryTextColor = CoffeeDark.copy(alpha = 0.78f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(8.dp),
            color = CoffeeSurfaceLight
        ) {
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail.asImageBitmap(),
                    contentDescription = prediction.getPrimaryText(null).toString(),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = primaryTextColor.copy(alpha = 0.78f)
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prediction.getPrimaryText(null).toString(),
                fontWeight = FontWeight.SemiBold,
                color = primaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = prediction.getSecondaryText(null).toString(),
                style = MaterialTheme.typography.bodySmall,
                color = secondaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            Icons.Filled.LocationOn,
            contentDescription = "Show on map",
            tint = primaryTextColor
        )
    }
}

@Composable
private fun SavedCafeCacheEffect(
    savedCafes: List<Cafe>,
    placesClient: PlacesClient?
) {
    val savedCafeKey = remember(savedCafes) {
        savedCafes.joinToString(separator = "|") { cafe -> cafe.id }
    }

    LaunchedEffect(savedCafeKey, placesClient) {
        val client = placesClient ?: return@LaunchedEffect
        savedCafes.forEach { savedCafe ->
            val currentCafe = CafeRepository.getCafe(savedCafe.id) ?: savedCafe
            if (!BookmarkRepository.shouldEnrichSavedCafeCache(currentCafe)) return@forEach
            if (!BookmarkRepository.markSavedCafeCacheEnrichmentStarted(currentCafe.id)) return@forEach

            val completeCafe = runCatching {
                fetchCompleteCafeForBookmarkCache(client, currentCafe)
            }.getOrNull()

            if (completeCafe == null) {
                BookmarkRepository.markSavedCafeCacheEnrichmentFailed(currentCafe.id)
                return@forEach
            }

            CafeRepository.updateCafeDetails(savedCafe.id, completeCafe)
            BookmarkRepository.finishSavedCafeCacheEnrichment(
                CafeRepository.getCafe(savedCafe.id) ?: completeCafe
            )
        }
    }
}

private suspend fun fetchCompleteCafeForBookmarkCache(
    placesClient: PlacesClient,
    cafe: Cafe
): Cafe {
    val detailCafe = if (isBookmarkCacheDataIncomplete(cafe)) {
        fetchCafeDetails(placesClient, cafe.id)
    } else {
        null
    }
    val enrichedCafe = detailCafe?.let { detail -> mergeCafeDetailData(cafe, detail) } ?: cafe
    return loadCafeCacheHeroImage(placesClient, enrichedCafe)
}

@Composable
private fun CafeDetailDataEffect(
    cafe: Cafe?,
    placesClient: PlacesClient?
) {
    val loadedPhotoCount = cafe?.photoBitmaps?.count { it != null } ?: 0

    LaunchedEffect(
        cafe?.id,
        cafe?.phone,
        cafe?.address,
        cafe?.latLng,
        cafe?.rating,
        cafe?.userRatingCount,
        cafe?.hours,
        cafe?.photoMetadatas?.size,
        placesClient
    ) {
        val selectedCafe = cafe ?: return@LaunchedEffect
        val client = placesClient ?: return@LaunchedEffect
        if (!isCafeDetailDataIncomplete(selectedCafe)) return@LaunchedEffect

        val detailCafe = runCatching {
            fetchCafeDetails(client, selectedCafe.id)
        }.getOrNull() ?: return@LaunchedEffect

        CafeRepository.updateCafeDetails(selectedCafe.id, detailCafe)
    }

    LaunchedEffect(
        cafe?.id,
        cafe?.photoMetadatas?.size,
        loadedPhotoCount,
        placesClient
    ) {
        val selectedCafe = cafe ?: return@LaunchedEffect
        val client = placesClient ?: return@LaunchedEffect
        val nextPhotoIndex = selectedCafe.nextUnloadedPhotoIndex() ?: return@LaunchedEffect

        val bitmap = fetchCafePhotoBitmap(
            placesClient = client,
            photoMetadata = selectedCafe.photoMetadatas[nextPhotoIndex]
        ) ?: return@LaunchedEffect

        CafeRepository.updateCafePhoto(selectedCafe.id, nextPhotoIndex, bitmap)
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun DetailInfoBubble(
    iconRes: Int,
    iconName: String,
    value: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
    copyLabel: String? = null
) {
    val context = LocalContext.current
    val canCopy = copyLabel != null && !isPhoneUnavailable(value)
    val isWholeBubbleClickable = copyLabel != null
    var tooltipMessage by remember(iconName, value) { mutableStateOf<String?>(null) }
    var showCopyAction by remember(iconName, value) { mutableStateOf(false) }
    val showTooltip = {
        tooltipMessage = iconName
        showCopyAction = canCopy
    }

    LaunchedEffect(tooltipMessage, showCopyAction) {
        val messageToClear = tooltipMessage ?: return@LaunchedEffect
        delay(if (showCopyAction) CROWD_TOOLTIP_DISPLAY_MILLIS * 2 else CROWD_TOOLTIP_DISPLAY_MILLIS)
        if (tooltipMessage == messageToClear) {
            tooltipMessage = null
            showCopyAction = false
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val maxBubbleWidth = maxWidth * 0.92f
        val maxTextWidth = if (maxBubbleWidth > 92.dp) maxBubbleWidth - 64.dp else maxBubbleWidth
        val surfaceModifier = Modifier
            .widthIn(max = maxBubbleWidth)
            .then(
                if (isWholeBubbleClickable) {
                    Modifier.clickable { showTooltip() }
                } else {
                    Modifier
                }
            )

        Surface(
            modifier = surfaceModifier,
            shape = RoundedCornerShape(24.dp),
            color = DetailInfoBubbleColor,
            contentColor = DetailInfoBubbleTextColor,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
            ) {
                Box {
                    val iconModifier = Modifier
                        .size(20.dp)
                        .then(
                            if (isWholeBubbleClickable) {
                                Modifier
                            } else {
                                Modifier.clickable { showTooltip() }
                            }
                        )

                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = iconName,
                        modifier = iconModifier,
                        tint = Color.Unspecified
                    )
                    DetailInfoTooltip(
                        message = tooltipMessage,
                        copyActionLabel = if (showCopyAction) "Copy number" else null,
                        onCopyClick = if (showCopyAction) {
                            {
                                copyTextToClipboard(context, copyLabel ?: iconName, value)
                                tooltipMessage = "$iconName copied to clipboard."
                                showCopyAction = false
                            }
                        } else {
                            null
                        },
                        onDismiss = {
                            tooltipMessage = null
                            showCopyAction = false
                        }
                    )
                }
                Text(
                    text = value,
                    modifier = Modifier.widthIn(max = maxTextWidth),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = DetailInfoBubbleTextColor,
                    textAlign = TextAlign.Center,
                    maxLines = maxLines,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DetailInfoTooltip(
    message: String?,
    copyActionLabel: String?,
    onCopyClick: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    val tooltipMessage = message ?: return
    val density = LocalDensity.current
    val positionProvider = remember(density) {
        CrowdTooltipPositionProvider(
            verticalGapPx = with(density) { 2.dp.roundToPx() },
            screenPaddingPx = with(density) { 8.dp.roundToPx() }
        )
    }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = false)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = 240.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .graphicsLayer { rotationZ = 45f }
                    .background(CrowdTooltipColor, RoundedCornerShape(1.dp))
            )
            Surface(
                modifier = Modifier.offset(y = (-4).dp),
                shape = RoundedCornerShape(6.dp),
                color = CrowdTooltipColor,
                contentColor = DetailInfoBubbleTextColor,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = tooltipMessage,
                        color = DetailInfoBubbleTextColor,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (copyActionLabel != null && onCopyClick != null) {
                        TextButton(
                            onClick = onCopyClick,
                            colors = ButtonDefaults.textButtonColors(contentColor = DetailInfoBubbleTextColor)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.copy),
                                contentDescription = copyActionLabel,
                                modifier = Modifier.size(18.dp),
                                tint = Color.Unspecified
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(copyActionLabel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactDetailInfoBubble(
    iconRes: Int,
    iconName: String,
    value: String,
    modifier: Modifier = Modifier,
    copyLabel: String? = null,
    height: Dp = 42.dp,
    iconSize: Dp = 18.dp,
    horizontalPadding: Dp = 10.dp,
    horizontalSpacing: Dp = 7.dp,
    useCompactText: Boolean = true,
    fillBubbleWidth: Boolean = true
) {
    val context = LocalContext.current
    val canCopy = copyLabel != null && !isPhoneUnavailable(value)
    var tooltipMessage by remember(iconName, value) { mutableStateOf<String?>(null) }
    var showCopyAction by remember(iconName, value) { mutableStateOf(false) }

    LaunchedEffect(tooltipMessage, showCopyAction) {
        val messageToClear = tooltipMessage ?: return@LaunchedEffect
        delay(if (showCopyAction) CROWD_TOOLTIP_DISPLAY_MILLIS * 2 else CROWD_TOOLTIP_DISPLAY_MILLIS)
        if (tooltipMessage == messageToClear) {
            tooltipMessage = null
            showCopyAction = false
        }
    }

    Box(modifier = modifier) {
        val bubbleModifier = Modifier
            .height(height)
            .then(if (fillBubbleWidth) Modifier.fillMaxWidth() else Modifier)

        Surface(
            modifier = bubbleModifier
                .clickable {
                    tooltipMessage = "$iconName: $value"
                    showCopyAction = canCopy
                },
            shape = RoundedCornerShape(24.dp),
            color = DetailInfoBubbleColor,
            contentColor = DetailInfoBubbleTextColor,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = horizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = iconName,
                    modifier = Modifier.size(iconSize),
                    tint = Color.Unspecified
                )
                Text(
                    text = value,
                    modifier = if (fillBubbleWidth) Modifier.weight(1f) else Modifier,
                    style = if (useCompactText) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = DetailInfoBubbleTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        DetailInfoTooltip(
            message = tooltipMessage,
            copyActionLabel = if (showCopyAction) "Copy number" else null,
            onCopyClick = if (showCopyAction) {
                {
                    copyTextToClipboard(context, copyLabel ?: iconName, value)
                    tooltipMessage = "$iconName copied to clipboard."
                    showCopyAction = false
                }
            } else {
                null
            },
            onDismiss = {
                tooltipMessage = null
                showCopyAction = false
            }
        )
    }
}

@Composable
private fun CardInfoBubble(
    iconRes: Int,
    iconName: String,
    value: String,
    modifier: Modifier = Modifier,
    copyLabel: String? = null,
    fillBubbleWidth: Boolean = true
) {
    CompactDetailInfoBubble(
        iconRes = iconRes,
        iconName = iconName,
        value = value,
        modifier = modifier,
        copyLabel = copyLabel,
        height = 36.dp,
        iconSize = 15.dp,
        horizontalPadding = 8.dp,
        horizontalSpacing = 5.dp,
        useCompactText = true,
        fillBubbleWidth = fillBubbleWidth
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun CardTodayHoursBubble(
    hours: LinkedHashMap<String, String>,
    modifier: Modifier = Modifier
) {
    val todayName = currentWeekdayName()
    val summaryText = remember(hours, todayName) {
        val displayHours = if (hours.isEmpty()) linkedMapOf("Hours" to "Hours unavailable") else hours
        todayFirstHoursEntries(displayHours, todayName)
            .firstOrNull()
            ?.let { formatHoursLabel(it.first, it.second) }
            ?: "Hours unavailable"
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CardInfoBubble(
            iconRes = R.drawable.calender,
            iconName = "Today's hours",
            value = summaryText,
            modifier = Modifier.widthIn(max = maxWidth * 0.92f),
            fillBubbleWidth = false
        )
    }
}

@Composable
fun CafeDetailsScreen(navController: NavHostController, cafeId: String) {
    val context = LocalContext.current
    val placesClient = remember(context) { if (Places.isInitialized()) Places.createClient(context) else null }
    val detailTextColor = Color.White
    val detailSecondaryTextColor = Color.White.copy(alpha = 0.78f)
    val cafe = CafeRepository.getCafe(cafeId)
    val crowdAttributeBackendState = rememberCrowdAttributeBackendState(cafe?.id ?: cafeId)

    LaunchedEffect(cafeId) {
        RecentRepository.add(cafeId)
        runCatching { ReviewRepository.loadReviews(cafeId)}
    }

    val reviews = ReviewRepository.reviewsFor(cafeId)

    val isBookmarked = BookmarkRepository.isBookmarked(cafeId)
    CafeDetailDataEffect(cafe = cafe, placesClient = placesClient)
    var studyComposerCafe by remember { mutableStateOf<Cafe?>(null) }
    var suggestionCafe by remember { mutableStateOf<Cafe?>(null) }
    ReportBottomNavOverlayCoverage(isActive = studyComposerCafe != null || suggestionCafe != null)

    BackHandler(enabled = studyComposerCafe != null) {
        studyComposerCafe = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = { BottomNavBar(navController) },
            containerColor = SelectedCafeSurfaceColor,
            contentColor = detailTextColor
        ) { innerPadding ->
            if (cafe == null) {
                Box(
                    modifier = Modifier.padding(innerPadding).fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text("Cafe not found", color = detailTextColor) }
                return@Scaffold
            }
            val crowdAttributes = CrowdAttributeRepository.attributesFor(cafe.id)

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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = detailTextColor
                        )
                    }

                    Text(
                        cafe.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = detailTextColor
                    )

                    IconButton(
                        onClick = { BookmarkRepository.toggle(cafeId) }
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                            tint = detailTextColor
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailInfoBubble(
                        iconRes = R.drawable.address,
                        iconName = "Address",
                        value = cafe.address,
                        maxLines = 3
                    )
                    DetailInfoBubble(
                        iconRes = R.drawable.phone,
                        iconName = "Phone",
                        value = cafe.phone,
                        maxLines = 2,
                        copyLabel = "${cafe.name} phone number"
                    )
                    Text(cafe.status, color = detailTextColor)
                    HoursDropdown(cafe.hours)
                }
                Spacer(Modifier.height(12.dp))
                StudySessionActionRow(
                    cafe = cafe,
                    onCreateStudySession = { studyComposerCafe = cafe },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                CafeCrowdAttributesPanel(
                    cafe = cafe,
                    attributes = crowdAttributes,
                    backendState = crowdAttributeBackendState,
                    onSuggestChanges = { suggestionCafe = cafe }
                )
            }

            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color.Black),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = SelectedCafeSurfaceColor,
                        contentColor = detailTextColor
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Menu", modifier = Modifier.weight(1f), color = detailTextColor)
                        Icon(Icons.Filled.PlayArrow, null, tint = detailTextColor)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Reviews:", modifier = Modifier.weight(1f), color = detailTextColor)
                    TextButton(
                        onClick = { navController.navigate(Screen.WriteReview.createRoute(cafeId)) },
                        colors = ButtonDefaults.textButtonColors(contentColor = detailTextColor)
                    ) {
                        Text("Write review")
                    }
                }
            }

            if (reviews.isEmpty()) {
                item { Text("No reviews yet.", color = detailSecondaryTextColor) }
            } else {
                items(reviews) { review ->
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        border = BorderStroke(1.dp, Color.Black),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = SelectedCafeSurfaceColor,
                            contentColor = detailTextColor
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            review.display_name?.let { name ->
                                Text(name, fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.labelSmall, color = detailTextColor)
                            }
                            Text(review.body, color = detailTextColor)
                        }
                    }
                }
            }
        }
        }

        studyComposerCafe?.let { selectedCafe ->
            StudySessionComposerOverlay(
                cafe = selectedCafe,
                onDismiss = { studyComposerCafe = null },
                modifier = Modifier.matchParentSize()
            )
        }

        suggestionCafe?.let { selectedCafe ->
            CrowdAttributeSuggestionOverlay(
                cafe = selectedCafe,
                attributes = CrowdAttributeRepository.attributesFor(selectedCafe.id),
                onDismiss = { suggestionCafe = null },
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun HoursDropdown(hours: LinkedHashMap<String, String>) {
    val displayHours = remember(hours) {
        if (hours.isEmpty()) linkedMapOf("Hours" to "Hours unavailable") else hours
    }

    val todayName = currentWeekdayName()
    val orderedHours = remember(displayHours, todayName) {
        todayFirstHoursEntries(displayHours, todayName)
    }
    val summaryEntry = orderedHours.firstOrNull()
    val summaryText = summaryEntry?.let { formatHoursLabel(it.first, it.second) } ?: "Hours unavailable"
    var expanded by remember { mutableStateOf(false) }
    val isExpandable = orderedHours.size > 1

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val maxBubbleWidth = maxWidth * 0.92f
        val maxTextWidth = if (maxBubbleWidth > 92.dp) maxBubbleWidth - 86.dp else maxBubbleWidth

        Surface(
            modifier = Modifier
                .widthIn(max = maxBubbleWidth)
                .clickable(enabled = isExpandable) { expanded = !expanded },
            shape = RoundedCornerShape(24.dp),
            color = DetailInfoBubbleColor,
            contentColor = DetailInfoBubbleTextColor,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.calender),
                        contentDescription = "Hours",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Unspecified
                    )
                    Text(
                        text = summaryText,
                        modifier = Modifier.widthIn(max = maxTextWidth),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DetailInfoBubbleTextColor,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isExpandable) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = if (expanded) "Collapse hours" else "Expand hours",
                            modifier = Modifier.graphicsLayer {
                                rotationZ = if (expanded) 180f else 0f
                            },
                            tint = DetailInfoBubbleTextColor
                        )
                    }
                }

                if (expanded && orderedHours.size > 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 8.dp, bottom = 6.dp),
                        color = DetailInfoBubbleTextColor.copy(alpha = 0.16f)
                    )
                    orderedHours.drop(1).forEach { (day, hoursText) ->
                        Text(
                            text = formatHoursLabel(day, hoursText),
                            modifier = Modifier
                                .widthIn(max = maxTextWidth)
                                .padding(vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = DetailInfoBubbleTextColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private fun currentWeekdayName(): String {
    return Calendar.getInstance(Locale.US)
        .getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US)
        .orEmpty()
}

private fun todayFirstHoursEntries(
    hours: LinkedHashMap<String, String>,
    todayName: String
): List<Pair<String, String>> {
    val entries = hours.entries.map { it.key to it.value }
    val todayIndex = entries.indexOfFirst { (day, _) -> day.equals(todayName, ignoreCase = true) }
    return if (todayIndex > 0) {
        entries.drop(todayIndex) + entries.take(todayIndex)
    } else {
        entries
    }
}

private fun formatHoursLabel(day: String, hoursText: String): String {
    return if (day.equals("Hours", ignoreCase = true)) hoursText else "$day: $hoursText"
}

private val SelectedCafeSurfaceColor = Color(0xFF694B2E)
private val CrowdAttributeBubbleColor = Color(0xFFC5A07D)
private val CrowdTooltipColor = Color(0xFFF6E9D8)
private val DetailInfoBubbleColor = Color(0xFFC5A07D)
private val SuggestChangesBubbleColor = Color(0xFFBDEBFF)
private val DetailInfoBubbleTextColor = Color(0xFF4B3621)
private val StudySessionPopupSurfaceColor = SelectedCafeSurfaceColor
private val StudySessionPopupTextColor = Color.White
private val StudySessionPopupSecondaryTextColor = Color.White.copy(alpha = 0.78f)
private val StudySessionPopupFieldColor = DetailInfoBubbleColor
private val StudySessionPopupFieldTextColor = Color.Black
private val StudySessionCafeButtonColor = Color(0xFFB44436)
private val CrowdActionBubbleSize = 42.dp
private const val CROWD_TOOLTIP_DISPLAY_MILLIS = 2400L
private const val CAFE_CROWD_PHOTO_BUCKET = "cafe-crowd-photos"

private data class VisitorPhotoViewerState(
    val title: String,
    val photoUris: List<String>,
    val initialIndex: Int
)

private data class CrowdAttributeBackendState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)

@Composable
private fun rememberCrowdAttributeBackendState(cafeId: String?): CrowdAttributeBackendState {
    var backendState by remember(cafeId) { mutableStateOf(CrowdAttributeBackendState()) }

    LaunchedEffect(cafeId) {
        if (cafeId.isNullOrBlank()) {
            backendState = CrowdAttributeBackendState()
            return@LaunchedEffect
        }

        backendState = CrowdAttributeBackendState(isLoading = true)
        backendState = runCatching {
            CrowdAttributeRepository.loadFromSupabase(cafeId)
        }.fold(
            onSuccess = { loaded ->
                if (loaded) {
                    CrowdAttributeBackendState()
                } else {
                    CrowdAttributeBackendState(
                        message = "No community suggestions saved yet for this cafe id: ${cafeId.shortCafeId()}."
                    )
                }
            },
            onFailure = { error ->
                error.printStackTrace()
                CrowdAttributeBackendState(
                    message = error.toCrowdLoadMessage(),
                    isError = true
                )
            }
        )
    }

    return backendState
}

private fun String.shortCafeId(): String {
    return if (length <= 14) this else "${take(14)}..."
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun CafeCrowdAttributesPanel(
    cafe: Cafe,
    attributes: CafeCrowdAttributes,
    backendState: CrowdAttributeBackendState = CrowdAttributeBackendState(),
    onSuggestChanges: () -> Unit,
    modifier: Modifier = Modifier
) {
    val panelTextColor = LocalContentColor.current
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    var visitorPhotoViewerState by remember(cafe.id) { mutableStateOf<VisitorPhotoViewerState?>(null) }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        val compactAttributeMaxWidth = maxWidth * 0.62f
        val compactBubbleTextStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        val wifiValue = attributes.wifiName.displayCrowdValue()
        val bathroomValue = attributes.bathroomAvailability.label
        val shouldKeepWifiCompact = protectedAttributeFitsCompact(
            value = wifiValue,
            compactMaxWidth = compactAttributeMaxWidth,
            textStyle = compactBubbleTextStyle,
            textMeasurer = textMeasurer,
            density = density
        )
        val shouldKeepBathroomCompact = protectedAttributeFitsCompact(
            value = bathroomValue,
            compactMaxWidth = compactAttributeMaxWidth,
            textStyle = compactBubbleTextStyle,
            textMeasurer = textMeasurer,
            density = density
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CrowdAttributeBubbleSection(
                densePacking = true,
                maxItemWidthFraction = 0.62f,
                topEndAction = {
                    SuggestChangesIconBubble(onClick = onSuggestChanges)
                }
            ) {
                CrowdAttributeBubble(
                    iconRes = R.drawable.outlets,
                    iconName = "Outlets",
                    value = attributes.outletAvailability.label
                )
                if (shouldKeepWifiCompact) {
                    CrowdAttributeBubbleWithProtectedKey(
                        iconRes = R.drawable.wifi,
                        iconName = "WiFi",
                        value = wifiValue,
                        keyIconRes = R.drawable.key,
                        keyIconName = "WiFi key",
                        secret = attributes.wifiPassword,
                        isAvailable = attributes.wifiSpeed.toAvailabilityFlag(),
                        cafeLatLng = cafe.latLng,
                        secretLabel = "WiFi password",
                        clipboardLabel = "${cafe.name} WiFi key",
                        isCopyable = true
                    )
                }
                CrowdAttributeBubble(
                    iconRes = R.drawable.wifi_speed,
                    iconName = "WiFi Speed",
                    value = attributes.wifiSpeed.label
                )
                CrowdAttributeBubble(
                    iconRes = R.drawable.seating_availability,
                    iconName = "Seating Availability",
                    value = attributes.seatingAvailability.label,
                    iconSize = 26.dp
                )
                CrowdAttributeBubble(
                    iconRes = R.drawable.seating_space,
                    iconName = "Seating space",
                    value = attributes.seatingSpace.label,
                    iconSize = 26.dp
                )
                CrowdAttributeBubble(
                    iconRes = R.drawable.seating_comfort,
                    iconName = "Seating Comfort",
                    value = attributes.seatingComfort.label
                )
                if (shouldKeepBathroomCompact) {
                    CrowdAttributeBubbleWithProtectedKey(
                        iconRes = R.drawable.bathroom,
                        iconName = "Bathroom",
                        value = bathroomValue,
                        keyIconRes = R.drawable.key,
                        keyIconName = "Bathroom key",
                        secret = attributes.bathroomCode,
                        isAvailable = attributes.bathroomAvailability.toAvailabilityFlag(),
                        cafeLatLng = cafe.latLng,
                        secretLabel = "Bathroom code",
                        clipboardLabel = "${cafe.name} bathroom key",
                        isCopyable = false
                    )
                }
                CrowdAttributeBubble(
                    iconRes = R.drawable.pet_friendly,
                    iconName = "Pet friendly",
                    value = attributes.petFriendly.label
                )
                CrowdAttributeBubble(
                    iconRes = R.drawable.cleanliness,
                    iconName = "Cleanliness",
                    value = attributes.cleanlinessRating.label
                )
                CrowdAttributeBubble(
                    iconRes = R.drawable.crowd_level,
                    iconName = "Crowd Level",
                    value = attributes.crowdLevel.label
                )
                CrowdAttributeBubble(
                    iconRes = R.drawable.noise_level,
                    iconName = "Noise Level",
                    value = attributes.noiseLevel.label
                )
                CrowdAttributeBubble(
                    iconRes = R.drawable.vibe_and_atmosphere,
                    iconName = "Vibe and Atmosphere",
                    value = attributes.vibeTags.displayVibeValue()
                )
            }

            if (!shouldKeepWifiCompact || !shouldKeepBathroomCompact) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!shouldKeepWifiCompact) {
                        CrowdAttributeBubbleWithProtectedKey(
                            iconRes = R.drawable.wifi,
                            iconName = "WiFi",
                            value = wifiValue,
                            keyIconRes = R.drawable.key,
                            keyIconName = "WiFi key",
                            secret = attributes.wifiPassword,
                            isAvailable = attributes.wifiSpeed.toAvailabilityFlag(),
                            cafeLatLng = cafe.latLng,
                            secretLabel = "WiFi password",
                            clipboardLabel = "${cafe.name} WiFi key",
                            isCopyable = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (!shouldKeepBathroomCompact) {
                        CrowdAttributeBubbleWithProtectedKey(
                            iconRes = R.drawable.bathroom,
                            iconName = "Bathroom",
                            value = bathroomValue,
                            keyIconRes = R.drawable.key,
                            keyIconName = "Bathroom key",
                            secret = attributes.bathroomCode,
                            isAvailable = attributes.bathroomAvailability.toAvailabilityFlag(),
                            cafeLatLng = cafe.latLng,
                            secretLabel = "Bathroom code",
                            clipboardLabel = "${cafe.name} bathroom key",
                            isCopyable = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                VisitorPhotoCarousel(
                    title = "Seating photos",
                    photoUris = attributes.seatingPhotoUris,
                    modifier = Modifier.weight(1f),
                    onPhotoClick = { index ->
                        visitorPhotoViewerState = VisitorPhotoViewerState(
                            title = "Seating photos",
                            photoUris = attributes.seatingPhotoUris,
                            initialIndex = index
                        )
                    }
                )
                VisitorPhotoCarousel(
                    title = "Menu Photos",
                    photoUris = attributes.menuPhotoUris,
                    modifier = Modifier.weight(1f),
                    onPhotoClick = { index ->
                        visitorPhotoViewerState = VisitorPhotoViewerState(
                            title = "Menu Photos",
                            photoUris = attributes.menuPhotoUris,
                            initialIndex = index
                        )
                    }
                )
            }

            if (attributes.lastUpdatedEpochMillis != null) {
                Text(
                    text = "Updated from community suggestions.",
                    color = panelTextColor.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            CrowdAttributeBackendStatusText(
                backendState = backendState,
                hasCommunityAttributes = attributes.lastUpdatedEpochMillis != null,
                textColor = panelTextColor
            )
        }
    }

    visitorPhotoViewerState
        ?.takeIf { it.photoUris.isNotEmpty() }
        ?.let { viewerState ->
            VisitorPhotoFullScreenViewer(
                title = viewerState.title,
                photoUris = viewerState.photoUris,
                initialPage = viewerState.initialIndex,
                onDismiss = { visitorPhotoViewerState = null }
            )
    }
}

@Composable
private fun CrowdAttributeBackendStatusText(
    backendState: CrowdAttributeBackendState,
    hasCommunityAttributes: Boolean,
    textColor: Color
) {
    val message = when {
        backendState.isLoading -> "Loading community suggestions..."
        backendState.isError -> backendState.message
        hasCommunityAttributes -> null
        else -> backendState.message
    } ?: return

    Text(
        text = message,
        color = if (backendState.isError) MaterialTheme.colorScheme.error else textColor.copy(alpha = 0.78f),
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun CrowdAttributeSuggestionOverlay(
    cafe: Cafe,
    attributes: CafeCrowdAttributes,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    var isVisible by remember { mutableStateOf(false) }
    var isClosing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    fun closeSuggestion() {
        if (isClosing) return
        isClosing = true
        isVisible = false
        scope.launch {
            delay(140)
            onDismiss()
        }
    }

    BackHandler(enabled = true) {
        closeSuggestion()
    }

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "crowdSuggestionAlpha"
    )
    val overlayScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.92f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "crowdSuggestionScale"
    )

    Box(modifier = modifier.zIndex(10f)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.24f * overlayAlpha))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    closeSuggestion()
                }
        )

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(20.dp)
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .heightIn(max = 660.dp)
                .graphicsLayer {
                    alpha = overlayAlpha
                    scaleX = overlayScale
                    scaleY = overlayScale
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = StudySessionPopupSurfaceColor,
                contentColor = StudySessionPopupTextColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            border = BorderStroke(2.dp, Color.Black)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = SuggestChangesBubbleColor
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.edit),
                            contentDescription = null,
                            modifier = Modifier.padding(10.dp),
                            tint = Color.Unspecified
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Suggest changes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = StudySessionPopupTextColor
                        )
                        Text(
                            text = cafe.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = StudySessionPopupSecondaryTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { closeSuggestion() }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close suggestion form",
                            tint = StudySessionPopupTextColor
                        )
                    }
                }

                CrowdAttributeSuggestionForm(
                    cafe = cafe,
                    attributes = attributes,
                    onSubmitSuggestion = { suggestion ->
                        val suggestionWithUploadedPhotos = suggestion.copy(
                            seatingPhotoUris = uploadCafeCrowdPhotos(
                                context = context,
                                cafeId = cafe.id,
                                photoKind = "seating",
                                photoUris = suggestion.seatingPhotoUris
                            ),
                            menuPhotoUris = uploadCafeCrowdPhotos(
                                context = context,
                                cafeId = cafe.id,
                                photoKind = "menu",
                                photoUris = suggestion.menuPhotoUris
                            )
                        )
                        CrowdAttributeRepository.submitSuggestionToSupabase(
                            cafeId = cafe.id,
                            suggestion = suggestionWithUploadedPhotos
                        )
                    },
                    onCancel = { closeSuggestion() },
                    onSubmit = { closeSuggestion() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    contentPadding = PaddingValues(bottom = 0.dp)
                )
            }
        }
    }
}

@Composable
private fun SuggestChangesIconBubble(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(CrowdActionBubbleSize)
            .clickable { onClick() },
        shape = CircleShape,
        color = SuggestChangesBubbleColor,
        contentColor = Color.Black,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = R.drawable.edit),
                contentDescription = "Suggest changes",
                modifier = Modifier.size(21.dp),
                tint = Color.Unspecified
            )
        }
    }
}

@Composable
private fun CrowdAttributeBubbleSection(
    densePacking: Boolean = false,
    maxItemWidthFraction: Float = 1f,
    topEndAction: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (topEndAction == null) {
            WrappingBubbleRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalSpacing = 8.dp,
                verticalSpacing = 8.dp,
                densePacking = densePacking,
                maxItemWidthFraction = maxItemWidthFraction
            ) {
                content()
            }
        } else {
            WrappingBubbleRowWithTopEndAction(
                modifier = Modifier.fillMaxWidth(),
                horizontalSpacing = 8.dp,
                verticalSpacing = 8.dp,
                densePacking = densePacking,
                maxItemWidthFraction = maxItemWidthFraction,
                topEndAction = topEndAction
            ) {
                content()
            }
        }
    }
}

@Composable
private fun CrowdAttributeTooltip(
    message: String?,
    onDismiss: () -> Unit
) {
    val tooltipMessage = message ?: return
    val density = LocalDensity.current
    val positionProvider = remember(density) {
        CrowdTooltipPositionProvider(
            verticalGapPx = with(density) { 2.dp.roundToPx() },
            screenPaddingPx = with(density) { 8.dp.roundToPx() }
        )
    }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = false)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = 220.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .graphicsLayer { rotationZ = 45f }
                    .background(CrowdTooltipColor, RoundedCornerShape(1.dp))
            )
            Surface(
                modifier = Modifier.offset(y = (-4).dp),
                shape = RoundedCornerShape(6.dp),
                color = CrowdTooltipColor,
                contentColor = Color.Black,
                shadowElevation = 6.dp
            ) {
                Text(
                    text = tooltipMessage,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    color = Color.Black.copy(alpha = 0.86f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private class CrowdTooltipPositionProvider(
    private val verticalGapPx: Int,
    private val screenPaddingPx: Int
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val anchorCenterX = anchorBounds.left + (anchorBounds.width / 2)
        val preferredX = anchorCenterX - (popupContentSize.width / 2)
        val minX = screenPaddingPx
        val maxX = (windowSize.width - popupContentSize.width - screenPaddingPx).coerceAtLeast(minX)
        val x = preferredX.coerceIn(minX, maxX)

        val preferredY = anchorBounds.bottom + verticalGapPx
        val minY = screenPaddingPx
        val maxY = (windowSize.height - popupContentSize.height - screenPaddingPx).coerceAtLeast(minY)
        val y = preferredY.coerceIn(minY, maxY)

        return IntOffset(x, y)
    }
}

@Composable
private fun WrappingBubbleRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    densePacking: Boolean = false,
    maxItemWidthFraction: Float = 1f,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val horizontalSpacingPx = with(density) { horizontalSpacing.roundToPx() }
    val verticalSpacingPx = with(density) { verticalSpacing.roundToPx() }

    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val rowMaxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else constraints.minWidth
        val childMaxWidth = (rowMaxWidth * maxItemWidthFraction.coerceIn(0.1f, 1f))
            .roundToInt()
            .coerceAtLeast(0)
        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0, maxWidth = childMaxWidth)
        val placeables = measurables.map { measurable -> measurable.measure(childConstraints) }
        val positions = MutableList(placeables.size) { 0 to 0 }

        var x = 0
        var y = 0
        var rowHeight = 0
        var contentWidth = 0

        if (densePacking) {
            val rowItems = mutableListOf<MutableList<Int>>()
            val rowWidths = mutableListOf<Int>()
            val rowHeights = mutableListOf<Int>()
            val orderedIndices = placeables.indices
                .sortedWith(compareByDescending<Int> { placeables[it].width }.thenBy { it })

            orderedIndices.forEach { index ->
                val placeable = placeables[index]
                var bestRowIndex = -1
                var bestRemainingWidth = Int.MAX_VALUE

                rowWidths.forEachIndexed { rowIndex, rowWidth ->
                    val candidateWidth = rowWidth + horizontalSpacingPx + placeable.width
                    if (candidateWidth <= rowMaxWidth) {
                        val remainingWidth = rowMaxWidth - candidateWidth
                        if (remainingWidth < bestRemainingWidth) {
                            bestRemainingWidth = remainingWidth
                            bestRowIndex = rowIndex
                        }
                    }
                }

                if (bestRowIndex == -1) {
                    rowItems.add(mutableListOf(index))
                    rowWidths.add(placeable.width)
                    rowHeights.add(placeable.height)
                } else {
                    rowItems[bestRowIndex].add(index)
                    rowWidths[bestRowIndex] = rowWidths[bestRowIndex] + horizontalSpacingPx + placeable.width
                    rowHeights[bestRowIndex] = maxOf(rowHeights[bestRowIndex], placeable.height)
                }
            }

            rowItems.forEachIndexed { rowIndex, indices ->
                x = 0
                indices.sort()
                indices.forEach { index ->
                    val placeable = placeables[index]
                    val childX = if (x == 0) 0 else x + horizontalSpacingPx
                    positions[index] = childX to y
                    x = childX + placeable.width
                }
                contentWidth = maxOf(contentWidth, x)
                y += rowHeights[rowIndex] + verticalSpacingPx
            }
            if (rowItems.isNotEmpty()) {
                y -= verticalSpacingPx
            }
        } else {
            placeables.forEachIndexed { index, placeable ->
                val spacingBefore = if (x == 0) 0 else horizontalSpacingPx
                if (x > 0 && x + spacingBefore + placeable.width > rowMaxWidth) {
                    contentWidth = maxOf(contentWidth, x)
                    y += rowHeight + verticalSpacingPx
                    x = 0
                    rowHeight = 0
                }

                val childX = if (x == 0) 0 else x + horizontalSpacingPx
                positions[index] = childX to y
                x = childX + placeable.width
                rowHeight = maxOf(rowHeight, placeable.height)
            }

            contentWidth = maxOf(contentWidth, x)
            y = if (placeables.isEmpty()) 0 else y + rowHeight
        }

        val contentHeight = y
        val layoutWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else contentWidth

        layout(
            width = layoutWidth.coerceIn(constraints.minWidth, constraints.maxWidth),
            height = contentHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
        ) {
            placeables.forEachIndexed { index, placeable ->
                val (childX, childY) = positions[index]
                placeable.placeRelative(childX, childY)
            }
        }
    }
}

@Composable
private fun WrappingBubbleRowWithTopEndAction(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 8.dp,
    densePacking: Boolean = false,
    maxItemWidthFraction: Float = 1f,
    topEndAction: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val horizontalSpacingPx = with(density) { horizontalSpacing.roundToPx() }
    val verticalSpacingPx = with(density) { verticalSpacing.roundToPx() }

    Layout(
        content = {
            topEndAction()
            content()
        },
        modifier = modifier
    ) { measurables, constraints ->
        val rowMaxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else constraints.minWidth
        val actionConstraints = constraints.copy(minWidth = 0, minHeight = 0, maxWidth = rowMaxWidth)
        val actionPlaceable = measurables.firstOrNull()?.measure(actionConstraints)
        val bubbleMeasurables = measurables.drop(1)
        val childMaxWidth = (rowMaxWidth * maxItemWidthFraction.coerceIn(0.1f, 1f))
            .roundToInt()
            .coerceAtLeast(0)
        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0, maxWidth = childMaxWidth)
        val bubblePlaceables = bubbleMeasurables.map { measurable -> measurable.measure(childConstraints) }
        val bubblePositions = MutableList(bubblePlaceables.size) { 0 to 0 }

        val topRowReservedWidth = actionPlaceable?.let { it.width + horizontalSpacingPx } ?: 0
        val rowMaxWidths = mutableListOf((rowMaxWidth - topRowReservedWidth).coerceAtLeast(0))
        val rowItems = mutableListOf(mutableListOf<Int>())
        val rowWidths = mutableListOf(0)
        val rowHeights = mutableListOf(actionPlaceable?.height ?: 0)
        val orderedIndices = if (densePacking) {
            bubblePlaceables.indices
                .sortedWith(compareByDescending<Int> { bubblePlaceables[it].width }.thenBy { it })
        } else {
            bubblePlaceables.indices.toList()
        }

        fun addBubbleToRow(rowIndex: Int, bubbleIndex: Int) {
            val placeable = bubblePlaceables[bubbleIndex]
            rowItems[rowIndex].add(bubbleIndex)
            rowWidths[rowIndex] = if (rowWidths[rowIndex] == 0) {
                placeable.width
            } else {
                rowWidths[rowIndex] + horizontalSpacingPx + placeable.width
            }
            rowHeights[rowIndex] = maxOf(rowHeights[rowIndex], placeable.height)
        }

        orderedIndices.forEach { index ->
            val placeable = bubblePlaceables[index]
            var bestRowIndex = -1
            var bestRemainingWidth = Int.MAX_VALUE

            rowWidths.forEachIndexed { rowIndex, rowWidth ->
                val candidateWidth = if (rowWidth == 0) {
                    placeable.width
                } else {
                    rowWidth + horizontalSpacingPx + placeable.width
                }
                if (candidateWidth <= rowMaxWidths[rowIndex]) {
                    val remainingWidth = rowMaxWidths[rowIndex] - candidateWidth
                    if (!densePacking) {
                        bestRowIndex = rowIndex
                        return@forEachIndexed
                    }
                    if (remainingWidth < bestRemainingWidth) {
                        bestRemainingWidth = remainingWidth
                        bestRowIndex = rowIndex
                    }
                }
            }

            if (bestRowIndex == -1) {
                rowItems.add(mutableListOf())
                rowWidths.add(0)
                rowHeights.add(0)
                rowMaxWidths.add(rowMaxWidth)
                bestRowIndex = rowItems.lastIndex
            }

            addBubbleToRow(bestRowIndex, index)
        }

        var contentWidth = actionPlaceable?.width ?: 0
        var y = 0
        rowItems.forEachIndexed { rowIndex, indices ->
            var x = 0
            indices.sort()
            indices.forEach { index ->
                val placeable = bubblePlaceables[index]
                val childX = if (x == 0) 0 else x + horizontalSpacingPx
                bubblePositions[index] = childX to y
                x = childX + placeable.width
            }
            contentWidth = maxOf(contentWidth, x)
            y += rowHeights[rowIndex] + verticalSpacingPx
        }
        if (rowItems.isNotEmpty()) {
            y -= verticalSpacingPx
        }

        val layoutWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else contentWidth
        val contentHeight = maxOf(y, actionPlaceable?.height ?: 0)

        layout(
            width = layoutWidth.coerceIn(constraints.minWidth, constraints.maxWidth),
            height = contentHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
        ) {
            bubblePlaceables.forEachIndexed { index, placeable ->
                val (childX, childY) = bubblePositions[index]
                placeable.placeRelative(childX, childY)
            }
            actionPlaceable?.placeRelative(layoutWidth - actionPlaceable.width, 0)
        }
    }
}

@Composable
private fun CrowdAttributePlainSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = LocalContentColor.current)
        content()
    }
}

private fun protectedAttributeFitsCompact(
    value: String,
    compactMaxWidth: Dp,
    textStyle: TextStyle,
    textMeasurer: TextMeasurer,
    density: Density
): Boolean {
    val displayValue = value.displayCrowdBubbleValue()
    val requiredWidthPx = with(density) {
        if (displayValue == null) {
            CrowdActionBubbleSize.toPx() + 20.dp.toPx() + CrowdActionBubbleSize.toPx()
        } else {
            val textWidthPx = textMeasurer.measure(
                text = displayValue,
                style = textStyle
            ).size.width
            12.dp.toPx() + 20.dp.toPx() + 8.dp.toPx() + textWidthPx + 12.dp.toPx() +
                20.dp.toPx() + CrowdActionBubbleSize.toPx()
        }
    }

    return requiredWidthPx <= with(density) { compactMaxWidth.toPx() }
}

@Composable
private fun CrowdAttributeBubble(
    iconRes: Int,
    iconName: String,
    value: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp
) {
    val displayValue = value.displayCrowdBubbleValue()
    var tooltipMessage by remember(iconName, value) { mutableStateOf<String?>(null) }

    LaunchedEffect(tooltipMessage) {
        val messageToClear = tooltipMessage ?: return@LaunchedEffect
        delay(CROWD_TOOLTIP_DISPLAY_MILLIS)
        if (tooltipMessage == messageToClear) {
            tooltipMessage = null
        }
    }

    Box(modifier = modifier) {
        val surfaceModifier = if (displayValue == null) {
            Modifier.size(CrowdActionBubbleSize)
        } else {
            Modifier
                .height(CrowdActionBubbleSize)
                .widthIn(min = CrowdActionBubbleSize)
        }
        Surface(
            modifier = surfaceModifier
                .clickable { tooltipMessage = crowdAttributeTooltipText(iconName, value) },
            shape = CircleShape,
            color = CrowdAttributeBubbleColor,
            contentColor = Color.Black
        ) {
            if (displayValue == null) {
                Box(contentAlignment = Alignment.Center) {
                    CrowdBubbleIcon(iconRes = iconRes, contentDescription = iconName, size = iconSize)
                }
            } else {
                Row(
                    modifier = Modifier
                        .height(CrowdActionBubbleSize)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CrowdBubbleIcon(iconRes = iconRes, contentDescription = iconName, size = iconSize)
                    Text(
                        text = displayValue,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        CrowdAttributeTooltip(
            message = tooltipMessage,
            onDismiss = { tooltipMessage = null }
        )
    }
}

@Composable
private fun CrowdBubbleIcon(
    iconRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    val resources = LocalContext.current.resources
    val hasDrawableResource = remember(resources, iconRes) {
        runCatching {
            resources.getResourceTypeName(iconRes)
        }.getOrNull() in setOf("drawable", "mipmap")
    }

    if (hasDrawableResource) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = modifier.size(size),
            tint = Color.Black
        )
    } else {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = contentDescription,
            modifier = modifier.size(size),
            tint = Color.Black
        )
    }
}

@Composable
private fun CrowdAttributeBubbleWithProtectedKey(
    iconRes: Int,
    iconName: String,
    value: String,
    keyIconRes: Int,
    keyIconName: String,
    secret: ProtectedCrowdSecret,
    isAvailable: Boolean?,
    cafeLatLng: LatLng?,
    secretLabel: String,
    clipboardLabel: String,
    isCopyable: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CrowdAttributeBubble(
            iconRes = iconRes,
            iconName = iconName,
            value = value,
            modifier = Modifier.weight(1f, fill = false)
        )
        ProtectedKeyConnectorLine()
        ProtectedSecretKeyBubble(
            keyIconRes = keyIconRes,
            keyIconName = keyIconName,
            secret = secret,
            isAvailable = isAvailable,
            cafeLatLng = cafeLatLng,
            secretLabel = secretLabel,
            clipboardLabel = clipboardLabel,
            isCopyable = isCopyable
        )
    }
}

@Composable
private fun ProtectedKeyConnectorLine() {
    Box(
        modifier = Modifier
            .width(20.dp)
            .height(3.6.dp)
            .background(CrowdAttributeBubbleColor, RoundedCornerShape(2.dp))
    )
}

@Composable
private fun ProtectedSecretKeyBubble(
    keyIconRes: Int,
    keyIconName: String,
    secret: ProtectedCrowdSecret,
    isAvailable: Boolean?,
    cafeLatLng: LatLng?,
    secretLabel: String,
    clipboardLabel: String,
    isCopyable: Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember(context) { LocationServices.getFusedLocationProviderClient(context) }
    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    var displayState by remember(secret, isAvailable, cafeLatLng) {
        mutableStateOf(protectedSecretDisplayState(secret, isNearby = false, isAvailable = isAvailable))
    }
    var tooltipMessage by remember(secret, isAvailable, cafeLatLng) { mutableStateOf<String?>(null) }

    LaunchedEffect(tooltipMessage) {
        val messageToClear = tooltipMessage ?: return@LaunchedEffect
        delay(CROWD_TOOLTIP_DISPLAY_MILLIS)
        if (tooltipMessage == messageToClear) {
            tooltipMessage = null
        }
    }

    Box {
        Surface(
            modifier = Modifier.size(CrowdActionBubbleSize),
            onClick = {
                when (val state = displayState) {
                    is ProtectedSecretDisplayState.Revealed -> {
                        if (isCopyable) {
                            copySecretToClipboard(context, clipboardLabel, state.value)
                            tooltipMessage = "$secretLabel copied to clipboard."
                        } else {
                            tooltipMessage = protectedSecretRevealedMessage(secretLabel, state.value, isCopyable = false)
                        }
                    }

                    ProtectedSecretDisplayState.Censored -> {
                        if (!hasLocationPermission) {
                            tooltipMessage = "Location permission is needed to reveal this here."
                            return@Surface
                        }

                        scope.launch {
                            val userLatLng = runCatching {
                                getCurrentOrLastLocation(fusedLocationClient).toLatLng()
                            }.getOrNull()
                            val isNearby = isUserNearCafe(userLatLng, cafeLatLng)
                            val updatedState = protectedSecretDisplayState(
                                secret = secret,
                                isNearby = isNearby,
                                isAvailable = isAvailable
                            )
                            displayState = updatedState
                            tooltipMessage = when {
                                cafeLatLng == null -> "Cafe location is unavailable."
                                !isNearby -> "Visit this cafe to reveal."
                                updatedState == ProtectedSecretDisplayState.AskStaff -> "Ask staff while you are there."
                                updatedState == ProtectedSecretDisplayState.Unknown -> "No one has submitted this yet."
                                updatedState is ProtectedSecretDisplayState.Revealed -> {
                                    protectedSecretRevealedMessage(secretLabel, updatedState.value, isCopyable)
                                }
                                else -> displayTextForSecretState(updatedState)
                            }
                        }
                    }

                    ProtectedSecretDisplayState.AskStaff -> tooltipMessage = "Ask staff while you are there."
                    ProtectedSecretDisplayState.Unavailable -> tooltipMessage = "Not available."
                    ProtectedSecretDisplayState.Unknown -> tooltipMessage = "No one has submitted this yet."
                }
            },
            shape = CircleShape,
            color = CrowdAttributeBubbleColor,
            contentColor = Color.Black
        ) {
            Box(contentAlignment = Alignment.Center) {
                CrowdBubbleIcon(iconRes = keyIconRes, contentDescription = keyIconName)
            }
        }
        CrowdAttributeTooltip(
            message = tooltipMessage,
            onDismiss = { tooltipMessage = null }
        )
    }
}

private fun crowdAttributeTooltipText(iconName: String, value: String): String {
    return value.displayCrowdBubbleValue()?.let { "$iconName: $it" } ?: iconName
}

private fun protectedSecretRevealedMessage(
    secretLabel: String,
    value: String,
    isCopyable: Boolean
): String {
    return if (isCopyable) {
        "$secretLabel: $value\nTap again to copy."
    } else {
        "$secretLabel: $value"
    }
}

private fun String?.displayCrowdValue(): String {
    return this?.trim()?.takeIf { it.isNotBlank() } ?: "Unknown"
}

private fun String.displayCrowdBubbleValue(): String? {
    return trim()
        .takeIf { it.isNotBlank() }
        ?.takeUnless { it.equals("Unknown", ignoreCase = true) }
}

private fun Set<VibeTag>.displayVibeValue(): String {
    return if (isEmpty()) {
        "Unknown"
    } else {
        joinToString(separator = ", ") { it.label }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VisitorPhotoCarousel(
    title: String,
    photoUris: List<String>,
    modifier: Modifier = Modifier,
    onPhotoClick: (Int) -> Unit
) {
    val textColor = LocalContentColor.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            color = CrowdAttributeBubbleColor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        if (photoUris.isEmpty()) {
            Text(
                text = "No visitor photos yet.",
                color = textColor.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        } else {
            val pagerState = rememberPagerState(pageCount = { photoUris.size })

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .widthIn(max = 142.dp)
                    .aspectRatio(3f / 4f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.Black),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = CoffeeLight,
                    contentColor = Color.Black
                )
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onPhotoClick(page) }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = photoUris[page],
                            contentDescription = "$title ${page + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            if (photoUris.size > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VisitorPhotoPageIndicator(
                        currentPage = pagerState.currentPage,
                        pageCount = photoUris.size,
                        activeColor = textColor,
                        inactiveColor = textColor.copy(alpha = 0.38f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${pagerState.currentPage + 1} / ${photoUris.size}",
                        color = textColor.copy(alpha = 0.78f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VisitorPhotoFullScreenViewer(
    title: String,
    photoUris: List<String>,
    initialPage: Int,
    onDismiss: () -> Unit
) {
    if (photoUris.isEmpty()) return

    val startPage = initialPage.coerceIn(0, photoUris.lastIndex)
    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { photoUris.size }
    )
    var zoomScale by remember { mutableStateOf(1f) }
    var zoomOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(pagerState.currentPage) {
        zoomScale = 1f
        zoomOffset = Offset.Zero
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler(enabled = true) {
            onDismiss()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 84.dp),
                userScrollEnabled = zoomScale <= 1.01f
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(page) {
                            awaitEachGesture {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val pressedCount = event.changes.count { it.pressed }
                                    if (pressedCount == 0) break

                                    val isMultiTouch = pressedCount > 1
                                    if (isMultiTouch || zoomScale > 1f) {
                                        val pressedChanges = event.changes.filter { it.pressed }
                                        val currentCentroid = pressedChanges
                                            .map { it.position }
                                            .averageOffset()
                                        val previousCentroid = pressedChanges
                                            .map { it.previousPosition }
                                            .averageOffset()
                                        val zoomChange = if (isMultiTouch) {
                                            pressedChanges.pointerZoomChange(currentCentroid, previousCentroid)
                                        } else {
                                            1f
                                        }
                                        val panChange = currentCentroid - previousCentroid
                                        val updatedScale = (zoomScale * zoomChange).coerceIn(1f, 5f)

                                        zoomScale = updatedScale
                                        zoomOffset = if (updatedScale > 1f) {
                                            zoomOffset + panChange
                                        } else {
                                            Offset.Zero
                                        }

                                        event.changes.forEach { change ->
                                            if (change.positionChanged()) {
                                                change.consume()
                                            }
                                        }
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = photoUris[page],
                        contentDescription = "$title ${page + 1}",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = zoomScale
                                scaleY = zoomScale
                                translationX = zoomOffset.x
                                translationY = zoomOffset.y
                            },
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.68f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.16f), CircleShape)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close photo viewer", tint = Color.White)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (photoUris.size > 1) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${photoUris.size}",
                            color = Color.White.copy(alpha = 0.78f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.size(48.dp))
            }

            if (photoUris.size > 1) {
                VisitorPhotoPageIndicator(
                    currentPage = pagerState.currentPage,
                    pageCount = photoUris.size,
                    activeColor = Color.White,
                    inactiveColor = Color.White.copy(alpha = 0.38f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 28.dp)
                )
            }
        }
    }
}

@Composable
private fun VisitorPhotoPageIndicator(
    currentPage: Int,
    pageCount: Int,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier
) {
    if (pageCount <= 1) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { page ->
            Box(
                modifier = Modifier
                    .size(if (page == currentPage) 8.dp else 6.dp)
                    .background(
                        color = if (page == currentPage) activeColor else inactiveColor,
                        shape = CircleShape
                    )
            )
        }
    }
}

private fun List<Offset>.averageOffset(): Offset {
    if (isEmpty()) return Offset.Zero

    var x = 0f
    var y = 0f
    forEach { offset ->
        x += offset.x
        y += offset.y
    }

    return Offset(x / size, y / size)
}

private fun List<PointerInputChange>.pointerZoomChange(
    currentCentroid: Offset,
    previousCentroid: Offset
): Float {
    if (size < 2) return 1f

    var currentDistance = 0f
    var previousDistance = 0f
    forEach { change ->
        currentDistance += (change.position - currentCentroid).getDistance()
        previousDistance += (change.previousPosition - previousCentroid).getDistance()
    }

    return if (previousDistance > 0f) currentDistance / previousDistance else 1f
}

private fun addPickedPhotoUris(
    target: SnapshotStateList<String>,
    uris: List<Uri>
) {
    uris
        .map(Uri::toString)
        .forEach { uriText ->
            if (uriText !in target) {
                target.add(uriText)
            }
        }
}

private suspend fun uploadCafeCrowdPhotos(
    context: Context,
    cafeId: String,
    photoKind: String,
    photoUris: List<String>
): List<String> {
    if (photoUris.isEmpty()) return emptyList()

    val bucket = supabase.storage.from(CAFE_CROWD_PHOTO_BUCKET)
    val cafePath = cafeId.toStoragePathSegment()
    val kindPath = photoKind.toStoragePathSegment()
    val uploadedUrls = mutableListOf<String>()

    photoUris
        .mapNotNull { uriText -> uriText.trim().takeIf { it.isNotBlank() } }
        .distinct()
        .forEach { uriText ->
            if (uriText.startsWith("http://") || uriText.startsWith("https://")) {
                uploadedUrls.add(uriText)
                return@forEach
            }

            val uri = Uri.parse(uriText)
            val bytes = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.readBytes()
                }
            } ?: return@forEach

            val storagePath = "$cafePath/$kindPath/${UUID.randomUUID()}.jpg"
            bucket.upload(
                path = storagePath,
                data = bytes
            )
            uploadedUrls.add(bucket.publicUrl(storagePath))
        }

    return uploadedUrls
}

private fun String.toStoragePathSegment(): String {
    return replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "unknown" }
}

private fun Throwable.toCrowdSuggestionMessage(): String = toCrowdBackendMessage("submit this suggestion")

private fun Throwable.toCrowdLoadMessage(): String = toCrowdBackendMessage("load community suggestions")

private fun Throwable.toCrowdBackendMessage(operation: String): String {
    val details = buildList {
        message?.takeIf { it.isNotBlank() }?.let { add(it) }
        when (this@toCrowdBackendMessage) {
            is PostgrestRestException -> {
                code?.takeIf { it.isNotBlank() }?.let { add("Code: $it") }
                hint?.takeIf { it.isNotBlank() }?.let { add("Hint: $it") }
                details?.toString()?.takeIf { it.isNotBlank() && it != "null" }?.let { add("Details: $it") }
            }
            is RestException -> {
                description?.takeIf { it.isNotBlank() }?.let { add(it) }
            }
        }
    }.joinToString(" ")

    return when {
        this is RestException && (statusCode == 401 || statusCode == 403) ->
            "Supabase rejected this request. Check that you are signed in and that the cafe_crowd_attributes RLS policies were created. ${details.toDebugSuffix()}"
        this is RestException && statusCode == 404 ->
            "Supabase could not find the cafe_crowd_attributes table. Run the SQL setup file in Supabase. ${details.toDebugSuffix()}"
        this is HttpRequestException ->
            "The app could not reach Supabase. Check your connection and Supabase project URL. ${details.toDebugSuffix()}"
        details.contains("cafe_crowd_attributes", ignoreCase = true) ||
            details.contains("schema cache", ignoreCase = true) ||
            details.contains("column", ignoreCase = true) ->
            "Cafe attributes are not set up correctly in Supabase. Re-run the SQL setup file, then try again. ${details.toDebugSuffix()}"
        details.contains(CAFE_CROWD_PHOTO_BUCKET, ignoreCase = true) ->
            "Cafe photo uploads are not set up in Supabase yet. Create the cafe-crowd-photos bucket, then try again. ${details.toDebugSuffix()}"
        details.contains("Sign in", ignoreCase = true) ->
            "Sign in before suggesting changes."
        else ->
            "Could not $operation yet. Supabase said: ${details.ifBlank { this::class.simpleName ?: "Unknown error" }}"
    }
}

private fun String.toDebugSuffix(): String {
    return if (isBlank()) "" else "Supabase said: $this"
}

@Composable
private fun SuggestionPhotoPickerField(
    iconRes: Int,
    title: String,
    selectedPhotoUris: SnapshotStateList<String>,
    onPickPhotos: () -> Unit,
    iconSize: Dp = 20.dp
) {
    SuggestionBubbleContainer(
        iconRes = iconRes,
        iconName = title,
        label = title,
        iconSize = iconSize
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onPickPhotos,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = StudySessionPopupFieldColor,
                    contentColor = StudySessionPopupFieldTextColor
                ),
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedPhotoUris.isEmpty()) "Select photos" else "Add more photos")
            }
            if (selectedPhotoUris.isEmpty()) {
                Text(
                    text = "No photos selected yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudySessionPopupFieldTextColor.copy(alpha = 0.64f)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    selectedPhotoUris.forEachIndexed { index, uri ->
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.38f))
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "$title selection ${index + 1}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { selectedPhotoUris.removeAt(index) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Remove photo",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CrowdAttributeSuggestionForm(
    cafe: Cafe,
    attributes: CafeCrowdAttributes,
    onSubmitSuggestion: suspend (CrowdAttributeSuggestion) -> Unit,
    onCancel: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = 28.dp)
) {
    val scope = rememberCoroutineScope()
    var outletAvailability by remember(cafe.id, attributes) { mutableStateOf(attributes.outletAvailability) }
    var wifiName by remember(cafe.id, attributes) { mutableStateOf(attributes.wifiName.orEmpty()) }
    var wifiSpeed by remember(cafe.id, attributes) { mutableStateOf(attributes.wifiSpeed) }
    var wifiPassword by remember(cafe.id) { mutableStateOf("") }
    var bathroomAvailability by remember(cafe.id, attributes) { mutableStateOf(attributes.bathroomAvailability) }
    var bathroomCode by remember(cafe.id) { mutableStateOf("") }
    var seatingAvailability by remember(cafe.id, attributes) { mutableStateOf(attributes.seatingAvailability) }
    var seatingSpace by remember(cafe.id, attributes) { mutableStateOf(attributes.seatingSpace) }
    var seatingComfort by remember(cafe.id, attributes) { mutableStateOf(attributes.seatingComfort) }
    var crowdLevel by remember(cafe.id, attributes) { mutableStateOf(attributes.crowdLevel) }
    var noiseLevel by remember(cafe.id, attributes) { mutableStateOf(attributes.noiseLevel) }
    var petFriendly by remember(cafe.id, attributes) { mutableStateOf(attributes.petFriendly) }
    var cleanlinessRating by remember(cafe.id, attributes) { mutableStateOf(attributes.cleanlinessRating) }
    var selectedVibeTags by remember(cafe.id, attributes) { mutableStateOf(attributes.vibeTags) }
    var isSubmitting by remember(cafe.id) { mutableStateOf(false) }
    var submitError by remember(cafe.id) { mutableStateOf<String?>(null) }
    val selectedSeatingPhotoUris = remember(cafe.id) { mutableStateListOf<String>() }
    val selectedMenuPhotoUris = remember(cafe.id) { mutableStateListOf<String>() }
    val seatingPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        addPickedPhotoUris(selectedSeatingPhotoUris, uris)
    }
    val menuPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        addPickedPhotoUris(selectedMenuPhotoUris, uris)
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
            item {
                SuggestionDropdown(
                    iconRes = R.drawable.outlets,
                    label = "Outlets",
                    selected = outletAvailability,
                    options = OutletAvailability.entries,
                    optionLabel = { it.label }
                ) {
                    outletAvailability = it
                }
            }
            item {
                SuggestionTextField(
                    iconRes = R.drawable.wifi,
                    label = "WiFi",
                    value = wifiName,
                    onValueChange = { wifiName = it },
                    placeholder = "Network name"
                )
            }
            item {
                SuggestionDropdown(
                    iconRes = R.drawable.wifi_speed,
                    label = "WiFi Speed",
                    selected = wifiSpeed,
                    options = WifiSpeed.entries,
                    optionLabel = { it.label }
                ) {
                    wifiSpeed = it
                }
            }
            item {
                SuggestionTextField(
                    iconRes = R.drawable.key,
                    label = "WiFi key",
                    value = wifiPassword,
                    onValueChange = { wifiPassword = it },
                    placeholder = "WiFi Password",
                    visualTransformation = PasswordVisualTransformation()
                )
            }
            item {
                SuggestionDropdown(
                    iconRes = R.drawable.bathroom,
                    label = "Bathroom",
                    selected = bathroomAvailability,
                    options = BathroomAvailability.entries,
                    optionLabel = { it.label }
                ) {
                    bathroomAvailability = it
                }
            }
            item {
                SuggestionTextField(
                    iconRes = R.drawable.key,
                    label = "Bathroom key",
                    value = bathroomCode,
                    onValueChange = { bathroomCode = it },
                    placeholder = "Bathroom code",
                    visualTransformation = PasswordVisualTransformation()
                )
            }
            item {
                SuggestionDropdown(
                    iconRes = R.drawable.seating_availability,
                    label = "Seating Availability",
                    selected = seatingAvailability,
                    options = SeatingAvailability.entries,
                    optionLabel = { it.label },
                    iconSize = 26.dp
                ) {
                    seatingAvailability = it
                }
            }
            item {
                SuggestionDropdown(
                    iconRes = R.drawable.seating_space,
                    label = "Seating space",
                    selected = seatingSpace,
                    options = SeatingSpace.entries,
                    optionLabel = { it.label },
                    iconSize = 26.dp
                ) {
                    seatingSpace = it
                }
            }
            item {
                SuggestionDropdown(
                    iconRes = R.drawable.seating_comfort,
                    label = "Seating Comfort",
                    selected = seatingComfort,
                    options = SeatingComfort.entries,
                    optionLabel = { it.label }
                ) {
                    seatingComfort = it
                }
            }
            item {
                SuggestionDropdown(
                    iconRes = R.drawable.crowd_level,
                    label = "Crowd Level",
                    selected = crowdLevel,
                    options = CrowdLevel.entries,
                    optionLabel = { it.label }
                ) {
                    crowdLevel = it
                }
            }
            item {
                SuggestionDropdown(
                    iconRes = R.drawable.noise_level,
                    label = "Noise Level",
                    selected = noiseLevel,
                    options = NoiseLevel.entries,
                    optionLabel = { it.label }
                ) {
                    noiseLevel = it
                }
            }
            item {
                SuggestionDropdown(
                    iconRes = R.drawable.pet_friendly,
                    label = "Pet friendly",
                    selected = petFriendly,
                    options = PetFriendly.entries,
                    optionLabel = { it.label }
                ) {
                    petFriendly = it
                }
            }
            item {
                SuggestionDropdown(
                    iconRes = R.drawable.cleanliness,
                    label = "Cleanliness",
                    selected = cleanlinessRating,
                    options = CleanlinessRating.entries,
                    optionLabel = { it.label }
                ) {
                    cleanlinessRating = it
                }
            }
            item {
                VibeTagSelector(
                    selectedTags = selectedVibeTags,
                    onSelectedTagsChanged = { selectedVibeTags = it }
                )
            }
            item {
                SuggestionPhotoPickerField(
                    iconRes = R.drawable.seating_availability,
                    title = "Seating photos",
                    selectedPhotoUris = selectedSeatingPhotoUris,
                    onPickPhotos = { seatingPhotoPicker.launch("image/*") },
                    iconSize = 26.dp
                )
            }
            item {
                SuggestionPhotoPickerField(
                    iconRes = R.drawable.edit,
                    title = "Menu photos",
                    selectedPhotoUris = selectedMenuPhotoUris,
                    onPickPhotos = { menuPhotoPicker.launch("image/*") }
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onCancel,
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = StudySessionPopupTextColor,
                            disabledContentColor = StudySessionPopupTextColor.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                        val suggestion = CrowdAttributeSuggestion(
                                outletAvailability = outletAvailability,
                                wifiName = wifiName,
                                wifiSpeed = wifiSpeed,
                                wifiPassword = wifiPassword.toTypedSecretSuggestion(),
                                bathroomAvailability = bathroomAvailability,
                                bathroomCode = bathroomCode.toTypedSecretSuggestion(),
                                seatingAvailability = seatingAvailability,
                                seatingSpace = seatingSpace,
                                seatingComfort = seatingComfort,
                                crowdLevel = crowdLevel,
                                noiseLevel = noiseLevel,
                                vibeTags = selectedVibeTags,
                                petFriendly = petFriendly,
                                cleanlinessRating = cleanlinessRating,
                                seatingPhotoUris = selectedSeatingPhotoUris.toList(),
                                menuPhotoUris = selectedMenuPhotoUris.toList()
                            )
                        scope.launch {
                            isSubmitting = true
                            submitError = null
                            try {
                                onSubmitSuggestion(suggestion)
                                onSubmit()
                            } catch (error: Exception) {
                                submitError = error.toCrowdSuggestionMessage()
                                error.printStackTrace()
                            } finally {
                                isSubmitting = false
                            }
                        }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudySessionPopupFieldColor,
                            contentColor = StudySessionPopupFieldTextColor,
                            disabledContainerColor = StudySessionPopupFieldColor.copy(alpha = 0.42f),
                            disabledContentColor = StudySessionPopupFieldTextColor.copy(alpha = 0.5f)
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = LocalContentColor.current
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submitting...")
                        } else {
                            Text("Submit")
                        }
                    }
                }
            }
            submitError?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
    }
}

@Composable
private fun <T> SuggestionDropdown(
    iconRes: Int,
    label: String,
    selected: T,
    options: Iterable<T>,
    optionLabel: (T) -> String,
    iconSize: Dp = 20.dp,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        SuggestionBubbleContainer(
            iconRes = iconRes,
            iconName = label,
            label = label,
            iconSize = iconSize,
            modifier = Modifier.clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = optionLabel(selected),
                    modifier = Modifier.weight(1f),
                    color = StudySessionPopupFieldTextColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = StudySessionPopupFieldTextColor
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SuggestionTextField(
    iconRes: Int,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    iconSize: Dp = 20.dp,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    SuggestionBubbleContainer(
        iconRes = iconRes,
        iconName = label,
        label = label,
        iconSize = iconSize
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = DetailInfoBubbleTextColor) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = visualTransformation,
            colors = studySessionTextFieldColors()
        )
    }
}

private fun String.toTypedSecretSuggestion(): ProtectedCrowdSecret? {
    return trim()
        .takeIf { it.isNotBlank() }
        ?.let { ProtectedCrowdSecret(value = it, knownToExist = false) }
}

@Composable
private fun SuggestionBubbleContainer(
    iconRes: Int,
    iconName: String,
    label: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = StudySessionPopupFieldColor,
        contentColor = StudySessionPopupFieldTextColor,
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.72f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(CrowdActionBubbleSize)
                    .background(Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CrowdBubbleIcon(iconRes = iconRes, contentDescription = iconName, size = iconSize)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = label,
                    color = StudySessionPopupFieldTextColor.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                content()
            }
        }
    }
}

@Composable
private fun VibeTagSelector(
    selectedTags: Set<VibeTag>,
    onSelectedTagsChanged: (Set<VibeTag>) -> Unit
) {
    SuggestionBubbleContainer(
        iconRes = R.drawable.vibe_and_atmosphere,
        iconName = "Vibe and Atmosphere",
        label = "Vibe and Atmosphere"
    ) {
        val chipColors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White.copy(alpha = 0.18f),
            labelColor = StudySessionPopupFieldTextColor,
            selectedContainerColor = SelectedCafeSurfaceColor,
            selectedLabelColor = StudySessionPopupTextColor
        )

        VibeTag.entries.chunked(2).forEach { rowTags ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                rowTags.forEach { tag ->
                    FilterChip(
                        selected = tag in selectedTags,
                        onClick = {
                            onSelectedTagsChanged(
                                if (tag in selectedTags) selectedTags - tag else selectedTags + tag
                            )
                        },
                        label = {
                            Text(
                                text = tag.label,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = chipColors,
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = tag in selectedTags,
                            borderColor = Color.Black.copy(alpha = 0.38f),
                            selectedBorderColor = Color.Black.copy(alpha = 0.76f),
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.dp
                        )
                    )
                }
                if (rowTags.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun WifiSpeed.toAvailabilityFlag(): Boolean? {
    return when (this) {
        WifiSpeed.FAST,
        WifiSpeed.DECENT,
        WifiSpeed.SLOW -> true
        WifiSpeed.NONE -> false
        WifiSpeed.UNKNOWN -> null
    }
}

private fun BathroomAvailability.toAvailabilityFlag(): Boolean? {
    return when (this) {
        BathroomAvailability.AVAILABLE -> true
        BathroomAvailability.NONE -> false
        BathroomAvailability.UNKNOWN -> null
    }
}

private fun displayTextForSecretState(state: ProtectedSecretDisplayState): String {
    return when (state) {
        ProtectedSecretDisplayState.Censored -> "Hidden until nearby"
        is ProtectedSecretDisplayState.Revealed -> state.value
        ProtectedSecretDisplayState.AskStaff -> "Ask staff"
        ProtectedSecretDisplayState.Unavailable -> "Not available"
        ProtectedSecretDisplayState.Unknown -> "Unknown"
    }
}

private fun copyTextToClipboard(context: Context, label: String, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
}

private fun copySecretToClipboard(context: Context, label: String, value: String) {
    copyTextToClipboard(context, label, value)
}

@Composable
fun RecentCafeRow(
    cafe: Cafe,
    onClick: () -> Unit
) {
    val rowSurfaceColor = CoffeeSurfaceLight
    val primaryTextColor = CafeDark
    val secondaryTextColor = CoffeeDark.copy(alpha = 0.78f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(8.dp),
            color = rowSurfaceColor
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
            Text(cafe.name, fontWeight = FontWeight.SemiBold, color = primaryTextColor)
            Text(
                cafe.address,
                style = MaterialTheme.typography.bodySmall,
                color = secondaryTextColor,
                maxLines = 1
            )
        }

        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = "Open",
            tint = primaryTextColor
        )
    }
}

@Composable
fun WriteReviewScreen(navController: NavHostController, cafeId: String) {
    var text by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isPosting by remember { mutableStateOf(false) }
    var authorDisplayName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val user = supabase.auth.currentUserOrNull() ?: return@LaunchedEffect
        runCatching {
            val profile = supabase.from("profiles")
                .select { filter { eq("id", user.id) } }
                .decodeSingle<Profile>()
            authorDisplayName = profile.first_name.trim().ifBlank { null }
        }.onFailure {
            authorDisplayName = user.email
        }
    }

    Scaffold(bottomBar = { BottomNavBar(navController) }, containerColor = Color(0xFFC79A87)) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                text, { text = it },
                placeholder = { Text("Write a review...") },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { navController.popBackStack() }) { Text("Cancel") }
                Button(
                    onClick = {
                        isPosting = true
                        scope.launch {
                            runCatching {
                                ReviewRepository.addReview(
                                    cafeId = cafeId,
                                    body = text,
                                    displayName = authorDisplayName
                                )
                            }
                            isPosting = false
                            navController.popBackStack()
                        }
                    },
                    enabled = text.isNotBlank() && !isPosting
                ) {
                    if (isPosting) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text("Post")
                }
            }
        }
    }
}

@Composable
private fun PlaceSaved(
    savedCafes: List<Cafe>,
    showAllSaved: Boolean,
    onToggleShowAllSaved: () -> Unit,
    onSavedCafeSelected: (Cafe) -> Unit,
    onStudySessionSelected: (StudySession) -> Unit
) {
    val outlineColor = Color(0xFFE6E6E6)
    val studySessions = StudySessionRepository.sessions()
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
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = CoffeeSurfaceLight,
                            contentColor = CafeDark
                        ),
                        border = BorderStroke(1.dp, outlineColor)
                    ) {
                        Text(
                            text = "No saved places yet. Swipe right on a cafe to add it here.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CoffeeDark.copy(alpha = 0.78f)
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
            if (studySessions.isEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SavedTile(Modifier.weight(1f))
                    SavedTile(Modifier.weight(1f))
                    SavedTile(Modifier.weight(1f), true)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    studySessions.forEach { session ->
                        StudySessionSavedCard(
                            session = session,
                            onOpen = { onStudySessionSelected(session) },
                            onDelete = { StudySessionRepository.remove(session.id) }
                        )
                    }
                }
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
private fun StudySessionSavedCard(
    session: StudySession,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val firstPhotoUri = session.photoUris.firstOrNull()

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE6E6E6)),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color(0xFFD9EEFF),
            contentColor = Color.Black
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedCard(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.24f)),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = StudySessionPopupFieldColor,
                    contentColor = StudySessionPopupFieldTextColor
                )
            ) {
                if (firstPhotoUri != null) {
                    AsyncImage(
                        model = firstPhotoUri,
                        contentDescription = session.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.study),
                            contentDescription = null,
                            modifier = Modifier.size(34.dp),
                            tint = Color.Unspecified
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = session.cafeName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${session.className} - ${session.dateText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.74f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Time: ${session.timeRangeText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.68f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = session.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.64f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete study session",
                    tint = Color.Black.copy(alpha = 0.74f)
                )
            }
        }
    }
}

private fun StudySession.toCafePlaceholder(): Cafe {
    return Cafe(
        id = cafeId,
        name = cafeName,
        address = cafeAddress.takeUnless(::isCafeAddressUnavailable) ?: ADDRESS_UNAVAILABLE_TEXT,
        phone = PHONE_UNAVAILABLE_TEXT,
        status = NO_RATINGS_TEXT,
        hours = linkedMapOf("Hours" to HOURS_UNAVAILABLE_TEXT),
        features = listOf("Coffee house"),
        ambience = listOf("Study session")
    )
}

@Composable
private fun StudySessionDetailOverlay(
    session: StudySession,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onOpenCafeDetails: () -> Unit,
    isOpeningCafeDetails: Boolean = false,
    backHandlerEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    var isVisible by remember { mutableStateOf(false) }
    var isClosing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    fun closeDetails() {
        if (isClosing) return
        isClosing = true
        isVisible = false
        scope.launch {
            delay(140)
            onDismiss()
        }
    }

    BackHandler(enabled = backHandlerEnabled) {
        closeDetails()
    }

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "studySessionDetailAlpha"
    )
    val overlayScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.92f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "studySessionDetailScale"
    )

    Box(modifier = modifier.zIndex(10f)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.24f * overlayAlpha))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    closeDetails()
                }
        )

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(20.dp)
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .heightIn(max = 620.dp)
                .graphicsLayer {
                    alpha = overlayAlpha
                    scaleX = overlayScale
                    scaleY = overlayScale
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = StudySessionPopupSurfaceColor,
                contentColor = StudySessionPopupTextColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            border = BorderStroke(2.dp, Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = Color(0xFFD5F1FF)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.study),
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp),
                            tint = Color.Unspecified
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = session.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = StudySessionPopupTextColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = session.cafeName,
                            style = MaterialTheme.typography.bodySmall,
                            color = StudySessionPopupSecondaryTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { closeDetails() }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close study session",
                            tint = StudySessionPopupTextColor
                        )
                    }
                }

                StudySessionSubmittedPhotos(photoUris = session.photoUris)

                StudySessionReadonlyField(label = "Class", value = session.className)
                StudySessionReadonlyField(label = "Date", value = session.dateText)
                StudySessionReadonlyField(label = "Time", value = session.timeRangeText)
                StudySessionCafeDetailsButton(
                    cafeName = session.cafeName,
                    onClick = onOpenCafeDetails,
                    isLoading = isOpeningCafeDetails
                )
                StudySessionReadonlyField(label = "Summary", value = session.summary)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = StudySessionPopupTextColor
                        )
                    ) {
                        Text("Delete")
                    }
                    Button(
                        onClick = { closeDetails() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudySessionPopupFieldColor,
                            contentColor = StudySessionPopupFieldTextColor
                        )
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
private fun StudySessionReadonlyField(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = StudySessionPopupSecondaryTextColor
        )
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.Black),
            colors = CardDefaults.outlinedCardColors(
                containerColor = StudySessionPopupFieldColor,
                contentColor = StudySessionPopupFieldTextColor
            )
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = StudySessionPopupFieldTextColor
            )
        }
    }
}

@Composable
private fun StudySessionCafeDetailsButton(
    cafeName: String,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Cafe",
            style = MaterialTheme.typography.labelMedium,
            color = StudySessionPopupSecondaryTextColor
        )
        Button(
            onClick = onClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = StudySessionCafeButtonColor,
                contentColor = Color.White,
                disabledContainerColor = StudySessionCafeButtonColor.copy(alpha = 0.72f),
                disabledContentColor = Color.White.copy(alpha = 0.78f)
            ),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.address),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = cafeName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StudySessionSubmittedPhotos(photoUris: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Submitted photos",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = StudySessionPopupTextColor
        )

        if (photoUris.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(8.dp),
                color = StudySessionPopupFieldColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.study),
                        contentDescription = null,
                        modifier = Modifier.size(46.dp),
                        tint = Color.Unspecified
                    )
                }
            }
        } else {
            AsyncImage(
                model = photoUris.first(),
                contentDescription = "Submitted study photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )

            if (photoUris.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    photoUris.forEach { uri ->
                        OutlinedCard(
                            modifier = Modifier.size(76.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.18f))
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Submitted study photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
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
    val cardTextColor = Color.White
    val cardSecondaryTextColor = Color.White.copy(alpha = 0.78f)
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
        colors = CardDefaults.cardColors(
            containerColor = SelectedCafeSurfaceColor,
            contentColor = cardTextColor
        ),
        border = BorderStroke(2.dp, Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(340.dp).clickable {
                onOpen(cardBounds ?: Rect.Zero, cafe.primaryImageModel())
            }) {
                AsyncImage(
                    model = cafe.primaryImageModel(),
                    contentDescription = cafe.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                val isBookmarked = BookmarkRepository.isBookmarked(cafe.id)
                IconButton(onClick = { BookmarkRepository.toggle(cafe.id) }, modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).background(Color.Black.copy(alpha = 0.35f), CircleShape)) {
                    Icon(imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder, contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark", tint = Color.White)
                }
                Box(modifier = Modifier.fillMaxWidth().height(100.dp).align(Alignment.BottomCenter).background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)))))
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Text(
                        text = cafe.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val rating = cafe.rating
                val reviewCount = cafe.userRatingCount ?: 0
                if (rating != null && reviewCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RatingStars(rating)
                        Spacer(Modifier.width(8.dp))
                        Text(String.format(Locale.US, "%.1f (%d Reviews)", rating, reviewCount), color = cardSecondaryTextColor, style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    Text("No ratings yet", color = cardSecondaryTextColor, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompactDetailInfoBubble(
                        iconRes = R.drawable.distance,
                        iconName = "Distance",
                        value = formatDistanceAway(cafe.distanceMeters) ?: "Distance unavailable",
                        modifier = Modifier.weight(1f)
                    )
                    CompactDetailInfoBubble(
                        iconRes = R.drawable.address,
                        iconName = "Address",
                        value = cafe.address,
                        modifier = Modifier.weight(1f)
                    )
                    CompactDetailInfoBubble(
                        iconRes = R.drawable.phone,
                        iconName = "Phone",
                        value = cafe.phone,
                        modifier = Modifier.weight(1f),
                        copyLabel = "${cafe.name} phone number"
                    )
                }
                CardTodayHoursBubble(
                    hours = cafe.hours,
                    modifier = Modifier.fillMaxWidth()
                )
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
    val crowdAttributes = CrowdAttributeRepository.attributesFor(cafe.id)
    val crowdAttributeBackendState = rememberCrowdAttributeBackendState(cafe.id)
    val pageCount = cafe.photoPageCount()
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val detailRevealAlpha = transitionRevealAlpha(transitionProgress)
    val detailSurfaceAlpha = transitionDetailSurfaceAlpha(transitionProgress)
    val pagerAlpha = ((transitionProgress - 0.22f) / 0.18f).coerceIn(0f, 1f)
    val overlayAlpha = sCurve(((transitionProgress - 0.04f) / 0.34f).coerceIn(0f, 1f))
    val detailTextColor = Color.White
    val detailSecondaryTextColor = Color.White.copy(alpha = 0.78f)
    var showStudyComposer by remember(cafe.id) { mutableStateOf(false) }
    var showSuggestionOverlay by remember(cafe.id) { mutableStateOf(false) }

    LaunchedEffect(cafe.id) {
        runCatching { ReviewRepository.loadReviews(cafe.id) }
    }

    BackHandler(enabled = showStudyComposer) {
        showStudyComposer = false
    }

    Card(
        modifier = modifier.graphicsLayer { alpha = overlayAlpha },
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = SelectedCafeSurfaceColor.copy(alpha = detailSurfaceAlpha),
            contentColor = detailTextColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(2.dp, Color.Black)
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
                        val rating = cafe.rating
                        val reviewCount = cafe.userRatingCount ?: 0
                        if (rating != null && reviewCount > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RatingStars(rating)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = String.format(Locale.US, "%.1f (%d Reviews)", rating, reviewCount),
                                    color = detailSecondaryTextColor,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            Text("No ratings yet", color = detailSecondaryTextColor, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            cafe.distanceMeters
                                ?.let { distanceMeters -> formatDistanceAway(distanceMeters) }
                                ?.let { distanceText ->
                                    CompactDetailInfoBubble(
                                        iconRes = R.drawable.distance,
                                        iconName = "Distance",
                                        value = distanceText,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            CompactDetailInfoBubble(
                                iconRes = R.drawable.address,
                                iconName = "Address",
                                value = cafe.address,
                                modifier = Modifier.weight(1f)
                            )
                            CompactDetailInfoBubble(
                                iconRes = R.drawable.phone,
                                iconName = "Phone",
                                value = cafe.phone,
                                modifier = Modifier.weight(1f),
                                copyLabel = "${cafe.name} phone number"
                            )
                        }
                        HoursDropdown(cafe.hours)
                        StudySessionActionRow(
                            cafe = cafe,
                            onCreateStudySession = { showStudyComposer = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    CafeCrowdAttributesPanel(
                        cafe = cafe,
                        attributes = crowdAttributes,
                        backendState = crowdAttributeBackendState,
                        onSuggestChanges = { showSuggestionOverlay = true },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), color = detailTextColor)
                        TextButton(
                            onClick = { navController.navigate(Screen.WriteReview.createRoute(cafe.id)) },
                            colors = ButtonDefaults.textButtonColors(contentColor = detailTextColor)
                        ) {
                            Text("Write review")
                        }
                    }
                }

                if (reviews.isEmpty()) {
                    item {
                        Text(
                            text = "No reviews yet.",
                            color = detailSecondaryTextColor,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                } else {
                    items(reviews) { review ->
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            border = BorderStroke(1.dp, Color.Black),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = SelectedCafeSurfaceColor,
                                contentColor = detailTextColor
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                review.display_name?.let { name ->
                                    Text(name, fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.labelSmall, color = detailTextColor)
                                }
                                Text(review.body, color = detailTextColor)
                            }
                        }
                    }
                }
            }

            if (showStudyComposer) {
                StudySessionComposerOverlay(
                    cafe = cafe,
                    onDismiss = { showStudyComposer = false },
                    modifier = Modifier.matchParentSize()
                )
            }

            if (showSuggestionOverlay) {
                CrowdAttributeSuggestionOverlay(
                    cafe = cafe,
                    attributes = CrowdAttributeRepository.attributesFor(cafe.id),
                    onDismiss = { showSuggestionOverlay = false },
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}

@Composable
private fun StudySessionActionRow(
    cafe: Cafe,
    onCreateStudySession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier.heightIn(min = CrowdActionBubbleSize),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = { openDirectionsInGoogleMaps(context, cafe) },
            enabled = cafe.directionsDestination() != null,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(Icons.Filled.Directions, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Directions")
        }

        StudyBubbleButton(
            onClick = onCreateStudySession,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun StudyBubbleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(CrowdActionBubbleSize)
            .clickable { onClick() },
        shape = CircleShape,
        color = Color(0xFFD5F1FF),
        contentColor = Color.Black,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = R.drawable.study),
                contentDescription = "Create study session",
                modifier = Modifier.size(21.dp),
                tint = Color.Unspecified
            )
        }
    }
}

@Composable
private fun studySessionTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = StudySessionPopupFieldTextColor,
    unfocusedTextColor = StudySessionPopupFieldTextColor,
    focusedContainerColor = StudySessionPopupFieldColor,
    unfocusedContainerColor = StudySessionPopupFieldColor,
    focusedLabelColor = StudySessionPopupFieldTextColor,
    unfocusedLabelColor = StudySessionPopupFieldTextColor.copy(alpha = 0.72f),
    focusedBorderColor = Color.Black,
    unfocusedBorderColor = Color.Black.copy(alpha = 0.72f),
    cursorColor = StudySessionPopupFieldTextColor
)

@Composable
private fun StudySessionPickerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = StudySessionPopupFieldColor,
            onPrimary = StudySessionPopupFieldTextColor,
            primaryContainer = StudySessionPopupFieldColor,
            onPrimaryContainer = StudySessionPopupFieldTextColor,
            secondary = Color(0xFFD5F1FF),
            onSecondary = Color.Black,
            secondaryContainer = Color(0xFFD5F1FF),
            onSecondaryContainer = Color.Black,
            surface = StudySessionPopupSurfaceColor,
            onSurface = StudySessionPopupTextColor,
            surfaceVariant = StudySessionPopupFieldColor.copy(alpha = 0.72f),
            onSurfaceVariant = StudySessionPopupTextColor
        ),
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}

private enum class StudySessionTimeField {
    Start,
    End
}

@Composable
private fun StudySessionCompactFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 11.sp,
            lineHeight = 11.sp
        ),
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip
    )
}

@Composable
private fun StudySessionPickerField(
    value: String,
    label: String,
    placeholder: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: TextFieldColors = studySessionTextFieldColors()
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { StudySessionCompactFieldLabel(label) },
            placeholder = {
                Text(
                    text = placeholder,
                    color = StudySessionPopupFieldTextColor.copy(alpha = 0.56f),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = StudySessionPopupFieldTextColor
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = colors
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onClick() }
        )
    }
}

@Composable
private fun StudySessionPickerDialogShell(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean = true,
    confirmLabel: String = "Apply",
    maxWidth: Dp = 420.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val maxDialogHeight = maxOf(320.dp, LocalConfiguration.current.screenHeightDp.dp - 48.dp)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .fillMaxWidth()
                .widthIn(max = maxWidth)
                .heightIn(max = maxDialogHeight),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = StudySessionPopupSurfaceColor,
                contentColor = StudySessionPopupTextColor
            ),
            border = BorderStroke(2.dp, Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = StudySessionPopupTextColor
                )
                content()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = StudySessionPopupTextColor
                        )
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = confirmEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudySessionPopupFieldColor,
                            contentColor = StudySessionPopupFieldTextColor,
                            disabledContainerColor = StudySessionPopupFieldColor.copy(alpha = 0.42f),
                            disabledContentColor = StudySessionPopupFieldTextColor.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(confirmLabel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudySessionDatePickerDialog(
    initialDateUtcMillis: Long?,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateUtcMillis ?: System.currentTimeMillis()
    )
    val calendarScale = 0.86f
    val calendarBaseWidth = 360.dp
    val calendarBaseHeight = 500.dp

    StudySessionPickerDialogShell(
        title = "Pick a date",
        onDismiss = onDismiss,
        onConfirm = {
            pickerState.selectedDateMillis?.let(onDateSelected)
        },
        confirmEnabled = pickerState.selectedDateMillis != null,
        confirmLabel = "Select",
        maxWidth = 420.dp
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = StudySessionPopupFieldColor.copy(alpha = 0.26f)
        ) {
            StudySessionPickerTheme {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(calendarBaseHeight * calendarScale),
                    contentAlignment = Alignment.TopCenter
                ) {
                    DatePicker(
                        state = pickerState,
                        modifier = Modifier
                            .requiredWidth(calendarBaseWidth)
                            .requiredHeight(calendarBaseHeight)
                            .graphicsLayer {
                                scaleX = calendarScale
                                scaleY = calendarScale
                                transformOrigin = TransformOrigin(0.5f, 0f)
                            },
                        colors = DatePickerDefaults.colors(
                            containerColor = Color.Transparent
                        ),
                        showModeToggle = false,
                        title = null,
                        headline = null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudySessionTimePickerDialog(
    title: String,
    initialHour: Int?,
    initialMinute: Int?,
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    val calendar = remember {
        Calendar.getInstance()
    }
    val pickerState = rememberTimePickerState(
        initialHour = initialHour ?: calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = initialMinute ?: calendar.get(Calendar.MINUTE),
        is24Hour = false
    )

    StudySessionPickerDialogShell(
        title = title,
        onDismiss = onDismiss,
        onConfirm = { onTimeSelected(pickerState.hour, pickerState.minute) },
        confirmLabel = "Select"
    ) {
        StudySessionPickerTheme {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = pickerState)
            }
        }
    }
}

private fun persistStudySessionPhotoPermission(
    context: Context,
    uri: Uri
) {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudySessionComposerOverlay(
    cafe: Cafe,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    var isVisible by remember { mutableStateOf(false) }
    var isClosing by remember { mutableStateOf(false) }
    var title by remember(cafe.id) { mutableStateOf("") }
    var className by remember(cafe.id) { mutableStateOf("") }
    var selectedDateUtcMillis by remember(cafe.id) { mutableStateOf<Long?>(null) }
    var selectedStartHour by remember(cafe.id) { mutableStateOf<Int?>(null) }
    var selectedStartMinute by remember(cafe.id) { mutableStateOf<Int?>(null) }
    var selectedEndHour by remember(cafe.id) { mutableStateOf<Int?>(null) }
    var selectedEndMinute by remember(cafe.id) { mutableStateOf<Int?>(null) }
    var summary by remember(cafe.id) { mutableStateOf("") }
    var showDatePicker by remember(cafe.id) { mutableStateOf(false) }
    var activeTimeField by remember(cafe.id) { mutableStateOf<StudySessionTimeField?>(null) }
    val selectedPhotoUris = remember(cafe.id) { mutableStateListOf<String>() }
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            persistStudySessionPhotoPermission(context, uri)
            val uriText = uri.toString()
            if (selectedPhotoUris.size < 5 && uriText !in selectedPhotoUris) {
                selectedPhotoUris.add(uriText)
            }
        }
    }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    fun closeComposer() {
        if (isClosing) return
        isClosing = true
        isVisible = false
        scope.launch {
            delay(140)
            onDismiss()
        }
    }

    BackHandler(enabled = true) {
        closeComposer()
    }

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "studyComposerAlpha"
    )
    val overlayScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.92f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "studyComposerScale"
    )
    val draft = StudySessionDraft(
        title = title,
        className = className,
        scheduledDateUtcMillis = selectedDateUtcMillis,
        startHour = selectedStartHour,
        startMinute = selectedStartMinute,
        endHour = selectedEndHour,
        endMinute = selectedEndMinute,
        summary = summary,
        photoUris = selectedPhotoUris.toList()
    )
    val canCreate = StudySessionRepository.canCreate(draft)
    val textFieldColors = studySessionTextFieldColors()
    val dateText = selectedDateUtcMillis?.let(::formatStudySessionDate).orEmpty()
    val startTimeText = if (selectedStartHour != null && selectedStartMinute != null) {
        formatStudySessionTime(
            hour = selectedStartHour ?: 0,
            minute = selectedStartMinute ?: 0
        )
    } else {
        ""
    }
    val endTimeText = if (selectedEndHour != null && selectedEndMinute != null) {
        formatStudySessionTime(
            hour = selectedEndHour ?: 0,
            minute = selectedEndMinute ?: 0
        )
    } else {
        ""
    }
    val showTimeRangeError = selectedStartHour != null &&
        selectedStartMinute != null &&
        selectedEndHour != null &&
        selectedEndMinute != null &&
        !isStudySessionTimeRangeValid(
            startHour = selectedStartHour,
            startMinute = selectedStartMinute,
            endHour = selectedEndHour,
            endMinute = selectedEndMinute
        )
    val suggestedEndTotalMinutes = if (selectedStartHour != null && selectedStartMinute != null) {
        minOf(
            (((selectedStartHour ?: 0) * 60) + (selectedStartMinute ?: 0) + 60),
            (24 * 60) - 1
        )
    } else {
        null
    }

    Box(modifier = modifier.zIndex(10f)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.24f * overlayAlpha))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    closeComposer()
                }
        )

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(20.dp)
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .heightIn(max = 620.dp)
                .graphicsLayer {
                    alpha = overlayAlpha
                    scaleX = overlayScale
                    scaleY = overlayScale
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = StudySessionPopupSurfaceColor,
                contentColor = StudySessionPopupTextColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            border = BorderStroke(2.dp, Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = Color(0xFFD5F1FF)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.study),
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp),
                            tint = Color.Unspecified
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Create study session",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = StudySessionPopupTextColor
                        )
                        Text(
                            text = cafe.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = StudySessionPopupSecondaryTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { closeComposer() }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close study session form",
                            tint = StudySessionPopupTextColor
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Class") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors
                )
                StudySessionPickerField(
                    value = dateText,
                    label = "Date",
                    placeholder = "Choose a date",
                    icon = Icons.Filled.CalendarMonth,
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StudySessionPickerField(
                        value = startTimeText,
                        label = "Start",
                        placeholder = "Start time",
                        icon = Icons.Filled.AccessTime,
                        onClick = { activeTimeField = StudySessionTimeField.Start },
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors
                    )
                    StudySessionPickerField(
                        value = endTimeText,
                        label = "End",
                        placeholder = "End time",
                        icon = Icons.Filled.AccessTime,
                        onClick = { activeTimeField = StudySessionTimeField.End },
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors
                    )
                }
                if (showTimeRangeError) {
                    Text(
                        text = "End time needs to be later than the start time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = StudySessionPopupSecondaryTextColor
                    )
                }
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Summary") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp),
                    maxLines = 5,
                    colors = textFieldColors
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Work photos",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f),
                            color = StudySessionPopupTextColor
                        )
                        OutlinedButton(
                            onClick = { photoPicker.launch(arrayOf("image/*")) },
                            enabled = selectedPhotoUris.size < 5,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = StudySessionPopupFieldColor,
                                contentColor = StudySessionPopupFieldTextColor,
                                disabledContainerColor = StudySessionPopupFieldColor.copy(alpha = 0.42f),
                                disabledContentColor = StudySessionPopupFieldTextColor.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(1.dp, Color.Black)
                        ) {
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (selectedPhotoUris.isEmpty()) "Add" else "Add more")
                        }
                    }
                    if (selectedPhotoUris.isEmpty()) {
                        Text(
                            text = "Add up to 5 photos of notes, slides, or problems you want to work on.",
                            style = MaterialTheme.typography.bodySmall,
                            color = StudySessionPopupSecondaryTextColor
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedPhotoUris.forEach { uri ->
                                StudySessionPhotoPreview(
                                    uri = uri,
                                    onRemove = { selectedPhotoUris.remove(uri) }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { closeComposer() },
                        colors = ButtonDefaults.textButtonColors(contentColor = StudySessionPopupTextColor)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            StudySessionRepository.add(cafe, draft)?.let {
                                closeComposer()
                            }
                        },
                        enabled = canCreate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudySessionPopupFieldColor,
                            contentColor = StudySessionPopupFieldTextColor,
                            disabledContainerColor = StudySessionPopupFieldColor.copy(alpha = 0.42f),
                            disabledContentColor = StudySessionPopupFieldTextColor.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("Create")
                    }
                }
            }
        }

        if (showDatePicker) {
            StudySessionDatePickerDialog(
                initialDateUtcMillis = selectedDateUtcMillis,
                onDismiss = { showDatePicker = false },
                onDateSelected = { pickedDate ->
                    selectedDateUtcMillis = pickedDate
                    showDatePicker = false
                }
            )
        }

        activeTimeField?.let { timeField ->
            StudySessionTimePickerDialog(
                title = if (timeField == StudySessionTimeField.Start) {
                    "Pick a start time"
                } else {
                    "Pick an end time"
                },
                initialHour = when (timeField) {
                    StudySessionTimeField.Start -> selectedStartHour
                    StudySessionTimeField.End -> selectedEndHour ?: suggestedEndTotalMinutes?.div(60)
                },
                initialMinute = when (timeField) {
                    StudySessionTimeField.Start -> selectedStartMinute
                    StudySessionTimeField.End -> selectedEndMinute ?: suggestedEndTotalMinutes?.rem(60)
                },
                onDismiss = { activeTimeField = null },
                onTimeSelected = { hour, minute ->
                    when (timeField) {
                        StudySessionTimeField.Start -> {
                            selectedStartHour = hour
                            selectedStartMinute = minute
                            if (selectedEndHour == null || selectedEndMinute == null) {
                                val defaultEndTotalMinutes = minOf(
                                    ((hour * 60) + minute) + 60,
                                    (24 * 60) - 1
                                )
                                selectedEndHour = defaultEndTotalMinutes / 60
                                selectedEndMinute = defaultEndTotalMinutes % 60
                            }
                        }
                        StudySessionTimeField.End -> {
                            selectedEndHour = hour
                            selectedEndMinute = minute
                        }
                    }
                    activeTimeField = null
                }
            )
        }
    }
}

@Composable
private fun StudySessionPhotoPreview(
    uri: String,
    onRemove: () -> Unit
) {
    Box(modifier = Modifier.size(76.dp)) {
        OutlinedCard(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.18f))
        ) {
            AsyncImage(
                model = uri,
                contentDescription = "Selected study photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(3.dp)
                .size(24.dp)
                .clickable { onRemove() },
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.62f),
            contentColor = Color.White
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remove study photo",
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun RatingStars(rating: Float) {
    Row { repeat(rating.toInt()) { Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107)) }; repeat(5 - rating.toInt()) { Icon(Icons.Filled.Star, contentDescription = null, tint = Color.LightGray) } }
}

private data class BottomNavItem(
    val route: String,
    val iconRes: Int,
    val contentDescription: String
)

private class BottomNavMotionState {
    var settledIndex by mutableIntStateOf(0)
    var bubblePosition by mutableFloatStateOf(0f)
}

private class BottomNavUiState {
    var enabled by mutableStateOf(true)
    var dimFraction by mutableFloatStateOf(0f)
    var overlayCoversToolbar by mutableStateOf(false)
}

private val LocalBottomNavMotionState = staticCompositionLocalOf { BottomNavMotionState() }
private val LocalBottomNavUiState = staticCompositionLocalOf { BottomNavUiState() }
private val LocalUsesPersistentBottomNav = staticCompositionLocalOf { false }
private val BottomNavToolbarColor = Color(0xFF6B5947)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.MainScreen.route, R.drawable.nav_cards, "Cards"),
    BottomNavItem(Screen.MapScreen.route, R.drawable.nav_map, "Map"),
    BottomNavItem(Screen.BookmarkScreen.route, R.drawable.nav_save, "Save"),
    BottomNavItem(Screen.ProfileScreen.route, R.drawable.nav_profile_coffee, "Profile")
)

private val bottomNavRoutes = setOf(
    Screen.MainScreen.route,
    Screen.MapScreen.route,
    Screen.BookmarkScreen.route,
    Screen.ProfileScreen.route,
    Screen.Preferences.route,
    Screen.CafeDetails.route,
    Screen.WriteReview.route
)

private val bottomNavToolbarHeight = 68.dp
private val bottomNavBubbleSize = 50.dp

private fun Modifier.bottomNavFrame(): Modifier {
    return fillMaxWidth()
        .navigationBarsPadding()
        .padding(start = 28.dp, end = 28.dp, top = 6.dp, bottom = 14.dp)
        .height(bottomNavToolbarHeight)
}

@Composable
private fun ReportBottomNavOverlayCoverage(isActive: Boolean) {
    val usesPersistentBottomNav = LocalUsesPersistentBottomNav.current
    val uiState = LocalBottomNavUiState.current

    SideEffect {
        if (usesPersistentBottomNav) {
            uiState.overlayCoversToolbar = isActive
        }
    }

    DisposableEffect(usesPersistentBottomNav) {
        onDispose {
            if (usesPersistentBottomNav) {
                uiState.overlayCoversToolbar = false
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController, enabled: Boolean = true, dimFraction: Float = if (enabled) 0f else 1f) {
    if (LocalUsesPersistentBottomNav.current) {
        val uiState = LocalBottomNavUiState.current

        SideEffect {
            uiState.enabled = enabled
            uiState.dimFraction = dimFraction
        }

        Spacer(modifier = Modifier.bottomNavFrame())
        return
    }

    FloatingBottomNavBar(
        navController = navController,
        enabled = enabled,
        dimFraction = dimFraction
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun FloatingBottomNavBar(
    navController: NavHostController,
    enabled: Boolean = true,
    dimFraction: Float = if (enabled) 0f else 1f,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val route = navBackStackEntry?.destination?.route
    val motionState = LocalBottomNavMotionState.current
    val routeIndex = bottomNavItems.indexOfFirst { item -> item.route == route }
    val selectedIndex = routeIndex.takeIf { index -> index >= 0 } ?: motionState.settledIndex
    val bubblePosition = remember { Animatable(motionState.bubblePosition) }
    var motionStart by remember { mutableFloatStateOf(motionState.bubblePosition) }
    var motionEnd by remember { mutableFloatStateOf(motionState.bubblePosition) }

    LaunchedEffect(selectedIndex) {
        val start = motionState.bubblePosition
        val target = selectedIndex.toFloat()

        if (abs(bubblePosition.value - start) > 0.001f) {
            bubblePosition.snapTo(start)
        }

        motionStart = start
        motionEnd = target

        if (abs(start - target) > 0.001f) {
            bubblePosition.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
            )
        } else {
            bubblePosition.snapTo(target)
        }

        motionState.bubblePosition = target
        motionState.settledIndex = selectedIndex
    }

    LaunchedEffect(bubblePosition) {
        snapshotFlow { bubblePosition.value }.collect { position ->
            motionState.bubblePosition = position
        }
    }

    val motionRange = motionEnd - motionStart
    val motionProgress = if (motionRange > -0.001f && motionRange < 0.001f) {
        1f
    } else {
        ((bubblePosition.value - motionStart) / motionRange).coerceIn(0f, 1f)
    }
    val motionDirection = if (motionEnd >= motionStart) 1f else -1f
    val bubbleCurveOffset = (sin(motionProgress * PI * 2).toFloat() * 8f * motionDirection).dp
    val barAlpha = (1f - (0.55f * dimFraction)).coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = modifier
            .bottomNavFrame()
            .graphicsLayer { alpha = barAlpha }
    ) {
        val itemWidth = maxWidth / bottomNavItems.size
        val bubbleX = itemWidth * bubblePosition.value + ((itemWidth - bottomNavBubbleSize) / 2)
        val bubbleY = ((bottomNavToolbarHeight - bottomNavBubbleSize) / 2) + bubbleCurveOffset

        Box(
            modifier = Modifier
                .matchParentSize()
                .shadow(elevation = 12.dp, shape = CircleShape, clip = false)
                .background(BottomNavToolbarColor, CircleShape)
        )
        Box(
            modifier = Modifier
                .offset(x = bubbleX, y = bubbleY)
                .size(bottomNavBubbleSize)
                .background(Color.White.copy(alpha = 0.32f), CircleShape)
        )
        Row(
            modifier = Modifier.matchParentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEachIndexed { index, item ->
                key(item.route) {
                    val isSelected = selectedIndex == index
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.08f else 1f,
                        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                        label = "bottomNavIconScale-${item.route}"
                    )
                    val interactionSource = remember { MutableInteractionSource() }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                enabled = enabled && !isSelected,
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                motionState.settledIndex = selectedIndex
                                motionState.bubblePosition = bubblePosition.value
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(item.iconRes),
                            contentDescription = item.contentDescription,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(32.dp)
                                .graphicsLayer {
                                    alpha = if (enabled) 1f else 0.62f
                                    scaleX = iconScale
                                    scaleY = iconScale
                                }
                        )
                    }
                }
            }
        }
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
    val placesClient = remember(context) { if (Places.isInitialized()) Places.createClient(context) else null }

    var profile by remember { mutableStateOf<Profile?>(null) }
    var bioText by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isEditingBio by remember { mutableStateOf(false) }
    var isEditingName by remember { mutableStateOf(false) }
    var editFirstName by remember { mutableStateOf("") }
    var editLastName by remember { mutableStateOf("") }
    var noise by remember { mutableIntStateOf(3) }
    var outlet by remember { mutableIntStateOf(3) }
    var seating by remember { mutableIntStateOf(3) }
    var wifi by remember { mutableIntStateOf(3) }

    // "saved" | "sessions" | "reviews" | null
    var activeStat by remember { mutableStateOf<String?>(null) }
    var selectedRecentCafeId by rememberSaveable { mutableStateOf<String?>(null) }
    var recentOverlayCafe by remember { mutableStateOf<Cafe?>(null) }
    val detailProgress = remember { Animatable(0f) }
    val detailOverlayLayoutSpec = remember { DetailOverlayLayoutSpec() }
    val detailScrimInteractionSource = remember { MutableInteractionSource() }

    val savedCafes = BookmarkRepository.cafes()

    val myReviews = ReviewRepository.myReviews()

    LaunchedEffect(Unit) {
        runCatching { ReviewRepository.loadMyReviews() }
    }

    val studySessions = StudySessionRepository.sessions()
    val savedCount = savedCafes.size
    val studySessionCount = studySessions.size
    val reviewCount = myReviews.size
    val selectedRecentCafe = selectedRecentCafeId?.let { cafeId -> CafeRepository.getCafe(cafeId) }
    CafeDetailDataEffect(cafe = selectedRecentCafe, placesClient = placesClient)

    LaunchedEffect(selectedRecentCafe) {
        if (selectedRecentCafe != null) {
            recentOverlayCafe = selectedRecentCafe
        }
    }

    LaunchedEffect(selectedRecentCafeId) {
        if (selectedRecentCafeId != null) {
            detailProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )
        } else if (recentOverlayCafe != null || detailProgress.value > 0f) {
            detailProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            )
            recentOverlayCafe = null
        }
    }

    BackHandler(enabled = selectedRecentCafeId != null) {
        selectedRecentCafeId = null
    }

    val heroBg = Color(0xFF4A231C)
    val caramel = Color(0xFFC5A07D)
    val cream = Color(0xFFF6E9D8)
    val lightBrown = Color(0xFFEAD7CE)
    val darkBrown = Color(0xFF694B2E)

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            avatarUri = uri
            scope.launch {
                val user = supabase.auth.currentUserOrNull() ?: return@launch
                try {
                    val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                        ?: return@launch
                    val fileName = "${user.id}.png"
                    supabase.storage.from("avatars")
                        .upload(path = fileName, data = bytes) { upsert = true }
                    val publicUrl = supabase.storage.from("avatars").publicUrl(fileName)
                    supabase.from("profiles")
                        .update(mapOf("avatar_url" to publicUrl)) {
                            filter { eq("id", user.id) }
                        }
                    profile = profile?.copy(avatar_url = publicUrl)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val user = supabase.auth.currentUserOrNull()
        if (user != null) {
            try {
                val result = supabase.from("profiles")
                    .select { filter { eq("id", user.id) } }
                    .decodeSingle<Profile>()
                profile = result
                bioText = result.bio ?: ""
                editFirstName = result.first_name
                editLastName = result.last_name
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                val prefs = supabase.from("user_preferences")
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
            modifier = Modifier.fillMaxSize().background(heroBg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = caramel)
                Spacer(Modifier.height(12.dp))
                Text("Loading profile...", color = cream)
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    navController = navController,
                    enabled = detailProgress.value < 0.01f,
                    dimFraction = detailProgress.value
                )
            },
            containerColor = cream
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(heroBg)
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        // Ring avatar
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clickable { pickImage.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            // Outer ring
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                shape = CircleShape,
                                color = Color.Transparent,
                                border = BorderStroke(2.dp, caramel)
                            ) {}
                            // Inner avatar
                            Surface(
                                modifier = Modifier.size(74.dp),
                                shape = CircleShape,
                                color = caramel
                            ) {
                                when {
                                    avatarUri != null ->
                                        AsyncImage(
                                            model = avatarUri,
                                            contentDescription = "Profile picture",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    profile?.avatar_url != null -> {
                                        val avatarUrl = profile!!.avatar_url!!
                                        val bustedUrl = remember(avatarUrl) {
                                            "$avatarUrl?t=${System.currentTimeMillis()}"
                                        }
                                        AsyncImage(
                                            model = bustedUrl,
                                            contentDescription = "Profile picture",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    else ->
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = profile?.let {
                                                    "${it.first_name.firstOrNull() ?: ""}${it.last_name.firstOrNull() ?: ""}"
                                                        .uppercase().ifBlank { "?" }
                                                } ?: "?",
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = heroBg
                                            )
                                        }
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = profile?.let {
                                    "${it.first_name.trim()} ${it.last_name.trim()}".trim().ifBlank { "User" }
                                } ?: "User",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = cream
                            )
                            Surface(
                                modifier = Modifier.clickable {
                                    editFirstName = profile?.first_name ?: ""
                                    editLastName = profile?.last_name ?: ""
                                    isEditingName = true
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = caramel.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, caramel.copy(alpha = 0.38f))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit name",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp).size(12.dp),
                                    tint = caramel
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = caramel.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, caramel.copy(alpha = 0.38f))
                        ) {

                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    ProfileStatPill(
                        count = savedCount,
                        label = "SAVED",
                        isActive = activeStat == "saved",
                        onClick = { activeStat = if (activeStat == "saved") null else "saved" },
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatPill(
                        count = studySessionCount,
                        label = "SESSIONS",
                        isActive = activeStat == "sessions",
                        onClick = { activeStat = if (activeStat == "sessions") null else "sessions" },
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatPill(
                        count = reviewCount,
                        label = "REVIEWS",
                        isActive = activeStat == "reviews",
                        onClick = { activeStat = if (activeStat == "reviews") null else "reviews" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (activeStat != null) {
                item {
                    ProfileStatPanel(
                        activeStat = activeStat!!,
                        savedCafes = savedCafes,
                        studySessions = studySessions,
                        myReviews = myReviews,
                        navController = navController,
                        onClose = { activeStat = null },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 6.dp)
                    )
                }
            }

            // About section
            item {
                ProfileSectionCard(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "ABOUT",
                            style = MaterialTheme.typography.labelSmall,
                            color = caramel,
                            letterSpacing = 0.09.sp
                        )
                        if (isEditingBio) {
                            TextButton(
                                onClick = {
                                    isEditingBio = false
                                    scope.launch {
                                        val user = supabase.auth.currentUserOrNull()
                                            ?: return@launch
                                        runCatching {
                                            supabase.from("profiles")
                                                .update(mapOf("bio" to bioText)) {
                                                    filter { eq("id", user.id) }
                                                }
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp),
                                colors = ButtonDefaults.textButtonColors(contentColor = caramel)
                            ) {
                                Text("Save", style = MaterialTheme.typography.labelSmall)
                            }
                        } else {
                            ProfileEditChip(onClick = { isEditingBio = true })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (isEditingBio) {
                        OutlinedTextField(
                            value = bioText,
                            onValueChange = { bioText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 72.dp),
                            placeholder = {
                                Text(
                                    "Tell us about yourself...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = darkBrown.copy(alpha = 0.4f)
                                )
                            },
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = darkBrown,
                                unfocusedTextColor = darkBrown,
                                focusedContainerColor = lightBrown,
                                unfocusedContainerColor = lightBrown,
                                focusedBorderColor = caramel,
                                unfocusedBorderColor = caramel.copy(alpha = 0.4f),
                                cursorColor = darkBrown
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = lightBrown
                        ) {
                            Text(
                                text = bioText.ifBlank { "Tap edit to add a bio..." },
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 10.dp
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (bioText.isBlank()) darkBrown.copy(alpha = 0.4f)
                                else darkBrown,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                }
            }

            // Preferences
            item {
                ProfileSectionCard(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "PREFERENCES",
                            style = MaterialTheme.typography.labelSmall,
                            color = caramel,
                            letterSpacing = 0.09.sp
                        )
                        ProfileEditChip(
                            onClick = { navController.navigate(Screen.Preferences.route) }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            CompactPrefBar(label = "WiFi", value = wifi)
                            CompactPrefBar(label = "Seating", value = seating)
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            CompactPrefBar(label = "Noise", value = noise)
                            CompactPrefBar(label = "Outlets", value = outlet)
                        }
                    }
                }
            }

            // Recently viewed
            item {
                val recentCafes = RecentRepository.previewCafes(limit = 3)
                ProfileSectionCard(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "RECENTLY VIEWED",
                            style = MaterialTheme.typography.labelSmall,
                            color = caramel,
                            letterSpacing = 0.09.sp
                        )
                        Text(
                            "See all",
                            style = MaterialTheme.typography.labelSmall,
                            color = caramel,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.MainScreen.route)
                            }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    if (recentCafes.isEmpty()) {
                        Text(
                            "No recently viewed cafes yet.",
                            color = darkBrown.copy(alpha = 0.45f),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        recentCafes.forEachIndexed { index, cafe ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        RecentRepository.add(cafe.id)
                                        recentOverlayCafe = cafe
                                        selectedRecentCafeId = cafe.id
                                    }
                                    .padding(vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(42.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = lightBrown
                                ) {
                                    AsyncImage(
                                        model = cafe.heroImageBitmap ?: cafe.imageUrl
                                        ?: cafe.imageResId,
                                        contentDescription = cafe.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        cafe.name,
                                        fontWeight = FontWeight.SemiBold,
                                        color = darkBrown,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        formatDistanceAway(cafe.distanceMeters) ?: cafe.address,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = caramel,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    tint = caramel,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (index < recentCafes.lastIndex) {
                                HorizontalDivider(color = lightBrown)
                            }
                        }
                    }
                }
            }

            // Logout
            item {
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
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.Red),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color.Red)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Logout",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            }
        }

        val detailCafe = recentOverlayCafe
        if (detailCafe != null && detailProgress.value > 0f) {
            val scrimAlpha by animateFloatAsState(
                targetValue = 0.2f * detailProgress.value,
                animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
                label = "profileRecentDetailScrimAlpha"
            )
            val overlayScale by animateFloatAsState(
                targetValue = 0.94f + (0.06f * detailProgress.value),
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "profileRecentDetailOverlayScale"
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(20f)
                    .background(Color.Black.copy(alpha = scrimAlpha))
                    .clickable(
                        interactionSource = detailScrimInteractionSource,
                        indication = null
                    ) {
                        selectedRecentCafeId = null
                    }
            )

            ExpandedCafeDetailOverlay(
                navController = navController,
                cafe = detailCafe,
                onClose = { selectedRecentCafeId = null },
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
                sharedHeroModel = detailCafe.primaryImageModel(),
                transitionProgress = detailProgress.value,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(21f)
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
    if (isEditingName) {
        NameEditDialog(
            firstName = editFirstName,
            lastName = editLastName,
            onFirstNameChange = { editFirstName = it },
            onLastNameChange = { editLastName = it },
            onDismiss = { isEditingName = false },
            onSave = {
                isEditingName = false
                profile = profile?.copy(
                    first_name = editFirstName.trim(),
                    last_name = editLastName.trim()
                )
                scope.launch {
                    val user = supabase.auth.currentUserOrNull() ?: return@launch
                    runCatching {
                        supabase.from("profiles")
                            .update(mapOf(
                                "first_name" to editFirstName.trim(),
                                "last_name" to editLastName.trim()
                            )) {
                                filter { eq("id", user.id) }
                            }
                    }
                }
            }
        )
    }
}
@Composable
private fun NameEditDialog(
    firstName: String,
    lastName: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val cream = Color(0xFFF6E9D8)
    val caramel = Color(0xFFC5A07D)
    val darkBrown = Color(0xFF4A231C)
    val lightBrown = Color(0xFFEAD7CE)
    val playfairDisplay = remember { FontFamily(Font(R.font.playfair_display)) }
    val isSaveEnabled = firstName.trim().isNotBlank() || lastName.trim().isNotBlank()

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = darkBrown,
        unfocusedTextColor = darkBrown,
        focusedContainerColor = lightBrown,
        unfocusedContainerColor = lightBrown,
        focusedBorderColor = caramel,
        unfocusedBorderColor = caramel.copy(alpha = 0.45f),
        focusedLabelColor = darkBrown,
        unfocusedLabelColor = darkBrown.copy(alpha = 0.6f),
        cursorColor = darkBrown
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .widthIn(max = 400.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = cream,
                contentColor = darkBrown
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            border = BorderStroke(1.dp, caramel.copy(alpha = 0.38f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape,
                        color = caramel.copy(alpha = 0.22f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = caramel
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Edit name",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = playfairDisplay,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = darkBrown,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Cancel",
                            tint = darkBrown.copy(alpha = 0.5f)
                        )
                    }
                }

                HorizontalDivider(color = caramel.copy(alpha = 0.22f))

                // Fields
                OutlinedTextField(
                    value = firstName,
                    onValueChange = onFirstNameChange,
                    label = {
                        Text(
                            "First name",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = playfairDisplay),
                            color = darkBrown
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = onLastNameChange,
                    label = {
                        Text(
                            "Last name",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = playfairDisplay),
                            color = darkBrown
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                    shape = RoundedCornerShape(14.dp)
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = darkBrown.copy(alpha = 0.6f)
                        )
                    ) {
                        Text(
                            "Cancel",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = playfairDisplay)
                        )
                    }
                    Button(
                        onClick = onSave,
                        enabled = isSaveEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = caramel,
                            contentColor = darkBrown,
                            disabledContainerColor = caramel.copy(alpha = 0.32f),
                            disabledContentColor = darkBrown.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Save",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = playfairDisplay)
                        )
                    }
                }
            }
        }
    }
}

// Stat Pill
@Composable
private fun ProfileStatPill(
    count: Int,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heroBg = Color(0xFF4A231C)
    val caramel = Color(0xFFC5A07D)

    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isActive) heroBg else Color.White,
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) heroBg else Color(0xFFEAD7CE)
        ),
        shadowElevation = if (isActive) 0.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isActive) Color(0xFFF6E9D8) else heroBg
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) caramel else caramel,
                letterSpacing = 0.3.sp,
                fontSize = 8.sp
            )
        }
    }
}

// Stat Panel (expands below pills)
@Composable
private fun ProfileStatPanel(
    activeStat: String,
    savedCafes: List<Cafe>,
    studySessions: List<StudySession>,
    myReviews: List<Review>,
    navController: NavHostController,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heroBg = Color(0xFF4A231C)
    val caramel = Color(0xFFC5A07D)
    val cream = Color(0xFFF6E9D8)
    val lightBrown = Color(0xFFEAD7CE)
    val darkBrown = Color(0xFF694B2E)

    val title = when (activeStat) {
        "saved" -> "SAVED CAFES"
        "sessions" -> "STUDY SESSIONS"
        "reviews" -> "MY REVIEWS"
        else -> ""
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(0.5.dp, lightBrown),
        shadowElevation = 2.dp
    ) {
        Column {
            // Panel header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(heroBg, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = caramel,
                    letterSpacing = 0.4.sp,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onClose() },
                    shape = CircleShape,
                    color = caramel.copy(alpha = 0.25f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = caramel,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Panel content
            Column(modifier = Modifier.padding(12.dp)) {
                when (activeStat) {

                    "saved" -> {
                        if (savedCafes.isEmpty()) {
                            Text(
                                "No saved cafes yet. Swipe right on a cafe to save it.",
                                style = MaterialTheme.typography.bodySmall,
                                color = darkBrown.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            savedCafes.take(5).forEachIndexed { index, cafe ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            navController.navigate(
                                                Screen.CafeDetails.createRoute(cafe.id)
                                            )
                                        }
                                        .padding(vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(36.dp),
                                        shape = RoundedCornerShape(7.dp),
                                        color = lightBrown
                                    ) {
                                        AsyncImage(
                                            model = cafe.heroImageBitmap ?: cafe.imageUrl
                                            ?: cafe.imageResId,
                                            contentDescription = cafe.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            cafe.name,
                                            fontWeight = FontWeight.SemiBold,
                                            color = darkBrown,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            formatDistanceAway(cafe.distanceMeters)
                                                ?: "Distance unavailable",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = caramel
                                        )
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = lightBrown
                                    ) {
                                        Text(
                                            "Saved",
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 3.dp
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = darkBrown,
                                            fontSize = 8.sp
                                        )
                                    }
                                }
                                if (index < minOf(savedCafes.size, 5) - 1) {
                                    HorizontalDivider(color = lightBrown)
                                }
                            }
                            if (savedCafes.size > 5) {
                                TextButton(
                                    onClick = {
                                        navController.navigate(Screen.BookmarkScreen.route)
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = caramel),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text(
                                        "View all ${savedCafes.size}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    "sessions" -> {
                        if (studySessions.isEmpty()) {
                            Text(
                                "No study sessions yet. Open a cafe and tap the study icon to create one.",
                                style = MaterialTheme.typography.bodySmall,
                                color = darkBrown.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            studySessions.take(5).forEachIndexed { index, session ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            navController.navigate(
                                                Screen.CafeDetails.createRoute(session.cafeId)
                                            )
                                        }
                                        .padding(vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(36.dp),
                                        shape = RoundedCornerShape(7.dp),
                                        color = Color(0xFFD5F1FF)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.study),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = Color.Unspecified
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            session.title,
                                            fontWeight = FontWeight.SemiBold,
                                            color = darkBrown,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "${session.cafeName} · ${session.dateText}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = caramel,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFD5F1FF)
                                    ) {
                                        Text(
                                            session.timeText,
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 3.dp
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF185FA5),
                                            fontSize = 8.sp
                                        )
                                    }
                                }
                                if (index < minOf(studySessions.size, 5) - 1) {
                                    HorizontalDivider(color = lightBrown)
                                }
                            }
                            if (studySessions.size > 5) {
                                TextButton(
                                    onClick = {
                                        navController.navigate(Screen.BookmarkScreen.route)
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = caramel),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text(
                                        "View all ${studySessions.size}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    "reviews" -> {
                        if (myReviews.isEmpty()) {
                            Text(
                                "No reviews yet. Open a cafe and tap \"Write review\" to add one.",
                                style = MaterialTheme.typography.bodySmall,
                                color = darkBrown.copy(alpha = 0.5f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            myReviews.take(5).forEachIndexed { index, review ->
                                val cafe = CafeRepository.getCafe(review.cafe_id)
                                val deleteScope = rememberCoroutineScope()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            navController.navigate(Screen.CafeDetails.createRoute(review.cafe_id))
                                        }
                                        .padding(vertical = 7.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Surface(
                                        modifier = Modifier.size(36.dp),
                                        shape = RoundedCornerShape(7.dp),
                                        color = lightBrown
                                    ) {
                                        if (cafe != null) {
                                            AsyncImage(
                                                model = cafe.heroImageBitmap ?: cafe.imageUrl ?: cafe.imageResId,
                                                contentDescription = cafe.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            cafe?.name ?: review.cafe_id,
                                            fontWeight = FontWeight.SemiBold,
                                            color = darkBrown,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            review.body,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = darkBrown.copy(alpha = 0.75f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            lineHeight = 16.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            deleteScope.launch {
                                                runCatching { ReviewRepository.deleteReview(review) }
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Delete review",
                                            tint = caramel.copy(alpha = 0.72f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                if (index < minOf(myReviews.size, 5) - 1) {
                                    HorizontalDivider(color = lightBrown)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun CompactPrefBar(label: String, value: Int, max: Int = 5) {
    val caramel = Color(0xFFC5A07D)
    val darkBrown = Color(0xFF694B2E)
    val lightBrown = Color(0xFFEAD7CE)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(48.dp),
            style = MaterialTheme.typography.labelSmall,
            color = darkBrown,
            fontSize = 10.sp
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .background(lightBrown, RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(
                        fraction = value.coerceIn(0, max).toFloat() / max.toFloat()
                    )
                    .background(Color(0xFF694B2E), RoundedCornerShape(2.dp))
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = value.toString(),
            modifier = Modifier.width(12.dp),
            style = MaterialTheme.typography.labelSmall,
            color = caramel,
            fontSize = 10.sp,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ProfileSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp), content = content)
    }
}

@Composable
private fun ProfileEditChip(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFC5A07D).copy(alpha = 0.12f),
        border = BorderStroke(1.dp, Color(0xFFC5A07D).copy(alpha = 0.45f))
    ) {
        Text(
            text = "Edit",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFC5A07D)
        )
    }
}

@Composable
private fun ProfileBadgeChip(label: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFEAD7CE)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF4A231C)
        )
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
    val cardColor = CoffeeSurfaceLight
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
                                val user = supabase.auth.currentUserOrNull() ?: return@launch
                                val prefs = UserPreferences(
                                    user_id = user.id,
                                    noise_level = noise,
                                    outlet_importance = outlet,
                                    seating_importance = seating,
                                    wifi_importance = wifi
                                )

                                val existing = supabase.from("user_preferences")
                                    .select { filter { eq("user_id", user.id) } }
                                    .decodeSingleOrNull<UserPreferences>()

                                if (existing != null) {
                                    supabase.from("user_preferences")
                                        .update(prefs) {
                                            filter { eq("user_id", user.id) }
                                        }
                                } else {
                                    supabase.from("user_preferences").insert(prefs)
                                }

                                navController.navigate(Screen.MainScreen.route) { popUpTo(0) }
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
            onValueChange = { onChange(it.roundToInt()) },
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
