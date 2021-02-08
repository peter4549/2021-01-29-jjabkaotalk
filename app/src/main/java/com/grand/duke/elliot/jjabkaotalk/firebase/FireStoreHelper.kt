package com.grand.duke.elliot.jjabkaotalk.firebase

import com.google.firebase.firestore.*
import com.google.gson.Gson
import com.grand.duke.elliot.jjabkaotalk.data.ChatMessage
import com.grand.duke.elliot.jjabkaotalk.data.ChatRoom
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber


class FireStoreHelper {

    private val firebaseUser = MainApplication.getFirebaseAuthInstance().currentUser
    @Suppress("SpellCheckingInspection")
    private val gson = Gson()

    private val openChatRoomCollectionReference = FirebaseFirestore.getInstance().collection(Collection.ChatRooms)
    private val userCollectionReference = FirebaseFirestore.getInstance().collection(Collection.Users)

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    private var onOpenChatRoomSnapshotListener: OnOpenChatRoomSnapshotListener? = null
    private var onUserDocumentSnapshotListener: OnUserDocumentSnapshotListener? = null
    private var onSetUserListener: OnSetUserListener? = null
    private var onSetOpenChatRoomListener: OnSetOpenChatRoomListener? = null
    private var onMyChatRoomsSnapshotListener: OnMyChatRoomsSnapshotListener? = null

    fun setOnOpenChatRoomSnapshotListener(onOpenChatRoomSnapshotListener: OnOpenChatRoomSnapshotListener) {
        this.onOpenChatRoomSnapshotListener = onOpenChatRoomSnapshotListener
    }

    fun setOnUserDocumentSnapshotListener(onUserDocumentSnapshotListener: OnUserDocumentSnapshotListener) {
        this.onUserDocumentSnapshotListener = onUserDocumentSnapshotListener
    }

    fun setOnSetUserListener(onSetUserListener: OnSetUserListener) {
        this.onSetUserListener = onSetUserListener
    }

    fun setOnSetOpenChatRoomListener(onSetOpenChatRoomListener: OnSetOpenChatRoomListener) {
        this.onSetOpenChatRoomListener = onSetOpenChatRoomListener
    }

    fun setOnMyChatRoomsSnapshotListener(onMyChatRoomsSnapshotListener: OnMyChatRoomsSnapshotListener) {
        this.onMyChatRoomsSnapshotListener = onMyChatRoomsSnapshotListener
    }

    interface OnMyChatRoomsSnapshotListener {
        fun onMyChatRoomsSnapshot(documentChanges: List<DocumentChange>)
        fun onException(exception: Exception)
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
                .whereEqualTo(ChatRoom.FIELD_LOCATION, location)
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

    fun convertToChatRoom(map: Map<String, Any>): ChatRoom {
        return gson.fromJson(JSONObject(map).toString(), ChatRoom::class.java)
    }

    fun setOpenChatRoom(chatRoom: ChatRoom, chatMessage: ChatMessage) {
        val openChatRoomDocumentReference = openChatRoomCollectionReference.document(chatRoom.id)

        openChatRoomDocumentReference
                .set(chatRoom).addOnSuccessListener {
                    openChatRoomDocumentReference.collection(Collection.Messages)
                            .add(chatMessage)
                            .addOnSuccessListener {
                                Timber.d("openChatRoom created.")
                                onSetOpenChatRoomListener?.onSuccess(chatRoom.id)
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

        if (userIds.isEmpty()) {
            onUsers.invoke(users)
            return
        }

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

    fun leaveChatRoom(user: User, chatRoom: ChatRoom, delete: Boolean, onComplete: (exception: Exception?) -> Unit) {
        val chatRoomDocumentReference = openChatRoomCollectionReference.document(chatRoom.id)
        userCollectionReference.document(user.uid)
                .update(User.FIELD_CHAT_ROOMS, FieldValue.arrayRemove(chatRoom.id))
                .addOnCompleteListener {
                    chatRoomDocumentReference
                            .update(mapOf(
                                    ChatRoom.FIELD_USERS to FieldValue.arrayRemove(user),
                                    ChatRoom.FIELD_USER_IDS to FieldValue.arrayRemove(user.uid)
                            ))
                            .addOnCompleteListener {
                                if (delete) {
                                    coroutineScope.launch {
                                        val messageCollection = chatRoomDocumentReference.collection(Collection.Messages)
                                        deleteCollection(messageCollection, 10)
                                        chatRoomDocumentReference.delete()
                                    }
                                }
                                onComplete(it.exception)
                            }
                }
    }

    private fun deleteCollection(collection: CollectionReference, batchSize: Int) {
        try {
            // retrieve a small batch of documents to avoid out-of-memory errors
            val future = collection.limit(batchSize.toLong()).get()
            future.addOnSuccessListener {
                var deleted = 0
                val documents = it.documents

                for (document in documents) {
                    document.reference.delete()
                    ++deleted
                }

                if (deleted >= batchSize)
                    deleteCollection(collection, batchSize)
            }
        } catch (e: java.lang.Exception) {
            System.err.println("Error deleting collection : " + e.message)
        }
    }

    fun userArrayUnion(uid: String, field: String, value: Any, onSuccess: (() -> Unit)? = null) {
        userCollectionReference.document(uid)
                .update(field, FieldValue.arrayUnion(value))
                .addOnSuccessListener {
                    Timber.d("User updated.")
                    onSuccess?.invoke()
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

    fun addUserToChatRoom(documentId: String, user: User, onSuccess: (() -> Unit)?) {
        openChatRoomCollectionReference.document(documentId)
                .update(mapOf(
                        ChatRoom.FIELD_USERS to FieldValue.arrayUnion(user),
                        ChatRoom.FIELD_USER_IDS to FieldValue.arrayUnion(user.uid)
                ))
                .addOnSuccessListener {
                    onSuccess?.invoke()
                    Timber.d("ChatRoom updated.")
                }
                .addOnFailureListener {
                    Timber.w("ChatRoom update failed.")
                }
    }

    fun registerMyChatRoomSnapshotListener(user: User): ListenerRegistration? {
        if (user.chatRooms.isEmpty())
            return null

        return openChatRoomCollectionReference
                .whereArrayContains(ChatRoom.FIELD_USER_IDS, user.uid)
                .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    error?.let {
                        onOpenChatRoomSnapshotListener?.onException(it)
                    } ?: run {
                        value?.let {
                            /** Success. */
                            onMyChatRoomsSnapshotListener?.onMyChatRoomsSnapshot(it.documentChanges)
                        } ?: run {
                            @Suppress("ThrowableNotThrown")
                            onMyChatRoomsSnapshotListener?.onException(NullPointerException("querySnapshot is null."))
                        }
                    }
                }
    }

    fun unFriend(user: User, friend: User, onComplete: (exception: Exception?) -> Unit) {
        userCollectionReference.document(user.uid)
                .update(User.FIELD_FRIEND_IDS, FieldValue.arrayRemove(friend.uid))
                .addOnCompleteListener {
                    onComplete(it.exception)
                }
    }

    fun getMyChatRooms(user: User, onSuccess: ((List<ChatRoom>) -> Unit)? = null) {
        val chatRoomIds = user.chatRooms
        val chatRooms = mutableListOf<ChatRoom>()

        if (chatRoomIds.isEmpty()) {
            onSuccess?.invoke(chatRooms)
            return
        }

        openChatRoomCollectionReference
                .whereArrayContains(ChatRoom.FIELD_USERS, user.uid)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    querySnapshot.documents.forEach {
                        it.data?.let { data ->
                            chatRooms.add(convertToChatRoom(data))
                        }
                    }
                    onSuccess?.invoke(chatRooms)
                }
    }
}