package com.grand.duke.elliot.jjabkaotalk.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class CloudStorageHelper {

    private val firebaseUser = MainApplication.getFirebaseAuthInstance().currentUser

    fun storeProfilePhoto(deviceProfilePhotoUri: Uri, onDownloadUrl: (Uri?) -> Unit) {
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val profilePhotoFileName = "$timestamp.png"
        val storageReference = firebaseUser?.uid?.let {
            FirebaseStorage.getInstance().reference
                    .child(ProfilePhotos)
                    .child(it)
                    .child(profilePhotoFileName)
        } ?: run {
            Timber.e("firebaseUser is null.")
            return
        }

        storageReference.putFile(deviceProfilePhotoUri).continueWithTask {
            return@continueWithTask storageReference.downloadUrl
        }.addOnCompleteListener {
            if (it.isSuccessful)
                onDownloadUrl(it.result)
            else
                onDownloadUrl(null)
        }
    }

    fun deleteProfilePhoto(profilePhotoUri: Uri) {
        // TODO imp.
    }

    companion object {
        const val ProfilePhotos = "profile_photos"
    }
}