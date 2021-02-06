package com.grand.duke.elliot.jjabkaotalk.chat.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.grand.duke.elliot.jjabkaotalk.data.ChatRoom
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.firebase.FireStoreHelper
import timber.log.Timber

class ChatRoomsViewModel: ViewModel(), FireStoreHelper.OnOpenChatRoomSnapshotListener {

    private val fireStoreHelper = FireStoreHelper()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?>
        get() = _user

    private val _chatRooms = MutableLiveData<MutableList<ChatRoom>>()
    val chatRooms: LiveData<MutableList<ChatRoom>>
        get() = _chatRooms

    private val _exception = MutableLiveData<Exception>()
    val exception: LiveData<Exception>
        get() = _exception

    private val _updatedPosition = MutableLiveData<Int>()
    val updatedPosition: LiveData<Int>
        get() = _updatedPosition

    init {
        _updatedPosition.value = -1
    }

    fun registerChatRoomSnapshotListener(): ListenerRegistration {
        fireStoreHelper.setOnOpenChatRoomSnapshotListener(this)
        return fireStoreHelper.registerOpenChatRoomSnapshotListener("busan") // todo. test city.
    }

    /** FireStoreHelper.OnOpenChatRoomSnapshotListener */
    override fun onOpenChatRoomDocumentSnapshot(documentChanges: List<DocumentChange>) {
        documentChanges.forEach { documentChange ->
            val openChatRoom = fireStoreHelper.convertToChatRoom(documentChange.document.data)

            fireStoreHelper.getUsers(openChatRoom.users.map { it.uid }) { users ->
                openChatRoom.users = users.toMutableList()  // Update users.

                when(documentChange.type) {
                    DocumentChange.Type.ADDED -> {
                        val value = mutableListOf<ChatRoom>()
                        chatRooms.value?.let { chatRooms ->
                            chatRooms.forEach { chatRoom ->
                                value.add(chatRoom.deepCopy())
                            }
                        }

                        value.add(openChatRoom)
                        _chatRooms.value = value
                    }
                    DocumentChange.Type.MODIFIED -> update(openChatRoom)
                    DocumentChange.Type.REMOVED -> remove(openChatRoom)
                }
            }
        }
    }

    private fun update(chatRoom: ChatRoom) {
        chatRooms.value?.let { chatRooms ->
            val value = chatRooms.find { it.id == chatRoom.id } ?: return@let
            val index = chatRooms.indexOf(value)

            if (index == -1)
                return@let

            chatRooms[index] = value
            _chatRooms.value = chatRooms
            _updatedPosition.value = index
        }
    }

    fun clear() {
        _chatRooms.value = null
    }

    private fun remove(chatRoom: ChatRoom) {
        chatRooms.value?.let { chatRooms ->
            val value = chatRooms.find { it.id == chatRoom.id }
            val index = chatRooms.indexOf(value)

            if (index == -1)
                return@let

            chatRooms.removeAt(index)
            _chatRooms.value = chatRooms
        }
    }

    override fun onException(exception: Exception) {
        Timber.e(exception)
        _exception.value = exception
    }
}