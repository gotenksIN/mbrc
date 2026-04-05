package com.kelsos.mbrc.service.mediasession

import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.media3.session.MediaSession
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.kelsos.mbrc.core.common.state.BasicTrackInfo
import com.kelsos.mbrc.core.common.state.PlayerState
import com.kelsos.mbrc.core.common.state.PlayingPosition
import com.kelsos.mbrc.core.platform.mediasession.NotificationData
import com.kelsos.mbrc.utils.testDispatcher
import com.kelsos.mbrc.utils.testDispatchers
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNotificationManagerTest {

  @Test
  fun `updatePlayingTrack should not overwrite a newer player state`() = runTest(testDispatcher) {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val notificationManager = mockk<NotificationManager>()
    every { notificationManager.notify(any(), any()) } just Runs

    val captured = mutableListOf<NotificationData>()
    val notificationBuilder = mockk<NotificationBuilder>()
    every { notificationBuilder.createBuilder(any(), any()) } answers {
      captured += firstArg<NotificationData>()
      NotificationCompat.Builder(context, AppNotificationManager.CHANNEL_ID)
    }
    every { notificationBuilder.createPlaceholderBuilder() } returns
      NotificationCompat.Builder(context, AppNotificationManager.CHANNEL_ID)

    val mediaSessionManager = mockk<MediaSessionManager>()
    every { mediaSessionManager.scope } returns CoroutineScope(SupervisorJob() + testDispatcher)
    every { mediaSessionManager.mediaSession } returns mockk<MediaSession>(relaxed = true)

    val appNotificationManager = AppNotificationManagerImpl(
      context = context,
      dispatchers = testDispatchers,
      notificationManager = notificationManager,
      notificationBuilder = notificationBuilder,
      mediaSessionManager = mediaSessionManager,
      coverDecoder = {
        delay(100)
        Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
      }
    )

    appNotificationManager.updatePlayingTrack(
      BasicTrackInfo(title = "Track Title", artist = "Artist", coverUrl = "/tmp/cover.jpg")
    )
    appNotificationManager.updateState(PlayerState.Playing, PlayingPosition(current = 42_000, total = 180_000))
    advanceUntilIdle()

    assertThat(captured).isNotEmpty()
    assertThat(captured.last().track.title).isEqualTo("Track Title")
    assertThat(captured.last().playerState).isEqualTo(PlayerState.Playing)
  }
}
