package com.grand.duke.elliot.jjabkaotalk.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PhoneAuthenticationViewModelFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(PhoneAuthenticationViewModel::class.java)) {
            return PhoneAuthenticationViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}