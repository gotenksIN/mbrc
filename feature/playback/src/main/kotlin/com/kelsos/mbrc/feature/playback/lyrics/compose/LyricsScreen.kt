package com.kelsos.mbrc.feature.playback.lyrics.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kelsos.mbrc.core.common.state.PlayingPosition
import com.kelsos.mbrc.core.common.state.TrackInfo
import com.kelsos.mbrc.core.ui.compose.EmptyScreen
import com.kelsos.mbrc.core.ui.compose.ThinSlider
import com.kelsos.mbrc.core.ui.compose.WaveProgressIndicator
import com.kelsos.mbrc.feature.playback.R
import com.kelsos.mbrc.feature.playback.lyrics.LyricsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun LyricsScreen(
  onCollapse: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: LyricsViewModel = koinViewModel()
) {
  val lyrics by viewModel.lyrics.collectAsStateWithLifecycle(initialValue = emptyList())
  val playingTrack by viewModel.playingTrack.collectAsStateWithLifecycle()
  val playingPosition by viewModel.playingPosition.collectAsStateWithLifecycle()
  val trackDetails by viewModel.trackDetails.collectAsStateWithLifecycle()
  val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

  LyricsScreenContent(
    lyrics = lyrics,
    playingTrack = playingTrack,
    playingPosition = playingPosition,
    composer = trackDetails.composer,
    isPlaying = isPlaying,
    onCollapse = onCollapse,
    onPlayPauseClick = viewModel::playPause,
    onSeek = { viewModel.seek(it.toInt()) },
    modifier = modifier
  )
}

@Composable
fun LyricsScreenContent(
  modifier: Modifier = Modifier,
  composer: String = "",
  lyrics: List<String>,
  playingTrack: TrackInfo,
  playingPosition: PlayingPosition,
  isPlaying: Boolean,
  onCollapse: () -> Unit,
  onPlayPauseClick: () -> Unit,
  onSeek: (Float) -> Unit
) {
  val lyricTimestamps = remember(lyrics) { lyrics.map(::leadingTimestampMs) }
  val hasSyncedLyrics = remember(lyricTimestamps) { lyricTimestamps.any { it != null } }
  val activeLineIndex = remember(lyricTimestamps, playingPosition.current) {
    findActiveLyricIndex(lyricTimestamps, playingPosition.current)
  }
  val listState = rememberLazyListState()

  LaunchedEffect(activeLineIndex, hasSyncedLyrics) {
    if (hasSyncedLyrics && activeLineIndex >= 0) {
      listState.animateScrollToItem(index = maxOf(activeLineIndex - 2, 0))
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.primary)
      .windowInsetsPadding(WindowInsets.statusBars)
  ) {
    // Header with collapse button and track info
    LyricsHeader(
      trackTitle = playingTrack.title,
      artistName = playingTrack.artist,
      composer = composer,
      onCollapse = onCollapse
    )

    // Lyrics content
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      if (lyrics.isEmpty()) {
        EmptyScreen(
          message = stringResource(R.string.no_lyrics),
          icon = Icons.Default.MusicNote,
          modifier = Modifier.fillMaxSize(),
          contentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
        )
      } else {
        LazyColumn(
          state = listState,
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          itemsIndexed(lyrics) { index, line ->
            val timestamp = lyricTimestamps[index]
            LyricsLine(
              text = line,
              isActive = index == activeLineIndex,
              onClick = if (timestamp != null) { { onSeek(timestamp.toFloat()) } } else null
            )
          }
        }
      }
    }

    // Footer with progress and play/pause
    LyricsFooter(
      playingPosition = playingPosition,
      isPlaying = isPlaying,
      onPlayPauseClick = onPlayPauseClick,
      onSeek = onSeek
    )
  }
}

@Composable
private fun LyricsHeader(
  trackTitle: String,
  artistName: String,
  composer: String,
  onCollapse: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    IconButton(onClick = onCollapse) {
      Icon(
        imageVector = Icons.Default.KeyboardArrowDown,
        contentDescription = stringResource(R.string.lyrics_collapse),
        tint = MaterialTheme.colorScheme.onPrimary
      )
    }

    Column(
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = trackTitle.ifEmpty { stringResource(R.string.unknown_title) },
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
      )
      Text(
        text = artistName.ifEmpty { stringResource(R.string.unknown_artist) },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
      )
      if (composer.isNotBlank()) {
        Text(
          text = stringResource(R.string.track_details_composer) + ": " + composer,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          textAlign = TextAlign.Center
        )
      }
    }

    // Spacer to balance the collapse button
    Spacer(modifier = Modifier.size(48.dp))
  }
}

@Composable
private fun LyricsLine(
  text: String,
  isActive: Boolean,
  onClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val displayText = text.removeLeadingTimestamp().trim()

  if (displayText.isBlank()) {
    // Spacer for empty lines (verse breaks)
    Spacer(modifier = modifier.height(16.dp))
  } else {
    Text(
      text = displayText,
      style = MaterialTheme.typography.headlineSmall.copy(
        fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
        lineHeight = 32.sp
      ),
      color = if (isActive) {
        MaterialTheme.colorScheme.onPrimary
      } else {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
      },
      modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
        .let { if (onClick != null) it.clickable { onClick() } else it }
    )
  }
}

private val leadingTimestampRegex = Regex("""^\[(\d{1,2}):(\d{2})(?:[.:](\d{1,3}))?]""")

private fun leadingTimestampMs(line: String): Long? {
  val match = leadingTimestampRegex.find(line) ?: return null
  val minutes = match.groups[1]?.value?.toLongOrNull() ?: return null
  val seconds = match.groups[2]?.value?.toLongOrNull() ?: return null
  val fraction = match.groups[3]?.value.orEmpty()
  val millis = when (fraction.length) {
    0 -> 0L
    1 -> fraction.toLong() * 100L
    2 -> fraction.toLong() * 10L
    else -> fraction.take(3).toLong()
  }

  return minutes * 60_000L + seconds * 1_000L + millis
}

private fun String.removeLeadingTimestamp(): String = replaceFirst(leadingTimestampRegex, "")

private fun findActiveLyricIndex(timestamps: List<Long?>, positionMs: Long): Int {
  var activeIndex = -1
  timestamps.forEachIndexed { index, timestamp ->
    if (timestamp != null && timestamp <= positionMs) {
      activeIndex = index
    }
  }
  return activeIndex
}

@Composable
private fun LyricsFooter(
  playingPosition: PlayingPosition,
  isPlaying: Boolean,
  onPlayPauseClick: () -> Unit,
  onSeek: (Float) -> Unit,
  modifier: Modifier = Modifier
) {
  var seekPosition by remember { mutableFloatStateOf(0f) }
  var isSeeking by remember { mutableStateOf(false) }

  val progress = if (playingPosition.total > 0) {
    playingPosition.current.toFloat() / playingPosition.total.toFloat()
  } else {
    0f
  }

  val isStream = playingPosition.isStream

  Column(
    modifier = modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.navigationBars)
      .padding(horizontal = 24.dp, vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    val sliderColor = MaterialTheme.colorScheme.onPrimary

    ThinSlider(
      value = if (isSeeking) seekPosition else progress,
      onValueChange = { newValue ->
        seekPosition = newValue
        isSeeking = true
      },
      onValueChangeFinished = {
        onSeek(seekPosition * playingPosition.total)
        isSeeking = false
      },
      enabled = !isStream,
      modifier = Modifier.fillMaxWidth(),
      trackColor = sliderColor,
      inactiveTrackColor = sliderColor.copy(alpha = 0.3f),
      thumbColor = sliderColor
    )

    // Time display
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = playingPosition.currentMinutes,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
      )
      Text(
        text = playingPosition.totalMinutes,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Play/Pause button
    FilledIconButton(
      onClick = onPlayPauseClick,
      modifier = Modifier.size(64.dp),
      shape = CircleShape,
      colors = IconButtonDefaults.filledIconButtonColors(
        containerColor = MaterialTheme.colorScheme.onPrimary,
        contentColor = MaterialTheme.colorScheme.primary
      )
    ) {
      Icon(
        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
        contentDescription = stringResource(R.string.main_button_play_pause_description),
        modifier = Modifier.size(32.dp)
      )
    }
  }
}
