package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextSubdued
import com.example.ui.theme.TranslucentSystemDrawer
import com.example.viewmodel.LoopTogetherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: LoopTogetherViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()

                // List of screens where standard bottom navigation bar is active
                val showBottomBar = currentScreen in listOf("home", "explore", "profile")

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = TranslucentSystemDrawer,
                                tonalElevation = 8.dp
                            ) {
                                // Home navigation button
                                NavigationBarItem(
                                    selected = currentScreen == "home",
                                    onClick = { viewModel.navigateTo("home") },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home Hub") },
                                    label = { Text("Home", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = NeonBlue,
                                        selectedTextColor = NeonBlue,
                                        indicatorColor = NeonPurple.copy(alpha = 0.2f),
                                        unselectedIconColor = TextSubdued,
                                        unselectedTextColor = TextSubdued
                                    )
                                )

                                // Search/Explore navigation button
                                NavigationBarItem(
                                    selected = currentScreen == "explore",
                                    onClick = { viewModel.navigateTo("explore") },
                                    icon = { Icon(Icons.Default.Search, contentDescription = "Search & Explore") },
                                    label = { Text("Search", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = NeonBlue,
                                        selectedTextColor = NeonBlue,
                                        indicatorColor = NeonPurple.copy(alpha = 0.2f),
                                        unselectedIconColor = TextSubdued,
                                        unselectedTextColor = TextSubdued
                                    )
                                )

                                // User parameters / profile details button
                                NavigationBarItem(
                                    selected = currentScreen == "profile",
                                    onClick = { viewModel.navigateTo("profile") },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Personal Profile") },
                                    label = { Text("Profile", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = NeonBlue,
                                        selectedTextColor = NeonBlue,
                                        indicatorColor = NeonPurple.copy(alpha = 0.2f),
                                        unselectedIconColor = TextSubdued,
                                        unselectedTextColor = TextSubdued
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
                    ) {
                        when (currentScreen) {
                            "splash" -> SplashScreen(viewModel = viewModel)
                            "login" -> LoginScreen(viewModel = viewModel)
                            "home" -> HomeScreen(viewModel = viewModel)
                            "explore" -> ExploreScreen(viewModel = viewModel)
                            "room" -> RoomScreen(viewModel = viewModel)
                            "profile" -> ProfileScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
