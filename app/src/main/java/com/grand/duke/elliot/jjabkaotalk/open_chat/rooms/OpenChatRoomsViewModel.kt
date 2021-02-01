package com.grand.duke.elliot.jjabkaotalk.open_chat.rooms

import androidx.lifecycle.ViewModel
import com.grand.duke.elliot.jjabkaotalk.data.OpenChatRoom

class OpenChatRoomsViewModel: ViewModel() {
    val openChatRooms = mutableListOf<OpenChatRoom>()
}