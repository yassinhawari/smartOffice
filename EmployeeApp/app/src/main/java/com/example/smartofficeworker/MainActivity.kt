package com.example.smartofficeworker

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.smartofficeworker.ui.theme.SmartOfficeWorkerTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartOfficeWorkerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isSplashScreenDismissed by remember { mutableStateOf(false) }

                    val database: DatabaseReference = FirebaseDatabase.getInstance().reference
                    val auth = FirebaseAuth.getInstance()

                    val isNetworkAvailable =
                        remember { mutableStateOf(NetworkUtils.isNetworkAvailable(this)) }



                    if (isSplashScreenDismissed) {
                        if (isNetworkAvailable.value) {
                            SetupNavigation(auth ,database)
                        } else {
                            NoConnectionScreen()
                        }
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
@Composable
fun NoConnectionScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "There is no connection",
            color = Color.Blue,
            modifier = Modifier.padding(16.dp),
            fontFamily = FontFamily(Font(R.font.sanista_extra))
        )
    }
}
object NetworkUtils {
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Network is available
            }

            override fun onLost(network: Network) {
                // Network is lost
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}