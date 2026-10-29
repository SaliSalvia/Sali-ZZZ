package com.salizzai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.salizzai.app.ui.SaliZZaiAppNavigation
import com.salizzai.app.ui.theme.SaliZZaiTheme

/**
 * Main Activity — single-activity Compose app.
 * Hosts all screens (Chat, History, Discover, Image Gen, Settings).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen (purple background + Z logo)
        val splash = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Edge-to-edge display
        enableEdgeToEdge()

        setContent {
            SaliZZaiTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SaliZZaiAppNavigation()
                }
            }
        }
    }
}
