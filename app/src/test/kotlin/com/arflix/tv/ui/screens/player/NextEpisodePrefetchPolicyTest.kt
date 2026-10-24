package com.arflix.tv.ui.screens.player

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NextEpisodePrefetchPolicyTest {
    private fun eligible(position: Long = 540_000L, duration: Long = 600_000L,
        live: Boolean = false, started: Boolean = true, error: Boolean = false,
        autoPlay: Boolean = true, available: Boolean = true, casting: Boolean = false
    ) = shouldStartNextEpisodePrefetch(position, duration, live, started, error,
        autoPlay, available, casting)

    @Test fun startsAtOneMinuteRemaining() {
        assertTrue(eligible())
        assertTrue(eligible(position = 599_999L))
        assertFalse(eligible(position = 539_999L))
    }

    @Test fun rejectsInvalidPlaybackStates() {
        assertFalse(eligible(live = true))
        assertFalse(eligible(started = false))
        assertFalse(eligible(error = true))
        assertFalse(eligible(autoPlay = false))
        assertFalse(eligible(available = false))
        assertFalse(eligible(casting = true))
    }

    @Test fun rejectsInvalidOrPlaceholderDurations() {
        assertFalse(eligible(position = -1))
        assertFalse(eligible(position = 600_000L))
        assertFalse(eligible(duration = 0))
        assertFalse(eligible(position = 100_000L, duration = 110_000L))
    }
}
