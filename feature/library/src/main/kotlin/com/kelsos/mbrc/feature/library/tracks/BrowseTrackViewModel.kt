package com.kelsos.mbrc.feature.library.tracks

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.kelsos.mbrc.core.common.settings.LibrarySettings
import com.kelsos.mbrc.core.common.settings.TrackSortPreference
import com.kelsos.mbrc.core.common.state.ConnectionStateFlow
import com.kelsos.mbrc.core.data.library.track.TrackRepository
import com.kelsos.mbrc.feature.library.LibrarySearchModel
import com.kelsos.mbrc.feature.library.domain.LibrarySyncUseCase
import com.kelsos.mbrc.feature.library.queue.QueueHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BrowseTrackViewModel(
  private val repository: TrackRepository,
  private val librarySyncUseCase: LibrarySyncUseCase,
  private val queueHandler: QueueHandler,
  private val searchModel: LibrarySearchModel,
  private val librarySettings: LibrarySettings,
  connectionStateFlow: ConnectionStateFlow
) : BaseTrackViewModel(queueHandler, librarySettings, connectionStateFlow) {
  override fun currentSearchTerm(): String? = searchModel.currentTerm.value.takeIf { it.isNotBlank() }

  val searchQuery: StateFlow<String> = searchModel.currentTerm
  val sortPreference: Flow<TrackSortPreference> = librarySettings.trackSortPreferenceFlow

  override val tracks =
    combine(searchModel.term, sortPreference) { term, sort ->
      Triple(term, sort.field, sort.order)
    }.flatMapLatest { (term, field, order) ->
      if (term.isEmpty()) {
        repository.getAll(field, order)
      } else {
        repository.search(term, field, order)
      }
    }.cachedIn(viewModelScope)

  val showSync = searchModel.term.map { it.isEmpty() }

  fun sync() {
    viewModelScope.launch {
      if (!checkConnection()) {
        emit(TrackUiMessage.NetworkUnavailable)
        return@launch
      }
      librarySyncUseCase.sync()
    }
  }

  fun updateSortPreference(preference: TrackSortPreference) {
    viewModelScope.launch {
      librarySettings.setTrackSortPreference(preference)
    }
  }

  fun playAll(shuffle: Boolean) {
    viewModelScope.launch {
      if (!checkConnection()) {
        emit(TrackUiMessage.NetworkUnavailable)
        return@launch
      }

      val result = queueHandler.playAllTracks(shuffle = shuffle)
      val message = if (result.isSuccess) {
        TrackUiMessage.QueueSuccess(result.getOrNull() ?: 0)
      } else {
        TrackUiMessage.QueueFailed
      }

      emit(message)
    }
  }
}
