package com.example.shuffle_cafe

import androidx.compose.runtime.mutableStateMapOf
import com.google.android.gms.maps.model.LatLng

private const val DEFAULT_NEARBY_THRESHOLD_METERS = 100f

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

    if (isAvailable == null && normalizedValue.isBlank() && !secret.knownToExist) {
        return ProtectedSecretDisplayState.Unknown
    }

    if (!isNearby) {
        return ProtectedSecretDisplayState.Censored
    }

    if (normalizedValue.isNotBlank()) {
        return ProtectedSecretDisplayState.Revealed(normalizedValue)
    }

    return if (secret.knownToExist || isAvailable == true) {
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

    fun applySuggestion(
        cafeId: String,
        suggestion: CrowdAttributeSuggestion,
        nowMillis: Long = System.currentTimeMillis()
    ) {
        val current = attributesFor(cafeId)
        attributesByCafeId[cafeId] = current.copy(
            outletAvailability = suggestion.outletAvailability ?: current.outletAvailability,
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

    private fun mergeUriDrafts(current: List<String>, drafts: List<String>): List<String> {
        val cleanedDrafts = drafts.mapNotNull { uri -> uri.trim().takeIf { it.isNotBlank() } }
        return (current + cleanedDrafts).distinct()
    }
}
