package com.example.shuffle_cafe

import com.google.android.gms.maps.model.LatLng
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CrowdAttributeRepositoryTest {

    @Before
    fun setUp() {
        CrowdAttributeRepository.resetForTest()
    }

    @After
    fun tearDown() {
        CrowdAttributeRepository.resetForTest()
    }

    @Test
    fun nearbyGateAllowsUsersWithinOneHundredMetersOnly() {
        val cafe = LatLng(34.1000, -117.3000)
        val nearbyUser = LatLng(34.1005, -117.3002)
        val farUser = LatLng(34.1020, -117.3000)

        assertTrue(isUserNearCafe(nearbyUser, cafe))
        assertFalse(isUserNearCafe(farUser, cafe))
        assertFalse(isUserNearCafe(null, cafe))
        assertFalse(isUserNearCafe(nearbyUser, null))
    }

    @Test
    fun protectedSecretDisplayStatesCoverCensoredRevealAskStaffAndAvailability() {
        val secret = ProtectedCrowdSecret(value = "bean-fi-42")

        assertEquals(
            ProtectedSecretDisplayState.Censored,
            protectedSecretDisplayState(secret, isNearby = false, isAvailable = true)
        )
        assertEquals(
            ProtectedSecretDisplayState.Revealed("bean-fi-42"),
            protectedSecretDisplayState(secret, isNearby = true, isAvailable = true)
        )
        assertEquals(
            ProtectedSecretDisplayState.AskStaff,
            protectedSecretDisplayState(
                ProtectedCrowdSecret(knownToExist = true),
                isNearby = true,
                isAvailable = true
            )
        )
        assertEquals(
            ProtectedSecretDisplayState.Unavailable,
            protectedSecretDisplayState(secret, isNearby = true, isAvailable = false)
        )
        assertEquals(
            ProtectedSecretDisplayState.Unknown,
            protectedSecretDisplayState(ProtectedCrowdSecret(), isNearby = true, isAvailable = null)
        )
    }

    @Test
    fun suggestionUpdatesOnlyTargetCafe() {
        CrowdAttributeRepository.applySuggestion(
            cafeId = "alpha",
            suggestion = CrowdAttributeSuggestion(
                outletAvailability = OutletAvailability.PLENTY,
                wifiPassword = ProtectedCrowdSecret(value = "alpha-pass"),
                vibeTags = setOf(VibeTag.STUDY_HEAVY),
                seatingPhotoUris = listOf(" content://alpha-seat ")
            ),
            nowMillis = 100L
        )
        CrowdAttributeRepository.applySuggestion(
            cafeId = "bravo",
            suggestion = CrowdAttributeSuggestion(
                noiseLevel = NoiseLevel.QUIET,
                menuPhotoUris = listOf("content://bravo-menu")
            ),
            nowMillis = 200L
        )

        val alpha = CrowdAttributeRepository.attributesFor("alpha")
        val bravo = CrowdAttributeRepository.attributesFor("bravo")

        assertEquals(OutletAvailability.PLENTY, alpha.outletAvailability)
        assertEquals(ProtectedCrowdSecret(value = "alpha-pass", knownToExist = true), alpha.wifiPassword)
        assertEquals(setOf(VibeTag.STUDY_HEAVY), alpha.vibeTags)
        assertEquals(listOf("content://alpha-seat"), alpha.seatingPhotoUris)
        assertEquals(NoiseLevel.UNKNOWN, alpha.noiseLevel)
        assertEquals(100L, alpha.lastUpdatedEpochMillis)

        assertEquals(OutletAvailability.UNKNOWN, bravo.outletAvailability)
        assertEquals(NoiseLevel.QUIET, bravo.noiseLevel)
        assertEquals(listOf("content://bravo-menu"), bravo.menuPhotoUris)
        assertEquals(200L, bravo.lastUpdatedEpochMillis)
    }

    @Test
    fun cachedCafeConversionRemainsBackwardCompatible() {
        val cachedCafe = CachedCafeDto(
            id = "cached",
            name = "Cached Cafe",
            address = "123 Bean Street",
            phone = "555-0100",
            status = "Open",
            hours = linkedMapOf("Monday" to "7:00 AM - 5:00 PM"),
            features = listOf("Coffee house"),
            ambience = listOf("Coffee")
        )

        val cafe = cachedCafe.toCafe(distanceReference = null)

        assertEquals("cached", cafe.id)
        assertEquals(listOf("Coffee house"), cafe.features)
        assertEquals(CafeCrowdAttributes(), CrowdAttributeRepository.attributesFor(cafe.id))
    }
}
