package com.kelsos.mbrc.feature.playback.nowplaying.compose

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.kelsos.mbrc.core.common.state.ConnectionStatus
import com.kelsos.mbrc.core.data.library.track.Track
import com.kelsos.mbrc.core.data.library.track.TrackRepository
import com.kelsos.mbrc.core.data.nowplaying.NowPlaying
import com.kelsos.mbrc.core.ui.compose.EmptyScreen
import com.kelsos.mbrc.core.ui.compose.LoadingScreen
import com.kelsos.mbrc.core.ui.compose.dragContainer
import com.kelsos.mbrc.core.ui.compose.rememberDragDropState
import com.kelsos.mbrc.feature.library.compose.components.AlbumCoverByKey
import com.kelsos.mbrc.feature.playback.R
import com.kelsos.mbrc.feature.playback.nowplaying.NowPlayingViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import com.kelsos.mbrc.core.ui.R as UiR
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
  onDismiss: () -> Unit,
  onGoToAlbum: ((album: String, artist: String) -> Unit)?,
  onGoToArtist: (artist: String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: NowPlayingViewModel = koinViewModel()
) {
  val trackRepository: TrackRepository = koinInject()
  val tracks = viewModel.tracks.collectAsLazyPagingItems()
  val playingTrack by viewModel.playingTrack.collectAsStateWithLifecycle()
  val connectionState by viewModel.connectionState.collectAsStateWithLifecycle(
    initialValue = ConnectionStatus.Offline
  )
  val isConnected = connectionState is ConnectionStatus.Connected

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val lazyListState = rememberLazyListState()
  val draggableList = remember { mutableListOf<NowPlaying>().toMutableStateList() }
  val trackMetadata = remember { mutableStateMapOf<String, Track?>() }

  val dataSignature = remember(tracks.itemSnapshotList) {
    tracks.itemSnapshotList.items.map { it.id }.hashCode()
  }

  LaunchedEffect(dataSignature) {
    val newItems = tracks.itemSnapshotList.items
    val needsUpdate = draggableList.size != newItems.size ||
      draggableList.zip(newItems).any { (old, new) -> old.id != new.id }

    if (needsUpdate) {
      draggableList.clear()
      draggableList.addAll(newItems)
    }
  }

  LaunchedEffect(dataSignature) {
    val newItems = tracks.itemSnapshotList.items
    if (newItems.isEmpty()) return@LaunchedEffect

    androidx.compose.runtime.snapshotFlow {
      val first = lazyListState.firstVisibleItemIndex
      val last = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: first
      first to last
    }.collect { (firstIndex, visibleEndIndex) ->
      val first = firstIndex.coerceAtMost(newItems.lastIndex)
      val last = visibleEndIndex.coerceAtMost(newItems.lastIndex)
      val prefetchStart = (first - 10).coerceAtLeast(0)
      val prefetchEnd = (last + 10).coerceAtMost(newItems.lastIndex)

      val candidatePaths = newItems
        .subList(prefetchStart, prefetchEnd + 1)
        .map { it.path }
        .distinct()
        .filterNot { trackMetadata.containsKey(it) }

      if (candidatePaths.isNotEmpty()) {
        candidatePaths.forEach { trackMetadata[it] = null }

        candidatePaths.chunked(12).forEach { batch ->
          batch.forEach { path ->
            val track = trackRepository.getByPath(path)
            if (track != null) {
              trackMetadata[path] = track
            }
          }
          kotlinx.coroutines.yield()
        }
      }
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    modifier = modifier
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
    ) {
      Text(
        text = stringResource(R.string.nav_queue),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
      )

      Box(modifier = Modifier.weight(1f)) {
        when (val refreshState = tracks.loadState.refresh) {
          is LoadState.Loading if tracks.itemCount == 0 && draggableList.isEmpty() -> LoadingScreen()

          is LoadState.Error if tracks.itemCount == 0 && draggableList.isEmpty() -> {
            EmptyScreen(
              message = refreshState.error.message ?: stringResource(R.string.refresh_failed),
              icon = Icons.AutoMirrored.Filled.QueueMusic
            )
          }

          is LoadState.NotLoading if tracks.itemCount == 0 && draggableList.isEmpty() -> {
            EmptyScreen(
              message = stringResource(R.string.now_playing__empty_list),
              icon = Icons.AutoMirrored.Filled.QueueMusic
            )
          }

          else -> {
            QueueTrackList(
              lazyListState = lazyListState,
              draggableList = draggableList,
              tracks = tracks,
              playingTrackPath = playingTrack.path,
              isConnected = isConnected,
              trackMetadata = trackMetadata,
              onTrackClick = { position -> viewModel.actions.play(position) },
              onTrackRemove = { position -> viewModel.actions.removeTrack(position) },
              onTrackMove = { from, to -> viewModel.actions.moveTrack(from, to) },
              onDragEnd = { viewModel.actions.move() },
              onGoToAlbum = onGoToAlbum,
              onGoToArtist = onGoToArtist
            )
          }
        }
      }
    }
  }
}

@Composable
private fun QueueTrackList(
  lazyListState: LazyListState,
  draggableList: SnapshotStateList<NowPlaying>,
  tracks: LazyPagingItems<NowPlaying>,
  playingTrackPath: String,
  isConnected: Boolean,
  trackMetadata: Map<String, Track?>,
  onTrackClick: (Int) -> Unit,
  onTrackRemove: (Int) -> Unit,
  onTrackMove: (Int, Int) -> Unit,
  onDragEnd: () -> Unit,
  onGoToAlbum: ((album: String, artist: String) -> Unit)?,
  onGoToArtist: (artist: String) -> Unit,
  modifier: Modifier = Modifier
) {
  val dragDropState = rememberDragDropState(
    lazyListState = lazyListState,
    onMove = { from, to ->
      draggableList.add(to, draggableList.removeAt(from))
      onTrackMove(from, to)
    },
    onDragEnd = onDragEnd
  )

  val dragModifier = if (isConnected) Modifier.dragContainer(dragDropState) else Modifier

  LazyColumn(
    state = lazyListState,
    modifier = modifier
      .fillMaxSize()
      .then(dragModifier),
    flingBehavior = ScrollableDefaults.flingBehavior()
  ) {
    itemsIndexed(
      items = draggableList,
      key = { _, track -> track.id }
    ) { index, track ->
      val resolvedTrack = trackMetadata[track.path]

      QueueTrackItem(
        track = track,
        resolvedTrack = resolvedTrack,
        isPlaying = track.path == playingTrackPath,
        isDragging = index == dragDropState.draggingItemIndex,
        onClick = { onTrackClick(track.position) },
        onRemove = { onTrackRemove(index) },
        onGoToAlbum = if (resolvedTrack != null && resolvedTrack.album.isNotBlank() && onGoToAlbum != null) {
          {
            onGoToAlbum(
              resolvedTrack.album,
              resolvedTrack.albumArtist.ifBlank { resolvedTrack.artist }
            )
          }
        } else {
          null
        },
        onGoToArtist = { onGoToArtist(resolvedTrack?.artist ?: track.artist) },
        modifier = Modifier.animateItem()
      )
    }

    if (tracks.loadState.append is LoadState.Loading) {
      item(contentType = "loading") {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator()
        }
      }
    }
  }
}

@Composable
private fun QueueTrackItem(
  track: NowPlaying,
  resolvedTrack: Track?,
  isPlaying: Boolean,
  isDragging: Boolean,
  onClick: () -> Unit,
  onRemove: () -> Unit,
  onGoToAlbum: (() -> Unit)?,
  onGoToArtist: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showMenu by remember { mutableStateOf(false) }
  val elevation by animateDpAsState(
    targetValue = if (isDragging) 8.dp else 0.dp,
    label = "queue_drag_elevation"
  )

  val backgroundColor = if (isPlaying) {
    MaterialTheme.colorScheme.surfaceContainerHighest
  } else {
    MaterialTheme.colorScheme.surface
  }

  Card(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(defaultElevation = elevation),
    colors = CardDefaults.cardColors(containerColor = backgroundColor),
    shape = RoundedCornerShape(0.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Min)
    ) {
      Box(
        modifier = Modifier
          .width(4.dp)
          .fillMaxHeight()
          .background(if (isPlaying) MaterialTheme.colorScheme.primary else Color.Transparent)
      )

      Row(
        modifier = Modifier
          .weight(1f)
          .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.DragHandle,
          contentDescription = stringResource(R.string.now_playing__drag_handle),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (resolvedTrack != null && resolvedTrack.album.isNotBlank()) {
          AlbumCoverByKey(
            artist = resolvedTrack.albumArtist.ifBlank { resolvedTrack.artist },
            album = resolvedTrack.album,
            modifier = Modifier.background(
              MaterialTheme.colorScheme.surfaceVariant,
              RoundedCornerShape(4.dp)
            )
          )
        } else {
          AsyncImage(
            model = "",
            contentDescription = null,
            placeholder = painterResource(UiR.drawable.ic_image_no_cover),
            error = painterResource(UiR.drawable.ic_image_no_cover),
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .size(48.dp)
              .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = track.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = track.artist,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Box {
          IconButton(onClick = { showMenu = true }) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = stringResource(R.string.menu_overflow_description),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
          ) {
            if (onGoToAlbum != null) {
              DropdownMenuItem(
                text = { Text(stringResource(R.string.player_go_to_album)) },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.Album,
                    contentDescription = null
                  )
                },
                onClick = {
                  showMenu = false
                  onGoToAlbum()
                }
              )
            }
            DropdownMenuItem(
              text = { Text(stringResource(R.string.player_go_to_artist)) },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null
                )
              },
              onClick = {
                showMenu = false
                onGoToArtist()
              }
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.menu_remove_track)) },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = null
                )
              },
              onClick = {
                showMenu = false
                onRemove()
              }
            )
          }
        }
      }
    }
  }
}
