package com.kelsos.mbrc.core.networking.protocol.actions

import com.google.common.truth.Truth.assertThat
import com.kelsos.mbrc.core.common.state.BasicTrackInfo
import com.kelsos.mbrc.core.networking.protocol.base.Protocol
import com.kelsos.mbrc.core.networking.protocol.base.ProtocolMessage
import com.squareup.moshi.Moshi
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UpdateNowPlayingTrackTest {

  private val moshi = Moshi.Builder().build()

  @Test
  fun `execute should update track and request details without extra persistence`() = runTest {
    val stateHandler = mockk<PlayerStateHandler>(relaxed = true)
    val notifier = mockk<TrackChangeNotifier>(relaxed = true)
    every { stateHandler.playingTrack } returns flowOf(
      BasicTrackInfo(coverUrl = "cover", duration = 1234L)
    )

    val action = UpdateNowPlayingTrack(stateHandler, notifier, moshi)

    action.execute(
      object : ProtocolMessage {
        override val type = Protocol.NowPlayingTrack
        override val data = mapOf(
          "artist" to "Artist",
          "album" to "Album",
          "title" to "Title",
          "year" to "2025",
          "path" to "/music/file.mp3"
        )
      }
    )

    verify(exactly = 1) {
      stateHandler.updatePlayingTrack(
        withArg { updated ->
          assertThat(updated.artist).isEqualTo("Artist")
          assertThat(updated.album).isEqualTo("Album")
          assertThat(updated.title).isEqualTo("Title")
          assertThat(updated.year).isEqualTo("2025")
          assertThat(updated.path).isEqualTo("/music/file.mp3")
          assertThat(updated.coverUrl).isEqualTo("cover")
          assertThat(updated.duration).isEqualTo(1234L)
        }
      )
    }
    verify(exactly = 1) { notifier.notifyTrackChanged(any()) }
    verify(exactly = 1) { notifier.requestTrackDetails() }
  }
}
