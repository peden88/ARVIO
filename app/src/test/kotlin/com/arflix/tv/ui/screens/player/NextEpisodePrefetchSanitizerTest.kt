package com.arflix.tv.ui.screens.player

import com.arflix.tv.data.model.StreamSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NextEpisodePrefetchSanitizerTest {
    private fun source(url: String?, addonId: String = "addon") = StreamSource(
        source = "Test", addonName = "Test addon", addonId = addonId,
        quality = "1080p", size = "", url = url
    )

    @Test fun rejectsInvalidAndUnsafeCachedUrls() {
        val cleaned = sanitizeNextEpisodePrefetchedStreams(listOf(
            source(null), source(""), source("magnet:?xt=urn:btih:test"),
            source("javascript:alert(1)"), source("file:///tmp/video"),
            source("data:text/plain,test"), source("https://example.com/video")
        ))
        assertEquals(1, cleaned.size)
        assertEquals("https://example.com/video", cleaned.single().url)
    }

    @Test fun removesDuplicateResultsFromSameProvider() {
        val stream = source("https://example.com/video")
        val cleaned = sanitizeNextEpisodePrefetchedStreams(listOf(stream, stream, stream))
        assertEquals(1, cleaned.size)
    }

    @Test fun allowsOrdinaryHttpStreamsAndEmptyResults() {
        assertEquals(1, sanitizeNextEpisodePrefetchedStreams(listOf(source("http://example.com/video"))).size)
        assertTrue(sanitizeNextEpisodePrefetchedStreams(emptyList()).isEmpty())
    }
}
