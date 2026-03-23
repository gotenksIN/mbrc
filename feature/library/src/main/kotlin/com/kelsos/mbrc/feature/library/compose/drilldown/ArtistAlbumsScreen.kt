package com.kelsos.mbrc.feature.library.compose.drilldown

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.kelsos.mbrc.core.common.settings.AlbumSortField
import com.kelsos.mbrc.core.common.settings.AlbumSortPreference
import com.kelsos.mbrc.core.common.settings.AlbumViewMode
import com.kelsos.mbrc.core.common.settings.SortOrder
import com.kelsos.mbrc.core.common.settings.SortPreference
import com.kelsos.mbrc.core.common.utilities.AppError
import com.kelsos.mbrc.core.common.utilities.Outcome
import com.kelsos.mbrc.core.data.library.album.Album
import com.kelsos.mbrc.core.queue.Queue
import com.kelsos.mbrc.core.ui.compose.ActionItem
import com.kelsos.mbrc.core.ui.compose.NavigationIconType
import com.kelsos.mbrc.core.ui.compose.PagingGridScreen
import com.kelsos.mbrc.core.ui.compose.PagingListScreen
import com.kelsos.mbrc.core.ui.compose.QueueResultEffect
import com.kelsos.mbrc.core.ui.compose.ScreenScaffold
import com.kelsos.mbrc.feature.library.R
import com.kelsos.mbrc.feature.library.albums.AlbumUiMessage
import com.kelsos.mbrc.feature.library.albums.ArtistAlbumsViewModel
import com.kelsos.mbrc.feature.library.compose.SortBottomSheet
import com.kelsos.mbrc.feature.library.compose.SortOption
import com.kelsos.mbrc.feature.library.compose.components.AlbumGridItem
import com.kelsos.mbrc.feature.library.compose.components.AlbumListItem
import com.kelsos.mbrc.feature.minicontrol.MiniControl
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import org.koin.androidx.compose.koinViewModel

private val albumSortOptions = listOf(
  SortOption(AlbumSortField.NAME, R.string.sort_by_name)
)

@Composable
fun ArtistAlbumsScreen(
  artistName: String,
  onNavigateBack: () -> Unit,
  onNavigateToAlbumTracks: (Album) -> Unit,
  onNavigateToPlayer: () -> Unit,
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier,
  viewModel: ArtistAlbumsViewModel = koinViewModel()
) {
  val albums = viewModel.albums.collectAsLazyPagingItems()
  val sortPreference by viewModel.sortPreference.collectAsStateWithLifecycle(
    initialValue = SortPreference(AlbumSortField.NAME, SortOrder.ASC)
  )
  val albumViewMode by viewModel.albumViewMode.collectAsStateWithLifecycle(
    initialValue = AlbumViewMode.AUTO
  )
  val screenWidthDp = with(LocalDensity.current) {
    LocalWindowInfo.current.containerSize.width.toDp()
  }
  val isGridMode = albumViewMode.isGrid(screenWidthDp.value.toInt())
  var showSortSheet by rememberSaveable { mutableStateOf(false) }

  // Load artist albums
  LaunchedEffect(artistName) {
    viewModel.load(artistName)
  }

  LaunchedEffect(Unit) {
    viewModel.events.filterIsInstance<AlbumUiMessage.OpenAlbumTracks>().collect { event ->
      onNavigateToAlbumTracks(event.album)
    }
  }

  val queueResults = remember {
    viewModel.events.map { event ->
      when (event) {
        is AlbumUiMessage.QueueSuccess -> Outcome.Success(event.tracksCount)
        is AlbumUiMessage.QueueFailed -> Outcome.Failure(AppError.OperationFailed)
        is AlbumUiMessage.NetworkUnavailable -> Outcome.Failure(AppError.NetworkUnavailable)
        else -> null
      }
    }.filterIsInstance<Outcome<Int>>()
  }

  QueueResultEffect(
    queueResults = queueResults,
    snackbarHostState = snackbarHostState
  )

  val sortDescription = stringResource(R.string.sort_button_description)
  val viewModeDescription = stringResource(R.string.album_view_mode_description)
  val playAllLabel = stringResource(R.string.menu_play_all)
  val shuffleAllLabel = stringResource(R.string.menu_shuffle_all)

  ScreenScaffold(
    title = artistName,
    snackbarHostState = snackbarHostState,
    navigationIcon = NavigationIconType.Back(onNavigateBack),
    actionItems = listOf(
      ActionItem(
        icon = if (isGridMode) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
        contentDescription = viewModeDescription,
        onClick = { viewModel.toggleViewMode() }
      ),
      ActionItem(
        icon = Icons.AutoMirrored.Filled.Sort,
        contentDescription = sortDescription,
        onClick = { showSortSheet = true }
      )
    ),
    modifier = modifier
  ) { paddingValues ->
    Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedButton(
          onClick = { viewModel.playArtist(artistName, shuffle = false) },
          modifier = Modifier.weight(1f)
        ) {
          Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
          Text(
            text = playAllLabel,
            modifier = Modifier.padding(start = 8.dp)
          )
        }

        Button(
          onClick = { viewModel.playArtist(artistName, shuffle = true) },
          modifier = Modifier.weight(1f)
        ) {
          Icon(imageVector = Icons.Default.Shuffle, contentDescription = null)
          Text(
            text = shuffleAllLabel,
            modifier = Modifier.padding(start = 8.dp)
          )
        }
      }

      if (isGridMode) {
        PagingGridScreen(
          items = albums,
          modifier = Modifier.weight(1f),
          emptyMessage = stringResource(R.string.albums_list_empty),
          emptyIcon = Icons.Default.Album,
          key = { it.id }
        ) { album ->
          AlbumGridItem(
            album = album,
            onClick = { viewModel.queue(Queue.Default, album) },
            onQueue = { queue -> viewModel.queue(queue, album) }
          )
        }
      } else {
        PagingListScreen(
          items = albums,
          modifier = Modifier.weight(1f),
          emptyMessage = stringResource(R.string.albums_list_empty),
          emptyIcon = Icons.Default.Album,
          key = { it.id }
        ) { album ->
          AlbumListItem(
            album = album,
            onClick = { viewModel.queue(Queue.Default, album) },
            onQueue = { queue -> viewModel.queue(queue, album) }
          )
        }
      }

      MiniControl(
        onNavigateToPlayer = onNavigateToPlayer,
        snackbarHostState = snackbarHostState
      )
    }
  }

  if (showSortSheet) {
    SortBottomSheet(
      title = stringResource(R.string.sort_title),
      options = albumSortOptions,
      selectedField = sortPreference.field,
      selectedOrder = sortPreference.order,
      onSortSelected = { field, order ->
        viewModel.updateSortPreference(AlbumSortPreference(field, order))
      },
      onDismiss = { showSortSheet = false }
    )
  }
}
