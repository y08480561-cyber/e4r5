package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.tts.CoinManager
import com.example.ui.HomeScreen
import com.example.ui.ProfileScreen
import com.example.ui.RegisterScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val coinManager = remember { CoinManager(this) }
        var isRegistered by remember { mutableStateOf(coinManager.isUserRegistered()) }
        var currentScreen by remember { mutableStateOf("home") }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          if (!isRegistered) {
            RegisterScreen(
              onRegistered = { isRegistered = true }
            )
          } else if (currentScreen == "profile") {
            ProfileScreen(
              onBack = { currentScreen = "home" }
            )
          } else {
            HomeScreen(
              onOpenProfile = { currentScreen = "profile" }
            )
          }
        }
      }
    }
  }
}
