package com.pageos.launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Thin programmatic entry point.
 *
 * Page's real UI lives in [PageLauncherActivity] (the HOME activity). This
 * activity exists so the launcher can be opened from code, deep links, or
 * development tooling without creating a second home-screen icon. It simply
 * forwards to [PageLauncherActivity] and finishes.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(
            Intent(this, PageLauncherActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        finish()
    }
}
