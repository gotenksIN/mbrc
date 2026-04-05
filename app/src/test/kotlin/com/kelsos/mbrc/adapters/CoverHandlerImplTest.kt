package com.kelsos.mbrc.adapters

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.kelsos.mbrc.core.networking.api.PlaybackApi
import com.kelsos.mbrc.core.networking.protocol.payloads.CoverPayload
import com.kelsos.mbrc.utils.testDispatcher
import com.kelsos.mbrc.utils.testDispatchers
import io.mockk.coEvery
import io.mockk.mockk
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoverHandlerImplTest {

  @Test
  fun `fetchAndStoreCover should store cover inside managed cover directory`() = runTest(testDispatcher) {
    val (filesDir, cacheDir, app) = createApplicationDirs()
    val playbackApi = mockk<PlaybackApi>()
    coEvery { playbackApi.getCover() } returns CoverPayload(status = CoverPayload.SUCCESS, cover = jpegBase64(Color.RED))

    val handler = CoverHandlerImpl(app, playbackApi, testDispatchers)

    val storedCover = File(requireNotNull(Uri.parse(handler.fetchAndStoreCover()).path))

    assertThat(storedCover.parentFile).isEqualTo(File(filesDir, CoverHandlerImpl.COVER_DIR))
    assertThat(storedCover.exists()).isTrue()

    filesDir.deleteRecursively()
    cacheDir.deleteRecursively()
  }

  @Test
  fun `clearCovers should remove the previously stored cover`() = runTest(testDispatcher) {
    val (filesDir, cacheDir, app) = createApplicationDirs()
    val playbackApi = mockk<PlaybackApi>()
    coEvery { playbackApi.getCover() } returns CoverPayload(status = CoverPayload.SUCCESS, cover = jpegBase64(Color.BLUE))

    val handler = CoverHandlerImpl(app, playbackApi, testDispatchers)

    val storedCover = File(requireNotNull(Uri.parse(handler.fetchAndStoreCover()).path))
    handler.clearCovers()

    assertThat(storedCover.exists()).isFalse()
    assertThat(File(filesDir, CoverHandlerImpl.COVER_DIR).listFiles().orEmpty()).isEmpty()

    filesDir.deleteRecursively()
    cacheDir.deleteRecursively()
  }

  private fun createApplicationDirs(): Triple<File, File, Application> {
    val root = File(System.getProperty("java.io.tmpdir"), "cover-handler-${UUID.randomUUID()}")
    val filesDir = File(root, "files").apply { mkdirs() }
    val cacheDir = File(root, "cache").apply { mkdirs() }
    val app = mockk<Application>()
    io.mockk.every { app.filesDir } returns filesDir
    io.mockk.every { app.cacheDir } returns cacheDir
    return Triple(filesDir, cacheDir, app)
  }

  private fun jpegBase64(color: Int): String {
    val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
    val bytes = ByteArrayOutputStream().use { stream ->
      check(bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream))
      stream.toByteArray()
    }
    bitmap.recycle()
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
  }
}
