package com.example.smartoffice.device

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.example.smartoffice.AppBottomBar
import com.example.smartoffice.R
import com.google.firebase.database.DatabaseReference

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ControlPanelScreen(
    database:DatabaseReference,
    navController : NavHostController,
    viewModel: ControlPanelViewModel = remember { ControlPanelViewModel(database) }
) {
    val lights by viewModel.lights.collectAsState()
    val sensors by viewModel.sensors.collectAsState()
    val lockDown by viewModel.lockDownMode.collectAsState()
    val alpha = animateFloatAsState(
        targetValue = 1f ,
        animationSpec = tween(durationMillis = 300), label = "")
    val (showPopup, setshowPopup) = remember { mutableStateOf(false) }
    val lato= FontFamily(Font(R.font.lato_bold))
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.secondary,
                ),
                title = {
                    Text(
                        "Devices Management",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = lato,
                    )
                })
        },
        bottomBar = {
        AppBottomBar(
            navController
        )
    }) {
        Box(modifier = Modifier
            .padding(it)
            .fillMaxSize()){
            Image(
                painter = painterResource(id = R.drawable.background_gradient_lights),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.2f))
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LazyColumn {
                items(lights) { light ->
                    LightComponent(light, viewModel)
                }
            }
            LazyColumn {
                items(sensors) { sensor ->
                    SensorComponent(sensor = sensor)
                }
            }

            // Button for office lockdown mode
            Button(
                onClick = { setshowPopup(true)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(top = 16.dp)
            ) {
                Text(if(lockDown){"Disable Lockdown Mode"}else{"Enable lockdown mode"})
            }
        }
        if (showPopup) {
            AlertDialog(
                modifier = Modifier
                    .animateContentSize()
                    .alpha(alpha.value),
                onDismissRequest = {
                    // Close the dialog when dismissed
                    setshowPopup(false)
                },
                title = {
                    Text("Confirmation")
                },
                text = {
                    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if(lockDown){"Are you sure to disable lockdown mode?"}else{"Are you sure to enable lockdown mode?"}, textAlign = TextAlign.Center)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.toggleLockdownMode(!lockDown)
                            setshowPopup(false)
                            Toast.makeText(context, if(!lockDown)"Entering Lockdown mode!" else "Exiting Lockdown mode!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                        Button(
                            onClick = {
                                // Close the dialog without sending the message
                                setshowPopup(false)
                            }
                        ) {
                            Text("Cancel")
                        }
                },
                properties = DialogProperties(
                    dismissOnClickOutside = false // Disable dismissing the dialog on click outside
                ),
                shape = RoundedCornerShape(8.dp), // Set the dialog shape
                containerColor = Color.LightGray, // Set the dialog container background color
                iconContentColor = Color.DarkGray, // Set the icon content color
                textContentColor = Color.DarkGray, // Set the text content color
                tonalElevation = 8.dp, // Set the tonal elevation

            )
        }
    }
}

@Composable
fun LightComponent(light: Light, viewModel: ControlPanelViewModel){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (light.id) {
            "light1" -> {
                Text("Reception room light" , style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Switch(checked = light.status, onCheckedChange = {newValue-> viewModel.setLight(newValue,light.id)})}
            "light2" -> {
                Text("Meeting room light" , style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Switch(checked = light.status, onCheckedChange = {newValue-> viewModel.setLight(newValue,light.id)})}
            "light3" -> {
                Text("Front door light" , style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Switch(checked = light.status, onCheckedChange = {newValue-> viewModel.setLight(newValue,light.id)})
            }
        }
    }
}

@Composable
private fun SensorComponent(sensor: Sensor) {
    Spacer(modifier=Modifier.height(20.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (sensor.id) {
            "ldrSensor" -> {
                Text("Luminosity percentage:" , style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text("${sensor.value}%",style = MaterialTheme.typography.titleMedium)}
            "pirSensor" -> {
                Text("Movement sensor:" , style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(text=if(sensor.value.toInt() ==1)"Movement in the office" else "No movement captured",style = MaterialTheme.typography.titleMedium)}
            "temperatureSensor" -> {
                Text("Temperature value:" , style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(text=sensor.value+"°C",style = MaterialTheme.typography.titleMedium)
            }
            "time" -> {
                Text("Last check:" , style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(text=sensor.value,style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

