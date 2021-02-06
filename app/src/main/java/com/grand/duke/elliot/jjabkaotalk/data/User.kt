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
        var friendIds: MutableList<String>,
        var chatRooms: MutableList<String>,
        var token: String = blank,
        var verified: Boolean,
        var blacklist: MutableList<String>
): Parcelable {

    fun deepCopy(): User {
        val profilePhotoUris = this.profilePhotoUris.map { it }.toMutableList()
        val friendIds = this.friendIds.map { it }.toMutableList()
        val chatRooms = this.chatRooms.map { it }.toMutableList()
        val blacklist = this.blacklist.map { it }.toMutableList()

        return User (
                uid = this.uid,
                name = this.name,
                profilePhotoUris = profilePhotoUris,
                location = location,
                friendIds = friendIds,
                chatRooms = chatRooms,
                token = token,
                verified = verified,
                blacklist = blacklist
        )
    }

    companion object {
        const val FIELD_UID = "uid"
        const val FIELD_NAME = "name"
        const val FIELD_PROFILE_PHOTO_URI = "profilePhotoUri"
        const val FIELD_LOCATION = "location"
        const val FIELD_FRIEND_IDS = "friendIds"
        const val FIELD_CHAT_ROOMS = "chatRooms"
        const val FIELD_TOKEN = "token"
        const val FIELD_VERIFIED = "verified"
        const val FIELD_BLACKLIST = "blacklist"
    }
}