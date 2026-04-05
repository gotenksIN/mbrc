package com.kelsos.mbrc.state

import android.app.Application
import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.kelsos.mbrc.core.common.state.BasicTrackInfo
import com.kelsos.mbrc.core.common.state.TrackInfo
import com.kelsos.mbrc.core.common.utilities.coroutines.AppCoroutineDispatchers
import com.kelsos.mbrc.store.Store
import com.kelsos.mbrc.store.Track
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber

internal val Context.cacheDataStore: DataStore<Store> by dataStore(
  fileName = "cache_store.db",
  serializer = PlayerStateSerializer
)

interface PlayingTrackCache {
  suspend fun persistInfo(playingTrack: TrackInfo)

  suspend fun restoreInfo(): TrackInfo
}

class PlayingTrackCacheImpl(
  private val context: Application,
  private val dispatchers: AppCoroutineDispatchers
) : PlayingTrackCache {
  private val storeFlow: Flow<Store> =
    context.cacheDataStore.data
      .catch { exception ->
        // dataStore.data throws an IOException when an error is encountered when reading data
        if (exception is IOException) {
          Timber.e(exception, "Error reading sort order preferences.")
          emit(Store())
        } else {
          throw exception
        }
      }

  override suspend fun persistInfo(playingTrack: TrackInfo) {
    withContext(dispatchers.io) {
      try {
        context.cacheDataStore.updateData { store ->
          val track = Track(
            album = playingTrack.album,
            artist = playingTrack.artist,
            path = playingTrack.path,
            title = playingTrack.title,
            year = playingTrack.year
          )

          store.copy(
            track = track,
            cover = playingTrack.coverUrl
          )
        }
      } catch (e: IOException) {
        Timber.e(e, "Failed to persist playing track info")
      }
    }
  }

  override suspend fun restoreInfo(): TrackInfo = withContext(dispatchers.io) {
    val store = storeFlow.first()
    val track = store.track
    if (track != null) {
      BasicTrackInfo(
        artist = track.artist,
        title = track.title,
        album = track.album,
        year = track.year,
        path = track.path,
        coverUrl = store.cover
      )
    } else {
      BasicTrackInfo(
        artist = "",
        title = "",
        album = "",
        year = "",
        path = "",
        coverUrl = store.cover
      )
    }
  }
}

object PlayerStateSerializer : Serializer<Store> {
  override suspend fun readFrom(input: InputStream): Store {
    try {
      return Store.ADAPTER.decode(input)
    } catch (exception: IOException) {
      throw CorruptionException("Cannot read proto.", exception)
    }
  }

  override suspend fun writeTo(t: Store, output: OutputStream) {
    Store.ADAPTER.encode(output, t)
  }

  override val defaultValue: Store
    get() = Store()
}
