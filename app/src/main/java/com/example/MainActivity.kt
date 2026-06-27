package com.example

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.ui.SocialHubApp
import com.example.ui.SocialHubViewModel
import com.example.ui.theme.SocialHubTheme

class MainActivity : ComponentActivity() {
  private val viewModel: SocialHubViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Highly optimized cache loader configuration for Coil to eliminate any image loading lag or app hangs
    val imageLoader = ImageLoader.Builder(applicationContext)
        .memoryCache {
            MemoryCache.Builder(applicationContext)
                .maxSizePercent(0.3) // Allocate up to 30% of RAM to image cache
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(applicationContext.cacheDir.resolve("socialhub_image_cache"))
                .maxSizePercent(0.1) // Allocate up to 10% of disk cache
                .build()
        }
        .bitmapConfig(Bitmap.Config.RGB_565) // Cut memory consumption in half
        .crossfade(true) // Smooth transition crossfade
        .build()
    Coil.setImageLoader(imageLoader)

    enableEdgeToEdge()
    setContent {
      SocialHubTheme {
        SocialHubApp(viewModel)
      }
    }
  }
}
