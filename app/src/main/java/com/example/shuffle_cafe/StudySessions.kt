package com.example.shuffle_cafe

import androidx.compose.runtime.mutableStateListOf

private const val MAX_STUDY_SESSION_PHOTOS = 5

internal data class StudySession(
    val id: String,
    val cafeId: String,
    val cafeName: String,
    val cafeAddress: String,
    val title: String,
    val className: String,
    val dateText: String,
    val timeText: String,
    val summary: String,
    val photoUris: List<String>,
    val createdAtEpochMillis: Long
)

internal data class StudySessionDraft(
    val title: String = "",
    val className: String = "",
    val dateText: String = "",
    val timeText: String = "",
    val summary: String = "",
    val photoUris: List<String> = emptyList()
)

internal object StudySessionRepository {
    private val localSessions = mutableStateListOf<StudySession>()

    fun sessions(): List<StudySession> = localSessions.toList()

    fun canCreate(draft: StudySessionDraft): Boolean {
        return draft.title.normalizedRequired() != null &&
            draft.className.normalizedRequired() != null &&
            draft.dateText.normalizedRequired() != null &&
            draft.timeText.normalizedRequired() != null &&
            draft.summary.normalizedRequired() != null
    }

    fun add(
        cafe: Cafe,
        draft: StudySessionDraft,
        nowMillis: Long = System.currentTimeMillis()
    ): StudySession? {
        val title = draft.title.normalizedRequired() ?: return null
        val className = draft.className.normalizedRequired() ?: return null
        val dateText = draft.dateText.normalizedRequired() ?: return null
        val timeText = draft.timeText.normalizedRequired() ?: return null
        val summary = draft.summary.normalizedRequired() ?: return null

        val session = StudySession(
            id = "study-${nowMillis}-${localSessions.size}",
            cafeId = cafe.id,
            cafeName = cafe.name,
            cafeAddress = cafe.address,
            title = title,
            className = className,
            dateText = dateText,
            timeText = timeText,
            summary = summary,
            photoUris = normalizePhotoUris(draft.photoUris),
            createdAtEpochMillis = nowMillis
        )
        localSessions.add(0, session)
        return session
    }

    internal fun resetForTest() {
        localSessions.clear()
    }

    private fun String.normalizedRequired(): String? {
        return trim().takeIf { it.isNotBlank() }
    }

    private fun normalizePhotoUris(photoUris: List<String>): List<String> {
        return photoUris
            .mapNotNull { uri -> uri.trim().takeIf { it.isNotBlank() } }
            .distinct()
            .take(MAX_STUDY_SESSION_PHOTOS)
    }
}
