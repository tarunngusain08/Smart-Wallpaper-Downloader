package com.wallshift.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.wallshift.app.ui.WallShiftApp
import com.wallshift.app.ui.theme.WallShiftTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WallShiftTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WallShiftApp()
                }
            }
        }
    }
}
