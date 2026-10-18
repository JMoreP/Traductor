package com.example.traductorlsv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.traductorlsv.navigation.TraductorNavHost
import com.example.traductorlsv.ui.theme.TraductorLSVTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TraductorLSVTheme {
                val navController = rememberNavController()
                TraductorNavHost(navController = navController)
            }
        }
    }
}