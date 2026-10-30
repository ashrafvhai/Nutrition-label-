package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import com.example.ui.components.attemptAutoSignIn
import com.example.ui.navigation.AppNavigation
import com.example.ui.screens.AuthScreen
import com.example.ui.theme.NutriLensTheme
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Step 1: Mandatory App Check initialization with launch intent before any UI or Firebase calls
        configureAppCheck(this, intent)

        // Step 2: Enable full bleed edge-to-edge
        enableEdgeToEdge()

        // Step 3: Set Compose content
        setContent {
            NutriLensTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            val credentialManager = CredentialManager.create(context)
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { currentUser = Firebase.auth.currentUser },
                onUnauthenticated = { /* remain on auth screen */ },
                scope = coroutineScope
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B11))
    ) {
        if (currentUser == null) {
            AuthScreen(
                onAuthSuccess = { currentUser = Firebase.auth.currentUser }
            )
        } else {
            AppNavigation(
                onSignOutSuccess = { currentUser = null }
            )
        }
    }
}
