package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.data.AppTheme
import com.example.ui.ZikirmatikViewModel
import com.example.utils.UpdateManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyarlarScreen(viewModel: ZikirmatikViewModel) {
    val currentTheme by viewModel.currentTheme.collectAsState()
    val context = LocalContext.current
    val updateManager = remember { UpdateManager(context) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ayarlar") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Tema Seçimi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AppTheme.values()) { theme ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setTheme(theme) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentTheme == theme) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = theme.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (currentTheme == theme) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (currentTheme == theme) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Seçili",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Uygulama Güncellemesi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    var updateUrlText by remember { mutableStateOf(updateManager.getUpdateUrl()) }
                    
                    OutlinedTextField(
                        value = updateUrlText,
                        onValueChange = { 
                            updateUrlText = it
                            updateManager.setUpdateUrl(it)
                        },
                        label = { Text("Güncelleme Sunucu Linki (APK)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        singleLine = true,
                        placeholder = { Text("https://github.com/saykun/ZikirmatikApp/releases/latest/download/app-release.apk") }
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                updateManager.checkForUpdateAndDownload(updateUrlText) 
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Text("Denetle", style = MaterialTheme.typography.bodyLarge)
                        }
                        
                        OutlinedButton(
                            onClick = { 
                                val defaultUrl = "https://github.com/saykun/ZikirmatikApp/releases/latest/download/app-release.apk"
                                updateUrlText = defaultUrl
                                updateManager.setUpdateUrl(defaultUrl)
                            },
                            modifier = Modifier.weight(0.8f),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Text("Sıfırla", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}
