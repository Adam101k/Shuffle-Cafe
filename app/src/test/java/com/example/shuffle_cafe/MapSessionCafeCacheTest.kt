package com.example.shuffle_cafe

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapSessionCafeCacheTest {

    @Test
    fun recordViewportMergesCafesFromMultipleViewportLoads() {
        var now = 1_000L
        val cache = MapSessionCafeCache(ttlMillis = 10_000L, nowMillis = { now })

        cache.recordViewport(
            viewportKey = "viewport_a",
            cafes = listOf(
                testCafe(id = "alpha"),
                testCafe(id = "beta", name = "Old Beta")
            )
        )

        now += 100L
        cache.recordViewport(
            viewportKey = "viewport_b",
            cafes = listOf(
                testCafe(id = "beta", name = "New Beta"),
                testCafe(id = "charlie")
            )
        )

        val snapshot = cache.snapshot()

        assertEquals(3, snapshot.size)
        assertEquals(setOf("alpha", "beta", "charlie"), snapshot.map { cafe -> cafe.id }.toSet())
        assertEquals("New Beta", snapshot.single { cafe -> cafe.id == "beta" }.name)
    }

    @Test
    fun viewportIsFreshOnlyWithinTtl() {
        var now = 10L
        val cache = MapSessionCafeCache(ttlMillis = 1_000L, nowMillis = { now })

        cache.recordViewport("viewport_a", listOf(testCafe(id = "alpha")))

        assertTrue(cache.isViewportFresh("viewport_a"))

        now += 999L
        assertTrue(cache.isViewportFresh("viewport_a"))

        now += 1L
        assertFalse(cache.isViewportFresh("viewport_a"))
    }

    @Test
    fun snapshotPrunesStaleCafes() {
        var now = 500L
        val cache = MapSessionCafeCache(ttlMillis = 1_000L, nowMillis = { now })

        cache.recordViewport("viewport_a", listOf(testCafe(id = "alpha")))

        now += 999L
        assertEquals(listOf("alpha"), cache.snapshot().map { cafe -> cafe.id })

        now += 1L
        assertTrue(cache.snapshot().isEmpty())
        assertFalse(cache.isViewportFresh("viewport_a"))
    }

    @Test
    fun snapshotRecomputesDistancesFromReferenceLocation() {
        val cache = MapSessionCafeCache(ttlMillis = 10_000L, nowMillis = { 1_000L })
        cache.recordViewport(
            viewportKey = "viewport_a",
            cafes = listOf(
                testCafe(
                    id = "nearby",
                    latLng = LatLng(34.1008, -117.3004),
                    distanceMeters = 90_000f
                )
            )
        )

        val persistedDistance = cache.snapshot(distanceReference = null).single().distanceMeters
        val recomputedDistance = cache.snapshot(distanceReference = LatLng(34.1000, -117.3000))
            .single()
            .distanceMeters

        assertEquals(90_000f, persistedDistance ?: 0f, 0.001f)
        assertTrue((recomputedDistance ?: Float.MAX_VALUE) < 150f)
    }

    private fun testCafe(
        id: String,
        name: String = "Cafe $id",
        latLng: LatLng = LatLng(34.1000, -117.3000),
        distanceMeters: Float? = 1_200f
    ): Cafe {
        return Cafe(
            id = id,
            name = name,
            address = "123 Bean Street",
            phone = "555-0100",
            status = "Open",
            hours = linkedMapOf("Monday" to "7:00 AM - 5:00 PM"),
            features = listOf("Coffee house"),
            ambience = listOf("Coffee"),
            distanceMeters = distanceMeters,
            latLng = latLng
        )
    }
}
