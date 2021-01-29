package com.grand.duke.elliot.jjabkaotalk.authentication

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.PhoneAuthProvider
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication

class PhoneAuthenticationViewModel: ViewModel() {
    val firebaseAuth = MainApplication.getFirebaseAuthInstance()
    var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null
    var smsCode: String? = null
}