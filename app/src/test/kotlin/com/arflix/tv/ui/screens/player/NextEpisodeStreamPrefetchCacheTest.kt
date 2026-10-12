package com.arflix.tv.ui.screens.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NextEpisodeStreamPrefetchCacheTest {
    private val key = NextEpisodeStreamPrefetchKey(
        profileId = "a",
        mediaId = 42,
        season = 2,
        episode = 7,
        imdbId = "tt12345",
    )

    @Test fun emptyCacheReturnsNothing() {
        assertTrue(NextEpisodeStreamPrefetchCache<String>().get(key).isEmpty())
    }

    @Test fun cacheSurvivesBetweenConsumersAndCopiesInput() {
        var now = 1_000L
        val cache = NextEpisodeStreamPrefetchCache<String>(nowMs = { now })
        val source = mutableListOf("one", "two")
        cache.put(key, source)
        source.clear()
        now += 30_000L
        assertEquals(listOf("one", "two"), cache.get(key))
        assertEquals(listOf("one", "two"), cache.get(key))
    }

    @Test fun requiresExactEpisodeAndProfile() {
        val cache = NextEpisodeStreamPrefetchCache<String>()
        cache.put(key, listOf("one"))
        assertTrue(cache.get(key.copy(episode = 8)).isEmpty())
        assertTrue(cache.get(key.copy(season = 3)).isEmpty())
        assertTrue(cache.get(key.copy(mediaId = 43)).isEmpty())
        assertTrue(cache.get(key.copy(imdbId = "tt67890")).isEmpty())
        assertTrue(cache.get(key.copy(profileId = "b")).isEmpty())
        assertEquals(listOf("one"), cache.get(key))
    }

    @Test fun expiresAndRejectsClockRollback() {
        var now = 1_000L
        val cache = NextEpisodeStreamPrefetchCache<String>(nowMs = { now })
        cache.put(key, listOf("one"))
        now += 120_000L
        assertEquals(listOf("one"), cache.get(key))
        now += 1L
        assertTrue(cache.get(key).isEmpty())
        cache.put(key, listOf("one"))
        now -= 1L
        assertTrue(cache.get(key).isEmpty())
    }

    @Test fun emptyResultsCannotReplaceValidResults() {
        val cache = NextEpisodeStreamPrefetchCache<String>()
        cache.put(key, listOf("one"))
        cache.put(key.copy(episode = 8), emptyList())
        assertEquals(listOf("one"), cache.get(key))
        cache.clear()
        assertTrue(cache.get(key).isEmpty())
    }
}
