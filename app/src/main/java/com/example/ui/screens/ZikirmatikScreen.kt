package com.example.ui.screens

import android.Manifest
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StaticData
import com.example.ui.ZikirmatikViewModel
import com.example.utils.SpeechManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ZikirmatikScreen(viewModel: ZikirmatikViewModel) {
    val currentCount by viewModel.currentCount.collectAsState()
    val currentZikir by viewModel.currentZikir.collectAsState()
    val targetCount by viewModel.targetCount.collectAsState()
    val showCelebration by viewModel.showCelebration.collectAsState()
    val quoteOfTheDay = viewModel.quoteOfTheDay
    val view = LocalView.current
    val context = LocalContext.current

    var expanded by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var targetInput by remember { mutableStateOf(targetCount?.toString() ?: "") }
    
    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    val speechManager = remember {
        SpeechManager(context) {
            viewModel.increment()
            view.playSoundEffect(SoundEffectConstants.CLICK)
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechManager.stopListening()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.FormatQuote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Günün Sözü",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = quoteOfTheDay,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = currentZikir,
                onValueChange = {},
                readOnly = true,
                label = { Text("Zikir Seçin") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                StaticData.zikirler.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            viewModel.setZikir(selectionOption)
                            expanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = targetInput,
            onValueChange = { 
                targetInput = it
                viewModel.setTarget(it)
            },
            label = { Text("Hedef Sayı (Opsiyonel)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(250.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable { 
                    viewModel.increment()
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentCount.toString(),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.reset() }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Sıfırla")
                Spacer(Modifier.width(8.dp))
                Text("Sıfırla")
            }
            Button(
                onClick = {
                    if (micPermissionState.status.isGranted) {
                        if (isListening) {
                            speechManager.stopListening()
                            isListening = false
                        } else {
                            speechManager.startListening()
                            isListening = true
                        }
                    } else {
                        micPermissionState.launchPermissionRequest()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    if (isListening) Icons.Filled.MicOff else Icons.Filled.Mic, 
                    contentDescription = "Ses"
                )
            }
            Button(onClick = { viewModel.saveSession() }) {
                Icon(Icons.Filled.Save, contentDescription = "Kaydet")
                Spacer(Modifier.width(8.dp))
                Text("Kaydet")
            }
        }
        
        if (showCelebration) {
            CelebrationDialog(onDismiss = { viewModel.dismissCelebration() })
        }
    }
}

@Composable
fun CelebrationDialog(onDismiss: () -> Unit) {
    var isScaled by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isScaled) 1.5f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        isScaled = true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Tebrikler",
                    tint = Color(0xFFFFD700), // Gold
                    modifier = Modifier
                        .size(80.dp)
                        .scale(scale)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Maşallah!", style = MaterialTheme.typography.headlineMedium)
            }
        },
        text = {
            Text(
                "Belirlediğiniz zikir hedefine ulaştınız. Allah kabul etsin.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Tamam")
            }
        }
    )
}
