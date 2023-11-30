package com.example.texter.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class User(
    var uid: String = "",
    val email: String = "",
    val username: String = "",
    var lastMessage: Message? = null,
    var lastMessageTimestamp: Long? = 0
)

data class Chat(
    val user1Id: String = "",
    val user2Id: String = "",
    val onMessagesLoaded: (List<Message>) -> Unit,
    val user2Name: String
) {
    val chatId: String
        get() = "$user1Id-$user2Id"

    fun getMessages() {
        val db = FirebaseDatabase.getInstance("https://texter-56cc1-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val query = db.getReference("messages").child(chatId)
        val query2 = db.getReference("messages").child("$user2Id-$user1Id")

        query.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                query2.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val messages2 = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                        onMessagesLoaded(messages + messages2)
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }
                })

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}

data class Message(
    val sender: String? = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0
)  {
    private val auth = FirebaseAuth.getInstance()
    val isFromMe: Boolean
        get() = senderId == auth.currentUser?.uid
}
