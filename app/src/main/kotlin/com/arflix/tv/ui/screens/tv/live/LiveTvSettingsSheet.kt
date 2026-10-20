package com.arflix.tv.ui.screens.tv.live

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arflix.tv.R
import com.arflix.tv.data.model.IptvProgram
import com.arflix.tv.data.model.LiveTvPreferences
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal val LocalLiveTvPreferences = staticCompositionLocalOf { LiveTvPreferences() }

/** Focusable side sheet: Android owns Back and focus cannot escape to the guide. */
@Composable
internal fun LiveTvSettingsSheet(
    preferences: LiveTvPreferences,
    saveError: Boolean,
    onChange: ((LiveTvPreferences) -> LiveTvPreferences) -> Unit,
    onManageSources: () -> Unit,
    onRefreshGuide: () -> Unit,
    onAppSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    var page by rememberSaveable { mutableStateOf("root") }
    val back: () -> Unit = { if (page == "root") onDismiss() else page = "root" }
    val title = stringResource(when (page) {
        "guide" -> R.string.live_settings_guide
        "player" -> R.string.live_settings_player
        else -> R.string.live_settings_title
    })
    LiveSideSheet(title = title, identity = page, onDismiss = back) { firstFocus ->
        item(key = "back") {
            LiveSettingRow(
                label = stringResource(if (page == "root") R.string.live_settings_close else R.string.back),
                onClick = back,
                modifier = Modifier.focusRequester(firstFocus).testTag("live-settings-back"),
            )
        }
        if (saveError) item(key = "error") {
            Text(stringResource(R.string.live_settings_save_failed), color = Color(0xFFFFB4AB),
                modifier = Modifier.padding(12.dp), fontSize = 13.sp)
        }
        when (page) {
            "guide" -> {
                item { LiveSettingRow(stringResource(R.string.live_settings_rows),
                    if (preferences.guideRows == 0) stringResource(R.string.live_settings_existing) else preferences.guideRows.toString(),
                    { onChange { it.copy(guideRows = LiveTvPreferences.next(it.guideRows, LiveTvPreferences.GUIDE_ROWS)) } }) }
                item { LiveSettingRow(stringResource(R.string.live_settings_timeline),
                    preferences.timelineScale.toString() + "%",
                    { onChange { it.copy(timelineScale = LiveTvPreferences.next(it.timelineScale, LiveTvPreferences.TIMELINE_SCALES)) } }) }
                item { LiveSettingToggle(R.string.live_settings_numbers, preferences.showChannelNumbers) {
                    onChange { it.copy(showChannelNumbers = !it.showChannelNumbers) } } }
                item { LiveSettingToggle(R.string.live_settings_channel_lines, preferences.twoLineChannels) {
                    onChange { it.copy(twoLineChannels = !it.twoLineChannels) } } }
                item { LiveSettingToggle(R.string.live_settings_programme_lines, preferences.twoLineProgrammes) {
                    onChange { it.copy(twoLineProgrammes = !it.twoLineProgrammes) } } }
                item { LiveSettingToggle(R.string.live_settings_highlight, preferences.highlightCurrentProgrammes) {
                    onChange { it.copy(highlightCurrentProgrammes = !it.highlightCurrentProgrammes) } } }
                item { LiveSettingToggle(R.string.live_settings_now_line, preferences.showNowLine) {
                    onChange { it.copy(showNowLine = !it.showNowLine) } } }
                item { LiveSettingRow(stringResource(R.string.live_settings_accent),
                    stringResource(when (preferences.accent) {
                        "blue" -> R.string.live_settings_blue
                        "teal" -> R.string.live_settings_teal
                        else -> R.string.live_settings_app_accent
                    }),
                    { onChange { current ->
                        val choices = LiveTvPreferences.ACCENTS
                        current.copy(accent = choices[(choices.indexOf(current.accent) + 1).mod(choices.size)])
                    } }) }
            }
            "player" -> {
                item { LiveSettingToggle(R.string.live_settings_preview, preferences.showPreview) {
                    onChange { it.copy(showPreview = !it.showPreview) } } }
                item { LiveSettingToggle(R.string.live_settings_fullscreen, preferences.tuneFullscreen) {
                    onChange { it.copy(tuneFullscreen = !it.tuneFullscreen) } } }
                item { LiveSettingRow(stringResource(R.string.live_settings_timeout),
                    stringResource(R.string.live_settings_seconds, preferences.panelTimeoutSeconds),
                    { onChange { it.copy(panelTimeoutSeconds = LiveTvPreferences.next(it.panelTimeoutSeconds, LiveTvPreferences.PANEL_TIMEOUTS)) } }) }
            }
            else -> {
                item { LiveSettingRow(stringResource(R.string.live_settings_guide), onClick = { page = "guide" }) }
                item { LiveSettingRow(stringResource(R.string.live_settings_player), onClick = { page = "player" }) }
                item { LiveSettingRow(stringResource(R.string.live_settings_sources), onClick = onManageSources) }
                item { LiveSettingRow(stringResource(R.string.live_settings_refresh), onClick = onRefreshGuide) }
                item { LiveSettingRow(stringResource(R.string.live_settings_classic), onClick = {
                    onChange { LiveTvPreferences.Classic }
                }) }
                item { LiveSettingRow(stringResource(R.string.live_settings_reset), onClick = {
                    onChange { LiveTvPreferences() }
                }) }
                item { LiveSettingRow(stringResource(R.string.live_settings_app), onClick = onAppSettings) }
            }
        }
    }
}

@Composable
internal fun LiveProgrammeDetailsSheet(
    channelName: String,
    program: IptvProgram,
    nowMillis: Long,
    favorite: Boolean,
    onWatchLive: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit,
) {
    val dateFormat = remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault()) }
    LiveSideSheet(title = program.title, identity = "programme", onDismiss = onDismiss) { firstFocus ->
        item { LiveSettingRow(stringResource(R.string.live_settings_close), onClick = onDismiss,
            modifier = Modifier.focusRequester(firstFocus)) }
        item {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(channelName, color = LiveColors.Fg, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(dateFormat.format(Instant.ofEpochMilli(program.startUtcMillis)) + " – " +
                    dateFormat.format(Instant.ofEpochMilli(program.endUtcMillis)),
                    color = LiveColors.FgDim, fontSize = 13.sp)
                Text(stringResource(when {
                    program.startUtcMillis > nowMillis -> R.string.live_programme_upcoming
                    program.isLive(nowMillis) -> R.string.live_programme_on_now
                    else -> R.string.live_programme_no_archive
                }),
                    color = LiveColors.FgDim, fontSize = 13.sp)
                program.category?.takeIf { it.isNotBlank() }?.let { Text(it, color = liveAccent(), fontSize = 13.sp) }
                Text(program.description?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.live_programme_no_description), color = LiveColors.Fg, fontSize = 14.sp)
            }
        }
        item { LiveSettingRow(stringResource(R.string.live_programme_watch_channel), onClick = onWatchLive) }
        item { LiveSettingRow(stringResource(if (favorite) R.string.live_menu_favorite_remove else R.string.live_menu_favorite_add),
            onClick = onToggleFavorite) }
    }
}

@Composable
private fun LiveSideSheet(
    title: String,
    identity: String,
    onDismiss: () -> Unit,
    content: LazyListScope.(FocusRequester) -> Unit,
) {
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(identity) {
        withFrameNanos { }
        firstFocus.requestFocus()
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .45f)).focusProperties { canFocus = false }.clickable(onClick = onDismiss))
            Column(
                modifier = Modifier.align(Alignment.CenterEnd).width(380.dp).fillMaxHeight()
                    .background(LiveColors.Panel).padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(title, color = LiveColors.Fg, fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 10.dp))
                androidx.compose.runtime.key(identity) {
                    LazyColumn(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        content(firstFocus)
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveSettingToggle(labelRes: Int, enabled: Boolean, onClick: () -> Unit) {
    LiveSettingRow(stringResource(labelRes), stringResource(if (enabled) R.string.live_settings_on else R.string.live_settings_off), onClick)
}

@Composable
private fun LiveSettingRow(
    label: String,
    value: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val foreground = if (focused) Color.Black else LiveColors.Fg
    Row(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
            .background(if (focused) Color.White else Color.Transparent)
            .onFocusChanged { focused = it.isFocused }.clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(label, color = foreground, fontSize = 14.sp, modifier = Modifier.weight(1f))
        if (value != null) Text(value, color = if (focused) Color.Black else liveAccent(), fontSize = 13.sp)
    }
}
