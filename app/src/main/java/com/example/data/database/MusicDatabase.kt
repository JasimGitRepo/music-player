package com.example.data.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.Playlist
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1")
    fun getFavoriteSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE downloadStatus = 2")
    fun getDownloadedSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE id = :songId LIMIT 1")
    suspend fun getSongById(songId: String): Song?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Update
    suspend fun updateSong(song: Song)

    @Query("UPDATE songs SET isFavorite = :isFav WHERE id = :songId")
    suspend fun updateFavoriteStatus(songId: String, isFav: Boolean)

    @Query("UPDATE songs SET downloadStatus = :status, localFilePath = :localPath WHERE id = :songId")
    suspend fun updateDownloadStatus(songId: String, status: Int, localPath: String?)

    @Delete
    suspend fun deleteSong(song: Song)
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun deleteCrossRef(playlistId: Long, songId: String)

    @Query("SELECT * FROM songs INNER JOIN playlist_song_cross_ref ON songs.id = playlist_song_cross_ref.songId WHERE playlist_song_cross_ref.playlistId = :playlistId")
    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>>
}

@Database(entities = [Song::class, Playlist::class, PlaylistSongCrossRef::class], version = 1, exportSchema = false)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
}
