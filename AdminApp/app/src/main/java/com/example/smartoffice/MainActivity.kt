package com.example.smartoffice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.smartoffice.ui.theme.SmartOfficeTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
             SmartOfficeTheme{
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val database: DatabaseReference = FirebaseDatabase.getInstance().reference

                    val auth = FirebaseAuth.getInstance()

                    var isSplashScreenDismissed by remember { mutableStateOf(false) }

                    if (isSplashScreenDismissed) {
                        SetupNavigation(database,auth)
                    } else {
                        // Show the splash screen
                        SplashScreen(
                            modifier = Modifier.fillMaxSize(),
                            onSplashScreenDismissed = {
                                isSplashScreenDismissed = true
                            }
                        )
                    }
                }
            }
        }
    }
}

