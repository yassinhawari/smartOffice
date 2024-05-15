package com.example.smartofficeworker.chat

import android.app.DownloadManager
import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

data class Worker(
    val id: String,
    val name: String,
    val email: String,
    val timeIn: String,
    val timeOut: String,
    val group:String,
)
data class Message(
    val senderName: String,
    var messageText: String?,
    var messageDoc: String?,
    val date: String,
    val timeStamp:Timestamp
)
class ChatViewModel(auth:FirebaseAuth,database: DatabaseReference):ViewModel() {

    private val firestore: FirebaseFirestore = Firebase.firestore
    val storage : FirebaseStorage = Firebase.storage

    private val groupsRef: DatabaseReference = database.child("smartOffice/groups")
    private val workersRef: DatabaseReference = database.child("smartOffice/workers")
    private val userEmail= mutableStateOf(auth.currentUser?.email)

    private val _currentWorker = mutableStateOf<Worker?>(null)
    val currentWorker = _currentWorker

    private val _chatRoomMessages = MutableStateFlow<List<Message>>(emptyList())
    val chatRoomMessages: StateFlow<List<Message>> = _chatRoomMessages

    init {
        viewModelScope.launch {
            val worker = getCurrentWorker()
            _currentWorker.value = worker
            worker?.let { loadChatRoomMessages(it.group) }
        }
    }
    private suspend fun getCurrentWorker(): Worker? {
        return suspendCancellableCoroutine { continuation ->
            userEmail.value?.let { userEmail ->
                // Query the database to find the worker with the given email
                workersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            // Iterate through the results to find the matching worker
                            for (data in snapshot.children) {
                                val id = data.key ?: ""
                                val name = data.child("name").getValue(String::class.java) ?: ""
                                val email = data.child("email").getValue(String::class.java) ?: ""
                                val timeIn = data.child("timeIn").getValue(String::class.java) ?: ""
                                val timeOut = data.child("timeOut").getValue(String::class.java) ?: ""
                                val group = data.child("group").getValue(String::class.java) ?: ""

                                val worker = Worker(id, name, email, timeIn, timeOut, group)
                                continuation.resume(worker)
                                return
                            }
                            // If no worker is found, resume with null
                            continuation.resume(null)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.d("current user error", error.toString())
                            // Resume with null on cancellation or error
                            continuation.resume(null)
                        }
                    }
                )
            }
        }
    }

    private fun loadChatRoomMessages(groupName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
            firestore.collection("chatRooms").document(groupName)
                .collection("messages")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.d("messages", "Snapshot is null or empty${error}")
                        return@addSnapshotListener
                    }
                    val messages = mutableListOf<Message>()
                    if ((snapshot != null) && !snapshot.isEmpty) {
                        snapshot.documents.forEach { document ->
                            val senderName=document.getString("senderName")?:""
                            val messageText=document.getString("messageText")?:""
                            val timestamp=document.getTimestamp("timeStamp")?: Timestamp.now()
                            val date=document.getString("date")?: Timestamp.now().toDate().toString()
                            val messageDocReference = document.getString("messageDoc")
                            val message = Message(senderName, messageText, null,date, timestamp)
                            if (!messageDocReference.isNullOrBlank()) {
                                // If messageDoc reference exists, add it to the Message object
                                message.messageDoc = messageDocReference
                            }
                            messages.add(message)
                        }
                         _chatRoomMessages.value = messages
                        Log.d("messages",_chatRoomMessages.value.toString())
                } else {
                        Log.d("messages", "Snapshot is null or empty")
                        _chatRoomMessages.value= emptyList()
                    }
                }
            } catch (e: Exception) {
                Log.e("messages", "Error loading chat room messages", e)
            }
        }
    }
    fun sendMessage(groupName: String, senderName: String, messageText: String?,documentUri: Uri?) {
        viewModelScope.launch {
            try{
                val currentTimestamp = Timestamp.now()
                val timestampString = currentTimestamp.toDate().toString()
                val message = Message(senderName, null, null,timestampString,currentTimestamp)
                if(messageText!=null){
                    message.messageText=messageText
                }
                if (documentUri != null) {
                    val documentReference = uploadDocumentToStorage(documentUri,groupName)
                    message.messageDoc = documentReference
                }
                Log.d("message",message.toString())
                firestore.collection("chatRooms").document(groupName)
                    .collection("messages").document(timestampString)
                    .set(message)
                    .addOnSuccessListener {
                        Log.d("sending","succes ")
                }
                .addOnFailureListener { e ->
                Log.d("sending","error sending ,$e")
                }
            }
        catch (e: Exception) {
            Log.e(TAG, "Error loading chat room messages", e)
        }
        }
    }
    private suspend fun uploadDocumentToStorage(documentUri: Uri, groupName: String): String {
        val storageRef=storage.reference
        val currentTimestamp = Timestamp.now()
        val fileName = currentTimestamp.toDate().toString()

        // Create a reference to the document in Firebase Cloud Storage
        val documentRef = storageRef.child("chatRooms/$groupName/$fileName")

        // Upload the document to Firebase Cloud Storage
        val uploadTask = documentRef.putFile(documentUri)

        // Await until the upload task completes
        val taskSnapshot = uploadTask.await()

        // Retrieve the download URL for the uploaded document
        return documentRef.downloadUrl.await().toString()
    }
    fun downloadImage(context: Context, imageUrl: String) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadUri = Uri.parse(imageUrl)
        val request = DownloadManager.Request(downloadUri)
            .setTitle("Image Download")
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "image.jpg")
        downloadManager.enqueue(request)
        Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
    }
}

