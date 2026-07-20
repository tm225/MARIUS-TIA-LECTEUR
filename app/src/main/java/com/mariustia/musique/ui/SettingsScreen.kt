package com.mariustia.musique.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mariustia.musique.ui.theme.ColorSwatches
import com.mariustia.musique.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    currentColor: Color,
    trackCount: Int,
    onColorSelected: (Color) -> Unit,
    onOpenEqualizer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            "PARAMÈTRES",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE6F1FF)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "// configuration du système audio",
            color = TextSecondary,
            style = MaterialTheme.typography.labelSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "COULEUR DU LECTEUR",
            color = currentColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Choisissez la couleur d'accentuation de l'application",
            color = TextSecondary,
            style = MaterialTheme.typography.labelSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.height(180.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(ColorSwatches) { color ->
                ColorSwatch(
                    color = color,
                    selected = color == currentColor,
                    onClick = { onColorSelected(color) }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        HorizontalDivider(color = Color(0xFF1E2733))
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onOpenEqualizer() }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = currentColor)
                Spacer(modifier = Modifier.width(10.dp))
                Text("ÉGALISEUR AUDIO", color = Color(0xFFE6F1FF), fontWeight = FontWeight.Bold)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xFF1E2733))
        Spacer(modifier = Modifier.height(20.dp))

        InfoRow(label = "APPLICATION", value = "MARIUS TIA MUSIQUE")
        InfoRow(label = "VERSION", value = "1.0")
        InfoRow(label = "PISTES INDEXÉES", value = trackCount.toString())
        InfoRow(label = "DÉVELOPPEUR", value = "Tia Toikesse Marius")
    }
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) Color.White else Color(0xFF1E2733),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(Icons.Filled.Check, contentDescription = "Sélectionné", tint = Color.Black)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        Text(value, color = Color(0xFFE6F1FF), style = MaterialTheme.typography.labelSmall)
    }
}
