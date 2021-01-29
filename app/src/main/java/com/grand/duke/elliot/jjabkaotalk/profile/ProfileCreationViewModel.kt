package com.grand.duke.elliot.jjabkaotalk.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.firebase.FireStoreHelper
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import com.grand.duke.elliot.jjabkaotalk.util.blank

class ProfileCreationViewModel: ViewModel() {
    private val firebaseAuth = MainApplication.getFirebaseAuthInstance()
    private val fireStoreHelper = FireStoreHelper()

    var name = blank
    var verified = false

    var deviceProfilePhotoUri: Uri? = null
    var profilePhotoUri: String? = null

    fun setUser(user: User) {
        fireStoreHelper.setUser(user)
    }

    fun uid() = firebaseAuth.uid
}