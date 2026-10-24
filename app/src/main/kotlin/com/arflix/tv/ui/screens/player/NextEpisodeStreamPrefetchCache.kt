package com.arflix.tv.ui.screens.player

import com.arflix.tv.data.model.StreamSource

/**
 * A playback-session handoff key. The profile prevents sharing addon URLs across users,
 * while the IMDb ID protects against a stale TMDB mapping for the same media ID.
 */
internal data class NextEpisodeStreamPrefetchKey(
    val profileId: String,
    val mediaId: Int,
    val season: Int,
    val episode: Int,
    val imdbId: String,
)

/**
 * Small process-local handoff cache. Unlike a ViewModel field, it survives player
 * navigation/recreation but never persists stream URLs to disk.
 *
 * Only one episode is retained, and reads require an exact key and a fresh timestamp.
 * Synchronized operations make discovery writes safe across coroutine dispatchers.
 */
internal class NextEpisodeStreamPrefetchCache<T>(
    private val ttlMs: Long = 120_000L,
    private val nowMs: () -> Long = System::currentTimeMillis,
) {
    private data class Entry<T>(
        val key: NextEpisodeStreamPrefetchKey,
        val values: List<T>,
        val savedAtMs: Long,
    )

    private var entry: Entry<T>? = null

    @Synchronized
    fun put(key: NextEpisodeStreamPrefetchKey, values: List<T>) {
        if (values.isEmpty()) return
        entry = Entry(key, values.toList(), nowMs())
    }

    @Synchronized
    fun get(key: NextEpisodeStreamPrefetchKey): List<T> {
        val cached = entry ?: return emptyList()
        val ageMs = nowMs() - cached.savedAtMs
        if (ageMs < 0L || ageMs > ttlMs) {
            entry = null
            return emptyList()
        }
        return if (cached.key == key) cached.values.toList() else emptyList()
    }

    @Synchronized
    fun clear() {
        entry = null
    }
}

/** Shared only within this app process, including across successive PlayerViewModels. */
internal object NextEpisodeStreamPrefetchStore {
    private val cache = NextEpisodeStreamPrefetchCache<StreamSource>()

    fun put(key: NextEpisodeStreamPrefetchKey, streams: List<StreamSource>) =
        cache.put(key, streams)

    fun get(key: NextEpisodeStreamPrefetchKey): List<StreamSource> =
        cache.get(key)
}
