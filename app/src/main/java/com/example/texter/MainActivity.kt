package com.example.texter

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.texter.data.Chat
import com.example.texter.data.Message
import com.example.texter.data.User
import com.example.texter.ui.theme.TexterTheme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.initialize
import kotlinx.coroutines.launch
import java.lang.Exception
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val db = FirebaseDatabase.getInstance("https://texter-56cc1-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val userRef = db.getReference("users")
    private val msgRef = db.getReference("messages")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent {
            TexterTheme {
                AppContent()
            }
        }
    }
    private fun toLogoutActivity () {
        val loggedInIntent = Intent(this, LoginActivity::class.java)
        startActivity(loggedInIntent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AppContent() {
        if (currentUser == null) {
            toLogoutActivity()
        }

        var messageText by remember { mutableStateOf("") }
        var currentChat by remember { mutableStateOf<Chat?>(null) }
        var userList by remember { mutableStateOf<List<User?>>(emptyList()) }
        var messages by remember { mutableStateOf<List<Message>>(emptyList()) }

        fun goBack () {
            currentChat = null
        }

        fun onComplete(m: Message) {

        }
        LaunchedEffect(key1 = messages) {
            userList = emptyList()
            userRef.get().addOnSuccessListener {
                it.children.forEach {
                        userSnapshot ->
                    val userId = userSnapshot.key
                    val userData = userSnapshot.getValue(User::class.java)
                    if (userId != null) {
                        userData!!.uid = userId
                    }

                    val messageRef = msgRef.child("${currentUser!!.uid}-${userId}")
                    val query = messageRef.orderByChild("timestamp")

                    query.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val message = snapshot.children.last().getValue(Message::class.java)
                                if (message != null) {
                                    userData!!.lastMessage = message
                                    userData!!.lastMessageTimestamp = message.timestamp
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {

                        }
                    })

                        userList = (userList + userData)

                }
            }



        }

        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background)
        )  {


            // Chat List
            if (currentChat == null) {
                // Header
                TopAppBar(
                    title = {
                        Row ( verticalAlignment = Alignment.CenterVertically){
                            Image(painter = painterResource(id = R.drawable.ic_app), contentDescription = null, modifier=Modifier.height(48.dp))
                            Text(text = "Chats", fontSize = 24.sp)
                        }

                    },
                    navigationIcon = { if (currentChat != null) {
                        IconButton(onClick = { currentChat = null}) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                // Handle sign-out action
                                auth.signOut()
                                toLogoutActivity()
                            }
                        ) {
                            Icon(painter= painterResource(id = R.drawable.ic_logout), contentDescription = null)
                        }
                    }
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(userList) { user ->
                        if (user != null && user.uid != currentUser?.uid) {
                            UserItem(
                                user = user,
                                onClick = {
                                    currentChat = Chat(
                                        user1Id = currentUser!!.uid,
                                        user2Id = user.uid,
                                        user2Name = user.username,
                                        onMessagesLoaded = {
                                                loadedMessages ->
                                            messages = loadedMessages
                                        }
                                    )
                                    currentChat!!.getMessages()
                                }
                            )
                        }
                    }
                }
            } else {
                ChatScreen(chat = currentChat!!,
                    messageText = messageText,
                    onMessageTextChange = { messageText = it},
                    onSendMessage = {sendMessage(currentChat!!, messageText)},
                    messages=messages,
                    goBack = {goBack()}
                )
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun UserItem(user: User, onClick: () -> Unit) {
        val time = user.lastMessageTimestamp?.let { Instant.ofEpochMilli(it) }
        val localDateTime = LocalDateTime.ofInstant(time, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        var actual_time = localDateTime.format(formatter)
        if (user.lastMessageTimestamp?.toInt() == 0) {
            actual_time = ""
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp)
                .height(50.dp) // Set a fixed height for each user item (adjust as needed)
        ) {
            // Left side (title)
            Column(
                modifier = Modifier
                    .padding(end = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = user.username, style = MaterialTheme.typography.titleMedium)
                Row {
                    user.lastMessage?.let {
                        Text(
                            text = it.text,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(end = 16.dp)
                        )
                    }
                    Text(
                        text = actual_time,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(end = 16.dp)
                    )
                }
            }

//            Icon(painter = painterResource(id = R.drawable.ic_check_send),
//                contentDescription = null,
//                modifier = Modifier.align(
//                    Alignment.BottomEnd))
        }
        Divider()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatScreen(chat: Chat, messageText: String, onMessageTextChange: (String) -> Unit, onSendMessage: () -> Unit, messages: List<Message>, goBack: ()->Unit) {
        Scaffold(
            topBar = {
                // Header
                TopAppBar(
                    title = {
                        Text(text = chat.user2Name, fontSize = 24.sp)
                    },
                    navigationIcon = {
                        IconButton(onClick = { goBack.invoke() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")

                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                // Handle sign-out action
                                auth.signOut()
                                toLogoutActivity()
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_logout),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)

                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { onMessageTextChange(it) },
                        placeholder = { Text("Type your message...") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 1.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = { onSendMessage.invoke(); onMessageTextChange("") }
                        ),
                        trailingIcon = {
                            IconButton(onClick = { onSendMessage.invoke(); onMessageTextChange("") }) {
                                Icon(Icons.Default.Send, contentDescription = "Send")
                            }
                        }
                    )
                }
            },
            content = {
                Column(modifier = Modifier.padding(it)) {

                    val listState = rememberLazyListState()
                    val scope = rememberCoroutineScope()
                    // Messages list
                    LazyColumn(
                        modifier =
                        Modifier
                            .fillMaxWidth(),
//                            .weight(1.0f),
                        state = listState
                    ) {
                        if (messages.isEmpty()) {
                            item {
                                // Display a placeholder message when there are no messages
                                Text(
                                    text = "No messages yet. Start the conversation!",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            val msg = messages.sortedBy { it.timestamp }
                            items(msg) { message ->
                                MessageItem(message)
                            }
                            scope.launch {
                                listState.scrollToItem(index = msg.size)
                            }
                        }
                    }
                }
            }
        )
    }

        @Composable
        fun MessageItem(message: Message) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start,
            ) {
                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 48f,
                                topEnd = 48f,
                                bottomStart = if (message.isFromMe) 48f else 0f,
                                bottomEnd = if (message.isFromMe) 0f else 48f
                            )
                        )
                        .background(if (message.isFromMe) Color.LightGray else MaterialTheme.colorScheme.primary)
                        .padding(16.dp)

                ) {
                    Text(text = message.text, color = if (message.isFromMe) Color.Black else Color.White)
                }
            }

        }



        @RequiresApi(Build.VERSION_CODES.O)
        @Preview
        @Composable
        fun PreviewLoginPage () {
            TexterTheme {
                AppContent()
            }
        }
    }

fun sendMessage(chat: Chat, text: String): Boolean {
     val auth = FirebaseAuth.getInstance()
     val currentUser = auth.currentUser
     val db = FirebaseDatabase.getInstance("https://texter-56cc1-default-rtdb.asia-southeast1.firebasedatabase.app/")
     val userRef = db.getReference("users")
     val msgRef = db.getReference("messages")
    try {
        // Get a reference to the chat in the database
        val chatReference = msgRef.child(chat.chatId)

        // Create a new message
        val message = Message(currentUser?.displayName, currentUser!!.uid, text, System.currentTimeMillis())

        // Add the message to the chat in the database
        val messageReference = chatReference.push()
        messageReference.setValue(message)

        // Create timestamp and text for most recent message of two people
        chat.getMessages()
        return true
    } catch (err: Exception) {
        return false
    }
}