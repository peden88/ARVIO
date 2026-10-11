package com.arflix.tv.ui.screens.player

import org.junit.Assert.assertTrue
import org.junit.Test

class NextEpisodeStreamPrefetchCacheTest {
    @Test fun emptyCacheReturnsNothing() {
        val cache = NextEpisodeStreamPrefetchCache<String>()
        val key = NextEpisodeStreamPrefetchKey("a", 1, 1, 1, "tt1")
        assertTrue(cache.get(key).isEmpty())
    }
    @Test fun storesEpisodeStreams() {
        val cache = NextEpisodeStreamPrefetchCache<String>()
        val key = NextEpisodeStreamPrefetchKey("a", 1, 1, 1, "tt1")
        cache.put(key, listOf("source"))
        assertTrue(cache.get(key).isNotEmpty())
    }
}
