package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.StaticData
import com.example.data.Surah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurelerScreen() {
    val context = LocalContext.current
    var currentlyPlayingSurah by remember { mutableStateOf<Surah?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer?.release()
        }
    }

    val playAudio = { sure: Surah ->
        try {
            if (currentlyPlayingSurah == sure && isPlaying) {
                exoPlayer?.pause()
                isPlaying = false
            } else if (currentlyPlayingSurah == sure && !isPlaying) {
                exoPlayer?.play()
                isPlaying = true
            } else {
                try {
                    exoPlayer?.stop()
                } catch (e: Exception) {}
                try {
                    exoPlayer?.release()
                } catch (e: Exception) {}
                
                sure.audioResId?.let { resId ->
                    val player = ExoPlayer.Builder(context).build()
                    val uri = Uri.parse("android.resource://${context.packageName}/$resId")
                    val mediaItem = MediaItem.fromUri(uri)
                    
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_ENDED) {
                                isPlaying = false
                                currentlyPlayingSurah = null
                            }
                        }
                    })
                    player.play()
                    
                    exoPlayer = player
                    currentlyPlayingSurah = sure
                    isPlaying = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try { exoPlayer?.release() } catch (ex: Exception) {}
            exoPlayer = null
            isPlaying = false
            currentlyPlayingSurah = null
            android.widget.Toast.makeText(context, "Hata oluştu: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val stopAudio = {
        try {
            exoPlayer?.stop()
        } catch (e: Exception) {}
        try {
            exoPlayer?.release()
        } catch (e: Exception) {}
        exoPlayer = null
        currentlyPlayingSurah = null
        isPlaying = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Günlük Takip Edilecek Sureler") }) },
        bottomBar = {
            if (currentlyPlayingSurah != null) {
                BottomAppBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = currentlyPlayingSurah!!.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row {
                            IconButton(onClick = { playAudio(currentlyPlayingSurah!!) }) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.PauseCircleOutline else Icons.Filled.PlayCircleOutline,
                                    contentDescription = "Oynat/Duraklat"
                                )
                            }
                            IconButton(onClick = { stopAudio() }) {
                                Icon(
                                    imageVector = Icons.Filled.StopCircle,
                                    contentDescription = "Durdur"
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(StaticData.dailySurahs) { sure ->
                SurahCard(
                    sure = sure,
                    isPlaying = currentlyPlayingSurah == sure && isPlaying,
                    onPlayClick = { playAudio(sure) }
                )
            }
        }
    }
}

@Composable
fun SurahCard(sure: Surah, isPlaying: Boolean, onPlayClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = sure.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (sure.audioResId != null) {
                    IconButton(onClick = onPlayClick) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.PauseCircleOutline else Icons.Filled.PlayCircleOutline,
                            contentDescription = "Dinle",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = sure.arabicText,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = sure.turkishMeaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
