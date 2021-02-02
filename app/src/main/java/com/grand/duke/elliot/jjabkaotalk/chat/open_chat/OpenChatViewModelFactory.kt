package com.grand.duke.elliot.jjabkaotalk.chat.open_chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grand.duke.elliot.jjabkaotalk.data.OpenChatRoom

class OpenChatViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(OpenChatViewModel::class.java)) {
            return OpenChatViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}