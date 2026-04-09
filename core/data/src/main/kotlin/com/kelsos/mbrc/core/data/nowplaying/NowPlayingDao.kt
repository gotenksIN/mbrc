package com.kelsos.mbrc.core.data.nowplaying

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

@Dao
abstract class NowPlayingDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract fun insertAll(list: List<NowPlayingEntity>)

  @Query("delete from now_playing")
  abstract fun deleteAll()

  @Query("select * from now_playing order by sort_index")
  abstract fun getAll(): PagingSource<Int, NowPlayingEntity>

  @Query("select * from now_playing order by sort_index")
  abstract fun all(): List<NowPlayingEntity>

  @Query("select id, position, path from now_playing")
  abstract fun cached(): List<CachedNowPlaying>

  @Query(
    """
      select * from now_playing
      where title like '%' || :term || '%'
      or artist like '%' || :term || '%'
      """
  )
  abstract fun search(term: String): PagingSource<Int, NowPlayingEntity>

  @Query(
    """
      select * from now_playing
      where title like '%' || :term || '%'
      or artist like '%' || :term || '%'
      """
  )
  abstract fun simpleSearch(term: String): List<NowPlayingEntity>

  @Query("select count(*) from now_playing")
  abstract fun count(): Long

  @Query("select count(*) from now_playing")
  abstract fun observeCount(): Flow<Int>

  @Query("delete from now_playing where date_added != :added")
  abstract fun removePreviousEntries(added: Long)

  @Query("delete from now_playing where position = :position")
  abstract fun removeByPosition(position: Int): Int

  @Query(
    "update now_playing set position = position - 1, sort_index = sort_index - 1 where position > :position "
  )
  abstract fun updateRemoved(position: Int): Int

  @Transaction
  open fun remove(position: Int) {
    val deleted = removeByPosition(position)
    val updated = updateRemoved(position)
    Timber.v("deleted $deleted rows and updated $updated")
  }

  @Query("select id from now_playing where position = :position")
  abstract fun findIdByPosition(position: Int): Long

  @Query("update now_playing set position = :position, sort_index = :position where id = :id")
  abstract fun updatePosition(id: Long, position: Int)

  @Query(
    """
    update now_playing set position = position - 1, sort_index = sort_index - 1
    where position > :from
    and position <= :to
    """
  )
  abstract fun updateMovedDown(from: Int, to: Int): Int

  @Query(
    """
    update now_playing set position = position + 1, sort_index = sort_index + 1
    where position < :from
    and position >= :to
    """
  )
  abstract fun updateMovedUp(from: Int, to: Int): Int

  @Transaction
  open fun move(from: Int, to: Int) {
    val fromId = findIdByPosition(from)
    if (from < to) {
      updateMovedDown(from, to)
    } else if (from > to) {
      updateMovedUp(from, to)
    }

    updatePosition(fromId, to)
  }

  @Query(
    """
        select position from now_playing
        where title like '%' || :query || '%'
        or artist like '%' || :query || '%'
        """
  )
  abstract fun findPositionByQuery(query: String): Int?

  @Query(
    """
        select position, title from now_playing
        where title like '%' || :query || '%'
        or artist like '%' || :query || '%'
        limit 1
        """
  )
  abstract fun searchTrack(query: String): SearchResult?

  @Update
  abstract fun update(existing: List<NowPlayingEntity>)

  @Query("select * from now_playing where id = :id")
  abstract fun getById(id: Long): NowPlayingEntity?
}
