package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.StaticData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DualarScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Günlük Dualar") }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(StaticData.dailyPrayers) { dua ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = dua,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
