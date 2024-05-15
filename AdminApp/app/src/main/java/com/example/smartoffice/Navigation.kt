package com.example.smartoffice

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartoffice.Login.LoginScreen
import com.example.smartoffice.device.ControlPanelScreen
import com.example.smartoffice.group.GroupListScreen
import com.example.smartoffice.setting.SettingsUI
import com.example.smartoffice.worker.WorkerListScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

sealed class BottomBarScreen(
    val route: String,
    val label: String,
    val SelectedIcon:ImageVector,
    val UnSelectedIcon:ImageVector
) {
    object login: BottomBarScreen(
        route="login",
        label="login",
        SelectedIcon= Icons.Filled.Login,
        UnSelectedIcon= Icons.Outlined.Login
    )
    object Worker: BottomBarScreen(
        route="worker",
        label="Workers List",
        SelectedIcon= Icons.Filled.Person,
        UnSelectedIcon= Icons.Outlined.Person
    )
    object Group: BottomBarScreen(
        route="Group",
        label="Groups List",
        SelectedIcon= Icons.Filled.Groups,
        UnSelectedIcon= Icons.Outlined.Groups
    )
    object Devices: BottomBarScreen(
        route="Devices",
        label="Devices Management",
        SelectedIcon= Icons.Filled.DeviceThermostat,
        UnSelectedIcon= Icons.Outlined.DeviceThermostat
    )
    object Settings: BottomBarScreen(
        route="Settings",
        label="Settings",
        SelectedIcon= Icons.Filled.Settings,
        UnSelectedIcon= Icons.Outlined.Settings
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val screens = listOf(
        BottomBarScreen.Worker,
        BottomBarScreen.Group,
        BottomBarScreen.Devices,
        BottomBarScreen.Settings
    )
    NavigationBar(modifier = Modifier
        .clip(RoundedCornerShape(0.dp, 0.dp, 15.dp, 15.dp))) {
        screens.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        modifier =if(currentRoute == screen.route){Modifier.size(30.dp)}else{Modifier.size(24.dp)},
                        imageVector = if(currentRoute == screen.route){screen.SelectedIcon}else{screen.UnSelectedIcon},
                        contentDescription = screen.label,
                    )
                },
                /*label = {
                    Text(
                        screen.label,
                    )
                }*/
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route)  {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
@Composable
fun SetupNavigation(database: DatabaseReference, auth: FirebaseAuth) {
    val navController :NavHostController= rememberNavController()
    NavHost(
        navController = navController,
        startDestination = if(auth.currentUser!=null)BottomBarScreen.Worker.route else BottomBarScreen.login.route
    ) {
        composable(route=BottomBarScreen.login.route) {
            LoginScreen(auth,onLoginClick = { navController.navigate(BottomBarScreen.Worker.route) })
        }
        composable(BottomBarScreen.Group.route) {
            GroupListScreen(auth,navController =navController,database=database)
        }
        composable(BottomBarScreen.Worker.route) {
            WorkerListScreen(auth,navController =navController,database=database)
        }
        composable(BottomBarScreen.Devices.route) {
            ControlPanelScreen(navController=navController,database=database)
        }
        composable(BottomBarScreen.Settings.route){
            SettingsUI(auth,navController =navController,database=database)
        }
    }
}