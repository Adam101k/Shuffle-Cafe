package com.example.shuffle_cafe

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

class CafeAttributeFiltersTest {

    @Test
    fun cleanlinessPoorIncludesAllKnownCleanlinessValues() {
        val filter = CafeAttributeFilters(cleanlinessRating = CleanlinessRating.POOR)

        assertTrue(testCafe().matches(filter, CafeCrowdAttributes(cleanlinessRating = CleanlinessRating.POOR)))
        assertTrue(testCafe().matches(filter, CafeCrowdAttributes(cleanlinessRating = CleanlinessRating.OKAY)))
        assertTrue(testCafe().matches(filter, CafeCrowdAttributes(cleanlinessRating = CleanlinessRating.GOOD)))
        assertTrue(testCafe().matches(filter, CafeCrowdAttributes(cleanlinessRating = CleanlinessRating.GREAT)))
        assertFalse(testCafe().matches(filter, CafeCrowdAttributes(cleanlinessRating = CleanlinessRating.UNKNOWN)))
    }

    @Test
    fun studyFriendlyCrowdOrderingTreatsLighterCrowdsAsHigher() {
        val moderateOrBetter = CafeAttributeFilters(crowdLevel = CrowdLevel.MODERATE)

        assertTrue(testCafe().matches(moderateOrBetter, CafeCrowdAttributes(crowdLevel = CrowdLevel.EMPTY)))
        assertTrue(testCafe().matches(moderateOrBetter, CafeCrowdAttributes(crowdLevel = CrowdLevel.LIGHT)))
        assertTrue(testCafe().matches(moderateOrBetter, CafeCrowdAttributes(crowdLevel = CrowdLevel.MODERATE)))
        assertFalse(testCafe().matches(moderateOrBetter, CafeCrowdAttributes(crowdLevel = CrowdLevel.BUSY)))
        assertFalse(testCafe().matches(moderateOrBetter, CafeCrowdAttributes(crowdLevel = CrowdLevel.PACKED)))
    }

    @Test
    fun studyFriendlyNoiseOrderingTreatsQuieterNoiseAsHigher() {
        val quietOrBetter = CafeAttributeFilters(noiseLevel = NoiseLevel.QUIET)

        assertTrue(testCafe().matches(quietOrBetter, CafeCrowdAttributes(noiseLevel = NoiseLevel.SILENT)))
        assertTrue(testCafe().matches(quietOrBetter, CafeCrowdAttributes(noiseLevel = NoiseLevel.QUIET)))
        assertFalse(testCafe().matches(quietOrBetter, CafeCrowdAttributes(noiseLevel = NoiseLevel.MODERATE)))
        assertFalse(testCafe().matches(quietOrBetter, CafeCrowdAttributes(noiseLevel = NoiseLevel.LOUD)))
        assertFalse(testCafe().matches(quietOrBetter, CafeCrowdAttributes(noiseLevel = NoiseLevel.VERY_LOUD)))
    }

    @Test
    fun bathroomAndPetFiltersMatchExactValues() {
        val exactFilters = CafeAttributeFilters(
            bathroomAvailability = BathroomAvailability.AVAILABLE,
            petFriendly = PetFriendly.PATIO_ONLY
        )

        assertTrue(
            testCafe().matches(
                exactFilters,
                CafeCrowdAttributes(
                    bathroomAvailability = BathroomAvailability.AVAILABLE,
                    petFriendly = PetFriendly.PATIO_ONLY
                )
            )
        )
        assertFalse(
            testCafe().matches(
                exactFilters,
                CafeCrowdAttributes(
                    bathroomAvailability = BathroomAvailability.NONE,
                    petFriendly = PetFriendly.PATIO_ONLY
                )
            )
        )
        assertFalse(
            testCafe().matches(
                exactFilters,
                CafeCrowdAttributes(
                    bathroomAvailability = BathroomAvailability.AVAILABLE,
                    petFriendly = PetFriendly.INDOOR_ALLOWED
                )
            )
        )
    }

    @Test
    fun unknownValuesAreExcludedOnlyWhenRelatedFilterIsActive() {
        val cafe = testCafe()

        assertTrue(
            cafeMatchesAttributeFilters(
                cafe = cafe,
                attributes = CafeCrowdAttributes(),
                filters = CafeAttributeFilters()
            )
        )
        assertFalse(
            cafeMatchesAttributeFilters(
                cafe = cafe,
                attributes = CafeCrowdAttributes(),
                filters = CafeAttributeFilters(wifiSpeed = WifiSpeed.SLOW)
            )
        )
        assertTrue(
            cafeMatchesAttributeFilters(
                cafe = cafe,
                attributes = CafeCrowdAttributes(bathroomAvailability = BathroomAvailability.AVAILABLE),
                filters = CafeAttributeFilters(bathroomAvailability = BathroomAvailability.AVAILABLE)
            )
        )
    }

    @Test
    fun openNowParsingHandlesOpenClosedUnavailableAndTwentyFourHours() {
        val wednesdayMorning = testCalendar(
            year = 2026,
            month = Calendar.APRIL,
            day = 22,
            hour = 10,
            minute = 30
        )

        assertEquals(
            CafeOpenState.OPEN,
            openStateForHours(linkedMapOf("Wednesday" to "8:00 AM - 5:00 PM"), wednesdayMorning)
        )
        assertEquals(
            CafeOpenState.CLOSED,
            openStateForHours(linkedMapOf("Wednesday" to "4:00 PM - 9:00 PM"), wednesdayMorning)
        )
        assertEquals(
            CafeOpenState.UNKNOWN,
            openStateForHours(linkedMapOf("Wednesday" to "Hours unavailable"), wednesdayMorning)
        )
        assertEquals(
            CafeOpenState.OPEN,
            openStateForHours(linkedMapOf("Wednesday" to "Open 24 hours"), wednesdayMorning)
        )
    }

    @Test
    fun openNowFilterHidesUnknownHoursWhenActive() {
        val wednesdayMorning = testCalendar(
            year = 2026,
            month = Calendar.APRIL,
            day = 22,
            hour = 10,
            minute = 30
        )

        assertTrue(
            cafeMatchesAttributeFilters(
                cafe = testCafe(hours = linkedMapOf("Wednesday" to "8:00 AM - 5:00 PM")),
                attributes = CafeCrowdAttributes(),
                filters = CafeAttributeFilters(openNow = OpenNowFilter.YES),
                now = wednesdayMorning
            )
        )
        assertTrue(
            cafeMatchesAttributeFilters(
                cafe = testCafe(hours = linkedMapOf("Wednesday" to "4:00 PM - 9:00 PM")),
                attributes = CafeCrowdAttributes(),
                filters = CafeAttributeFilters(openNow = OpenNowFilter.NO),
                now = wednesdayMorning
            )
        )
        assertFalse(
            cafeMatchesAttributeFilters(
                cafe = testCafe(hours = linkedMapOf("Wednesday" to "Hours unavailable")),
                attributes = CafeCrowdAttributes(),
                filters = CafeAttributeFilters(openNow = OpenNowFilter.YES),
                now = wednesdayMorning
            )
        )
    }

    private fun Cafe.matches(filters: CafeAttributeFilters, attributes: CafeCrowdAttributes): Boolean {
        return cafeMatchesAttributeFilters(
            cafe = this,
            attributes = attributes,
            filters = filters,
            now = testCalendar(
                year = 2026,
                month = Calendar.APRIL,
                day = 22,
                hour = 10,
                minute = 30
            )
        )
    }

    private fun testCafe(
        hours: LinkedHashMap<String, String> = linkedMapOf("Wednesday" to "8:00 AM - 5:00 PM")
    ): Cafe {
        return Cafe(
            id = "test-cafe",
            name = "Test Cafe",
            address = "123 Bean Street",
            phone = "555-0100",
            status = "Open",
            hours = hours,
            features = emptyList(),
            ambience = emptyList()
        )
    }

    private fun testCalendar(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int
    ): Calendar {
        return GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US).apply {
            clear()
            set(year, month, day, hour, minute)
        }
    }
}
