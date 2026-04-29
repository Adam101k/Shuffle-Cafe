package com.example.shuffle_cafe

import com.google.android.gms.maps.model.LatLng
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BookmarkRepositoryTest {

    @Before
    fun setUp() {
        CafeRepository.resetForTest()
        BookmarkRepository.resetForTest()
    }

    @After
    fun tearDown() {
        BookmarkRepository.resetForTest()
        CafeRepository.resetForTest()
    }

    @Test
    fun addIsIdempotentAndKeepsMostRecentBookmarkFirst() {
        BookmarkRepository.add("alpha")
        BookmarkRepository.add("bravo")
        BookmarkRepository.add("alpha")

        assertEquals(listOf("alpha", "bravo"), BookmarkRepository.ids())
    }

    @Test
    fun toggleUsesSaveOrderingForNewBookmarksAndRemovesExistingOnSecondTap() {
        BookmarkRepository.add("alpha")

        BookmarkRepository.toggle("bravo")
        assertEquals(listOf("bravo", "alpha"), BookmarkRepository.ids())

        BookmarkRepository.toggle("bravo")
        assertEquals(listOf("alpha"), BookmarkRepository.ids())
    }

    @Test
    fun completeSavedCafeIsNotSelectedForCacheEnrichment() {
        val completeCafe = testCafe(id = "complete")
        BookmarkRepository.add(completeCafe.id)
        BookmarkRepository.finishSavedCafeCacheEnrichment(completeCafe)

        assertFalse(BookmarkRepository.shouldEnrichSavedCafeCache(completeCafe))
        assertTrue(BookmarkRepository.isSavedCafeCacheEnrichmentCompleteForTest(completeCafe.id))
    }

    @Test
    fun incompleteSavedCafeIsSelectedForCacheEnrichment() {
        val incompleteCafe = testCafe(id = "incomplete", phone = "Phone unavailable", imageUrl = null)
        BookmarkRepository.add(incompleteCafe.id)

        assertTrue(BookmarkRepository.shouldEnrichSavedCafeCache(incompleteCafe))
    }

    @Test
    fun recentSavedCafeCacheAttemptSuppressesRepeatUntilWindowExpires() {
        val now = 10_000L
        val incompleteCafe = testCafe(id = "retry", phone = "Phone unavailable", imageUrl = null)
        BookmarkRepository.add(incompleteCafe.id)

        assertTrue(BookmarkRepository.markSavedCafeCacheEnrichmentStarted(incompleteCafe.id, now))

        assertFalse(
            BookmarkRepository.shouldEnrichSavedCafeCache(
                incompleteCafe,
                now + SAVED_CAFE_CACHE_ATTEMPT_TTL_MILLIS - 1
            )
        )
        assertTrue(
            BookmarkRepository.shouldEnrichSavedCafeCache(
                incompleteCafe,
                now + SAVED_CAFE_CACHE_ATTEMPT_TTL_MILLIS
            )
        )
    }

    @Test
    fun unsavingClearsSavedCafeCacheEnrichmentState() {
        val now = 20_000L
        val completeCafe = testCafe(id = "unsaved")
        BookmarkRepository.add(completeCafe.id)

        BookmarkRepository.markSavedCafeCacheEnrichmentStarted(completeCafe.id, now)
        BookmarkRepository.finishSavedCafeCacheEnrichment(completeCafe, now + 1)

        assertTrue(BookmarkRepository.isSavedCafeCacheEnrichmentCompleteForTest(completeCafe.id))

        BookmarkRepository.remove(completeCafe.id)

        assertFalse(BookmarkRepository.isSavedCafeCacheEnrichmentCompleteForTest(completeCafe.id))
        assertNull(BookmarkRepository.savedCafeCacheAttemptedAtForTest(completeCafe.id))
    }

    @Test
    fun savedCafePersistenceSnapshotKeepsCachedCafeData() {
        val savedCafe = testCafe(id = "cached", imageUrl = "https://example.com/cached.jpg")
        CafeRepository.cacheCafes(listOf(savedCafe))

        BookmarkRepository.add(savedCafe.id)

        val snapshot = BookmarkRepository.savedCafeCacheSnapshotForTest()
        assertEquals(listOf(savedCafe.id), snapshot.map { cafe -> cafe.id })
        assertEquals(savedCafe.imageUrl, snapshot.single().imageUrl)
        assertFalse(isBookmarkCacheDataIncomplete(snapshot.single()))
    }

    @Test
    fun savedCafeNoteCanBeSavedAndUpdated() {
        SavedCafeNotesRepository.save("note-cafe", "Quiet corner table")

        assertEquals("Quiet corner table", SavedCafeNotesRepository.noteFor("note-cafe"))

        SavedCafeNotesRepository.save("note-cafe", "Best outlets by the window")

        assertEquals("Best outlets by the window", SavedCafeNotesRepository.noteFor("note-cafe"))
    }

    @Test
    fun blankSavedCafeNoteRemovesExistingNote() {
        SavedCafeNotesRepository.save("blank-note", "Try the seasonal latte")

        SavedCafeNotesRepository.save("blank-note", "   ")

        assertNull(SavedCafeNotesRepository.noteFor("blank-note"))
    }

    @Test
    fun removingBookmarkClearsSavedCafeNote() {
        val savedCafe = testCafe(id = "remove-note")
        CafeRepository.cacheCafes(listOf(savedCafe))
        BookmarkRepository.add(savedCafe.id)
        SavedCafeNotesRepository.save(savedCafe.id, "Good for afternoon study")

        BookmarkRepository.remove(savedCafe.id)

        assertNull(SavedCafeNotesRepository.noteFor(savedCafe.id))
    }

    @Test
    fun togglingBookmarkOffClearsSavedCafeNote() {
        val savedCafe = testCafe(id = "toggle-note")
        CafeRepository.cacheCafes(listOf(savedCafe))
        BookmarkRepository.add(savedCafe.id)
        SavedCafeNotesRepository.save(savedCafe.id, "Crowded after lunch")

        BookmarkRepository.toggle(savedCafe.id)

        assertFalse(BookmarkRepository.isBookmarked(savedCafe.id))
        assertNull(SavedCafeNotesRepository.noteFor(savedCafe.id))
    }

    @Test
    fun savedCafeNotesResetClearsState() {
        SavedCafeNotesRepository.save("reset-note", "Keep this until reset")

        SavedCafeNotesRepository.resetForTest()

        assertNull(SavedCafeNotesRepository.noteFor("reset-note"))
        assertTrue(SavedCafeNotesRepository.notesSnapshotForTest().isEmpty())
    }

    private fun testCafe(
        id: String,
        phone: String = "555-0100",
        address: String = "123 Bean Street",
        imageUrl: String? = "https://example.com/$id.jpg"
    ): Cafe {
        return Cafe(
            id = id,
            name = "Cafe $id",
            address = address,
            phone = phone,
            status = "Open",
            hours = linkedMapOf("Monday" to "7:00 AM - 5:00 PM"),
            features = listOf("Coffee house"),
            ambience = listOf("Coffee"),
            rating = 4.6f,
            userRatingCount = 120,
            imageUrl = imageUrl,
            latLng = LatLng(34.1000, -117.3000)
        )
    }
}
