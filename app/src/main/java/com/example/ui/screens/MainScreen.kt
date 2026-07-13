package com.example.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.ZikirmatikViewModel

import androidx.compose.material.icons.filled.Schedule

@Composable
fun MainScreen(viewModel: ZikirmatikViewModel) {
    val navController = rememberNavController()
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Zikirmatik", "Dualar", "Sureler", "Vakitler", "Ayarlar")
    val icons = listOf(Icons.Filled.TouchApp, Icons.Filled.List, Icons.Filled.Book, Icons.Filled.Schedule, Icons.Filled.Settings)
    val routes = listOf("zikirmatik", "dualar", "sureler", "vakitler", "ayarlar")

    Scaffold(
        bottomBar = {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "zikirmatik",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("zikirmatik") { ZikirmatikScreen(viewModel) }
            composable("dualar") { DualarScreen() }
            composable("sureler") { SurelerScreen() }
            composable("vakitler") { NamazVakitleriScreen() }
            composable("ayarlar") { AyarlarScreen(viewModel) }
        }
    }
}
