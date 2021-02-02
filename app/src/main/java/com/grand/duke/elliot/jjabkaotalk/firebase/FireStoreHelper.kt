package com.grand.duke.elliot.jjabkaotalk.firebase

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import com.grand.duke.elliot.jjabkaotalk.data.ChatMessage
import com.grand.duke.elliot.jjabkaotalk.data.OpenChatRoom
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
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
    private var onSetUserListener: OnSetUserListener? = null
    private var onSetOpenChatRoomListener: OnSetOpenChatRoomListener? = null

    private lateinit var openChatRoomsListenerRegistration: ListenerRegistration

    fun setOnOpenChatRoomSnapshotListener(onOpenChatRoomSnapshotListener: OnOpenChatRoomSnapshotListener) {
        this.onOpenChatRoomSnapshotListener = onOpenChatRoomSnapshotListener
    }

    fun setOnDocumentSnapshotListener(onUserDocumentSnapshotListener: OnUserDocumentSnapshotListener) {
        this.onUserDocumentSnapshotListener = onUserDocumentSnapshotListener
    }

    fun setOnSetUserListener(onSetUserListener: OnSetUserListener) {
        this.onSetUserListener = onSetUserListener
    }

    fun setOnSetOpenChatRoomListener(onSetOpenChatRoomListener: OnSetOpenChatRoomListener) {
        this.onSetOpenChatRoomListener = onSetOpenChatRoomListener
    }

    interface OnUserDocumentSnapshotListener {
        fun onUserDocumentSnapshot(user: User)
        fun onException(exception: Exception)
        fun onNoUserDocumentSnapshot()
    }

    interface OnSetUserListener {
        fun onSuccess()
        fun onFailure()
    }

    interface OnSetOpenChatRoomListener {
        fun onSuccess(openChatRoomId: String)
        fun onFailure()
    }

    interface OnOpenChatRoomSnapshotListener {
        fun onOpenChatRoomDocumentSnapshot(documentChanges: List<DocumentChange>)
        fun onException(exception: Exception)
    }

    fun registerUserSnapshotListener(uid: String): ListenerRegistration {
        val documentReference = userCollectionReference.document(uid)
        return documentReference.addSnapshotListener { documentSnapshot, fireStoreException ->
            fireStoreException?.let {
                onUserDocumentSnapshotListener?.onException(it)
            } ?: run {
                documentSnapshot?.let { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        documentSnapshot.data?.let {
                            val user = gson.fromJson(JSONObject(it).toString(), User::class.java)
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
                    onSetUserListener?.onSuccess()
                } else {
                    task.exception?.let { e ->
                        Timber.e(e, "Failed to set user.")
                        // 유저 정보를 생성 못함.
                        onSetUserListener?.onFailure()
                    }
                }
            }
        } ?: run {
            onUserDocumentSnapshotListener?.onException(NullPointerException("uid is null."))
        }
    }

    fun registerOpenChatRoomSnapshotListener(location: String): ListenerRegistration {
        return openChatRoomCollectionReference
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

    fun setOpenChatRoom(openChatRoom: OpenChatRoom, chatMessage: ChatMessage) {
        val openChatRoomDocumentReference = openChatRoomCollectionReference.document(openChatRoom.id)

        openChatRoomDocumentReference
                .set(openChatRoom).addOnSuccessListener {
                    openChatRoomDocumentReference.collection(Collection.Messages)
                            .add(chatMessage)
                            .addOnSuccessListener {
                                Timber.d("openChatRoom created.")
                                onSetOpenChatRoomListener?.onSuccess(openChatRoom.id)
                            }
                            .addOnFailureListener {
                                Timber.e(it)
                                onSetOpenChatRoomListener?.onFailure()
                            }
                }.addOnFailureListener {
                    Timber.e(it)
                    onSetOpenChatRoomListener?.onFailure()
                }
    }

    fun getUsers(userIds: List<String>, onUsers: (List<User>) -> Unit) {
        val users = mutableListOf<User>()
        userCollectionReference.whereIn(User.FIELD_UID, userIds).get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                document?.data?.let {
                    users.add(gson.fromJson(JSONObject(it).toString(), User::class.java))
                }
            }

            onUsers.invoke(users)
        }
    }

    fun updateUser(user: User, field: String, value: Any) {
        userCollectionReference.document(user.uid)
                .update(mapOf(field to value))
                .addOnSuccessListener {
                    Timber.d("User updated.")
                }
                .addOnFailureListener {
                    Timber.w("User update failed.")
                }
    }

    fun userArrayUnion(uid: String, field: String, value: Any) {
        userCollectionReference.document(uid)
                .update(field, FieldValue.arrayUnion(value))
                .addOnSuccessListener {
                    Timber.d("User updated.")
                }
                .addOnFailureListener {
                    Timber.w("User update failed.")
                }
    }

    fun chatRoomArrayUnion(id: String, field: String, value: Any) {
        openChatRoomCollectionReference.document(id)
                .update(field, FieldValue.arrayUnion(value))
                .addOnSuccessListener {
                    Timber.d("ChatRoom updated.")
                }
                .addOnFailureListener {
                    Timber.w("ChatRoom update failed.")
                }
    }
}