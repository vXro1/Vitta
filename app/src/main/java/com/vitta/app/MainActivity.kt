package com.vitta.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.vitta.app.navigation.VittaNavHost
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VittaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = VittaColorRoles.background
                ) {
                    VittaNavHost()
                }
            }
        }
    }
}
