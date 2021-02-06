package com.grand.duke.elliot.jjabkaotalk.chat.mine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.grand.duke.elliot.jjabkaotalk.data.ChatRoom
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.firebase.FireStoreHelper

class MyChatRoomsViewModel: ViewModel(), FireStoreHelper.OnMyChatRoomsSnapshotListener {

    private val fireStoreHelper = FireStoreHelper()
    private var listenerRegistration: ListenerRegistration? = null

    private val _myChatRooms = MutableLiveData<MutableList<ChatRoom>>()
    val myChatRooms: LiveData<MutableList<ChatRoom>>
        get() = _myChatRooms

    private val _exception = MutableLiveData<Exception>()
    val exception: LiveData<Exception>
        get() = _exception

    private val _updatedPosition = MutableLiveData<Int>()
    val updatedPosition: LiveData<Int>
        get() = _updatedPosition

    init {
        fireStoreHelper.setOnMyChatRoomsSnapshotListener(this)
        _updatedPosition.value = -1
    }

    fun registerMyChatRoomSnapshotListener(user: User) {
        if (listenerRegistration == null)
            listenerRegistration = fireStoreHelper.registerMyChatRoomSnapshotListener(user.chatRooms)
    }

    fun unregisterMyChatRoomSnapshotListener() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    override fun onMyChatRoomsSnapshot(documentChanges: List<DocumentChange>) {
        documentChanges.forEach { documentChange ->
            val chatRoom = fireStoreHelper.convertToChatRoom(documentChange.document.data)

            when(documentChange.type) {
                DocumentChange.Type.ADDED -> {
                    val value = mutableListOf<ChatRoom>()
                    myChatRooms.value?.let { chatRooms ->
                        chatRooms.forEach { chatRoom ->
                            value.add(chatRoom.deepCopy())
                        }
                    }

                    value.add(chatRoom)
                    _myChatRooms.value = value
                }
                DocumentChange.Type.MODIFIED -> update(chatRoom)
                DocumentChange.Type.REMOVED -> remove(chatRoom)
            }
        }
    }

    private fun update(chatRoom: ChatRoom) {
        myChatRooms.value?.let { chatRooms ->
            val value = chatRooms.find { it.id == chatRoom.id } ?: return@let
            val index = chatRooms.indexOf(value)

            if (index == -1)
                return@let

            chatRooms[index] = value
            _myChatRooms.value = chatRooms
            _updatedPosition.value = index
        }
    }

    private fun remove(chatRoom: ChatRoom) {
        myChatRooms.value?.let { chatRooms ->
            val value = chatRooms.find { it.id == chatRoom.id }
            val index = chatRooms.indexOf(value)

            if (index == -1)
                return@let

            chatRooms.removeAt(index)
            _myChatRooms.value = chatRooms
        }
    }

    override fun onException(exception: Exception) {
        _exception.value = exception
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}