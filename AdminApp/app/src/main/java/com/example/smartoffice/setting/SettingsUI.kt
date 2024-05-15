package com.example.smartoffice.setting
import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smartoffice.AppBottomBar
import com.example.smartoffice.BottomBarScreen
import com.example.smartoffice.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsUI(
    auth: FirebaseAuth,
    database: DatabaseReference,
    navController: NavHostController,
    viewModel: SettingsViewModel = remember { SettingsViewModel(database) }
) {
    val lato= FontFamily(Font(R.font.lato_bold))
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.secondary,
                ),
                title = {
                    Text(
                        "Settings",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = lato,
                    )
                },
                navigationIcon={
                    IconButton(onClick = { viewModel.signOut(auth)
                        navController.navigate(BottomBarScreen.login.route)  {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                            restoreState = true
                        }
                    }) {
                        Icon(imageVector = Icons.Filled.ExitToApp, contentDescription =null )
                    }
                })
        },
        bottomBar = {
            AppBottomBar(
                navController
            )
        }
    ) {
        Box() {
            Image(
                painter = painterResource(id = R.drawable.background_gradient_lights),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.2f))

        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Toggle Switches
            SwitchSetting("Enable Notifications", viewModel.enableNotifications)
            SwitchSetting("Dark Mode", viewModel.darkMode)
            SwitchSetting("Sound Effects", viewModel.soundEffects)

            //TextSetting("User Name", viewModel.userName)
            // TextSetting("Email", viewModel.email)

            // Selection Menus
            //SelectionSetting("Theme", viewModel.selectedTheme, listOf("Light", "Dark", "System Default"))

            // Numeric Input
            //NumericSetting("Font Size", viewModel.fontSize)

            // Buttons
            Row(verticalAlignment = Alignment.Bottom){
                Button(onClick = { viewModel.resetToDefault() }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Reset to Default")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { viewModel.saveSettings() }, modifier = Modifier.padding(top = 16.dp)) {
                Text("Save")
            }
            }

        }
    }
}}

@Composable
private fun SwitchSetting(label: String, checkedState: MutableState<Boolean>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = label, modifier = Modifier.weight(1f),style = MaterialTheme.typography.titleMedium)
        Switch(checked = checkedState.value, onCheckedChange = { newValue ->
            checkedState.value = newValue
        })
    }
}

@Composable
private fun TextSetting(label: String, textState: MutableState<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        BasicTextField(
            value = textState.value,
            onValueChange = { newValue ->
                textState.value = newValue
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

@Composable
private fun SelectionSetting(label: String, selectedItem: MutableState<String>, options: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(options[0]) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = selectedOption, color = Color.White)
            IconButton(
                onClick = { expanded = true },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color.White
                )
            }

        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .padding(8.dp)
                .background(Color.Gray)
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    selectedOption = option
                },
                    text = { Text(text = option) }
                )
            }


        }
    }
}

@Composable
private fun NumericSetting(label: String, numericState: MutableState<Int>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        BasicTextField(
            value = numericState.value.toString(),
            onValueChange = { newValue ->
                numericState.value = newValue.toIntOrNull() ?: 0
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}