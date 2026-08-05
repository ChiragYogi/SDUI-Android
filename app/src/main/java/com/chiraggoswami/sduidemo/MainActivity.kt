package com.chiraggoswami.sduidemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.chiraggoswami.sduidemo.screen.SduiScreen
import com.chiraggoswami.sduidemo.ui.theme.SDUIDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SDUIDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SduiScreen(screenId = "home", modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
