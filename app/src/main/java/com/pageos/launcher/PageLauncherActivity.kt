package com.pageos.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pageos.launcher.ui.PageApp

/**
 * The home screen. Declared with MAIN/HOME/DEFAULT/LAUNCHER so it can be set as
 * the device's default launcher. Hosts the shared [PageApp] composable.
 *
 * Note: because this is a HOME activity, the system already prevents it from
 * being "finished" by Back; we simply render the calm home surface.
 */
class PageLauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PageApp()
        }
    }
}
