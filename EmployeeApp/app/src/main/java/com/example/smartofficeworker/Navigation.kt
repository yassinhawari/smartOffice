package com.example.smartofficeworker

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartofficeworker.chat.ChatScreen
import com.example.smartofficeworker.login.LoginScreen
import com.example.smartofficeworker.stat.StateScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

sealed class TopBarScreen(
    val route: String,
    val label: String,
) {
    object Login: TopBarScreen(
        route="login",
        label="login",
    )
    object Chat: TopBarScreen(
        route="chat",
        label="Chat Room",
    )
    object States: TopBarScreen(
        route="state",
        label="My States",
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(navController: NavHostController,groupName:String,auth: FirebaseAuth) {
    val lato= FontFamily(Font(R.font.lato_bold))
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    CenterAlignedTopAppBar(colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.primary,
    ),title = {
        if(currentRoute==TopBarScreen.Chat.route){
            Text(text =if(groupName!="")"Group:${groupName}" else "You have no group", textAlign = TextAlign.Center, fontFamily =lato )}
        else if(currentRoute==TopBarScreen.States.route){
            Text(text = TopBarScreen.States.label, textAlign = TextAlign.Center, fontFamily =lato)
        }},
        actions = {
            if(currentRoute==TopBarScreen.Chat.route){
                IconButton(onClick = { navController.navigate(TopBarScreen.States.route){
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true
                    restoreState = true
                } }) {
                    Icon(imageVector = Icons.Filled.Chat, contentDescription =null )
                 }
            }},
        navigationIcon = {
            if(currentRoute==TopBarScreen.States.route){
                IconButton(onClick = { navController.navigate(TopBarScreen.Chat.route){
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true
                    restoreState = true} }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription =null )
                }
            }
            if(currentRoute==TopBarScreen.Chat.route){
                IconButton(onClick = {
                    auth.signOut()
                    navController.navigate(TopBarScreen.Login.route){
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true
                    restoreState = true} }) {
                    Icon(imageVector = Icons.Filled.ExitToApp, contentDescription =null )
                }
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun SetupNavigation(auth:FirebaseAuth,database: DatabaseReference) {
    val navController :NavHostController= rememberNavController()
    NavHost(
        navController = navController,
        startDestination = if(auth.currentUser==null)TopBarScreen.Login.route else TopBarScreen.Chat.route
    ) {
        composable(route=TopBarScreen.Login.route) {
            LoginScreen(auth,onLoginClick = { navController.navigate(TopBarScreen.Chat.route){
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
                restoreState = true} })
        }
        composable(route=TopBarScreen.States.route) {
            StateScreen(auth,navController =navController,database=database)
        }
        composable(route=TopBarScreen.Chat.route) {
            ChatScreen(auth,navController =navController,database=database)
        }
    }
}