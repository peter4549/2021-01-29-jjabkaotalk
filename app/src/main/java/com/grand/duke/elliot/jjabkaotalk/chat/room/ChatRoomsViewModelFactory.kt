package com.grand.duke.elliot.jjabkaotalk.chat.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ChatRoomsViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(ChatRoomsViewModel::class.java)) {
            return ChatRoomsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}