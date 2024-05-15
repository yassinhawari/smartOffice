package com.example.smartoffice.setting

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class SettingsViewModel(database: DatabaseReference):ViewModel() {

    val enableNotifications: MutableState<Boolean> = mutableStateOf(false)
    val darkMode: MutableState<Boolean> = mutableStateOf(false)
    val soundEffects: MutableState<Boolean> = mutableStateOf(false)
    val userName: MutableState<String> = mutableStateOf("")
    val email: MutableState<String> = mutableStateOf("")
    val selectedTheme: MutableState<String> = mutableStateOf("")
    val fontSize: MutableState<Int> = mutableIntStateOf(16)

    init {
        // Initialize properties from database or other sources if needed
        // Example:
        // enableNotifications = database.child("settings/enableNotifications").getValue(Boolean::class.java) ?: true
        // ...
    }

    // Method to save settings
    fun saveSettings() {
        // Implement logic to save settings to the database or other storage
        // Example:
        // database.child("settings/enableNotifications").setValue(enableNotifications)
        // ...
    }

    // Method to reset settings to default values
    fun resetToDefault() {
        // Implement logic to reset settings to default values
        // Example:
        // enableNotifications = true
        // darkMode = false
        // ...
    }
    fun signOut(auth:FirebaseAuth){
        auth.signOut()
    }
}