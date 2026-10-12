package com.arflix.tv.ui.screens.player

/** Timing-only gate for future next-episode stream discovery. No network side effects. */
internal const val NEXT_EPISODE_PREFETCH_LEAD_MS = 60_000L

internal fun shouldStartNextEpisodePrefetch(
    positionMs: Long,
    durationMs: Long,
    isLive: Boolean,
    hasPlaybackStarted: Boolean,
    hasPlaybackError: Boolean,
    autoPlayNextEpisode: Boolean,
    nextEpisodeAvailable: Boolean,
    isCasting: Boolean,
    leadMs: Long = NEXT_EPISODE_PREFETCH_LEAD_MS,
): Boolean {
    if (isLive || isCasting || !hasPlaybackStarted || hasPlaybackError ||
        !autoPlayNextEpisode || !nextEpisodeAvailable) return false
    if (durationMs <= 0L || positionMs < 0L || positionMs >= durationMs) return false
    // Avoid triggering on short trailers, placeholders or incorrectly reported duration.
    if (durationMs < 120_000L) return false
    val remainingMs = durationMs - positionMs
    return remainingMs <= leadMs.coerceAtLeast(0L)
}
