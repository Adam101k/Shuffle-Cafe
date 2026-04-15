package com.example.shuffle_cafe

import com.google.android.gms.maps.model.LatLng
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CafeRepositoryStateTest {

    @Before
    fun setUp() {
        CafeRepository.resetForTest()
    }

    @After
    fun tearDown() {
        CafeRepository.resetForTest()
    }

    @Test
    fun mapFeedUpdatesDoNotChangeHomeFeed() {
        CafeRepository.setHomeFeedForTest(
            cafes = listOf(testCafe(id = "home_alpha"))
        )

        CafeRepository.setMapFeedForTest(
            cafes = listOf(testCafe(id = "map_bravo"))
        )

        assertEquals(listOf("home_alpha"), CafeRepository.homeUiState.cafes.map { it.id })
        assertEquals(listOf("map_bravo"), CafeRepository.mapUiState.cafes.map { it.id })
    }

    @Test
    fun homeFeedUpdatesDoNotChangeMapFeed() {
        CafeRepository.setMapFeedForTest(
            cafes = listOf(testCafe(id = "map_alpha"))
        )

        CafeRepository.setHomeFeedForTest(
            cafes = listOf(testCafe(id = "home_bravo"))
        )

        assertEquals(listOf("map_alpha"), CafeRepository.mapUiState.cafes.map { it.id })
        assertEquals(listOf("home_bravo"), CafeRepository.homeUiState.cafes.map { it.id })
    }

    @Test
    fun mapOnlyCafeCanBeResolvedById() {
        CafeRepository.setMapFeedForTest(
            cafes = listOf(testCafe(id = "map_only"))
        )

        val resolvedCafe = CafeRepository.getCafe("map_only")

        assertNotNull(resolvedCafe)
        assertEquals("map_only", resolvedCafe?.id)
    }

    @Test
    fun updatingSharedCafePatchesExistingFeedsWithoutChangingMembership() {
        CafeRepository.setHomeFeedForTest(
            cafes = listOf(
                testCafe(id = "shared", status = "Before"),
                testCafe(id = "home_only")
            )
        )
        CafeRepository.setMapFeedForTest(
            cafes = listOf(
                testCafe(id = "shared", status = "Before"),
                testCafe(id = "map_only")
            )
        )

        CafeRepository.updateCafeForTest("shared") { cafe ->
            cafe.copy(status = "Updated")
        }

        assertEquals(listOf("shared", "home_only"), CafeRepository.homeUiState.cafes.map { it.id })
        assertEquals(listOf("shared", "map_only"), CafeRepository.mapUiState.cafes.map { it.id })
        assertEquals("Updated", CafeRepository.homeUiState.cafes.first { it.id == "shared" }.status)
        assertEquals("Updated", CafeRepository.mapUiState.cafes.first { it.id == "shared" }.status)
        assertEquals("home_only", CafeRepository.homeUiState.cafes.last().id)
        assertEquals("map_only", CafeRepository.mapUiState.cafes.last().id)
    }

    @Test
    fun mapAreaFilteringUsesCafeCoordinatesInsteadOfDisplayedDistance() {
        val searchCenter = LatLng(34.1000, -117.3000)
        val cafes = listOf(
            testCafe(
                id = "nearby",
                latLng = LatLng(34.1008, -117.3004),
                distanceMeters = 90_000f
            ),
            testCafe(
                id = "far",
                latLng = LatLng(34.1400, -117.3400),
                distanceMeters = 10f
            )
        )

        val filteredIds = cafes
            .filter { cafe -> isCafeWithinMapArea(cafe.latLng, searchCenter, 500f) }
            .map { it.id }

        assertEquals(listOf("nearby"), filteredIds)
    }

    @Test
    fun cachedCafeDistancesAreRecomputedFromReferenceLocation() {
        val cachedCafe = CachedCafeDto(
            id = "cached",
            name = "Cached Cafe",
            address = "123 Bean Street",
            phone = "555-0100",
            status = "Open",
            hours = linkedMapOf("Monday" to "7:00 AM - 5:00 PM"),
            features = listOf("Coffee house"),
            ambience = listOf("Coffee"),
            rating = 4.6f,
            userRatingCount = 120,
            latitude = 34.1008,
            longitude = -117.3004
        )

        val nearbyReference = LatLng(34.1000, -117.3000)
        val fartherReference = LatLng(34.4000, -117.7000)

        val nearbyDistance = cachedCafe.toCafe(distanceReference = nearbyReference).distanceMeters
        val fartherDistance = cachedCafe.toCafe(distanceReference = fartherReference).distanceMeters

        assertNotNull(nearbyDistance)
        assertNotNull(fartherDistance)
        assertTrue(fartherDistance!! > nearbyDistance!!)
        assertNull(cachedCafe.toCafe(distanceReference = null).distanceMeters)
    }

    @Test
    fun cachedCafeFallsBackToPersistedDistanceWhenReferenceLocationIsUnavailable() {
        val cachedCafe = CachedCafeDto(
            id = "persisted_distance",
            name = "Persisted Distance Cafe",
            address = "123 Bean Street",
            phone = "555-0100",
            status = "Open",
            hours = linkedMapOf("Monday" to "7:00 AM - 5:00 PM"),
            features = listOf("Coffee house"),
            ambience = listOf("Coffee"),
            distanceMeters = 1450f
        )

        val restoredCafe = cachedCafe.toCafe(distanceReference = null)

        assertEquals(1450f, restoredCafe.distanceMeters ?: 0f, 0.001f)
    }

    private fun testCafe(
        id: String,
        status: String = "Open",
        latLng: LatLng = LatLng(34.1000, -117.3000),
        distanceMeters: Float? = 1200f
    ): Cafe {
        return Cafe(
            id = id,
            name = "Cafe $id",
            address = "123 Bean Street",
            phone = "555-0100",
            status = status,
            hours = linkedMapOf("Monday" to "7:00 AM - 5:00 PM"),
            features = listOf("Coffee house"),
            ambience = listOf("Coffee"),
            distanceMeters = distanceMeters,
            latLng = latLng
        )
    }
}
