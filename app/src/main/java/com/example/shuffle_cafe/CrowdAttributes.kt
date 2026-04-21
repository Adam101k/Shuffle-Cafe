package com.example.shuffle_cafe

import androidx.compose.runtime.mutableStateMapOf
import com.google.android.gms.maps.model.LatLng
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

private const val DEFAULT_NEARBY_THRESHOLD_METERS = 100f
private const val CAFE_CROWD_ATTRIBUTES_TABLE = "cafe_crowd_attributes"

internal enum class OutletAvailability(val label: String) {
    PLENTY("Plenty"),
    SOME("Some"),
    SCARCE("Scarce"),
    UNKNOWN("Unknown")
}

internal enum class WifiSpeed(val label: String) {
    FAST("Fast"),
    DECENT("Decent"),
    SLOW("Slow"),
    NONE("None"),
    UNKNOWN("Unknown")
}

internal enum class BathroomAvailability(val label: String) {
    AVAILABLE("Available"),
    NONE("None"),
    UNKNOWN("Unknown")
}

internal enum class SeatingAvailability(val label: String) {
    PLENTY("Plenty"),
    FAIR("Fair"),
    SCARCE("Scarce"),
    NONE("None"),
    UNKNOWN("Unknown")
}

internal enum class SeatingSpace(val label: String) {
    SPACIOUS("Spacious"),
    FAIR("Fair"),
    CRAMPED("Cramped"),
    UNKNOWN("Unknown")
}

internal enum class SeatingComfort(val label: String) {
    UNCOMFORTABLE("Uncomfortable"),
    OKAY("Okay"),
    COMFORTABLE("Comfortable"),
    VERY_COMFORTABLE("Very comfortable"),
    UNKNOWN("Unknown")
}

internal enum class CrowdLevel(val label: String) {
    PACKED("Packed"),
    BUSY("Busy"),
    MODERATE("Moderate"),
    LIGHT("Light"),
    EMPTY("Empty"),
    UNKNOWN("Unknown")
}

internal enum class NoiseLevel(val label: String) {
    VERY_LOUD("Very loud"),
    LOUD("Loud"),
    MODERATE("Moderate"),
    QUIET("Quiet"),
    SILENT("Silent"),
    UNKNOWN("Unknown")
}

internal enum class PetFriendly(val label: String) {
    INDOOR_ALLOWED("Indoor allowed"),
    PATIO_ONLY("Patio only"),
    NOT_ALLOWED("Not allowed"),
    UNKNOWN("Unknown")
}

internal enum class CleanlinessRating(val label: String) {
    POOR("Poor"),
    OKAY("Okay"),
    GOOD("Good"),
    GREAT("Great"),
    UNKNOWN("Unknown")
}

internal enum class VibeTag(val label: String) {
    STUDY_HEAVY("Study heavy"),
    MINIMAL("Minimal"),
    ARTSY("Artsy"),
    TRENDY("Trendy"),
    MODERN("Modern"),
    COZY("Cozy"),
    SOCIAL("Social"),
    FAMILY_FRIENDLY("Family friendly")
}

internal data class ProtectedCrowdSecret(
    val value: String? = null,
    val knownToExist: Boolean = false
)

internal sealed class ProtectedSecretDisplayState {
    data object Censored : ProtectedSecretDisplayState()
    data class Revealed(val value: String) : ProtectedSecretDisplayState()
    data object AskStaff : ProtectedSecretDisplayState()
    data object Unavailable : ProtectedSecretDisplayState()
    data object Unknown : ProtectedSecretDisplayState()
}

internal data class CafeCrowdAttributes(
    val outletAvailability: OutletAvailability = OutletAvailability.UNKNOWN,
    val wifiName: String? = null,
    val wifiSpeed: WifiSpeed = WifiSpeed.UNKNOWN,
    val wifiPassword: ProtectedCrowdSecret = ProtectedCrowdSecret(),
    val bathroomAvailability: BathroomAvailability = BathroomAvailability.UNKNOWN,
    val bathroomCode: ProtectedCrowdSecret = ProtectedCrowdSecret(),
    val seatingAvailability: SeatingAvailability = SeatingAvailability.UNKNOWN,
    val seatingSpace: SeatingSpace = SeatingSpace.UNKNOWN,
    val seatingComfort: SeatingComfort = SeatingComfort.UNKNOWN,
    val crowdLevel: CrowdLevel = CrowdLevel.UNKNOWN,
    val noiseLevel: NoiseLevel = NoiseLevel.UNKNOWN,
    val vibeTags: Set<VibeTag> = emptySet(),
    val petFriendly: PetFriendly = PetFriendly.UNKNOWN,
    val cleanlinessRating: CleanlinessRating = CleanlinessRating.UNKNOWN,
    val seatingPhotoUris: List<String> = emptyList(),
    val menuPhotoUris: List<String> = emptyList(),
    val lastUpdatedEpochMillis: Long? = null
)

internal data class CrowdAttributeSuggestion(
    val outletAvailability: OutletAvailability? = null,
    val wifiName: String? = null,
    val wifiSpeed: WifiSpeed? = null,
    val wifiPassword: ProtectedCrowdSecret? = null,
    val bathroomAvailability: BathroomAvailability? = null,
    val bathroomCode: ProtectedCrowdSecret? = null,
    val seatingAvailability: SeatingAvailability? = null,
    val seatingSpace: SeatingSpace? = null,
    val seatingComfort: SeatingComfort? = null,
    val crowdLevel: CrowdLevel? = null,
    val noiseLevel: NoiseLevel? = null,
    val vibeTags: Set<VibeTag>? = null,
    val petFriendly: PetFriendly? = null,
    val cleanlinessRating: CleanlinessRating? = null,
    val seatingPhotoUris: List<String> = emptyList(),
    val menuPhotoUris: List<String> = emptyList()
)

@Serializable
internal data class CafeCrowdAttributesRow(
    val cafe_id: String,
    val outlet_availability: String = OutletAvailability.UNKNOWN.name,
    val wifi_name: String? = null,
    val wifi_speed: String = WifiSpeed.UNKNOWN.name,
    val wifi_password: String? = null,
    val wifi_password_known_to_exist: Boolean = false,
    val bathroom_availability: String = BathroomAvailability.UNKNOWN.name,
    val bathroom_code: String? = null,
    val bathroom_code_known_to_exist: Boolean = false,
    val seating_availability: String = SeatingAvailability.UNKNOWN.name,
    val seating_space: String = SeatingSpace.UNKNOWN.name,
    val seating_comfort: String = SeatingComfort.UNKNOWN.name,
    val crowd_level: String = CrowdLevel.UNKNOWN.name,
    val noise_level: String = NoiseLevel.UNKNOWN.name,
    val vibe_tags: List<String> = emptyList(),
    val pet_friendly: String = PetFriendly.UNKNOWN.name,
    val cleanliness_rating: String = CleanlinessRating.UNKNOWN.name,
    val seating_photo_urls: List<String> = emptyList(),
    val menu_photo_urls: List<String> = emptyList(),
    val last_updated_epoch_millis: Long? = null,
    val updated_by: String? = null
)

internal fun isUserNearCafe(
    user: LatLng?,
    cafe: LatLng?,
    thresholdMeters: Float = DEFAULT_NEARBY_THRESHOLD_METERS
): Boolean {
    if (user == null || cafe == null) return false
    return distanceBetweenMeters(user, cafe) <= thresholdMeters
}

internal fun protectedSecretDisplayState(
    secret: ProtectedCrowdSecret,
    isNearby: Boolean,
    isAvailable: Boolean?
): ProtectedSecretDisplayState {
    val normalizedValue = secret.value?.trim().orEmpty()

    if (isAvailable == false) {
        return ProtectedSecretDisplayState.Unavailable
    }

    if (normalizedValue.isBlank() && !secret.knownToExist) {
        return ProtectedSecretDisplayState.Unknown
    }

    if (!isNearby) {
        return ProtectedSecretDisplayState.Censored
    }

    if (normalizedValue.isNotBlank()) {
        return ProtectedSecretDisplayState.Revealed(normalizedValue)
    }

    return if (secret.knownToExist) {
        ProtectedSecretDisplayState.AskStaff
    } else {
        ProtectedSecretDisplayState.Unknown
    }
}

internal object CrowdAttributeRepository {
    private val attributesByCafeId = mutableStateMapOf<String, CafeCrowdAttributes>()

    fun attributesFor(cafeId: String): CafeCrowdAttributes {
        return attributesByCafeId[cafeId] ?: CafeCrowdAttributes()
    }

    suspend fun loadFromSupabase(cafeId: String): Boolean {
        val row = supabase
            .from(CAFE_CROWD_ATTRIBUTES_TABLE)
            .select {
                filter { eq("cafe_id", cafeId) }
            }
            .decodeSingleOrNull<CafeCrowdAttributesRow>()
            ?: return false

        attributesByCafeId[cafeId] = row.toCafeCrowdAttributes()
        return true
    }

    suspend fun loadManyFromSupabase(cafeIds: Iterable<String>): Int {
        val ids = cafeIds
            .mapNotNull { cafeId -> cafeId.trim().takeIf { it.isNotBlank() } }
            .distinct()
        if (ids.isEmpty()) return 0

        val rows = supabase
            .from(CAFE_CROWD_ATTRIBUTES_TABLE)
            .select {
                filter { isIn("cafe_id", ids.map<String, Any> { it }) }
            }
            .decodeList<CafeCrowdAttributesRow>()

        rows.forEach { row ->
            attributesByCafeId[row.cafe_id] = row.toCafeCrowdAttributes()
        }
        return rows.size
    }

    suspend fun submitSuggestionToSupabase(
        cafeId: String,
        suggestion: CrowdAttributeSuggestion,
        nowMillis: Long = System.currentTimeMillis()
    ) {
        val user = supabase.auth.currentUserOrNull()
            ?: error("Sign in before suggesting changes.")
        val updatedAttributes = mergedAttributesFor(cafeId, suggestion, nowMillis)
        val rowToSave = updatedAttributes.toSupabaseRow(cafeId = cafeId, updatedBy = user.id)

        val savedRow = supabase
            .from(CAFE_CROWD_ATTRIBUTES_TABLE)
            .upsert(rowToSave) {
                onConflict = "cafe_id"
                select()
            }
            .decodeSingle<CafeCrowdAttributesRow>()

        attributesByCafeId[cafeId] = savedRow.toCafeCrowdAttributes()
    }

    fun applySuggestion(
        cafeId: String,
        suggestion: CrowdAttributeSuggestion,
        nowMillis: Long = System.currentTimeMillis()
    ): CafeCrowdAttributes {
        val updatedAttributes = mergedAttributesFor(cafeId, suggestion, nowMillis)
        attributesByCafeId[cafeId] = updatedAttributes
        return updatedAttributes
    }

    private fun mergedAttributesFor(
        cafeId: String,
        suggestion: CrowdAttributeSuggestion,
        nowMillis: Long
    ): CafeCrowdAttributes {
        val current = attributesFor(cafeId)
        return current.copy(
            outletAvailability = suggestion.outletAvailability ?: current.outletAvailability,
            wifiName = suggestion.wifiName?.normalizedText() ?: current.wifiName,
            wifiSpeed = suggestion.wifiSpeed ?: current.wifiSpeed,
            wifiPassword = suggestion.wifiPassword?.normalized() ?: current.wifiPassword,
            bathroomAvailability = suggestion.bathroomAvailability ?: current.bathroomAvailability,
            bathroomCode = suggestion.bathroomCode?.normalized() ?: current.bathroomCode,
            seatingAvailability = suggestion.seatingAvailability ?: current.seatingAvailability,
            seatingSpace = suggestion.seatingSpace ?: current.seatingSpace,
            seatingComfort = suggestion.seatingComfort ?: current.seatingComfort,
            crowdLevel = suggestion.crowdLevel ?: current.crowdLevel,
            noiseLevel = suggestion.noiseLevel ?: current.noiseLevel,
            vibeTags = suggestion.vibeTags ?: current.vibeTags,
            petFriendly = suggestion.petFriendly ?: current.petFriendly,
            cleanlinessRating = suggestion.cleanlinessRating ?: current.cleanlinessRating,
            seatingPhotoUris = mergeUriDrafts(current.seatingPhotoUris, suggestion.seatingPhotoUris),
            menuPhotoUris = mergeUriDrafts(current.menuPhotoUris, suggestion.menuPhotoUris),
            lastUpdatedEpochMillis = nowMillis
        )
    }

    internal fun resetForTest() {
        attributesByCafeId.clear()
    }

    private fun ProtectedCrowdSecret.normalized(): ProtectedCrowdSecret {
        val cleanedValue = value?.trim()?.takeIf { it.isNotBlank() }
        return copy(
            value = cleanedValue,
            knownToExist = knownToExist || cleanedValue != null
        )
    }

    private fun String.normalizedText(): String? {
        return trim().takeIf { it.isNotBlank() }
    }

    private fun mergeUriDrafts(current: List<String>, drafts: List<String>): List<String> {
        val cleanedDrafts = drafts.mapNotNull { uri -> uri.trim().takeIf { it.isNotBlank() } }
        return (current + cleanedDrafts).distinct()
    }
}

private fun CafeCrowdAttributesRow.toCafeCrowdAttributes(): CafeCrowdAttributes {
    return CafeCrowdAttributes(
        outletAvailability = enumValueOrDefault(outlet_availability, OutletAvailability.UNKNOWN),
        wifiName = wifi_name?.trim()?.takeIf { it.isNotBlank() },
        wifiSpeed = enumValueOrDefault(wifi_speed, WifiSpeed.UNKNOWN),
        wifiPassword = protectedSecretFromRow(wifi_password, wifi_password_known_to_exist),
        bathroomAvailability = enumValueOrDefault(bathroom_availability, BathroomAvailability.UNKNOWN),
        bathroomCode = protectedSecretFromRow(bathroom_code, bathroom_code_known_to_exist),
        seatingAvailability = enumValueOrDefault(seating_availability, SeatingAvailability.UNKNOWN),
        seatingSpace = enumValueOrDefault(seating_space, SeatingSpace.UNKNOWN),
        seatingComfort = enumValueOrDefault(seating_comfort, SeatingComfort.UNKNOWN),
        crowdLevel = enumValueOrDefault(crowd_level, CrowdLevel.UNKNOWN),
        noiseLevel = enumValueOrDefault(noise_level, NoiseLevel.UNKNOWN),
        vibeTags = vibe_tags.mapNotNull { enumValueOrNull<VibeTag>(it) }.toSet(),
        petFriendly = enumValueOrDefault(pet_friendly, PetFriendly.UNKNOWN),
        cleanlinessRating = enumValueOrDefault(cleanliness_rating, CleanlinessRating.UNKNOWN),
        seatingPhotoUris = seating_photo_urls.cleanedDistinctValues(),
        menuPhotoUris = menu_photo_urls.cleanedDistinctValues(),
        lastUpdatedEpochMillis = last_updated_epoch_millis
    )
}

private fun CafeCrowdAttributes.toSupabaseRow(
    cafeId: String,
    updatedBy: String
): CafeCrowdAttributesRow {
    return CafeCrowdAttributesRow(
        cafe_id = cafeId,
        outlet_availability = outletAvailability.name,
        wifi_name = wifiName?.trim()?.takeIf { it.isNotBlank() },
        wifi_speed = wifiSpeed.name,
        wifi_password = wifiPassword.value?.trim()?.takeIf { it.isNotBlank() },
        wifi_password_known_to_exist = wifiPassword.knownToExist || !wifiPassword.value.isNullOrBlank(),
        bathroom_availability = bathroomAvailability.name,
        bathroom_code = bathroomCode.value?.trim()?.takeIf { it.isNotBlank() },
        bathroom_code_known_to_exist = bathroomCode.knownToExist || !bathroomCode.value.isNullOrBlank(),
        seating_availability = seatingAvailability.name,
        seating_space = seatingSpace.name,
        seating_comfort = seatingComfort.name,
        crowd_level = crowdLevel.name,
        noise_level = noiseLevel.name,
        vibe_tags = vibeTags.map { it.name }.sorted(),
        pet_friendly = petFriendly.name,
        cleanliness_rating = cleanlinessRating.name,
        seating_photo_urls = seatingPhotoUris.cleanedDistinctValues(),
        menu_photo_urls = menuPhotoUris.cleanedDistinctValues(),
        last_updated_epoch_millis = lastUpdatedEpochMillis,
        updated_by = updatedBy
    )
}

private fun protectedSecretFromRow(value: String?, knownToExist: Boolean): ProtectedCrowdSecret {
    val cleanedValue = value?.trim()?.takeIf { it.isNotBlank() }
    return ProtectedCrowdSecret(
        value = cleanedValue,
        knownToExist = knownToExist || cleanedValue != null
    )
}

private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, defaultValue: T): T {
    return value?.let { enumValueOrNull<T>(it) } ?: defaultValue
}

private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? {
    return runCatching { enumValueOf<T>(value) }.getOrNull()
}

private fun List<String>.cleanedDistinctValues(): List<String> {
    return mapNotNull { value -> value.trim().takeIf { it.isNotBlank() } }.distinct()
}
