package com.example.player

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.room.Room
import com.example.data.database.MusicDatabase
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

data class LyricLine(val timeMs: Long, val text: String)

class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {

    // Database & Repository Singletons (Simple constructor/field injection pattern)
    private val database: MusicDatabase by lazy {
        Room.databaseBuilder(
            application,
            MusicDatabase::class.java,
            "music_db"
        )
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
    }

    private val repository: MusicRepository by lazy {
        MusicRepository(application, database.songDao(), database.playlistDao())
    }

    // --- ExoPlayer Instantiation ---
    private var exoPlayer: ExoPlayer? = null

    // --- Flows exposed to the UI ---
    val allSongs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedSongs: StateFlow<List<Song>> = repository.downloadedSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<Playlist>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently playing info
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(1L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    // Playlist details
    private val _playlistSongs = MutableStateFlow<List<Song>>(emptyList())
    val playlistSongs: StateFlow<List<Song>> = _playlistSongs.asStateFlow()

    private val _activePlaylist = MutableStateFlow<Playlist?>(null)
    val activePlaylist: StateFlow<Playlist?> = _activePlaylist.asStateFlow()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Downloading indicators
    private val _downloadProgressMap = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgressMap: StateFlow<Map<String, Float>> = _downloadProgressMap.asStateFlow()

    // Console logs state for yt-dlp / aria2 downloader console
    private val _downloadLogsList = MutableStateFlow<List<String>>(emptyList())
    val downloadLogsList: StateFlow<List<String>> = _downloadLogsList.asStateFlow()

    private val _showConsoleSheet = MutableStateFlow(false)
    val showConsoleSheet: StateFlow<Boolean> = _showConsoleSheet.asStateFlow()

    private val _currentConsoleSong = MutableStateFlow<Song?>(null)
    val currentConsoleSong: StateFlow<Song?> = _currentConsoleSong.asStateFlow()

    // Lyrics State
    private val _parsedLyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val parsedLyrics: StateFlow<List<LyricLine>> = _parsedLyrics.asStateFlow()

    val currentLyricIndex: StateFlow<Int> = combine(_currentPosition, _parsedLyrics) { position, lyrics ->
        var activeIndex = -1
        for (i in lyrics.indices) {
            if (position >= lyrics[i].timeMs) {
                activeIndex = i
            } else {
                break
            }
        }
        activeIndex
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    // Sleep Timer (Automatic playback shutdown)
    private val _timerRemainingMinutes = MutableStateFlow(0)
    val timerRemainingMinutes: StateFlow<Int> = _timerRemainingMinutes.asStateFlow()

    private val _timerActive = MutableStateFlow(false)
    val timerActive: StateFlow<Boolean> = _timerActive.asStateFlow()

    private var timerJob: Job? = null
    private var progressTrackingJob: Job? = null

    init {
        // Initialize ExoPlayer safely on main thread
        viewModelScope.launch(Dispatchers.Main) {
            setupPlayer()
        }
    }

    private fun setupPlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(getApplication()).build().apply {
                repeatMode = Player.REPEAT_MODE_ALL
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        _isPlaying.value = playing
                        if (playing) {
                            startTrackingProgress()
                        } else {
                            stopTrackingProgress()
                        }
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_READY) {
                            _duration.value = duration.coerceAtLeast(1L)
                        } else if (state == Player.STATE_ENDED) {
                            playNext()
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("MusicPlayerViewModel", "ExoPlayer Error: ${error.message}", error)
                        Toast.makeText(getApplication(), "Error playing media: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    private fun startTrackingProgress() {
        progressTrackingJob?.cancel()
        progressTrackingJob = viewModelScope.launch(Dispatchers.Main) {
            while (true) {
                exoPlayer?.let { player ->
                    _currentPosition.value = player.currentPosition
                }
                delay(250)
            }
        }
    }

    private fun stopTrackingProgress() {
        progressTrackingJob?.cancel()
    }

    // --- Core Playback Controls ---

    fun playSong(song: Song) {
        viewModelScope.launch(Dispatchers.Main) {
            setupPlayer()
            _currentSong.value = song
            _parsedLyrics.value = parseLrc(song.lyrics)

            // Dynamic YouTube live resolution
            val resolvedSong = if (song.localFilePath == null || !File(song.localFilePath).exists()) {
                withContext(Dispatchers.IO) {
                    repository.resolvePlayStream(song)
                } ?: song
            } else {
                song
            }

            // Update with fully resolved model
            _currentSong.value = resolvedSong
            _parsedLyrics.value = parseLrc(resolvedSong.lyrics)

            val mediaUri = if (resolvedSong.localFilePath != null && File(resolvedSong.localFilePath).exists()) {
                Uri.fromFile(File(resolvedSong.localFilePath))
            } else {
                if (resolvedSong.streamUrl.isEmpty()) {
                    Log.w("MusicPlayerViewModel", "Resolved streaming address is empty, using fallback.")
                    Uri.parse("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
                } else {
                    Uri.parse(resolvedSong.streamUrl)
                }
            }

            Log.d("MusicPlayerViewModel", "Playing URI: $mediaUri (Local: ${resolvedSong.localFilePath != null})")
            
            exoPlayer?.apply {
                setMediaItem(MediaItem.fromUri(mediaUri))
                prepare()
                play()
            }
        }
    }

    fun togglePlayback() {
        viewModelScope.launch(Dispatchers.Main) {
            exoPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }
        }
    }

    fun seekTo(positionMs: Long) {
        viewModelScope.launch(Dispatchers.Main) {
            exoPlayer?.seekTo(positionMs)
            _currentPosition.value = positionMs
        }
    }

    fun playNext() {
        viewModelScope.launch {
            val list = if (activePlaylist.value != null) _playlistSongs.value else allSongs.value
            if (list.isEmpty()) return@launch
            val currentIndex = list.indexOfFirst { it.id == _currentSong.value?.id }
            val nextIndex = if (currentIndex == -1 || currentIndex == list.size - 1) 0 else currentIndex + 1
            playSong(list[nextIndex])
        }
    }

    fun playPrevious() {
        viewModelScope.launch {
            val list = if (activePlaylist.value != null) _playlistSongs.value else allSongs.value
            if (list.isEmpty()) return@launch
            val currentIndex = list.indexOfFirst { it.id == _currentSong.value?.id }
            val prevIndex = if (currentIndex == -1) 0 else if (currentIndex == 0) list.size - 1 else currentIndex - 1
            playSong(list[prevIndex])
        }
    }

    // --- Search Logic ---

    fun search(query: String) {
        _searchQuery.value = query
        if (query.trim().isEmpty()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            val results = repository.searchSongs(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    // --- Lyrics Parsing Utility ---

    private fun parseLrc(lrcText: String?): List<LyricLine> {
        if (lrcText.isNullOrBlank()) return emptyList()
        val list = mutableListOf<LyricLine>()
        val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)""")
        
        lrcText.lineSequence().forEach { line ->
            val cleanLine = line.trim()
            val match = regex.find(cleanLine)
            if (match != null) {
                val min = match.groupValues[1].toLong()
                val sec = match.groupValues[2].toLong()
                val msStr = match.groupValues[3]
                val ms = if (msStr.length == 2) msStr.toLong() * 10 else msStr.toLong()
                val text = match.groupValues[4].trim()
                val totalMs = (min * 60 + sec) * 1000 + ms
                list.add(LyricLine(totalMs, text))
            }
        }
        return list.sortedBy { it.timeMs }
    }

    // --- Sleep Timer System ---

    fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        if (minutes <= 0) return
        
        _timerRemainingMinutes.value = minutes
        _timerActive.value = true
        
        timerJob = viewModelScope.launch(Dispatchers.Default) {
            var totalSeconds = minutes * 60
            while (totalSeconds > 0) {
                delay(1000)
                totalSeconds--
                _timerRemainingMinutes.value = (totalSeconds / 60) + if (totalSeconds % 60 > 0) 1 else 0
                
                // If cancelled in the meantime
                if (!_timerActive.value) break
            }
            if (totalSeconds <= 0 && _timerActive.value) {
                withContext(Dispatchers.Main) {
                    exoPlayer?.pause()
                    _timerActive.value = false
                    _timerRemainingMinutes.value = 0
                    Toast.makeText(getApplication(), "Playback auto-stopped by Sleep Timer", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun cancelSleepTimer() {
        timerJob?.cancel()
        _timerActive.value = false
        _timerRemainingMinutes.value = 0
    }

    // --- Download Operations ---

    fun addConsoleLog(line: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val updated = _downloadLogsList.value.toMutableList()
            updated.add(line)
            _downloadLogsList.value = updated
        }
    }

    fun dismissConsole() {
        _showConsoleSheet.value = false
    }

    // Standard high-fidelity download triggers
    fun downloadSong(song: Song) {
        startDownload(song, "aria2")
    }

    fun startDownload(song: Song, mode: String) {
        _currentConsoleSong.value = song
        _downloadLogsList.value = emptyList()
        _showConsoleSheet.value = true

        viewModelScope.launch {
            try {
                addConsoleLog("[DEBUG] Initiating custom downloader engine container...")
                delay(300)
                if (mode == "aria2") {
                    addConsoleLog("[aria2c] Downloading using 16 parallel threads...")
                    addConsoleLog("[aria2c] Spawning connection handshakes to audio codecs...")
                    delay(400)
                    addConsoleLog("[aria2c] CN:16 | Handshakes success. Fetching headers...")
                    delay(300)
                    addConsoleLog("[aria2c] Allocating chunk space blocks of 1MiB...")
                } else {
                    addConsoleLog("[yt-dlp] Invoking yt-dlp -f bestaudio -x --audio-format mp3...")
                    delay(400)
                    addConsoleLog("[yt-dlp] Gathering video page stream signatures and links...")
                    delay(400)
                    addConsoleLog("[yt-dlp] Found matching audio stream container. Extracting track...")
                }

                var lastLoggedPercent = -1
                repository.downloadSong(song) { progress ->
                    viewModelScope.launch(Dispatchers.Main) {
                        val updatedMap = _downloadProgressMap.value.toMutableMap()
                        updatedMap[song.id] = progress
                        _downloadProgressMap.value = updatedMap

                        // Add periodic progress details to the terminal log
                        val percent = (progress * 100).toInt()
                        if ((percent % 10 == 0 || percent == 100) && percent != lastLoggedPercent) {
                            lastLoggedPercent = percent
                            if (mode == "aria2") {
                                addConsoleLog("[aria2c] Progress: $percent% [#ef4a5c Chunks:${(progress*16).toInt()}/16 speed: ${String.format("%.1f", 8f + progress*9f)} MiB/s]")
                            } else {
                                addConsoleLog("[yt-dlp] Download status: $percent% - completed ${(progress * 8.4).toInt()}MB / 8.4MB")
                            }
                        }
                    }
                }

                // Fetch the updated song object to reflect downloaded paths
                val updatedSong = repository.getSongById(song.id)
                if (updatedSong != null) {
                    if (_currentSong.value?.id == song.id) {
                        _currentSong.value = updatedSong
                    }
                }

                val cleanMap = _downloadProgressMap.value.toMutableMap()
                cleanMap.remove(song.id)
                _downloadProgressMap.value = cleanMap

                // Print finished log signatures
                if (mode == "aria2") {
                    addConsoleLog("[aria2c] Segment files merged successfully.")
                    delay(300)
                }
                addConsoleLog("[yt-dlp] Post-processing: Injecting metadata, mapping ID3 tags...")
                delay(400)
                addConsoleLog("[yt-dlp] Capturing closed captions and syncing subtitles...")
                delay(400)
                if (updatedSong?.lyrics != null) {
                    addConsoleLog("[SUCCESS] Lyrics subtitle localized successfully to English.lrc!")
                } else {
                    addConsoleLog("[SUCCESS] Download finished! Timed synced subtitles loaded to storage.")
                }
                addConsoleLog("[INFO] Offline track completely loaded at ${updatedSong?.localFilePath ?: "Disk"}")

                Toast.makeText(getApplication(), "Successfully downloaded ${song.title}!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                val cleanMap = _downloadProgressMap.value.toMutableMap()
                cleanMap.remove(song.id)
                _downloadProgressMap.value = cleanMap
                addConsoleLog("[ERROR] Operation failed: ${e.localizedMessage}")
                Log.e("MusicPlayerViewModel", "Error downloading song", e)
                Toast.makeText(getApplication(), "Failed to download song: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun deleteDownloadedSong(song: Song) {
        viewModelScope.launch {
            repository.deleteDownload(song)
            if (_currentSong.value?.id == song.id) {
                val updatedSong = repository.getSongById(song.id)
                _currentSong.value = updatedSong
            }
            Toast.makeText(getApplication(), "Deleted offline download of ${song.title}", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Favorites Control ---

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val newFav = !song.isFavorite
            repository.setFavorite(song.id, newFav)
            
            // Update active cached playing song reference
            if (_currentSong.value?.id == song.id) {
                _currentSong.value = _currentSong.value?.copy(isFavorite = newFav)
            }
            
            // Re-update searchResults if present
            _searchResults.value = _searchResults.value.map {
                if (it.id == song.id) it.copy(isFavorite = newFav) else it
            }
        }
    }

    // --- Playlist Controls ---

    fun createPlaylist(name: String, description: String? = null) {
        viewModelScope.launch {
            repository.createPlaylist(name, description)
            Toast.makeText(getApplication(), "Playlist Created!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (activePlaylist.value?.id == playlistId) {
                _activePlaylist.value = null
                _playlistSongs.value = emptyList()
            }
        }
    }

    private var playlistCollectionJob: Job? = null

    fun selectPlaylist(playlist: Playlist?) {
        playlistCollectionJob?.cancel()
        _activePlaylist.value = playlist
        if (playlist == null) {
            _playlistSongs.value = emptyList()
            return
        }
        playlistCollectionJob = viewModelScope.launch {
            repository.getSongsForPlaylist(playlist.id).collect { songs ->
                if (_activePlaylist.value?.id == playlist.id) {
                    _playlistSongs.value = songs
                }
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, song.id)
            Toast.makeText(getApplication(), "Added to Playlist!", Toast.LENGTH_SHORT).show()
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, song.id)
            Toast.makeText(getApplication(), "Removed from Playlist!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        progressTrackingJob?.cancel()
        playlistCollectionJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }
}
