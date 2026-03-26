package com.example.shuffle_cafe

internal data class CafeMarkerVisualStyle(
    val zoomBucket: Int,
    val scale: Float,
    val showLabel: Boolean
)

internal data class RenderedMapCafeEntry(
    val cafe: Cafe,
    val viewportKey: String
)

internal fun markerVisualStyleForZoom(zoom: Float): CafeMarkerVisualStyle {
    return when {
        zoom < 12.25f -> CafeMarkerVisualStyle(zoomBucket = 0, scale = 0.42f, showLabel = false)
        zoom < 13.75f -> CafeMarkerVisualStyle(zoomBucket = 1, scale = 0.56f, showLabel = true)
        zoom < 15.5f -> CafeMarkerVisualStyle(zoomBucket = 2, scale = 0.68f, showLabel = true)
        zoom < 17.0f -> CafeMarkerVisualStyle(zoomBucket = 3, scale = 0.86f, showLabel = true)
        else -> CafeMarkerVisualStyle(zoomBucket = 4, scale = 1f, showLabel = true)
    }
}

internal fun reconcileRenderedMapCafeEntries(
    previousEntries: List<RenderedMapCafeEntry>,
    currentViewportCafes: List<Cafe>,
    currentViewportSourceKey: String?,
    activeViewportKey: String?,
    isViewportLoadInFlight: Boolean,
    clearEntries: Boolean
): List<RenderedMapCafeEntry> {
    if (clearEntries || activeViewportKey.isNullOrBlank()) {
        return emptyList()
    }

    val currentEntries = currentViewportSourceKey
        ?.let { sourceKey ->
            currentViewportCafes.map { cafe ->
                RenderedMapCafeEntry(
                    cafe = cafe,
                    viewportKey = sourceKey
                )
            }
        }
        .orEmpty()

    if (!isViewportLoadInFlight) {
        return currentEntries.filter { it.viewportKey == activeViewportKey }
    }

    if (currentEntries.isEmpty()) {
        return previousEntries
    }

    val currentCafeIds = currentEntries
        .asSequence()
        .map { entry -> entry.cafe.id }
        .toHashSet()
    val retainedEntries = previousEntries.filterNot { entry -> entry.cafe.id in currentCafeIds }
    return currentEntries + retainedEntries
}
