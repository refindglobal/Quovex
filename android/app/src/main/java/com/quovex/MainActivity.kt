package com.quovex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.quovex.theme.QuovexTheme
import com.quovex.ui.navigation.QuovexNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            QuovexTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = QuovexTheme.colors.background
                ) {
                    QuovexNavGraph()
                }
            }
        }
    }
}
