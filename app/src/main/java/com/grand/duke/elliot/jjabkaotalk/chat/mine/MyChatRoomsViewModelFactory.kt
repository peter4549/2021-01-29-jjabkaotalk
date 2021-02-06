package com.grand.duke.elliot.jjabkaotalk.chat.mine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grand.duke.elliot.jjabkaotalk.data.User

class MyChatRoomsViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(MyChatRoomsViewModel::class.java)) {
            return MyChatRoomsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}