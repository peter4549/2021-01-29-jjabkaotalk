package com.grand.duke.elliot.jjabkaotalk.chat.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.grand.duke.elliot.jjabkaotalk.data.ChatRoom
import com.grand.duke.elliot.jjabkaotalk.data.User
import com.grand.duke.elliot.jjabkaotalk.firebase.FireStoreHelper
import com.grand.duke.elliot.jjabkaotalk.main.MainApplication
import timber.log.Timber

class ChatRoomsViewModel: ViewModel(), FireStoreHelper.OnOpenChatRoomSnapshotListener {

    private val fireStoreHelper = FireStoreHelper()

    private val _displayChatRooms = MutableLiveData<DisplayChatRoomList>()
    val displayChatRooms: LiveData<DisplayChatRoomList>
        get() = _displayChatRooms

    private val _exception = MutableLiveData<Exception>()
    val exception: LiveData<Exception>
        get() = _exception

    fun registerChatRoomSnapshotListener(user: User): ListenerRegistration {
        fireStoreHelper.setOnOpenChatRoomSnapshotListener(this)
        return fireStoreHelper.registerOpenChatRoomSnapshotListener("busan") // todo. test city.
    }

    /** FireStoreHelper.OnOpenChatRoomSnapshotListener */
    override fun onOpenChatRoomDocumentSnapshot(documentChanges: List<DocumentChange>) {
        for (documentChange in documentChanges) {
            val openChatRoom = fireStoreHelper.convertToChatRoom(documentChange.document.data)

            if (MainApplication.user.value?.chatRooms?.contains(openChatRoom.id) == true)
                continue

            fireStoreHelper.getUsers(openChatRoom.users.map { it.uid }) { users ->
                openChatRoom.users = users.toMutableList()  // Update users.

                when(documentChange.type) {
                    DocumentChange.Type.ADDED -> {
                        val value = mutableListOf<ChatRoom>()
                        displayChatRooms.value?.let { chatRooms ->
                            chatRooms.chatRoomList.forEach { chatRoom ->
                                try {
                                    value.add(chatRoom.deepCopy())
                                } catch (e: java.lang.Exception) {
                                    Timber.e(e)
                                }
                            }
                        }

                        value.add(openChatRoom)
                        _displayChatRooms.value = DisplayChatRoomList(value, null)
                    }
                    DocumentChange.Type.MODIFIED -> update(openChatRoom)
                    DocumentChange.Type.REMOVED -> remove(openChatRoom)
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
        Timber.e(exception)
        _exception.value = exception
    }
}

data class DisplayChatRoomList(
        var chatRoomList: MutableList<ChatRoom>,
        var modifiedChatRoom: ChatRoom?
)