package com.arflix.tv.ui.screens.tv.live

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LiveTvDirectTuneTest {
    @Test fun directTuneOpensNewChannelFullscreen() {
        assertThat(channelRowInteractionAction(false, directFullscreen = true))
            .isEqualTo(EpgInteractionAction.PlayLiveFullscreen)
    }

    @Test fun directTuneDoesNotTurnFutureProgrammeIntoLivePlayback() {
        assertThat(epgProgramInteractionAction(EpgTemporalState.Future, false, false, true, true))
            .isEqualTo(EpgInteractionAction.ShowProgramInfo)
    }

    @Test fun directTunePreservesArchiveAndUnavailableArchiveRules() {
        assertThat(epgProgramInteractionAction(EpgTemporalState.Past, false, true, true, true))
            .isEqualTo(EpgInteractionAction.PlayCatchup)
        assertThat(epgProgramInteractionAction(EpgTemporalState.Past, false, false, true, true))
            .isEqualTo(EpgInteractionAction.NoOp)
    }

    @Test fun directTuneBypassesVodLookupOnlyForLiveProgramme() {
        assertThat(epgProgramInteractionAction(EpgTemporalState.Live, true, false, true, true))
            .isEqualTo(EpgInteractionAction.PlayLiveFullscreen)
    }
}
