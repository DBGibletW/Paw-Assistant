package com.pawbyte.pawassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pawbyte.pawassistant.ui.ChatScreen
import com.pawbyte.pawassistant.ui.theme.PawAssistantTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PawAssistantTheme {
                ChatScreen()
            }
        }
    }
}
