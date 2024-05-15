package com.example.smartoffice.Login


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.smartoffice.R
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(auth: FirebaseAuth, onLoginClick: () -> Unit, viewModel: LoginViewModel = remember { LoginViewModel() }) {
    val context = LocalContext.current
    val isImeVisible by rememberImeState()
    val (showPopup, setshowPopup) = remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isUserLoggedIn by remember { viewModel.isUserLoggedIn }
    val error by remember { viewModel.error }
    var enable by remember { mutableStateOf(false) }
    enable = email.isNotBlank() && password.isNotBlank()
    var passwordVisible by remember { mutableStateOf(false) }
    val robito= FontFamily(Font(R.font.robotoi))
    val sanista= FontFamily(Font(R.font.sanista_extra))
    if (isUserLoggedIn) {
        onLoginClick()
    } else {
        GradientBox(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val animatedUpperSectionRatio by animateFloatAsState(
                    targetValue = if (isImeVisible) 0f else 0.35f,
                    label = "",
                )
                AnimatedVisibility(visible = !isImeVisible, enter = fadeIn(), exit = fadeOut()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(animatedUpperSectionRatio),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment=Alignment.CenterHorizontally,
                            verticalArrangement=Arrangement.Center,
                            modifier = Modifier.padding(16.dp) ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            AdminImage()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Welcome!",
                                fontFamily = sanista,
                                textAlign = TextAlign.Start,
                                style = MaterialTheme.typography.headlineLarge,
                                modifier = Modifier.weight(0.4f) // Use weight to allow Text to expand
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                        .background(Color.White),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isSmallScreenHeight()) {
                        Spacer(modifier = Modifier.fillMaxSize(0.03f))
                    } else {
                        Spacer(modifier = Modifier.fillMaxSize(0.07f))
                    }
                    Text(
                        text = "Log In",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black,
                        fontFamily = robito
                        )
                    if (isSmallScreenHeight()) {
                        Spacer(modifier = Modifier.fillMaxSize(0.05f))
                    } else {
                        Spacer(modifier = Modifier.fillMaxSize(0.1f))
                    }
                    OutlinedTextField(
                        singleLine = true,
                        isError = error,
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        leadingIcon = { Icon(Icons.Filled.MailOutline, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        trailingIcon = {
                            val image =
                                if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description =
                                if (passwordVisible) "Hide password" else "Show password"
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = image,
                                    description
                                )
                            }
                        },
                        singleLine = true,
                        isError = error,
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Send, keyboardType = KeyboardType.Password
                        ),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    if (isImeVisible) {
                        Text(
                            text = "Forgot Password?",
                            color = Color.Blue,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                setshowPopup(true)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.loginUser(auth,context, email, password)
                            },
                            //modifier = Modifier.fillMaxWidth().padding(top = 20.dp).padding(horizontal = 16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            enabled = enable,
                            /*colors = ButtonDefaults.buttonColors(
                                   containerColor = Color(0xFF0D4C92),
                                   contentColor = Color.White
                               ),
                                shape = RoundedCornerShape(10.dp)*/
                        ) {
                            Text("Login")
                        }
                    } else {
                            Text(
                                text = "Forgot Password?",
                                color = Color.Blue,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.clickable {
                                    setshowPopup(true)
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    viewModel.loginUser(auth,context, email, password)
                                },
                                //modifier = Modifier.fillMaxWidth().padding(top = 20.dp).padding(horizontal = 16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                enabled = enable,
                                /*colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF0D4C92),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)*/
                            ) {
                                Text("Login")
                                //style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight(500))
                                // }
                            }
                    }
                }
            }
        }
        if (showPopup) {
            PasswordRecoveryDialog(auth,viewModel) {
                setshowPopup(false)
            }
        }
    }
}
@Composable
fun AdminImage() {
    // Display SVG image
    val imagePainter: Painter = painterResource(R.drawable.manager_svgrepo_com)
    Image(
        painter = imagePainter,
        contentDescription = "My Image", // Provide a content description for accessibility
        modifier = Modifier
            .size(100.dp) // Adjust size as needed
    )
}
@Composable
fun WaveLine(color: androidx.compose.ui.graphics.Color) {
    // Draw a wavy line using Canvas
    Canvas(modifier = Modifier.fillMaxSize()) {
        val waveLength = size.width
        val amplitude = 16f
        val waveOffset = 32f

        for (i in 0 until waveLength.toInt() step 32) {
            drawLine(
                color = color,
                start = Offset(i.toFloat(), waveOffset + amplitude),
                end = Offset(i.toFloat() + 16f, waveOffset - amplitude),
                strokeWidth = 4f
            )
        }
    }
}
@Composable
fun PasswordRecoveryDialog(
    auth: FirebaseAuth,
    viewModel: LoginViewModel,
        onDismiss: () -> Unit
    ) {
        val context = LocalContext.current
        var recoveryEmail by remember { mutableStateOf("") }
        AlertDialog(
                onDismissRequest = { onDismiss() },
                title = { Text("Recover Password") },
                text = {
                    OutlinedTextField(
                        value = recoveryEmail,
                        onValueChange = { recoveryEmail = it },
                        label = { Text("E-mail") }
                    )
                },
                confirmButton = {
                    Button(enabled = recoveryEmail.isNotBlank(), onClick = {
                        onDismiss()
                        viewModel.recoverPassword(auth,recoveryEmail,context)
                    }) {
                        Text("Recover")
                    }
                },
                dismissButton = {
                    Button(onClick = { onDismiss() }) {
                        Text("Cancel")
                    }
                }
            )
        }