package com.example.shuffle_cafe

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapMarkerRetentionTest {

    @Test
    fun initialViewportLoadPopulatesRenderedEntries() {
        val cafes = listOf(
            testCafe(id = "alpha"),
            testCafe(id = "beta")
        )

        val reconciled = reconcileRenderedMapCafeEntries(
            previousEntries = emptyList(),
            currentViewportCafes = cafes,
            currentViewportSourceKey = "viewport_a",
            activeViewportKey = "viewport_a",
            isViewportLoadInFlight = false,
            clearEntries = false
        )

        assertEquals(listOf("alpha", "beta"), reconciled.map { it.cafe.id })
        assertEquals(listOf("viewport_a", "viewport_a"), reconciled.map { it.viewportKey })
    }

    @Test
    fun startingViewportRefreshKeepsPreviousMarkersAndMergesIncomingOnes() {
        val previousEntries = listOf(
            RenderedMapCafeEntry(testCafe(id = "alpha"), "viewport_a"),
            RenderedMapCafeEntry(testCafe(id = "beta"), "viewport_a")
        )
        val currentViewportCafes = listOf(
            testCafe(id = "charlie"),
            testCafe(id = "delta")
        )

        val reconciled = reconcileRenderedMapCafeEntries(
            previousEntries = previousEntries,
            currentViewportCafes = currentViewportCafes,
            currentViewportSourceKey = "viewport_b",
            activeViewportKey = "viewport_b",
            isViewportLoadInFlight = true,
            clearEntries = false
        )

        assertEquals(listOf("charlie", "delta", "alpha", "beta"), reconciled.map { it.cafe.id })
        assertEquals(
            listOf("viewport_b", "viewport_b", "viewport_a", "viewport_a"),
            reconciled.map { it.viewportKey }
        )
    }

    @Test
    fun refreshCompletionPrunesMarkersOutsideNewestViewport() {
        val previousEntries = listOf(
            RenderedMapCafeEntry(testCafe(id = "alpha"), "viewport_a"),
            RenderedMapCafeEntry(testCafe(id = "beta"), "viewport_a"),
            RenderedMapCafeEntry(testCafe(id = "charlie"), "viewport_b")
        )
        val currentViewportCafes = listOf(
            testCafe(id = "charlie"),
            testCafe(id = "delta")
        )

        val reconciled = reconcileRenderedMapCafeEntries(
            previousEntries = previousEntries,
            currentViewportCafes = currentViewportCafes,
            currentViewportSourceKey = "viewport_b",
            activeViewportKey = "viewport_b",
            isViewportLoadInFlight = false,
            clearEntries = false
        )

        assertEquals(listOf("charlie", "delta"), reconciled.map { it.cafe.id })
        assertTrue(reconciled.all { it.viewportKey == "viewport_b" })
    }

    @Test
    fun repeatedCafeIdsUpdateExistingEntryWithoutDuplicating() {
        val previousEntries = listOf(
            RenderedMapCafeEntry(testCafe(id = "alpha", name = "Old Name"), "viewport_a")
        )
        val currentViewportCafes = listOf(
            testCafe(id = "alpha", name = "New Name")
        )

        val reconciled = reconcileRenderedMapCafeEntries(
            previousEntries = previousEntries,
            currentViewportCafes = currentViewportCafes,
            currentViewportSourceKey = "viewport_b",
            activeViewportKey = "viewport_b",
            isViewportLoadInFlight = true,
            clearEntries = false
        )

        assertEquals(1, reconciled.size)
        assertEquals("alpha", reconciled.single().cafe.id)
        assertEquals("New Name", reconciled.single().cafe.name)
        assertEquals("viewport_b", reconciled.single().viewportKey)
    }

    @Test
    fun markerLabelsStayHiddenBelowThresholdAndShowAtOrAboveIt() {
        assertFalse(markerVisualStyleForZoom(12.24f).showLabel)
        assertTrue(markerVisualStyleForZoom(12.25f).showLabel)
        assertTrue(markerVisualStyleForZoom(13.5f).showLabel)
    }

    private fun testCafe(
        id: String,
        name: String = "Cafe $id"
    ): Cafe {
        return Cafe(
            id = id,
            name = name,
            address = "123 Bean Street",
            phone = "555-0100",
            status = "Open",
            hours = linkedMapOf("Monday" to "7:00 AM - 5:00 PM"),
            features = emptyList(),
            ambience = emptyList()
        )
    }
}
