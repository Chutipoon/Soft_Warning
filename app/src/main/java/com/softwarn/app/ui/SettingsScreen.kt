package com.softwarn.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.softwarn.app.util.SoundManager

@Composable
fun SettingsScreen(
    selectedSoundId: Int,
    onSoundSelected: (Int) -> Unit,
    soundManager: SoundManager? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        SoundPickerRow(
            selectedSoundId = selectedSoundId,
            onSoundSelected = onSoundSelected,
            onPreviewSound = { id -> soundManager?.play(id) }
        )
    }
}

@Composable
fun SoundPickerRow(
    selectedSoundId: Int,
    onSoundSelected: (Int) -> Unit,
    onPreviewSound: (Int) -> Unit
) {
    val sounds = listOf(
        SoundManager.SOUND_NONE to "None",
        SoundManager.SOUND_CHIME to "Chime",
        SoundManager.SOUND_BELL to "Bell",
        SoundManager.SOUND_POP to "Pop",
        SoundManager.SOUND_BREATH to "Breath"
    )

    Column {
        Text(
            text = "Warning Sound",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(sounds) { (id, name) ->
                SoundOptionItem(
                    name = name,
                    isSelected = id == selectedSoundId,
                    onSelect = { onSoundSelected(id) },
                    onPreview = { onPreviewSound(id) }
                )
            }
        }
    }
}

@Composable
fun SoundOptionItem(
    name: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Column(
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(8.dp))
        IconButton(
            onClick = { onPreview() },
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Preview",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
