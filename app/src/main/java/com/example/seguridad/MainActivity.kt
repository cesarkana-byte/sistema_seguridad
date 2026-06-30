package com.example.seguridad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.seguridad.ui.navigation.AppRoot
import com.example.seguridad.ui.theme.SeguridadTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {
            SeguridadTheme {
                AppRoot()
            }
        }
    }
}