package com.mariustia.musique.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mariustia.musique.data.Track
import com.mariustia.musique.ui.theme.SurfaceLight
import com.mariustia.musique.ui.theme.TextSecondary

@Composable
fun AddTracksScreen(
    playlistName: String,
    allTracks: List<Track>,
    trackIdsInPlaylist: List<Long>,
    accentColor: Color,
    onBack: () -> Unit,
    onToggleTrack: (Track) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Retour", tint = accentColor)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "AJOUTER À \"${playlistName.uppercase()}\"",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE6F1FF),
                maxLines = 1
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            items(allTracks, key = { it.id }) { track ->
                val added = track.id in trackIdsInPlaylist
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceLight.copy(alpha = 0.4f))
                        .clickable { onToggleTrack(track) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(track.title, color = Color(0xFFE6F1FF), maxLines = 1)
                        Text(track.artist, color = TextSecondary, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                    if (added) {
                        Icon(Icons.Filled.Check, contentDescription = "Déjà ajouté", tint = accentColor)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
