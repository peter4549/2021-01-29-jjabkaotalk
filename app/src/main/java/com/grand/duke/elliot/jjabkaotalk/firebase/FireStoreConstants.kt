package com.grand.duke.elliot.jjabkaotalk.firebase

import com.google.firebase.firestore.FirebaseFirestoreException

object Collection {
    const val Messages = "messages"
    const val ChatRooms = "chat_rooms"
    const val Users = "users"
}

fun FirebaseFirestoreException?.isNotNull() = this != null
fun FirebaseFirestoreException?.isNull() = this == null