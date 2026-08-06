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
import com.chiraggoswami.sduidemo.static.StaticHomeScreen
import com.chiraggoswami.sduidemo.ui.theme.SDUIDemoTheme

/** `EXTRA_SCREEN_VARIANT` = [VARIANT_STATIC] picks the hardcoded twin; anything else is SDUI. */
const val EXTRA_SCREEN_VARIANT = "screen_variant"
const val VARIANT_STATIC = "static"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // One installable app, one launcher icon. :macrobenchmark cold-starts this same
        // Activity with/without the extra — force-stop between iterations means both paths
        // still measure a genuine cold start, no separate app/flavor needed for that.
        val showStatic = intent?.getStringExtra(EXTRA_SCREEN_VARIANT) == VARIANT_STATIC
        setContent {
            SDUIDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (showStatic) {
                        StaticHomeScreen(modifier = Modifier.padding(innerPadding))
                    } else {
                        SduiScreen(screenId = "home", modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}
