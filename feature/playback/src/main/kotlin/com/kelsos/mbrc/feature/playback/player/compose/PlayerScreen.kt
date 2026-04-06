package com.kelsos.mbrc.feature.playback.player.compose

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.outlined.Lyrics
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.toBitmap
import com.kelsos.mbrc.core.common.state.LfmRating
import com.kelsos.mbrc.core.common.state.PlayerState
import com.kelsos.mbrc.core.common.state.PlayingPosition
import com.kelsos.mbrc.core.common.state.PlayerScreenVisibilityTracker
import com.kelsos.mbrc.core.common.state.Repeat
import com.kelsos.mbrc.core.common.state.ShuffleMode
import com.kelsos.mbrc.core.common.state.TrackInfo
import com.kelsos.mbrc.core.common.state.TrackRating
import com.kelsos.mbrc.core.ui.R as CoreUiR
import com.kelsos.mbrc.core.ui.compose.DynamicScreenScaffold
import com.kelsos.mbrc.core.ui.compose.ThinSlider
import com.kelsos.mbrc.core.ui.compose.TopBarState
import com.kelsos.mbrc.feature.misc.output.compose.OutputSelectionBottomSheet
import com.kelsos.mbrc.feature.playback.R
import com.kelsos.mbrc.feature.playback.lyrics.LyricsViewModel
import com.kelsos.mbrc.feature.playback.player.IPlayerActions
import com.kelsos.mbrc.feature.playback.player.PlaybackState
import com.kelsos.mbrc.feature.playback.player.PlayerViewModel
import com.kelsos.mbrc.feature.playback.player.VolumeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun PlayerScreen(
  onNavigateToNowPlaying: () -> Unit,
  onNavigateToAlbum: (album: String, artist: String) -> Unit,
  onNavigateToArtist: (artist: String) -> Unit,
  snackbarHostState: SnackbarHostState,
  onOpenDrawer: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: PlayerViewModel = koinViewModel(),
  lyricsViewModel: LyricsViewModel = koinViewModel()
) {
  // Collect separate state flows for granular recomposition
  val playingTrack by viewModel.playingTrack.collectAsStateWithLifecycle()
  val playingPosition by viewModel.playingPosition.collectAsStateWithLifecycle()
  val volumeState by viewModel.volumeState.collectAsStateWithLifecycle()
  val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
  val trackDetails by viewModel.trackDetails.collectAsStateWithLifecycle()
  val playerScreenVisibilityTracker: PlayerScreenVisibilityTracker = koinInject()

  // Lyrics state
  val lyrics by lyricsViewModel.lyrics.collectAsStateWithLifecycle(initialValue = emptyList())

  var showOutputSelection by remember { mutableStateOf(false) }
  var showLyrics by remember { mutableStateOf(false) }

  DisposableEffect(Unit) {
    playerScreenVisibilityTracker.isVisible = true
    onDispose {
      playerScreenVisibilityTracker.isVisible = false
    }
  }

  val title = stringResource(R.string.nav_now_playing)

  // Compute scaffold configuration based on current state
  val topBarState = TopBarState.WithTitle(title)
  val onOverflowClick: (() -> Unit)? = null
  val darkTheme = isSystemInDarkTheme()
  val defaultBackground = MaterialTheme.colorScheme.background
  val topBarAlbumArtState = rememberAlbumArtState(
    coverUrl = playingTrack.coverUrl,
    defaultBackground = defaultBackground,
    darkTheme = darkTheme
  )
  val topBarContentColor = remember(topBarAlbumArtState.colors.dominant) {
    playerUiColorsFor(topBarAlbumArtState.colors.dominant).primaryForeground
  }
  val view = LocalView.current
  val fallbackUseDarkStatusBarIcons = MaterialTheme.colorScheme.background.luminance() > 0.5f

  DisposableEffect(view, topBarContentColor, fallbackUseDarkStatusBarIcons) {
    val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose {}
    val insetsController = WindowCompat.getInsetsController(window, view)

    insetsController.isAppearanceLightStatusBars = topBarContentColor.luminance() < 0.5f

    onDispose {
      insetsController.isAppearanceLightStatusBars = fallbackUseDarkStatusBarIcons
    }
  }

  if (showOutputSelection) {
    OutputSelectionBottomSheet(
      onDismiss = { showOutputSelection = false }
    )
  }

  DynamicScreenScaffold(
    topBarState = topBarState,
    snackbarHostState = snackbarHostState,
    defaultTitle = title,
    onOpenDrawer = onOpenDrawer,
    onOverflowClick = onOverflowClick,
    isTransparent = true,
    contentColor = topBarContentColor,
    modifier = modifier
  ) { paddingValues ->
    // Ignore padding for player screen as it uses transparent top bar
    PlayerScreenContent(
      playingTrack = playingTrack,
      playingPosition = playingPosition,
      volumeState = volumeState,
      playbackState = playbackState,
      actions = viewModel.actions,
      lyrics = lyrics,
      showLyrics = showLyrics,
      hasLyrics = lyrics.isNotEmpty(),
      onNavigateToAlbum = onNavigateToAlbum,
      onNavigateToArtist = onNavigateToArtist,
      onLyricsClick = {
        if (lyrics.isNotEmpty()) {
          showLyrics = !showLyrics
        }
      },
      onOutputClick = { showOutputSelection = true },
      onQueueClick = onNavigateToNowPlaying,
      albumArtState = topBarAlbumArtState,
      contentPadding = paddingValues
    )
  }
}

@Composable
internal fun PlayerScreenContent(
  playingTrack: TrackInfo,
  playingPosition: PlayingPosition,
  volumeState: VolumeState,
  playbackState: PlaybackState,
  actions: IPlayerActions,
  lyrics: List<String>,
  showLyrics: Boolean,
  hasLyrics: Boolean,
  onNavigateToAlbum: (album: String, artist: String) -> Unit,
  onNavigateToArtist: (artist: String) -> Unit,
  onLyricsClick: () -> Unit,
  onOutputClick: () -> Unit,
  onQueueClick: () -> Unit,
  albumArtState: AlbumArtState,
  contentPadding: PaddingValues = PaddingValues(),
  modifier: Modifier = Modifier
) {
  val configuration = LocalConfiguration.current
  val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

  // Animate color transitions smoothly when track changes
  val animatedDominant by animateColorAsState(
    targetValue = albumArtState.colors.dominant.copy(alpha = 0.6f),
    animationSpec = tween(durationMillis = 500),
    label = "dominant_color"
  )
  val animatedBackground by animateColorAsState(
    targetValue = albumArtState.colors.backgroundColor,
    animationSpec = tween(durationMillis = 500),
    label = "background_color"
  )

  // Create gradient brush
  val gradientBrush = Brush.verticalGradient(
    colors = listOf(
      animatedDominant,
      animatedBackground
    )
  )

  val topInset = contentPadding.calculateTopPadding()
  val bottomInset = contentPadding.calculateBottomPadding()
  val playerUiColors = remember(albumArtState.colors.dominant) {
    playerUiColorsFor(albumArtState.colors.dominant)
  }

  CompositionLocalProvider(LocalPlayerUiColors provides playerUiColors) {
    BoxWithConstraints(modifier = modifier) {
      val isTablet = maxWidth >= PlayerConstants.TABLET_WIDTH_THRESHOLD
      BlurredPlayerBackdrop(
        painter = albumArtState.painter,
        gradientBrush = gradientBrush,
        modifier = Modifier.fillMaxSize()
      )

      when {
        isLandscape -> LandscapePlayerLayout(
          painter = albumArtState.painter,
          playingTrack = playingTrack,
          playingPosition = playingPosition,
          lyrics = lyrics,
          showLyrics = showLyrics,
          hasLyrics = hasLyrics,
          volumeState = volumeState,
          playbackState = playbackState,
          gradientBrush = gradientBrush,
          topInset = topInset,
          bottomInset = bottomInset,
          actions = actions,
          onNavigateToAlbum = onNavigateToAlbum,
          onNavigateToArtist = onNavigateToArtist,
          onLyricsClick = onLyricsClick,
          onOutputClick = onOutputClick,
          onQueueClick = onQueueClick
        )

        isTablet -> TabletPlayerLayout(
          painter = albumArtState.painter,
          playingTrack = playingTrack,
          playingPosition = playingPosition,
          lyrics = lyrics,
          showLyrics = showLyrics,
          hasLyrics = hasLyrics,
          volumeState = volumeState,
          playbackState = playbackState,
          gradientBrush = gradientBrush,
          topInset = topInset,
          bottomInset = bottomInset,
          actions = actions,
          onNavigateToAlbum = onNavigateToAlbum,
          onNavigateToArtist = onNavigateToArtist,
          onLyricsClick = onLyricsClick,
          onOutputClick = onOutputClick,
          onQueueClick = onQueueClick
        )

        else -> PortraitPlayerLayout(
          painter = albumArtState.painter,
          playingTrack = playingTrack,
          playingPosition = playingPosition,
          lyrics = lyrics,
          showLyrics = showLyrics,
          hasLyrics = hasLyrics,
          volumeState = volumeState,
          playbackState = playbackState,
          gradientBrush = gradientBrush,
          topInset = topInset,
          bottomInset = bottomInset,
          actions = actions,
          onNavigateToAlbum = onNavigateToAlbum,
          onNavigateToArtist = onNavigateToArtist,
          onLyricsClick = onLyricsClick,
          onOutputClick = onOutputClick,
          onQueueClick = onQueueClick
        )
      }
    }
  }
}

/**
 * Constants for player layout dimensions and values.
 */
private object PlayerConstants {
  const val LANDSCAPE_ALBUM_HEIGHT_FRACTION = 0.85f
  const val VOLUME_MAX = 100f
  const val SLIDER_DEBOUNCE_MS = 1000L
  val TABLET_WIDTH_THRESHOLD = 600.dp
  val TOP_BAR_HEIGHT = 64.dp // Padding for transparent top bar
  val CONTENT_PADDING = 24.dp
  val BLUR_RADIUS = 72.dp
  val PLAYER_SURFACE_SCRIM = Color.Black.copy(alpha = 0.48f)
  val PLAYER_SURFACE_FADE = Color.Black.copy(alpha = 0.72f)
  val PRIMARY_FOREGROUND = Color.White
  val SECONDARY_FOREGROUND = Color.White.copy(alpha = 0.72f)
  val MUTED_FOREGROUND = Color.White.copy(alpha = 0.52f)
  val DISABLED_FOREGROUND = Color.White.copy(alpha = 0.35f)
  val LYRICS_PANEL_SCRIM = Color.Black.copy(alpha = 0.22f)
}

/**
 * Data class to hold colors extracted from album artwork.
 */
internal data class AlbumColors(
  val dominant: Color,
  val vibrant: Color,
  val darkVibrant: Color,
  val backgroundColor: Color
)

private data class PlayerUiColors(
  val surfaceScrim: Color,
  val surfaceFade: Color,
  val primaryForeground: Color,
  val secondaryForeground: Color,
  val mutedForeground: Color,
  val disabledForeground: Color,
  val lyricsPanelScrim: Color,
  val playButtonContainer: Color,
  val playButtonContent: Color
)

private val LocalPlayerUiColors = staticCompositionLocalOf { playerUiColorsFor(Color.Black) }

private fun playerUiColorsFor(baseColor: Color): PlayerUiColors {
  val useDarkForeground = baseColor.luminance() > 0.55f
  val baseForeground = if (useDarkForeground) Color(0xFF111111) else Color.White

  return if (useDarkForeground) {
    PlayerUiColors(
      surfaceScrim = Color.White.copy(alpha = 0.18f),
      surfaceFade = Color.White.copy(alpha = 0.42f),
      primaryForeground = baseForeground,
      secondaryForeground = baseForeground.copy(alpha = 0.78f),
      mutedForeground = baseForeground.copy(alpha = 0.62f),
      disabledForeground = baseForeground.copy(alpha = 0.40f),
      lyricsPanelScrim = Color.White.copy(alpha = 0.24f),
      playButtonContainer = Color(0xFF111111),
      playButtonContent = Color.White
    )
  } else {
    PlayerUiColors(
      surfaceScrim = Color.Black.copy(alpha = 0.48f),
      surfaceFade = Color.Black.copy(alpha = 0.72f),
      primaryForeground = baseForeground,
      secondaryForeground = baseForeground.copy(alpha = 0.72f),
      mutedForeground = baseForeground.copy(alpha = 0.52f),
      disabledForeground = baseForeground.copy(alpha = 0.35f),
      lyricsPanelScrim = Color.Black.copy(alpha = 0.22f),
      playButtonContainer = Color.White,
      playButtonContent = Color.Black
    )
  }
}

/**
 * Data class to hold album art state including painter and extracted colors.
 */
internal data class AlbumArtState(val painter: AsyncImagePainter, val colors: AlbumColors)

/**
 * Remembers album art state including the painter and extracted colors.
 * This ensures the image is only loaded once and shared between display and color extraction.
 */
@Composable
internal fun rememberAlbumArtState(
  coverUrl: String,
  defaultBackground: Color,
  darkTheme: Boolean
): AlbumArtState {
  val context = LocalContext.current

  val imageRequest = remember(coverUrl) {
    ImageRequest.Builder(context)
      .data(coverUrl.ifEmpty { null })
      .crossfade(true)
      .allowHardware(false) // Required for Palette color extraction
      .build()
  }

  val painter = rememberAsyncImagePainter(model = imageRequest)

  // Collect painter state as a snapshot state for proper recomposition
  val painterState by painter.state.collectAsStateWithLifecycle()

  var colors by remember(defaultBackground) {
    mutableStateOf(
      AlbumColors(
        dominant = defaultBackground,
        vibrant = defaultBackground,
        darkVibrant = defaultBackground,
        backgroundColor = defaultBackground
      )
    )
  }

  // Update colors when theme changes or image loads
  LaunchedEffect(painterState, darkTheme, defaultBackground) {
    when (val currentState = painterState) {
      is AsyncImagePainter.State.Success -> {
        val bitmap = currentState.result.image.toBitmap()
        val extracted = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
          extractColorsFromBitmap(bitmap, defaultBackground, darkTheme)
        }
        colors = extracted
      }

      else -> {
        // Reset to default background when no image
        colors = AlbumColors(
          dominant = defaultBackground,
          vibrant = defaultBackground,
          darkVibrant = defaultBackground,
          backgroundColor = defaultBackground
        )
      }
    }
  }

  return AlbumArtState(painter = painter, colors = colors)
}

/**
 * Extracts colors from a bitmap using Android's Palette API.
 * Uses different palette swatches based on the current theme:
 * - Dark theme: Uses darker/muted colors
 * - Light theme: Uses lighter/vibrant colors
 */
private fun extractColorsFromBitmap(
  bitmap: Bitmap,
  defaultBackground: Color,
  darkTheme: Boolean
): AlbumColors {
  val palette = Palette.from(bitmap).generate()
  val defaultColor = defaultBackground.copy(alpha = 1f)
  val androidDefault = android.graphics.Color.argb(
    (defaultColor.alpha * 255).toInt(),
    (defaultColor.red * 255).toInt(),
    (defaultColor.green * 255).toInt(),
    (defaultColor.blue * 255).toInt()
  )

  // Choose appropriate palette swatch based on theme
  val dominant = if (darkTheme) {
    palette.getDarkMutedColor(palette.getDominantColor(androidDefault))
  } else {
    palette.getLightMutedColor(palette.getDominantColor(androidDefault))
  }

  val vibrant = if (darkTheme) {
    palette.getDarkVibrantColor(dominant)
  } else {
    palette.getLightVibrantColor(dominant)
  }

  val darkVibrant = palette.getDarkVibrantColor(dominant)

  return AlbumColors(
    dominant = Color(dominant),
    vibrant = Color(vibrant),
    darkVibrant = Color(darkVibrant),
    backgroundColor = defaultBackground
  )
}

@Composable
private fun BlurredPlayerBackdrop(
  painter: AsyncImagePainter,
  gradientBrush: Brush,
  modifier: Modifier = Modifier
) {
  val uiColors = LocalPlayerUiColors.current
  val painterState by painter.state.collectAsStateWithLifecycle()
  val placeholderPainter = painterResource(CoreUiR.drawable.ic_image_no_cover)
  val activePainter = when (painterState) {
    is AsyncImagePainter.State.Success -> painter
    is AsyncImagePainter.State.Error -> placeholderPainter
    else -> placeholderPainter
  }

  Box(modifier = modifier) {
    Image(
      painter = activePainter,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxSize()
        .graphicsLayer {
          scaleX = 1.18f
          scaleY = 1.18f
        }
        .blur(PlayerConstants.BLUR_RADIUS)
    )
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(gradientBrush)
    )
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              uiColors.surfaceScrim,
              uiColors.surfaceFade
            )
          )
        )
    )
  }
}

@Composable
private fun CoverOrLyricsPanel(
  painter: AsyncImagePainter,
  lyrics: List<String>,
  playingPosition: PlayingPosition,
  showLyrics: Boolean,
  onSeek: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  if (showLyrics && lyrics.isNotEmpty()) {
    ReplaceCoverLyricsPanel(
      lyrics = lyrics,
      playingPosition = playingPosition,
      onSeek = onSeek,
      modifier = modifier
    )
  } else {
    AlbumCover(painter = painter, modifier = modifier)
  }
}

@Composable
private fun ReplaceCoverLyricsPanel(
  lyrics: List<String>,
  playingPosition: PlayingPosition,
  onSeek: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val uiColors = LocalPlayerUiColors.current
  val lyricTimestamps = remember(lyrics) { lyrics.map(::leadingTimestampMs) }
  val displayLyrics = remember(lyrics) { lyrics.map { it.removeLeadingTimestamp().trim() } }
  val activeLineIndex = remember(lyricTimestamps, playingPosition.current) {
    findActiveLyricIndex(lyricTimestamps, playingPosition.current)
  }
  val listState = rememberLazyListState()

  LaunchedEffect(activeLineIndex) {
    if (activeLineIndex >= 0) {
      listState.animateScrollToItem(maxOf(activeLineIndex - 2, 0))
    }
  }

  Surface(
    modifier = modifier,
    color = uiColors.lyricsPanelScrim,
    shape = MaterialTheme.shapes.large
  ) {
    if (lyrics.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Outlined.Lyrics,
          contentDescription = null,
          tint = uiColors.secondaryForeground,
          modifier = Modifier.size(40.dp)
        )
      }
    } else {
      LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        itemsIndexed(displayLyrics) { index, displayText ->
          val timestamp = lyricTimestamps[index]
          if (displayText.isBlank()) {
            Spacer(modifier = Modifier.height(18.dp))
          } else {
            Text(
              text = displayText,
              textAlign = TextAlign.Center,
              style = if (index == activeLineIndex) {
                MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
              } else {
                MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
              },
              color = if (index == activeLineIndex) uiColors.primaryForeground else uiColors.disabledForeground,
              modifier = Modifier
                .fillMaxWidth()
                .let { m ->
                  if (timestamp != null) {
                    m.clickable { onSeek(timestamp.toInt()) }
                  } else {
                    m
                  }
                }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PortraitPlayerLayout(
  painter: AsyncImagePainter,
  playingTrack: TrackInfo,
  playingPosition: PlayingPosition,
  lyrics: List<String>,
  showLyrics: Boolean,
  hasLyrics: Boolean,
  volumeState: VolumeState,
  playbackState: PlaybackState,
  gradientBrush: Brush,
  topInset: androidx.compose.ui.unit.Dp,
  bottomInset: androidx.compose.ui.unit.Dp,
  actions: IPlayerActions,
  onNavigateToAlbum: (album: String, artist: String) -> Unit,
  onNavigateToArtist: (artist: String) -> Unit,
  onLyricsClick: () -> Unit,
  onOutputClick: () -> Unit,
  onQueueClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(
        top = topInset + 8.dp,
        bottom = bottomInset + 20.dp
      ),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .padding(horizontal = 24.dp)
        .fillMaxWidth()
        .aspectRatio(1f)
    ) {
      CoverOrLyricsPanel(
        painter = painter,
        lyrics = lyrics,
        playingPosition = playingPosition,
        showLyrics = showLyrics,
        onSeek = actions.seek,
        modifier = Modifier.fillMaxSize()
      )
    }

    Spacer(modifier = Modifier.height(32.dp))

    TrackInfoPanel(
      track = playingTrack,
      onNavigateToAlbum = onNavigateToAlbum,
      onNavigateToArtist = onNavigateToArtist,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    ProgressSection(
      position = playingPosition,
      onSeek = actions.seek,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    PlaybackControls(
      playbackState = playbackState,
      actions = actions,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    VolumeSection(
      volumeState = volumeState,
      actions = actions,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    PlayerBottomBar(
      hasLyrics = hasLyrics,
      showLyrics = showLyrics,
      onLyricsClick = onLyricsClick,
      onOutputClick = onOutputClick,
      onQueueClick = onQueueClick,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
    )

    Spacer(modifier = Modifier.height(32.dp))
  }
}

@Composable
private fun TabletPlayerLayout(
  painter: AsyncImagePainter,
  playingTrack: TrackInfo,
  playingPosition: PlayingPosition,
  lyrics: List<String>,
  showLyrics: Boolean,
  hasLyrics: Boolean,
  volumeState: VolumeState,
  playbackState: PlaybackState,
  gradientBrush: Brush,
  topInset: androidx.compose.ui.unit.Dp,
  bottomInset: androidx.compose.ui.unit.Dp,
  actions: IPlayerActions,
  onNavigateToAlbum: (album: String, artist: String) -> Unit,
  onNavigateToArtist: (artist: String) -> Unit,
  onLyricsClick: () -> Unit,
  onOutputClick: () -> Unit,
  onQueueClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  // For tablets in portrait, use a centered layout with max width constraint
  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(
        top = topInset + PlayerConstants.CONTENT_PADDING,
        bottom = bottomInset
      ),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .widthIn(max = 500.dp)
        .verticalScroll(rememberScrollState())
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Album cover
      CoverOrLyricsPanel(
        painter = painter,
        lyrics = lyrics,
        playingPosition = playingPosition,
        showLyrics = showLyrics,
        onSeek = actions.seek,
        modifier = Modifier
          .size(320.dp)
      )

      Spacer(modifier = Modifier.height(40.dp))

      // Track info
      TrackInfoPanel(
        track = playingTrack,
        onNavigateToAlbum = onNavigateToAlbum,
        onNavigateToArtist = onNavigateToArtist,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(32.dp))

      // Progress bar
      ProgressSection(
        position = playingPosition,
        onSeek = actions.seek,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Playback controls
      PlaybackControls(
        playbackState = playbackState,
        actions = actions,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(32.dp))

      // Volume control
      VolumeSection(
        volumeState = volumeState,
        actions = actions,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(32.dp))

      PlayerBottomBar(
        hasLyrics = hasLyrics,
        showLyrics = showLyrics,
        onLyricsClick = onLyricsClick,
        onOutputClick = onOutputClick,
        onQueueClick = onQueueClick,
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@Composable
private fun LandscapePlayerLayout(
  painter: AsyncImagePainter,
  playingTrack: TrackInfo,
  playingPosition: PlayingPosition,
  lyrics: List<String>,
  showLyrics: Boolean,
  hasLyrics: Boolean,
  volumeState: VolumeState,
  playbackState: PlaybackState,
  gradientBrush: Brush,
  topInset: androidx.compose.ui.unit.Dp,
  bottomInset: androidx.compose.ui.unit.Dp,
  actions: IPlayerActions,
  onNavigateToAlbum: (album: String, artist: String) -> Unit,
  onNavigateToArtist: (artist: String) -> Unit,
  onLyricsClick: () -> Unit,
  onOutputClick: () -> Unit,
  onQueueClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxSize()
      .padding(
        top = topInset + PlayerConstants.CONTENT_PADDING,
        start = PlayerConstants.CONTENT_PADDING,
        end = PlayerConstants.CONTENT_PADDING,
        bottom = bottomInset + PlayerConstants.CONTENT_PADDING
      ),
    horizontalArrangement = Arrangement.spacedBy(32.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Left side - Album cover (constrained size)
    Box(
      modifier = Modifier.weight(1f),
      contentAlignment = Alignment.Center
    ) {
      CoverOrLyricsPanel(
        painter = painter,
        lyrics = lyrics,
        playingPosition = playingPosition,
        showLyrics = showLyrics,
        onSeek = actions.seek,
        modifier = Modifier
          .fillMaxHeight(PlayerConstants.LANDSCAPE_ALBUM_HEIGHT_FRACTION)
          .aspectRatio(1f)
      )
    }

    // Right side - Controls
    Column(
      modifier = Modifier
        .weight(1f)
        .fillMaxHeight(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Track info with favorite/ban
      TrackInfoPanel(
        track = playingTrack,
        onNavigateToAlbum = onNavigateToAlbum,
        onNavigateToArtist = onNavigateToArtist,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Progress bar
      ProgressSection(
        position = playingPosition,
        onSeek = actions.seek,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Playback controls
      PlaybackControls(
        playbackState = playbackState,
        actions = actions,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Volume control
      VolumeSection(
        volumeState = volumeState,
        actions = actions,
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@Composable
private fun AlbumCover(painter: AsyncImagePainter, modifier: Modifier = Modifier) {
  val placeholderPainter = painterResource(CoreUiR.drawable.ic_image_no_cover)
  val painterState by painter.state.collectAsStateWithLifecycle()

  Surface(
    modifier = modifier
      .shadow(
        elevation = 24.dp,
        shape = MaterialTheme.shapes.medium,
        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
      )
      .clip(MaterialTheme.shapes.medium),
    tonalElevation = 0.dp
  ) {
    val activePainter = when (painterState) {
      is AsyncImagePainter.State.Success -> painter
      is AsyncImagePainter.State.Error -> placeholderPainter
      else -> placeholderPainter
    }

    Image(
      painter = activePainter,
      contentDescription = stringResource(R.string.description_album_cover),
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize()
    )
  }
}

@Composable
private fun TrackInfoPanel(
  track: TrackInfo,
  onNavigateToAlbum: (album: String, artist: String) -> Unit,
  onNavigateToArtist: (artist: String) -> Unit,
  modifier: Modifier = Modifier
) {
  val uiColors = LocalPlayerUiColors.current
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = track.title.ifEmpty { stringResource(R.string.unknown_title) },
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold,
      color = uiColors.primaryForeground,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center,
      modifier = Modifier.height(28.dp)
    )

    Spacer(modifier = Modifier.height(4.dp))

    val artistText = track.artist.ifEmpty { stringResource(R.string.unknown_artist) }
    
    Row(
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      if (track.album.isNotEmpty()) {
        Text(
          text = track.album,
          style = MaterialTheme.typography.bodyLarge,
          color = uiColors.secondaryForeground,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .weight(1f, fill = false)
            .clickable { onNavigateToAlbum(track.album, track.artist) }
        )
        Text(
          text = " - ",
          style = MaterialTheme.typography.bodyLarge,
          color = uiColors.secondaryForeground
        )
      }
      Text(
        text = artistText,
        style = MaterialTheme.typography.bodyLarge,
        color = uiColors.secondaryForeground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .weight(1f, fill = false)
          .clickable { onNavigateToArtist(track.artist) }
      )
    }
  }
}

@Composable
private fun PlayerBottomBar(
  hasLyrics: Boolean,
  showLyrics: Boolean,
  onLyricsClick: () -> Unit,
  onOutputClick: () -> Unit,
  onQueueClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiColors = LocalPlayerUiColors.current
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically
  ) {
    IconButton(onClick = onOutputClick) {
      Icon(
        imageVector = Icons.Default.SpeakerGroup,
        contentDescription = stringResource(R.string.output_selection_title),
        tint = uiColors.primaryForeground,
        modifier = Modifier.size(24.dp)
      )
    }

    IconButton(
      onClick = onLyricsClick,
      enabled = hasLyrics
    ) {
      Icon(
        imageVector = if (showLyrics) Icons.Filled.Lyrics else Icons.Outlined.Lyrics,
        contentDescription = stringResource(R.string.nav_lyrics),
        tint = if (hasLyrics) uiColors.primaryForeground else uiColors.disabledForeground,
        modifier = Modifier.size(24.dp)
      )
    }

    IconButton(onClick = onQueueClick) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
        contentDescription = stringResource(R.string.nav_queue),
        tint = uiColors.primaryForeground,
        modifier = Modifier.size(24.dp)
      )
    }
  }
}

@Composable
private fun ProgressSection(
  position: PlayingPosition,
  onSeek: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val uiColors = LocalPlayerUiColors.current
  var sliderPosition by remember { mutableFloatStateOf(0f) }
  var isUserSeeking by remember { mutableStateOf(false) }
  var ignoreServerUpdates by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  val isStream = position.isStream
  val totalMs = position.total.toFloat().coerceAtLeast(1f)
  val currentNormalized = if (isStream) 0f else position.current.toFloat() / totalMs

  val animatedSliderPosition by animateFloatAsState(
    targetValue = if (isUserSeeking || ignoreServerUpdates) sliderPosition else currentNormalized,
    label = "progress_slider_animation"
  )

  LaunchedEffect(position.current) {
    if (!isUserSeeking && !ignoreServerUpdates && !isStream) {
      sliderPosition = currentNormalized
    }
  }

  Column(modifier = modifier) {
    ThinSlider(
      value = animatedSliderPosition,
      onValueChange = {
        isUserSeeking = true
        sliderPosition = it
      },
      onValueChangeFinished = {
        onSeek((sliderPosition * totalMs).toInt())
        isUserSeeking = false
        ignoreServerUpdates = true
        // Keep ignoring server updates for a bit after release to prevent jumping
        scope.launch {
          delay(PlayerConstants.SLIDER_DEBOUNCE_MS)
          ignoreServerUpdates = false
        }
      },
      enabled = !isStream,
      trackColor = uiColors.primaryForeground,
      inactiveTrackColor = uiColors.disabledForeground,
      thumbColor = uiColors.primaryForeground,
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(4.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = position.currentMinutes,
        style = MaterialTheme.typography.labelSmall,
        color = uiColors.secondaryForeground
      )
      Text(
        text = position.totalMinutes,
        style = MaterialTheme.typography.labelSmall,
        color = uiColors.secondaryForeground
      )
    }
  }
}

@Composable
private fun VolumeSection(
  volumeState: VolumeState,
  actions: IPlayerActions,
  modifier: Modifier = Modifier
) {
  val uiColors = LocalPlayerUiColors.current
  var sliderPosition by remember { mutableFloatStateOf(0f) }
  var isUserDragging by remember { mutableStateOf(false) }
  var ignoreServerUpdates by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  val volumeNormalized = volumeState.volume.toFloat() / PlayerConstants.VOLUME_MAX

  LaunchedEffect(volumeState.volume) {
    if (!isUserDragging && !ignoreServerUpdates) {
      sliderPosition = volumeNormalized
    }
  }

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Icon(
      imageVector = if (volumeState.mute || volumeState.volume == 0) {
        Icons.AutoMirrored.Filled.VolumeOff
      } else {
        Icons.AutoMirrored.Filled.VolumeUp
      },
      contentDescription = stringResource(R.string.main_button_mute_description),
      tint = uiColors.secondaryForeground,
      modifier = Modifier
        .size(20.dp)
        .clickable(onClick = actions.mute)
    )

    // Thin volume slider with local state for smooth dragging
    ThinSlider(
      value = if (volumeState.mute) {
        0f
      } else if (isUserDragging || ignoreServerUpdates) {
        sliderPosition
      } else {
        volumeNormalized
      },
      onValueChange = {
        isUserDragging = true
        sliderPosition = it
        actions.changeVolume((it * PlayerConstants.VOLUME_MAX).toInt())
      },
      onValueChangeFinished = {
        isUserDragging = false
        ignoreServerUpdates = true
        // Keep ignoring server updates for a bit after release to prevent jumping
        scope.launch {
          delay(PlayerConstants.SLIDER_DEBOUNCE_MS)
          ignoreServerUpdates = false
        }
      },
      trackColor = uiColors.primaryForeground,
      inactiveTrackColor = uiColors.disabledForeground,
      thumbColor = uiColors.primaryForeground,
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
private fun PlaybackControls(playbackState: PlaybackState, actions: IPlayerActions, modifier: Modifier = Modifier) {
  val uiColors = LocalPlayerUiColors.current
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Shuffle button
    ShuffleButton(
      shuffleMode = playbackState.shuffle,
      onClick = actions.shuffle
    )

    // Previous button - larger
    IconButton(
      onClick = actions.previous,
      modifier = Modifier.size(56.dp)
    ) {
      Icon(
        imageVector = Icons.Default.SkipPrevious,
        contentDescription = stringResource(R.string.main_button_previous_description),
        tint = uiColors.primaryForeground,
        modifier = Modifier.size(36.dp)
      )
    }

    // Play/Pause button - large filled circle
    FilledIconButton(
      onClick = actions.playPause,
      modifier = Modifier.size(64.dp),
      shape = CircleShape,
      colors = IconButtonDefaults.filledIconButtonColors(
        containerColor = uiColors.playButtonContainer,
        contentColor = uiColors.playButtonContent
      )
    ) {
      Icon(
        imageVector = if (playbackState.playerState == PlayerState.Playing) {
          Icons.Default.Pause
        } else {
          Icons.Default.PlayArrow
        },
        contentDescription = stringResource(R.string.main_button_play_pause_description),
        modifier = Modifier.size(32.dp)
      )
    }

    // Next button - larger
    IconButton(
      onClick = actions.next,
      modifier = Modifier.size(56.dp)
    ) {
      Icon(
        imageVector = Icons.Default.SkipNext,
        contentDescription = stringResource(R.string.main_button_next_description),
        tint = uiColors.primaryForeground,
        modifier = Modifier.size(36.dp)
      )
    }

    // Repeat button
    IconButton(onClick = actions.repeat) {
      Icon(
        imageVector = if (playbackState.repeat == Repeat.One) {
          Icons.Default.RepeatOne
        } else {
          Icons.Default.Repeat
        },
        contentDescription = stringResource(R.string.main_button_repeat_description),
        tint = if (playbackState.repeat != Repeat.None) uiColors.primaryForeground else uiColors.mutedForeground,
        modifier = Modifier.size(24.dp)
      )
    }
  }
}

@Composable
private fun ShuffleButton(shuffleMode: ShuffleMode, onClick: () -> Unit) {
  val uiColors = LocalPlayerUiColors.current
  val isActive = shuffleMode != ShuffleMode.Off
  val isAutoDj = shuffleMode == ShuffleMode.AutoDJ

  IconButton(onClick = onClick) {
    if (isAutoDj) {
      Icon(
        imageVector = Icons.Default.Headset,
        contentDescription = stringResource(R.string.main_button_shuffle_description),
        tint = uiColors.primaryForeground,
        modifier = Modifier.size(24.dp)
      )
    } else {
      Icon(
        imageVector = Icons.Default.Shuffle,
        contentDescription = stringResource(R.string.main_button_shuffle_description),
        tint = if (isActive) uiColors.primaryForeground else uiColors.mutedForeground,
        modifier = Modifier.size(24.dp)
      )
    }
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
