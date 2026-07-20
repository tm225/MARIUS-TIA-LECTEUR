package com.mariustia.musique.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
    sleepTimerMinutesLeft: Int?,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onSetSleepTimer: (Int?) -> Unit
) {
    var showSleepDialog by remember { mutableStateOf(false) }

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

        // Disque rotatif avec pochette réelle (tourne pendant la lecture)
        val infiniteTransition = rememberInfiniteTransition(label = "disque")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 12000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )

        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(Color(0xFF11161F))
                .border(2.dp, accentColor.copy(alpha = 0.6f), CircleShape)
                .rotate(if (isPlaying) rotation else 0f),
            contentAlignment = Alignment.Center
        ) {
            var artLoaded by remember(track.id) { mutableStateOf(true) }
            if (artLoaded) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(track.albumArtUri)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    onError = { artLoaded = false }
                )
            } else {
                Icon(
                    Icons.Filled.GraphicEq,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(80.dp)
                )
            }
            // Trou central façon vinyle
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0A0E14))
                    .border(1.dp, accentColor, CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Barres d'égalisation animées (vivantes pendant la lecture)
        EqualizerBars(accentColor = accentColor, animating = isPlaying)

        Spacer(modifier = Modifier.height(16.dp))

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

            // Bouton lecture/pause avec pulsation douce pendant la lecture
            val pulseTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by pulseTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 900, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseScale"
            )
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .graphicsLayer(
                        scaleX = if (isPlaying) pulseScale else 1f,
                        scaleY = if (isPlaying) pulseScale else 1f
                    )
                    .clip(CircleShape)
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

        Spacer(modifier = Modifier.height(14.dp))

        // Minuteur de sommeil
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { showSleepDialog = true }
        ) {
            Icon(
                Icons.Filled.Bedtime,
                contentDescription = "Minuteur de sommeil",
                tint = if (sleepTimerMinutesLeft != null) accentColor else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = sleepTimerMinutesLeft?.let { "ARRÊT DANS $it MIN" } ?: "MINUTEUR DE SOMMEIL",
                color = if (sleepTimerMinutesLeft != null) accentColor else TextSecondary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }

    if (showSleepDialog) {
        val options = listOf(15, 30, 45, 60)
        AlertDialog(
            onDismissRequest = { showSleepDialog = false },
            title = { Text("MINUTEUR DE SOMMEIL", color = Color(0xFFE6F1FF)) },
            text = {
                Column {
                    options.forEach { minutes ->
                        Text(
                            "$minutes minutes",
                            color = Color(0xFFE6F1FF),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSetSleepTimer(minutes)
                                    showSleepDialog = false
                                }
                                .padding(vertical = 10.dp)
                        )
                    }
                    Text(
                        "Désactiver",
                        color = TextSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSetSleepTimer(null)
                                showSleepDialog = false
                            }
                            .padding(vertical = 10.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepDialog = false }) { Text("FERMER", color = accentColor) }
            },
            containerColor = Color(0xFF11161F)
        )
    }
}

@Composable
private fun EqualizerBars(accentColor: Color, animating: Boolean) {
    val barCount = 5
    val transition = rememberInfiniteTransition(label = "bars")
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(28.dp)
    ) {
        repeat(barCount) { i ->
            val heightFraction by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 420 + i * 90, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar$i"
            )
            val fraction = if (animating) heightFraction else 0.2f
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight(fraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor.copy(alpha = 0.85f))
            )
        }
    }
}
