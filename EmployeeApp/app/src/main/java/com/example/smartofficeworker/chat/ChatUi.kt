package com.example.smartofficeworker.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Dimension
import coil.size.Size
import com.example.smartofficeworker.AppTopBar
import com.example.smartofficeworker.R
import com.example.smartofficeworker.ui.theme.PurpleGrey80
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ChatScreen(
    auth:FirebaseAuth,
    database: DatabaseReference,
    navController : NavHostController,
    viewModel: ChatViewModel = remember { ChatViewModel(auth,database) }
) {
    val chatRoomMessages by viewModel.chatRoomMessages.collectAsState()
    val currentWorker = viewModel.currentWorker.value

    Scaffold(topBar = {
        if (currentWorker != null) {
            AppTopBar(
                navController,currentWorker.group,auth
            )
        }
    }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
        ) {
            Image(
                colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
                painter = painterResource(id = R.drawable.backg),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.2f))
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment= Alignment.BottomEnd
            ) {
                        if (chatRoomMessages.isNotEmpty()){
                            ChatMessages(messages = chatRoomMessages,viewModel)
                        }
                else{
                    Text(text = "There is no messages in this chat")
                }

                        if (currentWorker != null) {
                            MessageInput(viewModel = viewModel,currentWorker)
                        }
                    }
            }
        }
}
@Composable
fun ChatMessages(messages: List<Message>,viewModel: ChatViewModel) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 75.dp)
    ) {
        items(messages) { message ->
            MessageItem(
                message = message,
                viewModel =viewModel )
        }
    }
    LaunchedEffect(messages) {
        listState.scrollToItem(messages.size - 1)
    }
}

@Composable
fun MessageItem(viewModel: ChatViewModel, message: Message) {
    val isCurrentUser = message.senderName == viewModel.currentWorker.value?.name
    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start
    val sender = if (isCurrentUser) "Me" else message.senderName

    val context = LocalContext.current
    val (showFull, setShowFull) = remember { mutableStateOf(false) }
    val (showDate, setShowDate) = remember { mutableStateOf(false) }

    Column(horizontalAlignment = alignment, verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
            .padding(bottom= 10.dp,top=2.dp)) {
        Text(
            text = sender,
            style = MaterialTheme.typography.labelMedium
        )
        Column(modifier=
            Modifier.padding(top=2.dp,end=if(isCurrentUser) 8.dp else 0.dp, start = if(!isCurrentUser) 8.dp else 0.dp),
            horizontalAlignment=alignment)
        {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart =  if (isCurrentUser) 48f else 0f,
                            topEnd = if (isCurrentUser) 0f else 48f,
                            bottomStart =48f,
                            bottomEnd = 48f
                        )
                    )
                    .background(if(isCurrentUser) PurpleGrey80 else Color.LightGray)
                    .padding(12.dp)
                    .clickable { setShowDate(!showDate) }
            ) {
                if(message.messageText!=""){
                    Text(
                        modifier = Modifier
                            .wrapContentSize() // Wrap content width
                            .padding(end = 16.dp), // Add padding to the end
                        text = "${message.messageText}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { setShowFull(true) }
            ) {
                message.messageDoc?.let {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End.takeIf { isCurrentUser }
                            ?: Arrangement.Start
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(it)
                                .crossfade(true)
                                .size(Dimension(400), Dimension(400))
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(160.dp)
                                .aspectRatio(1f)
                                .clip(
                                    RoundedCornerShape(4.dp)
                                )
                        ) {
                            val state = painter.state
                            if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                                CircularProgressIndicator()
                            } else {
                                SubcomposeAsyncImageContent()
                            }
                        }
                    }
                }
            }
        }

        if (showDate) {
            Text(
                text = message.date,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }

    if (showFull) {
        message.messageDoc?.let {
            FullScreenImageView(viewModel = viewModel, context = context, it) {
                setShowFull(false)
            }
        }
    }
}
@Composable
fun FullScreenImageView(
    viewModel: ChatViewModel,
    context: Context,
    documentRef:String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
            .background(Color.Transparent),containerColor=Color.Transparent,onDismissRequest = { onDismiss() }, confirmButton = { /*TODO*/ },text={
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(4.dp),
            //contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(documentRef)
                    .crossfade(true)
                    .size(Size.ORIGINAL)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            ) {
                val state = painter.state
                if (state is AsyncImagePainter.State.Loading || state is AsyncImagePainter.State.Error) {
                    CircularProgressIndicator()
                } else {
                    SubcomposeAsyncImageContent()
                }
            }
            // Close button
            IconButton(
                onClick = {
                    onDismiss()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = {
                    viewModel.downloadImage(context,documentRef)
                    onDismiss()
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    })
}
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MessageInput(viewModel: ChatViewModel, currentWorker: Worker) {
    var selectedDocumentUri by remember { mutableStateOf<Uri?>(null) }
    var messageText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val (showFile, setshowFile) = remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val selectedImageUri: Uri? = data?.data
            selectedDocumentUri = selectedImageUri
        }
    }

    Column {
        AnimatedVisibility(visible = showFile, enter = fadeIn(), exit = fadeOut()) {
            showAttachmentOptions(context, imagePickerLauncher){setshowFile(false)}
            }
            selectedDocumentUri?.let { uri ->
                Box (contentAlignment = Alignment.TopCenter){
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
                  )
                    IconButton(onClick = { selectedDocumentUri=null }) {
                        Icon(imageVector = Icons.Default.Cancel, contentDescription =null )
                    }
                }
        }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            leadingIcon={  IconButton(onClick = {
                setshowFile(!showFile)
            }) {
                Icon(Icons.Filled.AttachFile, contentDescription = "Attach File")
            }},
            value = messageText,
            onValueChange = { messageText =it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message...") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (selectedDocumentUri!=null || messageText.isNotBlank() ) {
                    viewModel.sendMessage(currentWorker.group, currentWorker.name, messageText,selectedDocumentUri)
                    selectedDocumentUri = null
                    messageText =""
                    keyboardController?.hide()
                }
            }
            ),
            trailingIcon={  IconButton(onClick = {
                if (selectedDocumentUri!=null || messageText.isNotBlank() ) {
                viewModel.sendMessage(currentWorker.group, currentWorker.name, messageText,selectedDocumentUri)
                selectedDocumentUri = null
                messageText =""
                 keyboardController?.hide()
            }}) {
                Icon(Icons.Filled.Send, contentDescription = "Attach File")
            }}
        )
    }
    }
}
@RequiresApi(Build.VERSION_CODES.TIRAMISU)

@Composable
fun showAttachmentOptions(
    context: Context,
    imagePickerLauncher: ActivityResultLauncher<Intent>,
    onDismiss: () -> Unit,
) {
    val requestPermissionFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            val chooseIntent = Intent.createChooser(intent, "Select File")
            imagePickerLauncher.launch(chooseIntent)
            onDismiss()
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }


    val requestPermissionCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            imagePickerLauncher.launch(intent)
            onDismiss()
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    val requestPermissionImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            val chooseIntent = Intent.createChooser(intent, "Select Image")
            imagePickerLauncher.launch(chooseIntent)
            onDismiss()
        } else {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
    ElevatedCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .size(width = 160.dp, height = 80.dp)
            .padding(start = 8.dp)    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
        IconButton(onClick = {
            requestPermissionCameraLauncher.launch(android.Manifest.permission.CAMERA) }) {
                Icon(imageVector =Icons.Rounded.CameraAlt, contentDescription = null)
        }
        IconButton(onClick = { requestPermissionImageLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES) }) {

                Icon(imageVector = Icons.Rounded.PhotoLibrary, contentDescription = null)

        }
    }
    }
}

