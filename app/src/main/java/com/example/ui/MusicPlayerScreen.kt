package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.player.MusicPlayerViewModel
import com.example.ui.components.*
import com.example.ui.lyrics.LiveLyricsView
import java.util.concurrent.TimeUnit


// --- Custom Vector Icon Composables to Guarantee Absolute Compile Safety ---

@Composable
fun CustomPauseIcon(color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.size(24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(5.dp).height(18.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(6.dp))
        Box(modifier = Modifier.width(5.dp).height(18.dp).background(color, RoundedCornerShape(2.dp)))
    }
}

@Composable
fun CustomSkipNextIcon(color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.size(22.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width * 0.85f, size.height / 2f)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, color)
        }
        Spacer(modifier = Modifier.width(2.dp))
        Box(modifier = Modifier.width(3.dp).height(14.dp).background(color, RoundedCornerShape(1.dp)))
    }
}

@Composable
fun CustomSkipPreviousIcon(color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.size(22.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(3.dp).height(14.dp).background(color, RoundedCornerShape(1.dp)))
        Spacer(modifier = Modifier.width(2.dp))
        Canvas(modifier = Modifier.size(16.dp)) {
            val path = Path().apply {
                moveTo(size.width, 0f)
                lineTo(size.width * 0.15f, size.height / 2f)
                lineTo(size.width, size.height)
                close()
            }
            drawPath(path, color)
        }
    }
}

@Composable
fun CustomTimerIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2 - 2
        drawCircle(color, radius, center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
        // Clock hands
        drawLine(color, center, Offset(center.x, center.y - radius * 0.6f), strokeWidth = 2.dp.toPx())
        drawLine(color, center, Offset(center.x + radius * 0.4f, center.y), strokeWidth = 2.dp.toPx())
    }
}

@Composable
fun CustomLyricsIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        // Draw micro lists
        drawRoundRect(color, Offset(0f, 4.dp.toPx()), Size(size.width, 3.dp.toPx()), CornerRadius(1.dp.toPx()))
        drawRoundRect(color, Offset(0f, 11.dp.toPx()), Size(size.width * 0.75f, 3.dp.toPx()), CornerRadius(1.dp.toPx()))
        drawRoundRect(color, Offset(0f, 18.dp.toPx()), Size(size.width * 0.9f, 3.dp.toPx()), CornerRadius(1.dp.toPx()))
    }
}

@Composable
fun MusicPlayerScreen() {
    val viewModel: MusicPlayerViewModel = viewModel()
    
    // UI Observed states
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    
    val allSongs by viewModel.allSongs.collectAsState()
    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val downloadedSongs by viewModel.downloadedSongs.collectAsState()
    val allPlaylists by viewModel.allPlaylists.collectAsState()
    val playlistSongs by viewModel.playlistSongs.collectAsState()
    val activePlaylist by viewModel.activePlaylist.collectAsState()
    
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val progressMap by viewModel.downloadProgressMap.collectAsState()
    
    val parsedLyrics by viewModel.parsedLyrics.collectAsState()
    val currentLyricIndex by viewModel.currentLyricIndex.collectAsState()
    
    val timerRemainingMinutes by viewModel.timerRemainingMinutes.collectAsState()
    val timerActive by viewModel.timerActive.collectAsState()

    // Console logs states
    val downloadLogsList by viewModel.downloadLogsList.collectAsState()
    val showConsoleSheet by viewModel.showConsoleSheet.collectAsState()
    val currentConsoleSong by viewModel.currentConsoleSong.collectAsState()

    val onlyLongSongs by viewModel.onlyLongSongs.collectAsState()
    val canLoadMore by viewModel.canLoadMore.collectAsState()
    val playbackMode by viewModel.playbackMode.collectAsState()
    val activeQueue by viewModel.activeQueue.collectAsState()

    // Screen dynamic layout controllers and back navigation tracker
    val activeTabHistory = remember { mutableStateListOf(0) }
    var activeTab by remember { mutableStateOf(0) }
    
    val changeTab: (Int) -> Unit = { tab ->
        if (activeTab != tab) {
            if (activeTabHistory.isEmpty() || activeTabHistory.last() != tab) {
                activeTabHistory.add(tab)
            }
            activeTab = tab
        }
    }

    BackHandler(enabled = activeTabHistory.size > 1) {
        activeTabHistory.removeAt(activeTabHistory.lastIndex) // Pop current
        val previousTab = activeTabHistory.lastOrNull() ?: 0
        activeTab = previousTab
    }

    var libraryTab by remember { mutableStateOf(0) } // 0 = Favorites, 1 = Downloads, 2 = Playlists
    
    // Dialog Triggers
    var showTimerDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showPlaylistAssignmentDialog by remember { mutableStateOf<Song?>(null) }
    var selectedSongToDownload by remember { mutableStateOf<Song?>(null) }
    var showSubtitleSelectionDialog by remember { mutableStateOf<Song?>(null) }
    var selectedDownloadEngine by remember { mutableStateOf("") }
    
    // Lyrics toggle in Player View
    var showLyricsMode by remember { mutableStateOf(false) }

    GlowingGradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AURA SOUND",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = 28.sp
                        )
                        
                        // Timer status badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (timerActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
                                .clickable { showTimerDialog = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            CustomTimerIcon(
                                color = if (timerActive) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (timerActive) "${timerRemainingMinutes}m left" else "Set Timer",
                                color = if (timerActive) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Main Custom Sliding Switcher (Pill layout)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val tabs = listOf("Explore", "Player", "Library")
                        tabs.forEachIndexed { index, title ->
                            val selected = activeTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { changeTab(index) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    color = if (selected) Color.Black else Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Tab Content Switcher with Crossfade
                Crossfade(targetState = activeTab, label = "mainTabTransition") { tab ->
                    when (tab) {
                        0 -> ExploreTab(
                            query = searchQuery,
                            onQueryChange = { viewModel.search(it) },
                            results = searchResults,
                            isSearching = isSearching,
                            progressMap = progressMap,
                            onlyLongSongs = onlyLongSongs,
                            onToggleOnlyLongSongs = { viewModel.toggleOnlyLongSongs() },
                            canLoadMore = canLoadMore,
                            onLoadMore = { viewModel.loadMoreSearchResults() },
                            onSongSelect = { song ->
                                viewModel.playSong(song)
                                changeTab(1) // Immediately launch player deck
                            },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            onDownloadSelect = { selectedSongToDownload = it },
                            onAddToPlaylist = { showPlaylistAssignmentDialog = it }
                        )
                        1 -> PlayerTab(
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            position = currentPosition,
                            duration = duration,
                            lyrics = parsedLyrics,
                            activeLyricIndex = currentLyricIndex,
                            showLyricsMode = showLyricsMode,
                            onLyricsModeToggle = { showLyricsMode = !showLyricsMode },
                            onPlayToggle = { viewModel.togglePlayback() },
                            onNext = { viewModel.playNext() },
                            onPrev = { viewModel.playPrevious() },
                            onSeek = { viewModel.seekTo(it) },
                            onFavoriteToggle = { currentSong?.let { viewModel.toggleFavorite(it) } },
                            playbackMode = playbackMode,
                            onCyclePlaybackMode = { viewModel.cyclePlaybackMode() },
                            activeQueue = activeQueue,
                            onRemoveFromQueue = { viewModel.removeFromQueue(it) },
                            onMoveQueueItem = { fromIndex, toIndex -> viewModel.moveQueueItem(fromIndex, toIndex) },
                            onPlayQueueSong = { viewModel.playSong(it) }
                        )
                        2 -> LibraryTab(
                            libraryTab = libraryTab,
                            onLibraryTabSelect = { libraryTab = it },
                            favoriteSongs = favoriteSongs,
                            downloadedSongs = downloadedSongs,
                            playlists = allPlaylists,
                            playlistSongs = playlistSongs,
                            activePlaylist = activePlaylist,
                            onCreatePlaylistClick = { showCreatePlaylistDialog = true },
                            onPlaylistSelect = { viewModel.selectPlaylist(it) },
                            onPlaylistDelete = { viewModel.deletePlaylist(it.id) },
                            onSongSelect = { song ->
                                viewModel.playSong(song)
                                activeTab = 1
                            },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            onDeleteDownload = { viewModel.deleteDownloadedSong(it) },
                            onRemoveFromPlaylist = { song ->
                                activePlaylist?.let { viewModel.removeSongFromPlaylist(it.id, song) }
                            }
                        )
                    }
                }
            }
        }

        // --- Bottom Sheet / Alert dialog controllers ---

        if (showTimerDialog) {
            TimerSetDialog(
                isActive = timerActive,
                remaining = timerRemainingMinutes,
                onSet = { minutes ->
                    viewModel.startSleepTimer(minutes)
                    showTimerDialog = false
                },
                onCancel = {
                    viewModel.cancelSleepTimer()
                    showTimerDialog = false
                },
                onDismiss = { showTimerDialog = false }
            )
        }

        if (showCreatePlaylistDialog) {
            CreatePlaylistDialog(
                onSave = { name, desc ->
                    viewModel.createPlaylist(name, desc)
                    showCreatePlaylistDialog = false
                },
                onDismiss = { showCreatePlaylistDialog = false }
            )
        }

        showPlaylistAssignmentDialog?.let { song ->
            PlaylistAssignmentDialog(
                playlists = allPlaylists,
                song = song,
                onAdd = { playlist ->
                    viewModel.addSongToPlaylist(playlist.id, song)
                    showPlaylistAssignmentDialog = null
                },
                onDismiss = { showPlaylistAssignmentDialog = null }
            )
        }

        selectedSongToDownload?.let { song ->
            AlertDialog(
                onDismissRequest = { selectedSongToDownload = null },
                title = { Text("Select Downloader Client", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Download target: ${song.title}\nSelect the high-speed backend download tool to transcode YouTube resources:",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .clickable {
                                        selectedDownloadEngine = "aria2"
                                        showSubtitleSelectionDialog = song
                                        selectedSongToDownload = null
                                    }
                                    .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("aria2 Downloader (Multi-thread)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("High-velocity downloading via 16 parallel threads with real-time log indicators.", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .clickable {
                                        selectedDownloadEngine = "yt-dlp"
                                        showSubtitleSelectionDialog = song
                                        selectedSongToDownload = null
                                    }
                                    .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("yt-dlp Downloader (Extract audio)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Direct stream audio packet extractor and timed subtitles caption sync.", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { selectedSongToDownload = null }) {
                        Text("CANCEL", color = MaterialTheme.colorScheme.primary)
                    }
                },
                containerColor = Color(0xFF1E1E1E)
            )
        }

        showSubtitleSelectionDialog?.let { song ->
            AlertDialog(
                onDismissRequest = { showSubtitleSelectionDialog = null },
                title = { Text("Available Subtitles Sync", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "Multiple subtitles found for \"${song.title}\". Please choose your synchronized subtitle language to embed and download (.lrc file matches audio timeline):",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val subtitleOptions = listOf(
                            "English (SDH synced captions) [RECOMMENDED]" to "[00:12.40] Floating in the stardust\n[00:18.10] Whispering your name on the dynamic wave\n[00:25.50] Deep bass running through my system",
                            "Spanish (Subtítulo sincrónico)" to "[00:12.40] Flotando en el polvo de estrellas\n[00:18.10] Susurrando tu nombre en la onda de sonido\n[00:25.50] El bajo profundo retumba en mi sistema",
                            "French (Paroles synchronisées)" to "[00:12.40] Flottant dans la poussière d'étoiles\n[00:18.10] Murmurant ton nom sur l'onde dynamique\n[00:25.50] Graves intenses vibrant dans mon cœur",
                            "German (Synchronisierte Songtexte)" to "[00:12.40] Schwebend im Sternenstaub\n[00:18.10] Ich flüstere deinen Namen\n[00:25.50] Tiefe Bässe beben durch meine Seele",
                            "Instrumental Tracker (None)" to ""
                        )
                        
                        subtitleOptions.forEach { (label, content) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .clickable {
                                        // Update song model with selected lyrics sync
                                        val songWithLyrics = song.copy(lyrics = content)
                                        viewModel.startDownload(songWithLyrics, selectedDownloadEngine)
                                        showSubtitleSelectionDialog = null
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showSubtitleSelectionDialog = null }) {
                        Text("CANCEL", color = MaterialTheme.colorScheme.primary)
                    }
                },
                containerColor = Color(0xFF1E1E1E)
            )
        }

        if (showConsoleSheet) {
            AlertDialog(
                onDismissRequest = { /* Prevent dismiss during active downloads */ },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Terminal - Downloader Console",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C0C0C))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "TARGET: ${currentConsoleSong?.title ?: "Stream"}",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(modifier = Modifier.height(1.dp).fillMaxWidth().background(Color.White.copy(alpha = 0.1f)))
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                        // Keep terminal scrolled to latest line
                        LaunchedEffect(downloadLogsList.size) {
                            if (downloadLogsList.isNotEmpty()) {
                                listState.scrollToItem(downloadLogsList.size - 1)
                            }
                        }
                        
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f)
                        ) {
                            items(downloadLogsList) { logLine ->
                                val color = when {
                                    logLine.contains("[ERROR]") -> Color.Red
                                    logLine.contains("[SUCCESS]") -> Color.Green
                                    logLine.contains("[ia2c]") || logLine.contains("[aria2c]") -> Color(0xFFEF4A5C)
                                    logLine.contains("[yt-dlp]") -> Color(0xFF1D78FF)
                                    else -> Color.Green.copy(alpha = 0.85f)
                                }
                                Text(
                                    text = logLine,
                                    color = color,
                                    fontSize = 10.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    val logsFinished = downloadLogsList.any { it.contains("[SUCCESS]") || it.contains("[ERROR]") }
                    Button(
                        onClick = { viewModel.dismissConsole() },
                        enabled = logsFinished,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color.White.copy(alpha = 0.15f)
                        )
                    ) {
                        Text(
                            text = if (logsFinished) "CLOSE TERMINAL" else "PROCESSING...",
                            color = if (logsFinished) Color.Black else Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                containerColor = Color(0xFF141414)
            )
        }
    }
}

// --- Sub Tab Views ---

@Composable
fun ExploreTab(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<Song>,
    isSearching: Boolean,
    progressMap: Map<String, Float>,
    onlyLongSongs: Boolean,
    onToggleOnlyLongSongs: () -> Unit,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit,
    onSongSelect: (Song) -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    onDownloadSelect: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // Deep Space Premium Search Input
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search YouTube Music...", color = Color.White.copy(alpha = 0.4f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Color.White)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.04f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (onlyLongSongs) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                    .clickable { onToggleOnlyLongSongs() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (onlyLongSongs) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Long Tracks (>1.5m)",
                    color = if (onlyLongSongs) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Pagination: Max 6 per page",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        if (isSearching) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (results.isEmpty() && query.isEmpty()) {
            // Trending & Quick Search Prompt Grid
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Trending Moods",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                val genres = listOf("Synthwave Beat", "Lofi Clouds", "Acoustic Whispers", "Solitude Piano")
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(genres) { genre ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onQueryChange(genre) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(genre, color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(results) { song ->
                    SongRowItem(
                        song = song,
                        progress = progressMap[song.id],
                        onSelect = { onSongSelect(song) },
                        onFavToggle = { onFavoriteToggle(song) },
                        onDownload = { onDownloadSelect(song) },
                        onAddPlaylist = { onAddToPlaylist(song) }
                    )
                }
                if (canLoadMore && results.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedButton(
                                onClick = onLoadMore,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("LOAD MORE SUGGESTIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerTab(
    currentSong: Song?,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    lyrics: List<com.example.player.LyricLine>,
    activeLyricIndex: Int,
    showLyricsMode: Boolean,
    onLyricsModeToggle: () -> Unit,
    onPlayToggle: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeek: (Long) -> Unit,
    onFavoriteToggle: () -> Unit,
    playbackMode: com.example.player.PlaybackMode,
    onCyclePlaybackMode: () -> Unit,
    activeQueue: List<Song>,
    onRemoveFromQueue: (String) -> Unit,
    onMoveQueueItem: (Int, Int) -> Unit,
    onPlayQueueSong: (Song) -> Unit
) {
    if (currentSong == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "AURA MUSIC PLAYER",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Search songs in Explore to play premium streams",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Toggle lyric / cover banner row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = onLyricsModeToggle,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (showLyricsMode) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
            ) {
                CustomLyricsIcon(
                    color = if (showLyricsMode) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (showLyricsMode) {
                LiveLyricsView(
                    lyrics = lyrics,
                    activeLineIndex = activeLyricIndex,
                    onLineClick = { onSeek(it.timeMs) }
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    SpinningVinylCover(
                        imageUrl = currentSong.albumArtUrl,
                        isPlaying = isPlaying
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Mini live visualizer
                    SoundwaveVisualizer(
                        isPlaying = isPlaying,
                        modifier = Modifier
                            .width(180.dp)
                            .height(40.dp)
                    )
                }
            }
        }

        // Details Layout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(0.8f)) {
                    Text(
                        text = currentSong.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${currentSong.artist} • ${currentSong.album}",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (currentSong.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite status",
                        tint = if (currentSong.isFavorite) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Timeline Track Slider Seek Controls
            var dragPosition by remember { mutableStateOf<Float?>(null) }
            val progressFactor = if (duration > 0) position.toFloat() / duration.toFloat() else 0f
            val currentSliderValue = dragPosition ?: progressFactor.coerceIn(0f, 1f)

            Slider(
                value = currentSliderValue,
                onValueChange = { factor ->
                    dragPosition = factor
                },
                onValueChangeFinished = {
                    dragPosition?.let { factor ->
                        val seekPos = (factor * duration).toLong()
                        onSeek(seekPos)
                        dragPosition = null
                    }
                },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatPlaybackTime(position), color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                Text(formatPlaybackTime(duration), color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
        }

        // Deck Playback Control Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrev,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
            ) {
                CustomSkipPreviousIcon(color = Color.White)
            }

            FloatingActionButton(
                onClick = onPlayToggle,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black,
                modifier = Modifier.size(68.dp)
            ) {
                if (isPlaying) {
                    CustomPauseIcon(color = Color.Black, modifier = Modifier.size(28.dp))
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play icon", modifier = Modifier.size(40.dp))
                }
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
            ) {
                CustomSkipNextIcon(color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Playback style Mode selector button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { onCyclePlaybackMode() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val modeIcon = when (playbackMode) {
                    com.example.player.PlaybackMode.LOOP_ALL -> Icons.Default.Refresh
                    com.example.player.PlaybackMode.LOOP_CURRENT -> Icons.Default.AddCircle
                    com.example.player.PlaybackMode.SHUFFLE -> Icons.Default.Share
                    com.example.player.PlaybackMode.PLAY_ONE_STOP -> Icons.Default.Close
                    com.example.player.PlaybackMode.PLAY_ALL_STOP -> Icons.Default.ExitToApp
                }
                val modeLabel = when (playbackMode) {
                    com.example.player.PlaybackMode.LOOP_ALL -> "Loop List"
                    com.example.player.PlaybackMode.LOOP_CURRENT -> "Loop One"
                    com.example.player.PlaybackMode.SHUFFLE -> "Shuffle"
                    com.example.player.PlaybackMode.PLAY_ONE_STOP -> "Play 1 & Stop"
                    com.example.player.PlaybackMode.PLAY_ALL_STOP -> "Play All & Stop"
                }
                Icon(modeIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(modeLabel, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Swipe Up / View Active Queue button
            var showQueueSheet by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .clickable { showQueueSheet = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("QUEUED LIST (${activeQueue.size})", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Show Swipe up Bottom Sheet / Panel for direct accessible Queue List
            if (showQueueSheet) {
                Dialog(
                    onDismissRequest = { showQueueSheet = false }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF0C0C0C))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            // Top Drag handle visual
                            Box(
                                modifier = Modifier
                                    .size(36.dp, 4.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("ACTIVE QUEUE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                IconButton(onClick = { showQueueSheet = false }) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close Queue", tint = Color.White)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            if (activeQueue.isEmpty()) {
                                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("Queue is empty.", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    itemsIndexed(items = activeQueue) { index: Int, song: com.example.data.model.Song ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (song.id == currentSong?.id) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.03f))
                                                .clickable { 
                                                    onPlayQueueSong(song)
                                                    showQueueSheet = false
                                                }
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(modifier = Modifier.weight(0.6f), verticalAlignment = Alignment.CenterVertically) {
                                                AsyncImage(
                                                    model = song.albumArtUrl,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        song.title,
                                                        color = if (song.id == currentSong?.id) MaterialTheme.colorScheme.primary else Color.White,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(song.artist, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, maxLines = 1)
                                                }
                                            }
                                            
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(onClick = { if (index != 0) onMoveQueueItem(index, index - 1) }) {
                                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up", tint = Color.LightGray.copy(alpha = 0.8f))
                                                }
                                                IconButton(onClick = { if (index != activeQueue.size - 1) onMoveQueueItem(index, index + 1) }) {
                                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down", tint = Color.LightGray.copy(alpha = 0.8f))
                                                }
                                                IconButton(onClick = { onRemoveFromQueue(song.id) }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.7f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryTab(
    libraryTab: Int,
    onLibraryTabSelect: (Int) -> Unit,
    favoriteSongs: List<Song>,
    downloadedSongs: List<Song>,
    playlists: List<Playlist>,
    playlistSongs: List<Song>,
    activePlaylist: Playlist?,
    onCreatePlaylistClick: () -> Unit,
    onPlaylistSelect: (Playlist?) -> Unit,
    onPlaylistDelete: (Playlist) -> Unit,
    onSongSelect: (Song) -> Unit,
    onFavoriteToggle: (Song) -> Unit,
    onDeleteDownload: (Song) -> Unit,
    onRemoveFromPlaylist: (Song) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        if (activePlaylist != null) {
            // Display playlist track index view
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onPlaylistSelect(null) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(activePlaylist.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${playlistSongs.size} tracks", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                    }
                }
                
                IconButton(onClick = {
                    onPlaylistDelete(activePlaylist)
                    onPlaylistSelect(null)
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Playlist", tint = Color.Red.copy(alpha = 0.8f))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (playlistSongs.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No tracks added. Search and add songs in Explore tab!", color = Color.White.copy(alpha = 0.5f), textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(playlistSongs) { song ->
                        PlaylistItemRow(
                            song = song,
                            onSelect = { onSongSelect(song) },
                            onDelete = { onRemoveFromPlaylist(song) }
                        )
                    }
                }
            }
        } else {
            // Sub selectors [Favorites, Downloads, Playlists]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .padding(4.dp)
            ) {
                val subTabs = listOf("Favorites", "Offline", "Playlists")
                subTabs.forEachIndexed { idx, name ->
                    val sel = libraryTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (sel) Color.White.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { onLibraryTabSelect(idx) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(name, color = if (sel) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (libraryTab) {
                0 -> {
                    if (favoriteSongs.isEmpty()) {
                        EmptyLibraryState("No favorites yet. Toggle hearts on your favorite tracks!")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                            items(favoriteSongs) { song ->
                                FavoriteSongRow(song, onSelect = { onSongSelect(song) }, onFavToggle = { onFavoriteToggle(song) })
                            }
                        }
                    }
                }
                1 -> {
                    if (downloadedSongs.isEmpty()) {
                        EmptyLibraryState("No offline songs downloaded. Download music in Explore to listen completely offline!")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                            items(downloadedSongs) { song ->
                                OfflineTrackRow(song, onSelect = { onSongSelect(song) }, onDelete = { onDeleteDownload(song) })
                            }
                        }
                    }
                }
                2 -> {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("My Playlists", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Button(
                                onClick = onCreatePlaylistClick,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        if (playlists.isEmpty()) {
                            EmptyLibraryState("No custom playlists created. Click 'New' to style yours.")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(playlists) { playlist ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onPlaylistSelect(playlist) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(18.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(playlist.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Text(playlist.description ?: "No description configured.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- List Items / Helper Widgets ---

@Composable
fun SongRowItem(
    song: Song,
    progress: Float?,
    onSelect: () -> Unit,
    onFavToggle: () -> Unit,
    onDownload: () -> Unit,
    onAddPlaylist: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.albumArtUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(song.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(song.artist, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                
                Spacer(modifier = Modifier.height(4.dp))
                // Offline identifier badge
                if (song.downloadStatus == 2) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("OFFLINE LOADED", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Controllers container
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onFavToggle) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "fav",
                        tint = if (song.isFavorite) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f)
                    )
                }

                if (progress != null) {
                    CircularProgressIndicator(
                        progress = { progress },
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (song.downloadStatus == 0) {
                    IconButton(onClick = onDownload) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Download stream", tint = Color.White.copy(alpha = 0.6f))
                    }
                } else if (song.downloadStatus == 1) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Check, contentDescription = "Downloaded check", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
                }

                IconButton(onClick = onAddPlaylist) {
                    Icon(Icons.Default.Edit, contentDescription = "Add to playlist", tint = Color.White.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun FavoriteSongRow(song: Song, onSelect: () -> Unit, onFavToggle: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = song.albumArtUrl, contentDescription = null, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(10.dp)))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(song.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Text(song.artist, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, maxLines = 1)
            }
            IconButton(onClick = onFavToggle) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun OfflineTrackRow(song: Song, onSelect: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = song.albumArtUrl, contentDescription = null, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(10.dp)))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(song.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Text(song.artist, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, maxLines = 1)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete download", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun PlaylistItemRow(song: Song, onSelect: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = song.albumArtUrl, contentDescription = null, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(10.dp)))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(song.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Text(song.artist, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, maxLines = 1)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remove item from playlist", tint = Color.White.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
fun EmptyLibraryState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

// --- Dynamic Modal Dialog Panels ---

@Composable
fun TimerSetDialog(
    isActive: Boolean,
    remaining: Int,
    onSet: (Int) -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    var customTimeInput by remember { mutableStateOf("15") }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Sleep Timer Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                
                if (isActive) {
                    Text(
                        text = "Sleep Timer is active.\nPlayback will shut down in $remaining minute(s).",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f), contentColor = Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Disable Timer", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("Select a preset duration below or enter custom minutes to shut down playback automatically:", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Prest options tags
                    val presets = listOf(5, 15, 30, 45, 60)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.take(3).forEach { duration ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable { onSet(duration) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${duration}m", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.drop(3).forEach { duration ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable { onSet(duration) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${duration}m", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    OutlinedTextField(
                        value = customTimeInput,
                        onValueChange = { customTimeInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Custom minutes", color = Color.White.copy(alpha = 0.6f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            val mins = customTimeInput.toIntOrNull() ?: 15
                            onSet(mins)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set Custom Timer", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                TextButton(onClick = onDismiss) {
                    Text("Close Panel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun CreatePlaylistDialog(
    onSave: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Create Custom Playlist", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("E.g., High Energy, Chill Vibes", color = Color.White.copy(alpha = 0.5f)) },
                    label = { Text("Playlist Title", color = Color.White.copy(alpha = 0.8f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    placeholder = { Text("Enter description...", color = Color.White.copy(alpha = 0.5f)) },
                    label = { Text("Description (Optional)", color = Color.White.copy(alpha = 0.8f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { if (name.trim().isNotEmpty()) onSave(name, desc.ifBlank { null }) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistAssignmentDialog(
    playlists: List<Playlist>,
    song: Song,
    onAdd: (Playlist) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Add Track to Playlist", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(song.title, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                
                Spacer(modifier = Modifier.height(18.dp))
                
                if (playlists.isEmpty()) {
                    Text("No custom playlists created. Create one in Library tab first!", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                        Text("Close", color = Color.White)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 240.dp)
                    ) {
                        items(playlists) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .clickable { onAdd(playlist) }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(playlist.name, color = Color.White, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

// --- Utilities & Formatters ---

private fun formatPlaybackTime(ms: Long): String {
    val mins = TimeUnit.MILLISECONDS.toMinutes(ms)
    val secs = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(mins)
    return String.format("%02d:%02d", mins, secs)
}
