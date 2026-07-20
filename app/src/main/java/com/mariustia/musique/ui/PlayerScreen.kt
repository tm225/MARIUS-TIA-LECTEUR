package com.mariustia.musique.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mariustia.musique.data.Track
import com.mariustia.musique.data.formatDuration
import com.mariustia.musique.ui.theme.TextSecondary

@Composable
fun PlayerScreen(
    track: Track?,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    shuffleOn: Boolean,
    repeatOn: Boolean,
    accentColor: Color,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (track == null) {
            Text(
                "AUCUNE PISTE EN COURS\n> sélectionnez un fichier dans la bibliothèque_",
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            return@Column
        }

        // Pochette / visualiseur stylisé "terminal"
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF11161F))
                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.GraphicEq,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(96.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = track.title,
            color = Color(0xFFE6F1FF),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 2,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${track.artist} — ${track.album}",
            color = TextSecondary,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(20.dp))

        Slider(
            value = if (durationMs > 0) positionMs.toFloat() / durationMs.toFloat() else 0f,
            onValueChange = { fraction -> onSeek((fraction * durationMs).toLong()) },
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = Color(0xFF1E2733)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatDuration(positionMs), color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            Text(formatDuration(durationMs), color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            IconToggleButton(checked = shuffleOn, onCheckedChange = { onToggleShuffle() }) {
                Icon(
                    Icons.Filled.Shuffle,
                    contentDescription = "Aléatoire",
                    tint = if (shuffleOn) accentColor else TextSecondary
                )
            }

            IconButton(onClick = onPrevious) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "Précédent", tint = Color(0xFFE6F1FF), modifier = Modifier.size(36.dp))
            }

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(50))
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Lecture / Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            IconButton(onClick = onNext) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Suivant", tint = Color(0xFFE6F1FF), modifier = Modifier.size(36.dp))
            }

            IconToggleButton(checked = repeatOn, onCheckedChange = { onToggleRepeat() }) {
                Icon(
                    Icons.Filled.Repeat,
                    contentDescription = "Répéter",
                    tint = if (repeatOn) accentColor else TextSecondary
                )
            }
        }
    }
}
