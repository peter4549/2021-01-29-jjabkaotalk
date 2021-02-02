package com.grand.duke.elliot.jjabkaotalk.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ChatRoom (
    val id: String,
    var name: String,
    var users: MutableList<User>
): Parcelable {
    companion object {
        const val FIELD_TIME = "time"
    }
}