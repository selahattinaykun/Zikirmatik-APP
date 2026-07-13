package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.ui.ZikirmatikViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ZikirmatikViewModel) {
    val navController = rememberNavController()
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Zikirmatik", "Dualar", "Sureler", "Vakitler", "Ayarlar")
    val icons = listOf(Icons.Filled.TouchApp, Icons.Filled.List, Icons.Filled.Book, Icons.Filled.Schedule, Icons.Filled.Settings)
    val routes = listOf("zikirmatik", "dualar", "sureler", "vakitler", "ayarlar")

    val context = LocalContext.current
    
    // Persistent ExoPlayer instance living as long as MainScreen is active (across tab switches)
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    var currentlyPlayingSurah by remember { mutableStateOf<com.example.data.Surah?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableFloatStateOf(0f) }
    var totalDuration by remember { mutableLongStateOf(0L) }
    var isUserSeeking by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Fallback URLs
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
                val surah = currentlyPlayingSurah
                if (surah != null) {
                    val onlineUrl = onlineUrls[surah.name]
                    val uriString = exoPlayer.currentMediaItem?.localConfiguration?.uri?.toString() ?: ""
                    val isLocal = uriString.isNotEmpty() && !uriString.startsWith("http://") && !uriString.startsWith("https://")
                    if (onlineUrl != null && isLocal) {
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

    val playAudio = { sure: com.example.data.Surah ->
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
                
                // Play from local raw resource (which now holds the high-quality Abdullah Altun recordings)
                val localUri = if (sure.audioResId != null) {
                    androidx.media3.datasource.RawResourceDataSource.buildRawResourceUri(sure.audioResId)
                } else {
                    null
                }

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

    Scaffold(
        bottomBar = {
            Column {
                // Minimized Player sitting right above the bottom NavigationBar
                AnimatedVisibility(
                    visible = currentlyPlayingSurah != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    currentlyPlayingSurah?.let { surah ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedItem = 2
                                    navController.navigate("sureler") {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Mini play/pause button
                                        FilledIconButton(
                                            onClick = { playAudio(surah) },
                                            modifier = Modifier.size(36.dp),
                                            colors = IconButtonDefaults.filledIconButtonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            if (isLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                    contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.width(12.dp))
                                        
                                        Column {
                                            Text(
                                                text = surah.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (isLoading) "Yükleniyor..." else "Abdullah Altun",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Progress time
                                        val curSec = (currentPosition / 1000).toInt()
                                        val totSec = (totalDuration / 1000).toInt()
                                        Text(
                                            text = String.format("%02d:%02d / %02d:%02d", 
                                                curSec / 60, curSec % 60,
                                                totSec / 60, totSec % 60
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        
                                        IconButton(
                                            onClick = { stopAudio() },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = "Kapat",
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                                
                                // Small dynamic linear progress indicator under the bar
                                val progress = if (totalDuration > 0) currentPosition / totalDuration.toFloat() else 0f
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.5.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }
                
                // The bottom NavigationBar itself
                NavigationBar {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(icons[index], contentDescription = item) },
                            label = { Text(item) },
                            selected = selectedItem == index,
                            onClick = {
                                selectedItem = index
                                navController.navigate(routes[index]) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "zikirmatik",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("zikirmatik") { ZikirmatikScreen(viewModel) }
            composable("dualar") { DualarScreen() }
            composable("sureler") { 
                SurelerScreen(
                    currentlyPlayingSurah = currentlyPlayingSurah,
                    isPlaying = isPlaying,
                    currentPosition = currentPosition,
                    totalDuration = totalDuration,
                    isLoading = isLoading,
                    isUserSeeking = isUserSeeking,
                    onPlayClick = playAudio,
                    onStopClick = stopAudio,
                    onSeek = { pos ->
                        exoPlayer.seekTo(pos.toLong())
                        currentPosition = pos
                    },
                    onUserSeekingChange = { isSeeking ->
                        isUserSeeking = isSeeking
                    },
                    onPositionChange = { pos ->
                        currentPosition = pos
                    }
                )
            }
            composable("vakitler") { NamazVakitleriScreen() }
            composable("ayarlar") { AyarlarScreen(viewModel) }
        }
    }
}

