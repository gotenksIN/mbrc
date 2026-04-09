package com.kelsos.mbrc.feature.playback

import com.kelsos.mbrc.feature.playback.lyrics.LyricsViewModel
import com.kelsos.mbrc.feature.playback.nowplaying.MoveManager
import com.kelsos.mbrc.feature.playback.nowplaying.MoveManagerImpl
import com.kelsos.mbrc.feature.playback.nowplaying.NowPlayingRepository
import com.kelsos.mbrc.feature.playback.nowplaying.NowPlayingRepositoryImpl
import com.kelsos.mbrc.feature.playback.nowplaying.NowPlayingViewModel
import com.kelsos.mbrc.feature.playback.player.PlayerViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for playback feature dependencies.
 *
 * This module provides:
 * - NowPlaying repository and ViewModel
 * - Player ViewModel
 * - Lyrics ViewModel
 * - Rating dialog ViewModel
 * - Move manager for now playing list reordering
 *
 * Required dependencies from other modules:
 * - NowPlayingDao from core/data module
 * - PlaybackApi from core/networking module
 * - AppCoroutineDispatchers from app module
 * - AppStateFlow from app module
 */
val playbackModule = module {
  // Repository
  singleOf(::NowPlayingRepositoryImpl) { bind<NowPlayingRepository>() }

  // MoveManager is factory to prevent tracking stale state across NowPlaying screen visits
  factoryOf(::MoveManagerImpl) { bind<MoveManager>() }

  // ViewModels
  viewModelOf(::PlayerViewModel)
  viewModelOf(::LyricsViewModel)
  viewModelOf(::NowPlayingViewModel)
}
