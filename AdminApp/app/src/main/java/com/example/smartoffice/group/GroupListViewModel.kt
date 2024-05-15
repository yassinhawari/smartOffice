package com.example.smartoffice.group

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.yml.charts.common.extensions.isNotNull
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Worker(
    val id: String,
    val name: String,
    val email: String,
    val timeIn: String,
    val timeOut: String,
    val group:String,
)
data class Group(
    val id: String,
    val name:String,
    val members: List<Worker>
)

class GroupListViewModel(database: DatabaseReference) : ViewModel( ) {
    val db = Firebase.firestore

    private val groupsRef: DatabaseReference = database.child("smartOffice/groups")
    private val workersRef: DatabaseReference = database.child("smartOffice/workers")
    private val fingerRef: DatabaseReference = database.child("smartOffice/finger")

    private val _groupsFlow:MutableStateFlow<List<Group>> = MutableStateFlow(emptyList())
    val groupsFlow = _groupsFlow

    private val _workersFlow: MutableStateFlow<List<Worker>> = MutableStateFlow(emptyList())
    val workers = _workersFlow

    private val _workersWithNoGroupFlow : MutableStateFlow<List<Worker>> = MutableStateFlow(emptyList())
    val workersWithNoGroupFlow=_workersWithNoGroupFlow

    private val _workerId = MutableStateFlow<Int?>(null)
    val workerId= _workerId

    private val _removeState= MutableStateFlow<Boolean>(false)
    val removeState= _removeState

    init {
        fetchWorkers()
        fetchGroups()
    }
    private fun fetchWorkers() {
        viewModelScope.launch {
            val workersListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val workersNoGroupList = mutableListOf<Worker>()
                    val workersList = mutableListOf<Worker>()
                    for (workerSnapshot in snapshot.children) {
                        val id = workerSnapshot.key ?: ""
                        val name = workerSnapshot.child("name").getValue(String::class.java) ?: ""
                        val email = workerSnapshot.child("email").getValue(String::class.java) ?: ""
                        val timeIn = workerSnapshot.child("timeIn").getValue(String::class.java) ?: ""
                        val timeOut = workerSnapshot.child("timeOut").getValue(String::class.java) ?: ""
                        val group=workerSnapshot.child("group").getValue(String::class.java) ?: ""
                        val worker = Worker(id, name, email, timeIn, timeOut,group)
                        if(worker.group==""){
                            workersNoGroupList.add(worker)
                            Log.d("workerwithnogoupe", workersNoGroupList.toString())
                        }
                        workersList.add(worker)
                    }
                    _workersFlow.value = workersList
                    Log.d("workeristflow",_workersFlow.value.toString())
                    _workersWithNoGroupFlow.value=workersNoGroupList
                    Log.d("workerwithnogoupeflow", _workersWithNoGroupFlow.value.toString())
                    fetchGroups()
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.d("workers error", error.toString())
                }
            }
            workersRef.addValueEventListener(workersListener)
        }
    }
    private fun fetchGroups() {
        viewModelScope.launch() {
            groupsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val groups = mutableListOf<Group>()
                    snapshot.children.forEach { groupSnapshot ->
                        val groupId = groupSnapshot.key
                        val groupName = groupSnapshot.child("name").getValue(String::class.java)
                        var workersInThisGroup=mutableListOf<Worker>()
                        if(groupSnapshot.hasChild("members")){
                            groupSnapshot.child("members").children.forEach { memberSnapshot ->
                                val memberId = memberSnapshot.key
                                if (memberId != null) {
                                    workersInThisGroup = (workersInThisGroup+_workersFlow.value.filter { it.id == memberId  }.toMutableList()).toMutableList()
                                }
                            }
                            val group = Group(groupId.orEmpty(), groupName.orEmpty(),workersInThisGroup)
                            groups.add(group)
                        }
                        else {
                            val group = Group(groupId.orEmpty(), groupName.orEmpty(), emptyList())
                            groups.add(group)
                        }
                        Log.d("groupe",groups.toString())
                    }
                    _groupsFlow.value = groups
                    Log.d("groupe flow",_groupsFlow.value.toString())
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }
    fun deleteWorkerFromGroup(workerId: String, groupId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val groupMembersRef = groupsRef.child(groupId).child("members")
                groupMembersRef.child(workerId).removeValue().addOnCompleteListener {
                    if(it.isSuccessful){
                        workersRef.child(workerId).child("group").setValue("")
                    }
                }
            } catch (e: Exception) {
                Log.e("DeletWorkerFromGroup", "Error: ${e.message}", e)
            }
            fetchGroups()
        }
        }

    @SuppressLint("QueryPermissionsNeeded")
    fun sendEmail(subject: String, message: String, recipientEmail: String, context: Context) {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "plain/text"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
        }

        if (emailIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(emailIntent)
        } else {
            // Handle the case where no activity can handle the email intent
            Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show()
        }
    }

    fun addWorkerToGroup(workerId: String, groupId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Reference to the "members" node under the specified group
                val groupMembersRef = groupsRef.child(groupId).child("members")

                // Set the worker as a member of the group
                groupMembersRef.child(workerId).setValue(true).await()

                // Retrieve the name of the group
                val groupNameSnapshot = groupsRef.child(groupId).child("name").get().await()
                val groupName = groupNameSnapshot.value as? String ?: ""
                Log.d("groupeName", groupName)

                // Set the group name for the worker
                workersRef.child(workerId).child("group").setValue(groupName).await()
            } catch (e: Exception) {
                Log.e("AddWorkerToGroup", "Error: ${e.message}", e)
                // Handle error
            }
        }
        fetchGroups()
    }

    fun deleteGroup(groupId: String,groupName:String) {
        viewModelScope.launch() {
            val thisGroup=groupsRef.child(groupId)
            thisGroup.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.hasChild("members")){
                        for (snapshot in dataSnapshot.child("members").children) {
                            snapshot.key?.let { workersRef.child(it).child("group").setValue("") }
                        }
                    }
                    thisGroup.removeValue()
                    db.collection("chatRooms").document(groupName).delete()
                    }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("deleting group", "error deleting group$databaseError")
                }
            }
            )
            fetchGroups()
        }
    }
    fun addGroup(groupName: String){
        viewModelScope.launch() {
            val groupId = "group" + (_groupsFlow.value.size + 1).toString()
            groupsRef.child(groupId).child("name").setValue(groupName)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("group_adding","groupe added succesfully")
                    } else {
                        Log.e("group_adding", "Failed to add group: ${task.exception}")
                        // Handle the error case if needed
                    }
                }
        }
        fetchGroups()
    }

    fun deleteWorker(workerId:String,groupId:String){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val workerRef = workersRef.child(workerId)
                val groupMembersRef = groupsRef.child(groupId).child("members")
                groupMembersRef.child(workerId).removeValue().addOnCompleteListener {
                    if(it.isSuccessful){
                        workerRef.removeValue()
                    }
                }
            } catch (e: Exception) {
                Log.e("DeletWorkerFromGroup", "Error: ${e.message}", e)
            }
            fetchGroups()
        }
    }
    fun addWorker(worker: Worker,password:String,auth: FirebaseAuth){
        viewModelScope.launch(Dispatchers.IO) {
            auth.createUserWithEmailAndPassword(worker.email,password).addOnCompleteListener { it ->
                if(it.isSuccessful){
                    Log.d("adding auth","adding success")
                    workersRef.child(worker.id).setValue(worker).addOnCompleteListener {
                        if(it.isSuccessful){
                            Log.d("adding RTDB","adding success")
                        }
                        else{
                            Log.d("adding RTDB","adding failed",it.exception)
                        }
                    }
                }
                else{
                    Log.d("adding auth","adding failed",it.exception)
                }
            }
            fetchGroups()
        }
    }
    fun editWorker(workerId:String,worker: Worker){
        viewModelScope.launch(Dispatchers.IO) {
            workersRef.child(workerId).setValue(worker).await()
        }
        fetchGroups()
    }

    fun setWorkerNameAdd(workerName:String){
        viewModelScope.launch(Dispatchers.IO){
            fingerRef.child("add").child("name").setValue(workerName).await()
            getWorkerId()
        }
    }

    fun setWorkerIdRemove(workerId:String){
        val workerId=workerId.substring(6,8).toInt()
        viewModelScope.launch(Dispatchers.IO){
            fingerRef.child("remove").child("id").setValue(workerId).await()
            getRemoveState()
        }
    }
    private fun getWorkerId(){
        viewModelScope.launch(Dispatchers.IO){
            fingerRef.child("add").child("id").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val value = dataSnapshot.getValue(Int::class.java)
                    if(value!=0){
                        _workerId.value = value
                        deleteValuesAfterAdding()
                    }

                }
                override fun onCancelled(error: DatabaseError) {
                    _workerId.value = null
                }
            }
            )
        }
    }

    private fun getRemoveState(){
        viewModelScope.launch(Dispatchers.IO){
            fingerRef.child("remove").child("ok").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val value = dataSnapshot.getValue(Boolean::class.java)
                    if(value == true){
                        _removeState.value=true
                        deleteValuesAfterRemove()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    _removeState.value = false
                }
            }
            )
        }
    }
    fun deleteValuesAfterAdding(){
        viewModelScope.launch(Dispatchers.IO){
            fingerRef.child("add").child("name").setValue("").await()
            fingerRef.child("add").child("id").setValue(0).await()
        }
    }
    fun deleteValuesAfterRemove(){
        viewModelScope.launch(Dispatchers.IO){
            fingerRef.child("remove").child("id").setValue("").await()
            fingerRef.child("remove").child("ok").setValue(false).await()
        }
    }
}

