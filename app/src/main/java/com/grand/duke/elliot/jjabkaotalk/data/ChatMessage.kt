package com.grand.duke.elliot.jjabkaotalk.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChatMessage(
    val message: String,
    val readerIds: MutableList<String>,
    val senderId: String,
    val time: Long
): Parcelable {

    fun deepCopy(): ChatMessage {
        val readerIds = this.readerIds.map { it }.toMutableList()

        return ChatMessage(
                message = this.message,
                readerIds = readerIds,
                senderId = senderId,
                time = time
        )
    }

    companion object {
        const val FIELD_READER_IDS = "readerIds"
        const val FIELD_TIME = "time"
    }
}