package com.grand.duke.elliot.jjabkaotalk.data

data class ChatMessage(
        val message: String,
        val readerIds: MutableList<String>,
        val senderId: String,
        val time: Long
) {
    companion object {
        const val FIELD_READER_IDS = "readerIds"
        const val FIELD_TIME = "time"
    }
}