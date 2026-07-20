package com.mariustia.musique.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mariustia.musique.data.Playlist
import com.mariustia.musique.data.Track
import com.mariustia.musique.ui.theme.SurfaceLight
import com.mariustia.musique.ui.theme.TextSecondary

@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    accentColor: Color,
    onCreatePlaylist: (String) -> Unit,
    onOpenPlaylist: (Playlist) -> Unit,
    onDeletePlaylist: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        if (playlists.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    "AUCUNE PLAYLIST\n> créez-en une avec le bouton +",
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(playlists, key = { it.name }) { pl ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceLight.copy(alpha = 0.5f))
                            .clickable { onOpenPlaylist(pl) }
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.QueueMusic, contentDescription = null, tint = accentColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pl.name, color = Color(0xFFE6F1FF), fontWeight = FontWeight.Bold)
                            Text(
                                "${pl.trackIds.size} piste(s)",
                                color = TextSecondary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        IconButton(onClick = { onDeletePlaylist(pl.name) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Supprimer", tint = TextSecondary)
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            containerColor = accentColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Nouvelle playlist", tint = Color.Black)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false; newName = "" },
            title = { Text("NOUVELLE PLAYLIST", color = Color(0xFFE6F1FF)) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text("nom_playlist") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor, cursorColor = accentColor)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onCreatePlaylist(newName.trim())
                    newName = ""
                    showDialog = false
                }) { Text("CRÉER", color = accentColor, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false; newName = "" }) {
                    Text("ANNULER", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF11161F)
        )
    }
}

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    allTracks: List<Track>,
    accentColor: Color,
    currentTrackId: Long?,
    onBack: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onRemoveTrack: (Long) -> Unit,
    onAddTracks: () -> Unit
) {
    val tracksInPlaylist = remember(playlist, allTracks) {
        playlist.trackIds.mapNotNull { id -> allTracks.find { it.id == id } }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Retour", tint = accentColor)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(playlist.name.uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFFE6F1FF), style = MaterialTheme.typography.titleLarge)
                Text("${tracksInPlaylist.size} piste(s)", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onAddTracks) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter des pistes", tint = accentColor)
            }
        }

        if (tracksInPlaylist.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    "PLAYLIST VIDE\n> touchez + pour ajouter des pistes",
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                items(tracksInPlaylist, key = { it.id }) { track ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (track.id == currentTrackId) accentColor.copy(alpha = 0.12f) else SurfaceLight.copy(alpha = 0.4f))
                            .clickable { onTrackClick(track) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.PlaylistPlay, contentDescription = null, tint = if (track.id == currentTrackId) accentColor else TextSecondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(track.title, color = Color(0xFFE6F1FF), maxLines = 1)
                            Text(track.artist, color = TextSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                        IconButton(onClick = { onRemoveTrack(track.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Retirer", tint = TextSecondary)
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
