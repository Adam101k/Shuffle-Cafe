package com.example.shuffle_cafe

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private const val MAX_STUDY_SESSION_PHOTOS = 5
private const val STUDY_SESSION_CACHE_PREFS_NAME = "shuffle_cafe_study_session_cache"
private const val STUDY_SESSION_CACHE_ENTRY_KEY = "saved_study_sessions"
private const val STUDY_SESSION_CACHE_WRITE_DEBOUNCE_MILLIS = 350L

@Serializable
internal data class StudySession(
    val id: String,
    val cafeId: String,
    val cafeName: String,
    val cafeAddress: String,
    val title: String,
    val className: String,
    val scheduledDateUtcMillis: Long,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int? = null,
    val endMinute: Int? = null,
    val durationHours: Int = 0,
    val durationMinutes: Int = 0,
    val summary: String,
    val photoUris: List<String>,
    val createdAtEpochMillis: Long
) {
    private val resolvedEndTime: Pair<Int, Int>
        get() = resolveStudySessionEndTime(
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            durationHours = durationHours,
            durationMinutes = durationMinutes
        )

    val dateText: String
        get() = formatStudySessionDate(scheduledDateUtcMillis)

    val startTimeText: String
        get() = formatStudySessionTime(startHour, startMinute)

    val endTimeText: String
        get() {
            val (resolvedHour, resolvedMinute) = resolvedEndTime
            return formatStudySessionTime(resolvedHour, resolvedMinute)
        }

    val timeText: String
        get() = timeRangeText

    val timeRangeText: String
        get() = "$startTimeText - $endTimeText"

    val scheduleText: String
        get() = "$dateText at $timeRangeText"
}

internal data class StudySessionDraft(
    val title: String = "",
    val className: String = "",
    val scheduledDateUtcMillis: Long? = null,
    val startHour: Int? = null,
    val startMinute: Int? = null,
    val endHour: Int? = null,
    val endMinute: Int? = null,
    val summary: String = "",
    val photoUris: List<String> = emptyList()
)

internal object StudySessionRepository {
    private val localSessions = mutableStateListOf<StudySession>()
    private val persistenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var appContext: Context? = null
    private var isInitialized = false
    private var persistJob: Job? = null
    @Volatile
    private var persistGeneration = 0

    fun initialize(context: Context) {
        val applicationContext = context.applicationContext
        appContext = applicationContext
        if (isInitialized) return

        val cachedSessions = StudySessionCacheStore.load(applicationContext)
            .sortedByDescending { session -> session.createdAtEpochMillis }
        localSessions.clear()
        localSessions.addAll(cachedSessions)
        isInitialized = true
    }

    fun sessions(): List<StudySession> = localSessions.toList()

    fun canCreate(draft: StudySessionDraft): Boolean {
        return draft.title.normalizedRequired() != null &&
            draft.className.normalizedRequired() != null &&
            draft.scheduledDateUtcMillis != null &&
            draft.startHour != null &&
            draft.startMinute != null &&
            draft.endHour != null &&
            draft.endMinute != null &&
            isStudySessionTimeRangeValid(
                startHour = draft.startHour,
                startMinute = draft.startMinute,
                endHour = draft.endHour,
                endMinute = draft.endMinute
            ) &&
            draft.summary.normalizedRequired() != null
    }

    fun add(
        cafe: Cafe,
        draft: StudySessionDraft,
        nowMillis: Long = System.currentTimeMillis()
    ): StudySession? {
        val title = draft.title.normalizedRequired() ?: return null
        val className = draft.className.normalizedRequired() ?: return null
        val scheduledDateUtcMillis = draft.scheduledDateUtcMillis ?: return null
        val startHour = draft.startHour ?: return null
        val startMinute = draft.startMinute ?: return null
        val endHour = draft.endHour ?: return null
        val endMinute = draft.endMinute ?: return null
        if (!isStudySessionTimeRangeValid(startHour, startMinute, endHour, endMinute)) return null
        val duration = calculateStudySessionDuration(
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute
        )
        val summary = draft.summary.normalizedRequired() ?: return null

        val session = StudySession(
            id = "study-$nowMillis-${localSessions.size}",
            cafeId = cafe.id,
            cafeName = cafe.name,
            cafeAddress = cafe.address,
            title = title,
            className = className,
            scheduledDateUtcMillis = scheduledDateUtcMillis,
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            durationHours = duration.first,
            durationMinutes = duration.second,
            summary = summary,
            photoUris = normalizePhotoUris(draft.photoUris),
            createdAtEpochMillis = nowMillis
        )
        localSessions.add(0, session)
        persistSessions()
        return session
    }

    fun remove(sessionId: String) {
        if (localSessions.removeAll { session -> session.id == sessionId }) {
            persistSessions()
        }
    }

    internal fun resetForTest() {
        persistJob?.cancel()
        persistJob = null
        persistGeneration++
        localSessions.clear()
        appContext = null
        isInitialized = false
    }

    private fun persistSessions() {
        val context = appContext ?: return
        val sessionsSnapshot = localSessions.toList()
        val generation = ++persistGeneration

        persistJob?.cancel()
        persistJob = persistenceScope.launch {
            try {
                delay(STUDY_SESSION_CACHE_WRITE_DEBOUNCE_MILLIS)
                if (generation != persistGeneration) return@launch
                StudySessionCacheStore.save(context, sessionsSnapshot)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                error.printStackTrace()
            }
        }
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

private object StudySessionCacheStore {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(
            STUDY_SESSION_CACHE_PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }

    fun load(context: Context): List<StudySession> {
        return runCatching {
            val encoded = prefs(context).getString(STUDY_SESSION_CACHE_ENTRY_KEY, null)
                ?: return@runCatching emptyList()
            json.decodeFromString<List<StudySession>>(encoded)
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, sessions: List<StudySession>) {
        if (sessions.isEmpty()) {
            clear(context)
            return
        }

        prefs(context)
            .edit()
            .putString(STUDY_SESSION_CACHE_ENTRY_KEY, json.encodeToString(sessions))
            .apply()
    }

    fun clear(context: Context) {
        prefs(context)
            .edit()
            .remove(STUDY_SESSION_CACHE_ENTRY_KEY)
            .apply()
    }
}

internal fun isStudySessionTimeRangeValid(
    startHour: Int?,
    startMinute: Int?,
    endHour: Int?,
    endMinute: Int?
): Boolean {
    if (startHour == null || startMinute == null || endHour == null || endMinute == null) {
        return false
    }
    return timeToTotalMinutes(endHour, endMinute) > timeToTotalMinutes(startHour, startMinute)
}

internal fun calculateStudySessionDuration(
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int
): Pair<Int, Int> {
    val totalMinutes = timeToTotalMinutes(endHour, endMinute) - timeToTotalMinutes(startHour, startMinute)
    return totalMinutes / 60 to totalMinutes % 60
}

internal fun formatStudySessionDate(utcDateMillis: Long): String {
    val formatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(utcDateMillis)
}

internal fun formatStudySessionTime(hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(calendar.time)
}

private fun resolveStudySessionEndTime(
    startHour: Int,
    startMinute: Int,
    endHour: Int?,
    endMinute: Int?,
    durationHours: Int,
    durationMinutes: Int
): Pair<Int, Int> {
    if (endHour != null && endMinute != null) {
        return endHour to endMinute
    }

    val totalMinutes = timeToTotalMinutes(startHour, startMinute) + (durationHours * 60) + durationMinutes
    return (totalMinutes / 60) % 24 to totalMinutes % 60
}

private fun timeToTotalMinutes(hour: Int, minute: Int): Int = (hour * 60) + minute
