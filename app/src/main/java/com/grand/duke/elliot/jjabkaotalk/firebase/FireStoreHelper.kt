package com.grand.duke.elliot.jjabkaotalk.firebase

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import com.grand.duke.elliot.jjabkaotalk.data.ChatMessage
import com.grand.duke.elliot.jjabkaotalk.data.OpenChatRoom
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import com.grand.duke.elliot.jjabkaotalk.util.blank
import org.json.JSONObject
import timber.log.Timber
import kotlin.NullPointerException

class FireStoreHelper {

    private val firebaseUser = MainApplication.getFirebaseAuthInstance().currentUser
    @Suppress("SpellCheckingInspection")
    private val gson = Gson()
    private val openChatRoomCollectionReference = FirebaseFirestore.getInstance().collection(Collection.OpenChatRooms)
    private val userCollectionReference = FirebaseFirestore.getInstance().collection(Collection.Users)

    private var onOpenChatRoomSnapshotListener: OnOpenChatRoomSnapshotListener? = null
    private var onUserDocumentSnapshotListener: OnUserDocumentSnapshotListener? = null
    private var onUserSetListener: OnUserSetListener? = null

    private lateinit var openChatRoomsListenerRegistration: ListenerRegistration

    fun setOnOpenChatRoomSnapshotListener(onOpenChatRoomSnapshotListener: OnOpenChatRoomSnapshotListener) {
        this.onOpenChatRoomSnapshotListener = onOpenChatRoomSnapshotListener
    }

    fun setOnDocumentSnapshotListener(onUserDocumentSnapshotListener: OnUserDocumentSnapshotListener) {
        this.onUserDocumentSnapshotListener = onUserDocumentSnapshotListener
    }

    fun setOnUserSetListener(onUserSetListener: OnUserSetListener) {
        this.onUserSetListener = onUserSetListener
    }

    interface OnUserDocumentSnapshotListener {
        fun onUserDocumentSnapshot(user: User)
        fun onException(exception: Exception)
        fun onNoUserDocumentSnapshot()
    }

    interface OnUserSetListener {
        fun onSuccess()
        fun onFailure()
    }

    interface OnOpenChatRoomSnapshotListener {
        fun onOpenChatRoomDocumentSnapshot(documentChanges: List<DocumentChange>)
        fun onException(exception: Exception)
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
                    onUserSetListener?.onSuccess()
                } else {
                    task.exception?.let { e ->
                        Timber.e(e, "Failed to set user.")
                        // 유저 정보를 생성 못함.
                        onUserSetListener?.onFailure()
                    }
                }
            }
        } ?: run {
            onUserDocumentSnapshotListener?.onException(NullPointerException("uid is null."))
        }
    }

    fun setupOpenChatRoomSnapshotListener(location: String) {
        openChatRoomsListenerRegistration = openChatRoomCollectionReference
                .whereEqualTo(OpenChatRoom.FIELD_LOCATION, location)
                .addSnapshotListener { querySnapshot, fireStoreException ->
                    fireStoreException?.let {
                        onOpenChatRoomSnapshotListener?.onException(it)
                    } ?: run {
                        querySnapshot?.let {
                            /** Success. */
                            onOpenChatRoomSnapshotListener?.onOpenChatRoomDocumentSnapshot(it.documentChanges)
                        } ?: run {
                            @Suppress("ThrowableNotThrown")
                            onOpenChatRoomSnapshotListener?.onException(NullPointerException("querySnapshot is null."))
                        }
                    }
                }
    }

    fun convertToOpenChatRoom(map: Map<String, Any>): OpenChatRoom {
        return gson.fromJson(JSONObject(map).toString(), OpenChatRoom::class.java)
    }

    fun setOpenChatRoom(openChatRoom: OpenChatRoom, message: String) {
        val openChatRoomDocumentReference = openChatRoomCollectionReference.document(openChatRoom.id)
        val uid = MainApplication.user?.uid ?: throw NullPointerException("MainApplication.user?.uid is null.")
        val chatMessage = ChatMessage(
                message = message,
                readerIds = mutableListOf(uid),
                senderId = uid,
                time = openChatRoom.time
        )

        openChatRoomDocumentReference
                .set(openChatRoom).addOnSuccessListener {
                    openChatRoomDocumentReference.collection(Collection.Messages)
                            .add(chatMessage)
                            .addOnSuccessListener {
                                // TODO fill.
                            }
                            .addOnFailureListener {
                                // TODO fill.
                            }
                }.addOnFailureListener {
                    Timber.e(it)
                    // todo. fill.
                }
    }

    object Collection {
        const val Messages = "message"
        const val OpenChatRooms = "open_chat_rooms"
        const val Users = "users"
    }
}