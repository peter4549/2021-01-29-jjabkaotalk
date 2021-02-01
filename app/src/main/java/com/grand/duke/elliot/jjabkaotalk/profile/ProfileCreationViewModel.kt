package com.grand.duke.elliot.jjabkaotalk.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.grand.duke.elliot.jjabkaotalk.util.blank

class ProfileCreationViewModel: ViewModel() {
    var name = blank
    var verified = false

    var deviceProfilePhotoUri: Uri? = null
    var profilePhotoUri: String? = null
}