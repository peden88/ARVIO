# TiviMate-style IPTV in ARVIO

Research and first implementation: 20 October 2026.

## Target and status

The user selected **peden88/ARVIO**, based on **ARVIO/tweaks** at
`af2ea38464b757f52fb2b282bceac8bc9cb18211`.
Work is isolated on `feature/tivimate-iptv`.

This is an initial native integration, **not complete TiviMate feature parity**.
The existing IPTV engine is retained. The first implementation adds a usable
settings drawer and guide preferences, plus programme information for future EPG
entries. DVR, reminders and multiview remain work to implement and validate.
No APK or visual device test is considered verified until the corresponding
check actually passes.

The primary target remains the user's Google TV Streamer build:
`armeabi-v7a`, sideload debug, optional Discord SDK disabled. The CI workflow
checks the resulting ABI rather than assuming it from the device's processor.

## Research sources and how to use them

1. [Official TiviMate website](https://tivimate.com/).
2. [Developer's Google Play listing and six screenshots](https://play.google.com/store/apps/details?id=ar.tvplayer.tv).
3. [MikeG484's author-posted guide links](https://www.reddit.com/r/TiviMate/comments/1wu9ylc/tivimate_users_guide/).
4. [Community user guide, 15 September 2026, 70 pages](https://dn721900.ca.archive.org/0/items/ti-vi-mate-users-guide_202609/TiViMate%20Users%20Guide.pdf).
5. [Author's EPG supplement](https://dn710309.ca.archive.org/0/items/epg-in-ti-vi-mate/EPG%20in%20TiViMate.pdf).
6. [Author's remote-button supplement](https://ia903203.us.archive.org/23/items/remapping-remote-buttons-for-ti-vi-mate/Remapping%20Remote%20Buttons%20for%20TiViMate.pdf).
7. [Author's backup/SMB supplement](https://ia801602.us.archive.org/8/items/sharing-ti-vi-mate-settings-between-devices-and-setting-up-smb/Sharing%20TiViMate%20Settings%20Between%20Devices%20and%20Setting%20Up%20SMB.pdf).

TiviMate's developer confirms M3U, Xtream Codes and Stalker support, an EPG grid,
multiple playlists, favourites, catch-up, recording, search, parental controls
and multiview. The application is designed for TV remote navigation.
The official website supplies a feature overview; the detailed manual is
community-authored, not an official specification. Similarly named reseller
sites were not treated as developer documentation.

The manual describes separate guide/player remote mappings, editable menus,
channel/group organisation, EPG offsets, appearance controls, playback options,
recording padding and reminders. Its screenshots and examples depend on version
and configuration. Use them as behavioural references, not proof that every
configuration works on the user's device.

### Visual reference index

Keep the original screenshots as linked reference material; no TiviMate branding,
manual pages or screenshot assets are shipped in the app.

| Reference | Location |
| --- | --- |
| Official images 1–6 | Google Play screenshot carousel linked above |
| Setup and guide context | Community guide pp. 6–8 |
| Groups and quick menus | Community guide pp. 14–19 |
| Guide/player appearance | Community guide pp. 25–27 |
| Recording workflow | Community guide pp. 34–39 |
| Remote controls | Community guide pp. 51–54 |
| Settings and menu reference | Community guide pp. 55–61 |

Page numbers above are PDF page numbers, starting at one. They are reference
ranges, not a claim that every page is a screenshot. Official screenshots were
located; no pixel-level comparison against an ARVIO emulator/device capture has
been performed in this session.

The visual review should collect these exact states at the same display scale:

- Guide with categories collapsed and expanded, including a long channel name.
- Focused current programme and focused future programme.
- The currently playing video while focus moves to a different channel.
- Fullscreen playback, information banner, button row and recent-channel list.
- Channel context menu, playlist/group options and IPTV settings subpages.
- Catch-up playback and return to live.
- Search with a remote keyboard, empty results and several matching programmes.
- Recording list, scheduler and conflict message once DVR is implemented.
- Multiview with one tile selected and one audible source once implemented.

## What the existing fork actually provides

These findings are from the pinned repository source, not a claim of live testing
against every provider.

| Area | Existing implementation | Remaining work |
| --- | --- | --- |
| Providers | IptvRepository, playlist modal, account information and URL resolver | Keep source credentials, headers and portal URL resolution intact when adding consumers |
| Channels | Categories, favourites, hidden/locked groups, ordering and quality variants | Extend per-channel overrides and user-defined groups after defining stable identities |
| Guide | Time-aligned EpgGrid, indexed data, now/next/history, source checks | Configurable source priority and manual mapping need their own editor |
| Playback | Media3 session, retry policy, mini/fullscreen views, diagnostics | Test interruption, resume, source errors and rapid channel changes on the Streamer |
| Catch-up | IptvGuideHistory, provider URL resolution and archive controls | Validate each supported provider's archive format and time boundaries |
| Search | SearchOverlay and channel selection paths | Confirm programme coverage and focus restoration with large playlists |
| Profile isolation | ProfileManager keys and profile-specific IPTV state | Decide which new options should participate in optional cloud sync |
| Player controls | FullscreenHud, QuickZapOverlay and fullscreen guide | User-configurable remote mappings and menu ordering remain separate work |
| Provider VOD | Repository VOD paths and metadata integration | Keep live-TV routing and movie/series navigation coherent |
| Parental access | Profile PIN and locked-group flow | Audit every new route, search result and scheduled action for the same gate |
| DVR | No IPTV recording service/scheduler found; current manifest removes media-playback FGS permission | Implement recording engine, schedule persistence and output management |
| Reminders | Future EPG selection previously resolved to NoOp | Implement persistent scheduling and delivery before showing reminder controls |
| Multiview | Main live screen operates one playback session | Add an explicit multi-session manager and connection budget |
| Picture-in-picture | Manifest declares general activity support | This alone does not establish working TV IPTV PiP; inspect activity/session lifecycle and supported usage |

Relevant files are under:
`app/src/main/kotlin/com/arflix/tv/data/repository/`,
`ui/screens/tv/`, `ui/screens/tv/live/` and
`app/src/main/AndroidManifest.xml`.

## Implemented in this branch

### IPTV settings drawer

On the TV page, the settings button opens a right-hand drawer with Guide and
Player subpages, access to playlist management, a guide refresh action, a
Classic TV preset, reset, and access to all app settings.

Each visible option is connected to a real behaviour:

| Control | Behaviour |
| --- | --- |
| Guide rows | Use the previous row setting or choose 6–10 |
| Timeline width | 75%, 100% or 125%; timeline and current-time line share the same scale |
| Channel numbers | Show/hide the guide number column |
| Two-line channel names | Wrap names and disable the single-line focus marquee |
| Two-line programme titles | Apply to both drawn cells and focusable cells; suppress the footer when space is insufficient |
| Current-programme highlight | Toggle the live programme background |
| Current time line | Toggle the vertical now indicator |
| Accent | App colour, blue or teal |
| Video preview | Show video beside details or use a compact information area |
| First-press fullscreen | Choose immediate fullscreen or preview-then-fullscreen |
| Panel timeout | 3, 5, 8 or 12 seconds |
| Classic TV | Eight rows, wrapped titles and blue accents |
| Reset | Restore baseline presentation without changing playlist configuration |

Settings use a bounded, versioned record in the existing DataStore with explicit
profile keys. Updates transform the latest record inside the DataStore
transaction. This prevents rapid toggles from overwriting unrelated changes and
prevents a delayed save from selecting a different active profile.

These new preferences are local to the device and profile. Cloud sync and export
are not implemented for this record. Touch-device layout continues to use the
baseline presentation.

### Future programmes

Selecting a future EPG entry opens a side sheet containing its channel,
date/time, description and category. Available actions are Watch channel live,
toggle favourite and close. Selection alone does not tune a channel. Empty guide
placeholders do not open invented programme details.

Live and archive actions retain their existing routing. A programme without a
valid replay source is not offered a fake replay action.

## Product design for the remaining integration

The following is the proposed ARVIO design and engineering acceptance criteria,
rather than a transcription of another application's menus.

### Guide and navigation

Use a compact guide as the normal Live TV destination. Preserve the distinction
between **focused channel**, **playing channel** and **selected programme**.
Moving the remote should update metadata without constantly opening provider
connections. Preview and fullscreen must share a player for the same channel.

The settings sheet must keep D-pad focus inside the sheet, close one level at a
time, and return to the original guide selection. Profile switches must clear
pending overlays and resolve playback according to that profile's access.

Maintain the existing windowed guide/index architecture. Avoid rebuilding the
whole channel tree during every clock tick. Preserve stable channel and programme
keys while playlist or guide data refreshes.

### Source and EPG settings

Build a dedicated source editor around the existing repository. It should expose
connection diagnostics without showing passwords or token-bearing stream URLs.
Keep credentials scoped to their source; a refresh of one source must not replace
another source's customisations.

Represent EPG sources separately from channels, with explicit per-playlist source
order and optional per-channel mapping. Store broadcast timestamps as UTC; apply
display timezone separately. Offsets must be scoped and reversible, not applied
twice during parsing and playback. Keep the last valid guide available if a
refresh fails. Expire indexed history deliberately so memory use stays bounded.

### Channel organisation

Separate provider metadata from user overrides. Keep stable source/channel IDs
for favourites, visibility, names, logos, numbers and order. Custom groups should
reference those IDs rather than copy complete streams. Removing a provider
should report affected favourites and schedules before removing dependencies.

Channel and programme search should use the same visibility and PIN rules as the
guide. Do not allow a locked channel to bypass the gate through recents,
favourites, programme search, direct-number tuning or a reminder.

### Player and remote settings

Use context-specific actions for guide, live player and catch-up player. Short
press and long press must not both fire. A held OK should open one menu, not
select its first item when released. Preserve the existing guide key guard.

Expose only controls supported by the current stream. A live HLS/DASH window,
provider archive and persistent local timeshift are three different
capabilities. The first two do not automatically provide the third.
[Media3's live-streaming documentation](https://developer.android.com/media/media3/exoplayer/live-streaming)
describes the seekable live window and return-to-default-position behaviour.

Map proposed actions to an explicit enum, persist user bindings, provide a
restore-defaults action and reserve a reliable Back path. Keep track selectors,
aspect-ratio settings, external playback and audio options integrated with the
existing player instead of creating a second settings subsystem.

### DVR and reminders

DVR requires a foreground service with persistent job state and a recording
list. Keep scheduling separate from transport: a due job should resolve a fresh
provider URL, enforce the source's connection budget, obtain storage, then start
the appropriate recorder.

Implement and test direct MPEG-TS, HLS and DASH deliberately. Saving an HLS
manifest or concatenating arbitrary segment types is not a complete recorder.
Unsupported protected streams or protocols should return an explicit reason.
Do not describe a failed or partial output as a completed recording.

A job needs: profile/source/channel identifiers, UTC start/end, padding, output
destination, status, failure reason and bytes written. Resume/reconcile jobs
after process restart; distinguish missed, interrupted, cancelled and completed.
Stop cleanly when storage fills or permissions are revoked. Test that watching
one channel while recording another respects the provider's allowed sessions.

Use Android's storage picker for a local/external destination with durable URI
permission. An optional server destination can be a later transport adapter.
The user's VPS could host a recording worker, but no new server service is
assumed or deployed by this work.

For reminders, persist programme identity and schedule, handle programme changes,
notification permission and exact-alarm access, and provide a visible fallback
when precise delivery is unavailable. Avoid using an in-screen coroutine timer
as a substitute for a scheduler.

Primary Android implementation references:
[foreground services](https://developer.android.com/develop/background-work/services/fgs/service-types)
and [alarms](https://developer.android.com/develop/background-work/services/alarms).
Modern Android imposes service-start and alarm restrictions; the scheduler must
respect these and be tested across sleep/restart cases.

### Multiview and PiP

Add multiview as an explicit session owner with tile selection, replace/close,
single-tile fullscreen and one audible tile. Resolve source URLs through the
existing provider code. Release every decoder, surface and HTTP connection on
tile removal, profile switch or exit.

Start device validation with two streams. Increase the offered tile count only
after observing decoder, memory and connection limits on the target device;
do not promise a fixed high count based on the CPU's marketing specifications.
Catch-up and recording consumers count towards the same provider session budget.

In-app multiview is distinct from OS picture-in-picture.
[Android TV's PiP documentation](https://developer.android.com/training/tv/get-started/multitasking)
requires supported hardware/software and declares TV usage requirements. Check
capability and lifecycle behaviour before adding a working PiP control.

### Backup and migration

Define an ARVIO-owned, versioned export format for sources, preferences, channel
overrides and future schedules. Credentials need an explicit encrypted export
choice. Validate import before replacing existing data, support partial source
imports and reject unsupported schema versions.

TiviMate backup compatibility is not implemented or assumed. Changes here do not
import another application's private database.

## Validation and completion gates

The branch workflow builds a 32-bit sideload debug APK, runs unit tests and
checks native ABI, debug marking and signature. Test reports and the source
commit are uploaded with the APK.

Device checks still required:

1. Import the user's M3U/Xtream/Stalker source and verify live playback/EPG.
2. Open every settings subpage using only the Streamer remote. Change values,
   close/reopen, restart and switch profiles.
3. Check every guide setting at the actual TV display scale; inspect long titles,
   missing EPG, midnight and daylight-saving boundaries.
4. Confirm selection, preview, fullscreen, quality variants, quick zap, Back,
   holding OK and future-programme details.
5. Confirm baseline phone/tablet navigation still behaves as before.
6. Stress a large playlist, fast scrolling, source failures and repeated
   enter/exit while monitoring memory and provider sessions.
7. Run recording/reminder/multiview tests after those implementations exist.

Full parity must not be reported until the missing services above work, their
settings are connected, and the device tests pass.
