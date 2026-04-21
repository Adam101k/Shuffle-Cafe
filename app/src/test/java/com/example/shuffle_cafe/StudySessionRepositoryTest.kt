package com.example.shuffle_cafe

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StudySessionRepositoryTest {

    private val cafe = Cafe(
        id = "cafe-1",
        name = "Bean Library",
        address = "123 Study Ave",
        phone = "555-1234",
        status = "Open",
        hours = linkedMapOf("Monday" to "8:00 AM - 8:00 PM"),
        features = emptyList(),
        ambience = emptyList()
    )

    @Before
    fun setUp() {
        StudySessionRepository.resetForTest()
    }

    @After
    fun tearDown() {
        StudySessionRepository.resetForTest()
    }

    @Test
    fun requiredFieldsMustBeNonblankAfterTrimming() {
        val validDraft = StudySessionDraft(
            title = " Midterm Sprint ",
            className = " CS 101 ",
            scheduledDateUtcMillis = 1_704_067_200_000L,
            startHour = 16,
            startMinute = 0,
            endHour = 17,
            endMinute = 0,
            summary = " Review chapters 4 and 5. "
        )
        val invalidDraft = validDraft.copy(summary = "   ")

        assertTrue(StudySessionRepository.canCreate(validDraft))
        assertFalse(StudySessionRepository.canCreate(invalidDraft))
        assertNull(StudySessionRepository.add(cafe, invalidDraft, nowMillis = 10L))
    }

    @Test
    fun addedSessionsAreNewestFirst() {
        StudySessionRepository.add(
            cafe,
            completeDraft("First"),
            nowMillis = 100L
        )
        StudySessionRepository.add(
            cafe,
            completeDraft("Second"),
            nowMillis = 200L
        )

        val sessions = StudySessionRepository.sessions()
        assertEquals(listOf("Second", "First"), sessions.map { it.title })
    }

    @Test
    fun photoUrisAreTrimmedDeduplicatedAndCapped() {
        StudySessionRepository.add(
            cafe,
            completeDraft("Photo test").copy(
                photoUris = listOf(
                    " content://one ",
                    "content://two",
                    "content://one",
                    "   ",
                    "content://three",
                    "content://four",
                    "content://five",
                    "content://six"
                )
            ),
            nowMillis = 300L
        )

        assertEquals(
            listOf(
                "content://one",
                "content://two",
                "content://three",
                "content://four",
                "content://five"
            ),
            StudySessionRepository.sessions().single().photoUris
        )
    }

    @Test
    fun resetClearsLocalState() {
        StudySessionRepository.add(cafe, completeDraft("Reset me"), nowMillis = 400L)

        StudySessionRepository.resetForTest()

        assertTrue(StudySessionRepository.sessions().isEmpty())
    }

    private fun completeDraft(title: String): StudySessionDraft {
        return StudySessionDraft(
            title = title,
            className = "Biology 120",
            scheduledDateUtcMillis = 1_704_067_200_000L,
            startHour = 15,
            startMinute = 0,
            endHour = 16,
            endMinute = 0,
            summary = "Work through the study guide."
        )
    }
}
