package com.grand.duke.elliot.jjabkaotalk.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ProfileCreationViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(ProfileCreationViewModel::class.java)) {
            return ProfileCreationViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}