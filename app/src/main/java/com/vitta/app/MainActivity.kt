package com.vitta.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.vitta.app.navigation.VittaNavHost
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Vitta solo tiene diseño claro: forzamos iconos oscuros en las barras
        // del sistema para que la hora/batería no salgan blancas sobre el fondo
        // crema cuando el teléfono está en modo oscuro.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            VittaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = VittaColorRoles.background
                ) {
                    VittaNavHost(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                    )
                }
            }
        }
    }
}