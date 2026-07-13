package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import kotlin.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.StaticData
import com.example.data.Surah
import kotlinx.coroutines.delay

@OptIn(androidx.media3.common.util.UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SurelerScreen() {
    val context = LocalContext.current
    
    // Remember a single ExoPlayer instance for the lifetime of this Composable
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    var currentlyPlayingSurah by remember { mutableStateOf<Surah?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableFloatStateOf(0f) }
    var totalDuration by remember { mutableLongStateOf(0L) }
    var isUserSeeking by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Map each surah to its high-speed online URL as a robust fallback
    val onlineUrls = remember {
        mapOf(
            "Fatiha Suresi" to "https://download.quranicaudio.com/quran/mishari_rashid_al_afasy/001.mp3",
            "Ayet-el Kürsi" to "https://archive.org/download/AyatulKursiByMisharyRashidAlafasy/Ayatul%20Kursi%20By%20Mishary%20Rashid%20Alafasy.mp3",
            "İhlas Suresi" to "https://download.quranicaudio.com/quran/mishari_rashid_al_afasy/112.mp3",
            "Felak Suresi" to "https://download.quranicaudio.com/quran/mishari_rashid_al_afasy/113.mp3",
            "Nas Suresi" to "https://download.quranicaudio.com/quran/mishari_rashid_al_afasy/114.mp3",
            "Yasin Suresi" to "https://download.quranicaudio.com/quran/mishari_rashid_al_afasy/036.mp3",
            "Mülk Suresi" to "https://download.quranicaudio.com/quran/mishari_rashid_al_afasy/067.mp3",
            "Tarık Suresi" to "https://download.quranicaudio.com/quran/mishari_rashid_al_afasy/086.mp3",
            "Beyyine Suresi" to "https://download.quranicaudio.com/quran/mishari_rashid_al_afasy/098.mp3",
            "Fetih Suresi" to "https://download.quranicaudio.com/quran/mishari_rashid_al_afasy/048.mp3"
        )
    }

    // Set up Player Listener to sync player state with Compose state
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingChanged: Boolean) {
                isPlaying = isPlayingChanged
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                isLoading = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                } else if (playbackState == Player.STATE_ENDED) {
                    isPlaying = false
                    currentPosition = 0f
                    currentlyPlayingSurah = null
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                error.printStackTrace()
                // If local raw playing fails, let's automatically retry with the online URL!
                val surah = currentlyPlayingSurah
                if (surah != null) {
                    val onlineUrl = onlineUrls[surah.name]
                    if (onlineUrl != null && exoPlayer.currentMediaItem?.localConfiguration?.uri?.toString()?.startsWith("android.resource") == true) {
                        Toast.makeText(context, "Yerel ses yüklenemedi. İnternetten oynatılıyor...", Toast.LENGTH_SHORT).show()
                        val mediaItem = MediaItem.fromUri(Uri.parse(onlineUrl))
                        exoPlayer.setMediaItem(mediaItem)
                        exoPlayer.prepare()
                        exoPlayer.play()
                    } else {
                        Toast.makeText(context, "Ses oynatılamadı: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                        isPlaying = false
                        currentlyPlayingSurah = null
                    }
                } else {
                    isPlaying = false
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Polling effect to update progress seek bar
    LaunchedEffect(isPlaying, currentlyPlayingSurah, isUserSeeking) {
        if (isPlaying && currentlyPlayingSurah != null && !isUserSeeking) {
            while (isPlaying) {
                currentPosition = exoPlayer.currentPosition.toFloat()
                delay(250)
            }
        }
    }

    val playAudio = { sure: Surah ->
        try {
            if (currentlyPlayingSurah == sure) {
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                    isPlaying = false
                } else {
                    exoPlayer.play()
                    isPlaying = true
                }
            } else {
                currentlyPlayingSurah = sure
                isLoading = true
                
                // Try playing from local raw resource first
                val localUri = if (sure.audioResId != null) {
                    Uri.parse("android.resource://${context.packageName}/${sure.audioResId}")
                } else {
                    null
                }

                // Fallback to online URL if localUri is null
                val playUri = localUri ?: onlineUrls[sure.name]?.let { Uri.parse(it) }

                if (playUri != null) {
                    val mediaItem = MediaItem.fromUri(playUri)
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.play()
                    isPlaying = true
                } else {
                    Toast.makeText(context, "Ses kaynağı bulunamadı", Toast.LENGTH_SHORT).show()
                    currentlyPlayingSurah = null
                    isLoading = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isPlaying = false
            currentlyPlayingSurah = null
            isLoading = false
            Toast.makeText(context, "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    val stopAudio = {
        try {
            exoPlayer.stop()
        } catch (e: Exception) {}
        currentlyPlayingSurah = null
        isPlaying = false
        currentPosition = 0f
        totalDuration = 0L
    }

    fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Günlük Takip Edilecek Sureler", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = if (currentlyPlayingSurah != null) 160.dp else 16.dp)
            ) {
                items(StaticData.dailySurahs) { sure ->
                    SurahCard(
                        sure = sure,
                        isPlaying = currentlyPlayingSurah == sure && isPlaying,
                        isLoading = currentlyPlayingSurah == sure && isLoading,
                        onPlayClick = { playAudio(sure) }
                    )
                }
            }

            // Beautiful Bottom MP3 Player Control Panel
            AnimatedVisibility(
                visible = currentlyPlayingSurah != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                currentlyPlayingSurah?.let { surah ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Header: Now Playing
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isLoading) "YÜKLENİYOR..." else "ŞİMDİ OYNATILIYOR",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = surah.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                IconButton(onClick = { stopAudio() }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Kapat",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Slider (Seek bar)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatDuration(currentPosition.toLong()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                
                                Slider(
                                    value = currentPosition,
                                    onValueChange = {
                                        isUserSeeking = true
                                        currentPosition = it
                                    },
                                    onValueChangeFinished = {
                                        exoPlayer.seekTo(currentPosition.toLong())
                                        isUserSeeking = false
                                    },
                                    valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                                    )
                                )

                                Text(
                                    text = formatDuration(totalDuration),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            // Control buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Rewind 10s
                                IconButton(
                                    onClick = {
                                        val newPos = (exoPlayer.currentPosition - 10000).coerceAtLeast(0)
                                        exoPlayer.seekTo(newPos)
                                        currentPosition = newPos.toFloat()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Replay10,
                                        contentDescription = "10 Saniye Geri",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Play/Pause with buffering indicator
                                FilledIconButton(
                                    onClick = { playAudio(surah) },
                                    modifier = Modifier.size(54.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.5.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Fast Forward 10s
                                IconButton(
                                    onClick = {
                                        val newPos = (exoPlayer.currentPosition + 10000).coerceAtMost(totalDuration)
                                        exoPlayer.seekTo(newPos)
                                        currentPosition = newPos.toFloat()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Forward10,
                                        contentDescription = "10 Saniye İleri",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SurahCard(sure: Surah, isPlaying: Boolean, isLoading: Boolean, onPlayClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPlaying) 4.dp else 1.dp)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sure.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                FilledTonalIconButton(
                    onClick = onPlayClick,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Dinle"
                        )
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = sure.arabicText,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = sure.turkishMeaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
                )
            }
        }
    }
}
