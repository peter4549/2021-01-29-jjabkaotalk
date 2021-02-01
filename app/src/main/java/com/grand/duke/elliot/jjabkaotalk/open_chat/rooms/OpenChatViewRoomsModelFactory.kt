package com.grand.duke.elliot.jjabkaotalk.open_chat.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class OpenChatViewRoomsModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(OpenChatRoomsViewModel::class.java)) {
            return OpenChatRoomsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}