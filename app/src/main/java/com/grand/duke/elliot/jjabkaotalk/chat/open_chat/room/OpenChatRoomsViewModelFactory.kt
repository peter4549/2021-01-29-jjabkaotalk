package com.grand.duke.elliot.jjabkaotalk.chat.open_chat.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class OpenChatRoomsViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(OpenChatRoomsViewModel::class.java)) {
            return OpenChatRoomsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}