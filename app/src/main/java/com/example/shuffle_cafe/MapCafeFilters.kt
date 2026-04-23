package com.example.shuffle_cafe

import java.util.Calendar
import java.util.Locale

internal enum class OpenNowFilter(val label: String) {
    ANY("Any"),
    YES("Yes"),
    NO("No")
}

internal enum class CafeOpenState {
    OPEN,
    CLOSED,
    UNKNOWN
}

internal data class CafeAttributeFilters(
    val openNow: OpenNowFilter = OpenNowFilter.ANY,
    val wifiSpeed: WifiSpeed? = null,
    val bathroomAvailability: BathroomAvailability? = null,
    val seatingAvailability: SeatingAvailability? = null,
    val seatingComfort: SeatingComfort? = null,
    val crowdLevel: CrowdLevel? = null,
    val noiseLevel: NoiseLevel? = null,
    val petFriendly: PetFriendly? = null,
    val cleanlinessRating: CleanlinessRating? = null
) {
    val activeCount: Int
        get() = listOf(
            openNow.takeUnless { it == OpenNowFilter.ANY },
            wifiSpeed,
            bathroomAvailability,
            seatingAvailability,
            seatingComfort,
            crowdLevel,
            noiseLevel,
            petFriendly,
            cleanlinessRating
        ).count { it != null }

    val hasActiveFilters: Boolean
        get() = activeCount > 0
}

internal fun cafeMatchesAttributeFilters(
    cafe: Cafe,
    attributes: CafeCrowdAttributes,
    filters: CafeAttributeFilters,
    now: Calendar = Calendar.getInstance(Locale.US)
): Boolean {
    if (!matchesOpenNowFilter(cafe, filters.openNow, now)) return false

    filters.wifiSpeed?.let { selected ->
        if (!selectedOrBetter(selected.wifiStudyRank(), attributes.wifiSpeed.wifiStudyRank())) return false
    }
    filters.bathroomAvailability?.let { selected ->
        if (attributes.bathroomAvailability != selected) return false
    }
    filters.seatingAvailability?.let { selected ->
        if (!selectedOrBetter(selected.seatingStudyRank(), attributes.seatingAvailability.seatingStudyRank())) return false
    }
    filters.seatingComfort?.let { selected ->
        if (!selectedOrBetter(selected.comfortStudyRank(), attributes.seatingComfort.comfortStudyRank())) return false
    }
    filters.crowdLevel?.let { selected ->
        if (!selectedOrBetter(selected.crowdStudyRank(), attributes.crowdLevel.crowdStudyRank())) return false
    }
    filters.noiseLevel?.let { selected ->
        if (!selectedOrBetter(selected.noiseStudyRank(), attributes.noiseLevel.noiseStudyRank())) return false
    }
    filters.petFriendly?.let { selected ->
        if (attributes.petFriendly != selected) return false
    }
    filters.cleanlinessRating?.let { selected ->
        if (!selectedOrBetter(selected.cleanlinessStudyRank(), attributes.cleanlinessRating.cleanlinessStudyRank())) return false
    }

    return true
}

internal fun openStateForCafe(
    cafe: Cafe,
    now: Calendar = Calendar.getInstance(Locale.US)
): CafeOpenState {
    return openStateForHours(cafe.hours, now)
}

internal fun openStateForHours(
    hours: Map<String, String>,
    now: Calendar = Calendar.getInstance(Locale.US)
): CafeOpenState {
    if (hours.isEmpty()) return CafeOpenState.UNKNOWN

    val todayName = now.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US).orEmpty()
    val todaysHours = hours.entries
        .firstOrNull { (day, _) -> day.equals(todayName, ignoreCase = true) }
        ?.value
        ?: hours.entries.singleOrNull { (day, _) -> day.equals("Hours", ignoreCase = true) }?.value
        ?: return CafeOpenState.UNKNOWN

    return openStateForHoursText(todaysHours, now)
}

private fun matchesOpenNowFilter(
    cafe: Cafe,
    filter: OpenNowFilter,
    now: Calendar
): Boolean {
    if (filter == OpenNowFilter.ANY) return true

    return when (openStateForCafe(cafe, now)) {
        CafeOpenState.OPEN -> filter == OpenNowFilter.YES
        CafeOpenState.CLOSED -> filter == OpenNowFilter.NO
        CafeOpenState.UNKNOWN -> false
    }
}

private fun openStateForHoursText(
    hoursText: String,
    now: Calendar
): CafeOpenState {
    val normalized = hoursText
        .replace('\u2012', '-')
        .replace('\u2013', '-')
        .replace('\u2014', '-')
        .replace('\u2212', '-')
        .replace('\u202f', ' ')
        .replace('\u00a0', ' ')
        .trim()
        .lowercase(Locale.US)

    if (normalized.isBlank() || normalized.contains("unavailable")) return CafeOpenState.UNKNOWN
    if (normalized.contains("open 24") || normalized.contains("24 hours")) return CafeOpenState.OPEN
    if (normalized == "closed" || normalized.contains("closed")) return CafeOpenState.CLOSED

    val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    val ranges = normalized
        .split(",", ";")
        .map { segment -> segment.trim() }
        .filter { it.isNotBlank() }

    var parsedAnyRange = false
    ranges.forEach { range ->
        val parts = range.split("-", limit = 2).map { part -> part.trim() }
        if (parts.size != 2) return@forEach

        val start = parseTimeOfDayMinutes(parts[0]) ?: return@forEach
        val end = parseTimeOfDayMinutes(parts[1]) ?: return@forEach
        parsedAnyRange = true

        if (isMinuteWithinRange(nowMinutes, start, end)) {
            return CafeOpenState.OPEN
        }
    }

    return if (parsedAnyRange) CafeOpenState.CLOSED else CafeOpenState.UNKNOWN
}

private fun parseTimeOfDayMinutes(value: String): Int? {
    val match = Regex("""(?i)^\s*(\d{1,2})(?::(\d{2}))?\s*([ap])\.?\s*m\.?\s*$""")
        .matchEntire(value)
        ?: return null
    val hour = match.groupValues[1].toIntOrNull() ?: return null
    val minute = match.groupValues[2].takeIf { it.isNotBlank() }?.toIntOrNull() ?: 0
    val meridiem = match.groupValues[3].lowercase(Locale.US)

    if (hour !in 1..12 || minute !in 0..59) return null

    val hour24 = when {
        meridiem == "a" && hour == 12 -> 0
        meridiem == "a" -> hour
        meridiem == "p" && hour == 12 -> 12
        else -> hour + 12
    }
    return hour24 * 60 + minute
}

private fun isMinuteWithinRange(nowMinutes: Int, startMinutes: Int, endMinutes: Int): Boolean {
    return when {
        startMinutes == endMinutes -> true
        startMinutes < endMinutes -> nowMinutes >= startMinutes && nowMinutes < endMinutes
        else -> nowMinutes >= startMinutes || nowMinutes < endMinutes
    }
}

private fun selectedOrBetter(selectedRank: Int?, actualRank: Int?): Boolean {
    return selectedRank != null && actualRank != null && actualRank >= selectedRank
}

private fun WifiSpeed.wifiStudyRank(): Int? {
    return when (this) {
        WifiSpeed.FAST -> 3
        WifiSpeed.DECENT -> 2
        WifiSpeed.SLOW -> 1
        WifiSpeed.NONE,
        WifiSpeed.UNKNOWN -> null
    }
}

private fun SeatingAvailability.seatingStudyRank(): Int? {
    return when (this) {
        SeatingAvailability.PLENTY -> 3
        SeatingAvailability.FAIR -> 2
        SeatingAvailability.SCARCE -> 1
        SeatingAvailability.NONE,
        SeatingAvailability.UNKNOWN -> null
    }
}

private fun SeatingComfort.comfortStudyRank(): Int? {
    return when (this) {
        SeatingComfort.VERY_COMFORTABLE -> 4
        SeatingComfort.COMFORTABLE -> 3
        SeatingComfort.OKAY -> 2
        SeatingComfort.UNCOMFORTABLE -> 1
        SeatingComfort.UNKNOWN -> null
    }
}

private fun CrowdLevel.crowdStudyRank(): Int? {
    return when (this) {
        CrowdLevel.EMPTY -> 5
        CrowdLevel.LIGHT -> 4
        CrowdLevel.MODERATE -> 3
        CrowdLevel.BUSY -> 2
        CrowdLevel.PACKED -> 1
        CrowdLevel.UNKNOWN -> null
    }
}

private fun NoiseLevel.noiseStudyRank(): Int? {
    return when (this) {
        NoiseLevel.SILENT -> 5
        NoiseLevel.QUIET -> 4
        NoiseLevel.MODERATE -> 3
        NoiseLevel.LOUD -> 2
        NoiseLevel.VERY_LOUD -> 1
        NoiseLevel.UNKNOWN -> null
    }
}

private fun CleanlinessRating.cleanlinessStudyRank(): Int? {
    return when (this) {
        CleanlinessRating.GREAT -> 4
        CleanlinessRating.GOOD -> 3
        CleanlinessRating.OKAY -> 2
        CleanlinessRating.POOR -> 1
        CleanlinessRating.UNKNOWN -> null
    }
}
