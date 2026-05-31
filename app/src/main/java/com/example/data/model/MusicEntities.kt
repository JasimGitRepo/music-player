package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Junction
import androidx.room.Relation

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: String, // YouTube videoId or generic UUID
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long, // in milliseconds
    val streamUrl: String, // stream URL
    val localFilePath: String? = null, // downloaded file path if present
    val lyrics: String? = null, // Synced LRC lyrics format
    val albumArtUrl: String? = null,
    val isFavorite: Boolean = false,
    val downloadStatus: Int = 0 // 0 = None, 1 = Downloading, 2 = Downloaded
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_song_cross_ref", primaryKeys = ["playlistId", "songId"])
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: String
)

data class PlaylistWithSongs(
    val playlist: Playlist,
    val songs: List<Song>
)
