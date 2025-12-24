package com.mnn.cameranote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.mnn.cameranote.navigation.AppNavHost
import com.mnn.cameranote.ui.theme.TestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TestTheme{
            val navController = rememberNavController()
            AppNavHost(navController)}
        }
    }
}
