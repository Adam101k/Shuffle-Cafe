package com.example.shuffle_cafe

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.PhotoMetadata
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
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

    @Test
    fun cachedCafeEnvelopeIsFreshOnlyWithinThirtyMinuteWindow() {
        val savedAt = 10_000L
        val envelope = CachedCafeEnvelope(
            cityKey = "cache_window",
            savedAtEpochMillis = savedAt,
            cafes = emptyList()
        )

        assertTrue(envelope.isFresh(nowMillis = savedAt + CAFE_RESULT_CACHE_TTL_MILLIS - 1))
        assertFalse(envelope.isFresh(nowMillis = savedAt + CAFE_RESULT_CACHE_TTL_MILLIS))
    }

    @Test
    fun loadedHomeFeedStaysReusableOnlyWhileFreshAndIdle() {
        val savedAt = 10_000L
        val loadedFeed = CafeFeedUiState(
            cafes = listOf(testCafe(id = "fresh")),
            isLoading = false,
            isRefreshing = false,
            lastUpdatedEpochMillis = savedAt
        )

        assertTrue(loadedFeed.hasFreshLoadedCafes(nowMillis = savedAt + CAFE_RESULT_CACHE_TTL_MILLIS - 1))
        assertFalse(loadedFeed.copy(isRefreshing = true).hasFreshLoadedCafes(nowMillis = savedAt + 1))
        assertFalse(loadedFeed.copy(isLoading = true).hasFreshLoadedCafes(nowMillis = savedAt + 1))
        assertFalse(loadedFeed.copy(cafes = emptyList()).hasFreshLoadedCafes(nowMillis = savedAt + 1))
        assertFalse(loadedFeed.hasFreshLoadedCafes(nowMillis = savedAt + CAFE_RESULT_CACHE_TTL_MILLIS))
    }

    @Test
    fun cachedCafeEnvelopeRequiresImageDataBeforeSkippingCardRefresh() {
        val envelopeWithoutImages = CachedCafeEnvelope(
            cityKey = "missing_images",
            savedAtEpochMillis = 10_000L,
            cafes = listOf(
                CachedCafeDto(
                    id = "missing",
                    name = "Missing Image Cafe",
                    address = "123 Bean Street",
                    phone = "555-0100",
                    status = "Open",
                    hours = linkedMapOf("Monday" to "7:00 AM - 5:00 PM"),
                    features = listOf("Coffee house"),
                    ambience = listOf("Coffee"),
                    expectedPhotoCount = 1
                )
            )
        )
        val envelopeWithImages = envelopeWithoutImages.copy(
            cafes = listOf(
                envelopeWithoutImages.cafes.single().copy(
                    heroImageBase64 = "not-real-image-data"
                )
            )
        )

        assertFalse(envelopeWithoutImages.hasCompleteCachedCardImages())
        assertTrue(envelopeWithImages.hasCompleteCachedCardImages())
    }

    @Test
    fun detailEnrichmentUpdatesExistingCafeWithoutChangingFeedMembership() {
        CafeRepository.setHomeFeedForTest(
            cafes = listOf(
                testCafe(id = "shared", phone = "Phone unavailable", address = "Address unavailable"),
                testCafe(id = "home_only")
            )
        )
        CafeRepository.setMapFeedForTest(
            cafes = listOf(
                testCafe(id = "shared", phone = "Phone unavailable", address = "Address unavailable"),
                testCafe(id = "map_only")
            )
        )

        CafeRepository.updateCafeDetails(
            cafeId = "shared",
            detailCafe = testCafe(
                id = "shared",
                phone = "+1 555-0199",
                address = "999 Detail Lane",
                rating = 4.8f,
                userRatingCount = 42
            )
        )

        assertEquals(listOf("shared", "home_only"), CafeRepository.homeUiState.cafes.map { it.id })
        assertEquals(listOf("shared", "map_only"), CafeRepository.mapUiState.cafes.map { it.id })
        assertEquals("+1 555-0199", CafeRepository.homeUiState.cafes.first { it.id == "shared" }.phone)
        assertEquals("999 Detail Lane", CafeRepository.mapUiState.cafes.first { it.id == "shared" }.address)
        assertEquals("map_only", CafeRepository.mapUiState.cafes.last().id)
    }

    @Test
    fun detailEnrichmentPreservesExistingDistanceWhenDetailHasNoDistance() {
        CafeRepository.setHomeFeedForTest(
            cafes = listOf(testCafe(id = "distance", distanceMeters = 1450f))
        )

        CafeRepository.updateCafeDetails(
            cafeId = "distance",
            detailCafe = testCafe(id = "distance", distanceMeters = null, latLng = LatLng(35.0, -118.0))
        )

        assertEquals(1450f, CafeRepository.getCafe("distance")?.distanceMeters ?: 0f, 0.001f)
    }

    @Test
    fun detailEnrichmentPreservesLoadedPhotoBitmapsWhenMetadataIsRefreshed() {
        val loadedBitmap = fakeBitmap()
        CafeRepository.setHomeFeedForTest(
            cafes = listOf(
                testCafe(id = "photos").copy(
                    photoMetadatas = placeholderPhotoMetadatas(1),
                    photoBitmaps = listOf(loadedBitmap)
                )
            )
        )

        CafeRepository.updateCafeDetails(
            cafeId = "photos",
            detailCafe = testCafe(id = "photos").copy(
                photoMetadatas = placeholderPhotoMetadatas(2),
                photoBitmaps = emptyList()
            )
        )

        val updatedCafe = CafeRepository.getCafe("photos")
        assertEquals(2, updatedCafe?.photoMetadatas?.size)
        assertEquals(2, updatedCafe?.photoBitmaps?.size)
        assertSame(loadedBitmap, updatedCafe?.photoBitmaps?.first())
        assertNull(updatedCafe?.photoBitmaps?.get(1))
    }

    @Test
    fun missingDetailDetectionIdentifiesIncompleteAndCompleteCafes() {
        val completeCafe = testCafe(
            id = "complete",
            phone = "+1 555-0100",
            address = "123 Bean Street",
            rating = 4.4f,
            userRatingCount = 18
        ).copy(photoMetadatas = placeholderPhotoMetadatas(1))

        assertFalse(isCafeDetailDataIncomplete(completeCafe))
        assertTrue(isCafeDetailDataIncomplete(completeCafe.copy(phone = "Phone unavailable")))
        assertTrue(isCafeDetailDataIncomplete(completeCafe.copy(photoMetadatas = emptyList(), photoBitmaps = emptyList())))
        assertTrue(isCafeDetailDataIncomplete(completeCafe.copy(address = "Address unavailable")))
        assertTrue(isCafeDetailDataIncomplete(completeCafe.copy(latLng = null)))
        assertTrue(isCafeDetailDataIncomplete(completeCafe.copy(rating = null)))
        assertTrue(isCafeDetailDataIncomplete(completeCafe.copy(userRatingCount = null)))
        assertTrue(isCafeDetailDataIncomplete(completeCafe.copy(hours = linkedMapOf("Hours" to "Hours unavailable"))))
    }

    @Test
    fun cafePhoneSelectionPrefersInternationalNumber() {
        val phone = selectCafePhoneNumber(
            internationalPhoneNumber = "+1 555-0100",
            nationalPhoneNumber = "(555) 0100"
        )

        assertEquals("+1 555-0100", phone)
    }

    @Test
    fun cafePhoneSelectionFallsBackToNationalNumber() {
        val phone = selectCafePhoneNumber(
            internationalPhoneNumber = " ",
            nationalPhoneNumber = "(555) 0100"
        )

        assertEquals("(555) 0100", phone)
    }

    @Test
    fun cafePhoneSelectionUsesUnavailableTextWhenMissing() {
        val phone = selectCafePhoneNumber(
            internationalPhoneNumber = null,
            nationalPhoneNumber = ""
        )

        assertEquals("Phone unavailable", phone)
    }

    @Test
    fun resetForTestClearsMapSessionCache() {
        CafeRepository.cacheMapViewportForTest(
            viewportKey = "viewport_a",
            cafes = listOf(testCafe(id = "cached_map_ping"))
        )

        assertEquals(
            listOf("cached_map_ping"),
            CafeRepository.mapSessionCacheSnapshotForTest().map { cafe -> cafe.id }
        )

        CafeRepository.resetForTest()

        assertTrue(CafeRepository.mapSessionCacheSnapshotForTest().isEmpty())
    }

    private fun testCafe(
        id: String,
        status: String = "Open",
        address: String = "123 Bean Street",
        phone: String = "555-0100",
        latLng: LatLng = LatLng(34.1000, -117.3000),
        distanceMeters: Float? = 1200f,
        rating: Float? = 4.6f,
        userRatingCount: Int? = 120
    ): Cafe {
        return Cafe(
            id = id,
            name = "Cafe $id",
            address = address,
            phone = phone,
            status = status,
            hours = linkedMapOf("Monday" to "7:00 AM - 5:00 PM"),
            features = listOf("Coffee house"),
            ambience = listOf("Coffee"),
            rating = rating,
            userRatingCount = userRatingCount,
            distanceMeters = distanceMeters,
            latLng = latLng
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun placeholderPhotoMetadatas(count: Int): List<PhotoMetadata> {
        return List<PhotoMetadata?>(count) { null } as List<PhotoMetadata>
    }

    private fun fakeBitmap(): Bitmap {
        val unsafeClass = Class.forName("sun.misc.Unsafe")
        val unsafeField = unsafeClass.getDeclaredField("theUnsafe")
        unsafeField.isAccessible = true
        val unsafe = unsafeField.get(null)
        val allocateInstance = unsafeClass.getMethod("allocateInstance", Class::class.java)
        return allocateInstance.invoke(unsafe, Bitmap::class.java) as Bitmap
    }
}
