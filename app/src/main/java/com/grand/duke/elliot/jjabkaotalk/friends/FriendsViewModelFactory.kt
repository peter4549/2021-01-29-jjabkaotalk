package com.grand.duke.elliot.jjabkaotalk.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FriendsViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(FriendsViewModel::class.java)) {
            return FriendsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}