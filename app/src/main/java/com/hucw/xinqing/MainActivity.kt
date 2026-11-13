package com.hucw.xinqing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hucw.xinqing.ui.navigation.XinqingNavHost
import com.hucw.xinqing.ui.theme.XinqingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XinqingTheme {
                XinqingNavHost()
            }
        }
    }
}
