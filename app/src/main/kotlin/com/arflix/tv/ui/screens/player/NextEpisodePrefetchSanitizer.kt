package com.arflix.tv.ui.screens.player

import com.arflix.tv.data.model.StreamSource
import com.arflix.tv.data.repository.providerScopedStreamIdentity

/**
 * Prefetch results are speculative. Never allow an unusable cached URL to enter
 * the next episode's stream selection; ordinary discovery remains the fallback.
 */
internal fun sanitizeNextEpisodePrefetchedStreams(streams: List<StreamSource>): List<StreamSource> =
    streams.filter { stream ->
        val url = stream.url?.trim().orEmpty()
        url.isNotEmpty() &&
            !url.startsWith("magnet:", ignoreCase = true) &&
            !url.startsWith("javascript:", ignoreCase = true) &&
            !url.startsWith("data:", ignoreCase = true) &&
            !url.startsWith("file:", ignoreCase = true) &&
            (url.startsWith("https://", ignoreCase = true) ||
                url.startsWith("http://", ignoreCase = true))
    }.distinctBy(::providerScopedStreamIdentity)
