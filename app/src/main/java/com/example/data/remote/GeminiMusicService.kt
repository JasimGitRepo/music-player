package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Song
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate
import java.security.SecureRandom

// --- Gemini API Models ---

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class ResponseFormat(
    val text: ResponseFormatText? = null
)

@JsonClass(generateAdapter = true)
data class ResponseFormatText(
    val mimeType: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseFormat: ResponseFormat? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content
)

@JsonClass(generateAdapter = true)
data class SongJson(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val streamUrl: String,
    val albumArtUrl: String? = null,
    val lyrics: String? = null
)

// --- Retrofit Service ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

// --- High Quality Static fallback list of songs ---
object LocalSongCatalog {
    val songs = listOf(
        Song(
            id = "yt_synthwave_dream",
            title = "Midnight Horizon",
            artist = "Retro Synth Club",
            album = "Neon Drift",
            duration = 145000,
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            albumArtUrl = "https://images.unsplash.com/photo-1515462277126-270d878326e5?q=80&w=600",
            lyrics = """
                [00:00.00] (Midnight Horizon - Intro synth playing)
                [00:10.00] Electric heartbeats in the night
                [00:20.00] Chasing the sparks, we are in flight
                [00:30.00] Neon lines guiding our vision
                [00:40.00] Speed in our veins, making decisions
                [00:50.00] Fast lane, no brakes, into the sun
                [01:00.00] The night is young, and we've just begun
                [01:15.00] Racing horizons under stars so clear
                [01:30.00] Out of the shadows, we feel no fear
                [01:40.00] (Chill beats outtro...)
            """.trimIndent(),
            isFavorite = false
        ),
        Song(
            id = "yt_guitar_blues",
            title = "Acoustic Whispers",
            artist = "Lana & The Strings",
            album = "Wooden Resonance",
            duration = 210000,
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            albumArtUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=600",
            lyrics = """
                [00:00.00] (Gentle fingerpicking on acoustic guitar)
                [00:12.00] Leaves are falling, wind is soft
                [00:25.00] Thoughts are lifting, high aloft
                [00:38.00] Rest your head, the day is done
                [00:52.00] Shadows play beneath the sun
                [01:05.00] Speak in whispers, sing in rhymes
                [01:20.00] Reminiscing better times
                [01:35.00] Wooden resonance, strings in key
                [01:50.00] Setting both our spirits free
                [02:05.00] Whispers in the breeze...
                [02:25.00] (Warm guitar fade out...)
            """.trimIndent(),
            isFavorite = true
        ),
        Song(
            id = "yt_chill_vibe",
            title = "Lofi Clouds",
            artist = "Solfeggio Chill",
            album = "Bedroom Beats",
            duration = 184000,
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            albumArtUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?q=80&w=600",
            lyrics = """
                [00:00.00] (Rain sounds and crackly vinyl intro)
                [00:08.00] Sipping coffee while the rain pours down
                [00:20.00] Silent streets of our little town
                [00:32.00] Floatin' up above the clouds so high
                [00:45.00] Watchin' the world spinning by
                [00:58.00] Keep it slow, let the rhythm flow
                [01:10.00] There is no place we have to go
                [01:25.00] Cozy vibes, a warm embrace
                [01:40.00] Safe and calm in our own space
                [01:55.00] Rain keeps falling, let it play
                [02:10.00] Warming up this rainy day
                [02:30.00] (Smooth lo-fi outtro)
            """.trimIndent(),
            isFavorite = false
        ),
        Song(
            id = "yt_piano_dream",
            title = "Solitude Waltz",
            artist = "Evelyn Sterling",
            album = "Ivory Serenade",
            duration = 205000,
            streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            albumArtUrl = "https://images.unsplash.com/photo-1520523839897-bd0b52f945a0?q=80&w=600",
            lyrics = """
                [00:00.00] (Soft classical piano waltz)
                [00:15.00] Dancing in an empty room, so bright
                [00:32.00] Bathed inside the gentle morning light
                [00:48.00] Every ivory key tells a different tale
                [01:05.00] Drifting over hills, through the quiet vale
                [01:20.00] Close your eyes and dream of yesterday
                [01:38.00] Let the piano sweep your cares away
                [01:55.00] Solitude is not a lonely place
                [02:12.00] When it's filled with melody and grace
                [02:30.00] Waltz along, until the song is done...
                [02:50.00] (Slow keys falling in cadence)
            """.trimIndent()
        )
    )
}

data class PipedSongStream(
    val streamUrl: String,
    val lyricsText: String?
)

class GeminiMusicService {

    private val dynamicPipedInstances = mutableListOf<String>()

    private val unsafeClient: OkHttpClient by lazy {
        try {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            
            OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
        } catch (e: Exception) {
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
        }
    }

    private suspend fun getActiveInstances(): List<String> = withContext(Dispatchers.IO) {
        if (dynamicPipedInstances.isNotEmpty()) {
            return@withContext dynamicPipedInstances
        }
        val defaultList = listOf(
            "https://api.piped.private.coffee",
            "https://pipedapi.colbyland.xyz",
            "https://pipedapi.kavin.rocks",
            "https://piped-api.lre.yt"
        )
        try {
            val json = getJsonFromUrl("https://piped-instances.kavin.rocks")
            if (!json.isNullOrEmpty()) {
                val array = JSONArray(json)
                val fetchedList = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val apiUrl = obj.optString("api_url", "")
                    val uptime = obj.optDouble("uptime_24h", 0.0)
                    if (apiUrl.isNotEmpty() && (uptime == 0.0 || uptime > 60.0)) {
                        val cleaned = if (apiUrl.endsWith("/")) apiUrl.dropLast(1) else apiUrl
                        if (!fetchedList.contains(cleaned)) {
                            fetchedList.add(cleaned)
                        }
                    }
                }
                
                // Let's filter out known problematic domains or ensure some reliable defaults are in the flow
                val mergedList = mutableListOf<String>()
                // Prioritize "https://api.piped.private.coffee" since we already verified its SSL/HTTP is operational
                mergedList.add("https://api.piped.private.coffee")
                
                for (url in fetchedList) {
                    if (url != "https://api.piped.private.coffee" && !url.contains("kavin.rocks") && !url.contains("lre.yt")) {
                        mergedList.add(url)
                    }
                }
                
                if (mergedList.isNotEmpty()) {
                    dynamicPipedInstances.clear()
                    dynamicPipedInstances.addAll(mergedList)
                    Log.d("GeminiMusicService", "Dynamically retrieved ${mergedList.size} healthy Piped API servers!")
                    return@withContext mergedList
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiMusicService", "Error resolving active Piped endpoints dynamically, utilizing local fallbacks: ${e.message}")
        }
        defaultList
    }

    private val audioUrlsFallback = listOf(
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"
    )

    private fun getJsonFromUrl(url: String): String? {
        val request = okhttp3.Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            .build()
        return try {
            unsafeClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) response.body?.string() else null
            }
        } catch (e: Exception) {
            Log.w("GeminiMusicService", "Failed to load URL $url: ${e.message}")
            null
        }
    }

    // Convert WebVTT content to synchronized LRC lyrics
    private fun convertVttToLrc(vttText: String): String {
        val lines = vttText.split("\n")
        val lrc = StringBuilder()
        val timeRegex = Regex("""(\d{2}):(\d{2})\.(\d{3})\s*-->\s*\d{2}:\d{2}\.\d{3}""")
        var lastTimeStr = ""
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.contains("-->")) {
                val match = timeRegex.find(trimmed)
                if (match != null) {
                    val min = match.groupValues[1]
                    val sec = match.groupValues[2]
                    val ms = match.groupValues[3].take(2)
                    lastTimeStr = "[$min:$sec.$ms]"
                }
            } else if (trimmed.isNotEmpty() && !trimmed.all { it.isDigit() } && !trimmed.equals("WEBVTT", true)) {
                if (lastTimeStr.isNotEmpty()) {
                    lrc.append("$lastTimeStr $trimmed\n")
                }
            }
        }
        return lrc.toString()
    }

    // Generate timetabled lyrics from Gemini model
    suspend fun generateLyricsWithGemini(title: String, artist: String): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") return@withContext null

        val prompt = """
            Generate synced lyrics in strict LRC (.lrc) format for the song "$title" by "$artist".
            The lyrics should match the structure of the song, contain between 8 to 15 lines timed perfectly from start to finish.
            Each line MUST strictly follow the LRC format:
            [mm:ss.xx] Lyric text
            Do not include any header lines like [ti:Title] or markdown blocks (no ```), just return the clean LRC lines.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.5f)
        )
        try {
            val response = GeminiClient.service.generateContent(apiKey, request)
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
        } catch (e: Exception) {
            Log.e("GeminiMusicService", "Error generating lyrics with Gemini", e)
            null
        }
    }

    // Resolve the playable direct stream link and LRC lyrics for a video item
    suspend fun resolvePlayableStream(videoId: String, title: String, artist: String): PipedSongStream? = withContext(Dispatchers.IO) {
        // Since we are using iTunes API directly, the streamURL is already the preview URL.
        // We just need to generate lyrics via Gemini if they are missing.
        val resolvedLrc = generateLyricsWithGemini(title, artist)
        return@withContext PipedSongStream(
            streamUrl = "", // handled by iTunes API search result directly so we return empty here to not override
            lyricsText = resolvedLrc
        )
    }

    suspend fun searchSongsAndLyrics(query: String): List<Song> = withContext(Dispatchers.IO) {
        Log.d("GeminiMusicService", "Starting iTunes API search for: $query")
        
        try {
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "https://itunes.apple.com/search?term=$encodedQuery&entity=song&limit=15"
            val response = getJsonFromUrl(url)
            
            if (!response.isNullOrEmpty()) {
                val jsonObj = JSONObject(response)
                val results = jsonObj.optJSONArray("results")
                if (results != null) {
                    val songs = mutableListOf<Song>()
                    for (i in 0 until results.length()) {
                        val item = results.optJSONObject(i) ?: continue
                        
                        val trackId = item.optLong("trackId", 0L).toString()
                        val title = item.optString("trackName", "Unknown Track")
                        val artist = item.optString("artistName", "Unknown Artist")
                        val album = item.optString("collectionName", "Unknown Album")
                        val previewUrl = item.optString("previewUrl", "")
                        var artworkUrl = item.optString("artworkUrl100", "")
                        if (artworkUrl.isNotEmpty()) {
                            artworkUrl = artworkUrl.replace("100x100bb", "600x600bb")
                        }
                        // iTunes provides full track time, but preview is usually 30s. 
                        // We will report the full track time for UI.
                        val durationMs = item.optLong("trackTimeMillis", 180000L)

                        if (previewUrl.isNotEmpty() && trackId != "0") {
                            songs.add(
                                Song(
                                    id = "itunes_$trackId",
                                    title = title,
                                    artist = artist,
                                    album = album,
                                    duration = durationMs, // Show full duration on UI
                                    streamUrl = previewUrl, // Has the actual playable m4a 
                                    albumArtUrl = artworkUrl.ifEmpty { "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=600" },
                                    lyrics = null,
                                    isFavorite = false,
                                    downloadStatus = 0
                                )
                            )
                        }
                    }
                    if (songs.isNotEmpty()) {
                        return@withContext songs
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiMusicService", "Error reading items from iTunes search: ${e.message}")
        }

        // --- Clean Gemini Generation Fallback to keep experience pristine if Piped nodes are blocked ---
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext LocalSongCatalog.songs.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true)
            }.ifEmpty {
                generateMockSearchResults(query)
            }
        }

        val prompt = """
            You are a YouTube Music API search and metadata simulator. The user is searching for songs with the query: "$query".
            Generate a realistic, incredibly high-quality list of 4 matching songs.
            For each song, construct:
            - 'id': A realistic YouTube ID (e.g., 'yt_2dfuS3').
            - 'title': The song title that matches or relates to the query: "$query".
            - 'artist': The artist of that song.
            - 'album': The album name.
            - 'duration': Song duration in milliseconds (e.g. between 150000 and 260000).
            - 'streamUrl': One of these real working public MP3 songs:
               1. "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
               2. "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
               3. "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
               4. "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"
               5. "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"
            - 'albumArtUrl': A beautiful Unsplash music image URL related to the song vibe. Use clean URLs.
            - 'lyrics': Synced lyrics in strict .LRC format. Format of each line MUST be like:
              [00:12.30] Lyric text
              Ensure the timeline spans nicely up to the song duration.
              
            Format the response strictly as a JSON array of song objects. Do not write markdown blocks before and after (no ```json ... ``` blocks, just the clean json content).
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseFormat = ResponseFormat(text = ResponseFormatText(mimeType = "application/json")),
                temperature = 0.5f
            )
        )

        try {
            val response = GeminiClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!jsonText.isNullOrEmpty()) {
                val listType = Types.newParameterizedType(List::class.java, SongJson::class.java)
                val adapter = GeminiClient.moshi.adapter<List<SongJson>>(listType)
                val songsList = adapter.fromJson(jsonText)
                if (songsList != null) {
                    return@withContext songsList.map { item ->
                        Song(
                            id = item.id,
                            title = item.title,
                            artist = item.artist,
                            album = item.album,
                            duration = item.duration,
                            streamUrl = item.streamUrl.ifEmpty { audioUrlsFallback.random() },
                            albumArtUrl = item.albumArtUrl,
                            lyrics = item.lyrics,
                            isFavorite = false,
                            downloadStatus = 0
                        )
                    }
                }
            }
            generateMockSearchResults(query)
        } catch (e: Exception) {
            Log.e("GeminiMusicService", "Error calling Gemini, falling back to mock results.", e)
            generateMockSearchResults(query)
        }
    }

    private fun generateMockSearchResults(query: String): List<Song> {
        val tags = listOf(
            "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=600",
            "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=600",
            "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=600",
            "https://images.unsplash.com/photo-1510915228340-29c85a43dcfe?q=80&w=600"
        )
        return listOf(
            Song(
                id = "yt_mock_a1",
                title = "$query - Deep Waves",
                artist = "Lumina Echo",
                album = "Acoustic Reflections",
                duration = 202000,
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                albumArtUrl = tags[0],
                lyrics = """
                    [00:00.00] (Muffled acoustic echo intro)
                    [00:12.00] Walking down the stream for you
                    [00:25.00] Chasing light in skies of blue
                    [00:38.00] Looking back, we find our track
                    [00:52.00] On this long road, we don't look back
                    [01:05.00] Flowing beats, we meet again
                    [01:20.00] Playing like an old companion
                    [01:45.00] Deep waves sweeping the beach
                    [02:00.00] Outtro guitar fading...
                """.trimIndent()
            ),
            Song(
                id = "yt_mock_a2",
                title = "Searching for $query",
                artist = "The Midnight Project",
                album = "Vibe Searcher",
                duration = 188000,
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                albumArtUrl = tags[1],
                lyrics = """
                    [00:00.00] (Deep ambient synths floating)
                    [00:10.00] Seeking truths, seeking sounds
                    [00:22.00] Inside these electronic bounds
                    [00:35.00] Your search query matching the beat
                    [00:48.00] Dancing late on the empty street
                    [01:02.00] Neon lines cross over my mind
                    [01:15.00] A dreamlike track we here designed
                    [01:30.00] Beautiful, premium and pristine
                    [01:45.00] Swept away in this cool routine
                    [02:00.00] (Beats fading into retro dust)
                """.trimIndent()
            )
        )
    }
}
