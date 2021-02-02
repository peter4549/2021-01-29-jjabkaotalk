package com.grand.duke.elliot.jjabkaotalk.chat.open_chat.room

import androidx.lifecycle.ViewModel
import com.grand.duke.elliot.jjabkaotalk.data.OpenChatRoom

class OpenChatRoomsViewModel: ViewModel() {
    val openChatRooms = mutableListOf<OpenChatRoom>()
}