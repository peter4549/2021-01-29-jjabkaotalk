package com.grand.duke.elliot.jjabkaotalk.data

import android.os.Parcelable
import com.grand.duke.elliot.jjabkaotalk.util.blank
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User (
    val uid: String,
    var name: String,
    var profilePhotoUris: MutableList<String>,
    var location: String,
    var friends: MutableList<String>, // TODO, check, is able to be list? (mutable..)
    var chatRooms: MutableList<Long>, // TODO, check, is able to be list?
    var openChatRooms: MutableList<Long>,
    var pushToken: String = blank,
    var verified: Boolean
): Parcelable {

    companion object {
        const val FIELD_UID = "uid"
        const val FIELD_NAME = "name"
        const val FIELD_PROFILE_PHOTO_URI = "profilePhotoUri"
        const val FIELD_LOCATION = "location"
        const val FIELD_FRIENDS = "friends"
        const val FIELD_CHAT_ROOMS = "chatRooms"
        const val FIELD_VERIFIED = "verified"
    }
}