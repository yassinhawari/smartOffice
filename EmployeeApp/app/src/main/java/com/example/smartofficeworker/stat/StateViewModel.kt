package com.example.smartofficeworker.stat

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartofficeworker.chat.Worker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class workerSheet(
    val date: String,
    val totalHours: String
)
data class DataPoint(
    val totalHours: Float,
    val date:Float
)
class StateViewModel(auth:FirebaseAuth,context: Context,database: DatabaseReference): ViewModel() {
    private val _dataPointFlow: MutableStateFlow<List<DataPoint>> = MutableStateFlow(emptyList())
    val dataPointFlow = _dataPointFlow

    @SuppressLint("StaticFieldLeak")
    private val applicationContext: Context = context.applicationContext
    private val workersRef: DatabaseReference = database.child("smartOffice/workers")
    private val userEmail= mutableStateOf(auth.currentUser?.email)

    private val _currentWorker = mutableStateOf<Worker?>(null)
    val currentWorker = _currentWorker

    private val _currentMounth = mutableStateOf<String>("January")
    val currentMounth = _currentMounth

    private val BASE_URL = "https://sheets.googleapis.com/v4/"
    private val sheetsService: SheetsService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SheetsService::class.java)

    init {
        getCurrentWorker()
        fetchDataFromGoogleSheet()
    }
    private fun getCurrentWorker() {
        viewModelScope.launch {
            userEmail.value?.let { userEmail ->
                // Query the database to find the worker with the given email
                workersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            // Iterate through the results to find the matching worker
                            for (data in snapshot.children) {
                                val id=data.key?:""
                                val name=data.child("name").getValue(String::class.java)?:""
                                val email=data.child("email").getValue(String::class.java)?:""
                                val timeIn=data.child("timeIn").getValue(String::class.java)?:""
                                val timeOut=data.child("timeOut").getValue(String::class.java)?:""
                                val group=data.child("group").getValue(String::class.java)?:""
                                val worker= Worker(id, name, email, timeIn, timeOut, group)
                                // Update the _currentWorker state with the found worker
                                if(worker!=null) {
                                    _currentWorker.value = worker
                                    return
                                }
                                else{
                                    _currentWorker.value = null
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.d("current user error",error.toString())
                        }
                    }
                )
            }
        }
    }

    @SuppressLint("LogNotTimber")
    private fun fetchDataFromGoogleSheet() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = sheetsService.getSheetValues(
                    SPREADSHEET_ID,
                    RANGE,
                    googleApiKey
                ).execute()
                if (response.isSuccessful) {
                    val sheetValuesResponse = response.body()
                    val values = sheetValuesResponse?.values
                    Log.d("values", values.toString())
                    if (values != null) {
                        val workerPoint = parseSheetRows(values)
                        _dataPointFlow.value = workerPoint
                        Log.d("workerSheet", _dataPointFlow.value.toString())
                    }
                } else {
                    Log.e("FetchDataFromSheet", "Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("FetchDataFromSheet", "Error: ${e.message}", e)
            }
        }
    }

    private fun parseSheetRows(values: List<List<String>>): List<DataPoint> {
        val dates= mutableStateOf("")
        val day= mutableStateOf("")
        return values.mapNotNull { row ->
                if(row[0]==""){
                    day.value=dates.value
                }
                else{
                    day.value=row[0]
                    dates.value=row[0]
                }
                if(row[2]== currentWorker.value?.name){
                    currentMounth.value=ExtractMonthFromDate(day.value)
                    DataPoint(
                        date =day.value.substring(0, 2).toFloat(),
                        totalHours = row[7].toFloat()
                    )
                }
                else {
                null
            }
    } }
    companion object {
        private const val SPREADSHEET_ID = "161IreRieWL3i_2haBL_42EAuQFj5fl8WYIpo0TPNL1I"
        private const val RANGE = "SmartOffice!A5:H"
        private const val googleApiKey = "AIzaSyCADDLl67Vd1G64JO7kAdvvU44bZOEryGE"
    }
    fun ExtractMonthFromDate(dateString: String): String {
        // Define the format of your date string
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        // Parse the date string into a LocalDate object
        val date = LocalDate.parse(dateString, formatter)

        // Get the month from the LocalDate object
        val month = date.month

        // Return the month as a string
        return month.toString()
    }
}