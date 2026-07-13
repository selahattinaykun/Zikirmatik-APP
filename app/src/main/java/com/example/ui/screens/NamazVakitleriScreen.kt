package com.example.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.PrayerTimesViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun NamazVakitleriScreen() {
    val viewModel: PrayerTimesViewModel = viewModel()
    val context = LocalContext.current
    val timings by viewModel.timings.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            viewModel.fetchTimings(context)
        } else {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Namaz Vakitleri") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!locationPermissions.allPermissionsGranted) {
                Text("Konum izni gerekli. Varsayılan olarak İstanbul vakitleri gösterilecek.")
                Button(onClick = { locationPermissions.launchMultiplePermissionRequest() }) {
                    Text("İzin İste")
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Fallback attempt
                LaunchedEffect(Unit) {
                    viewModel.fetchTimings(context)
                }
            }

            if (loading) {
                CircularProgressIndicator()
            } else if (error != null) {
                Text(text = error!!, color = MaterialTheme.colorScheme.error)
            } else if (timings != null) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { PrayerTimeCard("Sabah", timings!!.Fajr) }
                    item { PrayerTimeCard("Öğle", timings!!.Dhuhr) }
                    item { PrayerTimeCard("İkindi", timings!!.Asr) }
                    item { PrayerTimeCard("Akşam", timings!!.Maghrib) }
                    item { PrayerTimeCard("Yatsı", timings!!.Isha) }
                }
            }
        }
    }
}

@Composable
fun PrayerTimeCard(name: String, time: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, style = MaterialTheme.typography.bodyLarge)
            Text(text = time, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
