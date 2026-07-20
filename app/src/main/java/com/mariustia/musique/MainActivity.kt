package com.mariustia.musique

import android.Manifest
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.common.util.concurrent.ListenableFuture
import com.mariustia.musique.audio.EqualizerManager
import com.mariustia.musique.data.MusicScanner
import com.mariustia.musique.data.Playlist
import com.mariustia.musique.data.PlaylistManager
import com.mariustia.musique.data.Track
import com.mariustia.musique.ui.AddTracksScreen
import com.mariustia.musique.ui.EqualizerScreen
import com.mariustia.musique.ui.LibraryHostScreen
import com.mariustia.musique.ui.PlayerScreen
import com.mariustia.musique.ui.PlaylistDetailScreen
import com.mariustia.musique.ui.SettingsScreen
import com.mariustia.musique.ui.theme.MariusTiaMusiqueTheme
import com.mariustia.musique.ui.theme.TextSecondary
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val controllerState = mutableStateOf<MediaController?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferencesManager.init(applicationContext)
        PlaylistManager.init(applicationContext)

        val neededPermission = if (Build.VERSION.SDK_INT >= 33)
            Manifest.permission.READ_MEDIA_AUDIO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        setContent {
            var hasPermission by remember {
                mutableStateOf(
                    androidx.core.content.ContextCompat.checkSelfPermission(
                        this, neededPermission
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                )
            }

            val localLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted -> hasPermission = granted }

            LaunchedEffect(Unit) {
                if (!hasPermission) localLauncher.launch(neededPermission)
            }

            val accentColor by PreferencesManager.accentColor

            MariusTiaMusiqueTheme(accentColor = accentColor) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (hasPermission) {
                        AppRoot(accentColor = accentColor)
                    } else {
                        PermissionRequiredScreen(accentColor = accentColor) {
                            localLauncher.launch(neededPermission)
                        }
                    }
                }
            }
        }

        connectToService()
    }

    private fun connectToService() {
        val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                controllerState.value = controllerFuture?.get()
            },
            androidx.core.content.ContextCompat.getMainExecutor(this)
        )
    }

    @Composable
    fun AppRoot(accentColor: Color) {
        val context = androidx.compose.ui.platform.LocalContext.current
        var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }

        LaunchedEffect(Unit) {
            tracks = MusicScanner.scanAllAudio(context)
        }

        val controller by controllerState
        val playlists by PlaylistManager.playlists

        var currentTrack by remember { mutableStateOf<Track?>(null) }
        var isPlaying by remember { mutableStateOf(false) }
        var position by remember { mutableStateOf(0L) }
        var shuffleOn by remember { mutableStateOf(false) }
        var repeatOn by remember { mutableStateOf(false) }
        var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }

        // Minuteur de sommeil
        var sleepTimerEndMillis by remember { mutableStateOf<Long?>(null) }
        var sleepMinutesLeft by remember { mutableStateOf<Int?>(null) }

        LaunchedEffect(sleepTimerEndMillis) {
            val end = sleepTimerEndMillis
            if (end == null) {
                sleepMinutesLeft = null
                return@LaunchedEffect
            }
            while (true) {
                val remaining = end - System.currentTimeMillis()
                if (remaining <= 0) {
                    controller?.pause()
                    sleepTimerEndMillis = null
                    sleepMinutesLeft = null
                    break
                }
                sleepMinutesLeft = ((remaining / 60000) + 1).toInt()
                delay(1000)
            }
        }

        // Écoute des changements d'état du lecteur
        DisposableEffect(controller) {
            val listener = object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    val id = mediaItem?.mediaId?.toLongOrNull()
                    currentTrack = tracks.find { it.id == id }
                }
            }
            controller?.addListener(listener)
            onDispose { controller?.removeListener(listener) }
        }

        // Boucle de mise à jour de la position de lecture
        LaunchedEffect(controller, isPlaying) {
            while (true) {
                controller?.let { position = it.currentPosition }
                delay(500)
            }
        }

        fun playTrackList(list: List<Track>, track: Track) {
            val c = controller ?: return
            val index = list.indexOfFirst { it.id == track.id }
            if (index < 0) return

            val items = list.map { t ->
                MediaItem.Builder()
                    .setMediaId(t.id.toString())
                    .setUri(t.uri)
                    .setMediaMetadata(
                        androidx.media3.common.MediaMetadata.Builder()
                            .setTitle(t.title)
                            .setArtist(t.artist)
                            .setAlbumTitle(t.album)
                            .build()
                    )
                    .build()
            }
            c.setMediaItems(items, index, 0L)
            currentTrack = track
            c.prepare()
            c.play()
        }

        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val showBottomBar = currentRoute in listOf("library", "player", "settings")

        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader(accentColor = accentColor)

            Box(modifier = Modifier.weight(1f)) {
                NavHost(navController = navController, startDestination = "library") {
                    composable("library") {
                        LibraryHostScreen(
                            tracks = tracks,
                            currentTrackId = currentTrack?.id,
                            accentColor = accentColor,
                            playlists = playlists,
                            onTrackClick = { track ->
                                playTrackList(tracks, track)
                                navController.navigate("player") { launchSingleTop = true }
                            },
                            onCreatePlaylist = { name -> PlaylistManager.createPlaylist(name) },
                            onOpenPlaylist = { pl ->
                                selectedPlaylist = pl
                                navController.navigate("playlist_detail") { launchSingleTop = true }
                            },
                            onDeletePlaylist = { name -> PlaylistManager.deletePlaylist(name) }
                        )
                    }
                    composable("player") {
                        PlayerScreen(
                            track = currentTrack,
                            isPlaying = isPlaying,
                            positionMs = position,
                            durationMs = controller?.duration?.coerceAtLeast(0) ?: 0L,
                            shuffleOn = shuffleOn,
                            repeatOn = repeatOn,
                            accentColor = accentColor,
                            sleepTimerMinutesLeft = sleepMinutesLeft,
                            onPlayPause = {
                                controller?.let { if (it.isPlaying) it.pause() else it.play() }
                            },
                            onNext = { controller?.seekToNextMediaItem() },
                            onPrevious = { controller?.seekToPreviousMediaItem() },
                            onSeek = { ms -> controller?.seekTo(ms) },
                            onToggleShuffle = {
                                shuffleOn = !shuffleOn
                                controller?.shuffleModeEnabled = shuffleOn
                            },
                            onToggleRepeat = {
                                repeatOn = !repeatOn
                                controller?.repeatMode =
                                    if (repeatOn) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                            },
                            onSetSleepTimer = { minutes ->
                                sleepTimerEndMillis = minutes?.let { System.currentTimeMillis() + it * 60_000L }
                            }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            currentColor = accentColor,
                            trackCount = tracks.size,
                            onColorSelected = { color -> PreferencesManager.setAccentColor(context, color) },
                            onOpenEqualizer = { navController.navigate("equalizer") { launchSingleTop = true } }
                        )
                    }
                    composable("equalizer") {
                        val available by EqualizerManager.isAvailable
                        val enabled by EqualizerManager.isEnabled
                        val bands by EqualizerManager.bands
                        val presets by EqualizerManager.presets
                        val currentPreset by EqualizerManager.currentPreset
                        EqualizerScreen(
                            accentColor = accentColor,
                            available = available,
                            enabled = enabled,
                            bands = bands,
                            presets = presets,
                            currentPreset = currentPreset,
                            onBack = { navController.popBackStack() },
                            onToggleEnabled = { EqualizerManager.setEnabled(it) },
                            onBandChange = { band, level -> EqualizerManager.setBandLevel(band, level) },
                            onPresetSelected = { EqualizerManager.usePreset(it) }
                        )
                    }
                    composable("playlist_detail") {
                        val pl = selectedPlaylist
                        if (pl != null) {
                            val liveVersion = playlists.find { it.name == pl.name } ?: pl
                            PlaylistDetailScreen(
                                playlist = liveVersion,
                                allTracks = tracks,
                                accentColor = accentColor,
                                currentTrackId = currentTrack?.id,
                                onBack = { navController.popBackStack() },
                                onTrackClick = { track ->
                                    val listTracks = liveVersion.trackIds.mapNotNull { id -> tracks.find { it.id == id } }
                                    playTrackList(listTracks, track)
                                    navController.navigate("player") { launchSingleTop = true }
                                },
                                onRemoveTrack = { id -> PlaylistManager.removeTrackFromPlaylist(liveVersion.name, id) },
                                onAddTracks = { navController.navigate("add_tracks") { launchSingleTop = true } }
                            )
                        }
                    }
                    composable("add_tracks") {
                        val pl = selectedPlaylist
                        if (pl != null) {
                            val liveVersion = playlists.find { it.name == pl.name } ?: pl
                            AddTracksScreen(
                                playlistName = liveVersion.name,
                                allTracks = tracks,
                                trackIdsInPlaylist = liveVersion.trackIds,
                                accentColor = accentColor,
                                onBack = { navController.popBackStack() },
                                onToggleTrack = { track ->
                                    if (track.id in liveVersion.trackIds) {
                                        PlaylistManager.removeTrackFromPlaylist(liveVersion.name, track.id)
                                    } else {
                                        PlaylistManager.addTrackToPlaylist(liveVersion.name, track.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (showBottomBar) {
                BottomNavBar(navController = navController, accentColor = accentColor)
            }
        }
    }

    override fun onDestroy() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onDestroy()
    }
}

@Composable
fun AppHeader(accentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0E14))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "MARIUS TIA MUSIQUE",
                color = accentColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "// lecteur audio local",
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun BottomNavBar(navController: androidx.navigation.NavHostController, accentColor: Color) {
    val items = listOf(
        Triple("library", "Bibliothèque", Icons.Filled.LibraryMusic),
        Triple("player", "Lecteur", Icons.Filled.PlayCircle),
        Triple("settings", "Paramètres", Icons.Filled.Settings)
    )
    NavigationBar(containerColor = Color(0xFF11161F)) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { (route, label, icon) ->
            val selected = currentDestination?.hierarchy?.any { it.route == route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(icon, contentDescription = label, tint = if (selected) accentColor else TextSecondary) },
                label = { Text(label, color = if (selected) accentColor else TextSecondary, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = accentColor.copy(alpha = 0.15f))
            )
        }
    }
}

@Composable
fun PermissionRequiredScreen(accentColor: Color, onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "ACCÈS AUX FICHIERS AUDIO REQUIS",
            color = accentColor,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "MARIUS TIA MUSIQUE a besoin de la permission d'accéder à la musique stockée sur votre téléphone pour construire votre bibliothèque.",
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onRequest, colors = ButtonDefaults.buttonColors(containerColor = accentColor)) {
            Text("AUTORISER L'ACCÈS", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
