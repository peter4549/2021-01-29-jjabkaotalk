package com.grand.duke.elliot.jjabkaotalk.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.*
import com.google.gson.Gson
import com.grand.duke.elliot.jjabkaotalk.data.ChatMessage
import com.grand.duke.elliot.jjabkaotalk.data.ChatRoom
import com.grand.duke.elliot.jjabkaotalk.firebase.Collection
import com.grand.duke.elliot.jjabkaotalk.firebase.FireStoreHelper
import com.grand.duke.elliot.jjabkaotalk.firebase.isNotNull
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import org.json.JSONObject
import org.w3c.dom.DocumentType
import timber.log.Timber

class ChatMessagesViewModel(chatRoom: ChatRoom): ViewModel() {

    private lateinit var listenerRegistration: ListenerRegistration
    val fireStoreHelper = FireStoreHelper()
    val openChatRoomDocumentReference = FirebaseFirestore.getInstance()
            .collection(Collection.ChatRooms)
            .document(chatRoom.id)
    val chatMessageCollectionReference: CollectionReference = openChatRoomDocumentReference.collection(Collection.Messages)

    init {
        setChatMessageSnapshotListener(chatRoom)
    }

    @Suppress("SpellCheckingInspection")
    private val gson = Gson()
    private val _displayChatMessages = MutableLiveData<DisplayChatMessages>()
    val displayChatMessages: LiveData<DisplayChatMessages>
        get() = _displayChatMessages

    private val user = MainApplication.user.value ?: throw NullPointerException("ChatMessageAdapter: MainApplication.user is null.")

    /** Receive chat messages. */
    private fun setChatMessageSnapshotListener(chatRoom: ChatRoom) {
        listenerRegistration = chatMessageCollectionReference
            .orderBy(ChatMessage.FIELD_TIME)
            .addSnapshotListener { querySnapshot, fireStoreException ->
                when {
                    fireStoreException.isNotNull() -> {}
                    else -> {
                        querySnapshot?.let {
                            for (documentChange in querySnapshot.documentChanges) {
                                val chatMessage = documentChange.document.data
                                    .toChatMessage(documentChange.document.id) ?: continue

                                when (documentChange.type) {
                                    DocumentChange.Type.ADDED -> {
                                        val value = arrayListOf<ChatMessage>()
                                        displayChatMessages.value?.let {
                                            value.addAll(it.chatMessages)
                                        }
                                        value.add(chatMessage)
                                        _displayChatMessages.value = DisplayChatMessages(value, DocumentChange.Type.ADDED, null)
                                        updateReaderIds(chatMessage, documentChange.document.id)
                                    }
                                    DocumentChange.Type.MODIFIED -> {
                                        update(chatMessage)
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        remove(chatMessage)
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun update(chatMessage: ChatMessage) {
        displayChatMessages.value?.let { displayChatMessages ->
            val chatMessages = displayChatMessages.chatMessages
            val value = chatMessages.find {
                it.senderId == chatMessage.senderId && it.time == chatMessage.time
            }
            val index = chatMessages.indexOf(value)

            if (index == -1)
                return@let

            println("UUUUUUUUUUUUUUUU: ${value}")
            value?.let {
                chatMessages[index] = chatMessage
                println("MMMMMMMMMMMupdate: ${chatMessages[index]}")
                _displayChatMessages.value = displayChatMessages.apply {
                    this.chatMessages = chatMessages
                    this.type = DocumentChange.Type.MODIFIED
                    this.changedChatMessage = chatMessage
                }
            }
        }
    }

    private fun remove(chatMessage: ChatMessage) {
        displayChatMessages.value?.let { displayChatMessages ->
            val chatMessages = displayChatMessages.chatMessages
            val value = chatMessages.find {
                it.senderId == chatMessage.senderId && it.time == chatMessage.time
            }
            val index = chatMessages.indexOf(value)

            if (index == -1)
                return@let

            chatMessages.removeAt(index)
            _displayChatMessages.value = displayChatMessages.apply {
                this.chatMessages = chatMessages
                this.type = DocumentChange.Type.REMOVED
                this.changedChatMessage = null
            }
        }
    }

    private fun Map<String, Any>.toChatMessage(chatMessageDocumentId: String? = null): ChatMessage? {
        val chatMessage = gson.fromJson(JSONObject(this).toString(), ChatMessage::class.java)
            ?: return null

        chatMessageDocumentId?.let {
            chatMessageCollectionReference.document(it)
                .update(
                    ChatMessage.FIELD_READER_IDS,
                    FieldValue.arrayUnion(user.uid)
                ).addOnSuccessListener {
                    Timber.d("readerIds updated.")
                }.addOnFailureListener { e ->
                    Timber.e(e)
                }
        }

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

data class DisplayChatMessages(
    var chatMessages: ArrayList<ChatMessage>,
    var type: DocumentChange.Type,
    var changedChatMessage: ChatMessage?
)