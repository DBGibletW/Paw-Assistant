package com.pawbyte.pawassistant

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.pawbyte.pawassistant.ui.AuthScreen
import com.pawbyte.pawassistant.ui.ChatScreen
import com.pawbyte.pawassistant.ui.theme.PawAssistantTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        PawMessagingService.ensureNotificationChannel(applicationContext)

        setContent {
            PawAssistantTheme {
                val auth = remember { FirebaseAuth.getInstance() }
                var currentUser by remember { mutableStateOf(auth.currentUser) }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { _ ->
                    // İzin verilse de reddedilse de uygulama sorunsuz çalışmaya devam eder.
                }

                DisposableEffect(auth) {
                    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                        currentUser = firebaseAuth.currentUser
                    }
                    auth.addAuthStateListener(listener)
                    onDispose {
                        auth.removeAuthStateListener(listener)
                    }
                }

                LaunchedEffect(currentUser?.uid) {
                    if (currentUser != null) {
                        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                            PawMessagingService.saveToken(applicationContext, token)
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val granted = ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED

                            val prefs = getSharedPreferences(
                                "paw_assistant_fcm_prefs",
                                Context.MODE_PRIVATE
                            )
                            val alreadyAsked = prefs.getBoolean("notification_permission_asked", false)

                            if (!granted && !alreadyAsked) {
                                prefs.edit()
                                    .putBoolean("notification_permission_asked", true)
                                    .apply()
                                notificationPermissionLauncher.launch(
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            }
                        }
                    }
                }

                if (currentUser == null) {
                    AuthScreen()
                } else {
                    ChatScreen(
                        onSignOut = { auth.signOut() }
                    )
                }
            }
        }
    }
}
