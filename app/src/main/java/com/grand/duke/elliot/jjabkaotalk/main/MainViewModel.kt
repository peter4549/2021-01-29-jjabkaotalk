package com.grand.duke.elliot.jjabkaotalk.main

import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.firebase.FireStoreHelper
import org.json.JSONObject
import timber.log.Timber
import java.lang.NullPointerException

class MainViewModel: ViewModel() {
    val fireStoreHelper = FireStoreHelper()
}