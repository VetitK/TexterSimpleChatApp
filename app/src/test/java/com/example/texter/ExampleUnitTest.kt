package com.example.texter


import com.example.texter.data.Chat
import org.junit.Assert.assertEquals
import org.junit.Test
import com.example.texter.data.User
import org.junit.Assert.assertNotEquals


class ChatFunctionalityTest {
    private val user = User("testId1", "vetit@gmail.com", "Vetit")
    private val chat = Chat(user.uid, "testId2", {}, "Due")
    @Test
    fun testValidChatId() {
        assertEquals(chat.chatId, user.uid + "-" + chat.user2Id)
    }

    @Test
    fun testNotEqualUid() {
        assertNotEquals(user.uid, chat.user2Id)
    }

    @Test
    fun testUsernameNotEqual() {
        assertNotEquals(user.username, chat.user2Name)
    }
}
