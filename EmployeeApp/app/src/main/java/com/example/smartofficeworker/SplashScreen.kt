package com.example.smartofficeworker

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Quotes(val number: Int, val Phrases: String,val author:String)

private val _quotes = mutableListOf(
    Quotes(1, "Learn as if you will live forever, live like you will die tomorrow.","-Mahatma Gandhi"),
    Quotes(2, "Success is not final; failure is not fatal: It is the courage to continue that counts","-Winston ChurchillWinston Churchill"),
    Quotes(3, "You learn more from failure than from success. Don’t let it stop you. Failure builds character.","—Unknown")
)

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onSplashScreenDismissed: () -> Unit
) {  val alpha =
    animateFloatAsState(
    targetValue = 1f ,
    animationSpec = tween(durationMillis = 300), label = "")
    var rnd = (0 until _quotes.size).random()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),

        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment =Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,modifier=Modifier.padding(16.dp)) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(200.dp).alpha(alpha.value),
            )
            Spacer(modifier=Modifier.height(16.dp))
            Text(_quotes[rnd].Phrases, textAlign = TextAlign.Start, fontFamily = FontFamily.Cursive,fontSize=15.sp, color = MaterialTheme.colorScheme.primary)
            Text(_quotes[rnd].author, textAlign = TextAlign.Center, fontFamily = FontFamily.Cursive,fontSize=9.sp, color = MaterialTheme.colorScheme.secondary)
        }
        val coroutineScope = rememberCoroutineScope()
        coroutineScope.launch {
            delay(2000)
            onSplashScreenDismissed()
        }
    }
}