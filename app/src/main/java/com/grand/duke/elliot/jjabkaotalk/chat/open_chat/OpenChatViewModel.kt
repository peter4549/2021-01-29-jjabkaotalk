package com.grand.duke.elliot.jjabkaotalk.chat.open_chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.*
import com.google.gson.Gson
import com.grand.duke.elliot.jjabkaotalk.data.ChatMessage
import com.grand.duke.elliot.jjabkaotalk.data.OpenChatRoom
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.firebase.Collection
import com.grand.duke.elliot.jjabkaotalk.firebase.FireStoreHelper
import com.grand.duke.elliot.jjabkaotalk.firebase.isNotNull
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber

class OpenChatViewModel: ViewModel() {

    private lateinit var chatMessageCollectionReference: CollectionReference
    private lateinit var listenerRegistration: ListenerRegistration
    val fireStoreHelper = FireStoreHelper()

    @Suppress("SpellCheckingInspection")
    private val gson = Gson()
    private val _chatMessages = MutableLiveData<ArrayList<ChatMessage>>()
    val chatMessages: LiveData<ArrayList<ChatMessage>>
        get() = _chatMessages

    private val user = MainApplication.user ?: throw NullPointerException("ChatMessageAdapter: MainApplication.user is null.")

    /** Receive chat messages. */
    fun setChatMessageSnapshotListener(openChatRoom: OpenChatRoom) {
        val openChatRoomCollectionReference = FirebaseFirestore.getInstance().collection(Collection.OpenChatRooms)
        chatMessageCollectionReference = openChatRoomCollectionReference
            .document(openChatRoom.id).collection(Collection.Messages)
        listenerRegistration = chatMessageCollectionReference
            .orderBy(ChatMessage.FIELD_TIME)
            .addSnapshotListener { querySnapshot, fireStoreException ->
                when {
                    fireStoreException.isNotNull() -> {}
                    else -> {
                        querySnapshot?.let {
                            for (documentChange in querySnapshot.documentChanges) {
                                val chatMessage = documentChange.document.data.toChatMessage() ?: continue

                                when (documentChange.type) {
                                    DocumentChange.Type.ADDED -> {
                                        val value = arrayListOf<ChatMessage>()
                                        chatMessages.value?.let {
                                            value.addAll(it)
                                        }
                                        value.add(chatMessage)
                                        _chatMessages.value = value
                                        updateReaderIds(chatMessage, documentChange.document.id)
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        remove(chatMessage)
                                    }
                                    else -> { /** Unused. */ }
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun remove(chatMessage: ChatMessage) {
        chatMessages.value?.let { chatMessages ->
            val value = chatMessages.find {
                it.senderId == chatMessage.senderId && it.time == chatMessage.time
            }
            val index = chatMessages.indexOf(value)

            if (index == -1)
                return@let

            chatMessages.removeAt(index)
            _chatMessages.value = chatMessages
        }
    }

    private fun Map<String, Any>.toChatMessage(): ChatMessage? {
        val chatMessage = gson.fromJson(JSONObject(this).toString(), ChatMessage::class.java)
            ?: return null

        return chatMessage.apply {
            readerIds.apply {
                if (this.contains(user.uid).not())
                    add(user.uid)
            }
        }
    }

    private fun updateReaderIds(chatMessage: ChatMessage, chatMessageDocumentId: String? = null) {
        if (chatMessage.readerIds.contains(user.uid).not()) {
            chatMessageDocumentId?.let {
                chatMessageCollectionReference
                    .document(chatMessageDocumentId)
                    .update(ChatMessage.FIELD_READER_IDS, FieldValue.arrayUnion(user.uid))
                    .addOnSuccessListener {
                        Timber.d("readerIds updated.")
                    }
                    .addOnFailureListener {
                        Timber.e(it)
                    }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        removeListenerRegistration()
    }

    private fun removeListenerRegistration() {
        if (this::listenerRegistration.isInitialized)
            listenerRegistration.remove()
    }
}