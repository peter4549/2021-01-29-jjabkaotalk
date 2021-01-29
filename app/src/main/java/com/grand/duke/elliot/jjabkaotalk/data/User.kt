package com.grand.duke.elliot.jjabkaotalk.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User (
    val uid: String,
    var name: String,
    var profilePhotoUri: String?,
    var location: String,
    var friends: Array<String>, // TODO, check, is able to be list? (mutable..)
    var chatRooms: Array<Long>, // TODO, check, is able to be list?
    var verified: Boolean
): Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (uid != other.uid) return false
        if (name != other.name) return false
        if (profilePhotoUri != other.profilePhotoUri) return false
        if (location != other.location) return false
        if (!friends.contentEquals(other.friends)) return false
        if (!chatRooms.contentEquals(other.chatRooms)) return false
        if (verified != other.verified) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (profilePhotoUri?.hashCode() ?: 0)
        result = 31 * result + location.hashCode()
        result = 31 * result + friends.contentHashCode()
        result = 31 * result + chatRooms.contentHashCode()
        result = 31 * result + verified.hashCode()
        return result
    }

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