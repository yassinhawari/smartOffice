package com.example.smartoffice.group

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import co.yml.charts.common.extensions.isNotNull
import com.example.smartoffice.AppBottomBar
import com.example.smartoffice.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

@Composable
fun GroupListScreen(
    auth: FirebaseAuth,
    database:DatabaseReference,
    navController : NavHostController,
    viewModel: GroupListViewModel = remember { GroupListViewModel(database) }
) {
    var(showAdd,setshowAdd) = remember { mutableStateOf(false) }

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
                        "Group list",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = lato,
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(0.dp, 0.dp,15.dp, 15.dp)))
        },
        bottomBar = {
        AppBottomBar(
            navController) },
        floatingActionButton={
            FloatingActionButton(onClick = { setshowAdd(true) }) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription =null )
            } }) {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextBetweenLines("Select a group to view")
            Spacer(modifier = Modifier.height(8.dp))
            GroupScreen(auth,viewModel)
        }
            if(showAdd){
                addWorker(auth = auth, viewModel =viewModel ) {
                    setshowAdd(false)
                }
            }
    }

}

}
@Composable
fun TextBetweenLines(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(color = MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(color = MaterialTheme.colorScheme.primary)
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(auth: FirebaseAuth,viewModel: GroupListViewModel) {
    val groups by viewModel.groupsFlow.collectAsState()
    var showDrop by remember { mutableStateOf(false) }
    var showAddGroupe by remember { mutableStateOf(false) }
    var showAddWorker by remember { mutableStateOf(false) }
    var selectedGroupIndex by remember { mutableIntStateOf(0) }

    val selectedGroup = groups.getOrNull(selectedGroupIndex)

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,) {
        Row() {
            ExposedDropdownMenuBox(
                expanded = showDrop,
                onExpandedChange = { showDrop = !showDrop })
            {
                TextField(
                    value = selectedGroup?.name ?: "Select a groupe",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDrop) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.menuAnchor())
                ExposedDropdownMenu(expanded = showDrop, onDismissRequest = { showDrop = false })
                {
                    groups.forEachIndexed { index, group ->
                        DropdownMenuItem(
                            text = { Text(text = group.name) },
                            onClick = { selectedGroupIndex = index
                                        showDrop = false })
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { showAddGroupe = true }) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
            }
        }
        if (showAddGroupe) {
            AddGroupDialog(viewModel) { showAddGroupe = false }
        }

        Spacer(modifier = Modifier.height(16.dp))

        selectedGroup?.let { group ->
            Log.d("thisGroupe",group.toString())
            Spacer(modifier = Modifier.height(8.dp))
            TextBetweenLines("Members list")
            Spacer(modifier = Modifier.height(8.dp))
            ElevatedCard(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ),
                modifier = Modifier
                    .padding(16.dp)
            ) {
            LazyColumn {
                items(group.members) { worker ->
                    WorkerItem(auth,worker = worker,viewModel,selectedGroup.name,selectedGroup.id)
                }
            } }
                ElevatedCard(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 12.dp
                    ),
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.Center) {
                        IconButton(onClick = { showAddWorker = true }) {
                            Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                        }
                        IconButton(onClick = { viewModel.deleteGroup(selectedGroup.id,selectedGroup.name) }) {
                            Icon(imageVector = Icons.Rounded.Delete, contentDescription = null)
                        }
                    }
                }
        }
        if (showAddWorker) {
            if (selectedGroup != null) {
                AddWorkerDialog(
                    onDismiss = { showAddWorker = false },
                    viewModel = viewModel,
                    groupId = selectedGroup.id
                )
            }
        }
    }
}
@SuppressLint("UnrememberedMutableState")
@Composable
fun WorkerItem(auth: FirebaseAuth,worker: Worker, viewModel: GroupListViewModel,groupName:String, selectedGroupId:String) {

    val (showOption, setshowOption) = remember { mutableStateOf(false) }
    val (showEdit,setshowEdit)= remember { mutableStateOf(false) }
    val (showRemove,setshowRemove)= remember { mutableStateOf(false) }
    val (showDelete,setshowDelete)= remember { mutableStateOf(false) }
    val (showEmail,setshowEmail)= remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Person, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(worker.name, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.weight(1f))
        Box(){
            IconButton(onClick = { setshowOption(true)}) {
                Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = null)
            }
            DropdownMenu(expanded = showOption, onDismissRequest = { setshowOption(false) },modifier=Modifier.clip(RoundedCornerShape(8.dp)
            )) {
                DropdownMenuItem(text = { Text("Send E-mail") },
                    onClick = {
                        setshowOption(false)
                        setshowEmail(true) },
                    trailingIcon={
                        Icon(Icons.Rounded.Send,contentDescription = null)}
                )
                DropdownMenuItem(
                    text = { Text(text ="Edit Worker" ) },
                    onClick = { setshowEdit(true)
                        setshowOption(false)},
                    trailingIcon={
                       Icon(Icons.Rounded.Edit,contentDescription = null)})
                DropdownMenuItem(
                    text = { Text(text ="Remove worker from group" ) },
                    onClick = {
                        setshowRemove(true)
                        setshowOption(false) },
                    trailingIcon={   Icon(Icons.Rounded.Remove,contentDescription = null)})
                DropdownMenuItem(
                    text = { Text(text ="Delete worker" ) },
                    onClick = {
                        setshowDelete(true)
                        setshowOption(false) },
                    trailingIcon={   Icon(Icons.Rounded.Delete,contentDescription = null)})
            }
        }
    }
    Divider(modifier=Modifier.fillMaxWidth(),color= Color.Black)
    if(showEmail){
        sendEmail(
            viewModel = viewModel,
            worker = worker
        ){
            setshowEmail(false)
        }
    }
    if (showEdit) {
        editWorker(viewModel, worker) { setshowEdit(false) }
    }
    if(showRemove){
        RemoveWorkerFromGroup(viewModel,worker, groupName,selectedGroupId){
            setshowRemove(false)
        }
    }
    if(showDelete){
        deleteWorker(worker,viewModel,selectedGroupId){
            setshowDelete(false)
        }
    }
}

@Composable
fun AddGroupDialog(
    viewModel: GroupListViewModel,
    onDismiss: () -> Unit
) {
    var groupName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Group") },
        text = {
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name") }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (groupName.isNotBlank()) {
                        viewModel.addGroup(groupName)
                        onDismiss()
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun AddWorkerDialog(
    viewModel: GroupListViewModel,
    groupId: String,
    onDismiss: () -> Unit
) {
    val workerWithNoGroup by viewModel.workersWithNoGroupFlow.collectAsState()

    var selectedWorker by mutableStateOf(Worker("","Select a worker","","","",""))
    var showDropworker by mutableStateOf(false)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Worker to Group") },
        text = {
            Column {
                Text("Select Worker:")
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = showDropworker,
                    onExpandedChange = { showDropworker= !showDropworker })
                {
                    TextField(
                        value = selectedWorker?.name ?: "Select a groupe",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropworker) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier.menuAnchor())
                    ExposedDropdownMenu(expanded = showDropworker, onDismissRequest = { showDropworker = false })
                    {
                        workerWithNoGroup.forEach { worker ->
                            DropdownMenuItem(
                                text = { Text(text=worker.name) },
                                onClick = {
                                    showDropworker=false
                                    selectedWorker = worker
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { IconButton(onClick = {
            viewModel.addWorkerToGroup(selectedWorker.id, groupId)
            onDismiss() },enabled=selectedWorker.id!="")
        { Icon(imageVector = Icons.Rounded.Check, contentDescription = null) } },
        dismissButton = {
            IconButton(onClick = {  onDismiss() }) {
                Icon(imageVector = Icons.Rounded.Cancel, contentDescription =null )
            }
        }
    )
}
@Composable
fun addWorker(auth: FirebaseAuth, viewModel: GroupListViewModel, onDismiss: () -> Unit){
    val name= remember { mutableStateOf("") }
    val email= remember { mutableStateOf("") }
    val password= remember { mutableStateOf("") }
    val workerId by viewModel.groupsFlow.collectAsState()
    var (showFinger,setshowFinger)= remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Add worker") },
        text = {
            Column() {
                OutlinedTextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Name") }
                )
                OutlinedTextField(
                    value = email.value,
                    onValueChange = { email.value= it },
                    label = { Text("E-mail") }
                )
                OutlinedTextField(
                    value = password.value,
                    onValueChange = { password.value = it },
                    label = { Text("Password") }
                )
            }
        },
        confirmButton = {
            IconButton(
                onClick = {
                    if (name.value.isNotBlank()&&email.value.isNotBlank()&&password.value.isNotBlank()) {
                        viewModel.setWorkerNameAdd(name.value)
                        setshowFinger(true)
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription =null )
            }
        },
        dismissButton = {
            IconButton(
                onClick = { onDismiss() }
            ) {
                Icon(imageVector = Icons.Default.Cancel, contentDescription =null )
            }
        }
    )
    if(showFinger){
        getFingerScreen(auth,name.value,email.value,password.value,viewModel){
            setshowFinger(false)
            onDismiss()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun sendEmail(
    viewModel: GroupListViewModel, worker: Worker, onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val (subject, setSubject) = remember { mutableStateOf("") }
    val (message, setMessage) = remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = { onDismiss() }) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row() {
                Text("to :${worker.name}")
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    viewModel.sendEmail(
                        recipientEmail = worker.email,
                        subject = subject,
                        message = message,
                        context = context
                    )
                    onDismiss()
                })
                {
                    Icon(imageVector = Icons.Filled.Send, contentDescription = "")
                }

            }
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = subject,
                onValueChange = { setSubject(it) },
                label = { Text("Subject") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                maxLines = 20,
                value = message,
                onValueChange = { setMessage(it) },
                label = { Text("Message") }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
@Composable
fun editWorker(viewModel: GroupListViewModel, worker: Worker, onDismiss: () -> Unit){
    val id= remember { mutableStateOf(worker.id) }
    val name= remember { mutableStateOf(worker.name) }
    val email= remember { mutableStateOf(worker.email) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit worker") },
        text = {
            Column() {
                OutlinedTextField(
                    value = id.value,
                    onValueChange = { id.value = it },
                    label = { Text("Worker Id") }
                )
                OutlinedTextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Worker Name") }
                )
                OutlinedTextField(
                    value = email.value,
                    onValueChange = { email.value= it },
                    label = { Text("Worker E-mail") }
                )
            }

        },
        confirmButton = {
            IconButton(
                onClick = {
                    if (name.value.isNotBlank()&&email.value.isNotBlank()) {
                        viewModel.editWorker(workerId = worker.id,
                            Worker(id=id.value,name=name.value,email=email.value, group = "", timeIn = "", timeOut = "")
                        )
                        onDismiss()
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription =null )
            }
        },
        icon={},
        dismissButton = {
            IconButton(
            onClick = { onDismiss() }
        ) {
            Icon(imageVector = Icons.Default.Cancel, contentDescription =null )
        }}
    )
}

@Composable
fun RemoveWorkerFromGroup(viewModel: GroupListViewModel, worker: Worker,groupName:String,groupId:String, onDismiss: () -> Unit){
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmation") },
        text = {
            Text(text = "Remove ${worker.name} from ${groupName}?")
        },
        confirmButton = {
            IconButton(
                onClick = {
                    viewModel.deleteWorkerFromGroup(workerId = worker.id, groupId =groupId )
                }
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription =null )
            }
        },
        dismissButton = {
            IconButton(
                onClick = { onDismiss() }
            ) {
                Icon(imageVector = Icons.Default.Cancel, contentDescription =null )
            }
        }
    )
}

@Composable
fun deleteWorker(worker: Worker,viewModel: GroupListViewModel, selectedGroupId:String, onDismiss: () -> Unit){
    var (showFinger,setshowFinger)= remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmation") },
        text = {
            Text(text = "Delete ${worker.name}?")
        },
        confirmButton = {
            IconButton(
                onClick = {
                    viewModel.setWorkerIdRemove(worker.id)
                    setshowFinger(true)
                }
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription =null )
            }
        },
        dismissButton = {
            IconButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Icon(imageVector = Icons.Default.Cancel, contentDescription =null )
            }
        }
    )
    if(showFinger){
        removeFingerScreen(worker,selectedGroupId,viewModel){
            setshowFinger(false)
            onDismiss()
        }
    }
}

@Composable
fun removeFingerScreen(worker:Worker,groupeId:String,viewModel: GroupListViewModel,onDismiss: () -> Unit){

    val removeState by viewModel.removeState.collectAsState()

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {},
        text = {
            if(removeState){
                Row(){
                    Text("Successfully removed worker fingerprint")
                }
            }
            else{
                Column() {
                    Text(text = "Waiting for deleting the fingerPrint!", textAlign = TextAlign.Center,)
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator()
                }
            }
        },
        confirmButton = {
            IconButton(
                enabled=(removeState),
                onClick = {
                    viewModel.deleteWorker(worker.id,groupeId)
                    onDismiss()
                }
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription =null )
            }},
        dismissButton = {
            IconButton(
                onClick = { onDismiss() }
            ) {
                Icon(imageVector = Icons.Default.Cancel, contentDescription =null )
            }
        }
    )
}

@Composable
fun getFingerScreen(auth: FirebaseAuth,name:String,email:String,password:String,viewModel: GroupListViewModel,onDismiss: () -> Unit){

    val workerId by viewModel.workerId.collectAsState()
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {},
        text = {
            if(workerId!=0 &&workerId.isNotNull()){
                Row(){
                    Text("Successfully added worker fingerprint:${workerId}")
                }
            }
            else{
                Column() {
                    Text(text = "Waiting for a fingerprint!", textAlign = TextAlign.Center,)
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator()
                }
            }
        },
        confirmButton = {
            IconButton(
                enabled=(workerId!=0 &&workerId.isNotNull()),
                onClick = {
                        viewModel.addWorker(Worker(id="worker${workerId}",name=name,email=email, group = "", timeIn = "", timeOut = ""),password,auth)
                        onDismiss()
                    }
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription =null )
        }},
        dismissButton = {
            IconButton(
                onClick = { onDismiss() }
            ) {
                Icon(imageVector = Icons.Default.Cancel, contentDescription =null )
            }
        }
    )
}

