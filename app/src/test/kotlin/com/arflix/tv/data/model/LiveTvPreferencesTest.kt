package com.arflix.tv.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LiveTvPreferencesTest {
    @Test fun allPresentationValuesSurviveRestart() {
        val original = LiveTvPreferences(
            guideRows = 9, timelineScale = 75, showChannelNumbers = false,
            twoLineChannels = true, twoLineProgrammes = true,
            highlightCurrentProgrammes = false, showNowLine = false,
            showPreview = false, tuneFullscreen = true, panelTimeoutSeconds = 12,
            accent = "teal",
        )
        assertThat(LiveTvPreferences.decode(original.encode())).isEqualTo(original)
    }

    @Test fun missingOrUnsupportedVersionKeepsExistingAppearance() {
        listOf(null, "", "rows=9", "version=2;rows=9").forEach {
            assertThat(LiveTvPreferences.decode(it)).isEqualTo(LiveTvPreferences())
        }
    }

    @Test fun corruptGeometryCannotCreateZeroHeightRowsOrHugeTimeline() {
        val decoded = LiveTvPreferences.decode(
            "version=1;rows=-20;scale=2147483647;timeout=0;accent=unknown;numbers=maybe;preview=false"
        )
        assertThat(decoded.guideRows).isEqualTo(0)
        assertThat(decoded.timelineScale).isEqualTo(100)
        assertThat(decoded.panelTimeoutSeconds).isEqualTo(5)
        assertThat(decoded.showChannelNumbers).isTrue()
        assertThat(decoded.showPreview).isFalse()
        assertThat(decoded.accent).isEqualTo("app")
    }

    @Test fun futureFieldsDoNotEraseKnownPreferences() {
        assertThat(LiveTvPreferences.decode("version=1;rows=7;futureSetting=abc;preview=false"))
            .isEqualTo(LiveTvPreferences(guideRows = 7, showPreview = false))
    }

    @Test fun oversizedInputIsRejectedBeforeParsing() {
        assertThat(LiveTvPreferences.decode("version=1;rows=9;" + "x".repeat(4096)))
            .isEqualTo(LiveTvPreferences())
    }
}
