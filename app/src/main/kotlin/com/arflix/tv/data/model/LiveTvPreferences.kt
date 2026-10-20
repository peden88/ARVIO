package com.arflix.tv.data.model

/**
 * Profile-scoped TV presentation preferences. No provider credentials are stored here.
 * The versioned, bounded codec deliberately ignores unknown keys so older builds can
 * read preferences written by a newer build without losing unrelated settings.
 */
data class LiveTvPreferences(
    val guideRows: Int = 0,
    val timelineScale: Int = 100,
    val showChannelNumbers: Boolean = true,
    val twoLineChannels: Boolean = false,
    val twoLineProgrammes: Boolean = false,
    val highlightCurrentProgrammes: Boolean = true,
    val showNowLine: Boolean = true,
    val showPreview: Boolean = true,
    val tuneFullscreen: Boolean = false,
    val panelTimeoutSeconds: Int = 5,
    val accent: String = "app",
) {
    fun normalized() = copy(
        guideRows = guideRows.takeIf { it == 0 || it in 6..10 } ?: 0,
        timelineScale = timelineScale.takeIf { it in TIMELINE_SCALES } ?: 100,
        panelTimeoutSeconds = panelTimeoutSeconds.takeIf { it in PANEL_TIMEOUTS } ?: 5,
        accent = accent.takeIf { it in ACCENTS } ?: "app",
    )

    fun encode(): String {
        val value = normalized()
        return listOf(
            "version=1",
            "rows=" + value.guideRows,
            "scale=" + value.timelineScale,
            "numbers=" + value.showChannelNumbers,
            "channelLines=" + value.twoLineChannels,
            "programmeLines=" + value.twoLineProgrammes,
            "highlight=" + value.highlightCurrentProgrammes,
            "nowLine=" + value.showNowLine,
            "preview=" + value.showPreview,
            "fullscreen=" + value.tuneFullscreen,
            "timeout=" + value.panelTimeoutSeconds,
            "accent=" + value.accent,
        ).joinToString(";")
    }

    companion object {
        const val PREFERENCE_KEY = "live_tv_presentation_v1"
        val GUIDE_ROWS = listOf(0, 6, 7, 8, 9, 10)
        val TIMELINE_SCALES = listOf(75, 100, 125)
        val PANEL_TIMEOUTS = listOf(3, 5, 8, 12)
        val ACCENTS = listOf("app", "blue", "teal")
        val Classic = LiveTvPreferences(
            guideRows = 8,
            twoLineChannels = true,
            twoLineProgrammes = true,
            accent = "blue",
        )

        fun decode(raw: String?): LiveTvPreferences {
            if (raw.isNullOrBlank() || raw.length > 4096) return LiveTvPreferences()
            val entries = raw.split(';').mapNotNull { field ->
                val separator = field.indexOf('=')
                if (separator <= 0) null else field.substring(0, separator) to field.substring(separator + 1)
            }.toMap()
            if (entries["version"] != "1") return LiveTvPreferences()
            fun bool(key: String, fallback: Boolean) = entries[key]?.toBooleanStrictOrNull() ?: fallback
            return LiveTvPreferences(
                guideRows = entries["rows"]?.toIntOrNull() ?: 0,
                timelineScale = entries["scale"]?.toIntOrNull() ?: 100,
                showChannelNumbers = bool("numbers", true),
                twoLineChannels = bool("channelLines", false),
                twoLineProgrammes = bool("programmeLines", false),
                highlightCurrentProgrammes = bool("highlight", true),
                showNowLine = bool("nowLine", true),
                showPreview = bool("preview", true),
                tuneFullscreen = bool("fullscreen", false),
                panelTimeoutSeconds = entries["timeout"]?.toIntOrNull() ?: 5,
                accent = entries["accent"] ?: "app",
            ).normalized()
        }

        fun next(current: Int, choices: List<Int>): Int =
            choices[(choices.indexOf(current) + 1).mod(choices.size)]
    }
}
