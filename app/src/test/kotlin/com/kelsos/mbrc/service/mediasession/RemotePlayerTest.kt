package com.kelsos.mbrc.service.mediasession

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kelsos.mbrc.core.common.state.AppState
import com.kelsos.mbrc.core.common.state.ShuffleMode
import com.kelsos.mbrc.core.networking.protocol.actions.UserAction
import com.kelsos.mbrc.core.networking.protocol.base.Protocol
import com.kelsos.mbrc.core.networking.protocol.usecases.UserActionUseCase
import com.kelsos.mbrc.core.networking.protocol.usecases.VolumeModifyUseCase
import com.kelsos.mbrc.utils.testDispatcher
import com.kelsos.mbrc.utils.testDispatchers
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemotePlayerTest {

  @Test
  fun `handleSetShuffleModeEnabled should send shuffle mode when enabled`() = runTest(testDispatcher) {
    val userActionUseCase = mockk<UserActionUseCase>()
    val volumeModifyUseCase = mockk<VolumeModifyUseCase>(relaxed = true)
    coEvery { userActionUseCase.perform(any()) } just Runs

    val player = createRemotePlayer(
      userActionUseCase,
      volumeModifyUseCase,
      CoroutineScope(SupervisorJob() + testDispatcher)
    )

    player.invokeSetShuffleModeEnabled(true)
    advanceUntilIdle()

    coVerify(exactly = 1) {
      userActionUseCase.perform(UserAction(Protocol.PlayerShuffle, ShuffleMode.SHUFFLE))
    }
  }

  @Test
  fun `handleSetShuffleModeEnabled should send off mode when disabled`() = runTest(testDispatcher) {
    val userActionUseCase = mockk<UserActionUseCase>()
    val volumeModifyUseCase = mockk<VolumeModifyUseCase>(relaxed = true)
    coEvery { userActionUseCase.perform(any()) } just Runs

    val player = createRemotePlayer(
      userActionUseCase,
      volumeModifyUseCase,
      CoroutineScope(SupervisorJob() + testDispatcher)
    )

    player.invokeSetShuffleModeEnabled(false)
    advanceUntilIdle()

    coVerify(exactly = 1) {
      userActionUseCase.perform(UserAction(Protocol.PlayerShuffle, ShuffleMode.OFF))
    }
  }

  private fun createRemotePlayer(
    userActionUseCase: UserActionUseCase,
    volumeModifyUseCase: VolumeModifyUseCase,
    scope: CoroutineScope
  ): RemotePlayer = RemotePlayer(
    context = ApplicationProvider.getApplicationContext<Context>(),
    userActionUseCase = userActionUseCase,
    volumeModifyUseCase = volumeModifyUseCase,
    appState = AppState(),
    dispatchers = testDispatchers,
    scope = scope
  )

  private fun RemotePlayer.invokeSetShuffleModeEnabled(enabled: Boolean) {
    val method = RemotePlayer::class.java.getDeclaredMethod(
      "handleSetShuffleModeEnabled",
      Boolean::class.javaPrimitiveType
    )
    method.isAccessible = true
    method.invoke(this, enabled)
  }
}
