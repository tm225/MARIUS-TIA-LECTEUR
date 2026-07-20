package com.mariustia.musique.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mariustia.musique.audio.EqBand
import com.mariustia.musique.ui.theme.SurfaceLight
import com.mariustia.musique.ui.theme.TextSecondary

@Composable
fun EqualizerScreen(
    accentColor: Color,
    available: Boolean,
    enabled: Boolean,
    bands: List<EqBand>,
    presets: List<String>,
    currentPreset: Short,
    onBack: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onBandChange: (Short, Short) -> Unit,
    onPresetSelected: (Short) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Retour", tint = accentColor)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("ÉGALISEUR", fontWeight = FontWeight.Bold, color = Color(0xFFE6F1FF), style = MaterialTheme.typography.titleLarge)
        }

        if (!available) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "L'ÉGALISEUR SERA DISPONIBLE\nDÈS QU'UNE PISTE EST LANCÉE",
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            return@Column
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceLight.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = accentColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ACTIVER L'ÉGALISEUR", color = Color(0xFFE6F1FF))
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggleEnabled,
                colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.4f))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "PRÉRÉGLAGES",
            color = TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            presets.forEachIndexed { i, name ->
                val selected = currentPreset.toInt() == i
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) accentColor else SurfaceLight.copy(alpha = 0.5f))
                        .clickable { onPresetSelected(i.toShort()) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        name,
                        color = if (selected) Color.Black else TextSecondary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            bands.forEach { band ->
                BandSlider(
                    band = band,
                    accentColor = accentColor,
                    enabled = enabled,
                    onChange = { level -> onBandChange(band.index, level) }
                )
            }
        }
    }
}

@Composable
private fun RowScope.BandSlider(
    band: EqBand,
    accentColor: Color,
    enabled: Boolean,
    onChange: (Short) -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .width(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = band.currentLevel.toFloat(),
                onValueChange = { onChange(it.toInt().toShort()) },
                valueRange = band.minLevel.toFloat()..band.maxLevel.toFloat(),
                enabled = enabled,
                modifier = Modifier
                    .rotate(-90f)
                    .width(140.dp),
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = Color(0xFF1E2733)
                )
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (band.centerFreqHz >= 1000) "${band.centerFreqHz / 1000}k" else "${band.centerFreqHz}",
            color = TextSecondary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
