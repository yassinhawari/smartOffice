package com.example.smartoffice.worker


import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartoffice.SheetsService
import com.example.smartoffice.group.Worker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class workerSheet(
    val date: String,
    val worker: Worker,
    val totalHours: String
)
data class DataPoint(
    val totalHours: Float,
    val workerName:String,
    val date:String
)
data class SheetValuesResponse(
    val values: List<List<String>>?
)

class WorkerListViewModel(context: Context,database: DatabaseReference) : ViewModel( ) {

    private val workersRef: DatabaseReference = database.child("smartOffice/workers")
    private val groupsRef: DatabaseReference = database.child("smartOffice/groups")

    @SuppressLint("StaticFieldLeak")
    private val applicationContext: Context = context.applicationContext

    private val BASE_URL = "https://sheets.googleapis.com/v4/"
    private val sheetsService: SheetsService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SheetsService::class.java)


    private val _workersFlow: MutableStateFlow<List<Worker>> = MutableStateFlow(emptyList())
    val workers = _workersFlow

    private val _workerSheetsFlow: MutableStateFlow<List<workerSheet>> = MutableStateFlow(emptyList())
    val workerSheetsFlow = _workerSheetsFlow

    private val _dataPointMap = MutableStateFlow<MutableMap<String, MutableList<DataPoint>>>(mutableMapOf())
    val dataPointMap: StateFlow<MutableMap<String, MutableList<DataPoint>>> = _dataPointMap



    init {
        //workerSheetToDataPoint(_workerSheetsFlow.value)
        fetchDataFromGoogleSheet()
        viewModelScope.launch {
            val workersListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val workersList = mutableListOf<Worker>()
                    for (workerSnapshot in snapshot.children) {
                        val id = workerSnapshot.key ?: ""
                        val name = workerSnapshot.child("name").getValue(String::class.java) ?: ""
                        val email = workerSnapshot.child("email").getValue(String::class.java) ?: ""
                        val timeIn = workerSnapshot.child("timeIn").getValue(String::class.java) ?: ""
                        val timeOut = workerSnapshot.child("timeOut").getValue(String::class.java) ?: ""
                        val group = workerSnapshot.child("group").getValue(String::class.java) ?: ""
                        val worker = Worker(id, name, email, timeIn, timeOut,group)
                        workersList.add(worker)
                    }
                    _workersFlow.value = workersList
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.d("dataerror", error.toString())
                }
            }
            workersRef.addValueEventListener(workersListener)
            Log.d("workers", workers.value.toString())

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
                        val workerSheets = parseSheetRows(values)
                        _workerSheetsFlow.value = workerSheets
                        Log.d("workerSheet", _workerSheetsFlow.value.toString())
                        updateDataPointMap()
                    }
                } else {
                    Log.e("FetchDataFromSheet", "Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("FetchDataFromSheet", "Error: ${e.message}", e)
            }
        }
    }

    private fun parseSheetRows(values: List<List<String>>): List<workerSheet> {
        val dates= mutableStateOf("")
        val day= mutableStateOf("")
        return values.mapNotNull { row ->
            if (row.size >= 8) {
                if(row[0]==""){
                    day.value=dates.value
                }
                else{
                    day.value=row[0]
                    dates.value=row[0]
                }
                workerSheet(
                    date =day.value,
                    worker = Worker(
                        id = row[1],
                        name = row[2],
                        group = row[3],
                        email = "",
                        timeIn = "",
                        timeOut ="",
                    ),
                    totalHours = row[7]
                )
            } else {
                null
            }
        }
    }

    companion object {
        private const val SPREADSHEET_ID = "161IreRieWL3i_2haBL_42EAuQFj5fl8WYIpo0TPNL1I"
        private const val RANGE = "SmartOffice!A5:H"
        private const val googleApiKey = "AIzaSyCADDLl67Vd1G64JO7kAdvvU44bZOEryGE"
    }
    fun updateDataPointMap() {
        val newWorkerSheets = _workerSheetsFlow.value
        val updatedMap = transformWorkerSheetsToDataPointMap(newWorkerSheets)
        _dataPointMap.value = updatedMap
        Log.d("workerSheet", _dataPointMap.value.toString())
    }

    private fun transformWorkerSheetsToDataPointMap(workerSheets: List<workerSheet>): MutableMap<String, MutableList<DataPoint>> {
        val dataPointMap = mutableMapOf<String, MutableList<DataPoint>>()

        // Group workerSheets by worker's name
        val groupedByWorkerName = workerSheets.groupBy { it.worker.name }

        // Iterate over each worker's sheets
        groupedByWorkerName.forEach { (workerName, sheets) ->
            val dataPoints = sheets.map { sheet ->
                DataPoint(
                    totalHours = sheet.totalHours.toFloat(), // Assuming totalHours can be parsed to Float
                    workerName = sheet.worker.name,
                    date = sheet.date
                )
            }.toMutableList()
            dataPointMap[workerName] = dataPoints
        }
        return dataPointMap
    }
}

