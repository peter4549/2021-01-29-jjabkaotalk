package com.grand.duke.elliot.jjabkaotalk.chat.mine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.grand.duke.elliot.jjabkaotalk.chat.room.DisplayChatRoomList
import com.grand.duke.elliot.jjabkaotalk.data.ChatRoom
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.firebase.FireStoreHelper

class MyChatRoomsViewModel: ViewModel(), FireStoreHelper.OnMyChatRoomsSnapshotListener {

    private val fireStoreHelper = FireStoreHelper()

    private val _displayChatRooms = MutableLiveData<DisplayChatRoomList>()
    val displayChatRooms: LiveData<DisplayChatRoomList>
        get() = _displayChatRooms

    private val _exception = MutableLiveData<Exception>()
    val exception: LiveData<Exception>
        get() = _exception

    init {
        fireStoreHelper.setOnMyChatRoomsSnapshotListener(this)
    }

    fun registerMyChatRoomSnapshotListener(user: User) = fireStoreHelper.registerMyChatRoomSnapshotListener(user)

    override fun onMyChatRoomsSnapshot(documentChanges: List<DocumentChange>) {

        println("AAAA: ${documentChanges.map { fireStoreHelper.convertToChatRoom(it.document.data) }}")
        documentChanges.forEach { documentChange ->
            val chatRoom = fireStoreHelper.convertToChatRoom(documentChange.document.data)

            fireStoreHelper.getUsers(chatRoom.users.map { it.uid }) { users ->
                chatRoom.users = users.toMutableList()  // Update users.

                when(documentChange.type) {
                    DocumentChange.Type.ADDED -> {
                        val value = mutableListOf<ChatRoom>()
                        displayChatRooms.value?.let { chatRooms ->
                            chatRooms.chatRoomList.forEach { chatRoom ->
                                value.add(chatRoom.deepCopy())
                            }
                        }

                        value.add(chatRoom)
                        _displayChatRooms.value = DisplayChatRoomList(value, null)
                    }
                    DocumentChange.Type.MODIFIED -> update(chatRoom)
                    DocumentChange.Type.REMOVED -> remove(chatRoom)
                }
            }
        }
    }

    private fun update(chatRoom: ChatRoom) {
        displayChatRooms.value?.let { chatRooms ->
            val chatRoomList = chatRooms.chatRoomList
            val value = chatRoomList.find { it.id == chatRoom.id } ?: return@let
            val index = chatRoomList.indexOf(value)

            if (index == -1)
                return@let

            chatRoomList[index] = chatRoom
            _displayChatRooms.value = DisplayChatRoomList(chatRoomList, chatRoom)
        }
    }

    fun clear() {
        _displayChatRooms.value = null
    }

    private fun remove(chatRoom: ChatRoom) {
        _displayChatRooms.value?.let { chatRooms ->
            val chatRoomList = chatRooms.chatRoomList
            val value = chatRoomList.find { it.id == chatRoom.id }
            val index = chatRoomList.indexOf(value)

            if (index == -1)
                return@let

            chatRoomList.removeAt(index)
            _displayChatRooms.value = DisplayChatRoomList(chatRoomList, null)
        }
    }

    override fun onException(exception: Exception) {
        _exception.value = exception
    }
}