package com.example.shuffle_cafe

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BookmarkRepositoryTest {

    @Before
    fun setUp() {
        BookmarkRepository.resetForTest()
    }

    @After
    fun tearDown() {
        BookmarkRepository.resetForTest()
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
}
