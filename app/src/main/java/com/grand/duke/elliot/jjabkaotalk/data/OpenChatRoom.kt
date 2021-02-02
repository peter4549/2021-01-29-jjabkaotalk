package com.grand.duke.elliot.jjabkaotalk.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OpenChatRoom (
        val id: String,
        var name: String,
        var lastMessage: ChatMessage,
        var location: String,
        val unreadCounter: MutableMap<String, Int>,
        var users: MutableList<User>,
        val time: Long
): Parcelable {
    companion object {
        const val FIELD_LAST_MESSAGE = "lastMessage"
        const val FIELD_LOCATION = "location"
        const val FIELD_UNREAD_COUNTER = "unreadCounter"
        const val FIELD_USERS = "users"
    }
}