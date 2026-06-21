package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.SocialHubApp
import com.example.ui.SocialHubViewModel
import com.example.ui.theme.SocialHubTheme

class MainActivity : ComponentActivity() {
  private val viewModel: SocialHubViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      SocialHubTheme {
        SocialHubApp(viewModel)
      }
    }
  }
}
