package com.mariustia.musique.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mariustia.musique.data.Playlist
import com.mariustia.musique.data.Track
import com.mariustia.musique.ui.theme.TextSecondary

@Composable
fun LibraryHostScreen(
    tracks: List<Track>,
    currentTrackId: Long?,
    accentColor: Color,
    playlists: List<Playlist>,
    onTrackClick: (Track) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onOpenPlaylist: (Playlist) -> Unit,
    onDeletePlaylist: (String) -> Unit
) {
    var tabIndex by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = tabIndex,
            containerColor = Color(0xFF0A0E14),
            contentColor = accentColor
        ) {
            Tab(
                selected = tabIndex == 0,
                onClick = { tabIndex = 0 },
                text = { Text("PISTES", color = if (tabIndex == 0) accentColor else TextSecondary, style = MaterialTheme.typography.labelSmall) }
            )
            Tab(
                selected = tabIndex == 1,
                onClick = { tabIndex = 1 },
                text = { Text("PLAYLISTS", color = if (tabIndex == 1) accentColor else TextSecondary, style = MaterialTheme.typography.labelSmall) }
            )
        }

        if (tabIndex == 0) {
            LibraryScreen(
                tracks = tracks,
                currentTrackId = currentTrackId,
                accentColor = accentColor,
                onTrackClick = onTrackClick
            )
        } else {
            PlaylistsScreen(
                playlists = playlists,
                accentColor = accentColor,
                onCreatePlaylist = onCreatePlaylist,
                onOpenPlaylist = onOpenPlaylist,
                onDeletePlaylist = onDeletePlaylist
            )
        }
    }
}
