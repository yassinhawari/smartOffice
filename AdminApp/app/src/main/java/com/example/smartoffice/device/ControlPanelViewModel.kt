package com.example.smartoffice.device

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class Sensor(
    val id: String,
    val value:String,
)

data class Light(
    val id: String,
    val status: Boolean,
)

class ControlPanelViewModel (database: DatabaseReference): ViewModel() {
    private val SensorFlow: MutableStateFlow<List<Sensor>> = MutableStateFlow(emptyList())
    private val LightFlow: MutableStateFlow<List<Light>> = MutableStateFlow(emptyList())
    private val dir=database.child("smartOffice")
    val sensors = SensorFlow
    val lights = LightFlow
    private val LockDownModeFlow :MutableStateFlow<Boolean> =MutableStateFlow(false)
    val lockDownMode=LockDownModeFlow

    init {
        viewModelScope.launch {
        val SensorListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sensorsList = mutableListOf<Sensor>()
                for (sensorSnapshot in snapshot.children) {
                    val id = sensorSnapshot.key ?: ""
                    val value = sensorSnapshot.child("value").getValue(String::class.java) ?: "0"
                    val sensor = Sensor(id, value)
                    sensorsList.add(sensor)
                }
                SensorFlow.value = sensorsList
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("dataerror", error.toString())
            }
        }
        database.child("smartOffice/sensors").addValueEventListener(SensorListener)

        val LightListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val LightList = mutableListOf<Light>()
                for (lightSnapshot in snapshot.children) {
                    val id = lightSnapshot.key ?: ""
                    val status = lightSnapshot.child("status").getValue(Boolean::class.java) ?: false
                    val light = Light(id, status)
                    LightList.add(light)
                }
                LightFlow.value = LightList
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("dataerror", error.toString())
            }
        }
        database.child("smartOffice/lights").addValueEventListener(LightListener)

        val lockListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lockState = mutableStateOf(false)
                val value = snapshot.child("value").getValue(Boolean::class.java) ?: false
                lockState.value=value
                LockDownModeFlow.value = lockState.value
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("dataerror", error.toString())
            }
        }
        database.child("smartOffice/lockDown").addValueEventListener(lockListener)

    }}

    fun setLight(value:Boolean,id:String){
        dir.child("lights/${id}/status").setValue(value)

    }
    fun toggleLockdownMode(value:Boolean) {
        dir.child("lockDown/value").setValue(value)
    }
}