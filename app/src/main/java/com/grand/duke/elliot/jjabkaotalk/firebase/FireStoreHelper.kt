package com.grand.duke.elliot.jjabkaotalk.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import org.json.JSONObject
import timber.log.Timber
import kotlin.NullPointerException

class FireStoreHelper {

    private val firebaseUser = MainApplication.getFirebaseAuthInstance().currentUser
    @Suppress("SpellCheckingInspection")
    private val gson = Gson()
    private val userCollectionReference = FirebaseFirestore.getInstance().collection(Collection.Users)
    private var onUserDocumentSnapshotListener: OnUserDocumentSnapshotListener? = null

    fun setOnDocumentSnapshotListener(onUserDocumentSnapshotListener: OnUserDocumentSnapshotListener) {
        this.onUserDocumentSnapshotListener = onUserDocumentSnapshotListener
    }

    interface OnUserDocumentSnapshotListener {
        fun onUserDocumentSnapshot(user: User)
        fun onException(exception: Exception)
        fun onNoUserDocumentSnapshot()
    }

    fun setupUserSnapshotListener(uid: String) {
        val documentReference = userCollectionReference.document(uid)
        documentReference.addSnapshotListener { documentSnapshot, fireStoreException ->
            fireStoreException?.let {
                onUserDocumentSnapshotListener?.onException(it)
            } ?: run {
                documentSnapshot?.let { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        documentSnapshot.data?.let {
                            val user =  gson.fromJson(JSONObject(it).toString(), User::class.java)
                            onUserDocumentSnapshotListener?.onUserDocumentSnapshot(user)
                        }
                    } else {
                        /** Request to fill out a profile. */
                        onUserDocumentSnapshotListener?.onNoUserDocumentSnapshot()
                    }
                } ?: run {
                    onUserDocumentSnapshotListener?.onException(NullPointerException("documentSnapshot is null."))
                }
            }
        }
    }

    fun setUser(user: User) {
        firebaseUser?.uid?.let {
            val documentReference = userCollectionReference.document(user.uid)
            documentReference.set(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("User set.")
                    // 유저 정보를 생성함. TODO check,, 더 할거 없을지도.
                } else {
                    task.exception?.let { e ->
                        Timber.e(e, "Failed to set user.")
                        // 유저 정보를 생성 못함.
                    }
                }
            }
        } ?: run {
            onUserDocumentSnapshotListener?.onException(NullPointerException("uid is null."))
        }
    }

    object Collection {
        const val Users = "users"
    }
}