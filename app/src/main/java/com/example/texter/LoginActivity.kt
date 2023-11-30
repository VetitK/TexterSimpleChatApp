package com.example.texter

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.texter.ui.theme.TexterTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class LoginActivity : ComponentActivity() {
    private val auth = FirebaseAuth.getInstance()
    val db =
        FirebaseDatabase.getInstance("https://texter-56cc1-default-rtdb.asia-southeast1.firebasedatabase.app/")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TexterTheme {
                LoginContent()
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val loggedInIntent = Intent(this, MainActivity::class.java)
            startActivity(loggedInIntent)
            finish()
        }
    }
    @Preview
    @Composable
    fun LoginContent() {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLogin by remember { mutableStateOf(true) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally

        ) {

                Image(
                    painter = painterResource(id = R.drawable.ic_message),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 2.dp)
                )
                Text ( text = "TEXTER", color = Color.Black, fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 20.dp))


            TabRow(
                selectedTabIndex = if (isLogin) 0 else 1,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (isLogin) 0 else 1])
                    )
                },

            ) {
                Tab(
                    selected = isLogin,
                    onClick = { isLogin = true }
                ) {
                    Text("Login", modifier =  Modifier.padding(vertical = 8.dp))
                }
                Tab(
                    selected = !isLogin,
                    onClick = { isLogin = false }
                ) {
                    Text("Register")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (isLogin) {
                Column(
                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.background)
                ) {


                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                // Handle login when "Done" is pressed on the keyboard
                                loginUser(email, password)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Button(
                        onClick = {
                            // Handle login button click
                            loginUser(email, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text("Login")
                    }
                }
            } else {
                RegisterContent { isLogin = true }
            }
        }
    }

    @Composable
    fun RegisterContent(onRegistrationSuccess: () -> Unit) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var username by remember { mutableStateOf("") }

        Column (modifier = Modifier
            .fillMaxSize()
//            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)){
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                visualTransformation = PasswordVisualTransformation(),
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        // Handle registration when "Done" is pressed on the keyboard
                        registerUser(email, password, onRegistrationSuccess, username)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),

            )

            Button(
                onClick = {
                    // Handle registration button click
                    registerUser(email, password, onRegistrationSuccess, username)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("Register")
            }
        }
    }

    @Preview
    @Composable
    fun PreviewLogin() {
        TexterTheme {
            LoginContent()
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login successful
                    Toast.makeText(
                        this,
                        "Login successful",
                        Toast.LENGTH_SHORT
                    ).show()
                    val loggedInIntent = Intent(this, MainActivity::class.java)
                    startActivity(loggedInIntent)
                    finish()
                } else {
                    // If login fails, display a message to the user.
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun registerUser(
        email: String,
        password: String,
        onRegistrationSuccess: () -> Unit,
        username: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful
                    Toast.makeText(
                        this,
                        "Registration successful",
                        Toast.LENGTH_SHORT
                    ).show()

                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Update username in auth first
                        val profileUpdates = userProfileChangeRequest {
                            displayName = username
                        }
                        auth.currentUser!!.updateProfile(profileUpdates)
                        val userRef = db.getReference("users").child(userId)
                        val userData = mapOf("email" to email, "username" to username)
                        userRef.setValue(userData).addOnCompleteListener { userCreationTask ->
                            if (userCreationTask.isSuccessful) {
                                onRegistrationSuccess.invoke()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Failed to add user data to Realtime Database",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                } else {
                    // If registration fails, display a message to the user.
                    Toast.makeText(
                        this,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
