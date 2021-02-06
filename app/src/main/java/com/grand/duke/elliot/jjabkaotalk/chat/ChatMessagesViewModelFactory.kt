package com.grand.duke.elliot.jjabkaotalk.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grand.duke.elliot.jjabkaotalk.data.ChatRoom

class ChatMessagesViewModelFactory(private val chatRoom: ChatRoom): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(ChatMessagesViewModel::class.java)) {
            return ChatMessagesViewModel(chatRoom) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}