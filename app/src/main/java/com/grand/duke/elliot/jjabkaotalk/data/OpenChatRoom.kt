package com.grand.duke.elliot.jjabkaotalk.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OpenChatRoom (
    val id: String,
    var name: String,
    var location: String,
    var users: MutableList<User>, // TODO, check, is able to be list?
    val time: Long
): Parcelable {
    companion object {
        const val FIELD_LOCATION = "location"
        const val FIELD_USERS = "users"
    }
}