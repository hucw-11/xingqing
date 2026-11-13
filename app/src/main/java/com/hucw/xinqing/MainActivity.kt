package com.hucw.xinqing

import android.os.Bundle
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hucw.xinqing.ui.navigation.XinqingNavHost
import com.hucw.xinqing.ui.theme.XinqingTheme

class MainActivity : ComponentActivity() {
    private val requestedDestination = mutableStateOf<Pair<String, Long>?>(null)

    private fun readDestination(intent: Intent?) {
        val destination = intent?.getStringExtra("destination") ?: return
        if (destination in listOf("today", "timetable")) {
            requestedDestination.value = destination to System.nanoTime()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readDestination(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readDestination(intent)
        setContent {
            XinqingTheme {
                XinqingNavHost(requestedDestination.value)
            }
        }
    }
}
