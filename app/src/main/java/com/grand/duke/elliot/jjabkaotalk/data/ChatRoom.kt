package com.grand.duke.elliot.jjabkaotalk.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatRoom (
        val id: String,
        var name: String,
        var lastMessage: ChatMessage,
        var location: String,
        val unreadCounter: MutableMap<String, Int>,
        var users: MutableList<User>,
        val time: Long,
        val type: Int
): Parcelable {

    fun deepCopy(): ChatRoom {
        val unreadCounter = HashMap(this.unreadCounter)
        val users = this.users.map { it.deepCopy() }.toMutableList()

        return ChatRoom(
                id = this.id,
                name = this.name,
                lastMessage = this.lastMessage.deepCopy(),
                location = this.location,
                unreadCounter = unreadCounter,
                users = users,
                time = this.time,
                type = this.type
        )
    }

    companion object {
        const val FIELD_ID = "id"
        const val FIELD_LAST_MESSAGE = "lastMessage"
        const val FIELD_LOCATION = "location"
        const val FIELD_UNREAD_COUNTER = "unreadCounter"
        const val FIELD_USERS = "users"

        const val TYPE_PRIVATE = 1
        const val TYPE_PUBLIC = 0
    }
}