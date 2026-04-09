package com.kelsos.mbrc.core.common.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface AppStateFlow {
  val playerStatus: StateFlow<PlayerStatusModel>
  val playingTrack: StateFlow<TrackInfo>
  val playingTrackRating: StateFlow<TrackRating>
  val playingTrackDetails: StateFlow<TrackDetails>
  val playingPosition: StateFlow<PlayingPosition>
  val lyrics: StateFlow<List<String>>
}

interface AppStatePublisher : AppStateFlow {
  fun updatePlayerStatus(status: PlayerStatusModel)
  fun updatePlayerStatus(block: (PlayerStatusModel) -> PlayerStatusModel)

  fun updatePlayingTrack(track: TrackInfo)
  fun updatePlayingTrack(block: (TrackInfo) -> TrackInfo)

  fun updateTrackRating(rating: TrackRating)
  fun updateTrackRating(block: (TrackRating) -> TrackRating)

  fun updateTrackDetails(details: TrackDetails)
  fun updateTrackDetails(block: (TrackDetails) -> TrackDetails)

  fun updatePlayingPosition(position: PlayingPosition)
  fun updatePlayingPosition(block: (PlayingPosition) -> PlayingPosition)

  fun updateLyrics(lyrics: List<String>)
  fun updateLyrics(block: (List<String>) -> List<String>)
}

class AppState : AppStatePublisher {
  private val _playerStatus = MutableStateFlow(PlayerStatusModel())
  private val _playingTrack = MutableStateFlow<TrackInfo>(BasicTrackInfo())
  private val _playingTrackRating = MutableStateFlow(TrackRating())
  private val _playingTrackDetails = MutableStateFlow(TrackDetails.EMPTY)
  private val _playingPosition = MutableStateFlow(PlayingPosition())
  private val _lyrics = MutableStateFlow(emptyList<String>())

  override val playerStatus: StateFlow<PlayerStatusModel> = _playerStatus.asStateFlow()
  override val playingTrack: StateFlow<TrackInfo> = _playingTrack.asStateFlow()
  override val playingTrackRating: StateFlow<TrackRating> = _playingTrackRating.asStateFlow()
  override val playingTrackDetails: StateFlow<TrackDetails> = _playingTrackDetails.asStateFlow()
  override val playingPosition: StateFlow<PlayingPosition> = _playingPosition.asStateFlow()
  override val lyrics: StateFlow<List<String>> = _lyrics.asStateFlow()

  override fun updatePlayerStatus(status: PlayerStatusModel) {
    _playerStatus.value = status
  }

  override fun updatePlayerStatus(block: (PlayerStatusModel) -> PlayerStatusModel) {
    _playerStatus.update(block)
  }

  override fun updatePlayingTrack(track: TrackInfo) {
    _playingTrack.value = track
  }

  override fun updatePlayingTrack(block: (TrackInfo) -> TrackInfo) {
    _playingTrack.update(block)
  }

  override fun updateTrackRating(rating: TrackRating) {
    _playingTrackRating.value = rating
  }

  override fun updateTrackRating(block: (TrackRating) -> TrackRating) {
    _playingTrackRating.update(block)
  }

  override fun updateTrackDetails(details: TrackDetails) {
    _playingTrackDetails.value = details
  }

  override fun updateTrackDetails(block: (TrackDetails) -> TrackDetails) {
    _playingTrackDetails.update(block)
  }

  override fun updatePlayingPosition(position: PlayingPosition) {
    _playingPosition.value = position
  }

  override fun updatePlayingPosition(block: (PlayingPosition) -> PlayingPosition) {
    _playingPosition.update(block)
  }

  override fun updateLyrics(lyrics: List<String>) {
    _lyrics.value = lyrics
  }

  override fun updateLyrics(block: (List<String>) -> List<String>) {
    _lyrics.update(block)
  }
}
